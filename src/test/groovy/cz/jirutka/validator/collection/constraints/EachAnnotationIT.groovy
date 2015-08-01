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
package cz.jirutka.validator.collection.constraints

import cz.jirutka.validator.collection.internal.HibernateValidatorInfo
import org.apache.commons.lang3.ClassUtils
import org.hibernate.validator.constraints.CreditCardNumber
import org.hibernate.validator.constraints.NotEmpty
import org.hibernate.validator.constraints.Range
import spock.lang.Specification
import spock.lang.Unroll

import static cz.jirutka.validator.collection.TestUtils.evalClassWithConstraint
import static cz.jirutka.validator.collection.TestUtils.validate

@Unroll
class EachAnnotationIT extends Specification {

    static final HV_VERSION = HibernateValidatorInfo.getVersion()

    // List of @Each* annotations for constraints defined in JSR 303/349.
    static final CONSTRAINTS_JSR = [
            EachAssertFalse, EachAssertTrue, EachDecimalMax, EachDecimalMin,
            EachDigits, EachFuture, EachMax, EachMin, EachNotNull, EachPast,
            EachPattern, EachSize
    ]

    // List of @Each* annotations for Hibernate constraints in HV 4.3.0.
    static final CONSTRAINTS_HV = [
            EachEmail, EachLength, EachNotBlank, EachNotEmpty, EachRange,
            EachScriptAssert, EachURL
    ]

    // List of @Each* annotations for Hibernate constraints in HV 5.1.0 and newer.
    static final CONSTRAINTS_5_1_0 = [
            EachCreditCardNumber, EachEAN, EachLuhnCheck, EachMod10Check,
            EachMod11Check, EachSafeHtml
    ]

    // List of @Each* annotations which are only a composition of other @Each* annotations.
    static final COMPOSITE_CONSTRAINTS = [
            EachCreditCardNumber, EachNotEmpty, EachRange
    ]


    def 'verify that @#name is annotated with @EachConstraint(validateAs = #expValidateAsName)'() {
        expect:
            constraint.isAnnotationPresent(EachConstraint)
        and:
            def validateAs = constraint.getAnnotation(EachConstraint).validateAs()
            constraint.simpleName == /Each${validateAs.simpleName}/
        where:
            constraint << (eachConstraints - COMPOSITE_CONSTRAINTS)
            name = constraint.simpleName
            expValidateAsName = name.replaceFirst('^Each', '') + '.class'
    }

    def 'verify that #constraint.simpleName defines same attributes as its validateAs constraint'() {
        setup:
           def validateAs = constraint.getAnnotation(EachConstraint).validateAs()
        expect:
            attributesTypesSet(constraint).containsAll attributesTypesSet(validateAs)
        where:
            constraint << (eachConstraints - COMPOSITE_CONSTRAINTS)
    }

    def 'verify that @#constraint.simpleName defines same attributes as #validateAs.simpleName'() {
        setup:
            // skip test for constraints that doesn't work in the current HV version
            if (!eachConstraints.contains(constraint)) return
        expect:
            attributesTypesSet(constraint).containsAll attributesTypesSet(validateAs)
        where:
            constraint           | validateAs
            EachNotEmpty         | NotEmpty
            EachRange            | Range
            EachCreditCardNumber | CreditCardNumber
    }

