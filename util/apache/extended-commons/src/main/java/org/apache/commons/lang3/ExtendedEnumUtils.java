/*
 * Copyright 2013 Lyor Goldstein
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
 */

package org.apache.commons.lang3;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.ExtendedSetUtils;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.numbers.ints.AbstractExtendedValue2IntTransfomer;
import org.apache.commons.collections15.numbers.ints.ExtendedValue2IntTransfomer;
import org.apache.commons.collections15.numbers.ints.Value2IntTransfomer;

/**
 * @author lgoldstein
 */
public class ExtendedEnumUtils extends EnumUtils {
	public ExtendedEnumUtils() {
		super();
	}

    /**
     * @param eClass The {@link Enum} class
     * @return A {@link Factory} that generates a new {@link EnumMap} every time
     * its {@linkplain Factory#create()} method is invoked
     */
    public static final <E extends Enum<E>,V> Factory<EnumMap<E,V>> enumMapFactory(final Class<E> eClass) {
        ExtendedValidate.notNull(eClass, "No enum class specified");
        // in case someone used a cast
        ExtendedValidate.isAssignableFrom(Enum.class, eClass, "Not an Enum class");

        return new Factory<EnumMap<E,V>>() {
            @Override
            public EnumMap<E,V> create () {
                return new EnumMap<E,V>(eClass);
            }
        };
    }

    /**
     * A convenience method that is missing from {@link EnumSet}
     * @param values The enumerated values
     * @return An {@link EnumSet} containing all the specified values
     */
    @SafeVarargs
    public static final <E extends Enum<E>> EnumSet<E> of (E ... values) {
        return EnumSet.copyOf(ExtendedArrayUtils.asList(values));
    }

    /**
     * @param eClass The {@link Enum} type
     * @return An empty {@link EnumSet} for the type
     * @throws NullPointerException if no enum class specified
     * @see EnumSet#noneOf(Class)
     */
    public static final <E extends Enum<E>> EnumSet<E> emptySet(Class<E> eClass) {
        return EnumSet.noneOf(ExtendedValidate.notNull(eClass, "No enum class specified"));
    }

    /**
     * @param eClass The {@link Enum} type
     * @return A {@link Factory} that returns an empty {@link EnumSet} of
     * the specified type every time {@link Factory#create()} is called
     * @throws NullPointerException if no enum class specified
     * @see #emptySet(Class)
     */
    public static final <E extends Enum<E>> Factory<EnumSet<E>> emptySetFactory(final Class<E> eClass) {
        ExtendedValidate.notNull(eClass, "No enum class specified");
        // in case someone used a cast
        ExtendedValidate.isAssignableFrom(Enum.class, eClass, "Not an Enum class");

        return new Factory<EnumSet<E>>() {
            @Override
            public EnumSet<E> create () {
                return emptySet(eClass);
            }
            
        };
    }

    /**
     * @param eClass The {@link Enum} type
     * @return An {@link EnumSet} that contains <U>all</U> the values for the type
     * @throws NullPointerException if no enum class specified
     * @see EnumSet#allOf(Class)
     */
    public static final <E extends Enum<E>> EnumSet<E> fullSet(Class<E> eClass) {
        return EnumSet.allOf(ExtendedValidate.notNull(eClass, "No enum class specified"));
    }

    /**
     * @param eClass The {@link Enum} type
     * @param comp A {@link Comparator} to use for sorting
     * @return A {@link SortedSet} of <U>all</U> the values, sorted according
     * to the provided comparator
     */
    public static final <E extends Enum<E>> SortedSet<E> fullSortedSet(Class<E> eClass, Comparator<? super E> comp) {
        return ExtendedCollectionUtils.append(ExtendedSetUtils.sortedSet(comp), fullSet(eClass));
    }

