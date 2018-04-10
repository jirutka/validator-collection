/*
 * The MIT License
 *
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
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

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;

import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

/**
 * Factory that builds instances of {@link ConstraintDescriptorImpl}.
 *
 * <p>This is a workaround to support multiple versions of Hibernate Validator;
 * 4.3.0 and greater. The problem is that {@code ConstraintDescriptorImpl} is
 * internal class of the Hibernate Validator and its constructor's signature
 * has been several times changed between major versions. The class implements
 * public interface {@link ConstraintDescriptor}, but its implementation is
 * pretty complex (547 LOC), it seems that it can't be reimplemented simpler
 * and I hate code copy&pasting...</p>
 */
public abstract class ConstraintDescriptorFactory {

    private static final ConstraintHelper CONSTRAINT_HELPER = new ConstraintHelper();

    protected final Constructor<ConstraintDescriptorImpl> constructor;


    private ConstraintDescriptorFactory() {
        try {
            this.constructor = ConstraintDescriptorImpl.class.getConstructor(getConstructorArguments());
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new instance of factory for the version of Hibernate Validator
     * detected on classpath.
     */
    public static ConstraintDescriptorFactory newInstance() {

        int version = HibernateValidatorInfo.getVersion();

        if (version >= 6_0_0) {
            try {
                final Class<?> descriptorClass
                    = Class.forName("org.hibernate.validator.internal.util.annotation.AnnotationDescriptor");
                return new ConstraintDescriptorFactory() {
                    Class[] getConstructorArguments() {
                        return new Class[]{ConstraintHelper.class, Member.class, descriptorClass, ElementType.class};
                    }
                    ConstraintDescriptorImpl newInstance(Annotation annotation) throws ReflectiveOperationException {
                        final Object descriptor = descriptorClass.getConstructor(Annotation.class).newInstance(annotation);
                        return constructor.newInstance(CONSTRAINT_HELPER, null, descriptor, ElementType.LOCAL_VARIABLE);
                    }
                };
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        } else if (version >= 5_1_0) {
            return new ConstraintDescriptorFactory() {
                Class[] getConstructorArguments() {
                    return new Class[]{ ConstraintHelper.class, Member.class, Annotation.class, ElementType.class };
                }
                ConstraintDescriptorImpl newInstance(Annotation annotation) throws ReflectiveOperationException {
                    return constructor.newInstance(CONSTRAINT_HELPER, null, annotation, ElementType.LOCAL_VARIABLE);
                }
            };
        } else if (version >= 5_0_0) {
            return new ConstraintDescriptorFactory() {
                Class[] getConstructorArguments() {
                    return new Class[]{ Annotation.class, ConstraintHelper.class, Class.class,
                                        ElementType.class, ConstraintOrigin.class, Member.class };
                }
                ConstraintDescriptorImpl newInstance(Annotation annotation) throws ReflectiveOperationException {
                    return constructor.newInstance(annotation, CONSTRAINT_HELPER, null, ElementType.LOCAL_VARIABLE,
                                                   ConstraintOrigin.DEFINED_LOCALLY, null);
                }
            };
        } else if (version >= 4_3_0) {
            return new ConstraintDescriptorFactory() {
                Class[] getConstructorArguments() {
                    return new Class[]{ Annotation.class, ConstraintHelper.class, Class.class,
                                        ElementType.class, ConstraintOrigin.class };
                }
                ConstraintDescriptorImpl newInstance(Annotation annotation) throws ReflectiveOperationException {
                    return constructor.newInstance(annotation, CONSTRAINT_HELPER, null, ElementType.LOCAL_VARIABLE,
                                                   ConstraintOrigin.DEFINED_LOCALLY);
                }
            };
        } else {
            throw new UnsupportedVersionException("Hibernate Validator older then 4.3.0 is not supported");
        }
    }

    /**
     * @param annotation The constraint annotation.
     * @return An instance of {@link ConstraintDescriptorImpl} for the given
     *         constraint.
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> ConstraintDescriptor<T> buildConstraintDescriptor(T annotation) {
        try {
            return newInstance(annotation);

        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    //////// Abstract methods ////////

    abstract ConstraintDescriptorImpl newInstance(Annotation annotation) throws ReflectiveOperationException;

    abstract Class[] getConstructorArguments();



    //////// Inner classes ////////

    public static class UnsupportedVersionException extends IllegalStateException {

        public UnsupportedVersionException(String message) {
            super(message);
        }
    }
}