    def 'validate @#constraintName on collection of #valuesType'() {
        setup:
            // skip test for constraints that doesn't work in the current HV version
            if (!eachConstraints.contains(constraint)) return
        and:
            def validEntity = evalClassWithConstraint(constraint, attributes, validValue)
            def invalidEntity = evalClassWithConstraint(constraint, attributes, invalidValue)
        expect:
            validate(validEntity).empty
            ! validate(invalidEntity).empty
        where:
            constraint      | attributes                | validValue         | invalidValue
            EachAssertFalse | [:]                       | [false, false]     | [false, true]
            EachAssertTrue  | [:]                       | [true, true]       | [true, false]
            EachCreditCardNumber | [:]                  | ['79927398713']    | ['79927398714']
            EachDecimalMax  | [value: '3']              | [1, 2, 3]          | [2, 3, 4]
            EachDecimalMax  | [value: '3']              | ['1', '2', '3']    | ['2', '3', '4']
            EachDecimalMin  | [value: '3']              | [3, 4, 5]          | [2, 3, 4]
            EachDecimalMin  | [value: '3']              | ['3', '4', '5']    | ['2', '3', '4']
            EachDigits      | [integer: 2, fraction: 1] | [42.1, 13.2]       | [42.1, 3.14]
            EachDigits      | [integer: 2, fraction: 1] | ['42.1', '13.2']   | ['42.1', '3.14']
            EachEAN         | [:]                       | ['1234567890128']  | ['1234567890128', '66']
            EachEmail       | [:]                       | ['x@y.z', 'a@b.c'] | ['x@y.z', 'ab.c']
            EachFuture      | [:]                       | [futureDate()]     | [pastDate()]
            EachLength      | [min: 1, max: 3]          | ['a', 'foo']       | ['a', 'allons-y!']
            EachLuhnCheck   | [:]                       | ['79927398713']    | ['79927398714']
            EachMax         | [value: 3L]               | [1, 2, 3]          | [2, 3, 4]
            EachMax         | [value: 3L]               | ['1', '2', '3']    | ['2', '3', '4']
            EachMin         | [value: 3L]               | [3, 4, 5]          | [1, 2, 3]
            EachMin         | [value: 3L]               | ['3', '4', '5']    | ['1', '2', '3']
            EachMod10Check  | [:]                       | ['123']            | ['123', '124']
            EachMod11Check  | [:]                       | ['124']            | ['124', '125']
            EachNotBlank    | [:]                       | ['foo', 'bar']     | ['foo', '']
            EachNotEmpty    | [:]                       | ['x', 'yz']        | ['x', '']
            EachNotEmpty    | [:]                       | [[1], [2, 3]]      | [[1], []]
            EachNotEmpty    | [:]                       | [[a: 1], [b: 2]]   | [[a: 1], [:]]
            EachNotNull     | [:]                       | ['foo', 'bar']     | ['foo', null]
            EachPast        | [:]                       | [pastDate()]       | [futureDate()]
            EachPattern     | [regexp: '[A-Z]+']        | ['FOO', 'BAR']     | ['FOO', '123']
            EachRange       | [min: 3L, max: 6L]        | [3, 4, 5]          | [6, 7, 8]
            EachRange       | [min: 3L, max: 6L]        | ['3', '4', '5']    | ['6', '7', '8']
            EachSafeHtml    | [:]                       | ['<b>foo</b>']     | ['<x>WAT?</x>']
            EachSize        | [min: 1, max: 2]          | ['a', 'xy']        | ['a', 'foo']
            EachSize        | [min: 1, max: 2]          | [[1], [2, 3]]      | [[1], [2, 3, 4]]
            EachSize        | [min: 1, max: 2]          | [[a: 1], [b: 2]]   | [[a: 1], [:]]
            EachURL         | [protocol: 'https']       | ['https://nic.cz'] | ['http://nic.cz']

            constraintName = ClassUtils.getSimpleName(constraint)  // using ClassUtils to avoid NoClassDefFoundError
            valuesType = validValue[0].getClass().simpleName + 's'
    }


    //////// Helpers ////////

    static getEachConstraints() {
        (CONSTRAINTS_JSR + CONSTRAINTS_HV + (HV_VERSION >= 5_1_0 ? CONSTRAINTS_5_1_0 : [])).toSet()
    }

    def attributesTypesSet(Class annotation) {
        annotation.declaredMethods.collect(new HashSet()) { m ->
            [m.name, m.returnType]
        }
    }

    def futureDate() {
        new Date().plus(1)
    }

    def pastDate() {
        new Date().minus(1)
    }
}
