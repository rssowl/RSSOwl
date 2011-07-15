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

package org.rssowl.core.persist.service;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.SearchHit;

import java.util.Collection;
import java.util.List;

/**
 * The central interface for searching types from the persistance layer. The
 * implementation is contributable via extension-point mechanism.
 *
 * @author bpasero
 */
public interface IModelSearch {

  /**
   * Search for the type <code>INews</code> in the persistance layer.
   *
   * @param search the instanceof {@link ISearch} describing the search to
   * perform.
   * @return Returns the result of the search as <code>List</code>. In case no
   * type is matching the search, an empty <code>List</code> is returned.
   * @throws PersistenceException In case of an error while searching.
   */
  public List<SearchHit<NewsReference>> searchNews(ISearch search) throws PersistenceException;

  /**
   * Search for the type <code>INews</code> in the persistance layer.
   *
   * @param conditions A <code>List</code> of Search-Conditions specifying the
   * search to perform.
   * @param matchAllConditions If <code>TRUE</code>, require all conditions to
   * match, and if <code>FALSE</code>, News are considered a result when they
   * match at least 1 condition.
   * @return Returns the result of the search as <code>List</code>. In case no
   * type is matching the search, an empty <code>List</code> is returned.
   * @throws PersistenceException In case of an error while searching.
   */
  List<SearchHit<NewsReference>> searchNews(Collection<ISearchCondition> conditions, boolean matchAllConditions) throws PersistenceException;

  /**
   * Search for the type <code>INews</code> in the persistance layer.
   *
   * @param conditions A <code>List</code> of Search-Conditions specifying the
   * search to perform.
   * @param scope a specific {@link ISearchCondition} that scopes the results.
   * As such, the scope condition is a must criteria for the results.
   * @param matchAllConditions If <code>TRUE</code>, require all conditions to
   * match, and if <code>FALSE</code>, News are considered a result when they
   * match at least 1 condition.
   * @return Returns the result of the search as <code>List</code>. In case no
   * type is matching the search, an empty <code>List</code> is returned.
   * @throws PersistenceException In case of an error while searching.
   */
  List<SearchHit<NewsReference>> searchNews(Collection<ISearchCondition> conditions, ISearchCondition scope, boolean matchAllConditions) throws PersistenceException;

  /**
   * Releases all resources used by the implementor of this interface. The
   * difference between this method and <code>stopIndexer</code> is that, in
   * addition to stopping the indexer, this method also releases the resources
   * required to perform a search.
   *
   * @param emergency If set to <code>TRUE</code>, this method is called from a
   * shutdown hook that got triggered from a non-normal shutdown (e.g. System
   * Shutdown).
   * @throws PersistenceException
   */
  void shutdown(boolean emergency) throws PersistenceException;

  /**
   * Makes the <code>IModelSearch</code> capable of indexing entities and
   * returning results. <br>
   * Note that this method can be called multiple times safely.
   *
   * @throws PersistenceException
   */
  void startup() throws PersistenceException;

  /**
   * Deletes all the information that is stored in the search index. This must
   * be called if the information stored in the persistence layer has been
   * cleared with a method that does not issue events for the elements that are
   * removed. An example of this is
   * <code>PersistenceLayer#recreateSchema()</code>.
   *
   * @throws PersistenceException
   * @see {@link IPersistenceService}#recreateSchema()
   */
  void clearIndex() throws PersistenceException;

  /**
   * Causes the underlying Index to re-index all entities. First the index is
   * removed by calling {@link IModelSearch#clearIndex()} and then each entity
   * that is participating in the Search loaded and indexed. Note that this is a
   * cpu- and memory-intensive operation, thats why a
   * <code>IProgressMonitor</code> is passed in to track progress and support
   * cancelation.
   *
   * @param monitor An instance of <code>IProgressMonitor</code> to track
   * progress and support cancelation during the operation.
   * @throws PersistenceException
   */
  void reindexAll(IProgressMonitor monitor) throws PersistenceException;

  /**
   * Instructs the model search service to schedule an reindexing run during the
   * next time the application is started. The actual reindexing type is
   * dependent on the search system being used and implementors are free to
   * leave this as a no-op in case the the search system reindexes itself
   * automatically during runtime.
   *
   * @throws PersistenceException in case a problem occurs while trying to
   * schedule this operation.
   */
  void reIndexOnNextStartup() throws PersistenceException;

  /**
   * Causes the underlying Index to clean up all entities. This means that every
   * entry in the index is checked for a related entry in the DB including
   * visibility. If the item is no longer part or hidden it will be removed from
   * the index. Note that this is a cpu- and memory-intensive operation, thats
   * why a <code>IProgressMonitor</code> is passed in to track progress and
   * support cancelation.
   *
   * @param monitor An instance of <code>IProgressMonitor</code> to track
   * progress and support cancelation during the operation.
   * @throws PersistenceException
   */
  void cleanUp(IProgressMonitor monitor) throws PersistenceException;

  /**
   * Instructs the model search service to schedule an cleanup run during the
   * next time the application is started. The actual cleanup type is dependent
   * on the search system being used and implementors are free to leave this as
   * a no-op in case the the search system cleans up itself automatically during
   * runtime.
   *
   * @throws PersistenceException in case a problem occurs while trying to
   * schedule this operation.
   */
  void cleanUpOnNextStartup() throws PersistenceException;

  /**
   * Adds a Listener to the list of Listeners that will be notified on index
   * events.
   *
   * @param listener The Listener to add to the list of Listeners that will be
   * notified on index events.
   */
  void addIndexListener(IndexListener listener);

  /**
   * Removes the Listener from the list of Listeners that will be notified on
   * index events.
   *
   * @param listener The Listener to remove from the list of Listeners that will
   * be notified on index events.
   */
  void removeIndexListener(IndexListener listener);

  /**
   * Optimizes the search index.
   *
   * @throws PersistenceException
   */
  void optimize() throws PersistenceException;
}