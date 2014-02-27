package cz.jirutka.validator.collection.constraint;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

public class CollectionElementsNotEmptyTest {

    @Test
    public void test() throws Exception {
	Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	List<String> data = new ArrayList<>();
	data.add("one");
	data.add("two");
	TestClass testedObject = new TestClass(data);

	assertEquals(0, validator.validateProperty(testedObject, "list").size());

	testedObject.list.add("");
	assertEquals(1, validator.validateProperty(testedObject, "list").size());
    }

    private static class TestClass {
	@CollectionElementsNotEmpty
	public List<String> list;

	public TestClass(List<String> list) {
	    this.list = list;
	}
    }
}
