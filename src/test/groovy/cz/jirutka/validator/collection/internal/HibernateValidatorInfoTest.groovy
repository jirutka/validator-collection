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

import org.hibernate.validator.HibernateValidator
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class HibernateValidatorInfoTest extends Specification {

    public static final String testVersionFile = "test-hibernate-version.txt"

    def 'parse version: #input'() {
        expect:
            HibernateValidatorInfo.parseVersion(input) == expected
        where:
            input            | expected
            '5.1.1.Final'    | 511
            '5.2.0-SNAPSHOT' | 520
            '4.0.0.GA'       | 400
    }

    def 'get version from file: #testVersionFile'() {
        expect:
            HibernateValidatorInfo.getVersionFromFile(testVersionFile) == "5.3.0.Final"
    }

    def 'get version: #file'() {
        expect:
            HibernateValidatorInfo.getVersion(testVersionFile) == 530
    }
}
