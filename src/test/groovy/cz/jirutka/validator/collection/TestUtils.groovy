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

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory

class TestUtils {

    static evalClassWithConstraint(annotationLine, List values) {
        def value = values ? "[ ${values.collect{"'$it'"}.join(',')} ]" : null

        def template = """
            import cz.jirutka.validator.collection.constraints.*
            import cz.jirutka.validator.collection.fixtures.*
            import javax.validation.constraints.*

            class TestMock {
                ${annotationLine}
                public List valuesList = $value
            }
        """
        new GroovyClassLoader().parseClass(template).newInstance()
    }

    static createAnnotation(Class annotationType, Map attributes) {
        createAnnotation(attributes, annotationType)
    }

    static createAnnotation(Map attributes=[:], Class annotationType) {
        def desc = AnnotationDescriptor.getInstance(annotationType, attributes)
        AnnotationFactory.create(desc)
    }
}
