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
import org.hibernate.validator.constraints.Mod10Check;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @since Hibernate Validator 5.1.0
 * @see Mod10Check
 * @see CommonEachValidator
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@EachConstraint(validateAs = Mod10Check.class)
@Constraint(validatedBy = CommonEachValidator.class)
public @interface EachMod10Check {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return The multiplier to be used for odd digits when calculating the Mod10 checksum.
     */
    int multiplier() default 3;

    /**
     * @return The weight to be used for even digits when calculating the Mod10 checksum.
     */
    int weight() default 1;

    /**
     * @return the start index (inclusive) for calculating the checksum. If not specified 0 is assumed.
     */
    int startIndex() default 0;

    /**
     * @return the end index (inclusive) for calculating the checksum. If not specified the whole value is considered.
     */
    int endIndex() default Integer.MAX_VALUE;

    /**
     * @return The index of the check digit in the input. Per default it is assumed that the check digit is the last
     * digit of the specified range. If set, the digit at the specified index is used. If set
     * the following must hold true:<br/>
     * {@code checkDigitIndex > 0 && (checkDigitIndex < startIndex || checkDigitIndex >= endIndex}.
     */
    int checkDigitIndex() default -1;

    /**
     * @return Whether non-digit characters in the validated input should be ignored ({@code true}) or result in a
     * validation error ({@code false}).
     */
    boolean ignoreNonDigitCharacters() default true;
}
