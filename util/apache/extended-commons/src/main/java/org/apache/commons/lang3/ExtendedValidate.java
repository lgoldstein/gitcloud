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

package org.apache.commons.lang3;

/**
 * @author Lyor G.
 * @since Jun 25, 2013 8:58:44 AM
 */
public class ExtendedValidate extends Validate {
    public ExtendedValidate () {
        super();
    }

    /**
     * Validate that the argument condition is {@code true}; otherwise
     * throwing an {@link IllegalArgumentException} with the specified message.
     * @param value Value to be evaluated
     * @param msg The message to be generated for the exception
     */
    public static final void isTrue(boolean value, String msg) {
        if (!value) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Validate that the argument condition is {@code true}; otherwise
     * throwing an {@link IllegalArgumentException} with the specified message.
     * @param value Value to be evaluated
     * @param msg The message format to be generated for the exception
     * @param obj The object argument for the {@link String#format(String, Object...)} call
     */
    public static final void isTrue(boolean value, String msg, Object obj) {
        if (!value) {
            throw new IllegalArgumentException(String.format(msg, obj));
        }
    }

    /**
     * Validate that the specified argument is not {@code null},
     * otherwise throwing a {@link NullPointerException} with the
     * specified message.
     * @param object Object to be evaluated
     * @param message Message to be attached if {@code null} object
     * @return Same as input object if not {@code null}
     */
    public static final <T> T notNull(T object, String message) {
        if (object != null) {
            return object;
        }
        
        throw new NullPointerException(message);
    }
    
    /**
     * Validate that the {@link CharSequence} argument is non-empty,
     * otherwise throwing an exception
     * @param chars The {@link CharSequence} to be examined
     * @param message The message to attach to the thrown exception(s)
     * @return Same as input sequence if non-empty
     * @throws NullPointerException if the character sequence is {@code null}
     * @throws IllegalArgumentException if the character sequence is empty
     */
    public static final <T extends CharSequence> T notEmpty(T chars, String message) {
        if (chars == null) {
            throw new NullPointerException(message);
        }

        if (chars.length() <= 0) {
            throw new IllegalArgumentException(message);
        }

        return chars;
    }
    
    /**
     * @param type The expected object type
     * @param obj The actual object
     * @param message The message to be placed in the thrown exception
     * @throws IllegalArgumentException if the object is {@code null}
     * or not an instance of the expected type
     */
    public static final void isInstanceOf(Class<?> type, Object obj, String message) {
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * @param superType The expected object type
     * @param type The actual object type
     * @param message The message to be placed in the thrown exception
     * @throws IllegalArgumentException if the super type is not assignable
     * from the type
     */
    public static final void isAssignableFrom(Class<?> superType, Class<?> type, String message) {
        if (!superType.isAssignableFrom(type)) {
            throw new IllegalArgumentException(message);
        }
    }
}
