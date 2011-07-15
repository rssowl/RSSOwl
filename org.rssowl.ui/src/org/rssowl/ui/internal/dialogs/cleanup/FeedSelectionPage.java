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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.PreviewFeedDialog;
import org.rssowl.ui.internal.util.FolderChildCheckboxTree;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class FeedSelectionPage extends WizardPage {
  private FolderChildCheckboxTree fFolderChildTree;
  private CheckboxTreeViewer fViewer;
  private Button fSelectAll;
  private Button fDeselectAll;
  private Button fDisplayFeedButton;

  /**
   * @param pageName
   */
  protected FeedSelectionPage(String pageName) {
    super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/cleanup_wiz.gif")); //$NON-NLS-1$
    setMessage(Messages.FeedSelectionPage_CHOOSE_BOOKMARKS);
  }

  /* Returns all selected Bookmarks */
  Set<IBookMark> getSelection() {
    Set<IBookMark> selection = new HashSet<IBookMark>();

    Object[] checkedElements = fViewer.getCheckedElements();
    for (Object checkedElement : checkedElements) {

      /* Folder */
      if (checkedElement instanceof IFolder)
        addAll(selection, (IFolder) checkedElement);

      /* Bookmark */
      else if (checkedElement instanceof IBookMark)
        selection.add((IBookMark) checkedElement);
    }

    return selection;
  }

  private void addAll(Set<IBookMark> bookmarks, IFolder folder) {

    /* Child Folders */
    List<IFolder> childFolders = folder.getFolders();
    for (IFolder childFolder : childFolders) {
      addAll(bookmarks, childFolder);
    }

    /* Bookmarks */
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark)
        bookmarks.add((IBookMark) mark);
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* Viewer for Folder Child Selection */
    fFolderChildTree = new FolderChildCheckboxTree(container, true);
    fViewer = fFolderChildTree.getViewer();

    /* Filter out any non Bookmarks and empty folders */
    fViewer.addFilter(new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IFolder)
          return hasBookMarks((IFolder) element);

        return element instanceof IBookMark;
      }
    });

    /* Update Display Button on Selection */
    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        fDisplayFeedButton.setEnabled(((IStructuredSelection) event.getSelection()).getFirstElement() instanceof IBookMark);
      }
    });

    /* Show Feed on Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        showFeeds((IStructuredSelection) event.getSelection());
      }
    });

    /* Set Input */
    fFolderChildTree.getViewer().setInput(CoreUtils.loadRootFolders());
    fFolderChildTree.setAllChecked(true);

    /* Select All / Deselect All */
    Composite buttonContainer = new Composite(container, SWT.NONE);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(4, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fSelectAll = new Button(buttonContainer, SWT.PUSH);
    fSelectAll.setText(Messages.FeedSelectionPage_SELECT_ALL);
    Dialog.applyDialogFont(fSelectAll);
    setButtonLayoutData(fSelectAll);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setAllChecked(true);
      }
    });

    fDeselectAll = new Button(buttonContainer, SWT.PUSH);
    fDeselectAll.setText(Messages.FeedSelectionPage_DESELECT_ALL);
    Dialog.applyDialogFont(fDeselectAll);
    setButtonLayoutData(fDeselectAll);
    fDeselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setAllChecked(false);
      }
    });

    if (!Application.IS_MAC) {
      Label sep = new Label(buttonContainer, SWT.SEPARATOR | SWT.VERTICAL);
      sep.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
      ((GridData) sep.getLayoutData()).heightHint = 20;
    }

    fDisplayFeedButton = new Button(buttonContainer, SWT.PUSH);
    fDisplayFeedButton.setText(Messages.FeedSelectionPage_DISPLAY);
    fDisplayFeedButton.setEnabled(false);
    fDisplayFeedButton.setToolTipText(Messages.FeedSelectionPage_DISPLAY_FEEDS);
    Dialog.applyDialogFont(fDisplayFeedButton);
    setButtonLayoutData(fDisplayFeedButton);
    fDisplayFeedButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showFeeds((IStructuredSelection) fViewer.getSelection());
      }
    });

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  private void showFeeds(IStructuredSelection selection) {
    if (!selection.isEmpty()) {
      Object[] elements = selection.toArray();
      int offset = 0;
      for (Object element : elements) {
        if (element instanceof IBookMark) {
          IBookMark bookmark = (IBookMark) element;

          PreviewFeedDialog dialog = new PreviewFeedDialog(getShell(), bookmark, bookmark.getFeedLinkReference());
          dialog.setBlockOnOpen(false);
          dialog.open();

          if (offset != 0) {
            Point location = dialog.getShell().getLocation();
            dialog.getShell().setLocation(location.x + offset, location.y + offset);
          }

          offset += 20;
        }
      }
    }
  }

  private boolean hasBookMarks(IFolder folder) {
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark)
        return true;
    }

    List<IFolder> childFolders = folder.getFolders();
    for (IFolder childFolder : childFolders) {
      if (hasBookMarks(childFolder))
        return true;
    }

    return false;
  }
}