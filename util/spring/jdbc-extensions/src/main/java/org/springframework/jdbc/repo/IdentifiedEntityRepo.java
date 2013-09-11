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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

/**
 * Represents basic repository functionality for {@link MutableIdentity} entities
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:32:26 AM
 */
public interface IdentifiedEntityRepo<E extends MutableIdentity> {
    /**
     * @return The persisted entity class
     */
    Class<E> getEntityClass();

    /**
     * @return A {@link IdentifiedEntityIdGenerator} for the persisted properties
     */
    IdentifiedEntityIdGenerator<E> idsBuilder();

    /**
     * @param entity The entity to be checked
     * @return <code>true</code> if the entity is persisted
     * @see #entityExists(String)
     */
    boolean entityExists(E entity);

    /**
     * @param id The entity identifier
     * @return <code>true</code> if the identifier represents a persisted
     * entity. <B>Note:</B> this may be a more efficient method rather than
     * checking if {@link #getProperties(String)} returns a <code>null</code>
     * or empty properties map
     */
    boolean entityExists(String id);

    /**
     * @return A {@link List} of the identifiers of the currently persisted entities
     */
    List<String> listEntitiesIdentifiers();

    /**
     * @return A {@link List} of all the currently persisted entities
     */
    List<E> listEntities();

    /**
     * @param idsList A {@link Collection} of entities identifiers - ignored
     * if <code>null</code>/empty
     * @return A {@link List} of all the entities that have these identifiers
     * and are actually persisted. <B>Note:</B> if an identifier is repeated
     * then only <U>one</U> instance will be returned
     * @see #findEntityById(String)
     */
    List<E> findEntities(Collection<String> idsList);

    /**
     * @param id The entity ID - ignored if <code>null</code>/empty
     * @return The entity value - <code>null</code> if no such identifier
     */
    E findEntityById(String id);

    /**
     * @param id The entity ID - ignored if <code>null</code>/empty
     * @param entityType - the expected entity type
     * @return The entity value cast to the expected type - <code>null</code>
     * if no such identifier or entity cannot be cast to the expected type
     */
    <T extends E> T findEntityById(String id, Class<T> entityType);

    /**
     * @param propName The entity property name
     * @param propValue The property value - may not be <code>null</code>
     * @return A {@link List} of all the matching entities
     * @see #findEntities(String, Serializable)
     */
    List<E> findEntities(String propName, Serializable propValue);

    /**
     * @param propName The entity property name
     * @param propValue The property value - may not be <code>null</code>
     * @return A {@link List} of all the identifiers for which the
     * specified property name matches the given value
     */
    List<String> findEntitiesIdentifiers(String propName, Serializable propValue);

    /**
     * @param propName The entity property name
     * @param predicate The {@link Predicate} to use to decide whether the
     * specified property value matches the value - if so, then the identifier
     * of the matching entity will be added to the results
     * @return A {@link List} of all the matching entities
     * @see #findEntities(String, Predicate)
     */
    List<E> findEntities(String propName, Predicate<? super Serializable> predicate);

    /**
     * @param propName The entity property name
     * @param predicate The {@link Predicate} to use to decide whether the
     * specified property value matches the value - if so, then the identifier
     * of the matching entity will be added to the results
     * @return A {@link List} of all the identifiers for which {@link Predicate#evaluate(Object)}
     * returned <code>true</code> on their property value
     * @throws IllegalArgumentException if <code>null</code>/empty property name
     * and/or predicate instance
     */
    List<String> findEntitiesIdentifiers(String propName, Predicate<? super Serializable> predicate);

    /**
     * Persists the specified entity and assigns a new identifier to it
     * @param entity The entity to be persisted
     * @return The assigned unique identifier - <B>Note:</B> any previous
     * identifier set is <U>replaced</U> with a new one
     */
    String createEntity(E entity);

    void updateEntity(E entity);

    /**
     * @return A {@link List} of all removed entities
     */
    List<E> removeAllEntities();

    /**
     * @return A {@link List} of all the removed entities identifiers
     */
    List<String> removeAllIdentifiers();

    /**
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @param ids A {@link Collection} of identifiers to remove
     * @return The {@link List} of entities actually removed
     */
    List<E> removeAll(Collection<String> ids);

    /**
     * @param ids A {@link Collection} of identifiers to remove
     * @return The {@link List} of identifiers actually removed
     */
    List<String> removeAllIdentifiers(Collection<String> ids);

    /**
     * @param id The entity ID - ignored if <code>null</code>/empty
     * @return The entity value - <code>null</code> if no such identifier
     */
    E removeEntity(String id);
}
