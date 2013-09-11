/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jdbc.repo;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:32:00 AM
 */
public interface RawPropertiesRepoFactory {
    /**
     * @param entityClass The {@link Identified} entity class
     * @return The {@link RawPropertiesRepo} to be used to access the entity's properties
     */
    <E extends Identified> RawPropertiesRepo<E> getRawEntityRepository(Class<E> entityClass);
    
    /**
     * @param entityClass The {@link MutableIdentity} entity class
     * @return The {@link IdentifiedEntityRepo} to be used to access the persisted entities
     */
    <E extends MutableIdentity> IdentifiedEntityRepo<E> getIdentifiedEntityRepository(Class<E> entityClass);
    
    /**
     * Used to persist entities that have several polymorphic implementations
     * @param entityClass The {@link MutableIdentity} entity class
     * @return The {@link IdentifiedEntityRepo} to be used to access the persisted entities
     */
    <E extends MutableIdentity> IdentifiedEntityRepo<E> getPolymorphicEntityRepository(Class<E> entityClass);

}
