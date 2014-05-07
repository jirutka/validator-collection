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

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

import static cz.jirutka.validator.collection.internal.AnnotationUtils.readAttribute

class AnnotationUtilsTest extends Specification {

    def "readAttribute: return attribute's value"() {
        setup:
            def attrs = value != null ? [(name): value] : [:]
            def annotation = createAnnotation(Size, attrs)
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
            def annotation = createAnnotation(NotNull)
        when:
            readAttribute(annotation, attrName, attrType)
        then:
            thrown IllegalArgumentException
        where:
            attrName  | attrType | reason
            'foo'     | String   | "doesn't exist"
            'message' | Integer  | 'is not instance of required type'
    }


    def createAnnotation(annotationType, attributes=[:]) {
        def desc = AnnotationDescriptor.getInstance(annotationType, attributes)
        AnnotationFactory.create(desc)
    }
}
