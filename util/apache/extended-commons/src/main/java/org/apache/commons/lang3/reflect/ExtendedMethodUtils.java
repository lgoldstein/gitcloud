/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.lang3.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;
import org.apache.commons.lang3.math.ExtendedNumberUtils;


/**
 * @author lgoldstein
 *
 */
public class ExtendedMethodUtils extends MethodUtils {

    public static final Method[] EMPTY_METHOD_ARRAY = {};
    
	public ExtendedMethodUtils() {
		super();
	}

	public static final Method getAccessibleMethod(Class<?> cls, String methodName) {
		return getAccessibleMethod(cls, methodName, ArrayUtils.EMPTY_CLASS_ARRAY);
	}

	public static final <T> T retrieveTypedStaticValue(Method method, Class<T> valueType) {
		return retrieveTypedStaticValue(method, valueType, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	public static final <T> T retrieveTypedStaticValue(Method method, Class<T> valueType, Object ... args) {
		return valueType.cast(invokeStatic(method, args));
	}

	@SuppressWarnings("cast")
    public static final Object invokeStatic(Method method) {
		return invokeStatic(method, (Object[]) ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	public static final Object invokeStatic(Method method, Object ... args) {
		return invoke(method, null, args);
	}

	public static final Object invoke(Method method, Object target) {
		return invoke(method, target, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	public static final <T> T retrieveTypedValue(Method method, Object target, Class<T> valueType) {
		return retrieveTypedValue(method, target, valueType, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	public static final <T> T retrieveTypedValue(Method method, Object target, Class<T> valueType, Object ... args) {
		return valueType.cast(invoke(method, target, args));
	}

	public static final Object invoke(Method method, Object target, Object ... args) {
		try {
			return method.invoke(target, args);
		} catch(Exception e) {
			throw ExtendedExceptionUtils.toRuntimeException(e, true);
		}
	}
	
	/**
	 * An {@link ExtendedTransformer} that returns a {@link Method#getReturnType()}
	 */
	@SuppressWarnings("rawtypes")
    public static final ExtendedTransformer<Method,Class>   RETURN_TYPE_EXTRACTOR=
	        new AbstractExtendedTransformer<Method, Class>(Method.class, Class.class) {
                @Override
                public Class<?> transform(Method m) {
                    if (m == null) {
                        return null;
                    } else {
                        return m.getReturnType();
                    }
                }
            };
    
	/**
	 * Compares the signature of 2 {@link Method}s as follows:
	 * <UL>
	 * 		<LI>The one with less parameters comes first</LI>
	 * 		<LI>The one with the lexicographical parameter's fully-qualified class name order comes first</LI> 
	 * </UL>
	 * @see Method#getParameterTypes()
	 */
	public static final Comparator<Method>	BY_SIGNATURE_COMPARATOR=
			new Comparator<Method>() {
				@Override
				public int compare(Method o1, Method o2) {
					if (o1 == o2) {
						return 0;
					}
					
					Class<?>[]	p1=(o1 == null) ? null : o1.getParameterTypes();
					Class<?>[]	p2=(o2 == null) ? null : o2.getParameterTypes();
					int			l1=ExtendedArrayUtils.length(p1);
					int			l2=ExtendedArrayUtils.length(p2);
					int			nRes=ExtendedNumberUtils.signOf(l1 - l2);
					if (nRes != 0) {
						return nRes;
					}
					
					for (int index=0; index < l1; index++) {
						Class<?>	c1=p1[index], c2=p2[index];
						if ((nRes=ExtendedClassUtils.BY_FULL_NAME_COMPARATOR.compare(c1, c2)) != 0) {
							return nRes;
						}
					}
					
					return 0;
				}
		};

    /**
     * Compares 2 {@link Method}s as follows:
     * <UL>
     *      <LI>First check the declaring class</LI>
     *      <LI>Then check the name</LI>
     *      <LI>Then check the signature</LI>
     * </UL>
     * @see ExtendedMemberUtils#BY_FQCN_COMPARATOR
     * @see #BY_SIGNATURE_COMPARATOR
     */
    public static final Comparator<Method>  BY_FULL_SIGNATURE_COMPARATOR=
            new Comparator<Method>() {
                @Override
                public int compare(Method o1, Method o2) {
                    if (o1 == o2) {
                        return 0;
                    }

                    int nRes=ExtendedMemberUtils.BY_FQCN_COMPARATOR.compare(o1, o2);
                    if (nRes != 0) {
                        return nRes;
                    }
                    
                    if ((nRes=BY_SIGNATURE_COMPARATOR.compare(o1, o2)) != 0) {
                        return nRes;
                    }

                    /*
                     * NOTE: no need to check the return type since if 2 methods
                     * have the same declaring class, name and signature they MUST
                     * be the same as per the JLS (in other words, 2 methods cannot
                     * differ only by their return type)
                     */
                    return 0;
                }
            };

	public static final ExtendedPredicate<Method> matchingSignaturePredicate(String name, Class<?> returnType) {
	    return matchingSignaturePredicate(name, returnType, ArrayUtils.EMPTY_CLASS_ARRAY);
	}

    public static final ExtendedPredicate<Method> matchingSignaturePredicate(final String name, final Class<?> returnType, final Class<?> ... params) {
        if (StringUtils.isEmpty(name) || (returnType == null)) {
            throw new IllegalArgumentException("Incomplete specification");
        }
        
        return new AbstractExtendedPredicate<Method>(Method.class) {
            @Override
            public boolean evaluate(Method m) {
                if (m == null) {
                    return false;
                } else {
                    return isMatchingSignature(m, name, returnType, params);
                }
            }
        };
    }
	/**
     * Checks if a given method matches a required signature
     * @param m The {@link Method} to test - ignored if <code>null</code>
     * @param name The expected name - ignored if <code>null</code>/empty
     * @param returnType The return type - ignored if <code>null</code>
     * @return <code>true</code> if matching signature (allowing for co-variant return)
     * @see #isMatchingSignature(Method, String, Class, Class...)
     */
    public static final boolean isMatchingSignature(Method m, String name, Class<?> returnType) {
        return isMatchingSignature(m, name, returnType, ArrayUtils.EMPTY_CLASS_ARRAY);
    }

    /**
     * Checks if a given method matches a required signature
     * @param m The {@link Method} to test - ignored if <code>null</code>
     * @param name The expected name - ignored if <code>null</code>/empty
     * @param returnType The return type - ignored if <code>null</code>
     * @param params The expected parameters
     * @return <code>true</code> if matching signature (allowing for co-variant return)
     */
    public static final boolean isMatchingSignature(Method m, String name, Class<?> returnType, Class<?> ... params) {
        if ((m == null) || StringUtils.isEmpty(name) || (returnType == null)) {
            return false;
        }
        
        if (!name.equals(m.getName())) {
            return false;
        }
        
        // we allow for co-variant return type
        if (!returnType.isAssignableFrom(m.getReturnType())) {
            return false;
        }
        
        Class<?>[]    argTypes=m.getParameterTypes();
        if (!Arrays.equals((params == null) ? ArrayUtils.EMPTY_CLASS_ARRAY : params, argTypes)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * A {@link Transformer} that returns a class'es declared methods
     * @see Class#getDeclaredMethods()
     */
    public static final Transformer<Class<?>, Method[]> DECLARED_METHODS_TRANSFORMER = new Transformer<Class<?>, Method[]>() {
            @Override
            public Method[] transform(Class<?> input) {
                if (input == null) {
                    return ExtendedMethodUtils.EMPTY_METHOD_ARRAY;
                } else {
                    return input.getDeclaredMethods();
                }
            }
        };

    /**
     * A {@link Transformer} that returns a class'es methods
     * @see Class#getMethods()
     */
    public static final Transformer<Class<?>, Method[]> CLASS_METHODS_TRANSFORMER = new Transformer<Class<?>, Method[]>() {
            @Override
            public Method[] transform(Class<?> input) {
                if (input == null) {
                    return ExtendedMethodUtils.EMPTY_METHOD_ARRAY;
                } else {
                    return input.getMethods();
                }
            }
        };
}
