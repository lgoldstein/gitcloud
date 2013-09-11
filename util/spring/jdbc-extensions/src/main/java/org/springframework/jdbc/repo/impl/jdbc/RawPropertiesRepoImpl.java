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
package org.springframework.jdbc.repo.impl.jdbc;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.InternalIdSetter;
import org.springframework.jdbc.core.JdbcOperationsUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.repo.Identified;
import org.springframework.jdbc.repo.IdentifiedEntityIdGenerator;
import org.springframework.jdbc.repo.impl.AbstractRawPropertiesRepoImpl;
import org.springframework.jdbc.repo.impl.IdentifiedEntityIdGeneratorImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 11:01:22 AM
 */
@Transactional
public class RawPropertiesRepoImpl<E extends Identified> extends AbstractRawPropertiesRepoImpl<E> {
    public static final String ENTITY_PROPERTIES_TABLE="EntityProperties";
    public static final String  PROPS_TABLE_ALIAS="props";
    public static final String  PROP_OWNER_COL="propertyOwner";
    public static final String  PROP_NAME_COL="propertyName";
    public static final String  PROP_TYPE_COL="propertyType";
    public static final String  PROP_VALUE_COL="propertyValue";

    public static final String  IDENTIFIED_ENTITIES_TABLE="IdentifiedEntities";
        public static final String  ENTITIES_TABLE_ALIAS="entities";
        public static final String  INTERNAL_ID_COL="id";
        public static final String  ENTITY_ID_COL="entityId";
        public static final String  ENTITY_TYPE_COL="entityType";

    protected final NamedParameterJdbcOperations    jdbcAccessor;
    protected final RowMapper<Pair<String,Serializable>>  valueMapper;

    public RawPropertiesRepoImpl(Class<E> entityClass, DataSource ds, ConversionService converter) {
        this(entityClass, new NamedParameterJdbcTemplate(ds) , converter);
    }

    public RawPropertiesRepoImpl(Class<E> entityClass, DataSource ds, ConversionService converter, IdentifiedEntityIdGenerator<E> idGen) {
        this(entityClass, new NamedParameterJdbcTemplate(ds) , converter, idGen);
    }

    public RawPropertiesRepoImpl(Class<E> entityClass, NamedParameterJdbcOperations jdbcOps, ConversionService converter) {
        this(entityClass, jdbcOps, converter, new IdentifiedEntityIdGeneratorImpl<E>(entityClass));
    }