    /**
     * @param eClass The {@link Enum} type
     * @return A {@link Factory} that returns a <U>full</U> {@link EnumSet} of
     * the specified type every time {@link Factory#create()} is called
     * @throws NullPointerException if no enum class specified
     * @see #fullSet(Class)
     */
    public static final <E extends Enum<E>> Factory<EnumSet<E>> fullSetFactory(final Class<E> eClass) {
        // Avoid creating a new array of the constants
        final EnumSet<E>    allValues=fullSet(eClass);
        return new Factory<EnumSet<E>>() {
            @Override
            public EnumSet<E> create () {
                return EnumSet.copyOf(allValues);
            }
        };
    }

    /**
     * @param s1 1st {@link Collection} of {@link Enum}-s - may be {@code null}/empty
     * @param s2 2nd {@link Collection} of {@link Enum}-s - may be {@code null}/empty
     * @return A {@link Set} of all values from both collections
     */
    public static final <E extends Enum<E>> Set<E> union (Collection<E> s1, Collection<E> s2) {
        if (ExtendedCollectionUtils.isEmpty(s1)) {
            if (ExtendedCollectionUtils.isEmpty(s2)) {
                return Collections.emptySet();
            } else {
                return EnumSet.copyOf(s2);
            }
        } else if (ExtendedCollectionUtils.isEmpty(s2)) {
            return EnumSet.copyOf(s1);
        } else {
            Set<E>    ret=EnumSet.copyOf(s1);
            ret.addAll(s2);
            return ret;
        }
    }

    /**
     * @param s1 1st {@link Collection} of {@link Enum}-s - may be {@code null}/empty
     * @param s2 2nd {@link Collection} of {@link Enum}-s - may be {@code null}/empty
     * @return A {@link Set} of all values that are common for both collections
     */
    public static final <E extends Enum<E>> Set<E> intersect (Collection<E> s1, Collection<E> s2) {
        if (ExtendedCollectionUtils.isEmpty(s1)
         || ExtendedCollectionUtils.isEmpty(s2)) {
            return Collections.emptySet();
        }

        Set<E>  ret=null;   // cannot pre-allocate since don't know the class
        for (final E v : s1) {
            if (!s2.contains(v)) {
                continue;
            }

            if (ret == null) {
                ret = EnumSet.of(v);
            } else {
                ret.add(v);
            }
        }

        if (ret == null) {
            return Collections.emptySet();
        } else {
            return ret;
        }
    }

    private static final Transformer<Enum<?>,String>   nameExtract=
            new Transformer<Enum<?>,String>() {
                @Override
                public String transform (Enum<?> e) {
                    if (e == null) {
                        return null;
                    } else {
                        return e.name();
                    }
                }
            };
    /**
     * @return A {@link Transformer} that returns the {@link Enum#name()} value
     */
    @SuppressWarnings("unchecked")
    public static final <E extends Enum<E>> Transformer<E,String> nameExtractor() {
        return (Transformer<E,String>) nameExtract;
    }

    /**
     * @param eClass The {@link Enum} type
     * @return An {@link ExtendedTransformer} that returns the {@link Enum#name()} value
     */
    public static final <E extends Enum<E>> ExtendedTransformer<E,String> nameExtractor(final Class<E> eClass) {
        return new AbstractExtendedTransformer<E,String>(eClass, String.class) {
            @Override
            public String transform (E e) {
                if (e == null) {
                    return null;
                } else {
                    return e.name();
                }
            }
        };
    }

    /**
     * @param caseSensitive Whether the mapping is case sensitive
     * @param values The {@link Collection} of {@link Enum} constants to be mapped
     * - ignored if <code>null</code>/empty (i.e., an empty map is returned)
     * @return A {@link SortedMap} whose keys are the {@link Enum#name()}
     * values and values are the matching {@link Enum} values
     */
    public static final <E extends Enum<E>> SortedMap<String,E> getEnumMap(boolean caseSensitive, Collection<? extends E> values) {
        return ExtendedMapUtils.mapSortedCollectionValues(
                true, ExtendedEnumUtils.<E>nameExtractor(), ExtendedStringUtils.stringCaseComparator(caseSensitive), values);
    }

