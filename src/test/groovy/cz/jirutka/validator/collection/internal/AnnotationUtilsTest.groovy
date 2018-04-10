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
package cz.jirutka.validator.collection.internal

import cz.jirutka.validator.collection.TestUtils
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

import static cz.jirutka.validator.collection.internal.AnnotationUtils.*

class AnnotationUtilsTest extends Specification {


    //////// readAttribute() ////////

    def "readAttribute: return attribute's value"() {
        given:
            def attrs = value != null ? [(name): value] : [:]
            def annotation = createAnnotationInternal(Size, attrs)
        expect:
            readAttribute(annotation, name, reqType) == expected
        where:
            name      | reqType  | value               || expected
            'min'     | Integer  | 1                   || 1
            'message' | String   | null                || '{javax.validation.constraints.Size.message}'
            'groups'  | Class[]  | [String] as Class[] || [String]
    }

    @Unroll
    def "readAttribute: throw IllegalArgumentException when attribute #reason"() {
        given:
            def annotation = TestUtils.createAnnotation(NotNull)
        when:
            readAttribute(annotation, attrName, attrType)
        then:
            thrown IllegalArgumentException
        where:
            attrName  | attrType | reason
            'foo'     | String   | "doesn't exist"
            'message' | Integer  | "isn't instance of required type"
    }


    //////// hasAttribute() ////////

    @Unroll
    def 'hasAttribute: return #expected for #desc attribute'() {
        expect:
            hasAttribute(Size, name) == expected
        where:
            name      | expected
            'min'     | true
            'foo'     | false

            desc = expected ? 'existing' : 'undefined'
    }


    //////// readAllAttributes() ////////

    def 'readAllAttributes: return attributes as map'() {
        given:
            def attrs = [message: 'allons-y!', max: 10]
            def annotation = createAnnotationInternal(Size, attrs)
            def expected = [groups: [], payload: [], min: 0] + attrs
        expect:
            readAllAttributes(annotation) == expected
    }


    //////// createAnnotation() ////////

    def 'createAnnotation: create annotation when given valid attributes'() {
        given:
            def attributes = [min: 42, message: 'allons-y!']
        when:
            def actual = createAnnotation(Size, attributes)
        then:
            actual instanceof Size
            actual.min()     == attributes['min']
            actual.message() == attributes['message']
    }

    @Unroll
    def 'createAnnotation: create annotation and ignore #desc'() {
        when:
            def actual = createAnnotation(Size, attributes)
        then:
            actual instanceof Size
            actual.message() == '{javax.validation.constraints.Size.message}'
        where:
            attributes        | desc
            [undefined: 666]  | 'undefined attribute'
            [message: null]   | 'attribute with null'
    }

    def 'createAnnotation: throw IllegalArgumentException when wrong attribute type'() {
        when:
            createAnnotation(Size, [min: 'fail'])
        then:
            def ex = thrown(IllegalArgumentException)
            ex.message == "Attribute 'min' expects int, but given: fail (java.lang.String)"
    }

    def 'createAnnotation: throw IllegalArgumentException when omit required attribute'() {
        when:
            createAnnotation(Pattern, [message: 'fail'])
        then:
            def ex = thrown(IllegalArgumentException)
            ex.message == 'Missing required attribute: regexp'
    }
}
