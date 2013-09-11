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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.context.RefreshedContextAttacher;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.repo.Identified;
import org.springframework.jdbc.repo.IdentifiedEntityRepo;
import org.springframework.jdbc.repo.MutableIdentity;
import org.springframework.jdbc.repo.RawPropertiesRepo;
import org.springframework.jdbc.repo.RawPropertiesRepoFactory;
import org.springframework.jdbc.repo.impl.jdbc.RawPropertiesRepoImpl;
import org.springframework.util.Assert;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 12:13:37 PM
 */
public class RawPropertiesRepoFactoryImpl extends RefreshedContextAttacher implements RawPropertiesRepoFactory {
    protected final NamedParameterJdbcOperations    jdbcOperations;
    protected final ConversionService conversionService;
    private final Map<Class<?>,RawPropertiesRepo<?>>    rawRepos=new HashMap<Class<?>, RawPropertiesRepo<?>>();
    private final Map<Class<?>,IdentifiedEntityRepo<?>>    identifiedRepos=new HashMap<Class<?>, IdentifiedEntityRepo<?>>();
    private final Map<Class<?>,IdentifiedEntityRepo<?>>    polymporphicRepos=new HashMap<Class<?>, IdentifiedEntityRepo<?>>();

    public RawPropertiesRepoFactoryImpl(DataSource ds, ConversionService converter) {
        this(new NamedParameterJdbcTemplate(ds), converter);
    }
    
    public RawPropertiesRepoFactoryImpl(NamedParameterJdbcOperations ops, ConversionService converter) {
        jdbcOperations = Validate.notNull(ops, "No JDBC accessor", ArrayUtils.EMPTY_OBJECT_ARRAY);
        conversionService = Validate.notNull(converter, "No conversion service", ArrayUtils.EMPTY_OBJECT_ARRAY);
    }
    
    @Override
    public <E extends Identified> RawPropertiesRepo<E> getRawEntityRepository(Class<E> entityClass) {
        Assert.notNull(entityClass, "No entity class provided");

        final RawPropertiesRepo<?>  mappedRepo;
        final RawPropertiesRepo<E>  resultRepo;
        synchronized(rawRepos) {
            if ((mappedRepo=rawRepos.get(entityClass)) == null) {
                resultRepo = createRawPropertiesRepo(entityClass);
                rawRepos.put(entityClass, resultRepo);
            } else {
                @SuppressWarnings("unchecked")
                RawPropertiesRepo<E>    entityRepo=(RawPropertiesRepo<E>) mappedRepo;
                resultRepo = entityRepo;
            }
        }

        if (mappedRepo == null) {
            logger.info("getEntityRepository(" + entityClass.getSimpleName() + ") created new instance");
        }

        return resultRepo;
    }

    @Override
    public <E extends MutableIdentity> IdentifiedEntityRepo<E> getIdentifiedEntityRepository(Class<E> entityClass) {
        Assert.notNull(entityClass, "No entity class provided");

        final IdentifiedEntityRepo<?>   mappedRepo;
        final IdentifiedEntityRepo<E>   resultRepo;
        synchronized(identifiedRepos) {
            if ((mappedRepo=identifiedRepos.get(entityClass)) == null) {
                resultRepo = createIdentifiedEntityRepository(entityClass);
                identifiedRepos.put(entityClass, resultRepo);
            } else {
                @SuppressWarnings("unchecked")
                IdentifiedEntityRepo<E> entityRepo=(IdentifiedEntityRepo<E>) mappedRepo;
                resultRepo = entityRepo;
            }
        }
        
        if (mappedRepo == null) {
            logger.info("getIdentifiedEntityRepository(" + entityClass.getSimpleName() + ") created new instance");
        }

        return resultRepo;
    }

    @Override
    public <E extends MutableIdentity> IdentifiedEntityRepo<E> getPolymorphicEntityRepository(Class<E> entityClass) {
        Assert.notNull(entityClass, "No entity class provided");

        final IdentifiedEntityRepo<?>   mappedRepo;
        final IdentifiedEntityRepo<E>   resultRepo;
        synchronized(polymporphicRepos) {
            if ((mappedRepo=polymporphicRepos.get(entityClass)) == null) {
                resultRepo = createPolymorphicIdentifiedEntityRepo(entityClass);
                polymporphicRepos.put(entityClass, resultRepo);
            } else {
                @SuppressWarnings("unchecked")
                IdentifiedEntityRepo<E> entityRepo=(IdentifiedEntityRepo<E>) mappedRepo;
                resultRepo = entityRepo;
            }
        }
        
        if (mappedRepo == null) {
            logger.info("getPolymorphicEntityRepository(" + entityClass.getSimpleName() + ") created new instance");
        }

        return resultRepo;
    }

    protected <E extends MutableIdentity> IdentifiedEntityRepo<E> createPolymorphicIdentifiedEntityRepo(Class<E> entityClass) {
        RawPropertiesRepo<E>    rawRepo=getRawEntityRepository(entityClass);
        return new RawPropertiesPolymorphicIdentifiedEntityRepoImpl<E>(rawRepo);
    }

    protected <E extends MutableIdentity> IdentifiedEntityRepo<E> createIdentifiedEntityRepository(Class<E> entityClass) {
        RawPropertiesRepo<E>    rawRepo=getRawEntityRepository(entityClass);
        return new RawPropertiesIdentifiedEntityRepoImpl<E>(rawRepo);
    }

    protected <E extends Identified> RawPropertiesRepo<E> createRawPropertiesRepo(Class<E> entityClass) {
        return new RawPropertiesRepoImpl<E>(entityClass, jdbcOperations, conversionService);
    }
}
