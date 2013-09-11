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

package org.apache.commons.lang3.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

import org.apache.commons.collections15.Factory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExtendedExceptionUtils;

/**
 * @author lgoldstein
 *
 */
public class ExtendedConstructorUtils extends ConstructorUtils {
	/**
	 * Default static (final) method used by {@link #getInstance(Class)}
	 */
	public static final String	DEFAULT_INSTANCE_METHOD="getInstance";

	public ExtendedConstructorUtils() {
		super();
	}

	public static <T> Constructor<T> getAccessibleDefaultConstructor(Class<T> cls) {
		return getAccessibleConstructor(cls, ArrayUtils.EMPTY_CLASS_ARRAY);
	}

    /**
     * Attempts to instantiate a give class by 1st looking for a <U>static</U>
     * instantiation method. If such a method is found, then it is invoked,
     * otherwise {@link Class#newInstance()} is called
     * @param cls The {@link Class} type to be instantiated
     * @return The instance value
     * @see #DEFAULT_INSTANCE_METHOD
     * @see #getInstance(Class, String)
     */
	public static <T> T getInstance(Class<T> cls) {
		return getInstance(cls, DEFAULT_INSTANCE_METHOD);
	}

    /**
     * Attempts to instantiate a give class by 1st looking for a (static)
     * instantiation method. If such a method is found, then it is invoked,
     * otherwise the default constructor (whatever its visibility) is invoked.
     * @param clazz The {@link Class} type to be instantiated
     * @param methodName The (<code>public static <U>final</U></code>) method
     * name to look for as instantiator. <B>Note:</B> if method is found but
     * is not <code>public</code> or <code>static</code> or <code>final</code>,
     * or the invocation result is <code>null</code> then exception is thrown.
     * @return The instance value
     * @see #getInstance(Class, String, Class)
     */
    public static <T> T getInstance (Class<T> clazz, String methodName) {
    	return getInstance(clazz, methodName, clazz);
    }

    /**
     * Attempts to instantiate a give class by 1st looking for a (static)
     * instantiation method. If such a method is found, then it is invoked,
     * otherwise {@link Class#newInstance()} is called
     * @param clazz The {@link Class} type to be instantiated
     * @param targetType The target type to which the result should be cast
     * @return The instance value
     * @see #getInstance(Class, String, Class)
     */
    public static <T> T getInstance (Class<?> clazz, Class<T> targetType) {
    	return getInstance(clazz, DEFAULT_INSTANCE_METHOD, targetType);
    }

    /**
     * Attempts to instantiate a give class by 1st looking for a (static)
     * instantiation method. If such a method is found, then it is invoked,
     * otherwise the default constructor (whatever its visibility) is invoked.
     * @param clazz The {@link Class} type to be instantiated
     * @param methodName The (<code>public static <U>final</U></code>) method
     * name to look for as instantiator. <B>Note:</B> if method is found but
     * is not <code>public</code> or <code>static</code> or <code>final</code>,
     * or the invocation result is <code>null</code> then exception is thrown.
     * @param targetType The target type to which the result should be cast
     * @return The instance value
     * @throws ClassCastException if the target type is not compatible with the
     * instantiated class to begin with
     * @throws IllegalStateException if the instantiation method is non-public,
     * non-static or non-final or the returned value from the instantiation
     * method is <code>null</code>
     * @throws NoSuchElementException if no instantiation method found, and no
     * default constructor either
     */
    public static <T> T getInstance (Class<?> clazz, String methodName, Class<T> targetType) {
    	// do it now - even before the instance is created
    	if (!targetType.isAssignableFrom(clazz)) {
    		throw new ClassCastException("getInstance(" + clazz.getName() + ")#" + methodName + " cannot be cast to: " + targetType.getName());
    	}

    	Method	instanceMethod=MethodUtils.getAccessibleMethod(clazz, methodName, ArrayUtils.EMPTY_CLASS_ARRAY);
    	final Object	instanceValue;
    	if (instanceMethod != null) {
        	int	mod=instanceMethod.getModifiers();
        	if (!Modifier.isPublic(mod)) {
        		throw new IllegalStateException("getInstance(" + clazz.getName() + ")#" + methodName + " - not public");
        	}

        	if (!Modifier.isStatic(mod)) {
        		throw new IllegalStateException("getInstance(" + clazz.getName() + ")#" + methodName + " - not static");
        	}

        	if ((instanceValue=ExtendedMethodUtils.invokeStatic(instanceMethod)) == null) {
        		throw new IllegalStateException("getInstance(" + clazz.getName() + ")#" + methodName + " - result is null");
        	}
    	} else {
    		Constructor<?>	ctor=getAccessibleDefaultConstructor(clazz);
    		if (ctor == null) {
    			throw new NoSuchElementException("getInstance(" + clazz.getName() + ")#" + methodName + " no default constructor");
    		}

    		instanceValue = newInstance(ctor);
    	}

    	if (instanceValue == null) {
    		return null;
    	} else {
    		return targetType.cast(instanceValue);
    	}
    }

    /**
     * @param cls The class to be instantiated
     * @return A {@link Factory} that invokes {@link #newInstance(Class)} when
     * its <code>create</code> method is invoked
     */
    public static final <T> Factory<T> newInstanceFactory(final Class<T> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("No class to instantiate");
        }
        
        return new Factory<T>() {
            @Override
            public T create() {
                return newInstance(cls);
            }
            
        };
    }

	public static final <T> T newInstance(Class<T> cls) {
		try {
			return cls.newInstance();
		} catch(Exception e) {
			throw ExtendedExceptionUtils.toRuntimeException(e, true);
		}
	}

	public static final <T> T newInstance(Class<T> cls, Object... args) {
		try {
			return invokeConstructor(cls, args);
		} catch(Exception e) {
			throw ExtendedExceptionUtils.toRuntimeException(e, true);
		}
	}

	public static <T> T newInstance(Class<T> cls, Object[] args, Class<?>[] parameterTypes) {
		try {
			return invokeConstructor(cls, args, parameterTypes);
		} catch(Exception e) {
			throw ExtendedExceptionUtils.toRuntimeException(e, true);
		}
	}

	public static final <T> T newInstance(Constructor<T> ctor) {
		return newInstance(ctor, ArrayUtils.EMPTY_OBJECT_ARRAY);
	}

	public static final <T> T newInstance(Constructor<T> ctor, Object ... args) {
		try {
			return ctor.newInstance(args);
		} catch(Exception e) {
			throw ExtendedExceptionUtils.toRuntimeException(e, true);
		}
	}
}
