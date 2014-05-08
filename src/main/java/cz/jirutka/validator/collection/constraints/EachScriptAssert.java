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
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @see ScriptAssert
 * @see CommonEachValidator
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@EachConstraint(validateAs = ScriptAssert.class)
@Constraint(validatedBy = CommonEachValidator.class)
public @interface EachScriptAssert {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return The name of the script language used by this constraint as
     *         expected by the JSR 223 {@link javax.script.ScriptEngineManager}. A
     *         {@link javax.validation.ConstraintDeclarationException} will be thrown upon script
     *         evaluation, if no engine for the given language could be found.
     */
    String lang();

    /**
     * @return The script to be executed. The script must return
     *         <code>Boolean.TRUE</code>, if the annotated element could
     *         successfully be validated, otherwise <code>Boolean.FALSE</code>.
     *         Returning null or any type other than Boolean will cause a
     *         {@link javax.validation.ConstraintDeclarationException} upon validation. Any
     *         exception occurring during script evaluation will be wrapped into
     *         a ConstraintDeclarationException, too. Within the script, the
     *         validated object can be accessed from the {@link javax.script.ScriptContext
     *         script context} using the name specified in the
     *         <code>alias</code> attribute.
     */
    String script();

    /**
     * @return The name, under which the annotated element shall be registered
     *         within the script context. Defaults to "_this".
     */
    String alias() default "_this";
}
