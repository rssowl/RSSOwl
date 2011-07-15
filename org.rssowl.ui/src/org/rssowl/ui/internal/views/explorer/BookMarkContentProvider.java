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

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.MarkEvent;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.NewsBinListener;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.util.JobRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author bpasero
 */
public class BookMarkContentProvider implements ITreeContentProvider {

  /* Delay in ms before updating Selection on Events */
  private static final int SELECTION_DELAY = 20;

  /* Listener */
  private FolderListener fFolderListener;
  private BookMarkListener fBookMarkListener;
  private NewsBinListener fNewsBinListener;
  private SearchMarkListener fSearchMarkListener;
  private NewsListener fNewsListener;

  /* Viewer Related */
  private IFolder fInput;
  private TreeViewer fViewer;
  private BookMarkFilter fBookmarkFilter;
  private BookMarkGrouping fBookmarkGrouping;

  /* Misc. */
  private IBookMarkDAO fBookMarkDAO = DynamicDAO.getDAO(IBookMarkDAO.class);

  /*
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    if (inputElement instanceof IFolder) {
      IFolder rootFolder = (IFolder) inputElement;

      /* No Grouping */
      if (!fBookmarkGrouping.isActive()) {
        Collection<IFolderChild> elements = rootFolder.getChildren();

        /* Return Children */
        return elements.toArray();
      }

      /* Grouping Enabled */
      List<IMark> marks = new ArrayList<IMark>();
      getAllMarks(rootFolder, marks);

