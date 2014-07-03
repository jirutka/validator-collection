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
package cz.jirutka.validator.collection;

import cz.jirutka.validator.collection.constraints.EachConstraint;
import cz.jirutka.validator.collection.internal.AnnotationUtils;
import cz.jirutka.validator.collection.internal.ConstraintDescriptorFactory;
import cz.jirutka.validator.collection.internal.MessageInterpolatorContext;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.MessageInterpolator.Context;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cz.jirutka.validator.collection.internal.AnnotationUtils.*;
import static cz.jirutka.validator.collection.internal.ConstraintValidatorContextUtils.addConstraintViolationInIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Common validator for collection constraints that validates each element of
 * the given collection.
 */
@SuppressWarnings("unchecked")
public class CommonEachValidator implements ConstraintValidator<Annotation, Collection<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(CommonEachValidator.class);

    private static final ConstraintDescriptorFactory DESCRIPTOR_FACTORY = ConstraintDescriptorFactory.newInstance();

    // injected by container, or set default during initialization
    private @Inject ValidatorFactory factory;

    // after initialization it's read-only
    private List<ConstraintDescriptor> descriptors;

    // after initialization it's read-only
    private Map<Class, Class<? extends ConstraintValidator<?, ?>>> validators;

    // modifiable after initialization; must be thread-safe!
    private Map<Class, ConstraintValidator> validatorInstances;

    private boolean earlyInterpolate = false;


    public void initialize(Annotation eachAnnotation) {

        Class<? extends Annotation> eachAType = eachAnnotation.annotationType();
        LOG.trace("Initializing CommonEachValidator for {}", eachAType);

        if (factory == null) {
            LOG.debug("No ValidatorFactory injected, building default one");
            factory = Validation.buildDefaultValidatorFactory();
        }
        validatorInstances = new ConcurrentHashMap<>(2);

        if (eachAType.isAnnotationPresent(EachConstraint.class)) {
            Class constraintClass = eachAType.getAnnotation(EachConstraint.class).validateAs();

            Annotation constraint = createConstraintAndCopyAttributes(constraintClass, eachAnnotation);
            ConstraintDescriptor descriptor = createConstraintDescriptor(constraint);

            descriptors = unmodifiableList(asList(descriptor));

        // legacy
        } else if (isWrapperAnnotation(eachAType)) {
            Annotation[] constraints = unwrapConstraints(eachAnnotation);
            Validate.notEmpty(constraints, "%s annotation does not contain any constraint", eachAType);

            List<ConstraintDescriptor> list = new ArrayList<>(constraints.length);
            for (Annotation constraint : constraints) {
                list.add( createConstraintDescriptor(constraint) );
            }
            descriptors = unmodifiableList(list);

            this.earlyInterpolate = true;

        } else {
            throw new IllegalArgumentException(String.format(
                    "%s is not annotated with @EachConstraint and doesn't declare 'value' of type Annotation[] either.",
                    eachAType.getName()));
        }
        // constraints are always of the same type, so just pick first
        ConstraintDescriptor descriptor = descriptors.get(0);

        validators = categorizeValidatorsByType(descriptor.getConstraintValidatorClasses());
        Validate.notEmpty(validators,
                "No validator found for constraint: %s", descriptor.getAnnotation().annotationType());
    }

    public boolean isValid(Collection<?> collection, ConstraintValidatorContext context) {
        if (collection == null || collection.isEmpty()) {
            return true;  //nothing to validate here
        }
        context.disableDefaultConstraintViolation();  //do not add wrapper's message

        int index = 0;
        for (Iterator<?> it = collection.iterator(); it.hasNext(); index++) {
            Object element = it.next();

            ConstraintValidator validator = element != null
                    ? getValidatorInstance(element.getClass())
                    : getAnyValidatorInstance();

            for (ConstraintDescriptor descriptor : descriptors) {
                validator.initialize(descriptor.getAnnotation());

                if (! validator.isValid(element, context)) {
                    LOG.debug("Element [{}] = '{}' is invalid according to: {}",
                            index, element, validator.getClass().getName());

                    String message;

                    if (this.earlyInterpolate) {
                        message = createMessage(descriptor, element);
                    } else {
                        message = getMessageTemplate(descriptor);
                    }
                    addConstraintViolationInIterable(context, message, index);
                    return false;
                }
            }
        }
        return true;
    }

    public void setValidatorFactory(ValidatorFactory factory) {
        this.factory = factory;
    }


    /**
     * Whether the given annotation type contains the {@code value} attribute
     * of the type that extends {@code Annotation[]}.
     */
    protected boolean isWrapperAnnotation(Class<? extends Annotation> annotationType) {
        return hasAttribute(annotationType, "value")
                && Annotation[].class.isAssignableFrom(getAttributeType(annotationType, "value"));
    }

    protected Annotation[] unwrapConstraints(Annotation wrapper) {
        return AnnotationUtils.readAttribute(wrapper, "value", Annotation[].class);
    }

    protected ConstraintDescriptor createConstraintDescriptor(Annotation constraint) {
        return DESCRIPTOR_FACTORY.buildConstraintDescriptor(constraint);
    }

    protected <T extends ConstraintValidator<?, ?>>
            Map<Class, Class<? extends T>> categorizeValidatorsByType(List<Class<? extends T>> validatorClasses) {

        Map<Class, Class<? extends T>> validators = new LinkedHashMap<>(10);

        for (Class<? extends T> validator : validatorClasses) {
            Class<?> type = determineTargetType(validator);
            if (type.isArray()) continue;

            LOG.trace("Found validator {} for type {}", validator.getName(), type.getName());
            validators.put(type, validator);
        }
        return unmodifiableMap(validators);
    }

    protected Class<?> determineTargetType(Class<? extends ConstraintValidator<?, ?>> validatorClass) {
        TypeVariable<?> typeVar = ConstraintValidator.class.getTypeParameters()[1];
        return TypeUtils.getRawType(typeVar, validatorClass);
    }

    /**
     * Returns initialized validator instance for the specified object type.
     * Instances are cached.
     *
     * @param type Type of the object to be validated.
     */
    protected ConstraintValidator getValidatorInstance(Class<?> type) {
        ConstraintValidator validator = validatorInstances.get(type);

        if (validator == null) {
            validator = findAndInitializeValidator(type);
            validatorInstances.put(type, validator);
        }
        return validator;
    }

    /**
     * Returns initialized validator instance for any object type. This is used
     * when the object to be validated is <tt>null</tt> so we can't determine
     * it's type. Instances are cached.
     */
    protected ConstraintValidator getAnyValidatorInstance() {

        if (validatorInstances.isEmpty()) {
            Class type = validators.keySet().iterator().next();
            return findAndInitializeValidator(type);

        } else {
            return validatorInstances.values().iterator().next();
        }
    }

    protected ConstraintValidator findAndInitializeValidator(Class<?> type) {
        LOG.trace("Looking for validator for type: {}", type.getName());

        for (Class<?> clazz : validators.keySet()) {
            if (! clazz.isAssignableFrom(type)) continue;

            Class validatorClass = validators.get(clazz);

            LOG.trace("Initializing validator: {}", validatorClass.getName());
            return factory.getConstraintValidatorFactory().getInstance(validatorClass);
        }
        throw new IllegalArgumentException("No validator found for type: " + type.getName());
    }

    /**
     * Reads and interpolates an error message for the given constraint and
     * value.
     *
     * @param descriptor Descriptor of the constraint that the value violated.
     * @param value The validated value.
     * @return An interpolated message.
     */
    protected String createMessage(ConstraintDescriptor descriptor, Object value) {
        Context context = new MessageInterpolatorContext(descriptor, value);

        Annotation constraint = descriptor.getAnnotation();
        String template = AnnotationUtils.readAttribute(constraint, "message", String.class);

        return factory.getMessageInterpolator().interpolate(template, context);
    }
    /**
     * Reads the error message template for the given constraint and
     * value.
     *
     * @param descriptor Descriptor of the constraint that the value violated.
     * @return A message template.
     */
    protected String getMessageTemplate(ConstraintDescriptor descriptor) {
        Annotation constraint = descriptor.getAnnotation();
        return AnnotationUtils.readAttribute(constraint, "message", String.class);
    }

    /**
     * Instantiates constraint of the specified type and copies values of all
     * the common attributes from the given source constraint (of any type)
     * to it.
     *
     * <p>If the source constraint's {@code message} is empty, then it will
     * <b>not</b> copy it (so the default {@code message} of the target
     * constraint will be preserved).</p>
     *
     * @param constraintType Type of the constraint to create.
     * @param source Any annotation to copy attribute values from.
     * @return An instance of the specified constraint.
     */
    protected <T extends Annotation> T createConstraintAndCopyAttributes(Class<T> constraintType, Annotation source) {
        Map<String, Object> attributes = readAllAttributes(source);

        // if message is not set, keep message from original constraint instead
        if (isEmpty((String) attributes.get("message"))) {
            attributes.remove("message");
        }
        return createAnnotation(constraintType, attributes);
    }
}
