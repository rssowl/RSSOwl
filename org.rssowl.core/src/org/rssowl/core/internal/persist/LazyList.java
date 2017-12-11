/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2009 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse Public License v1.0 which accompanies this    **
 **   distribution, and is available at:                                     **
 **   http://www.rssowl.org/legal/epl-v10.html                               **
 **                                                                          **
 **   A copy is found in the file epl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **                                                                          **
 **   This copyright notice MUST APPEAR in all copies of the file!           **
 **                                                                          **
 **   Contributors:                                                          **
 **     RSSOwl Development Team - initial API and implementation             **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IEntity;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An implementation of {@link List} that uses a provided
 * {@link ObjectContainer} to access a list of entities in a lazy way. Entities
 * get resolved lazily as they are retrieved from the list.
 *
 * @param <E> any type that extends {@link IEntity}.
 */
public final class LazyList<E extends IEntity> implements List<E> {
  private final LongArrayList fIds;
  private final ObjectContainer fObjectContainer;

  public LazyList(ObjectSet<? extends E> entities, ObjectContainer objectContainer) {
    Assert.isNotNull(entities, "entities"); //$NON-NLS-1$
    Assert.isNotNull(objectContainer, "objectContainer"); //$NON-NLS-1$
    long[] ids = entities.ext().getIDs();
    fIds = new LongArrayList(ids.length);
    fIds.setAll(ids);
    fObjectContainer = objectContainer;
  }

  /*
   * @see java.util.List#toArray()
   */
  @Override
  public Object[] toArray() {
    Object[] array = new Object[size()];
    int index = 0;
    for (E e : this)
      array[index++] = e;

    return array;
  }

  /*
   * @see java.util.List#toArray(T[])
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    int size = size();
    T[] array = a;
    if (a.length < size)
      array = (T[]) Array.newInstance(a.getClass().getComponentType(), size);

    int index = 0;
    for (E e : this)
      array[index++] = (T) e;

    return array;
  }

  private E getEntity(long id) {
    E object = fObjectContainer.ext().getByID(id);
    fObjectContainer.activate(object, Integer.MAX_VALUE);
    return object;
  }

  /*
   * @see java.util.List#get(int)
   */
  @Override
  public E get(int index) {
    return getEntity(fIds.get(index));
  }

  /*
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Override
  public int indexOf(Object o) {
    if (o instanceof IEntity) {
      IEntity entity = (IEntity) o;
      if (entity.getId() != null)
        return fIds.indexOf(entity.getId());
    }
    return -1;
  }

  /*
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Override
  public int lastIndexOf(Object o) {
    if (o instanceof IEntity) {
      IEntity entity = (IEntity) o;
      if (entity.getId() != null)
        return fIds.lastIndexOf(entity.getId());
    }
    return -1;
  }

  /*
   * @see java.util.List#listIterator()
   */
  @Override
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }

  /*
   * @see java.util.List#listIterator(int)
   */
  @Override
  public ListIterator<E> listIterator(int index) {
    return new ListIterator<E>() {

      private int cursor;
      private int lastReturned = -1;

      @Override
      public boolean hasNext() {
        return cursor < fIds.size();
      }

      @Override
      public boolean hasPrevious() {
        return cursor > 0;
      }

      @Override
      public E next() {
        E entity = getEntity(fIds.get(cursor));
        lastReturned = cursor++;
        return entity;
      }

      @Override
      public int nextIndex() {
        return cursor;
      }

      @Override
      public E previous() {
        E entity = getEntity(fIds.get(--cursor));
        lastReturned = cursor;
        return entity;
      }

      @Override
      public int previousIndex() {
        return cursor - 1;
      }

      @Override
      public void remove() {
        if (lastReturned == -1)
          throw new IllegalStateException();

        fIds.removeByIndex(lastReturned);
        if (lastReturned < cursor)
          --cursor;

        lastReturned = -1;
      }

      @Override
      public void set(E o) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void add(E o) {
        throw new UnsupportedOperationException();
      }
    };
  }

  /*
   * @see java.util.List#remove(int)
   */
  @Override
  public E remove(int index) {
    return getEntity(fIds.removeByIndex(index));
  }

  /*
   * @see java.util.List#iterator()
   */
  @Override
  public Iterator<E> iterator() {
    return listIterator();
  }

  /*
   * @see java.util.List#clear()
   */
  @Override
  public void clear() {
    fIds.clear();
  }

  /*
   * @see java.util.List#contains(java.lang.Object)
   */
  @Override
  public boolean contains(Object o) {
    if (o instanceof IEntity) {
      return fIds.contains(((IEntity) o).getId());
    }
    return false;
  }

  /*
   * @see java.util.List#containsAll(java.util.Collection)
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!contains(o))
        return false;
    }
    return true;
  }

  /*
   * @see java.util.List#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return fIds.isEmpty();
  }

  /*
   * @see java.util.List#remove(java.lang.Object)
   */
  @Override
  public boolean remove(Object o) {
    int index = indexOf(o);
    if (index >= 0) {
      fIds.removeByIndex(index);
      return true;
    }
    return false;
  }

  /*
   * @see java.util.List#removeAll(java.util.Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object o : c)
      changed |= remove(o);

    return changed;
  }

  /*
   * @see java.util.List#size()
   */
  @Override
  public int size() {
    return fIds.size();
  }

  /*
   * @see java.util.List#subList(int, int)
   */
  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.util.List#retainAll(java.util.Collection)
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.util.List#add(int, java.lang.Object)
   */
  @Override
  public void add(int index, E element) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.util.List#set(int, java.lang.Object)
   */
  @Override
  public E set(int index, E element) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.util.List#add(java.lang.Object)
   */
  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.util.List#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll(Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (o == null)
      return false;

    if (getClass() != o.getClass())
      return false;

    Collection<?> c = (Collection<?>) o;
    if (c.size() != size())
      return false;

    return containsAll(c);
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int h = 0;
    for (E e : this) {
      if (e != null)
        h += e.hashCode();
    }
    return h;
  }
}