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
package cz.jirutka.validator.collection

import cz.jirutka.validator.collection.internal.HibernateValidatorInfo
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

import static cz.jirutka.validator.collection.TestUtils.evalClassWithConstraint

@Unroll
class CommonEachValidatorIT extends Specification {

    static HV_VERSION = HibernateValidatorInfo.getVersion()

    def validator = Validation.buildDefaultValidatorFactory().getValidator()

    def constraint = null


    def 'validate @EachSize on field with #desc'() {
        given:
            constraint = '@EachSize(min=2, max=6)'
        expect:
            assertViolations values, isValid, invalidIndex, message
        where:
            values       | desc                     || isValid | invalidIndex | message
            ['f', 'ab']  | 'first value invalid'    || false   | 0            | 'size must be between 2 and 6'
            ['ab', '']   | 'last value invalid'     || false   | 1            | 'size must be between 2 and 6'
            ['foo']      | 'valid value'            || true    | null         | null
            ['ab', 'cd'] | 'valid values'           || true    | null         | null
            [null, 'ab'] | 'valid values with null' || true    | null         | null
            []           | 'empty list'             || true    | null         | null
            null         | 'null'                   || true    | null         | null
    }

    def 'validate @EachSize @EachPattern used in composite constraint with #desc'() {
        given:
            constraint = '@EachComposite'
        expect:
            assertViolations values, isValid, invalidIndex, message
        where:
            values         | desc                        || isValid  | invalidIndex | message
            ['f']          | 'value invalid by @Size'    || false    | 0            | 'size must be between 2 and 8'
            ['foo', '132'] | 'value invalid by @Pattern' || false    | 1            | 'must contain a-z only'
            ['ab', 'cd']   | 'valid values'              || true     | null         | null
    }

    def 'validate @LegacyEachSize on field with #desc'() {
        given:
            constraint = '@LegacyEachSize(@Size(min=2, max=6))'
        expect:
            assertViolations values, isValid, invalidIndex, message
        where:
            values       | desc             || isValid | invalidIndex | message
            ['f', 'ab']  | 'invalid value'  || false   | 0            | 'size must be between 2 and 6'
            ['ab', 'cd'] | 'valid values'   || true    | null         | null
    }

    def 'validate @EachNotNull on field with #desc'() {
        given:
            constraint = '@EachNotNull'
        expect:
            assertViolations values, isValid, invalidIndex, message
        where:
            values      | desc              || isValid | invalidIndex | message
            ['a', null] | 'a null value'    || false   | 1            | 'may not be null'
            ['a', 'b']  | 'not null values' || true    | null         | null
    }

    def 'validate @EachURL with custom message template'() {
        given:
            constraint = '@EachURL(protocol="https", message="must be a valid URL with {protocol}")'
        expect:
            assertViolations(['http://fit.cvut.cz'], false, 0, 'must be a valid URL with https')
    }


    //////// Helpers ////////

    def validate(entity) {
        validator.validate(entity)
    }

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
