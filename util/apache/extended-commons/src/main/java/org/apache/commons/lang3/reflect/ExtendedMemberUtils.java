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

package org.apache.commons.lang3.reflect;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;

import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedClassUtils;
import org.apache.commons.lang3.ExtendedStringUtils;

/**
 * @author lgoldstein
 */
public class ExtendedMemberUtils extends MemberUtils {
	public ExtendedMemberUtils() {
		super();
	}

	@SafeVarargs
	public static final <M extends Member> SortedMap<String,M> byNameMap(boolean ignoreDuplicates, M ... members) {
		return byNameMap(ignoreDuplicates, ExtendedArrayUtils.asList(members));
	}

	public static final <M extends Member> SortedMap<String,M> byNameMap(boolean ignoreDuplicates, Collection<? extends M> members) {
		return ExtendedMapUtils.mapSortedCollectionValues(ignoreDuplicates, TO_NAME_XFORMER, members);
	}

	public static final ExtendedTransformer<Member,String>	TO_NAME_XFORMER=
			new AbstractExtendedTransformer<Member, String>(Member.class, String.class) {
				@Override
				public String transform(Member m) {
					if (m == null) {
						return null;
					} else {
						return m.getName();
					}
				}
			};
	/**
	 * Compares 2 {@link Member}s 1st by their declaring class and 2nd by
	 * their name(s)
	 * @see #BY_DECLARING_CLASS_COMPARATOR
	 * @see #BY_NAME_COMPARATOR
	 */
	public static final Comparator<Member>	BY_FQCN_COMPARATOR=
			new Comparator<Member>() {
				@Override
				public int compare(Member o1, Member o2) {
					if (o1 == o2) {
						return 0;
					}

					int	nRes=BY_DECLARING_CLASS_COMPARATOR.compare(o1, o2);
					if (nRes != 0) {
						return nRes;
					}
					
					if ((nRes=BY_NAME_COMPARATOR.compare(o1, o2)) != 0) {
						return nRes;
					}
					
					return 0;
				}
			};

	/**
	 * Compares 2 {@link Member}s by their declaring class <U>fully-qualified</U>
	 * name(s)
	 * @see Member#getDeclaringClass()
	 */
	public static final Comparator<Member>	BY_DECLARING_CLASS_COMPARATOR=
			new Comparator<Member>() {
				@Override
				public int compare(Member o1, Member o2) {
					if (o1 == o2) {
						return 0;
					}
					Class<?>	c1=(o1 == null) ? null : o1.getDeclaringClass();
					Class<?>	c2=(o2 == null) ? null : o2.getDeclaringClass();
					return ExtendedClassUtils.BY_FULL_NAME_COMPARATOR.compare(c1, c2);
				}
		};

	/**
	 * Compares 2 {@link Member}s by their names
	 * @see Member#getName()
	 */
	public static final Comparator<Member>	BY_NAME_COMPARATOR=
			new Comparator<Member>() {
				@Override
				public int compare(Member o1, Member o2) {
					if (o1 == o2) {
						return 0;
					}
					String	n1=(o1 == null) ? null : o1.getName();
					String	n2=(o2 == null) ? null : o2.getName();
					return ExtendedStringUtils.safeCompare(n1, n2);
				}
			};
	
}
