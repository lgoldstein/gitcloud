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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a callback that can be used to set the identifier column
 * @author Lyor Goldstein
 * @since Sep 11, 2013 10:38:24 AM
 */
public interface InternalIdSetter {
    void setId(PreparedStatement ps, Object internalId) throws SQLException;

    /**
     * A pre-defined (un-modifiable) {@link Map} of {@link InternalIdSetter}s
     * for some useful identifier types ({@link Long}, {@link String}, {@link Integer})
     * where the identifier column is assumed to be the 1st one
     */
    static final Map<Class<?>,InternalIdSetter> SETTERS_MAP=
            Collections.unmodifiableMap(new HashMap<Class<?>,InternalIdSetter>() {
                private static final long serialVersionUID = 1L;
                {
                    put(Long.class, new InternalIdSetter() {
                            @Override
                            public void setId(PreparedStatement ps, Object internalId) throws SQLException {
                                ps.setLong(1, ((Number) internalId).longValue());
                            }
                        });
                    put(Integer.class, new InternalIdSetter() {
                            @Override
                            public void setId(PreparedStatement ps, Object internalId) throws SQLException {
                                ps.setInt(1, ((Number) internalId).intValue());
                            }
                        });
                    put(String.class, new InternalIdSetter() {
                            @Override
                            public void setId(PreparedStatement ps, Object internalId) throws SQLException {
                                ps.setString(1, internalId.toString());
                            }
                        });
                }
            });
}