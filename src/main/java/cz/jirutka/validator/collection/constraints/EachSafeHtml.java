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
package cz.jirutka.validator.collection.constraints;

import cz.jirutka.validator.collection.CommonEachValidator;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.Tag;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.SafeHtml.WhiteListType.RELAXED;

/**
 * @see SafeHtml
 * @see CommonEachValidator
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@EachConstraint(validateAs = SafeHtml.class)
@Constraint(validatedBy = CommonEachValidator.class)
public @interface EachSafeHtml {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return The built-in whitelist type which will be applied to the rich text value
     */
    WhiteListType whitelistType() default RELAXED;

    /**
     * @return Additional whitelist tags which are allowed on top of the tags specified by the
     * {@link #whitelistType()}.
     */
    String[] additionalTags() default { };

    /**
     * @return Allows to specify additional whitelist tags with optional attributes.
     * @since Hibernate Validator 5.1.0
     */
    Tag[] additionalTagsWithAttributes() default { };

    /**
     * @return Base URI used to resolve relative URIs to absolute ones. If not set, validation
     * of HTML containing relative URIs will fail.
     * @since Hibernate Validator 6.0.0
     */
    String baseURI() default "";
}
