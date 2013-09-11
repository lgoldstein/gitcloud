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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.RefreshedContextAttacher;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.repo.Identified;
import org.springframework.jdbc.repo.IdentifiedEntityIdGenerator;
import org.springframework.jdbc.repo.RawPropertiesRepo;

/**
 * Some useful common functionality for {@link RawPropertiesRepo} implementations
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:54:47 AM
 */
public abstract class AbstractRawPropertiesRepoImpl<E extends Identified>
                extends RefreshedContextAttacher
                implements RawPropertiesRepo<E> {
    protected final Class<E>  entityClass;
    protected final ClassLoader loader;
    protected final ConversionService conversionService;
    protected final IdentifiedEntityIdGenerator<E> idGenerator;

    protected AbstractRawPropertiesRepoImpl(Class<E> eClass, ConversionService converter) {
        this(eClass, converter, new IdentifiedEntityIdGeneratorImpl<E>(eClass));
    }

    protected AbstractRawPropertiesRepoImpl(Class<E> eClass, ConversionService converter, IdentifiedEntityIdGenerator<E> idGen) {
        super(eClass);
        
        entityClass = Validate.notNull(eClass, "No entity class", ArrayUtils.EMPTY_OBJECT_ARRAY);
        idGenerator = Validate.notNull(idGen, "No ID(s) generator", ArrayUtils.EMPTY_OBJECT_ARRAY);
        conversionService = Validate.notNull(converter, "No conversion service", ArrayUtils.EMPTY_OBJECT_ARRAY);
        loader = ExtendedClassUtils.getDefaultClassLoader(getClass());
    }
    @Override
    public final Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public final IdentifiedEntityIdGenerator<E> idsBuilder() {
        return idGenerator;
    }

    @Override
    public E getEntity(String id, Transformer<Map<String,?>, ? extends E> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("getEntity(" + id + ") no transformer");
        }

        Map<String, Serializable>   props=getProperties(id);
        if (ExtendedMapUtils.isEmpty(props)) {
            return null;
        } else {
            return transformer.transform(props);
        }
    }

    @Override
    public boolean entityExists(E entity) {
        return entityExists((entity == null) ? null : entity.getId());
    }

    @Override
    public List<E> listEntities(Transformer<Map<String,?>, ? extends E> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("listEntities() no transformer");
        }
        
        return toEntitiesList(transformer, listEntities());
    }

    @Override
    public List<E> listMatchingIdentifiedEntities(String pattern, Transformer<Map<String, ?>, ? extends E> transformer) {
        return toEntitiesList(transformer, listMatchingIdentifiers(pattern));
    }

    @Override
    public List<E> findEntities(String propName, Serializable propValue, Transformer<Map<String,?>, ? extends E> transformer) {
        if (propValue == null) {
            throw new IllegalArgumentException("findEntities(" + propName + ") no value");
        }

        if (transformer == null) {
            throw new IllegalArgumentException("findEntities(" + getEntityClass().getSimpleName() + ")[" + propName + "] no transformer");
        }

        return findEntities(propName, PredicateUtils.equalPredicate(propValue), transformer);
    }

    @Override
    public List<E> findEntities(String propName, Predicate<? super Serializable> predicate, Transformer<Map<String,?>, ? extends E> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("findEntities(" + propName + ") no transformer");
        }

        return toEntitiesList(transformer, findEntities(propName, predicate));
    }

    @Override
    public List<String> findEntities(String propName, Serializable propValue) {
        if (propValue == null) {
            throw new IllegalArgumentException("findEntities(" + propName + ") no value");
        }
        
        return findEntities(propName, PredicateUtils.equalPredicate(propValue));
    }

    @Override
    public List<E> removeAll(Transformer<Map<String, ?>, ? extends E> transformer) {
        return removeAll(transformer, listEntities());
    }

    @Override
    public List<String> removeAll() {
        return removeAll(listEntities());
    }

    @Override
    public Map<String,?> setProperties(E entity, Transformer<E,? extends Map<String,?>> propertiesBuilder) {
        if (entity == null) {
            throw new IllegalArgumentException("setProperties() no entity");
        }
        
        if (propertiesBuilder == null) {
            throw new IllegalArgumentException("setProperties() no properties builder");
        }
        
        Map<String,?>   props=propertiesBuilder.transform(entity);
        setProperties(entity.getId(), props);
        return props;
    }

    @Override
    public E removeEntity(String id, Transformer<Map<String,?>, ? extends E> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("removeEntity(" + id + ") no transformer");
        }

        Map<String, Serializable>   props=removeProperties(id);
        if (ExtendedMapUtils.isEmpty(props)) {
            return null;
        } else {
            return transformer.transform(props);
        }
    }

    public List<E> toEntitiesList(Transformer<Map<String,?>, ? extends E> transformer, Collection<String> idsList) {
        if (ExtendedCollectionUtils.isEmpty(idsList)) {
            return Collections.emptyList();
        }
        
        List<E> result=new ArrayList<E>(idsList.size());
        for (String id : idsList) {
            E   entity=getEntity(id, transformer);
            if (entity == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("toEntitiesList(" + id + ") no longer persisted");
                }
                continue;   // maybe someone beat us to it and deleted it
            }
            
            result.add(entity);
        }
    
        return result;
    }
    
    @Override
    public List<String> removeAll(Collection<String> ids) {
        return removeAll(String.class, new Transformer<Pair<String,Map<String,?>>,String>() {
                @Override
                public String transform(Pair<String, Map<String, ?>> p) {
                    return p.getLeft();
                }
            }, ids);
    }

    @Override
    public List<E> removeAll(final Transformer<Map<String,?>,? extends E> transformer, Collection<String> ids) {
        if (transformer == null) {
            throw new IllegalArgumentException("removeAll(" + ids + ") no transformer");
        }
        
        return removeAll(getEntityClass(), new Transformer<Pair<String,Map<String,?>>,E>() {
                @Override
                public E transform(Pair<String, Map<String, ?>> p) {
                    return transformer.transform(p.getRight());
                }
            }, ids);
    }

    protected abstract <R> List<R> removeAll(Class<R> elementType, Transformer<Pair<String,Map<String,?>>,? extends R> transformer, Collection<String> ids);
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getEntityClass().getSimpleName() + "]";
    }
}
