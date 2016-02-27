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

import javax.validation.ConstraintValidatorContext;

public abstract class ConstraintValidatorContextUtils {

    private static final int HV_VERSION = HibernateValidatorInfo.getVersion();

    /**
     * Builds and adds a constraint violation inside an iterable value to the
     * given {@code ConstraintValidatorContext}. If running with Hibernate
     * Validator 5.x, then it also registers index of the violated value.
     *
     * @param context The Constraint validator context.
     * @param message The interpolated error message.
     * @param index Index of the invalid value inside a list (ignored on HV 4.x).
     */
    public static void addConstraintViolationInIterable(ConstraintValidatorContext context, String message, int index) {
        if (HV_VERSION >= 5_0_0) {
            context.buildConstraintViolationWithTemplate(message)
                    .addBeanNode()
                    .inIterable()
                    .atIndex(index)
                    .addConstraintViolation();
        } else {
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
        }
    }
}