	/**
	 * @param enumClass The {@link Enum} class whose constants are to be mapped
	 * @param caseSensitive Whether the mapping is case sensitive
	 * @return A {@link SortedMap} whose keys are the {@link Enum#name()}
	 * values and values are the matching {@link Enum} values
	 * @see #fullSet(Class)
	 * @see #getEnumMap(boolean, Collection)
	 */
	public static final <E extends Enum<E>> SortedMap<String,E> getEnumMap(Class<E> enumClass, boolean caseSensitive) {
	    return getEnumMap(caseSensitive, fullSet(enumClass));
	}
	
    /**
     * @param caseSensitive Whether the mapping is case sensitive
     * @param values The {@link Enum} constants to be mapped - ignored if
     * <code>null</code>/empty (i.e., an empty map is returned)
     * @return A {@link SortedMap} whose keys are the {@link Enum#name()}
     * values and values are the matching {@link Enum} values
     */
	@SafeVarargs
    public static final <E extends Enum<E>> SortedMap<String,E> getEnumMap(boolean caseSensitive, E ... values) {
	    return getEnumMap(caseSensitive, of(values));
	}

    /**
     * @param eClass The {@link Enum} type
     * @param caseSensitive Whether name lookup should be case sensitive
     * (recommended: <code>false</code>)
     * @return An {@link ExtendedTransformer} whose {@link Transformer#transform(Object)}
     * method returns the matching enum value for a {@link String} (<code>null</code>
     * if no match found). <B>Note:</B> this method is preferable over
     * {@link EnumUtils#getEnum(Class, String)} call since it allows for case
     * insensitive lookup and also does not involve handling the exception
     * thrown by {@link Enum#valueOf(Class, String)} if no match found
     * @see #getEnumMap(Class, boolean)
     */
    public static final <E extends Enum<E>> ExtendedTransformer<String,E> fromNameTransformer(final Class<E> eClass, boolean caseSensitive) {
        final Map<String,E> namesMap=getEnumMap(eClass, caseSensitive);
        return new AbstractExtendedTransformer<String,E>(String.class, eClass) {
            @Override
            public E transform (String name) {
                if (StringUtils.isEmpty(name)) {
                    return null;
                } else {
                    return namesMap.get(name);
                }
            }
        };
    }

    /**
     * @param caseSensitive Whether name lookup should be case sensitive
     * (recommended: <code>false</code>)
     * @param values The values to be used to lookup for a match
     * @return A {@link Transformer} whose {@link Transformer#transform(Object)}
     * method returns the matching enum value for a {@link String} (<code>null</code>
     * if no match found). <B>Note:</B> this method is preferable over
     * {@link EnumUtils#getEnum(Class, String)} call since it allows for case
     * insensitive lookup and also does not involve handling the exception
     * thrown by {@link Enum#valueOf(Class, String)} if no match found
     * @see #fromNameTransformer(boolean, Collection)
     */
    @SafeVarargs
    public static final <E extends Enum<E>> Transformer<String,E> fromNameTransformer(boolean caseSensitive, E ... values) {
        return fromNameTransformer(caseSensitive, of(values));
    }
    
    /**
     * @param caseSensitive Whether name lookup should be case sensitive
     * (recommended: <code>false</code>)
     * @param values The values to be used to lookup for a match
     * @return A {@link Transformer} whose {@link Transformer#transform(Object)}
     * method returns the matching enum value for a {@link String} (<code>null</code>
     * if no match found). <B>Note:</B> this method is preferable over
     * {@link EnumUtils#getEnum(Class, String)} call since it allows for case
     * insensitive lookup and also does not involve handling the exception
     * thrown by {@link Enum#valueOf(Class, String)} if no match found
     * @see #getEnumMap(boolean, Collection)
     */
    public static final <E extends Enum<E>> Transformer<String,E> fromNameTransformer(boolean caseSensitive, Collection<? extends E> values) {
        final Map<String,E> namesMap=getEnumMap(caseSensitive, values);
        return new Transformer<String,E>() {
            @Override
            public E transform (String name) {
                if (StringUtils.isEmpty(name)) {
                    return null;
                } else {
                    return namesMap.get(name);
                }
            }
        };
    }

