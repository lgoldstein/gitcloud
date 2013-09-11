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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ExtendedBeanUtils;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ExtendedConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.repo.IdentifiedEntityIdGenerator;
import org.springframework.jdbc.repo.MutableIdentity;
import org.springframework.jdbc.repo.RawPropertiesRepo;
import org.springframework.util.Assert;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 12:30:07 PM
 */
public class RawPropertiesPolymorphicIdentifiedEntityRepoImpl<E extends MutableIdentity> extends AbstractIdentifiedEntityRepo<E> {
    // NOTE: name is specifically selected so as to match "Object#getClass" which we know has no setter
    public static final String  CLASS_PROP_NAME="class";

    protected final RawPropertiesRepo<E> repo;
    protected final Map<Class<?>,Pair<Transformer<Map<String,?>,E>,Transformer<E,? extends Map<String,?>>>> transformersMap=
            Collections.synchronizedMap(new HashMap<Class<?>,Pair<Transformer<Map<String,?>,E>,Transformer<E,? extends Map<String,?>>>>());
    protected final Map<Class<?>,IdentifiedEntityIdGenerator<?>>    idGeneratorsMap=
            Collections.synchronizedMap(new HashMap<Class<?>,IdentifiedEntityIdGenerator<?>>());

    public RawPropertiesPolymorphicIdentifiedEntityRepoImpl(RawPropertiesRepo<E> rawRepo) {
        super((rawRepo == null) ? null : rawRepo.getEntityClass(), (rawRepo == null) ? null : rawRepo.idsBuilder());
        repo = Validate.notNull(rawRepo, "No raw repository provided", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public List<String> listEntitiesIdentifiers() {
        return repo.listEntities();
    }

    @Override
    public E findEntityById(String id) {
        Map<String,?>   props=repo.getProperties(id);
        if (ExtendedMapUtils.isEmpty(props)) {
            return null;
        }

        Object clazzType=props.get(CLASS_PROP_NAME);
        if (!(clazzType instanceof Class)) {
            throw new IllegalStateException("findEntityById(" + id + ") no effective class");
        }
        
        Transformer<Map<String,?>,E> transformer=getPropertiesTransformer((Class<?>) clazzType);
        if (transformer == null) {
            throw new IllegalStateException("findEntityById(" + id + ") no properties transformer");
        }
        
        return transformer.transform(props);
    }

    @Override
    public boolean entityExists(String id) {
        return repo.entityExists(id);
    }

    @Override
    public List<String> findEntitiesIdentifiers(String propName, Predicate<? super Serializable> predicate) {
        return repo.findEntities(propName, predicate);
    }

    @Override
    public String createEntity(E entity) {
        Assert.notNull(entity, "No entity");

        final IdentifiedEntityIdGenerator<?>    entityIdGenerator=getEntityIdGenerator(entity);
        if (entityIdGenerator == null) {
            throw new IllegalStateException("createEntity(" + entity.getClass().getSimpleName() + ") no identity generator");
        }

        final String  prevId=entity.getId();
        final String  assignedId=entityIdGenerator.build();
        if (!StringUtils.isEmpty(prevId)) {
            logger.warn("createEntity(" + prevId + ") overwrite entity ID: " + assignedId);
        }
        entity.setId(assignedId);

        Transformer<E,? extends Map<String,?>>  transformer=getEntityTransformer(entity);
        if (transformer == null) {
            throw new IllegalStateException("createEntity(" + entity + ") no properties transformer");
        }
        
        Map<String,?>   props=repo.setProperties(entity, transformer);
        if (logger.isTraceEnabled()) {
            ExtendedMapUtils.forAllEntriesDo(props, new Closure<Map.Entry<String,?>>() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void execute(Entry<String, ?> e) {
                    logger.trace("createEntity(" + assignedId + ") " + e.getKey() + ": " + e.getValue());
                }
            });
        }

        return assignedId;
    }

    @Override
    public void updateEntity(final E entity) {
        Assert.notNull(entity, "No entity");
        if (!entityExists(entity)) {
            throw new UnsupportedOperationException("updateEntity(" + entity + ") entity not persisted");
        }

        if (logger.isTraceEnabled()) {
            Map<String,?>   props=repo.getProperties(entity);
            ExtendedMapUtils.forAllEntriesDo(props, new Closure<Map.Entry<String,?>>() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void execute(Entry<String, ?> e) {
                    logger.trace("updateEntity(" + entity.getId() + ")[" + e.getKey() + "]-BEFORE: " + e.getValue());
                }
            });
        }

        Transformer<E,? extends Map<String,?>>  transformer=getEntityTransformer(entity);
        if (transformer == null) {
            throw new IllegalStateException("updateEntity(" + entity + ") no properties transformer");
        }

        Map<String,?>   props=repo.setProperties(entity, transformer);
        if (logger.isTraceEnabled()) {
            ExtendedMapUtils.forAllEntriesDo(props, new Closure<Map.Entry<String,?>>() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void execute(Entry<String, ?> e) {
                    logger.trace("updateEntity(" + entity.getId() + ")[" + e.getKey() + "]-AFTER: " + e.getValue());
                }
            });
        }
    }

    @Override
    public List<String> removeAllIdentifiers(Collection<String> ids) {
        return repo.removeAll(ids);
    }

    @Override
    public E removeEntity(String id) {
        E   entity=findEntityById(id);
        if (entity == null) {
            return null;
        }
        
        repo.removeProperties(id);
        return entity;
    }

    protected IdentifiedEntityIdGenerator<?> getEntityIdGenerator(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("getEntityIdGenerator(" + getEntityClass().getSimpleName() + ") no entity");
        }
        
        return getEntityIdGenerator(entity.getClass());
    }

    @SuppressWarnings("unchecked")
    protected IdentifiedEntityIdGenerator<?> getEntityIdGenerator(Class<?> eClass) {
        IdentifiedEntityIdGenerator<?>  generator;
        synchronized(idGeneratorsMap) {
            if ((generator=idGeneratorsMap.get(eClass)) != null) {
                return generator;
            }

            generator = new IdentifiedEntityIdGeneratorImpl<E>((Class<E>) eClass);
            idGeneratorsMap.put(eClass, generator);
        }
        
        logger.info("getEntityIdGenerator(" + eClass.getSimpleName() + ") created");
        return generator;
    }

    protected Transformer<Map<String,?>,E> getPropertiesTransformer(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("getPropertiesTransformer(" + getEntityClass().getSimpleName() + ") no entity");
        }
        
        return getPropertiesTransformer(entity.getClass());
    }

    protected Transformer<Map<String,?>,E> getPropertiesTransformer(Class<?> eClass) {
        Pair<Transformer<Map<String,?>,E>,?>   pair=getEntityTransformers(eClass);
        return pair.getLeft();
    }

    protected Transformer<E,? extends Map<String,?>> getEntityTransformer(E entity) {
        Pair<?,Transformer<E,? extends Map<String,?>>>  pair=getEntityTransformers(entity);
        return pair.getRight();
    }

    protected Pair<Transformer<Map<String,?>,E>,Transformer<E,? extends Map<String,?>>> getEntityTransformers(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("getEntityTransformers(" + getEntityClass().getSimpleName() + ") no entity");
        }
        return getEntityTransformers(entity.getClass());
    }
    
    protected Pair<Transformer<Map<String,?>,E>,Transformer<E,? extends Map<String,?>>> getEntityTransformers(final Class<?> eClass) {
       
        /*
         * NOTE: there might be a "race condition" but we don't care since
         * result is the same either way
         */
        Pair<Transformer<Map<String,?>,E>,Transformer<E,? extends Map<String,?>>>   pair;
        synchronized(transformersMap) {
            if ((pair=transformersMap.get(eClass)) != null) {
                return pair;
            }
        }
        
        Map<String,Pair<Method,Method>>     entityAttributes=
                ExtendedBeanUtils.removeNonModifiableAttributes(ExtendedBeanUtils.describeBean(eClass, false, true));
        @SuppressWarnings("unchecked")
        final Transformer<Map<String,?>,E>  pureEntityTransformer=
                ExtendedBeanUtils.propertiesToBeanTransformer(ExtendedConstructorUtils.newInstanceFactory((Class<E>) eClass), entityAttributes);
        Transformer<Map<String,?>,E>    props2entityTransformer=new Transformer<Map<String,?>,E>() {
                @Override
                public E transform(Map<String, ?> beanProps) {
                    Object  value=beanProps.remove(CLASS_PROP_NAME);
                    if (value == null) {
                        throw new IllegalStateException("transform(" + eClass.getSimpleName() + ") missing reserved type property");
                    }

                    return pureEntityTransformer.transform(beanProps);
                }
            };

        @SuppressWarnings("unchecked")
        final Transformer<E,? extends Map<String,?>>   purePropsTransformer=
                ExtendedBeanUtils.beanToPropertiesTransformer((Class<E>) eClass, entityAttributes);
        Transformer<E,Map<String,?>>  entity2propsTransfomer=new Transformer<E,Map<String,?>>() {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public Map<String, ?> transform(E input) {
                    Map<String,?>   beanProps=purePropsTransformer.transform(input);
                    if (ExtendedMapUtils.isEmpty(beanProps)) {
                        return beanProps;
                    }
                    
                    Object  value=beanProps.get(CLASS_PROP_NAME);
                    if (value != null) {
                        throw new IllegalStateException("transform(" + input.getClass().getSimpleName() + ") reserved property used: " + value);
                    }
                    ((Map) beanProps).put(CLASS_PROP_NAME, eClass);
                    return beanProps;
                }
            };

        pair = Pair.<Transformer<Map<String,?>,E>,Transformer<E,? extends Map<String,?>>>of(props2entityTransformer, entity2propsTransfomer);
        synchronized(transformersMap) {
            transformersMap.put(eClass, pair);
        }
        
        logger.info("getEntityTransformers(" + eClass.getSimpleName() + ") created transformers");
        return pair;
    }
}
