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

package org.rssowl.core.internal.persist.dao;

import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.service.EntityIdsByEventType;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.persist.dao.IAttachmentDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.dao.ICategoryDAO;
import org.rssowl.core.persist.dao.IConditionalGetDAO;
import org.rssowl.core.persist.dao.IFeedDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.dao.IPersistableDAO;
import org.rssowl.core.persist.dao.IPersonDAO;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.dao.ISearchConditionDAO;
import org.rssowl.core.persist.dao.ISearchDAO;
import org.rssowl.core.persist.dao.ISearchFilterDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * Providing all DAO Services.
 */
public final class DAOServiceImpl extends DAOService {

  /* Entity DAOs */
  private final IPreferenceDAO fPreferencesDAO = new PreferencesDAOImpl();
  private final IAttachmentDAO fAttachmentDAO = new AttachmentDAOImpl();
  private final ICategoryDAO fCategoryDAO = new CategoryDAOImpl();
  private final IConditionalGetDAO fConditionalGetDAO = new ConditionalGetDAOImpl();
  private final IFeedDAO fFeedDAO = new FeedDAOImpl();
  private final INewsCounterDAO fNewsCounterDAO = new NewsCounterDAOImpl();
  private final INewsDAO fNewsDAO = new NewsDAOImpl();
  private final IPersonDAO fPersonDAO = new PersonDAOImpl();
  private final ISearchConditionDAO fSearchConditionDAO = new SearchConditionDAOImpl();
  private final EntitiesToBeIndexedDAOImpl fEntitiesToBeIndexedDAO = new EntitiesToBeIndexedDAOImpl();
  private final IDescriptionDAO fDescriptionDAO = new DescriptionDAOImpl();

  /* Caching DAOs (ordering is very important and must remain in this way) */
  private final ISearchFilterDAO fSearchFilterDAO = new CachingSearchFilterDAO();
  private final IFolderDAO fFolderDAO = new CachingFolderDAO();
  private final IBookMarkDAO fBookMarkDAO = new CachingBookMarkDAO();
  private final ISearchMarkDAO fSearchMarkDAO = new CachingSearchMarkDAO();
  private final INewsBinDAO fNewsBinDAO = new CachingNewsBinDAO();
  private final ISearchDAO fSearchDAO = new CachingSearchDAO();
  private final ILabelDAO fLabelDAO = new CachingLabelDAO();

  /* Mapping */
  private final Map<Class<?>, Object> fEntityInterfacesToDaosMap = new HashMap<Class<?>, Object>();
  private final Map<Class<?>, Object> fEntityDaoClassesToDaosMap = new HashMap<Class<?>, Object>();
  private final Map<Class<?>, Object> fEntityClassesToDaosMap = new HashMap<Class<?>, Object>();

  public DAOServiceImpl() {

    /* Map DAO Interface to DAO */
    fEntityDaoClassesToDaosMap.put(IAttachmentDAO.class, fAttachmentDAO);
    fEntityDaoClassesToDaosMap.put(IBookMarkDAO.class, fBookMarkDAO);
    fEntityDaoClassesToDaosMap.put(ICategoryDAO.class, fCategoryDAO);
    fEntityDaoClassesToDaosMap.put(IConditionalGetDAO.class, fConditionalGetDAO);
    fEntityDaoClassesToDaosMap.put(IFeedDAO.class, fFeedDAO);
    fEntityDaoClassesToDaosMap.put(IFolderDAO.class, fFolderDAO);
    fEntityDaoClassesToDaosMap.put(ILabelDAO.class, fLabelDAO);
    fEntityDaoClassesToDaosMap.put(INewsCounterDAO.class, fNewsCounterDAO);
    fEntityDaoClassesToDaosMap.put(INewsDAO.class, fNewsDAO);
    fEntityDaoClassesToDaosMap.put(IPersonDAO.class, fPersonDAO);
    fEntityDaoClassesToDaosMap.put(ISearchConditionDAO.class, fSearchConditionDAO);
    fEntityDaoClassesToDaosMap.put(ISearchMarkDAO.class, fSearchMarkDAO);
    fEntityDaoClassesToDaosMap.put(IPreferenceDAO.class, fPreferencesDAO);
    fEntityDaoClassesToDaosMap.put(INewsBinDAO.class, fNewsBinDAO);
    fEntityDaoClassesToDaosMap.put(ISearchDAO.class, fSearchDAO);
    fEntityDaoClassesToDaosMap.put(ISearchFilterDAO.class, fSearchFilterDAO);
    for (Object value : fEntityDaoClassesToDaosMap.values()) {
      IPersistableDAO<?> dao = (IPersistableDAO<?>) value;
      putInEntityClassesToDaosMap(dao);
    }

    /* Map Entity Interface to DAO */
    fEntityInterfacesToDaosMap.put(IAttachment.class, fAttachmentDAO);
    fEntityInterfacesToDaosMap.put(IBookMark.class, fBookMarkDAO);
    fEntityInterfacesToDaosMap.put(ICategory.class, fCategoryDAO);
    fEntityInterfacesToDaosMap.put(IConditionalGet.class, fConditionalGetDAO);
    fEntityInterfacesToDaosMap.put(IFeed.class, fFeedDAO);
    fEntityInterfacesToDaosMap.put(IFolder.class, fFolderDAO);
    fEntityInterfacesToDaosMap.put(ILabel.class, fLabelDAO);
    fEntityInterfacesToDaosMap.put(INews.class, fNewsDAO);
    fEntityInterfacesToDaosMap.put(IPerson.class, fPersonDAO);
    fEntityInterfacesToDaosMap.put(ISearchCondition.class, fSearchConditionDAO);
    fEntityInterfacesToDaosMap.put(ISearchMark.class, fSearchMarkDAO);
    fEntityInterfacesToDaosMap.put(IPreference.class, fPreferencesDAO);
    fEntityInterfacesToDaosMap.put(INewsBin.class, fNewsBinDAO);
    fEntityInterfacesToDaosMap.put(ISearch.class, fSearchDAO);
    fEntityInterfacesToDaosMap.put(NewsCounter.class, fNewsCounterDAO);
    fEntityInterfacesToDaosMap.put(ISearchFilter.class, fSearchFilterDAO);
  }

