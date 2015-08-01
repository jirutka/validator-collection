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

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory

import javax.validation.Validation

class TestUtils {

    static evalClassWithConstraint(annotationLine, List values) {
        def value = values ? "[ ${values.collect{ toLiteral(it) }.join(',')} ]" : null

        def template = """
            import cz.jirutka.validator.collection.constraints.*
            import cz.jirutka.validator.collection.fixtures.*
            import javax.validation.constraints.*

            class TestMock {
                ${annotationLine.replace('"', "'")}
                public List valuesList = $value
            }
        """
        new GroovyClassLoader().parseClass(template).newInstance()
    }

    static evalClassWithConstraint(Class annotationType, Map attributes, List values) {
        evalClassWithConstraint(createAnnotationString(annotationType, attributes), values)
    }

    static toLiteral(value) {
        switch (value) {
            case null    : return null
            case String  : return "'${value.toString()}'"
            case Long    : return "${value}L"
            case List    : return '[' + value.collect { toLiteral(it) }.join(', ') + ']'
            case Map     : return '[' + value.collect { k, v -> "${k}: ${ toLiteral(v) }" }.join(',') + ']'
            case Enum    : return "${value.declaringClass.name}.${value.name()}"
            case Date    : return "new Date(${toLiteral(value.time)})"
            default      : return String.valueOf(value)
        }
    }

    static createAnnotation(Class annotationType, Map attributes) {
        createAnnotation(attributes, annotationType)
    }

    static createAnnotation(Map attributes=[:], Class annotationType) {
        def desc = AnnotationDescriptor.getInstance(annotationType, attributes)
        AnnotationFactory.create(desc)
    }

    static createAnnotationString(Class annotationType, Map attributes) {
        def attrsLine = attributes.collect { k, v -> "${k}=${toLiteral(v)}" }.join(', ')
        "@${annotationType.name}(${attrsLine})"
    }

    static validate(entity) {
        Validation.buildDefaultValidatorFactory().validator.validate(entity)
    }
}
