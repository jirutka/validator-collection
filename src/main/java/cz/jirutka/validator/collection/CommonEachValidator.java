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

import cz.jirutka.validator.collection.support.LRUCache;
import cz.jirutka.validator.collection.support.ReflectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
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
import java.lang.annotation.ElementType;
import java.lang.reflect.TypeVariable;
import java.util.*;

import static java.util.Collections.EMPTY_MAP;

/**
 * Common validator for collection constraints that validates each element of
 * the given collection.
 */
@SuppressWarnings("unchecked")
public class CommonEachValidator implements ConstraintValidator<Annotation, Collection<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(CommonEachValidator.class);
    private static final ConstraintHelper CONSTRAINT_HELPER = new ConstraintHelper();

    private @Inject ValidatorFactory factory;

    private List<ConstraintDescriptor> descriptors;
    private Map<Class, Class<? extends ConstraintValidator<?, ?>>> validators;
    private Map<Class, ConstraintValidator> validatorInstancesCache;



    public void initialize(Annotation wrapper) {
        LOG.trace("Initializing BaseEachValidator for {}", wrapper.annotationType());

        if (factory == null) {
            LOG.debug("No ValidatorFactory injected, building default one");
            factory = Validation.buildDefaultValidatorFactory();
        }
        Annotation[] constraints = unwrapConstraints(wrapper);
        Validate.notEmpty(constraints, "Wrapper annotation does not contain any constraint");

        // constraints are always of same type
        Class<? extends Annotation> constraintClass = constraints[0].annotationType();

        descriptors = new ArrayList<>(2);
        for (Annotation constraint : constraints) {
            descriptors.add(createConstraintDescriptor(constraint));
        }
        ConstraintDescriptor descriptor = descriptors.get(0);

        validators = categorizeValidatorsByType(descriptor.getConstraintValidatorClasses());
        Validate.notEmpty(validators, "No validator found for constraint: %s", constraintClass.getName());

        validatorInstancesCache = new LRUCache<>(1, 6);
    }

    public boolean isValid(Collection<?> collection, ConstraintValidatorContext context) {
        if (collection == null || collection.isEmpty()) {
            return true;  // nothing for validation here
        }
        context.disableDefaultConstraintViolation();  //do not add wrapper's message

        int index = 0;
        for (Iterator<?> it = collection.iterator(); it.hasNext(); index++) {
            Object element = it.next();
            if (element == null) continue;

            ConstraintValidator validator = getCachedValidator(element.getClass());

            for (ConstraintDescriptor descriptor : descriptors) {
                validator.initialize(descriptor.getAnnotation());

                if (! validator.isValid(element, context)) {
                    LOG.debug("Element [{}] = '{}' is invalid according to: {}",
                            index, element, validator.getClass().getName());

                    String message = createMessage(descriptor, element);
                    context.buildConstraintViolationWithTemplate(message)
                            .addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }

    public void setValidatorFactory(ValidatorFactory factory) {
        this.factory = factory;
    }


    protected Annotation[] unwrapConstraints(Annotation wrapper) {
        return ReflectionUtils.invokeArrayGetter("value", Annotation.class, wrapper);
    }

    /**
     * Note: The <tt>member</tt> argument of the {@link ConstraintDescriptorImpl} constructor is
     * set to <tt>null</tt>, which may cause problems in some specific situations.
     *
     * <p>The problem is that it's not possible to access the validated "member" (field, method,
     * constructor) from the validator context (or just don't know how). However, this "member"
     * argument is currently (HV v5.0.1, 5.1.0) used only to determine type of the constraint in some
     * ambiguous situations (see {@link ConstraintDescriptorImpl#determineConstraintType}).
     * It seems that it's not necessary for "normal" validators and everything works fine. But to
     * be honest, I'm not sure which specific types of validators will fail with this.</p>
     *
     * @param constraint The constraint annotation to create descriptor for.
     */
    protected ConstraintDescriptor createConstraintDescriptor(Annotation constraint) {
        return new ConstraintDescriptorImpl(
                CONSTRAINT_HELPER, null, constraint, ElementType.LOCAL_VARIABLE, ConstraintType.GENERIC);
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
        return validators;
    }

    protected Class<?> determineTargetType(Class<? extends ConstraintValidator<?, ?>> validatorClass) {
        TypeVariable<?> typeVar = ConstraintValidator.class.getTypeParameters()[1];
        return TypeUtils.getRawType(typeVar, validatorClass);
    }


    protected Map<Class, ConstraintValidator> cache() {
        return validatorInstancesCache;
    }

    protected ConstraintValidator getCachedValidator(Class<?> type) {
        if (cache().containsKey(type)) {
            return cache().get(type);
        }
        ConstraintValidator validator = findAndInitializeValidator(type);
        cache().put(type, validator);

        return validator;
    }

    protected ConstraintValidator findAndInitializeValidator(Class<?> type) {
        LOG.trace("Looking for validator for type: {}", type.getName());

        for (Class<?> clazz : validators.keySet()) {
            if (! clazz.isAssignableFrom(type)) continue;

            Class<? extends ConstraintValidator> validatorClass = validators.get(clazz);

            LOG.trace("Initializing validator: {}", validatorClass.getName());
            ConstraintValidator validator = factory.getConstraintValidatorFactory().getInstance(validatorClass);

            return validator;
        }
        throw new IllegalArgumentException("No validator found for type: " + type.getName());
    }


    protected String readMessageTemplate(Annotation constraint) {
        return ReflectionUtils.invokeGetter("message", String.class, constraint);
    }

    protected String createMessage(ConstraintDescriptor descriptor, Object value) {
        Context context = new MessageInterpolatorContext(descriptor, value, Void.class, EMPTY_MAP);
        String template = readMessageTemplate(descriptor.getAnnotation());

        return factory.getMessageInterpolator().interpolate(template, context);
    }

}