  private void putInEntityClassesToDaosMap(IPersistableDAO<?> dao) {
    fEntityClassesToDaosMap.put(dao.getEntityClass(), dao);
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getPreferencesDAO()
   */
  @Override
  public final IPreferenceDAO getPreferencesDAO() {
    return fPreferencesDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getAttachmentDAO()
   */
  @Override
  public final IAttachmentDAO getAttachmentDAO() {
    return fAttachmentDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getBookMarkDAO()
   */
  @Override
  public final IBookMarkDAO getBookMarkDAO() {
    return fBookMarkDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getCategoryDAO()
   */
  @Override
  public final ICategoryDAO getCategoryDAO() {
    return fCategoryDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getConditionalGetDAO()
   */
  @Override
  public IConditionalGetDAO getConditionalGetDAO() {
    return fConditionalGetDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getFeedDAO()
   */
  @Override
  public final IFeedDAO getFeedDAO() {
    return fFeedDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getFolderDAO()
   */
  @Override
  public final IFolderDAO getFolderDAO() {
    return fFolderDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getNewsCounterDAO()
   */
  @Override
  public final INewsCounterDAO getNewsCounterDAO() {
    return fNewsCounterDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getNewsDAO()
   */
  @Override
  public final INewsDAO getNewsDAO() {
    return fNewsDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getPersonDAO()
   */
  @Override
  public final IPersonDAO getPersonDAO() {
    return fPersonDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getSearchConditionDAO()
   */
  @Override
  public final ISearchConditionDAO getSearchConditionDAO() {
    return fSearchConditionDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getSearchMarkDAO()
   */
  @Override
  public final ISearchMarkDAO getSearchMarkDAO() {
    return fSearchMarkDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getLabelDAO()
   */
  @Override
  public final ILabelDAO getLabelDAO() {
    return fLabelDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getNewsBinDao()
   */
  @Override
  public INewsBinDAO getNewsBinDao() {
    return fNewsBinDAO;
  }

  /**
   * @return the DAO to {@link EntityIdsByEventType}.
   */
  public EntitiesToBeIndexedDAOImpl getEntitiesToBeIndexedDAO() {
    return fEntitiesToBeIndexedDAO;
  }

  /**
   * @return the DAO to {@link Description}.
   */
  public IDescriptionDAO getDescriptionDAO() {
    return fDescriptionDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getSearchFilterDAO()
   */
  @Override
  public ISearchFilterDAO getSearchFilterDAO() {
    return fSearchFilterDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getSearchDAO()
   */
  @Override
  public ISearchDAO getSearchDAO() {
    return fSearchDAO;
  }

  /*
   * @see org.rssowl.core.persist.dao.DAOService#getDAO(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public final <T extends IPersistableDAO<?>> T getDAO(Class<T> daoInterface) {
    return (T) fEntityDaoClassesToDaosMap.get(daoInterface);
  }

  /*
   * @see
   * org.rssowl.core.persist.dao.DAOService#getDAOFromPersistable(java.lang.
   * Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public final <T extends IPersistableDAO<? super P>, P extends IPersistable> T getDAOFromPersistable(Class<P> persistableClass) {
    if (persistableClass.isInterface()) {
      Object value = fEntityInterfacesToDaosMap.get(persistableClass);
      return (T) value;
    }
    Object value = fEntityClassesToDaosMap.get(persistableClass);
    return (T) value;
  }
}