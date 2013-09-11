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
import org.springframework.jdbc.repo.MutableIdentity;
import org.springframework.jdbc.repo.RawPropertiesRepo;
import org.springframework.util.Assert;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 12:24:41 PM
 */
public class RawPropertiesIdentifiedEntityRepoImpl<E extends MutableIdentity> extends AbstractIdentifiedEntityRepo<E>  {
    protected final RawPropertiesRepo<E>    repo;
    protected final Transformer<Map<String,?>,E> props2entityTransformer;
    protected final Transformer<E,? extends Map<String,?>> entity2propsTransfomer;

    public RawPropertiesIdentifiedEntityRepoImpl(RawPropertiesRepo<E> rawRepo) {
        super((rawRepo == null) ? null : rawRepo.getEntityClass(), (rawRepo == null) ? null : rawRepo.idsBuilder());

        repo = Validate.notNull(rawRepo, "No raw repository", ArrayUtils.EMPTY_OBJECT_ARRAY);
        
        Class<E>                        eClass=rawRepo.getEntityClass();
        Map<String,Pair<Method,Method>> entityAttributes=
                ExtendedBeanUtils.removeNonModifiableAttributes(ExtendedBeanUtils.describeBean(eClass, false, true));
        props2entityTransformer = ExtendedBeanUtils.propertiesToBeanTransformer(
                ExtendedConstructorUtils.newInstanceFactory(eClass), entityAttributes);
        entity2propsTransfomer = ExtendedBeanUtils.beanToPropertiesTransformer(eClass, entityAttributes);
    }

    @Override
    public List<String> listEntitiesIdentifiers() {
        return repo.listEntities();
    }

    @Override
    public boolean entityExists(String id) {
        return repo.entityExists(id);
    }

    @Override
    public E findEntityById(String id) {
        return repo.getEntity(id, props2entityTransformer);
    }

    @Override
    public List<String> findEntitiesIdentifiers(String propName, Predicate<? super Serializable> predicate) {
        return repo.findEntities(propName, predicate);
    }

    @Override
    public String createEntity(E entity) {
        Assert.notNull(entity, "No entity instance");

        final String  prevId=entity.getId();
        final String  assignedId=idGenerator.build();
        if (!StringUtils.isEmpty(prevId)) {
            logger.warn("createEntity(" + prevId + ") overwrite entity ID: " + assignedId);
        }
        entity.setId(assignedId);

        Map<String,?>   props=repo.setProperties(entity, entity2propsTransfomer);
        logger.info("createEntity(" + getEntityClass() + ")[" + prevId + "] created");
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
        Assert.notNull(entity, "No entity provided");
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

        Map<String,?>   props=repo.setProperties(entity, entity2propsTransfomer);
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
        return repo.removeEntity(id, props2entityTransformer);
    }
}
