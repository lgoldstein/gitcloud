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
package org.springframework.jdbc.core;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.convert.ConversionService;

/**
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:36:07 AM
 */
public class JdbcOperationsUtils {
    /**
     * A {@link RowMapper} that returns the value of the 1st column in the {@link ResultSet}
     */
    public static final RowMapper<Object>  AS_OBJECT_MAPPER=new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getObject(1);
            }
        };

    /**
     * A {@link RowMapper} that returns the value of the 1st column in the {@link ResultSet}
     * as a {@link String}
     * @see #AS_OBJECT_MAPPER 
     */
    public static final RowMapper<String>   AS_STRING_MAPPER=new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
               return ExtendedStringUtils.safeToString(AS_OBJECT_MAPPER.mapRow(rs, rowNum));
            }
        };

    public static final boolean putIfFilterValue(Map<String,Object> params, String propName, String propValue) {
        if (StringUtils.isEmpty(propValue)) {
            return false;
        }

        params.put(propName, propValue);
        return true;
    }

    public static final RowMapper<Pair<String,Serializable>> getMappedPropertyValueMapper(
            final String propNameCol, final String propTypeCol, final String propValCol, final ClassLoader loader, final ConversionService converter) {
        return new RowMapper<Pair<String,Serializable>>() {
            @Override
            public Pair<String, Serializable> mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                String  propName=rs.getString(propNameCol);
                String  propType=rs.getString(propTypeCol);
                final Class<?>  targetType;
                try {
                    targetType = ClassUtils.getClass(loader, propType);
                } catch(ClassNotFoundException e) {
                    throw new SQLException("mapRow(" + propName + ")@" + rowNum + ": unknown property type: " + propType);
                }

                if (!Serializable.class.isAssignableFrom(targetType)) {
                    throw new SQLException("mapRow(" + propName + ")@" + rowNum + ": not serializable: " + propType);
                }

                if (!converter.canConvert(String.class, targetType)) {
                    throw new SQLException("mapRow(" + propName + ")@" + rowNum + ": cannot convert from " + propType);
                }

                String          propValue=rs.getString(propValCol);
                Serializable    convertedValue=(Serializable) converter.convert(propValue, targetType);
                return Pair.of(propName, convertedValue);
            }
        };
    }

    public static final void validatePropertyValue(String id, String propName, Object propValue, ConversionService converter) {
        if (StringUtils.isEmpty(propName)) {
            throw new IllegalStateException("validatePropertyValue(" + id + ") no property name");
        }

        if (propValue == null) {
            throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "] no value");
        }

        if (!(propValue instanceof Serializable)) {
            throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "] not serializable");
        }

        Class<?>    propType=resolveEffectivePropertyType(propValue);
        if (Date.class.isAssignableFrom(propType)
         || Calendar.class.isAssignableFrom(propType)) {
            return; // Date(s) have a special handling
        }

        if (propType == Class.class) {
            Class<?>    valueClass=(Class<?>) propValue;
            if (Proxy.isProxyClass(valueClass)) {
                throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "] proxies N/A");
            }

            if (valueClass.isAnonymousClass()) {
                throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "]"
                                              + " anonymous classes N/A: " + valueClass.getName());
            }

            if (valueClass.isLocalClass()) {
                throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "]"
                                              + " local classes N/A: " + valueClass.getName());
            }

            if (valueClass.isArray()) {
                throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "]"
                                              + " array classes N/A: " + valueClass.getName());
            }

            int mods=valueClass.getModifiers();
            if (!Modifier.isPublic(mods)) {
                throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "]"
                                              + " non-public classes N/A: " + valueClass.getName());
            }
        }

        if (!converter.canConvert(String.class, propType)) {
            throw new IllegalStateException("validatePropertyValue(" + id + ")[" + propName + "]"
                                          + " cannot convert a string to a " + propType.getSimpleName());
        }
    }

    public static final Object resolveEffectivePropertyValue(Object propValue) {
        if (propValue == null) {
            return null;
        } else if (propValue instanceof Class) {
            return ((Class<?>) propValue).getName();
        } else {
            return propValue;
        }
    }

    public static final Class<?> resolveEffectivePropertyType(Object propValue) {
        if (propValue == null) {
            return null;
        }

        Class<?>    propType=propValue.getClass();
        // handle constant body values
        if (Enum.class.isAssignableFrom(propType)) {
            propType = ((Enum<?>) propValue).getDeclaringClass();
        } else if (Calendar.class.isAssignableFrom(propType)) {
            propType = Calendar.class;
        }

        return propType;
    }
}
