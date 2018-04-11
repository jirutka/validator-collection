/*
 * The MIT License
 *
 * Copyright 2013-2016 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.validator.collection.internal;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public abstract class AnnotationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationUtils.class);

    @SuppressWarnings("unchecked")
    public static <T> T readAttribute(Annotation annotation, String name, Class<T> requiredType)
            throws IllegalArgumentException, IllegalStateException  {

        Object result = AnnotationUtils.invokeNonArgMethod(annotation, name);

        Validate.isInstanceOf(requiredType, result,
                "Method %s should return instance of %s", name, requiredType.getSimpleName());

        return (T) result;
    }

    /**
     * Whether the annotation type contains attribute of the specified name.
     */
    public static boolean hasAttribute(Class<? extends Annotation> annotationType, String attributeName) {

        for (Method m : annotationType.getDeclaredMethods()) {
            if (m.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns type of the specified attribute in the annotation type.
     */
    public static Class<?> getAttributeType(Class<? extends Annotation> annotationType, String attributeName) {
        try {
            return annotationType.getDeclaredMethod(attributeName).getReturnType();

        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(String.format(
                    "No such attribute %s in %s", attributeName, annotationType.getName()), ex);
        }
    }

    /**
     * Returns annotation's attributes (name and value) as map.
     */
    public static Map<String, Object> readAllAttributes(Annotation annotation) {
        Map<String, Object> attributes = new HashMap<>();

        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                Object value = method.invoke(annotation);
                attributes.put(method.getName(), value);

            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return attributes;
    }

    /**
     * Creates instance (proxy) of the specified annotation with the given
     * attributes.
     *
     * @param annotationType The annotation's class.
     * @param attributes A map with attribute values for the annotation to be created.
     * @param <T> The type of the annotation.
     *
     * @return An instance of the annotation.
     * @throws IllegalArgumentException if some attribute has wrong type or
     *         a required attribute is missing.
     */
    public static <T extends Annotation> T createAnnotation(Class<T> annotationType, Map<String, Object> attributes) {

        // check if given attributes are defined in annotation
        for (Iterator<String> it = attributes.keySet().iterator(); it.hasNext(); ) {
            String name = it.next();
            Object value = attributes.get(name);

            if (value == null) {
                LOG.warn("Attribute's value must not be null; will be ignored");
                it.remove();
                continue;
            }
            if (!hasAttribute(annotationType, name)) {
                LOG.warn("Annotation {} does not define attribute: {}; will be ignored",
                        annotationType.getName(), name);
                it.remove();
                continue;
            }
            Class<?> attrType = getAttributeType(annotationType, name);

            Validate.isTrue(isAssignable(value.getClass(), attrType),
                    "Attribute '%s' expects %s, but given: %s (%s)",
                    name, attrType.getName(), value, value.getClass().getName());
        }
        // check if required attributes are given
        for (Method m : annotationType.getDeclaredMethods()) {
            Validate.isTrue(attributes.containsKey(m.getName()) || m.getDefaultValue() != null,
                    "Missing required attribute: %s", m.getName());
        }

        return createAnnotationInternal(annotationType, attributes);
    }

    /**
     * Creates an annotation instance using the respective constructor for each HV version.
     * This is required to support both version 4.3-5.X and 6.0.0
     *
     * @param annotationType The annotation's class.
     * @param attributes A map with attribute values for the annotation to be created.
     * @param <T> The type of the annotation.
     *
     * @return An instance of the annotation.
     * @throws IllegalStateException if the required constructor classes or methods are not found.
     */
    public static <T extends Annotation> T createAnnotationInternal(Class<T> annotationType,
            Map<String, Object> attributes) {
        try {
            final Object descriptor;
            final Class<?> annotationDescriptorClass;
            final Class<?> annotationFactoryClass;

            int version = HibernateValidatorInfo.getVersion();
            if (version >= 6_0_0) {
                annotationDescriptorClass =
                    Class.forName("org.hibernate.validator.internal.util.annotation.AnnotationDescriptor");
                annotationFactoryClass =
                    Class.forName("org.hibernate.validator.internal.util.annotation.AnnotationFactory");

                Class<?> annotationDescriptorBuilderClass =
                    Class.forName("org.hibernate.validator.internal.util.annotation.AnnotationDescriptor$Builder");

                final Object descriptorBuilder = annotationDescriptorBuilderClass
                    .getConstructor(Class.class, Map.class)
                    .newInstance(annotationType, attributes);

                descriptor = annotationDescriptorBuilderClass
                    .getMethod("build")
                    .invoke(descriptorBuilder);
            } else {
                annotationDescriptorClass =
                    Class.forName("org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor");
                annotationFactoryClass =
                    Class.forName("org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory");

                descriptor = annotationDescriptorClass
                    .getMethod("getInstance", Class.class, Map.class)
                    .invoke(null, annotationType, attributes);
            }

            return (T) annotationFactoryClass
                .getMethod("create", annotationDescriptorClass)
                .invoke(null, descriptor);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Object invokeNonArgMethod(Object object, String methodName) {
        Class<?> clazz = object.getClass();

        try {
            return clazz.getMethod(methodName).invoke(object);

        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    String.format("Class should declare method %s()", methodName), ex);

        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
