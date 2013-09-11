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

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;

/**
 * @author lgoldstein
 *
 */
public class ExtendedFieldUtils extends FieldUtils {
	public ExtendedFieldUtils() {
		super();
	}

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied <code>name</code> <U>and</U> {@link Class type}.
     * @param clazz the class to introspect
     * @param name the name of the field
     * @param type the type of the field
     * @return the corresponding Field object, or <code>null</code> if not found
     * @throws IllegalArgumentException if no name or type specified
     */
    public static final Field getDeclaredField(Class<?> clazz, String name, Class<?> type) {
    	return getDeclaredField(clazz, name, type, false);
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied <code>name</code> <U>and</U> {@link Class type}.
     * @param clazz the class to introspect
     * @param name the name of the field
     * @param type the type of the field
     * @param makeAccessible whether to allow any visibility to access the field
     * @return the corresponding Field object, or <code>null</code> if not found
     * @throws IllegalArgumentException if no name or type specified
     */
    public static final Field getDeclaredField(Class<?> clazz, String name, Class<?> type, boolean makeAccessible) {
    	if (StringUtils.isEmpty(name) || (type == null)) {
    		throw new IllegalArgumentException("getField(" + clazz + ")[" + name + "]@" + type + " - incomplete specification");
    	}

    	/*
    	 * NOTE: we do not invoke the FieldUtils#getDeclaredField since
    	 * it returns null if makeAccessible=false and field is not accessible
    	 */
    	final Field	field;
    	try {
            field = clazz.getDeclaredField(name);
	    } catch (NoSuchFieldException e) { // NOPMD
	        return null;
	    }
    	
    	if (!type.isAssignableFrom(field.getType())) {
    		return null;
    	}

    	if (makeAccessible && (!MemberUtils.isAccessible(field))) {
    		field.setAccessible(true);
    	}

    	return field;
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied <code>name</code> <U>and</U> {@link Class type}. Searches all superclasses
     * up to {@link Object}.
     * @param clazz the class to introspect
     * @param name the name of the field
     * @param type the type of the field
     * @return the corresponding Field object, or <code>null</code> if not found
     * @throws IllegalArgumentException if no name or type specified
     * @see #getField(Class, String, Class, boolean)
     */
    public static final Field getField(Class<?> clazz, String name, Class<?> type) {
    	return getField(clazz, name, type, false);
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied <code>name</code> <U>and</U> {@link Class type}. Searches all superclasses
     * up to the {@link Object}
     * @param clazz the class to introspect
     * @param name the name of the field
     * @param type the type of the field
     * @param makeAccessible whether to allow any visibility to access the field
     * @return the corresponding Field object, or <code>null</code> if not found
     * @throws IllegalArgumentException if no name, type or stop class specified
     * @see #getField(Class, String, Class, Class, boolean)
     */
    public static final Field getField(Class<?> clazz, String name, Class<?> type, boolean makeAccessible) {
    	return getField(clazz, name, type, Object.class, makeAccessible);
    }
    
    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied <code>name</code> <U>and</U> {@link Class type}. Searches all superclasses
     * up to specified stop class (but not the stop class itself)
     * @param clazz the class to introspect
     * @param name the name of the field
     * @param type the type of the field
     * @param stopClass the {@link Class} where to stop climbing up the hierarchy.
     * <B>Note:</B> the stop class itself is <U>not</U> checked for the field
     * @return the corresponding Field object, or <code>null</code> if not found
     * @throws IllegalArgumentException if no name, type or stop class specified
     * @see #getField(Class, String, Class, Class, boolean)
     */
    public static final Field getField(Class<?> clazz, String name, Class<?> type, Class<?> stopClass) {
    	return getField(clazz, name, type, stopClass, false);
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied <code>name</code> <U>and</U> {@link Class type}. Searches all superclasses
     * up to specified stop class (but not the stop class itself)
     * @param clazz the class to introspect
     * @param name the name of the field
     * @param type the type of the field
     * @param stopClass the {@link Class} where to stop climbing up the hierarchy.
     * <B>Note:</B> the stop class itself is <U>not</U> checked for the field
     * @param makeAccessible whether to allow any visibility to access the field
     * @return the corresponding Field object, or <code>null</code> if not found
     * @throws IllegalArgumentException if no name, type or stop class specified
     */
    public static final Field getField(Class<?> clazz, String name, Class<?> type, Class<?> stopClass, boolean makeAccessible) {
    	if (StringUtils.isEmpty(name) || (type == null) || (stopClass == null)) {
    		throw new IllegalArgumentException("getField(" + clazz + ")[" + name + "]@" + type + " < " + stopClass
    										 + " - incomplete specification");
    	}

        Class<?> searchType = clazz;
        while (!stopClass.equals(searchType) && (searchType != null)) {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                if (name.equals(field.getName()) && type.isAssignableFrom(field.getType())) {
                	if (makeAccessible && (!MemberUtils.isAccessible(field))) {
                		field.setAccessible(true);
                	}
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

	public static final <T> T readStaticTypedField(Class<?> cls, String fieldName, Class<T> valType) throws IllegalAccessException {
        return readStaticTypedField(cls, fieldName, valType, false);
    }

    /**
     * Reads the named static field. Superclasses will be considered.
     * @param cls  the class to reflect, must not be null
     * @param fieldName  the field name to obtain
     * @param valType The expected field value type
     * @param forceAccess  whether to break scope restrictions using the
     *  <code>setAccessible</code> method. <code>False</code> will only
     *  match public fields.
     * @return the Field object
     * @throws IllegalArgumentException if the class is null, the field name is null or if the field could not be found
     * @throws IllegalAccessException if the field is not made accessible
     */
    public static final <T> T readStaticTypedField(Class<?> cls, String fieldName, Class<T> valType, boolean forceAccess) throws IllegalAccessException {
	    Object	value=readStaticField(cls, fieldName, forceAccess);
	    if (value == null) {
	    	return null;
	    } else {
	    	return valType.cast(value);
	    }
	}

    public static final <T> T readStaticTypedField(Field field, Class<T> valType) throws IllegalAccessException {
        return readStaticTypedField(field, valType, false);
    }

    /**
     * Reads a static Field.
     * @param field to read
     * @param valType The expected field value type
     * @param forceAccess  whether to break scope restrictions using the
     *  <code>setAccessible</code> method.
     * @return the field value
     * @throws IllegalArgumentException if the field is null or not static
     * @throws IllegalAccessException if the field is not made accessible
     */
    public static final <T> T readStaticTypedField(Field field, Class<T> valType, boolean forceAccess) throws IllegalAccessException {
        Object	value=readStaticField(field, forceAccess);
        if (value == null) {
        	return null;
        } else {
        	return valType.cast(value);
        }
    }

    public static final <T> T readTypedField(Field field, Object target, Class<T> valType) throws IllegalAccessException {
        return readTypedField(field, target, valType, false);
    }

    /**
     * Reads a {@link Field} and casts its value.
     * @param field  the field to use
     * @param target  the object to call on, may be null for static fields
     * @param valType The expected field value type
     * @param forceAccess  whether to break scope restrictions using the
     *  <code>setAccessible</code> method.
     * @return the field value
     * @throws IllegalArgumentException if the field is null
     * @throws IllegalAccessException if the field is not made accessible
     */
    public static final <T> T readTypedField(Field field, Object target, Class<T> valType, boolean forceAccess) throws IllegalAccessException {
        Object	value=readField(field, target, forceAccess);
        if (value == null) {
        	return null;
        } else {
        	return valType.cast(value);
        }
    }
}
