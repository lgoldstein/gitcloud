/*
 * Copyright 2002-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.springframework.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

/**
 * @author Lyor G.
 */
public class ExtendedPlaceholderResolverUtils {
	public ExtendedPlaceholderResolverUtils() {
		super();
	}

	public static final NamedExtendedPlaceholderResolver	EMPTY_RESOLVER=
			new NamedExtendedPlaceholderResolver() {
				@Override
				public String resolvePlaceholder(String placeholderName, String defaultValue) {
					return defaultValue;
				}

				@Override
				public String resolvePlaceholder(String placeholderName) {
					return null;
				}

				@Override
				public Collection<String> getPlaceholderNames() {
					return Collections.emptyList();
				}
			};

    public static final NamedExtendedPlaceholderResolver    SYSPROPS_RESOLVER=
            new NamedExtendedPlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String placeholderName, String defaultValue) {
                    return System.getProperty(placeholderName, defaultValue);
                }

                @Override
                public String resolvePlaceholder(String placeholderName) {
                    return System.getProperty(placeholderName);
                }

                @Override
                public List<String> getPlaceholderNames() {
                    return ExtendedCollectionUtils.collectToList(System.getProperties().keySet(), ExtendedStringUtils.SAFE_TOSTRING_XFORMER);
                }
        };

    public static final NamedExtendedPlaceholderResolver    ENVIRON_RESOLVER=
            new NamedExtendedPlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String placeholderName, String defaultValue) {
                    String  value=resolvePlaceholder(placeholderName);
                    if (value == null) {
                        return defaultValue;
                    } else {
                        return value;
                    }
                }

                @Override
                public String resolvePlaceholder(String placeholderName) {
                    return System.getenv(placeholderName);
                }

                @Override
                public SortedSet<String> getPlaceholderNames() {
                    return new TreeSet<String>(System.getenv().keySet());
                }
        };

	public static final ExtendedPlaceholderResolver toPlaceholderResolver(final PropertyResolver resolver) {
		if (resolver == null) {
			return EMPTY_RESOLVER;
		} else {
			return new ExtendedPlaceholderResolver() {
				@Override
				public String resolvePlaceholder(String placeholderName) {
					return resolver.getProperty(placeholderName);
				}

				@Override
				public String resolvePlaceholder(String placeholderName, String defaultValue) {
					return resolver.getProperty(placeholderName, defaultValue);
				}
			};
		}
	}
    
    public static final ExtendedPlaceholderResolver toPlaceholderResolver(final PropertySources sources) {
        if (sources == null) {
            return EMPTY_RESOLVER;
        } else {
            return new AbstractExtendedPlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String placeholderName) {
                    for (PropertySource<?> src : sources) {
                        Object  value=src.getProperty(placeholderName);
                        String  s=ExtendedStringUtils.safeToString(value);
                        if (s == null) {
                            continue;   // debug breakpoint
                        } else {
                            return s;
                        }
                    }
                    
                    return null;
                }
            };
        }
    }
	
	public static final ExtendedPlaceholderResolver toPlaceholderResolver(final PropertySource<?> src) {
		if (src == null) {
			return EMPTY_RESOLVER;
		} else {
			return new AbstractExtendedPlaceholderResolver() {
				@Override
				public String resolvePlaceholder(String placeholderName) {
					Object	value=src.getProperty(placeholderName);
					if (value == null) {
						return null;
					} else {
						return value.toString();
					}
				}
			};
		}
	}
	
	public static final NamedExtendedPlaceholderResolver toPlaceholderResolver(final EnumerablePropertySource<?> src) {
		if (src == null) {
			return EMPTY_RESOLVER;
		} else {		
			return new NamedExtendedPlaceholderResolver() {
				@Override
				public String resolvePlaceholder(String placeholderName, String defaultValue) {
					String	value=resolvePlaceholder(placeholderName);
					if (value == null) {
						return defaultValue;
					} else {
						return value;
					}
				}

				@Override
				public String resolvePlaceholder(String placeholderName) {
					Object	value=src.getProperty(placeholderName);
					if (value == null) {
						return null;
					} else {
						return value.toString();
					}
				}

				@Override
				public Collection<String> getPlaceholderNames() {
					return ExtendedArrayUtils.asList(src.getPropertyNames());
				}
			};
		}
	}
	
	public static final NamedExtendedPlaceholderResolver toPlaceholderResolver(final Properties props) {
        if (ExtendedMapUtils.isEmpty(props)) {
            return EMPTY_RESOLVER;
        } else {

    	    return new NamedExtendedPlaceholderResolver() {
                @Override
                public Collection<String> getPlaceholderNames() {
                    return ExtendedCollectionUtils.collectToList(props.keySet(), ExtendedStringUtils.SAFE_TOSTRING_XFORMER);
                }

                @Override
                public String resolvePlaceholder(String placeholderName) {
                    return props.getProperty(placeholderName);
                }
    
                @Override
                public String resolvePlaceholder(String placeholderName, String defaultValue) {
                    return props.getProperty(placeholderName, defaultValue);
                }
    	    };
        }
	}

	public static final NamedExtendedPlaceholderResolver toPlaceholderResolver(final Map<String,String> props) {
		if (ExtendedMapUtils.isEmpty(props)) {
			return EMPTY_RESOLVER;
		} else {
			return new NamedExtendedPlaceholderResolver() {
				@Override
				public String resolvePlaceholder(String placeholderName, String defaultValue) {
					String	value=resolvePlaceholder(placeholderName);
					if (value == null) {
						return defaultValue;
					} else {
						return value;
					}
				}

				@Override
				public String resolvePlaceholder(String placeholderName) {
					return props.get(placeholderName);
				}

				@Override
				public Collection<String> getPlaceholderNames() {
					// return a copy to avoid concurrent modifications
					return new TreeSet<String>(props.keySet());
				}
			};
		}
	}
	
	/**
	 * Invokes a {@link Closure} on all the non-<code>null</code> placeholder values
	 * @param resolver The {@link NamedExtendedPlaceholderResolver} to query
	 * @param closure The {@link Closure} to invoke
	 * @throws IllegalArgumentException if no resolver or closure
	 */
	public static final void forAllEntriesDo(NamedExtendedPlaceholderResolver resolver, Closure<Map.Entry<String,String>> closure) {
	    Assert.notNull(resolver, "No resolver");
	    Assert.notNull(closure, "No closure");
	    
	    Collection<String> names=resolver.getPlaceholderNames();
	    if (ExtendedCollectionUtils.isEmpty(names)) {
	        return;
	    }
	    
	    for (String propName : names) {
	        String propValue=resolver.resolvePlaceholder(propName);
	        if (propValue == null) {
	            continue;
	        }
	        
	        closure.execute(Pair.of(propName, propValue));
	    }
	}
}
