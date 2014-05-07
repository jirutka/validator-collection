/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
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
package cz.jirutka.validator.collection.internal;

import org.apache.commons.lang3.Validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public abstract class AnnotationUtils {

    @SuppressWarnings("unchecked")
    public static <T> T readAttribute(Annotation annotation, String name, Class<T> returnType)
            throws IllegalArgumentException, IllegalStateException  {

        Object result = AnnotationUtils.invokeNonArgMethod(annotation, name);

        Validate.isInstanceOf(returnType, result,
                "Method %s should return instance of %s", name, returnType.getSimpleName());

        return (T) result;
    }

    private static Object invokeNonArgMethod(Object object, String methodName) {
        Class<?> clazz = object.getClass();

        try {
            return clazz.getMethod(methodName).invoke(object);

        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException(String.format("Class should declare method %s()", methodName));

        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
