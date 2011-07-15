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

package org.rssowl.ui.internal.dialogs.preferences;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.SyncItem;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.ConfirmDialog;
import org.rssowl.ui.internal.dialogs.LabelDialog;
import org.rssowl.ui.internal.dialogs.LabelDialog.DialogMode;
import org.rssowl.ui.internal.dialogs.NewsFiltersListDialog;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author bpasero
 */
public class ManageLabelsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  /** ID of the Page */
  public static final String ID = "org.rssowl.ui.ManageLabels"; //$NON-NLS-1$

  /* Number of News to process when deleting labels */
  private static final int LABELS_CHUNK_SIZE = 100;

  private LocalResourceManager fResources;
  private TreeViewer fViewer;
  private Button fMoveDownButton;
  private Button fMoveUpButton;

  /** Leave for reflection */
  public ManageLabelsPreferencePage() {
    fResources = new LocalResourceManager(JFaceResources.getResources());
    setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/labels.gif")); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
    noDefaultAndApplyButton();
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = createContainer(parent);

    /* Label */
    Label infoLabel = new Label(container, SWT.None);
    infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    infoLabel.setText(Messages.ManageLabelsPreferencePage_LABEL_INFO);

    /* Label Viewer */
    createViewer(container);

    /* Button Box */
    createButtons(container);

    /* Info Container */
    Composite infoContainer = new Composite(container, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));

    Label infoImg = new Label(infoContainer, SWT.NONE);
    infoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/info.gif")); //$NON-NLS-1$
    infoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    Link infoText = new Link(infoContainer, SWT.WRAP);
    infoText.setText(Messages.ManageLabelsPreferencePage_LABEL_TIP);
    infoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    infoText.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        NewsFiltersListDialog dialog = NewsFiltersListDialog.getVisibleInstance();
        if (dialog == null) {
          dialog = new NewsFiltersListDialog(getShell());
          dialog.setBlockOnOpen(false);
          dialog.open();
        } else {
          dialog.getShell().forceActive();
          if (dialog.getShell().getMinimized())
            dialog.getShell().setMinimized(false);
        }
      }
    });

    applyDialogFont(container);

    return container;
  }

  private void createButtons(Composite container) {
    Composite buttonBox = new Composite(container, SWT.None);
    buttonBox.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    buttonBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

    Button addButton = new Button(buttonBox, SWT.PUSH);
    addButton.setText(Messages.ManageLabelsPreferencePage_NEW);
    Dialog.applyDialogFont(addButton);
    setButtonLayoutData(addButton);
    addButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onAdd();
      }
    });

    final Button editButton = new Button(buttonBox, SWT.PUSH);
    editButton.setText(Messages.ManageLabelsPreferencePage_EDIT);
    editButton.setEnabled(!fViewer.getSelection().isEmpty());
    Dialog.applyDialogFont(editButton);
    setButtonLayoutData(editButton);
    editButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onEdit();
      }
    });

    final Button deleteButton = new Button(buttonBox, SWT.PUSH);
    deleteButton.setText(Messages.ManageLabelsPreferencePage_DELETE);
    deleteButton.setEnabled(!fViewer.getSelection().isEmpty());
    Dialog.applyDialogFont(deleteButton);
    setButtonLayoutData(deleteButton);
    deleteButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onDelete();
      }
    });

    /* Move Label Up */
    fMoveUpButton = new Button(buttonBox, SWT.PUSH);
    fMoveUpButton.setText(Messages.ManageLabelsPreferencePage_MOVE_UP);
    fMoveUpButton.setEnabled(false);
    Dialog.applyDialogFont(fMoveUpButton);
    setButtonLayoutData(fMoveUpButton);
    ((GridData) fMoveUpButton.getLayoutData()).verticalIndent = 10;
    fMoveUpButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onMove(true);
      }
    });

    /* Move Label Down */
    fMoveDownButton = new Button(buttonBox, SWT.PUSH);
    fMoveDownButton.setText(Messages.ManageLabelsPreferencePage_MOVE_DOWN);
    fMoveDownButton.setEnabled(false);
    Dialog.applyDialogFont(fMoveDownButton);
    setButtonLayoutData(fMoveDownButton);
    fMoveDownButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onMove(false);
      }
    });

    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        editButton.setEnabled(!event.getSelection().isEmpty());
        deleteButton.setEnabled(!event.getSelection().isEmpty());
        updateMoveEnablement();
      }
    });
  }

  private void onMove(boolean up) {
    TreeItem[] items = fViewer.getTree().getItems();
    List<ILabel> sortedLabels = new ArrayList<ILabel>(items.length);
    for (TreeItem item : items) {
      sortedLabels.add((ILabel) item.getData());
    }

    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    ILabel selectedLabel = (ILabel) selection.getFirstElement();
    int selectedLabelOrder = selectedLabel.getOrder();
    ILabel otherLabel = null;
    int index = sortedLabels.indexOf(selectedLabel);

    /* Move Up */
    if (up && index > 0) {
      otherLabel = sortedLabels.get(index - 1);
      selectedLabel.setOrder(otherLabel.getOrder());
      otherLabel.setOrder(selectedLabelOrder);
    }

    /* Move Down */
    else if (!up && index < sortedLabels.size() - 1) {
      otherLabel = sortedLabels.get(index + 1);
      selectedLabel.setOrder(otherLabel.getOrder());
      otherLabel.setOrder(selectedLabelOrder);
    }

    DynamicDAO.getDAO(ILabelDAO.class).saveAll(Arrays.asList(new ILabel[] { selectedLabel, otherLabel }));
    fViewer.refresh();
    fViewer.getTree().showSelection();
    updateMoveEnablement();
  }

  private void updateMoveEnablement() {
    boolean enableMoveUp = true;
    boolean enableMoveDown = true;

    TreeItem[] selection = fViewer.getTree().getSelection();
    int[] selectionIndices = new int[selection.length];
    for (int i = 0; i < selection.length; i++)
      selectionIndices[i] = fViewer.getTree().indexOf(selection[i]);

    if (selectionIndices.length == 1) {
      enableMoveUp = selectionIndices[0] != 0;
      enableMoveDown = selectionIndices[0] != fViewer.getTree().getItemCount() - 1;
    } else {
      enableMoveUp = false;
      enableMoveDown = false;
    }

    fMoveUpButton.setEnabled(enableMoveUp);
    fMoveDownButton.setEnabled(enableMoveDown);
  }

  private void onAdd() {
    LabelDialog dialog = new LabelDialog(getShell(), DialogMode.ADD, null);
    if (dialog.open() == IDialogConstants.OK_ID) {
      String name = dialog.getName();
      RGB color = dialog.getColor();

      ILabel newLabel = Owl.getModelFactory().createLabel(null, name);
      newLabel.setColor(OwlUI.toString(color));
      newLabel.setOrder(fViewer.getTree().getItemCount());
      DynamicDAO.save(newLabel);

      fViewer.refresh();
      fViewer.setSelection(new StructuredSelection(newLabel));
    }
    fViewer.getTree().setFocus();
  }

  private void onEdit() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    if (!selection.isEmpty()) {
      ILabel label = (ILabel) selection.getFirstElement();
      LabelDialog dialog = new LabelDialog(getShell(), DialogMode.EDIT, label);
      if (dialog.open() == IDialogConstants.OK_ID) {
        boolean changed = false;
        String name = dialog.getName();
        RGB color = dialog.getColor();

        if (!label.getName().equals(name)) {
          onLabelNameChanged(label, label.getName(), name);
          label.setName(name);
          changed = true;
        }

        String colorStr = OwlUI.toString(color);
        if (!label.getColor().equals(colorStr)) {
          label.setColor(colorStr);
          changed = true;
        }

        /* Save Label */
        if (changed) {
          Controller.getDefault().getSavedSearchService().forceQuickUpdate();
          DynamicDAO.save(label);
          fViewer.update(label, null);
        }
      }
    }
    fViewer.getTree().setFocus();
  }

  private void onDelete() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    if (!selection.isEmpty()) {
      final List<ILabel> selectedLabels = ModelUtils.getEntities(selection, ILabel.class);

      String msg;
      if (selectedLabels.size() == 1)
        msg = NLS.bind(Messages.ManageLabelsPreferencePage_DELETE_LABEL_N, selectedLabels.get(0).getName());
      else
        msg = NLS.bind(Messages.ManageLabelsPreferencePage_DELETE_N_LABELS, selectedLabels.size());

      ConfirmDialog dialog = new ConfirmDialog(getShell(), Messages.ManageLabelsPreferencePage_CONFIRM_DELETE, Messages.ManageLabelsPreferencePage_NO_UNDO, msg, null);
      if (dialog.open() == IDialogConstants.OK_ID)
        deleteInBackground(selectedLabels);
    }
  }

  private void onLabelNameChanged(final ILabel label, final String oldName, final String newName) {
    IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.ManageLabelsPreferencePage_WAIT_UPDATE, IProgressMonitor.UNKNOWN);
        try {
          List<SyncItem> syncItems = new ArrayList<SyncItem>();

          ISearchField labelField = Owl.getModelFactory().createSearchField(INews.LABEL, INews.class.getName());
          ISearchField feedField = Owl.getModelFactory().createSearchField(INews.FEED, INews.class.getName());

          ISearchCondition labelCondition = Owl.getModelFactory().createSearchCondition(labelField, SearchSpecifier.IS, oldName);
          ISearchCondition feedCondition = Owl.getModelFactory().createSearchCondition(feedField, SearchSpecifier.BEGINS_WITH, SyncUtils.READER_HTTP_SCHEME);

          /* Find all news that are under sync control and have the label assigned */
          List<SearchHit<NewsReference>> result = Owl.getPersistenceService().getModelSearch().searchNews(Arrays.asList(labelCondition, feedCondition), true);
          List<List<SearchHit<NewsReference>>> chunks = CoreUtils.toChunks(result, LABELS_CHUNK_SIZE);
          for (List<SearchHit<NewsReference>> chunk : chunks) {
            for (SearchHit<NewsReference> item : chunk) {
              INews news = item.getResult().resolve();

              /* Item Exists */
              if (news != null && news.isVisible()) {
                if (SyncUtils.isSynchronized(news) && news.getLabels().contains(label)) {
                  SyncItem syncItem = SyncItem.toSyncItem(news);
                  syncItem.addLabel(newName);
                  syncItem.removeLabel(oldName);
                  syncItems.add(syncItem);
                }
              }

              /* Index Issue */
              else
                CoreUtils.reportIndexIssue();
            }
          }

          /* Tell SyncService to synchronize */
          if (!syncItems.isEmpty())
            Controller.getDefault().getSyncService().synchronize(syncItems);
        } finally {
          monitor.done();
        }
      }
    };

    /* Progress Dialog */
    ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell()) {
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

  private void deleteInBackground(final List<ILabel> labelsToDelete) {
    IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.ManageLabelsPreferencePage_WAIT_DELETE, IProgressMonitor.UNKNOWN);
        try {

          /* Can have an impact on news, thereby force quick update */
          Controller.getDefault().getSavedSearchService().forceQuickUpdate();

          /* Remove Labels from News in batched mode to reduce load */
          List<SearchHit<NewsReference>> labeledNews = findLabeledNews(labelsToDelete);
          if (!labeledNews.isEmpty()) {
            monitor.subTask(NLS.bind(Messages.ManageLabelsPreferencePage_UPDATE_NEWS_REMOVE_LABELS, labeledNews.size()));

            /* Chunkify */
            List<List<SearchHit<NewsReference>>> chunks = CoreUtils.toChunks(labeledNews, LABELS_CHUNK_SIZE);
            for (List<SearchHit<NewsReference>> chunk : chunks) {
              List<INews> newsToSave = new ArrayList<INews>(chunk.size());

              /* For each item in chunk */
              for (SearchHit<NewsReference> hit : chunk) {
                boolean needsSave = false;
                INews item = hit.getResult().resolve();

                /* Item Exists */
                if (item != null && item.isVisible()) {
                  for (ILabel labelToDelete : labelsToDelete) {
                    if (item.removeLabel(labelToDelete))
                      needsSave = true;
                  }

                  if (needsSave)
                    newsToSave.add(item);
                }

                /* Index Issue */
                else
                  CoreUtils.reportIndexIssue();
              }

              /* Save */
              if (!newsToSave.isEmpty())
                DynamicDAO.saveAll(newsToSave);
            }
          }

          /* Delete Labels from DB */
          DynamicDAO.deleteAll(labelsToDelete);

          /* Update UI */
          JobRunner.runInUIThread(fViewer.getControl(), new Runnable() {
            public void run() {
              fViewer.refresh();
              fixOrderAfterDelete();
              fViewer.getTree().setFocus();
            }
          });
        } finally {
          monitor.done();
        }
      }
    };

    /* Progress Dialog */
    ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell()) {
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

  private List<SearchHit<NewsReference>> findLabeledNews(List<ILabel> selectedLabels) {
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(selectedLabels.size());
    ISearchField labelField = Owl.getModelFactory().createSearchField(INews.LABEL, INews.class.getName());
    for (ILabel label : selectedLabels) {
      ISearchCondition condition = Owl.getModelFactory().createSearchCondition(labelField, SearchSpecifier.IS, label.getName());
      conditions.add(condition);
    }

    return Owl.getPersistenceService().getModelSearch().searchNews(conditions, false);
  }

  /* Ensure that after Delete, the orders are in sync again */
  private void fixOrderAfterDelete() {
    List<ILabel> labelsToSave = new ArrayList<ILabel>();

    TreeItem[] items = fViewer.getTree().getItems();
    for (int i = 0; i < items.length; i++) {
      TreeItem item = items[i];
      ILabel label = (ILabel) item.getData();
      label.setOrder(i);

      labelsToSave.add(label);
    }

    DynamicDAO.saveAll(labelsToSave);
  }

  private void createViewer(Composite container) {
    fViewer = new TreeViewer(container, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
    fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    ((GridData) fViewer.getTree().getLayoutData()).heightHint = 190;
    fViewer.getTree().setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
    fViewer.getTree().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    /* Content Provider */
    fViewer.setContentProvider(new ITreeContentProvider() {
      public Object[] getElements(Object inputElement) {
        return CoreUtils.loadSortedLabels().toArray();
      }

      public Object[] getChildren(Object parentElement) {
        return null;
      }

      public Object getParent(Object element) {
        return null;
      }

      public boolean hasChildren(Object element) {
        return false;
      }

      public void dispose() {}

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    });

    /* Label Provider */
    final RGB listBackground = fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
    final RGB listSelectionBackground = fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
    fViewer.setLabelProvider(new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        ILabel label = (ILabel) cell.getElement();

        /* Text */
        cell.setText(label.getName());

        /* Color */
        if (!OwlUI.isHighContrast()) {
          RGB labelRGB = OwlUI.getRGB(label);
          if (!listBackground.equals(labelRGB) && !listSelectionBackground.equals(labelRGB))
            cell.setForeground(OwlUI.getColor(fResources, labelRGB));
          else
            cell.setForeground(null);
        }
      }
    });

    /* Set dummy Input */
    fViewer.setInput(new Object());

    /* Edit on Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        onEdit();
      }
    });
  }

  private Composite createContainer(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
    composite.setFont(parent.getFont());
    return composite;
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    fResources.dispose();
  }
}