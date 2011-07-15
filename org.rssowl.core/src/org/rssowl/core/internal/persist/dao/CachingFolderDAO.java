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

import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ReparentInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caching DAO for {@link IFolder}.
 */
public class CachingFolderDAO extends CachingDAO<FolderDAOImpl, IFolder, FolderListener, FolderEvent> implements IFolderDAO {

  /* Dummy value to associate with an Object in the maps */
  private static final Object PRESENT = new Object();
  private final ConcurrentMap<IFolder, Object> fRootFolders;

  public CachingFolderDAO() {
    super(new FolderDAOImpl());
    fRootFolders = new ConcurrentHashMap<IFolder, Object>(4, 0.75f, 1);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.CachingDAO#onDatabaseClosed(org.rssowl
   * .core.internal.persist.service.DatabaseEvent)
   */
  @Override
  protected void onDatabaseClosed(DatabaseEvent event) {
    super.onDatabaseClosed(event);
    fRootFolders.clear();
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.CachingDAO#onDatabaseOpened(org.rssowl
   * .core.internal.persist.service.DatabaseEvent)
   */
  @Override
  protected void onDatabaseOpened(DatabaseEvent event) {
    if (USE_LEGACY_CACHE_ACTIVATION) {
      super.onDatabaseOpened(event);

      /* Ensure we start with a fresh cache */
      fRootFolders.clear();

      /* Load Roots */
      for (IFolder folder : getDAO().loadRoots())
        fRootFolders.put(folder, PRESENT);
    } else {

      /* Load Root Folders */
      Collection<IFolder> roots = getDAO().loadRoots();
      for (IFolder folder : roots) {
        fRootFolders.put(folder, PRESENT);
        getCache().put(folder.getId(), folder);
      }

      /* Cache all Folders from Roots */
      Set<IFolder> folders = new HashSet<IFolder>();
      CoreUtils.fillFolders(folders, roots);
      for (IFolder folder : folders) {
        getCache().put(folder.getId(), folder);
      }
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.CachingDAO#createEntityListener()
   */
  @Override
  protected FolderListener createEntityListener() {
    return new FolderListener() {
      public void entitiesAdded(Set<FolderEvent> events) {
        for (FolderEvent folderEvent : events) {
          IFolder folder = folderEvent.getEntity();
          getCache().put(folder.getId(), folder);
          if (folder.getParent() == null)
            fRootFolders.put(folder, PRESENT);
        }
      }

      public void entitiesDeleted(Set<FolderEvent> events) {
        for (FolderEvent folderEvent : events) {
          IFolder folder = folderEvent.getEntity();
          getCache().remove(folder.getId(), folder);
          if (folder.getParent() == null)
            fRootFolders.remove(folder);
        }
      }

      public void entitiesUpdated(Set<FolderEvent> events) {
        for (FolderEvent folderEvent : events) {
          if (folderEvent.getOldParent() != null) {
            IFolder folder = folderEvent.getEntity();
            if (folder.getParent() == null)
              fRootFolders.put(folder, PRESENT);
          }
        }
      }
    };
  }

  /*
   * @see org.rssowl.core.persist.dao.IFolderDAO#loadRoots()
   */
  public Collection<IFolder> loadRoots() throws PersistenceException {
    return Collections.unmodifiableSet(fRootFolders.keySet());
  }

  /*
   * @see org.rssowl.core.persist.dao.IFolderDAO#reparent(java.util.List)
   */
  public void reparent(List<ReparentInfo<IFolderChild, IFolder>> reparentInfos) throws PersistenceException {
    getDAO().reparent(reparentInfos);
  }
}