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
package org.springframework.jdbc.repo.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.AbstractExtendedClosure;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.context.RefreshedContextAttacher;
import org.springframework.jdbc.repo.IdentifiedEntityIdGenerator;
import org.springframework.jdbc.repo.IdentifiedEntityRepo;
import org.springframework.jdbc.repo.MutableIdentity;
import org.springframework.util.Assert;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 12:20:31 PM
 */
public abstract class AbstractIdentifiedEntityRepo<E extends MutableIdentity>
                 extends RefreshedContextAttacher
                 implements IdentifiedEntityRepo<E> {
    protected final Class<E>  entityClass;
    protected final IdentifiedEntityIdGenerator<E> idGenerator;

    protected AbstractIdentifiedEntityRepo(Class<E> eClass) {
        this(eClass, new IdentifiedEntityIdGeneratorImpl<E>(eClass));
    }
    
    protected AbstractIdentifiedEntityRepo(Class<E> eClass, IdentifiedEntityIdGenerator<E> idGen) {
        super(eClass);

        entityClass = Validate.notNull(eClass, "No entity class", ArrayUtils.EMPTY_OBJECT_ARRAY);
        idGenerator = Validate.notNull(idGen, "No identifiers generator", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }
    @Override
    public final Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public IdentifiedEntityIdGenerator<E> idsBuilder() {
        return idGenerator;
    }

    @Override
    public boolean entityExists(E entity) {
        if (entity == null) {
            return false;
        } else {
            return entityExists(entity.getId());
        }
    }

    @Override
    public boolean entityExists(String id) {
        if (StringUtils.isEmpty(id)) {
            return false;
        } else if (findEntityById(id) == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public <T extends E> T findEntityById(String id, Class<T> entityType) {
        Assert.notNull(entityType, "No target entity type");

        E   entity=findEntityById(id);
        if (entityType.isInstance(entity)) {
            return entityType.cast(entity);
        } else {
            return null;
        }
    }

    @Override
    public List<E> listEntities() {
        return findEntities(listEntitiesIdentifiers());
    }
    
    @Override
    public List<E> findEntities(Collection<String> idsList) {
        if (ExtendedCollectionUtils.isEmpty(idsList)) {
            return Collections.emptyList();
        }
        
        final List<E>   result=new ArrayList<E>(idsList.size());
        Set<String>     idSet=(idsList instanceof Set) ? (Set<String>) idsList : new LinkedHashSet<>(idsList);
        CollectionUtils.forAllDo(idSet, new AbstractExtendedClosure<String>(String.class) {
                @Override
                public void execute(String id) {
                    if (StringUtils.isEmpty(id)) {
                        return;
                    }
                    
                    E   entity=findEntityById(id);
                    if (entity == null) {
                        return;
                    }
                    
                    result.add(entity);
                }
            });
        return result;
    }

    @Override
    public List<E> findEntities(String propName, Serializable propValue) {
        return findEntities(findEntitiesIdentifiers(propName, propValue));
    }

    @Override
    public List<String> findEntitiesIdentifiers(String propName, Serializable propValue) {
        if (propValue == null) {
            throw new IllegalArgumentException("findEntitiesIdentifiers(" + propName + ") no value specified");
        }
        
        return findEntitiesIdentifiers(propName, PredicateUtils.equalPredicate(propValue));
    }

    @Override
    public List<E> findEntities(String propName, Predicate<? super Serializable> predicate) {
        return findEntities(findEntitiesIdentifiers(propName, predicate));
    }

    @Override
    public List<E> removeAllEntities() {
        return removeAll(listEntitiesIdentifiers());
    }

    @Override
    public List<String> removeAllIdentifiers() {
        return removeAllIdentifiers(listEntitiesIdentifiers());
    }

    @Override
    public List<E> removeAll(Collection<String> idsList) {
        if (ExtendedCollectionUtils.isEmpty(idsList)) {
            return Collections.emptyList();
        }
        
        final List<E>   result=new ArrayList<E>(idsList.size());
        CollectionUtils.forAllDo(idsList, new AbstractExtendedClosure<String>(String.class) {
                @Override
                public void execute(String id) {
                    if (StringUtils.isEmpty(id)) {
                        return;
                    }
                    
                    E   entity=removeEntity(id);
                    if (entity == null) {
                        return;
                    }
                    
                    result.add(entity);
                }
            });
        return result;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getEntityClass().getSimpleName() + "]";
    }
}
