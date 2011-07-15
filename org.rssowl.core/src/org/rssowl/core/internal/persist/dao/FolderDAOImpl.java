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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.MarkEvent;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.ReparentInfo;

import com.db4o.ext.Db4oException;
import com.db4o.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A data-access-object for <code>IFolder</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class FolderDAOImpl extends AbstractEntityDAO<IFolder, FolderListener, FolderEvent> implements IFolderDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public FolderDAOImpl() {
    super(Folder.class, false);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final FolderEvent createDeleteEventTemplate(IFolder entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final FolderEvent createSaveEventTemplate(IFolder entity) {
    return new FolderEvent(entity, null, true);
  }

  /*
   * @see org.rssowl.core.persist.dao.IFolderDAO#loadRoots()
   */
  public Collection<IFolder> loadRoots() {
    try {
      Query query = fDb.query();
      query.constrain(fEntityClass);
      query.descend("fParent").constrain(null); //$NON-NLS-1$
      List<IFolder> folders = getList(query);
      activateAll(folders);
      return new ArrayList<IFolder>(folders);
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }

  /*
   * @see org.rssowl.core.persist.dao.IFolderDAO#reparent(java.util.List)
   */
  public final void reparent(List<ReparentInfo<IFolderChild, IFolder>> reparentInfos) {
    Assert.isNotNull(reparentInfos, "reparentInfos"); //$NON-NLS-1$
    if (reparentInfos.isEmpty())
      return;

    fWriteLock.lock();
    try {
      List<FolderEvent> folderEvents = new ArrayList<FolderEvent>(3);
      List<MarkEvent> markEvents = new ArrayList<MarkEvent>();
      fillFolderChildEvents(reparentInfos, folderEvents, markEvents);

      for (FolderEvent event : folderEvents) {
        fDb.set(event.getOldParent());
        IFolder newParent = event.getEntity().getParent();
        if (newParent == null)
          fDb.set(event.getEntity());
        else
          fDb.set(newParent);
      }

      for (MarkEvent event : markEvents) {
        fDb.set(event.getOldParent());
        IFolder newParent = event.getEntity().getParent();
        fDb.set(newParent);
      }

      fDb.commit();
    } catch (Db4oException e) {
      throw DBHelper.rollbackAndPE(fDb, e);
    } finally {
      fWriteLock.unlock();
    }

    DBHelper.cleanUpAndFireEvents();
  }

  private void addFolder(IFolder parent, IFolder child, IFolderChild position, Boolean after) {
    child.setParent(parent);

    /* The new parent may be null. It becomes a root folder */
    if (parent != null)
      parent.addFolder(child, position, after);
  }

  private IFolder removeChildFromFolder(IFolderChild folderChild) {
    IFolder oldParent = folderChild.getParent();
    oldParent.removeChild(folderChild);
    return oldParent;
  }

  private void addMarkToFolder(IFolder parent, IMark child, IFolderChild position, Boolean after) {
    child.setParent(parent);
    parent.addMark(child, position, after);
  }

  private void fillFolderChildEvents(List<ReparentInfo<IFolderChild, IFolder>> reparentInfos, List<FolderEvent> folderEvents, List<MarkEvent> markEvents) {
    for (ReparentInfo<IFolderChild, IFolder> reparentInfo : reparentInfos) {
      IFolderChild child = reparentInfo.getObject();
      IFolder newParent = reparentInfo.getNewParent();
      IFolder oldParent = child.getParent();
      IFolderChild newPosition = reparentInfo.getNewPosition();

      /* Folder */
      if (child instanceof IFolder) {
        IFolder folder = (IFolder) child;
        synchronized (folder) {
          removeChildFromFolder(folder);
          addFolder(newParent, folder, newPosition, reparentInfo.isAfter());
          if (newPosition != null) {
            List<IFolder> folderList = Collections.singletonList(folder);
            newParent.reorderChildren(folderList, newPosition, reparentInfo.isAfter().booleanValue());
          }
        }
        FolderEvent eventTemplate = new FolderEvent(folder, oldParent, true);
        folderEvents.add(eventTemplate);
        DBHelper.putEventTemplate(eventTemplate);
      }

      /* Mark */
      else if (child instanceof IMark) {
        IMark mark = (IMark) child;
        MarkEvent markEvent;
        synchronized (mark) {
          removeChildFromFolder(mark);
          addMarkToFolder(newParent, mark, newPosition, reparentInfo.isAfter());
          if (newPosition != null) {
            List<IMark> markList = Collections.singletonList(mark);
            newParent.reorderChildren(markList, newPosition, reparentInfo.isAfter().booleanValue());
          }
        }

        if (mark instanceof IBookMark)
          markEvent = new BookMarkEvent((IBookMark) mark, oldParent, true);
        else if (mark instanceof ISearchMark)
          markEvent = new SearchMarkEvent((ISearchMark) mark, oldParent, true);
        else if (mark instanceof INewsBin)
          markEvent = new NewsBinEvent((INewsBin) mark, oldParent, true);
        else
          throw new IllegalArgumentException("Unknown IMark subclass found: " + child.getClass()); //$NON-NLS-1$

        DBHelper.putEventTemplate(markEvent);
        markEvents.add(markEvent);
      } else {
        throw new IllegalArgumentException("Unknown IFolderChild subclass found: " + child.getClass()); //$NON-NLS-1$
      }
    }
  }
}