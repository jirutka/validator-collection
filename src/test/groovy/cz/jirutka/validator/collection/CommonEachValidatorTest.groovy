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

import cz.jirutka.validator.collection.fixtures.LegacyEachSize
import spock.lang.Specification

import javax.validation.constraints.Future
import javax.validation.constraints.Size

import static cz.jirutka.validator.collection.TestUtils.createAnnotation

// TODO more tests
class CommonEachValidatorTest extends Specification {

    def validator = new CommonEachValidator()

    def 'readMessageTemplate'() {
        given:
            def expected = 'Allons-y!'
            def anno = createAnnotation(Future, message: expected)
        expect:
            validator.readMessageTemplate(anno) == expected
    }

    def 'unwrapConstraints'() {
        given:
            def expected = [ createAnnotation(Size, min: 10), createAnnotation(Size) ] as Size[]
            def eachAnno = createAnnotation(LegacyEachSize, value: expected)
        expect:
            validator.unwrapConstraints(eachAnno) == expected
    }
}
