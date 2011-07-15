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

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.RegExUtils;
import org.rssowl.core.util.ReparentInfo;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.ImportAction;
import org.rssowl.ui.internal.actions.MoveCopyNewsToBinAction;
import org.rssowl.ui.internal.actions.NewBookMarkAction;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;
import org.rssowl.ui.internal.util.JobRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The <code>BookMarkDropImpl</code> is handling all drop-operations resulting
 * from a DND.
 *
 * @author bpasero
 */
public class BookMarkDNDImpl extends ViewerDropAdapter implements DragSourceListener {
  private final BookMarkExplorer fExplorer;
  private final IFolderDAO fFolderDAO;

  /**
   * @param explorer
   * @param viewer
   */
  protected BookMarkDNDImpl(BookMarkExplorer explorer, Viewer viewer) {
    super(viewer);
    fExplorer = explorer;
    fFolderDAO = DynamicDAO.getDAO(IFolderDAO.class);
  }

  /*
   * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
   */
  public void dragStart(final DragSourceEvent event) {

    /* Set normalized selection into Transfer if not in grouping mode */
    if (!fExplorer.isGroupingEnabled()) {
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          LocalSelectionTransfer.getTransfer().setSelection(getNormalizedSelection());
          LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
          event.doit = true;
        }
      });
    }
  }

  private ISelection getNormalizedSelection() {
    IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
    List<?> selectedObjects = selection.toList();

    /* Retrieve dragged Marks / Folders and separate */
    List<IEntity> draggedEntities = new ArrayList<IEntity>(selectedObjects.size());
    List<IFolder> draggedFolders = new ArrayList<IFolder>(selectedObjects.size());
    for (Object object : selectedObjects) {

      /* Dragged Mark */
      if (object instanceof IMark)
        draggedEntities.add((IMark) object);

      /* Dragged Folder */
      else if (object instanceof IFolder) {
        draggedEntities.add((IFolder) object);
        draggedFolders.add((IFolder) object);
      }
    }

    /* Normalize the dragged entities */
    for (IFolder folder : draggedFolders)
      CoreUtils.normalize(folder, draggedEntities);

    return new StructuredSelection(draggedEntities);
  }

  /*
   * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
   */
  public void dragSetData(final DragSourceEvent event) {
    SafeRunner.run(new LoggingSafeRunnable() {
      public void run() throws Exception {

        /* Set Selection using LocalSelectionTransfer */
        if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType))
          event.data = LocalSelectionTransfer.getTransfer().getSelection();

        /* Set Text using TextTransfer */
        else if (TextTransfer.getInstance().isSupportedType(event.dataType))
          setTextData(event);

        /* Set Text using URLTranser */
        else if (URLTransfer.getInstance().isSupportedType(event.dataType))
          setURLData(event);
      }
    });
  }

  private void setTextData(DragSourceEvent event) {
    StringBuilder str = new StringBuilder(""); //$NON-NLS-1$
    IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
    List<?> selectedObjects = selection.toList();
    for (Object selectedObject : selectedObjects) {

      /* IBookMark */
      if (selectedObject instanceof IBookMark) {
        IBookMark bookmark = (IBookMark) selectedObject;
        str.append(URIUtils.toHTTP(bookmark.getFeedLinkReference().getLinkAsText())).append("\n"); //$NON-NLS-1$
        str.append(bookmark.getName()).append("\n\n"); //$NON-NLS-1$
      }

      /* Any other Folder Child */
      else if (selectedObject instanceof IFolderChild) {
        IFolderChild folderchild = (IFolderChild) selectedObject;
        str.append(folderchild.getName()).append("\n"); //$NON-NLS-1$
      }

      /* Entity Group */
      else if (selectedObject instanceof EntityGroup) {
        EntityGroup entitygroup = (EntityGroup) selectedObject;
        str.append(entitygroup.getName()).append("\n"); //$NON-NLS-1$
      }
    }

    if (str.length() > 0)
      event.data = str.toString();
  }

  private void setURLData(DragSourceEvent event) {
    StringBuilder str = new StringBuilder(""); //$NON-NLS-1$
    IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
    List<?> selectedObjects = selection.toList();
    for (Object selectedObject : selectedObjects) {

      /* IBookMark */
      if (selectedObject instanceof IBookMark) {
        IBookMark bookmark = (IBookMark) selectedObject;
        str.append(URIUtils.toHTTP(bookmark.getFeedLinkReference().getLinkAsText())).append("\n"); //$NON-NLS-1$
      }
    }

    if (str.length() > 0)
      event.data = str.toString();
  }

  /*
   * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
   */
  public void dragFinished(DragSourceEvent event) {
    SafeRunner.run(new LoggingSafeRunnable() {
      public void run() throws Exception {
        LocalSelectionTransfer.getTransfer().setSelection(null);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
      }
    });
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
   */
  @Override
  public void dragOver(DropTargetEvent event) {
    super.dragOver(event);

    Object currentTarget = getCurrentTarget();
    boolean isFolderChildsDragged = isFolderChildsDragged();

    /* Un-Set some feedback if sorting by name */
    if (fExplorer.isSortByNameEnabled()) {
      event.feedback &= ~DND.FEEDBACK_INSERT_AFTER;
      event.feedback &= ~DND.FEEDBACK_INSERT_BEFORE;
    }

    /* Un-Set some feedback if grouping */
    if (fExplorer.isGroupingEnabled() && !(currentTarget instanceof INewsBin)) {
      event.feedback &= ~DND.FEEDBACK_INSERT_AFTER;
      event.feedback &= ~DND.FEEDBACK_INSERT_BEFORE;
      event.feedback &= ~DND.FEEDBACK_SELECT;
    }

    /* Never give Select as Feedback from a Mark except INewsBin */
    if (currentTarget instanceof IMark && (!(currentTarget instanceof INewsBin) || isFolderChildsDragged))
      event.feedback &= ~DND.FEEDBACK_SELECT;

    /* Don't show this feedback for News Bins when non Folder-Childs are dragged */
    if (currentTarget instanceof INewsBin && !isFolderChildsDragged) {
      event.feedback &= ~DND.FEEDBACK_INSERT_AFTER;
      event.feedback &= ~DND.FEEDBACK_INSERT_BEFORE;
    }

    /* Unset some feedback when Text-, File- or URLTransfer is used */
    if (TextTransfer.getInstance().isSupportedType(event.currentDataType) || URLTransfer.getInstance().isSupportedType(event.currentDataType) || FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
      event.feedback &= ~DND.FEEDBACK_INSERT_AFTER;
      event.feedback &= ~DND.FEEDBACK_INSERT_BEFORE;
    }

    /* Fix for eclipse bug 235136 in ViewerDropAdapter that does not support URLTransfer */
    if (event.detail == DND.DROP_NONE && URLTransfer.getInstance().isSupportedType(event.currentDataType))
      event.detail = DND.DROP_LINK;
  }

  private boolean isFolderChildsDragged() {
    IStructuredSelection currentSource = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
    if (currentSource != null) {
      List<?> draggedItems = currentSource.toList();
      return containsFolderChilds(draggedItems);
    }

    return true;
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object,
   * int, org.eclipse.swt.dnd.TransferData)
   */
  @Override
  public boolean validateDrop(final Object target, int operation, TransferData transferType) {

    /* Text-, File- and URLTransfer (supported for all) */
    if (TextTransfer.getInstance().isSupportedType(transferType) || URLTransfer.getInstance().isSupportedType(transferType) || FileTransfer.getInstance().isSupportedType(transferType))
      return true;

    /* Require Entity as Target for other transfers */
    if (!(target instanceof IEntity))
      return false;

    /* Selection Transfer */
    if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
      final boolean[] result = new boolean[] { false };
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
          if (selection instanceof IStructuredSelection) {
            List<?> draggedObjects = ((IStructuredSelection) selection).toList();
            result[0] = isValidDrop((IEntity) target, draggedObjects);
          }
        }
      });

      return result[0];
    }

    return false;
  }

  private boolean isValidDrop(IEntity dropTarget, List<?> draggedObjects) {

    /* Check validity for each dragged Object */
    for (Object draggedObject : draggedObjects) {

      /* Shared rule: Do not allow drop when grouping is enabled */
      if (draggedObject instanceof IFolderChild && fExplorer.isGroupingEnabled())
        return false;

      /* Shared rule: Do not allow to drop on same Entity */
      if (draggedObject.equals(dropTarget))
        return false;

      /* Dragged Folder */
      if (draggedObject instanceof IFolder) {
        IFolder draggedFolder = (IFolder) draggedObject;
        if (!isValidDrop(draggedFolder, dropTarget))
          return false;
      }

      /* Dragged Mark */
      else if (draggedObject instanceof IMark) {
        IMark draggedMark = (IMark) draggedObject;
        if (!isValidDrop(draggedMark, dropTarget))
          return false;
      }

      /* Dragged News */
      else if (draggedObject instanceof INews) {
        INews draggedNews = (INews) draggedObject;
        return isValidDrop(draggedNews, dropTarget);
      }

      /* Dragged Entity Group of News */
      else if (draggedObject instanceof EntityGroup) {
        EntityGroup group = (EntityGroup) draggedObject;
        return isValidDrop(group, dropTarget);
      }
    }

    return true;
  }

  private boolean isValidDrop(@SuppressWarnings("unused")
  INews dragSource, IEntity dropTarget) {
    int loc = getCurrentLocation();

    /* Require to drop on actual Entity */
    if (loc == LOCATION_BEFORE || loc == LOCATION_AFTER)
      return false;

    /* Require News Bin as target */
    return dropTarget instanceof INewsBin;
  }

  private boolean isValidDrop(EntityGroup group, IEntity dropTarget) {
    int loc = getCurrentLocation();

    /* Require to drop on actual Entity */
    if (loc == LOCATION_BEFORE || loc == LOCATION_AFTER)
      return false;

    /* Require News Bin as target */
    return dropTarget instanceof INewsBin && NewsGrouping.GROUP_CATEGORY_ID.equals(group.getCategory());
  }

  private boolean isValidDrop(IFolder dragSource, IEntity dropTarget) {
    int loc = getCurrentLocation();

    /* Do not allow dropping on same Parent */
    if (loc == LOCATION_ON && dragSource.getParent().equals(dropTarget))
      return false;

    /* Do not allow Re-Ordering of Entities if sort by name */
    if (fExplorer.isSortByNameEnabled() && (loc == LOCATION_AFTER || loc == LOCATION_BEFORE)) {
      if (dropTarget instanceof IFolder) {
        IFolder target = (IFolder) dropTarget;
        if (target.getParent().containsChild(dragSource))
          return false;
      }
    }

    /* Do not allow Re-Ordering over IMarks (when sorting or grouping) */
    if ((fExplorer.isSortByNameEnabled() || fExplorer.isGroupingEnabled()) && dropTarget instanceof IMark) {
      IMark target = (IMark) dropTarget;
      if (target.getParent().containsChild(dragSource))
        return false;
    }

    /* Do not allow dropping in Child of Drag-Folder */
    if (CoreUtils.hasChildRelation(dragSource, dropTarget))
      return false;

    return true;
  }

  private boolean isValidDrop(IMark dragSource, IEntity dropTarget) {
    int loc = getCurrentLocation();

    /* Do not allow dropping on same Parent */
    if (loc == LOCATION_ON && dragSource.getParent().equals(dropTarget))
      return false;

    /* Do not allow Re-Ordering of Entities if sort by name */
    if (fExplorer.isSortByNameEnabled()) {
      if (dropTarget instanceof IMark) {
        IMark target = (IMark) dropTarget;
        if (target.getParent().containsChild(dragSource))
          return false;
      }
    }

    /* Do not allow Re-Ordering over IFolder (when sorting or grouping) */
    if ((fExplorer.isSortByNameEnabled() || fExplorer.isGroupingEnabled()) && dropTarget instanceof IFolder && (loc == LOCATION_AFTER || loc == LOCATION_BEFORE)) {
      IFolder target = (IFolder) dropTarget;
      if (target.getParent().containsChild(dragSource))
        return false;
    }

    return true;
  }

  private boolean containsFolderChilds(List<?> draggedObjects) {
    for (Object object : draggedObjects) {
      if (object instanceof IFolderChild)
        return true;
    }

    return false;
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
   */
  @Override
  public boolean performDrop(final Object data) {

    /* Selection-Transfer */
    if (data instanceof IStructuredSelection) {
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          IStructuredSelection selection = (IStructuredSelection) data;
          List<?> draggedObjects = selection.toList();

          if (getCurrentTarget() instanceof INewsBin && !containsFolderChilds(draggedObjects))
            perfromNewsDrop(draggedObjects);
          else
            perfromFolderChildDrop(draggedObjects);
        }
      });

      return true;
    }

    /* Text-Transfer (check for URLs) */
    else if (data instanceof String) {
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          final List<String> urls = RegExUtils.extractLinksFromText((String) data, false);
          if (urls.size() > 0) {

            /* Determine parent folder */
            final IFolder parent = getParentFolderFromDropTarget();

            /* Determine Position */
            Object dropTarget = getCurrentTarget();
            final IMark position = (IMark) ((dropTarget instanceof IMark) ? dropTarget : null);

            /* Open Dialog to add new BookMark (asyncly!) */
            JobRunner.runInUIThread(0, true, getViewer().getControl(), new Runnable() {
              public void run() {
                new NewBookMarkAction(getViewer().getControl().getShell(), parent, position, urls.get(0)).run(null);
              }
            });
          }
        }
      });

      return true;
    }

    /* File-Transfer (check for file to import feeds from) */
    else if (data instanceof String[]) {
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          String[] strArray = (String[]) data;
          if (strArray.length != 0) {
            final String strVal = strArray[0];
            File file = new File(strVal);
            if (file.exists() && (strVal.endsWith(".opml") || strVal.endsWith(".xml"))) { //$NON-NLS-1$ //$NON-NLS-2$

              /* Determine parent folder */
              final IFolder parent = getParentFolderFromDropTarget();

              /* Open Dialog to import Feeds (asyncly!) */
              JobRunner.runInUIThread(0, true, getViewer().getControl(), new Runnable() {
                public void run() {
                  ImportAction action = new ImportAction();
                  action.openWizardForFileImport(getViewer().getControl().getShell(), parent, strVal);
                }
              });
            }
          }
        }
      });

      return true;
    }

    return false;
  }

  private IFolder getParentFolderFromDropTarget() {
    Object dropTarget = getCurrentTarget();

    if (dropTarget instanceof IFolder)
      return (IFolder) dropTarget;

    if (dropTarget instanceof IMark)
      return ((IMark) dropTarget).getParent();

    if (dropTarget == null)
      return OwlUI.getSelectedBookMarkSet();

    return null;
  }

  private void perfromNewsDrop(List<?> draggedObjects) {
    int operation = getCurrentOperation();
    INewsBin dropTarget = (INewsBin) getCurrentTarget();

    new MoveCopyNewsToBinAction(new StructuredSelection(draggedObjects), dropTarget, operation == DND.DROP_MOVE).run();
  }

  private void perfromFolderChildDrop(List<?> draggedObjects) {
    Object dropTarget = getCurrentTarget();
    int location = getCurrentLocation();

    IFolder parentFolder = null;
    boolean requireSave = false;
    boolean on = (location == ViewerDropAdapter.LOCATION_ON);

    IFolderChild position = dropTarget != null ? (IFolderChild) dropTarget : null;

    /* Fix invalid Position */
    if (on && dropTarget instanceof IFolder)
      position = null;

    Boolean after = (location == ViewerDropAdapter.LOCATION_AFTER);
    if (position == null)
      after = null;

    /* Target is a Folder */
    if (dropTarget instanceof IFolder) {
      IFolder dropFolder = (IFolder) dropTarget;

      /* Target is the exact Folder */
      if (on)
        parentFolder = (IFolder) dropTarget;

      /* Target is below or above of the Folder */
      else
        parentFolder = dropFolder.getParent();
    }

    /* Target is a Mark */
    else if (dropTarget instanceof IMark) {
      IMark dropMark = (IMark) dropTarget;
      parentFolder = dropMark.getParent();
    }

    /* Require a Parent-Folder */
    if (parentFolder == null)
      return;

    /* Separate into Reparented FolderChildren and Re-Orders */
    List<IFolderChild> childReordering = null;
    List<ReparentInfo<IFolderChild, IFolder>> reparenting = null;

    /* For each dragged Object */
    for (Object object : draggedObjects) {

      /* Dragged Folder or Mark */
      if (object instanceof IFolder || object instanceof IMark) {
        IFolderChild draggedFolderChild = (IFolderChild) object;

        /* Reparenting to new Parent */
        if (!draggedFolderChild.getParent().equals(parentFolder)) {
          if (reparenting == null)
            reparenting = new ArrayList<ReparentInfo<IFolderChild, IFolder>>(draggedObjects.size());

          ReparentInfo<IFolderChild, IFolder> reparentInfo = ReparentInfo.create(draggedFolderChild, parentFolder, position, after);
          reparenting.add(reparentInfo);
        }

        /* Re-Ordering in same Parent */
        else {
          if (childReordering == null)
            childReordering = new ArrayList<IFolderChild>(draggedObjects.size());
          childReordering.add(draggedFolderChild);
        }
      }
    }

    /* Perform reparenting */
    if (reparenting != null) {
      final List<ReparentInfo<IFolderChild, IFolder>> finalReparenting = reparenting;
      BusyIndicator.showWhile(getViewer().getControl().getDisplay(), new Runnable() {
        public void run() {
          CoreUtils.reparentWithProperties(finalReparenting);
        }
      });
    }

    /* Perform Re-Ordering on Children */
    if (childReordering != null) {
      parentFolder.reorderChildren(childReordering, (IFolderChild) dropTarget, after);
      requireSave = true;
    }

    /* Save the Folder if required */
    if (requireSave)
      fFolderDAO.save(parentFolder);
  }
}