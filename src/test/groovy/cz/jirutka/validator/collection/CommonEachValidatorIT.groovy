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
package cz.jirutka.validator.collection

import cz.jirutka.validator.collection.internal.HibernateValidatorInfo
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

import static cz.jirutka.validator.collection.TestUtils.evalClassWithConstraint
import static cz.jirutka.validator.collection.TestUtils.validate

@Unroll
class CommonEachValidatorIT extends Specification {

    static {
        Locale.setDefault(new Locale("en", "US"))
    }

    static HV_VERSION = HibernateValidatorInfo.getVersion()

    def constraint = null


    def 'validate @EachX for common constraint [ #desc ]'() {
        given:
            constraint = '@EachSize(min=2, max=6)'
        expect:
            assertViolations values, isValid, invalidIndex, 'size must be between 2 and 6'
        where:
            values       | desc                     || isValid | invalidIndex
            ['f', 'ab']  | 'first value invalid'    || false   | 0
            ['ab', '']   | 'last value invalid'     || false   | 1
            ['foo']      | 'valid value'            || true    | null
            ['ab', 'cd'] | 'valid values'           || true    | null
            [null, 'ab'] | 'valid values with null' || true    | null
            []           | 'empty list'             || true    | null
            null         | 'null'                   || true    | null
    }

    def 'validate composite constraint with two @EachX [ #desc ]'() {
        given:
            constraint = '@EachComposite'
        expect:
            assertViolations values, isValid, invalidIndex, message
        where:
            values         | desc                            || isValid | invalidIndex | message
            ['f']          | 'value invalid by first cons.'  || false   | 0            | 'size must be between 2 and 8'
            ['foo', '132'] | 'value invalid by second cons.' || false   | 1            | 'must contain a-z only'
            ['ab', 'cd']   | 'valid values'                  || true    | null         | null
    }

    def 'validate @EachX for constraint that validates nulls [ #desc ]'() {
        given:
            constraint = '@EachNotNull'
        expect:
            assertViolations values, isValid, 1, (HV_VERSION >= 6_0_0 ? 'must' : 'may') + ' not be null'
        where:
            values      | desc              || isValid
            ['a', null] | 'a null value'    || false
            ['a', 'b']  | 'not null values' || true
    }

    @Ignore('should be fixed!') @Issue('#8')
    def 'validate @EachX for pure composite constraint [ #desc ]'() {
        given:
            constraint = '@EachRange(min=16L, max=64L)'
        expect:
            assertViolations values, isValid, 0, 'must be between 16 and 64'
        where:
            values | desc            || isValid
            [6]    | 'invalid value' || false
            [42]   | 'valid value'   || true
    }

    @Ignore('should be fixed!') @Issue('#8')
    def 'validate @EachX for half-composite constraint [ #desc ]'() {
        given:
            constraint = '@EachNotBlank'
        expect:
            assertViolations values, isValid, invalidIndex, 'may not be empty'
        where:
            values        | desc                               || isValid | invalidIndex
            ['']          | 'value invalid by top validator'   || false   | 0
            ['foo', null] | 'value invalid by composite cons.' || false   | 1
            ['allons-y!'] | 'valid value'                      || true    | null
    }

    @Ignore('should be fixed!') @Issue('#8')
    def 'validate @EachX for constraint that uses @OverridesAttribute [ #desc value ]'() {
        given:
            constraint = '@EachURL(protocol="https", regexp=".*[^x]$")'
        expect:
            assertViolations values, isValid, 0, 'must be a valid URL'
        where:
            values                  | desc                            || isValid
            ['http://fit.cvut.cz']  | "invalid normal attribute's"    || false
            ['https://fit.cvut.cx'] | "invalid overrided attribute's" || false
            ['https://fit.cvut.cz'] | 'valid'                         || true
    }

    def 'validate @EachX with custom message template'() {
        given:
            constraint = '@EachURL(protocol="https", message="must be a valid URL with {protocol}")'
        expect:
            assertViolations(['http://fit.cvut.cz'], false, 0, 'must be a valid URL with https')
    }

    def 'validate legacy @EachX constraint [ #desc ]'() {
        given:
            constraint = '@LegacyEachSize(@Size(min=2, max=6))'
        expect:
            assertViolations values, isValid, 0, 'size must be between 2 and 6'
        where:
            values       | desc             || isValid
            ['f', 'ab']  | 'invalid value'  || false
            ['ab', 'cd'] | 'valid values'   || true
    }


    //////// Helpers ////////

    void assertViolations(Object value, boolean shouldBeValid, Integer invalidIndex, String expectedMessage) {
        def entity = evalClassWithConstraint(constraint, value)
        def propertyPath = HV_VERSION >= 5_0_0 ? "valuesList[${invalidIndex}]" : 'valuesList'
        def violations = validate(entity)

        assert violations.isEmpty() == shouldBeValid

        if (!shouldBeValid) {
            assert violations.size() == 1
            assert violations[0].invalidValue == value
            assert violations[0].propertyPath.toString() == propertyPath
            assert violations[0].rootBean.is(entity)
            assert violations[0].rootBeanClass == entity.class
            assert violations[0].message == expectedMessage
        }
    }
}
