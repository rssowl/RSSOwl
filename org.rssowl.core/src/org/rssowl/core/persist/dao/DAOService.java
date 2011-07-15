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

package org.rssowl.core.persist.dao;

import org.rssowl.core.persist.IPersistable;

/**
 * The <code>DAOService</code> is an abstract class that provides getter to the
 * data access objects of all <code>IPersistable</code> model types in RSSOwl.
 * This service can be contributed by using the DAOService extension point
 * provided in this bundle.
 * <p>
 * Contributed via <code>org.rssowl.core.DAOService</code> Extension Point.
 * </p>
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public abstract class DAOService {

  /**
   * Returns the instance of <code>IPersistableDAO</code> matching the given
   * class of <code>IPersistableDAO</code>.
   *
   * @param <T> A concrete subclass of <code>IPersistableDAO</code>
   * @param daoInterface The <code>Class</code> to lookup an instance of
   * <code>IPersistableDAO</code> for.
   * @return Returns the instance of <code>IPersistableDAO</code> matching the
   * given class of <code>IPersistableDAO</code>.
   */
  public abstract <T extends IPersistableDAO<?>> T getDAO(Class<T> daoInterface);

  /**
   * Returns the instance of <code>IPersistableDAO</code> responsible for the
   * given <code>IPersistable</code>.
   *
   * @param <T> A concrete subclass of <code>IPersistableDAO</code>
   * @param <P> A concrete subclass of <code>IPersistable</code> to load the
   * responsible DAO for.
   * @param persistableClass The <code>Class</code> of the
   * <code>IPersistable</code> to load the responsible DAO for.
   * @return Returns the instance of <code>IPersistableDAO</code> responsible
   * for the given <code>IPersistable</code>.
   */
  public abstract <T extends IPersistableDAO<? super P>, P extends IPersistable> T getDAOFromPersistable(Class<P> persistableClass);

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IAttachment</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IAttachmentDAO getAttachmentDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IBookMark</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IBookMarkDAO getBookMarkDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>ICategory</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract ICategoryDAO getCategoryDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IFeed</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IFeedDAO getFeedDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IFolder</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IFolderDAO getFolderDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>INewsCounter</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract INewsCounterDAO getNewsCounterDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>INews</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract INewsDAO getNewsDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IPerson</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IPersonDAO getPersonDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IPreference</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IPreferenceDAO getPreferencesDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>ISearchCondition</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract ISearchConditionDAO getSearchConditionDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>ISearchMark</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract ISearchMarkDAO getSearchMarkDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>ILabel</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract ILabelDAO getLabelDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>IConditionalGet</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract IConditionalGetDAO getConditionalGetDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>INewsBin</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract INewsBinDAO getNewsBinDao();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>ISearch</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract ISearchDAO getSearchDAO();

  /**
   * @return Returns the instance of <code>IEntityDAO</code> responsible for
   * <code>ISearchFilter</code>.
   * @see DynamicDAO#getDAO(Class)
   */
  public abstract ISearchFilterDAO getSearchFilterDAO();
}