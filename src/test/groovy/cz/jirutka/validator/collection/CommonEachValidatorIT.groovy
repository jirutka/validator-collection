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

import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.Validation

import static cz.jirutka.validator.collection.TestUtils.evalClassWithConstraint

@Unroll
class CommonEachValidatorIT extends Specification {

    def validator = Validation.buildDefaultValidatorFactory().getValidator()

    def constraint = null


    def 'validate @Each* on field with #desc'() {
        given:
            constraint = '@EachSize(min=2, max=6)'
        expect:
            assertViolations values, isValid, message
        where:
            values       | desc                    || isValid | message
            ['f', 'ab']  | 'first value invalid'   || false   | 'size must be between 2 and 6'
            ['ab', '']   | 'last value invalid'    || false   | 'size must be between 2 and 6'
            ['foo']      | 'valid value'           || true    | null
            ['ab', 'cd'] | 'valid values'          || true    | null
            []           | 'empty list'            || true    | null
            null         | 'null'                  || true    | null
    }

    def 'validate @Each* used in composite constraint with #desc'() {
        given:
           constraint = '@EachComposite'
        expect:
            assertViolations values, isValid, message
        where:
            values         | desc                        || isValid  | message
            ['f']          | 'value invalid by @Size'    || false    | 'size must be between 2 and 8'
            ['foo', '132'] | 'value invalid by @Pattern' || false    | 'must contain a-z only'
            ['ab', 'cd']   | 'valid values'              || true     | null
    }

    def 'validate legacy @Each* on field with #desc'() {
        given:
            constraint = '@LegacyEachSize(@Size(min=2, max=6))'
        expect:
            assertViolations values, isValid, message
        where:
            values       | desc             || isValid | message
            ['f', 'ab']  | 'invalid value'  || false   | 'size must be between 2 and 6'
            ['ab', 'cd'] | 'valid values'   || true    | null
    }


    //////// Helpers ////////

    def validate(entity) {
        validator.validate(entity)
    }

    void assertViolations(List values, boolean shouldBeValid, String expectedMessage) {
        def entity = evalClassWithConstraint(constraint, values)
        def violations = validate(entity)

        assert violations.isEmpty() == shouldBeValid

        if (!shouldBeValid) {
            assert violations.size() == 1
            assert violations[0].invalidValue == values
            assert violations[0].propertyPath.toString() == 'valuesList'
            assert violations[0].rootBean.is(entity)
            assert violations[0].rootBeanClass == entity.class
            assert violations[0].message == expectedMessage
        }
    }
}
