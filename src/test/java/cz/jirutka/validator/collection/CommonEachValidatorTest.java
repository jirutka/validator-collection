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
package cz.jirutka.validator.collection;

import cz.jirutka.validator.collection.constraints.EachSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import java.util.List;

import static cz.jirutka.validator.collection.TestUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * TODO incomplete!
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
@RunWith(Enclosed.class)
public class CommonEachValidatorTest {

    public static class _should_validate_successfully {

        public @Test void given_valid_values() {
            Mock entity = new Mock() {
                @EachSize(@Size(min = 0))
                List<String> getList() { return asList("f", "o", "o"); }
            };
            assertValid(entity);
        }

        public @Test void given_empty_list() {
            Mock entity = new Mock() {
                @EachSize(@Size(min = 10))
                List<String> getList() { return asList(); }
            };
            assertValid(entity);
        }

        public @Test void given_null() {
            Mock entity = new Mock() {
                @EachSize(@Size(min = 10))
                List<String> getList() { return null; }
            };
            assertValid(entity);
        }
    }


    public static class _should_violate_constraint {

        public @Test void given_invalid_values() {
            Mock entity = new Mock() {
                @EachSize(@Size(min = 10))
                List<String> getList() { return asList("f", "o", "o"); }
            };
            assertInvalid(entity);
        }

        public @Test void given_invalid_value_at_end() {
            Mock entity = new Mock() {
                @EachSize(@Size(max = 2))
                List<String> getList() { return asList("f", "o", "ooooo"); }
            };
            assertInvalid(entity);
        }
    }


    public static class _violation_attributes {

        private Validator validator;
        private Mock entity;
        private List<String> values = asList("f", "o", "o");

        public @Before void initialize() {
            validator = getValidatorFactory().getValidator();
            entity = new Mock() {
                @EachSize(@Size(min = 2, max = 10, message = "between {min} and {max}"))
                List<String> getList() { return values; }
            };
        }

        public @Test void invalidValue() {
            for (ConstraintViolation violation : validator.validate(entity)) {
                assertEquals(values, violation.getInvalidValue());
            }
        }

        public @Test void message() {
            for (ConstraintViolation violation : validator.validate(entity)) {
                assertEquals("between 2 and 10", violation.getMessage());
            }
        }

        public @Test void propertyPath() {
            for (ConstraintViolation violation : validator.validate(entity)) {
                assertEquals("list", violation.getPropertyPath().toString());
            }
        }

        public @Test void rootBean() {
            for (ConstraintViolation violation : validator.validate(entity)) {
                assertSame(entity, violation.getRootBean());
            }
        }

        public @Test void rootBeanClass() {
            for (ConstraintViolation violation : validator.validate(entity)) {
                assertSame(entity.getClass(), violation.getRootBeanClass());
            }
        }
    }

}
