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

package org.rssowl.core.persist;

import org.rssowl.core.persist.reference.ModelReference;
import org.rssowl.core.persist.service.IDGenerator;

import java.io.Serializable;
import java.util.Map;

/**
 * Implementors of <code>IEntity</code> add a certain model-type to the
 * application. Any entity is <em>uniquely</em> identified by its ID.
 * Implementors have to make sure, that no entity of any kind will ever have the
 * same ID as another entity.
 * <p>
 * Compared to a simple <code>IPersistable</code> like <code>IImage</code>,
 * an entity adds the following features:
 * <ul>
 * <li>Identified by a unique ID that allows to load the entity from the
 * persistence layer</li>
 * <li> A Map allowing to store Properties. The content of the Map is stored
 * into the DataBase and thereby kept persistent in case they are Serializable.
 * This is done using Java's Serialization Feature and it is strongly
 * recommended not to use the Properties to store complex Objects. It is very
 * good to use with Strings or primitive Arrays. For any complex type, please
 * consider to extend the DataBase with a new relation to store your data.</li>
 * </ul>
 * </p>
 *
 * @see IDGenerator
 * @author bpasero
 */
public interface IEntity extends IPersistable {

  /**
   * Can be used in a
   * <code>ISearchField<code> to represent a search over all fields of the given Type.
   */
  public static final int ALL_FIELDS = -1;

  /**
   * Set a Property identified by a unique Key to this Model. This Method can be
   * used to extend the Model with values, for example in case the interpreted
   * Feed makes use of non-Feed-standard Elements.
   * <p>
   * It is <em>not</em> recommended to store complex types as Properties, but
   * Strings and other basic Types.
   * </p>
   * <p>
   * Chose a key with <em>caution</em>. The key should be qualified like
   * classes, for instance "org.yourproject.yourpackage.YourProperty" in order
   * to avoid overriding another key that was set by a different person.
   * </p>
   *
   * @param key The unique identifier of the Property.
   * @param value The value of the Property.
   */
  void setProperty(String key, Serializable value);

  /**
   * Get a Property from this Map or NULL if not existing for the given Key.
   *
   * @param key The unique identifier of the Property.
   * @return The value of the Property or NULL if no value is stored for the
   * given key.
   */
  Object getProperty(String key);

  /**
   * Removes a Property from this Map.
   *
   * @param key The unique identifier of the Property.
   * @return The value of the Property or NULL if no value is stored for the
   * given key.
   */
  Object removeProperty(String key);

  /**
   * Get the Map containing all Properties of this Type.
   *
   * @return The Map containing all Properties of this Type.
   */
  Map<String, Serializable> getProperties();

  /**
   * Get the unique id for this object. Implementors have to make sure, that no
   * entity of any kind will ever have the same ID as another entity.
   *
   * @return Unique id for the object.
   */
  Long getId();

  /**
   * Sets the unique id for this object. Implementors have to make sure, that no
   * entity of any kind will ever have the same ID as another entity.
   *
   * @param id Unique id for the object.
   */
  void setId(Long id);

  /**
   * @return a ModelReference for this object if the id is not null. If the id
   * is null, then an {@link IllegalStateException} is thrown.
   * @throws IllegalStateException if the id is null.
   */
  ModelReference toReference();
}