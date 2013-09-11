/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.collections15.set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections15.map.LRUMap;

/**
 * @param <E> Type of value being collected in the set
 * @author Lyor G.
 * @since Oct 3, 2011 8:10:18 AM
 */
public class LRUSet<E> extends AbstractSet<E> {
    public static final int DEFAULT_MAX_SIZE=100;
	public LRUSet ()
	{
		this(DEFAULT_MAX_SIZE);
	}

    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    public LRUSet (int maxSize)
    {
    	this(maxSize, DEFAULT_LOAD_FACTOR);
    }
    

    public LRUSet (int maxSize, float loadFactor)
    {
    	this(maxSize, loadFactor, false);
    }

    public LRUSet (int maxSize, boolean scanUntilRemovable)
    {
    	this(maxSize, DEFAULT_LOAD_FACTOR, scanUntilRemovable);
    }

    private final LRUMap<E,E>	_valuesMap;
    public LRUSet (int maxSize, float loadFactor, boolean scanUntilRemovable)
	{
		_valuesMap = new LRUMap<E,E>(maxSize, loadFactor, scanUntilRemovable);
	}

    public LRUSet (Collection<? extends E> c)
    {
    	this(c, false);
    }

    public LRUSet (Collection<? extends E> c, boolean scanUntilRemovable)
    {
    	this(c.size(), DEFAULT_LOAD_FACTOR, scanUntilRemovable);
    	addAll(c);
    }
	/*
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator ()
	{
		return _valuesMap.keySet().iterator();
	}
	/*
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size ()
	{
		return _valuesMap.size();
	}
	/*
	 * @see java.util.AbstractCollection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains (Object o)
	{
		return _valuesMap.containsKey(o);
	}
	/*
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	@Override
	public boolean add (E e)
	{
		final E	prev=_valuesMap.put(e, e);
		if (prev != null)
			return false;
		else	// debug breakpoint
			return true;
	}
	/*
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove (Object o)
	{
		final E	value=_valuesMap.remove(o);
		if (value != null)
			return true;
		else
			return false;
	}
	/*
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll (Collection<?> c)
	{
		boolean	modified=false;
		for (final Object o : c)
		{
			if (remove(o))
				modified = true;
		}

		return modified;
	}
	/*
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override
	public void clear ()
	{
		if (!_valuesMap.isEmpty())
			_valuesMap.clear();
	}
    /**
     * @return <code>true</code> if the set is full and any new mappings
     * will cause some entry to be pushed out
     */
    public boolean isFull()
    {
        return _valuesMap.isFull();
    }
    /**
     * @return the maximum number of elements the set can hold
     */
    public int maxSize ()
    {
        return _valuesMap.maxSize();
    }
    /**
     * @return true Whether this set will scan until a removable entry is
     * found when the set is full.
     */
    public boolean isScanUntilRemovable ()
    {
        return _valuesMap.isScanUntilRemovable();
    }
}
