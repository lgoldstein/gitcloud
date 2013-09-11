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

package org.springframework.validation;

/**
 * A &quot;generified&quot; validator implementation
 * @param <E> Type of entity being validated 
 * @author Lyor G.
 */
public interface TypedValidator<E> {
    /**
     * @return The {@link Class} of the entity that this validator supports
     */
    Class<E> getEntityClass();
    
    /**
     * Validate the supplied {@code target} object, which must be
     * of a {@link Class} matching {@link #getEntityClass()}
     * <p>instance can be .
     * @param target the object that is to be validated (can be {@code null})
     * @param errors The supplied {@link Errors errors} contextual state about
     * the validation process (never {@code null}) used to report any resulting
     * validation errors
     * @see ValidationUtils
     */
    void validate(E target, Errors errors);
}