    private static final Transformer<Enum<?>,Integer>   ordinalExtract=
            new Transformer<Enum<?>,Integer>() {
                @Override
                public Integer transform (Enum<?> e) {
                    if (e == null) {
                        return null;
                    } else {
                        return Integer.valueOf(e.ordinal());
                    }
                }
            };
    /**
     * @return A {@link Transformer} that returns the {@link Enum#ordinal()} value
     */
    @SuppressWarnings("unchecked")
    public static final <E extends Enum<E>> Transformer<E,Integer> ordinalExtractor() {
        return (Transformer<E,Integer>) ordinalExtract;
    }

    /**
     * @param eClass The {@link Enum} type
     * @return An {@link ExtendedTransformer} that returns the {@link Enum#ordinal()} value
     */
    public static final <E extends Enum<E>> ExtendedTransformer<E,Integer> ordinalExtractor(Class<E> eClass) {
        return new AbstractExtendedTransformer<E,Integer>(eClass, Integer.class) {
            @Override
            public Integer transform (E e) {
                if (e == null) {
                    return null;
                } else {
                    return Integer.valueOf(e.ordinal());
                }
            }
        };
    }
    
    private static final Value2IntTransfomer<Enum<?>>   ordinalValueExtract=
            new Value2IntTransfomer<Enum<?>>() {
                @Override
                public int transform (Enum<?> value) {
                    if (value == null) {
                        return (-1);
                    } else {
                        return value.ordinal();
                    }
                }
            };
    /**
     * @return A {@link Value2IntTransfomer} whose transformation result is
     * the ordinal value (negative for <code>null</code>'s)
     */
    @SuppressWarnings("unchecked")
    public static final <E extends Enum<E>> Value2IntTransfomer<E> ordinalValueExtractor() {
        return (Value2IntTransfomer<E>) ordinalValueExtract;
    }

    /**
     * @param eClass The {@link Enum} type
     * @return An {@link ExtendedValue2IntTransfomer} whose transformation
     * result is the ordinal value (negative for <code>null</code>'s)
     */
    public static final <E extends Enum<E>> ExtendedValue2IntTransfomer<E> ordinalValueExtractor(Class<E> eClass) {
        return new AbstractExtendedValue2IntTransfomer<E>(eClass) {
            @SuppressWarnings("synthetic-access")
            @Override
            public int transform (E value) {
                return ordinalValueExtract.transform(value);
            }
        };
    }

    /**
     * @param eClass The enumeration type
     * @return A {@link SortedMap} of all the values where key=the {@link Integer}
     * ordinal value and the value is the matching enumeration type.
     * @see #fullSet(Class)
     * @see #getOrdinalsMap(Collection)
     */
    public static final <E extends Enum<E>> SortedMap<Integer,E> getOrdinalsMap(Class<E> eClass) {
        return getOrdinalsMap(fullSet(eClass));
    }
    
    /**
     * @param values The enumeration values to map - if <code>null</code>/empty
     * then an empty map is returned
     * @return A {@link SortedMap} of all the values where key=the {@link Integer}
     * ordinal value and the value is the matching enumeration type.
     * @see #getOrdinalsMap(Collection)
     */
    @SafeVarargs
    public static final <E extends Enum<E>> SortedMap<Integer,E> getOrdinalsMap(E ... values) {
        return getOrdinalsMap(of(values));
    }

    /**
     * @param values The enumeration values to map - if <code>null</code>/empty
     * then an empty map is returned
     * @return A {@link SortedMap} of all the values where key=the {@link Integer}
     * ordinal value and the value is the matching enumeration type.
     * @see #ordinalExtractor()
     */
    public static final <E extends Enum<E>> SortedMap<Integer,E> getOrdinalsMap(Collection<? extends E> values) {
        return ExtendedMapUtils.mapSortedCollectionValues(true, ExtendedEnumUtils.<E>ordinalExtractor(), values);
    }
    
