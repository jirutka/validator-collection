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
package cz.jirutka.validator.collection.constraints

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class EachAnnotationTest extends Specification {

    static final CONSTRAINTS = [
            // JSR 303/349
            EachAssertFalse, EachAssertTrue, EachDecimalMax, EachDecimalMin,
            EachDigits, EachFuture, EachMax, EachMin, EachNotNull, EachPast,
            EachPattern, EachSize,
            // Hibernate
            EachCreditCardNumber, EachEAN, EachEmail, EachLength, EachLuhnCheck,
            EachMod10Check, EachMod11Check, EachNotBlank, EachNotEmpty,
            EachRange, EachSafeHtml, EachScriptAssert, EachURL
    ]

    def 'verify that @#name is annotated with @EachConstraint(validateAs = #expValidateAsName)'() {
        expect:
            constraint.isAnnotationPresent(EachConstraint)
        and:
            def validateAs = constraint.getAnnotation(EachConstraint).validateAs()
            constraint.simpleName == /Each${validateAs.simpleName}/
        where:
            constraint << CONSTRAINTS
            name = constraint.simpleName
            expValidateAsName = name.replaceFirst('^Each', '') + '.class'
    }

    def 'verify that #constraint.simpleName contains all attributes from its validateAs constraint'() {
        setup:
           def validateAs = constraint.getAnnotation(EachConstraint).validateAs()
        expect:
            attributesTypesSet(constraint).containsAll attributesTypesSet(validateAs)
        where:
            constraint << CONSTRAINTS
    }


    def attributesTypesSet(Class annotation) {
        annotation.declaredMethods.collect(new HashSet()) { m ->
            [m.name, m.returnType]
        }
    }
}
