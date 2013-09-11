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

package org.apache.commons.collections15;

import java.util.Comparator;

/**
 * @author Lyor G.
 */
public class ExtendedPredicateUtils extends PredicateUtils {
    public ExtendedPredicateUtils() {
        super();
    }

    /**
     * Converts a &quot;regular&quot; predicate into an extended one
     * @param argType Argument type
     * @param predicate Original {@link Predicate}
     * @return The {@link ExtendedPredicate} wrapper
     * @throws IllegalArgumentException if predicate is <code>null</code>
     * @throws IllegalStateException if argument type is <code>null</code>
     */
    public static final <T> ExtendedPredicate<T> extend(Class<T> argType, final Predicate<? super T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("Missing predicate");
        }
        
        return new AbstractExtendedPredicate<T>(argType) {
            @Override
            public boolean evaluate(T object) {
                return predicate.evaluate(object);
            }
        };
    }
    /**
     * @param value The value to compare with
     * @param comp The {@link Comparator} to use
     * @return A {@link Predicate} that returns <code>true</code> for any
     * object for which the {@link Comparator#compare(Object, Object)} returns
     * zero
     */
    public static final <T> Predicate<T> equals(final T value, final Comparator<? super T> comp) {
        if (comp == null) {
            throw new IllegalArgumentException("No comparator");
        }

        return new Predicate<T>() {
            @Override
            public boolean evaluate(T object) {
                if (comp.compare(value, object) == 0) {
                    return true;
                } else {
                    return false;
                }
            }
            
        };
    }
    
    /**
     * @param predicate The {@link ExtendedPredicate} to use (ignored if <code>null</code>)
     * @param item The item to evaluate (ignored if <code>null</code>)
     * @return The {@link Boolean} value of the evaluation - if possible,
     * <code>null</code> if cannot evaluate
     * @see #canEvaluate(ExtendedPredicate, Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final Boolean safeEvaluate(ExtendedPredicate<?> predicate, Object item) {
        if ((predicate == null) || (item == null) || (!canEvaluate(predicate, item))) {
            return null;
        } else {
            return Boolean.valueOf(((Predicate) predicate).evaluate(item));
        }
    }

    /**
     * @param predicate The {@link ExtendedPredicate} to examine
     * @param item The candidate item
     * @return <code>true</code> if the {@link ExtendedPredicate#getArgumentType()}
     * is compatible with the item's type
     * @throws IllegalArgumentException if no predicate or item provided
     */
    public static final boolean canEvaluate(ExtendedPredicate<?> predicate, Object item) {
        if ((predicate == null) || (item == null)) {
            throw new IllegalArgumentException("Missing predicate or items");
        }
        
        Class<?>    argType=predicate.getArgumentType();
        if (argType.isAssignableFrom(item.getClass())) {
            return true;
        } else {
            return false;
        }
    }
}
