/**
 * 
 */
package org.apache.commons.lang3.reflect;

import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtendedFieldUtilsTest extends AbstractTestSupport {
	public ExtendedFieldUtilsTest() {
		super();
	}

	@Test
	public void testGetDeclaredField() {
		assertNotNull("Cannot locate field", ExtendedFieldUtils.getDeclaredField(TestClass.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE));
		assertNull("Unexpected located field", ExtendedFieldUtils.getDeclaredField(TestClassExtension.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE));
	}

	@Test
	public void testGetDeclaredFieldWithMismatchedNameOrType() {
		assertNull("Unexpected located mis-named field",
				ExtendedFieldUtils.getDeclaredField(TestClass.class, TestClass.FIELD_NAME + "Blah", TestClass.FIELD_TYPE));
		assertNull("Unexpected located mis-typed field",
				ExtendedFieldUtils.getDeclaredField(TestClass.class, TestClass.FIELD_NAME + "Blah", getClass()));
	}

	@Test
	public void testGetField() {
		assertNotNull("Cannot locate direct field", ExtendedFieldUtils.getField(TestClass.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE));
		assertNotNull("Cannot locate derived field", ExtendedFieldUtils.getField(TestClassExtension.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE));
		assertNotNull("Cannot locate parent field", ExtendedFieldUtils.getField(TestClassSuperparent.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE));
	}

	@Test
	public void testGetFieldWithMismatchedNameOrType() {
		assertNull("Unexpected located mis-named field",
				ExtendedFieldUtils.getField(TestClass.class, TestClass.FIELD_NAME + "Blah", TestClass.FIELD_TYPE));
		assertNull("Unexpected located mis-typed field",
				ExtendedFieldUtils.getField(TestClass.class, TestClass.FIELD_NAME + "Blah", getClass()));
	}

	@Test
	public void testGetFieldWithStopClass() {
		assertNull("Unexpected located direct field",
			ExtendedFieldUtils.getField(TestClass.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE, TestClass.class));
		assertNull("Unexpected located derived field",
				ExtendedFieldUtils.getField(TestClassExtension.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE, TestClass.class));
		assertNull("Unexpected located parent field",
				ExtendedFieldUtils.getField(TestClassSuperparent.class, TestClass.FIELD_NAME, TestClass.FIELD_TYPE, TestClassExtension.class));
	}

	static class TestClass {
		static final Class<?>	FIELD_TYPE=Integer.TYPE;
		static final String		FIELD_NAME="intField";

		private int	intField;
		
		TestClass(int value) {
			intField = value;
		}
		
		int getValue() {
			return intField;
		}
	}
	
	static class TestClassExtension extends TestClass {
		TestClassExtension() {
			super(-1);
		}
	}
	
	static class TestClassSuperparent extends TestClassExtension {
		TestClassSuperparent() {
			super();
		}
	}
}