      return fBookmarkGrouping.group(marks);
    }

    return new Object[0];
  }

  /*
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object parentElement) {

    /* Return Children of Folder */
    if (parentElement instanceof IFolder) {
      IFolder parent = (IFolder) parentElement;
      Collection<IFolderChild> children = parent.getChildren();

      return children.toArray();
    }

    /* Return Children of Group */
    else if (parentElement instanceof EntityGroup) {
      List<IEntity> children = ((EntityGroup) parentElement).getEntities();
      return children.toArray();
    }

    return new Object[0];
  }

  /*
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object element) {

    /* Handle Grouping specially */
    if (fBookmarkGrouping.isActive() && element instanceof IEntity) {
      IEntity entity = (IEntity) element;
      EntityGroup[] groups = fBookmarkGrouping.group(Collections.singletonList(entity));
      if (groups.length == 1)
        return groups[0];
    }

    /* Grouping not enabled */
    else {

      /* Parent Folder of Folder */
      if (element instanceof IFolder) {
        IFolder folder = (IFolder) element;
        return folder.getParent();
      }

      /* Parent Folder of Mark */
      else if (element instanceof IMark) {
        IMark mark = (IMark) element;
        return mark.getParent();
      }
    }

    return null;
  }

  /*
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object element) {
    if (element instanceof IFolder) {
      IFolder folder = (IFolder) element;
      return folder.getChildren().size() > 0;
    }

    else if (element instanceof EntityGroup)
      return ((EntityGroup) element).size() > 0;

    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    unregisterListeners();
  }

  /*
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
   * java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    Assert.isTrue(newInput instanceof IFolder || newInput == null);
    fViewer = (TreeViewer) viewer;
    fInput = (IFolder) newInput;

    /* Register Listeners if Input is available */
    if (newInput != null && oldInput == null)
      registerListeners();

    /* If new Input is NULL, unregister Listeners */
    else if (newInput == null && oldInput != null)
      unregisterListeners();
  }

  /* The ContentProvider needs to know about this Filter */
  void setBookmarkFilter(BookMarkFilter bookmarkFilter) {
    fBookmarkFilter = bookmarkFilter;
  }

  /* The ContentProvider needs to know about this Grouping */
  void setBookmarkGrouping(BookMarkGrouping bookmarkGrouping) {
    fBookmarkGrouping = bookmarkGrouping;
  }

  private void registerListeners() {

    /* Folder Listener */
    fFolderListener = new FolderListener() {

      /* Folders got updated */
      public void entitiesUpdated(final Set<FolderEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          public void run() {
            Set<IFolder> updatedFolders = null;
            Map<IFolder, IFolder> reparentedFolders = null;

            /* Retrieve Updated Folders */
            for (FolderEvent event : events) {
              if (event.isRoot()) {

                /* Folder got reparented */
                if (event.getOldParent() != null) {
                  if (reparentedFolders == null)
                    reparentedFolders = new HashMap<IFolder, IFolder>();
                  reparentedFolders.put(event.getEntity(), event.getOldParent());
                }

                /* Normal Update */
                else {
                  if (updatedFolders == null)
                    updatedFolders = new HashSet<IFolder>();
                  updatedFolders.add(event.getEntity());
                }
              }
            }

            /* Event not interesting for us or we are disposed */
            if (updatedFolders == null && reparentedFolders == null)
              return;

            /* Ask Filter */
            if (fBookmarkFilter.needsRefresh(IFolder.class, events))
              fViewer.refresh(false);

            /* Ask Group */
            else if (fBookmarkGrouping.needsRefresh(IFolder.class))
              fViewer.refresh(false);

            /* Handle reparented Folders */
            else if (reparentedFolders != null) {
              Set<Entry<IFolder, IFolder>> entries = reparentedFolders.entrySet();
              Set<IFolder> parentsToUpdate = new HashSet<IFolder>();
              List<Object> expandedElements = new ArrayList<Object>(Arrays.asList(fViewer.getExpandedElements()));
              try {
                fViewer.getControl().getParent().setRedraw(false);
                for (Entry<IFolder, IFolder> entry : entries) {
                  IFolder reparentedFolder = entry.getKey();
                  IFolder oldParent = entry.getValue();

                  /* Reparent while keeping the Selection / Expansion */
                  ISelection selection = fViewer.getSelection();
                  boolean expand = expandedElements.contains(reparentedFolder);
                  fViewer.remove(oldParent, new Object[] { reparentedFolder });
                  fViewer.refresh(reparentedFolder.getParent(), false);
                  fViewer.setSelection(selection);

                  if (expand)
                    fViewer.setExpandedState(reparentedFolder, expand);

                  /* Remember to update parents */
                  parentsToUpdate.add(oldParent);
                  parentsToUpdate.add(reparentedFolder.getParent());
                }
              } finally {
                fViewer.getControl().getParent().setRedraw(true);
              }

              /* Update old Parents of Reparented Bookmarks */
              for (IFolder folder : parentsToUpdate)
                updateFolderAndParents(folder);
            }

            /* Handle Updated Folders */
            if (updatedFolders != null) {
              for (IFolder folder : updatedFolders) {
                if (fInput.equals(folder))
                  fViewer.refresh(fInput);
                else
                  fViewer.refresh(folder);
              }
            }
          }
        });
      }

      /* Folders got deleted */
      public void entitiesDeleted(final Set<FolderEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          public void run() {

            /* Retrieve Removed Folders */
            Set<IFolder> removedFolders = null;
            for (FolderEvent event : events) {
              if (event.isRoot() && event.getEntity().getParent() != null) {
                if (removedFolders == null)
                  removedFolders = new HashSet<IFolder>();
                removedFolders.add(event.getEntity());
              }
            }

            /* Event not interesting for us or we are disposed */
            if (removedFolders == null || removedFolders.size() == 0)
              return;

            /* Ask Filter */
            if (fBookmarkFilter.needsRefresh(IFolder.class, events))
              fViewer.refresh(false);

            /* Ask Group */
            else if (fBookmarkGrouping.needsRefresh(IFolder.class))
              fViewer.refresh(false);

            /* React normally then */
            else
              fViewer.remove(removedFolders.toArray());

            /* Update Read-State counters on Parents */
            if (!fBookmarkGrouping.isActive()) {
              for (FolderEvent event : events) {
                IFolder eventParent = event.getEntity().getParent();
                if (eventParent != null && eventParent.getParent() != null)
                  updateFolderAndParents(eventParent);
              }
            }
          }
        });
      }

      /* Folders got added */
      public void entitiesAdded(final Set<FolderEvent> events) {
        JobRunner.runInUIThread(SELECTION_DELAY, fViewer.getControl(), new Runnable() {
          public void run() {

            /* Reveal and Select added Folders */
            final List<IFolder> addedFolders = new ArrayList<IFolder>();
            for (FolderEvent folderEvent : events) {
              IFolder addedFolder = folderEvent.getEntity();
              if (addedFolder.getParent() != null)
                addedFolders.add(addedFolder);
            }

            if (addedFolders.size() == 1)
              fViewer.setSelection(new StructuredSelection(addedFolders), true);
          }
        });
      }
    };

    /* BookMark Listener */
    fBookMarkListener = new BookMarkListener() {

      /* BookMarks got Updated */
      public void entitiesUpdated(final Set<BookMarkEvent> events) {
        onMarksUpdated(events);
      }

      /* BookMarks got Deleted */
      public void entitiesDeleted(final Set<BookMarkEvent> events) {
        onMarksRemoved(events);
      }

      /* BookMarks got Added */
      public void entitiesAdded(Set<BookMarkEvent> events) {
        onMarksAdded(events);
      }
    };

    /* SearchMark Listener */
    fSearchMarkListener = new SearchMarkListener() {

      /* SearchMarks got Updated */
      public void entitiesUpdated(final Set<SearchMarkEvent> events) {
        onMarksUpdated(events);
      }

      /* SearchMarks got Deleted */
      public void entitiesDeleted(final Set<SearchMarkEvent> events) {
        onMarksRemoved(events);
      }

      /* SearchMarks got Added */
      public void entitiesAdded(Set<SearchMarkEvent> events) {
        onMarksAdded(events);
      }

      /* SearchMark result changed */
      public void newsChanged(final Set<SearchMarkEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          public void run() {

            /* Ask Filter for a refresh */
            if (fBookmarkFilter.needsRefresh(ISearchMark.class, events, true))
              fViewer.refresh(false);

            /* Update SearchMarks */
            Set<ISearchMark> updatedSearchMarks = new HashSet<ISearchMark>(events.size());
            for (SearchMarkEvent event : events) {
              updatedSearchMarks.add(event.getEntity());
            }

            fViewer.update(updatedSearchMarks.toArray(), null);

            /* Update Parents */
            if (!fBookmarkGrouping.isActive()) {
              for (ISearchMark searchMark : updatedSearchMarks)
                updateFolderAndParents(searchMark.getParent());
            }
          }
        });
      }
    };

    /* NewsBin Listener */
    fNewsBinListener = new NewsBinListener() {

      /* NewsBins got Updated */
      public void entitiesUpdated(final Set<NewsBinEvent> events) {
        onMarksUpdated(events);
      }

      /* NewsBins got Deleted */
      public void entitiesDeleted(final Set<NewsBinEvent> events) {
        onMarksRemoved(events);
      }

      /* Newsbins got Added */
      public void entitiesAdded(Set<NewsBinEvent> events) {
        onMarksAdded(events);
      }
    };

    /* News Listener */
    fNewsListener = new NewsAdapter() {

      @Override
      public void entitiesAdded(final Set<NewsEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          public void run() {

            /* Return on Shutdown */
            if (Controller.getDefault().isShuttingDown())
              return;

            /* Ask Filter */
            if (fBookmarkFilter.needsRefresh(INews.class, events))
              fViewer.refresh(false);

            /* Ask Group */
            else if (fBookmarkGrouping.needsRefresh(INews.class))
              fViewer.refresh(false);

            /* Updated affected Types on read-state if required */
            if (requiresUpdate(events))
              updateParents(events);
          }
        });
      }

      @Override
      public void entitiesUpdated(final Set<NewsEvent> events) {
        JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
          public void run() {

            /* Return on Shutdown */
            if (Controller.getDefault().isShuttingDown())
              return;

            /* Ask Filter */
            if (fBookmarkFilter.needsRefresh(INews.class, events))
              fViewer.refresh(false);

            /* Ask Group */
            else if (fBookmarkGrouping.needsRefresh(INews.class))
              fViewer.refresh(false);

            /* Updated affected Types on read-state if required */
            if (requiresUpdate(events))
              updateParents(events);
          }
        });
      }
    };

    /* Register Listeners */
    DynamicDAO.addEntityListener(IFolder.class, fFolderListener);
    DynamicDAO.addEntityListener(IBookMark.class, fBookMarkListener);
    DynamicDAO.addEntityListener(INewsBin.class, fNewsBinListener);
    DynamicDAO.addEntityListener(ISearchMark.class, fSearchMarkListener);
    DynamicDAO.addEntityListener(INews.class, fNewsListener);
  }

  private void onMarksAdded(Set<? extends MarkEvent> events) {

    /* Reveal and Select if single Entity added */
    if (events.size() == 1) {
      final MarkEvent event = events.iterator().next();
      JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
        public void run() {
          expand(event.getEntity().getParent());
        }
      });
    }
  }

  private void onMarksRemoved(final Set<? extends MarkEvent> events) {
    if (events.isEmpty())
      return;

    JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
      public void run() {

        /* Retrieve Removed Marks */
        Class<? extends IMark> clazz = null;
        Set<IMark> removedMarks = null;
        for (MarkEvent event : events) {
          if (event.isRoot()) {
            if (removedMarks == null)
              removedMarks = new HashSet<IMark>();
            removedMarks.add(event.getEntity());
          }

          if (clazz == null)
            clazz = event.getEntity().getClass();
        }

        /* Event not interesting for us or we are disposed */
        if (removedMarks == null || removedMarks.size() == 0)
          return;

        /* Ask Filter */
        if (fBookmarkFilter.needsRefresh(clazz, events))
          fViewer.refresh(false);

        /* Ask Group */
        else if (fBookmarkGrouping.needsRefresh(clazz))
          fViewer.refresh(false);

        /* React normally then */
        else
          fViewer.remove(removedMarks.toArray());

        /* Update Read-State counters on Parents */
        if (!fBookmarkGrouping.isActive()) {
          for (MarkEvent event : events) {
            IFolder eventParent = event.getEntity().getParent();
            if (eventParent != null && eventParent.getParent() != null)
              updateFolderAndParents(eventParent);
          }
        }
      }
    });
  }

  private void onMarksUpdated(final Set<? extends MarkEvent> events) {
    if (events.isEmpty())
      return;

    JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
      public void run() {
        Class<? extends IMark> clazz = null;
        Set<IMark> updatedMarks = null;
        Map<IMark, IFolder> reparentedMarks = null;

        /* Retrieve Updated Marks */
        for (MarkEvent event : events) {
          if (event.isRoot()) {
            IFolder oldParent = event.getOldParent();

            /* Mark got reparented */
            if (oldParent != null) {
              if (reparentedMarks == null)
                reparentedMarks = new HashMap<IMark, IFolder>();
              reparentedMarks.put(event.getEntity(), oldParent);
            }

            /* Normal Update */
            else {
              if (updatedMarks == null)
                updatedMarks = new HashSet<IMark>();
              updatedMarks.add(event.getEntity());
            }
          }

          if (clazz == null)
            clazz = event.getEntity().getClass();
        }

        /* Event not interesting for us or we are disposed */
        if (updatedMarks == null && reparentedMarks == null)
          return;

        /* Ask Filter */
        if (fBookmarkFilter.needsRefresh(clazz, events))
          fViewer.refresh(false);

        /* Ask Group */
        else if (fBookmarkGrouping.needsRefresh(clazz))
          fViewer.refresh(false);

        /* Handle reparented Marks */
        else if (reparentedMarks != null) {
          Set<Entry<IMark, IFolder>> entries = reparentedMarks.entrySet();
          Set<IFolder> parentsToUpdate = new HashSet<IFolder>();
          try {
            fViewer.getControl().getParent().setRedraw(false);
            for (Entry<IMark, IFolder> entry : entries) {
              IMark reparentedMark = entry.getKey();
              IFolder oldParent = entry.getValue();

              /* Reparent while keeping the Selection */
              ISelection selection = fViewer.getSelection();
              fViewer.remove(oldParent, new Object[] { reparentedMark });
              fViewer.refresh(reparentedMark.getParent(), false);
              fViewer.setSelection(selection);

              /* Remember to update parents */
              parentsToUpdate.add(oldParent);
              parentsToUpdate.add(reparentedMark.getParent());
            }
          } finally {
            fViewer.getControl().getParent().setRedraw(true);
          }

          /* Update old Parents of Reparented Marks */
          for (IFolder folder : parentsToUpdate)
            updateFolderAndParents(folder);
        }

        /* Handle Updated Marks */
        if (updatedMarks != null)
          fViewer.update(updatedMarks.toArray(), null);
      }
    });
  }

  private void unregisterListeners() {
    DynamicDAO.removeEntityListener(IFolder.class, fFolderListener);
    DynamicDAO.removeEntityListener(IBookMark.class, fBookMarkListener);
    DynamicDAO.removeEntityListener(INewsBin.class, fNewsBinListener);
    DynamicDAO.removeEntityListener(ISearchMark.class, fSearchMarkListener);
    DynamicDAO.removeEntityListener(INews.class, fNewsListener);
  }

  /* Update Entities that are affected by the given NewsEvents */
  private void updateParents(final Set<NewsEvent> events) {
    INewsBinDAO newsBinDao = DynamicDAO.getDAO(INewsBinDAO.class);

    /* Group by Feed and Bins */
    Set<FeedLinkReference> affectedFeeds = new HashSet<FeedLinkReference>();
    Set<IFolder> affectedBinFolders = new HashSet<IFolder>();
    Set<Long> handledBins = new HashSet<Long>();
    for (NewsEvent event : events) {
      INews news = event.getEntity();
      long parentId = news.getParentId();
      if (!fBookmarkGrouping.isActive() && parentId != 0) {
        if (!handledBins.contains(parentId)) {
          INewsBin bin = newsBinDao.load(parentId);
          if (bin != null) //Could have been deleted meanwhile
            affectedBinFolders.add(bin.getParent());
          handledBins.add(parentId);
        }
      } else
        affectedFeeds.add(news.getFeedReference());
    }

    /* Return on Shutdown */
    if (Controller.getDefault().isShuttingDown())
      return;

    /* Update related Entities */
    for (FeedLinkReference feedRef : affectedFeeds)
      updateParents(feedRef);

    for (IFolder folder : affectedBinFolders)
      updateFolderAndParents(folder);
  }

  private void updateParents(FeedLinkReference feedRef) throws PersistenceException {

    /* Collect all affected BookMarks */
    Collection<IBookMark> affectedBookMarks = fBookMarkDAO.loadAll(feedRef);

    /* Return on Shutdown */
    if (Controller.getDefault().isShuttingDown())
      return;

    /* Update them including Parents */
    updateMarksAndParents(affectedBookMarks);
  }

  private void updateMarksAndParents(Collection<IBookMark> bookmarks) {
    Set<IEntity> entitiesToUpdate = new HashSet<IEntity>();
    entitiesToUpdate.addAll(bookmarks);

    /* Collect parents */
    if (!fBookmarkGrouping.isActive()) {
      for (IBookMark bookmark : bookmarks) {
        List<IFolder> visibleParents = new ArrayList<IFolder>();
        collectParents(visibleParents, bookmark);

        entitiesToUpdate.addAll(visibleParents);

        /* Return on Shutdown */
        if (Controller.getDefault().isShuttingDown())
          return;
      }
    }

    /* Update Entities */
    fViewer.update(entitiesToUpdate.toArray(), null);
  }

  private void collectParents(List<IFolder> parents, IEntity entity) {

    /* Determine Parent Folder */
    IFolder parent = null;
    if (entity instanceof IMark)
      parent = ((IMark) entity).getParent();
    else if (entity instanceof IFolder)
      parent = ((IFolder) entity).getParent();

    /* Root reached */
    if (parent == null)
      return;

    /* Input reached */
    if (fInput.equals(parent))
      return;

    /* Check parent visible */
    parents.add(parent);

    /* Recursively collect visible parents */
    collectParents(parents, parent);
  }

  private void updateFolderAndParents(IFolder folder) {
    Set<IEntity> entitiesToUpdate = new HashSet<IEntity>();
    entitiesToUpdate.add(folder);

    /* Collect parents */
    List<IFolder> parents = new ArrayList<IFolder>();
    collectParents(parents, folder);
    entitiesToUpdate.addAll(parents);

    /* Return on Shutdown */
    if (Controller.getDefault().isShuttingDown())
      return;

    /* Update Entities */
    fViewer.update(entitiesToUpdate.toArray(), null);
  }

  private void getAllMarks(IFolder folder, List<IMark> marks) {

    /* Add all Marks */
    marks.addAll(folder.getMarks());

    /* Go through Subfolders */
    List<IFolder> folders = folder.getFolders();
    for (IFolder childFolder : folders)
      getAllMarks(childFolder, marks);
  }

  private boolean requiresUpdate(Set<NewsEvent> events) {
    for (NewsEvent newsEvent : events) {
      INews oldNews = newsEvent.getOldNews();
      INews currentNews = newsEvent.getEntity();

      /* Check Change in New-State */
      boolean oldStateNew = INews.State.NEW.equals(oldNews != null ? oldNews.getState() : null);
      boolean currentStateNew = INews.State.NEW.equals(currentNews.getState());
      if (oldStateNew != currentStateNew)
        return true;

      /* Check Change in Read-State */
      boolean oldStateUnread = CoreUtils.isUnread(oldNews != null ? oldNews.getState() : null);
      boolean newStateUnread = CoreUtils.isUnread(currentNews.getState());
      if (oldStateUnread != newStateUnread)
        return true;

      /* Check Change in Sticky-State */
      boolean oldStateSticky = oldNews != null ? oldNews.isFlagged() : false;
      boolean newStateSticky = currentNews.isVisible() && currentNews.isFlagged();
      if (oldStateSticky != newStateSticky)
        return true;
    }

    return false;
  }

  /* Recursively expand a folder and all parents */
  private void expand(IFolder folder) {
    IFolder parent = folder.getParent();
    if (parent != null)
      expand(parent);

    if (folder.getParent() != null) //Never expand Set, its visible anyways
      fViewer.setExpandedState(folder, true);
  }
}