    public RawPropertiesRepoImpl(Class<E> entityClass, NamedParameterJdbcOperations jdbcOps, ConversionService converter, IdentifiedEntityIdGenerator<E> idGen) {
        super(entityClass, converter, idGen);
        
        jdbcAccessor = Validate.notNull(jdbcOps, "No JDBC accessor", ArrayUtils.EMPTY_OBJECT_ARRAY);
        valueMapper = JdbcOperationsUtils.getMappedPropertyValueMapper(
                PROP_NAME_COL, PROP_TYPE_COL, PROP_VALUE_COL, loader, conversionService);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean entityExists(String id) {
        if (StringUtils.isEmpty(id)) {
            return false;
        }

        Object  internalId=findInternalId(id);
        if (internalId == null) {
            return false;   // debug breakpoint
        } else {
            return true;
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Map<String,Serializable> getProperties(E entity) {
        return getProperties((entity == null) ? null : entity.getId());
    }

    @Override
    @Transactional(readOnly=true)
    public Map<String, Serializable> getProperties(String id) {
        Pair<?,Map<String,Serializable>>   propVals=resolveProperties(id);
        if (propVals == null) {
            return Collections.emptyMap();
        } else {
            return propVals.getRight();
        }
    }

    @Override
    public Map<String, Serializable> removeProperties(String id) {
        Pair<?,Map<String,Serializable>>   propVals=resolveProperties(id);
        Object                             internalId=(propVals == null) ? null : propVals.getLeft();
        if (internalId == null) {
            return  null;
        }

        removeOwnerProperties(id, internalId);

        int nRows=jdbcAccessor.update(
                "DELETE FROM " + IDENTIFIED_ENTITIES_TABLE + " WHERE " + INTERNAL_ID_COL + " = :" + INTERNAL_ID_COL,
                Collections.singletonMap(INTERNAL_ID_COL, internalId));
        if (nRows > 1) {
            logger.warn("removeProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] unexpected rows count (" + nRows + ") for internal ID=" + internalId);
        }

        Map<String,Serializable>    props=propVals.getRight();
        if (ExtendedMapUtils.isEmpty(props)) {
            if (logger.isDebugEnabled()) {
                logger.debug("removeProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] no properties");
            }
            return null;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("removeProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] properties count: " + props.size());
        }

        return props;
    }

    /**
     * @param id The entity ID
     * @return A {@link Pair} whose left hand holds the internal identifier
     * and left hand the properties {@link Map} - <code>null</code> if cannot
     * locate internal identifier value
     */
    Pair<Object,Map<String,Serializable>> resolveProperties(String id) {
       Object  internalId=findInternalId(id);
        if (internalId == null) {
            return null;
        }
        
        List<Pair<String,Serializable>> propVals=
                jdbcAccessor.query("SELECT * FROM " + ENTITY_PROPERTIES_TABLE
                                 + " WHERE " + PROP_OWNER_COL + " = :" + INTERNAL_ID_COL,
                                   Collections.singletonMap(INTERNAL_ID_COL, internalId),
                                   valueMapper);
        if (ExtendedCollectionUtils.isEmpty(propVals)) {
            return Pair.of(internalId, Collections.<String,Serializable>emptyMap());
        }
        
        Map<String, Serializable>   propsMap=new TreeMap<String, Serializable>(String.CASE_INSENSITIVE_ORDER);
        for (Pair<String,Serializable> p : propVals) {
            String          propName=p.getKey();
            Serializable    propValue=p.getValue();
            if (propValue == null) {
                continue;   // ignore null(s)
            }

            Serializable    prevValue=propsMap.put(propName, propValue);
            if (prevValue != null) {
                throw new IllegalStateException("getProperties(" + getEntityClass().getSimpleName() + ")[" + id + "]"
                                              + " multiple values for property=" + propName
                                              + ": " + propValue + ", " + prevValue);
            }
        }
        
        return Pair.of(internalId, propsMap);
    }

    @Override
    @SuppressWarnings("synthetic-access")
    public void setProperties(final String id, final Map<String,?> props) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("setProperties(" + getEntityClass().getSimpleName() + ") no identifier");
        }

        if (ExtendedMapUtils.isEmpty(props)) {
            throw new IllegalArgumentException("setProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] no properties");
        }

        validatePropertyValues(id, props);

        Object  internalId=findInternalId(id);
        if (internalId == null) {
            internalId = createEntityEntry(id);
        }

        removeEntityProperties(id);

        Class<?>          idType=internalId.getClass();
        InternalIdSetter  idSetter=InternalIdSetter.SETTERS_MAP.get(idType);
        if (idSetter == null) {
            throw new UnsupportedOperationException("setProperties(" + getEntityClass().getSimpleName() + ")[" + id + "]"
                                                  + " no identifier setter for type " + idType.getSimpleName());
        }
        
        @SuppressWarnings("unchecked")
        Map<String,Object>[]  batchValues=new Map[props.size()];
        int                   batchIndex=0;
        final Object          assignedId=internalId;
        for (Map.Entry<String,?> pe : props.entrySet()) {
            final String      propName=pe.getKey();
            Object            orgValue=pe.getValue();
            final Class<?>    propType=JdbcOperationsUtils.resolveEffectivePropertyType(orgValue);
            final Object      propValue=JdbcOperationsUtils.resolveEffectivePropertyValue(orgValue);
            batchValues[batchIndex] = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER) {
                    private static final long serialVersionUID = 1L;
                    
                    {
                        if (!conversionService.canConvert(propType, String.class)) {
                            throw new UnsupportedOperationException("setProperties(" + id + ")"
                                                                   + " cannot convert " + propName + "[" + propType.getSimpleName() + "]"
                                                                   + " to string for value=" + propValue);
                        }

                        put(PROP_OWNER_COL, assignedId);
                        put(PROP_NAME_COL, propName);
                        put(PROP_TYPE_COL, propType.getName());
                        put(PROP_VALUE_COL, conversionService.convert(propValue, String.class));
                    }
                };
            batchIndex++;
        }

        try {
            int[]    changeSet=jdbcAccessor.batchUpdate(
                      "INSERT INTO " + ENTITY_PROPERTIES_TABLE
                    + " (" + PROP_OWNER_COL + "," + PROP_NAME_COL + "," + PROP_TYPE_COL + "," + PROP_VALUE_COL + ")"
                    + " VALUES(:" + PROP_OWNER_COL + ",:" + PROP_NAME_COL + ",:" + PROP_TYPE_COL + ",:" + PROP_VALUE_COL + ")",
                      batchValues);
            if (logger.isDebugEnabled()) {
                logger.debug("setProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] batch size=" + ExtendedArrayUtils.length(changeSet));
            }
        } catch(RuntimeException e) {
            logger.warn("setProperties(" + getEntityClass().getSimpleName() + ")[" + id + "]"
                      + " failed (" + e.getClass().getSimpleName() + ") to insert props=" + props
                      + ": " + e.getMessage());
            throw e;
        }
    }

