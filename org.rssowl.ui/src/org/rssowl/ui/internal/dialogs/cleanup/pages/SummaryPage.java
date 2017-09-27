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

package org.rssowl.ui.internal.dialogs.cleanup.pages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.TreeItem;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.PreviewFeedDialog;
import org.rssowl.ui.internal.dialogs.cleanup.operations.ICleanUpOperation;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.AbstractCleanUpTask;
import org.rssowl.ui.internal.dialogs.cleanup.tasks.BookMarkDeleteTask;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public final class SummaryPage extends WizardPage {
  private CheckboxTreeViewer fViewer;
  private ResourceManager fResources;
  private Button fSelectAll;
  private Button fDeselectAll;
  private Button fDisplayFeedButton;

  /* Summary Label Provider */
  class SummaryLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {
    private Color fGradientFgColor;
    private Color fGradientBgColor;
    private Color fGradientEndColor;
    private Color fGroupFgColor;

    SummaryLabelProvider() {
      fGradientFgColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_FG_COLOR);
      fGradientBgColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_BG_COLOR);
      fGradientEndColor = OwlUI.getColor(fResources, OwlUI.GROUP_GRADIENT_END_COLOR);
      fGroupFgColor = OwlUI.getColor(fResources, OwlUI.GROUP_FG_COLOR);
    }

    @Override
    public String getText(Object element) {
      if (element instanceof SummaryModelTaskGroup)
        return ((SummaryModelTaskGroup) element).getLabel();

      return ((AbstractCleanUpTask) element).getLabel();
    }

    @Override
    public Image getImage(Object element) {
      if (element instanceof SummaryModelTaskGroup)
        return OwlUI.getImage(fResources, OwlUI.GROUP);

      return OwlUI.getImage(fResources, ((AbstractCleanUpTask) element).getImage());
    }

    public Font getFont(Object element) {
      if (element instanceof SummaryModelTaskGroup)
        return OwlUI.getBold(JFaceResources.DEFAULT_FONT);

      return null;
    }

    public Color getBackground(Object element) {
      return null;
    }

    public Color getForeground(Object element) {
      if (element instanceof SummaryModelTaskGroup && !OwlUI.isHighContrast())
        return fGroupFgColor;

      return null;
    }

    void eraseGroup(Event event) {
      Scrollable scrollable = (Scrollable) event.widget;
      GC gc = event.gc;

      Rectangle area = scrollable.getClientArea();
      Rectangle rect = event.getBounds();

      /* Paint the selection beyond the end of last column */
      OwlUI.codExpandRegion(event, scrollable, gc, area);

      /* Draw Gradient Rectangle */
      Color oldForeground = gc.getForeground();
      Color oldBackground = gc.getBackground();

      /* Gradient */
      gc.setForeground(fGradientFgColor);
      gc.setBackground(fGradientBgColor);
      gc.fillGradientRectangle(0, rect.y, area.width, rect.height, true);

      /* Bottom Line */
      gc.setForeground(fGradientEndColor);
      gc.drawLine(0, rect.y + rect.height - 1, area.width, rect.y + rect.height - 1);

      gc.setForeground(oldForeground);
      gc.setBackground(oldBackground);

      /* Mark as Background being handled */
      event.detail &= ~SWT.BACKGROUND;
    }
  }

  /**
   * @param pageName
   */
  public SummaryPage(String pageName) {
    super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/cleanup_wiz.gif")); //$NON-NLS-1$
    setMessage(Messages.CleanUpSummaryPage_REVIEW_OPS);
    fResources = new LocalResourceManager(JFaceResources.getResources());
  }

  /*
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete() {
    return isCurrentPage();
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    fResources.dispose();
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* Viewer to select particular Tasks */
    fViewer = new CheckboxTreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
    fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fViewer.getTree().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    /* ContentProvider */
    fViewer.setContentProvider(getSummaryTreeContentProvider());

    /* LabelProvider */
    final SummaryLabelProvider summaryLabelProvider = new SummaryLabelProvider();
    fViewer.setLabelProvider(summaryLabelProvider);

    /* Custom Owner Drawn Category */
    if (!OwlUI.isHighContrast()) {
      fViewer.getControl().addListener(SWT.EraseItem, new Listener() {
        public void handleEvent(Event event) {
          Object element = event.item.getData();
          if (element instanceof SummaryModelTaskGroup)
            summaryLabelProvider.eraseGroup(event);
        }
      });
    }

    /* Listen on Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        onDoubleClick(event);
      }
    });

    /* Update Checks on Selection */
    fViewer.getTree().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onSelect(e);
      }
    });

    /* Update Display Button on Selection */
    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        fDisplayFeedButton.setEnabled(((IStructuredSelection) event.getSelection()).getFirstElement() instanceof BookMarkDeleteTask);
      }
    });

    /* Update Checks on Expand */
    fViewer.addTreeListener(new ITreeViewerListener() {
      public void treeExpanded(TreeExpansionEvent event) {
        onExpand(event);
      }

      public void treeCollapsed(TreeExpansionEvent event) {}
    });

    /* Select All / Deselect All */
    Composite buttonContainer = new Composite(container, SWT.NONE);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(4, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fSelectAll = new Button(buttonContainer, SWT.PUSH);
    fSelectAll.setText(Messages.CleanUpSummaryPage_SELECT_ALL);
    Dialog.applyDialogFont(fSelectAll);
    setButtonLayoutData(fSelectAll);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        OwlUI.setAllChecked(fViewer.getTree(), true);
      }
    });

    fDeselectAll = new Button(buttonContainer, SWT.PUSH);
    fDeselectAll.setText(Messages.CleanUpSummaryPage_DESELECT_ALL);
    Dialog.applyDialogFont(fDeselectAll);
    setButtonLayoutData(fDeselectAll);
    fDeselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        OwlUI.setAllChecked(fViewer.getTree(), false);
      }
    });

    if (!Application.IS_MAC) {
      Label sep = new Label(buttonContainer, SWT.SEPARATOR | SWT.VERTICAL);
      sep.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
      ((GridData) sep.getLayoutData()).heightHint = 20;
    }

    fDisplayFeedButton = new Button(buttonContainer, SWT.PUSH);
    fDisplayFeedButton.setText(Messages.CleanUpSummaryPage_DISPLAY);
    fDisplayFeedButton.setEnabled(false);
    fDisplayFeedButton.setToolTipText(Messages.CleanUpSummaryPage_DISPLAY_FEED);
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

  /**
   * @return
   */
  private ITreeContentProvider getSummaryTreeContentProvider() {
    return new ITreeContentProvider() {
      public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List<?>)
          return ((List<?>) inputElement).toArray();

        return new Object[0];
      }

      public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof SummaryModelTaskGroup) {
          SummaryModelTaskGroup group = (SummaryModelTaskGroup) parentElement;
          return group.getTasks().toArray();
        }

        return new Object[0];
      }

      public Object getParent(Object element) {
        if (element instanceof AbstractCleanUpTask)
          return ((AbstractCleanUpTask) element).getGroup();

        return null;
      }

      public boolean hasChildren(Object element) {
        return element instanceof SummaryModelTaskGroup;
      }

      public void dispose() {}

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    };
  }

  private void onDoubleClick(DoubleClickEvent event) {
    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    SummaryModelTaskGroup group = selection.getFirstElement() instanceof SummaryModelTaskGroup ? (SummaryModelTaskGroup) selection.getFirstElement() : null;

    /* Expand / Collapse Folder */
    if (group != null) {
      boolean expandedState = !fViewer.getExpandedState(group);
      fViewer.setExpandedState(group, expandedState);

      if (expandedState && fViewer.getChecked(group))
        setChildsChecked(group, true);
    }

    /* Display Bookmark */
    else if (selection.getFirstElement() instanceof BookMarkDeleteTask) {
      showFeeds(selection);
    }
  }

  private void onSelect(SelectionEvent e) {
    if (e.detail == SWT.CHECK) {
      TreeItem item = (TreeItem) e.item;

      if (item.getData() instanceof SummaryModelTaskGroup)
        setChildsChecked((SummaryModelTaskGroup) item.getData(), item.getChecked());

      if (!item.getChecked() && item.getData() instanceof AbstractCleanUpTask)
        setParentsChecked((AbstractCleanUpTask) item.getData(), false);
    }
  }

  private void onExpand(TreeExpansionEvent event) {
    boolean isChecked = fViewer.getChecked(event.getElement());
    if (isChecked)
      setChildsChecked((SummaryModelTaskGroup) event.getElement(), isChecked);
  }

  private void setChildsChecked(SummaryModelTaskGroup cleanUpGroup, boolean checked) {
    List<AbstractCleanUpTask> children = cleanUpGroup.getTasks();
    for (AbstractCleanUpTask child : children)
      fViewer.setChecked(child, checked);
  }

  private void setParentsChecked(AbstractCleanUpTask cleanUpTask, boolean checked) {
    SummaryModelTaskGroup parent = cleanUpTask.getGroup();
    if (parent != null)
      fViewer.setChecked(parent, checked);
  }

  private void showFeeds(IStructuredSelection selection) {
    if (!selection.isEmpty()) {
      Object firstElement = selection.getFirstElement();
      if (firstElement instanceof BookMarkDeleteTask) {
        IBookMark bookmark = ((BookMarkDeleteTask) firstElement).getBookmarkMark();

        PreviewFeedDialog dialog = new PreviewFeedDialog(getShell(), bookmark, bookmark.getFeedLinkReference());
        dialog.setBlockOnOpen(false);
        dialog.open();
      }
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    /* Generate the Summary */
    if (visible) {
      OperationsPage cleanUpOptionsPage = (OperationsPage) getPreviousPage();
      EntitySelectionPage feedSelectionPage = (EntitySelectionPage) cleanUpOptionsPage.getPreviousPage();

      final Set<IBookMark> selection = feedSelectionPage.getSelectedBookMarks();
      final ArrayList<ICleanUpOperation> operations = cleanUpOptionsPage.getOperations();

      IRunnableWithProgress runnable = new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor) {
          monitor.beginTask(Messages.CleanUpSummaryPage_WAIT_GENERATE_PREVIEW, IProgressMonitor.UNKNOWN);
          onGenerateSummary(operations, selection, monitor);
        }
      };

      try {
        getContainer().run(true, true, runnable);
      } catch (InvocationTargetException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (InterruptedException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }
  }

  private void onGenerateSummary(ArrayList<ICleanUpOperation> operations, Set<IBookMark> selection, IProgressMonitor monitor) {
    final SummaryModel model = new SummaryModel(operations, selection);
    model.generate(monitor);

    /* Show in Viewer */
    JobRunner.runInUIThread(fViewer.getTree(), new Runnable() {
      public void run() {
        fViewer.setInput(model.getSummaryTaskGroups());
        fViewer.expandAll();
        OwlUI.setAllChecked(fViewer.getTree(), true);
      }
    });
  }

  public List<AbstractCleanUpTask> getTasks() {
    Object[] checkedElements = fViewer.getCheckedElements();
    List<AbstractCleanUpTask> tasks = new ArrayList<AbstractCleanUpTask>(checkedElements.length);

    for (Object checkedElement : checkedElements)
      if (checkedElement instanceof AbstractCleanUpTask)
        tasks.add((AbstractCleanUpTask) checkedElement);

    return tasks;
  }
}