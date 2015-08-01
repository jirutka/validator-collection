/*
 * The MIT License
 *
 * Copyright 2013-2015 Jakub Jirutka <jakub@jirutka.cz>.
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

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Note: Constraint {@link org.hibernate.validator.constraints.CreditCardNumber CreditCardNumber}
 * was available even in Hibernate Validator 4.3.1, but it was composed from now deprecated
 * {@link org.hibernate.validator.constraints.ModCheck ModCheck} instead of
 * {@link org.hibernate.validator.constraints.LuhnCheck LuhnCheck}.Therefore this annotation
 * doesn't work in HV older than 5.1.0.
 *
 * @since Hibernate Validator 5.1.0
 * @see org.hibernate.validator.constraints.CreditCardNumber
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@EachLuhnCheck
@ReportAsSingleViolation
@Constraint(validatedBy = { })
public @interface EachCreditCardNumber {

    String message() default "{org.hibernate.validator.constraints.CreditCardNumber.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return Whether non-digit characters in the validated input should be ignored ({@code true}) or result in a
     * validation error ({@code false}). Default is {@code false}
     *
     * @since Hibernate Validator 5.1.2
     */
    @OverridesAttribute(constraint = EachLuhnCheck.class, name = "ignoreNonDigitCharacters")
    boolean ignoreNonDigitCharacters() default false;
}