    void validatePropertyValues(String id, Map<String,?> props) {
        if (ExtendedMapUtils.isEmpty(props)) {
            return;
        }

        final String    vid=getEntityClass().getSimpleName() + "[" + id + "]";
        for (Map.Entry<String,?> pe : props.entrySet()) {
            JdbcOperationsUtils.validatePropertyValue(vid, pe.getKey(), pe.getValue(), conversionService);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> listEntities() {
        return jdbcAccessor.query(
                "SELECT " + ENTITY_ID_COL + " FROM " + IDENTIFIED_ENTITIES_TABLE
              + " WHERE " + ENTITY_TYPE_COL + " =:" + ENTITY_TYPE_COL,
                Collections.singletonMap(ENTITY_TYPE_COL, getEntityClass().getSimpleName()),
                JdbcOperationsUtils.AS_STRING_MAPPER);
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> listMatchingIdentifiers(final String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return Collections.emptyList();
        }

        return jdbcAccessor.query(
                "SELECT " + ENTITY_ID_COL + " FROM " + IDENTIFIED_ENTITIES_TABLE
              + " WHERE " + ENTITY_TYPE_COL + " =:" + ENTITY_TYPE_COL
              + " AND " + ENTITY_ID_COL + " LIKE :" + ENTITY_ID_COL,
                new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER) {
                    private static final long serialVersionUID = 1L;

                    {
                        put(ENTITY_TYPE_COL, getEntityClass().getSimpleName());
                        put(ENTITY_ID_COL, pattern.replace('*', '%'));
                    }
                },
                JdbcOperationsUtils.AS_STRING_MAPPER);
    }

    @Override
    @Transactional(readOnly=true)
    public List<String> findEntities(final String propName, final Predicate<? super Serializable> predicate) {
        if (StringUtils.isEmpty(propName)) {
            throw new IllegalArgumentException("findEntities(" + getEntityClass().getSimpleName() + ") no property specified");
        }
        
        if (predicate == null) {
            throw new IllegalArgumentException("findEntities(" + getEntityClass().getSimpleName() + ")[" + propName + "] no predicate");
        }

        final Set<String>   internalIdsSet=new HashSet<String>();
        final Set<String>   matchingSet=jdbcAccessor.query(
                "SELECT " + PROPS_TABLE_ALIAS + "." + PROP_OWNER_COL + " AS " + PROP_OWNER_COL
                    + " ," + PROPS_TABLE_ALIAS + "." + PROP_NAME_COL + " AS " + PROP_NAME_COL
                    + " ," + PROPS_TABLE_ALIAS + "." + PROP_TYPE_COL + " AS " + PROP_TYPE_COL
                    + " ," + PROPS_TABLE_ALIAS + "." + PROP_VALUE_COL + " AS " + PROP_VALUE_COL
                    + " ," + ENTITIES_TABLE_ALIAS + "." + ENTITY_ID_COL + " AS " + ENTITY_ID_COL
              + " FROM " + ENTITY_PROPERTIES_TABLE + " AS " + PROPS_TABLE_ALIAS
              + " INNER JOIN " + IDENTIFIED_ENTITIES_TABLE + " AS " + ENTITIES_TABLE_ALIAS
                  + " ON " + ENTITIES_TABLE_ALIAS + "." + INTERNAL_ID_COL + " = " + PROPS_TABLE_ALIAS + "." + PROP_OWNER_COL
                  + " AND " + ENTITIES_TABLE_ALIAS + "." + ENTITY_TYPE_COL + " = :" + ENTITY_TYPE_COL
                  + " AND " + PROPS_TABLE_ALIAS + "." + PROP_NAME_COL + " = :" + PROP_NAME_COL,
                new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER) {
                    private static final long serialVersionUID = 1L;
                    
                    {
                        put(ENTITY_TYPE_COL, getEntityClass().getSimpleName());
                        put(PROP_NAME_COL, propName);
                    }
                },
                new ResultSetExtractor<Set<String>>() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Set<String> extractData(ResultSet rs)
                            throws SQLException, DataAccessException {
                        while(rs.next()) {
                            int                         rowNum=rs.getRow();
                            Pair<String,Serializable>   rowValue=valueMapper.mapRow(rs, rowNum);
                            String                      rowName=rowValue.getLeft();
                            String                      ownerId=rs.getString(ENTITY_ID_COL);
                            // just for safety
                            if (ExtendedStringUtils.safeCompare(propName, rowName, false) != 0) {
                                logger.warn("findEntities(" + getEntityClass().getSimpleName() + ")[" + propName + "]"
                                          + " mismatched property name at row " + rowNum
                                          + " for owner=" + ownerId + ": " + rowName);
                                continue;
                            }
    
                            Serializable propValue=rowValue.getRight();
                            if (!predicate.evaluate(propValue)) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("findEntities(" + getEntityClass().getSimpleName() + ")[" + propName + "]"
                                                + " skip row " + rowNum + " value=" + propValue + " for owner=" + ownerId);
                                }
                                continue;
                            }
                            
                            if (StringUtils.isEmpty(ownerId)) {
                                throw new SQLException("findEntities(" + getEntityClass().getSimpleName() + ")[" + propName + "]"
                                                      + " no owner for row " + rowNum + " on matching value=" + propValue);
                            }

                            if (logger.isTraceEnabled()) {
                                logger.trace("findEntities(" + getEntityClass().getSimpleName() + ")[" + propName + "]"
                                           + " matched row " + rowNum + " value=" + propValue + " for owner=" + ownerId);
                            }

                            if (!internalIdsSet.add(ownerId)) {
                                continue;   // debug breakpoint
                            }
                        }
    
                        return internalIdsSet;
                    }
                });
        if (ExtendedCollectionUtils.isEmpty(matchingSet)) {
            return Collections.emptyList();
        } else {
            return new ArrayList<String>(matchingSet);
        }
    }

    @Override
    protected <R> List<R> removeAll(Class<R> elementType, Transformer<Pair<String,Map<String,?>>,? extends R> transformer, Collection<String> ids) {
        Assert.notNull(elementType, "No removal result element type specified");
        if (ExtendedCollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        List<R> result=new ArrayList<>(ids.size());
        for (String id : ids) {
            Map<String,?>   props=removeProperties(id);
            if (ExtendedMapUtils.isEmpty(props)) {
                continue;
            }
            
            R   element=transformer.transform(Pair.<String,Map<String,?>>of(id, props));
            if (element == null) {
                continue;
            }
            
            result.add(element);
        }
        
        return result;
    }

    /**
     * Removes the associated entity properties
     * @param id The entity identifier
     * @return The internal identifier (or <code>null</code> if cannot resolve it)
     * @see #removeOwnerProperties(String, String)
     */
    private Object removeEntityProperties(String id) {
        Object  internalId=findInternalId(id);
        if (internalId == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("removeEntityProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] no mapped entity");
                }
            } else {
                removeOwnerProperties(id, internalId);
            }
            
            return internalId;
    }

    /**
     * Removes the associated entity properties
     * @param id The entity identifier
     * @param internalId The internal identifier
     * @return Number of affected rows
     */
    private int removeOwnerProperties(String id, Object internalId) {
        int nRows=jdbcAccessor.update(
                "DELETE FROM " + ENTITY_PROPERTIES_TABLE + " WHERE " + PROP_OWNER_COL + " = :" + INTERNAL_ID_COL,
                Collections.singletonMap(INTERNAL_ID_COL, internalId));
        if (logger.isDebugEnabled()) {
            logger.debug("removeOwnerProperties(" + getEntityClass().getSimpleName() + ")[" + id + "] affected rows count: " + nRows);
        }
    
        return nRows;
    }

    private Object createEntityEntry(final String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("createEntityEntry(" + getEntityClass().getSimpleName() + ") no ID provided");
        }
        
        // NOTE !!! we rely on auto-generated internal ID
        try {
            int nRows=jdbcAccessor.update(
                    "INSERT INTO " + IDENTIFIED_ENTITIES_TABLE
                  + "(" + ENTITY_TYPE_COL + "," + ENTITY_ID_COL + ")"
                  + " VALUES (:" + ENTITY_TYPE_COL + ",:" + ENTITY_ID_COL + ")",
                  new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER) {
                        private static final long serialVersionUID = 1L;
                        
                        {
                            put(ENTITY_TYPE_COL, getEntityClass().getSimpleName());
                            put(ENTITY_ID_COL, id);
                        }
                    });
            if (nRows < 0) {
                logger.warn("createEntityEntry(" + getEntityClass().getSimpleName() + ")[" + id + "] bad rows count: " + nRows);
            }
        } catch(RuntimeException e) {
            logger.warn("createEntityEntry(" + getEntityClass().getSimpleName() + ")[" + id + "]"
                      + " failed (" + e.getClass().getSimpleName() + ") to create entry: " + e.getMessage());
            throw e;
        }

        Object  internalId=findInternalId(id);
        if (internalId == null) {
            throw new IllegalStateException("createEntityEntry(" + getEntityClass().getSimpleName() + ")[" + id + "] no internal ID available");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("createEntityEntry(" + getEntityClass().getSimpleName() + ")[" + id + "]: " + internalId);
        }

        return internalId;
    }

    private Object findInternalId(final String id) {
        List<?>    candidates=jdbcAccessor.query(
                "SELECT " + INTERNAL_ID_COL + " FROM " + IDENTIFIED_ENTITIES_TABLE
              + " WHERE " + ENTITY_TYPE_COL + " = :" + ENTITY_TYPE_COL
              + " AND " + ENTITY_ID_COL + " =:" + ENTITY_ID_COL,
                  new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER) {
                            private static final long serialVersionUID = 1L;
                          
                            {
                                put(ENTITY_TYPE_COL, getEntityClass().getSimpleName());
                                put(ENTITY_ID_COL, id);
                            }
                        },
                  JdbcOperationsUtils.AS_OBJECT_MAPPER);
        if (ExtendedCollectionUtils.isEmpty(candidates)) {
            return null;
        }
        
        if (candidates.size() != 1) {
            throw new IllegalStateException("findInternalId(" + getEntityClass().getSimpleName() + ")[" + id + "] multiple matches: " + candidates);
        }
        
        Object  internalId=candidates.get(0);
        if (internalId == null) {
            throw new IllegalStateException("findInternalId(" + getEntityClass().getSimpleName() + ")[" + id + "] null/empty internal ID");
        }
        
        return internalId;
    }
}
