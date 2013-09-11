/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.apache.commons.test.AbstractTestSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * @author lgoldstein
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClosedWriterTest extends AbstractTestSupport {
	public ClosedWriterTest() {
		super();
	}

	@Test
	public void testWritingMethods() {
		ClosedWriter	writer=new ClosedWriter();
		for (Method m : Writer.class.getMethods()) {
			String	name=m.getName();
			if (name.startsWith("write")  || name.startsWith("append")) {
				Object[]	args=createTestArgs(name, m.getParameterTypes());
				try {
					Object	retval=m.invoke(writer, args);
					fail(name + ": unexpected success: " + retval);
				} catch(Exception t) {
					Throwable	e=ExtendedExceptionUtils.peelThrowable(t);
					assertObjectInstanceof(name + ": Unexpected thrown exception", IOException.class, e);
				}
			}
		}
	}
	
	@SuppressWarnings("cast")
    @Test
	public void testNonWritingMethods() {
		ClosedWriter	writer=new ClosedWriter();
		for (Method m : Writer.class.getMethods()) {
			String	name=m.getName();
			if (name.startsWith("flush") || name.startsWith("close")) {
				Object[]	args=createTestArgs(name, m.getParameterTypes());
				try {
					m.invoke(writer, (Object[]) args);
				} catch(Exception t) {
					Throwable	e=ExtendedExceptionUtils.peelThrowable(t);
					fail(name + ": Unexpected thrown exception (" + e.getClass().getSimpleName() + "): " + e.getMessage());
				}
			}
		}
	}

	private static Object[] createTestArgs(String methodName, Class<?> ... params) {
		if (ExtendedArrayUtils.length(params) <= 0) {
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}

		Object[]	args=new Object[params.length];
		for (int index=0; index < params.length; index++) {
			Class<?>	pType=params[index];
			if (Integer.TYPE.isAssignableFrom(pType)) {
				args[index] = Integer.valueOf(0);
			} else if (CharSequence.class.isAssignableFrom(pType)) {
				args[index] = String.valueOf(index);
			} else if (char[].class.isAssignableFrom(pType)) {
				args[index] = new char[] { 'r', 'o', 'y', 'l' };
			} else if (Character.TYPE.isAssignableFrom(pType)) {
				args[index] = Character.valueOf((char) ('a' + index));
			} else {
				fail(methodName + ": Unknown parameter type: " + pType.getSimpleName());
			}
		}
		
		return args;
	}
}