    /**
     * A better replacement for <code>MyEnumClass.values()[e.ordinal()]</code>
     * @param eClass The enumeration type
     * @return An {@link ExtendedTransformer} that converts any {@link Number}
     * to the matching value ordinal (or {@code null} if no match found)
     */
    public static final <E extends Enum<E>> ExtendedTransformer<Number,E> fromOrdinalTransformer(Class<E> eClass) {
        final Map<Integer,E>    valuesMap=getOrdinalsMap(eClass);
        return new AbstractExtendedTransformer<Number,E>(Number.class, eClass) {
            @Override
            public E transform (Number o) {
                if (o == null) {
                    return null;
                } else if (o instanceof Integer) {
                    return valuesMap.get(o);
                } else {
                    return valuesMap.get(Integer.valueOf(o.intValue()));
                }
            }
        };
    }
    
    /**
     * A better replacement for <code>MyEnumClass.values()[e.ordinal()]</code>
     * @param values The values to be used when looking up an ordinal
     * @return A {@link Transformer} that converts any {@link Number}
     * to the matching value ordinal (or {@code null} if no match found)
     * @see #fromOrdinalTransformer(Collection)
     */
    @SafeVarargs
    public static final <E extends Enum<E>> Transformer<Number,E> fromOrdinalTransformer(E ... values) {
        return fromOrdinalTransformer(of(values));
    }
    
    /**
     * A better replacement for <code>MyEnumClass.values()[e.ordinal()]</code>
     * @param values The values to be used when looking up an ordinal
     * @return A {@link Transformer} that converts any {@link Number}
     * to the matching value ordinal (or {@code null} if no match found)
     * @see #getOrdinalsMap(Collection)
     */
    public static final <E extends Enum<E>> Transformer<Number,E> fromOrdinalTransformer(Collection<? extends E> values) {
        final Map<Integer,E>    valuesMap=getOrdinalsMap(values);
        return new Transformer<Number,E>() {
            @Override
            public E transform (Number o) {
                if (o == null) {
                    return null;
                } else if (o instanceof Integer) {
                    return valuesMap.get(o);
                } else {
                    return valuesMap.get(Integer.valueOf(o.intValue()));
                }
            }
        };
    }

    /**
     * @param eClass The {@link Enum} class
     * @return A {@link Map} where key=the enum value, value=the value's
     * camel-case name
     * @see #asCamelCaseValueNamesMap(Class, Enum...)
     * @see Class#getEnumConstants()
     */
    public static final <E extends Enum<E>> Map<E,String> asCamelCaseValueNamesMap(Class<E> eClass) {
        return asCamelCaseValueNamesMap(eClass, eClass.getEnumConstants());
    }
    
    /**
     * @param eClass The {@link Enum} class
     * @param values The values to be used as keys for the map - ignored if
     * {@code null}/empty
     * @return A {@link Map} where key=the enum value, value=the value's
     * camel-case name
     * @see #asCamelCaseValueNamesMap(Class, Collection)
     */
    @SafeVarargs
    public static final <E extends Enum<E>> Map<E,String> asCamelCaseValueNamesMap(Class<E> eClass, E ... values) {
        return asCamelCaseValueNamesMap(eClass, of(values));
    }
    
    /**
     * @param eClass The {@link Enum} class
     * @param values The values to be used as keys for the map - ignored if
     * {@code null}/empty
     * @return A {@link Map} where key=the enum value, value=the value's
     * camel-case name
     * @see #asCamelCaseValueNameTransformer()
     */
    public static final <E extends Enum<E>> Map<E,String> asCamelCaseValueNamesMap(Class<E> eClass, Collection<E> values) {
        ExtendedValidate.notNull(eClass, "No values class provided");
        if (ExtendedCollectionUtils.isEmpty(values)) {
            return Collections.emptyMap();
        }

        return ExtendedMapUtils.updateCollectionKeysMap(
                                new EnumMap<E,String>(eClass),
                                false,  // ignore duplicates
                                ExtendedEnumUtils.<E>asCamelCaseValueNameTransformer(),
                                values);
    }
    /**
     * Converts the {@link Enum#name()} value to a camel-case, using underscore
     * (_) as the &quot;words&quot; separator
     * @param value The {@link Enum} constant - ignored if {@code null}
     * @return The camel case name
     * @see #asCamelCaseValueName(String)
     */
    public static final <E extends Enum<E>> String asCamelCaseValueName(E value) {
        return asCamelCaseValueName((value == null) ? null : value.name());
    }
    
