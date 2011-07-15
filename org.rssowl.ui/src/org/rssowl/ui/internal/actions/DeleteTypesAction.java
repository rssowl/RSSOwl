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

package org.rssowl.ui.internal.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.ConfirmDialog;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;
import org.rssowl.ui.internal.undo.NewsStateOperation;
import org.rssowl.ui.internal.undo.UndoStack;
import org.rssowl.ui.internal.util.ModelUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global Action for Deleting a Selection of <code>ModelReferences</code>.
 *
 * @author bpasero
 */
public class DeleteTypesAction extends Action implements IObjectActionDelegate {

  /* Number of News to Delete before running operation in Background */
  private static final int RUN_IN_BACKGROUND_CAP = 200;

  private IStructuredSelection fSelection;
  private INewsDAO fNewsDAO;
  private Shell fShell;
  private boolean fConfirmed;

  /**
   * Keep default constructor for reflection.
   * <p>
   * Note: This Constructor should <em>not</em> directly be called. Use
   * <code>DeleteTypesAction(IStructuredSelection selection)</code> instead.
   * </p>
   */
  public DeleteTypesAction() {
    this(null, StructuredSelection.EMPTY);
  }

  /**
   * Creates a new Action for Deleting Types from the given Selection.
   *
   * @param shell The Shell to be used to show a confirmation dialog.
   * @param selection The Selection to Delete.
   */
  public DeleteTypesAction(Shell shell, IStructuredSelection selection) {
    fShell = shell;
    fSelection = selection;
    fNewsDAO = DynamicDAO.getDAO(INewsDAO.class);
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    if (!fSelection.isEmpty() && confirmed()) {
      BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
        public void run() {
          internalRun();
        }
      });
    }
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    if (!fSelection.isEmpty() && confirmed()) {
      BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
        public void run() {
          internalRun();
        }
      });
    }
  }

  private boolean confirmed() {

    /* Ignore when deleting News since this operation can be undone */
    List<?> elements = fSelection.toList();
    for (Object element : elements) {
      if (element instanceof INews)
        return true;
      else if (element instanceof EntityGroup) {
        EntityGroup group = (EntityGroup) element;
        if (NewsGrouping.GROUP_CATEGORY_ID.equals(group.getCategory()))
          return true;
      }
    }

    /* Check if the Archive is being deleted */
    boolean includesArchive = includesArchive(fSelection);

    /* Create Dialog and open if confirmation required */
    ConfirmDialog dialog = new ConfirmDialog(fShell, Messages.DeleteTypesAction_CONFIRM_DELETE, Messages.DeleteTypesAction_NO_UNDO, getMessage(elements, includesArchive), null);
    return dialog.open() == IDialogConstants.OK_ID;
  }

  private String getMessage(List<?> elements, boolean includesArchive) {
    INewsBin archive = CoreUtils.findArchive();
    int archiveNewsCount = (archive != null) ? archive.getNewsCount(INews.State.getVisible()) : 0;
    if (archive == null)
      includesArchive = false;

    /* One Element */
    if (elements.size() == 1) {
      Object element = elements.get(0);

      /* Archive */
      if (includesArchive && element instanceof INewsBin) {
        if (archiveNewsCount != 0)
          return NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_ARCHIVE_N, archiveNewsCount);

        return Messages.DeleteTypesAction_CONFIRM_DELETE_ARCHIVE;
      }

      /* Folder */
      else if (element instanceof IFolder) {
        if (includesArchive && archiveNewsCount != 0) {
          String msg = NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_FOLDER, ((IFolder) element).getName());
          msg += "\n\n"; //$NON-NLS-1$
          msg += NLS.bind(Messages.DeleteTypesAction_NOTE_FOLDER_ARCHIVE, archiveNewsCount);
          return msg;
        }

        return NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_FOLDER, ((IFolder) element).getName());
      }

      /* Bookmark */
      else if (element instanceof IBookMark)
        return NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_BOOKMARK, ((IMark) element).getName());

      /* Newsbin */
      else if (element instanceof INewsBin)
        return NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_NEWSBIN, ((IMark) element).getName());

      /* Saved Search */
      else if (element instanceof ISearchMark)
        return NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_SEARCH, ((IMark) element).getName());

      /* News */
      else if (element instanceof INews)
        return Messages.DeleteTypesAction_CONFIRM_DELETE_NEWS;

      /* Entity Group */
      else if (element instanceof EntityGroup) {
        if (includesArchive && archiveNewsCount != 0) {
          String msg = NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_GROUP, ((EntityGroup) element).getName());
          msg += "\n\n"; //$NON-NLS-1$
          msg += NLS.bind(Messages.DeleteTypesAction_NOTE_GROUP_ARCHIVE, archiveNewsCount);
          return msg;
        }

        return NLS.bind(Messages.DeleteTypesAction_CONFIRM_DELETE_GROUP, ((EntityGroup) element).getName());
      }
    }

    /* N Elements including the Archive */
    if (includesArchive && archiveNewsCount != 0) {
      String msg = Messages.DeleteTypesAction_CONFIRM_DELETE_ELEMENTS;
      msg += "\n\n"; //$NON-NLS-1$
      msg += NLS.bind(Messages.DeleteTypesAction_NOTE_SELECTION_ARCHIVE, archiveNewsCount);
      return msg;
    }

    /* N Elements without Archive */
    return Messages.DeleteTypesAction_CONFIRM_DELETE_ELEMENTS;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection)
      fSelection = (IStructuredSelection) selection;
  }

  /*
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   * org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    fShell = targetPart.getSite().getShell();
  }

  private void internalRun() {
    fConfirmed = true;

    /* Only consider Entities */
    final List<IEntity> entities = ModelUtils.getEntities(fSelection);

    /* Normalize */
    CoreUtils.normalize(entities);

    /* Restore Editors if necessary */
    restoreEditorsIfNecessary(entities);

    /* Separate News */
    final List<INews> newsToDelete = new ArrayList<INews>();

    /* Extract News */
    for (Iterator<IEntity> it = entities.iterator(); it.hasNext();) {
      IEntity element = it.next();

      /* Separate News */
      if (element instanceof INews) {
        it.remove();
        newsToDelete.add((INews) element);
      }
    }

    /* Wrap into Runnable */
    Runnable deleteRunnable = new Runnable() {
      public void run() {

        /* Mark Saved Search Service as in need for a quick Update */
        Controller.getDefault().getSavedSearchService().forceQuickUpdate();

        /* Delete Folders and Marks in single Transaction */
        DynamicDAO.deleteAll(entities);

        /* Delete News in single Transaction */
        if (!newsToDelete.isEmpty()) {

          /* Support Undo */
          UndoStack.getInstance().addOperation(new NewsStateOperation(newsToDelete, INews.State.HIDDEN, false));

          /* Perform Operation */
          fNewsDAO.setState(newsToDelete, INews.State.HIDDEN, false, false);
        }
      }
    };

    /* Run in background given the potential time the op takes */
    if (shouldRunInBackground(entities, newsToDelete))
      deleteInBackground(deleteRunnable);

    /* Run this potential short op right away */
    else
      deleteRunnable.run();
  }

  private boolean includesArchive(IStructuredSelection selection) {
    INewsBin archive = CoreUtils.findArchive();
    if (archive == null)
      return false;

    List<IEntity> entities = ModelUtils.getEntities(selection);
    CoreUtils.normalize(entities);
    for (IEntity entity : entities) {
      if (entity.equals(archive))
        return true;

      if (entity instanceof IFolder && includesArchive((IFolder) entity, archive))
        return true;
    }

    return false;
  }

  private boolean includesArchive(IFolder folder, INewsBin archive) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {
      if (child.equals(archive))
        return true;

      if (child instanceof IFolder && includesArchive((IFolder) child, archive))
        return true;
    }

    return false;
  }

  private void restoreEditorsIfNecessary(List<IEntity> entitiesToDelete) {
    boolean restore = false;
    for (IEntity entity : entitiesToDelete) {
      if (entity instanceof IFolderChild) {
        restore = true;
        break;
      }
    }

    if (restore)
      OwlUI.getFeedViews();
  }

  private boolean shouldRunInBackground(List<IEntity> entities, List<INews> newsToDelete) {
    AtomicInteger newsCount = new AtomicInteger();
    newsCount.addAndGet(newsToDelete.size());

    for (IEntity entity : entities)
      countNewsWithLimit(entity, newsCount, RUN_IN_BACKGROUND_CAP);

    return newsCount.get() > RUN_IN_BACKGROUND_CAP;
  }

  private void countNewsWithLimit(IEntity entity, AtomicInteger count, int limit) {

    /* Check Limit first */
    if (count.get() > limit)
      return;

    /* Bookmark */
    if (entity instanceof IBookMark)
      count.addAndGet(ModelUtils.countNews(((IBookMark) entity)));

    /* News Bin */
    else if (entity instanceof INewsBin)
      count.addAndGet(((INewsBin) entity).getNewsCount(INews.State.getVisible()));

    /* Folder */
    else if (entity instanceof IFolder) {
      IFolder folder = (IFolder) entity;
      List<IFolderChild> children = folder.getChildren();
      for (IFolderChild child : children) {
        if (count.get() > limit)
          return;

        countNewsWithLimit(child, count, limit);
      }
    }
  }

  private void deleteInBackground(final Runnable deleteRunnable) {

    /* Runnable with Progress */
    IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.DeleteTypesAction_WAIT_DELETE, IProgressMonitor.UNKNOWN);
        try {
          deleteRunnable.run();
        } finally {
          monitor.done();
        }
      }
    };

    /* Progress Dialog */
    Shell shell = fShell;
    if (shell == null)
      shell = OwlUI.getActiveShell();

    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell) {
      @Override
      protected void initializeBounds() {
        super.initializeBounds();

        /* Size */
        Shell shell = getShell();
        int width = convertHorizontalDLUsToPixels(OwlUI.MIN_DIALOG_WIDTH_DLU);
        shell.setSize(width, shell.getSize().y);

        /* New Location */
        Rectangle containerBounds = shell.getParent().getBounds();
        int x = Math.max(0, containerBounds.x + (containerBounds.width - width) / 2);
        shell.setLocation(x, shell.getLocation().y);
      }
    };

    /* Open and Run */
    try {
      dialog.run(true, false, runnableWithProgress);
    } catch (InvocationTargetException e) {
      Activator.safeLogError(e.getMessage(), e);
    } catch (InterruptedException e) {
      Activator.safeLogError(e.getMessage(), e);
    }
  }

  /**
   * @return <code>TRUE</code> if the user confirmed the deletion and
   * <code>FALSE</code> otherwise.
   */
  public boolean isConfirmed() {
    return fConfirmed;
  }
}