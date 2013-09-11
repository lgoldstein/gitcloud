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
import java.util.Map;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

/**
 * Represents a repository that persists &quot;raw&quot; {@link Serializable}
 * properties for a bean entity that is {@link Identified}
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:29:15 AM
 */
public interface RawPropertiesRepo<E extends Identified> {
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
     * @param id The entity ID - ignored if <code>null</code>/empty
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @return The entity value - <code>null</code> if no such identifier
     * @see #getProperties(String)
     */
    E getEntity(String id, Transformer<Map<String,?>, ? extends E> transformer);

    /**
     * @param entity The entity
     * @return A {@link Map} of the properties - the meaning of the names
     * and/or values is up to the caller. If no such identifier then
     * returns <code>null</code>/empty value
     */
    Map<String,Serializable> getProperties(E entity);

    /**
     * @param id The entity ID - ignored if <code>null</code>/empty
     * @return A {@link Map} of the properties - the meaning of the names
     * and/or values is up to the caller. If no such identifier then
     * returns <code>null</code>/empty value
     */
    Map<String,Serializable> getProperties(String id);

   /**
    * @param id The entity ID - ignored if <code>null</code>/empty
    * @param transformer The {@link Transformer} to use in order to convert
    * the properties into an entity
    * @return The entity value - <code>null</code> if no such identifier
    * @see #removeProperties(String)
    */
    E removeEntity(String id, Transformer<Map<String,?>, ? extends E> transformer);

    /**
     * @param id The entity ID - ignored if <code>null</code>/empty
     * @return A {@link Map} of the removed entity properties - <code>null</code>
     * if entity does not exist anyway
     */
    Map<String,Serializable> removeProperties(String id);

    /**
     * @param entity The entity whose properties are to be persisted
     * @param propertiesBuilder The {@link Transformer} to be used to extract
     * the entity properties
     * @return The persisted {@link Map} of persisted properties. <B>Note:</B>
     * if entity already exists, then these properties <U>replace</U> any previous
     * ones
     * @see #setProperties(String, Map)
     */
    Map<String,?> setProperties(E entity, Transformer<E,? extends Map<String,?>> propertiesBuilder);

    /**
     * @param id The entity ID
     * @param props The properties to persist - if entity already exists, then
     * these properties <U>replace</U> any previous ones
     * @throws IllegalArgumentException if <code>null</code>/empty entity
     * identifier or properties
     */
    void setProperties(String id, Map<String,?> props);

    /**
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @return A {@link List} of all the current entities
     * @see #listEntities()
     */
    List<E> listEntities(Transformer<Map<String,?>, ? extends E> transformer);

    /**
     * @return A {@link List} of all the entity identifiers currently persisted
     */
    List<String> listEntities();

    /**
     * Allows looking for identifiers matching a given pattern where
     * the asterisk (*) may be used to match zero or more number of
     * characters  
     * @param pattern The pattern to match - ignored if <code>null</code>/empty
     * @return A {@link List} of all the entity identifiers currently persisted
     * that match the given pattern
     */
    List<String> listMatchingIdentifiers(String pattern);

    /**
     * Allows looking for entities whose identifiers matching a given pattern
     * where the asterisk (*) may be used to match zero or more number of
     * characters  
     * @param pattern The pattern to match - ignored if <code>null</code>/empty
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @return A {@link List} of all the current entities whose identifier
     * matches the given pattern
     */
    List<E> listMatchingIdentifiedEntities(String pattern, Transformer<Map<String,?>, ? extends E> transformer);

    /**
     * @param propName The entity property name
     * @param propValue The property value - may not be <code>null</code>
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @return A {@link List} of all the matching entities
     * @see #findEntities(String, Serializable)
     */
    List<E> findEntities(String propName, Serializable propValue, Transformer<Map<String,?>, ? extends E> transformer);

    /**
     * @param propName The entity property name
     * @param propValue The property value - may not be <code>null</code>
     * @return A {@link List} of all the identifiers for which the
     * specified property name matches the given value
     */
    List<String> findEntities(String propName, Serializable propValue);

    /**
     * @param propName The entity property name
     * @param predicate The {@link Predicate} to use to decide whether the
     * specified property value matches the value - if so, then the identifier
     * of the matching entity will be added to the results
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @return A {@link List} of all the matching entities
     * @see #findEntities(String, Predicate)
     */
    List<E> findEntities(String propName, Predicate<? super Serializable> predicate, Transformer<Map<String,?>, ? extends E> transformer);

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
    List<String> findEntities(String propName, Predicate<? super Serializable> predicate);

    /**
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @return A {@link List} of all removed entities
     */
    List<E> removeAll(Transformer<Map<String,?>, ? extends E> transformer);

    /**
     * @return A {@link List} of all the removed entities identifiers
     */
    List<String> removeAll();

    /**
     * @param transformer The {@link Transformer} to use in order to convert
     * the properties into an entity
     * @param ids A {@link Collection} of identifiers to remove
     * @return The {@link List} of entities actually removed
     */
    List<E> removeAll(Transformer<Map<String,?>, ? extends E> transformer, Collection<String> ids);

    /**
     * @param ids A {@link Collection} of identifiers to remove
     * @return The {@link List} of identifiers actually removed
     */
    List<String> removeAll(Collection<String> ids);

}