    private static final Transformer<Enum<?>,String> camelCaseValNameXformer=
            new Transformer<Enum<?>,String>() {
                @Override
                public String transform(Enum<?> e) {
                    if (e == null) {
                        return null;
                    } else {
                        return asCamelCaseValueName(e.name());
                    }
                }
            };
    /**
     * @return A {@link Transformer} that invokes the {@linkplain #asCamelCaseValueName(Enum)}
     * method
     */
    @SuppressWarnings("unchecked")
    public static final <E extends Enum<E>> Transformer<E,String> asCamelCaseValueNameTransformer() {
        return (Transformer<E,String>) camelCaseValNameXformer;
    }

    /**
     * The separator used by the {@linkplain #asCamelCaseValueName(String)}
     * method to distinguish &quot;words&quot; in an enum value name
     */
    public static final char    ENUM_NAME_WORDS_SEPARATOR='_';

    /**
     * Converts an {@linkplain Enum}'s name to camel-case, using underscore
     * (_) as the &quot;words&quot; separator
     * @param valName The name - ignored if {@code null}/empty
     * @return The camel case name
     */
    public static final String asCamelCaseValueName(String valName) {
        if (StringUtils.isEmpty(valName)) {
            return valName;
        }
        
        String[]    words=StringUtils.split(valName, ENUM_NAME_WORDS_SEPARATOR);
        for (int index=0; index < words.length; index++) {
            String  w=words[index].toLowerCase();
            if (index > 0) {
                w = ExtendedCharSequenceUtils.capitalize(w);
            }
            words[index] = w;
        }
        
        return StringUtils.join(words);
    }
    
    /**
     * An {@link ExtendedTransformer} that invokes {@link #asCamelCaseValueName(String)}
     */
    public static final ExtendedTransformer<String,String> NAME2CAMELCASE_XFORMER=
            new AbstractExtendedTransformer<String, String>(String.class,String.class) {
                @Override
                public String transform(String input) {
                    return asCamelCaseValueName(input);
                }
            };

    /**
     * Reverses the effects of {@linkplain #asCamelCaseValueName(String)}.
     * <B>Note:</B> assumes all camel-case components must be converted
     * to UPPERCASE in order to form the enum value name - i.e., values
     * such as <code>Bad_Enum_Value</code> will not yield the expected
     * result when converted to camel-case and back. In order to avoid this
     * issue, recommend using <U>case insensitive</U> lookup of the actual
     * constant value when using the result of this method (see
     * {@link #getEnumMap(Class, boolean)}).
     * @param valName The name - ignored if {@code null}/empty
     * @return The expected enum value name
     */
    public static final String fromCamelCaseValueName(String valName) {
        if (StringUtils.isEmpty(valName)) {
            return valName;
        }

        String[]    words=StringUtils.splitByCharacterTypeCamelCase(valName);
        for (int index=0; index < words.length; index++) {
            words[index] = words[index].toUpperCase();
        }
        
        return StringUtils.join(words, ENUM_NAME_WORDS_SEPARATOR);
    }
    
    /**
     * An {@link ExtendedTransformer} that invokes {@link #fromCamelCaseValueName(String)}
     */
    public static final ExtendedTransformer<String,String> CAMELCASE2NAME_XFORMER=
            new AbstractExtendedTransformer<String, String>(String.class,String.class) {
                @Override
                public String transform(String input) {
                    return fromCamelCaseValueName(input);
                }
            };

}
