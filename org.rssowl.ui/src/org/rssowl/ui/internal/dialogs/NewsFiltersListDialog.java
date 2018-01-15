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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.rssowl.core.INewsAction;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ISearchFilterDAO;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.filter.NewsActionDescriptor;
import org.rssowl.ui.internal.filter.NewsActionPresentationManager;
import org.rssowl.ui.internal.util.CColumnLayoutData;
import org.rssowl.ui.internal.util.CColumnLayoutData.Size;
import org.rssowl.ui.internal.util.CTable;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A dialog to manage news filters in RSSOwl. The dialog allows to add, edit and
 * delete filters as well as moving them up or down to define an order of
 * filters to apply.
 *
 * @author bpasero
 */
public class NewsFiltersListDialog extends TitleAreaDialog {

  /* Number of News to process in a forced Filter run at once */
  private static final int FILTER_CHUNK_SIZE = 50;

  /* Keep the visible instance saved */
  private static NewsFiltersListDialog fgVisibleInstance;

  /* Section for Dialogs Settings */
  private static final String SETTINGS_SECTION = "org.rssowl.ui.internal.dialogs.NewsFiltersListDialog"; //$NON-NLS-1$

  private NewsActionPresentationManager fNewsActionPresentationManager = NewsActionPresentationManager.getInstance();
  private LocalResourceManager fResources;
  private CheckboxTableViewer fViewer;
  private Button fEditButton;
  private Button fDeleteButton;
  private Button fMoveDownButton;
  private Button fMoveUpButton;
  private Image fFilterIcon;
  private ISearchFilterDAO fSearchFilterDao;
  private Button fApplySelectedFilter;
  private ISearchFilter fSelectedFilter;

  /**
   * @param parentShell
   */
  public NewsFiltersListDialog(Shell parentShell) {
    super(parentShell);
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fFilterIcon = OwlUI.getImage(fResources, OwlUI.FILTER);
    fSearchFilterDao = DynamicDAO.getDAO(ISearchFilterDAO.class);
  }

  /**
   * @return Returns an instance of <code>NewsFiltersListDialog</code> or
   * <code>NULL</code> in case no instance is currently open.
   */
  public static NewsFiltersListDialog getVisibleInstance() {
    return fgVisibleInstance;
  }

  /*
   * @see org.eclipse.jface.window.Window#open()
   */
  @Override
  public int open() {
    fgVisibleInstance = this;
    return super.open();
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#close()
   */
  @Override
  public boolean close() {
    boolean res = super.close();
    fgVisibleInstance = null;
    fResources.dispose();
    return res;
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Separator */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Title */
    setTitle(Messages.NewsFiltersListDialog_NEWS_FILTERS);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, "icons/wizban/filter_wiz.png")); //$NON-NLS-1$

    /* Composite to hold all components */
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 5, 10));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Composite tableContainer = new Composite(composite, SWT.NONE);
    tableContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    CTable cTable = new CTable(tableContainer, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

    fViewer = new CheckboxTableViewer(cTable.getControl());
    fViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fViewer.getTable().setHeaderVisible(true);
    ((GridData) fViewer.getTable().getLayoutData()).heightHint = fViewer.getTable().getItemHeight() * 15;
    fViewer.getTable().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    TableColumn nameCol = new TableColumn(fViewer.getTable(), SWT.NONE);

    CColumnLayoutData data = new CColumnLayoutData(Size.FILL, 100);
    cTable.manageColumn(nameCol, data, Messages.NewsFiltersListDialog_NAME, null, null, false, false);

    /* ContentProvider returns all filters */
    fViewer.setContentProvider(new IStructuredContentProvider() {
      @Override
      public Object[] getElements(Object inputElement) {
        return fSearchFilterDao.loadAll().toArray();
      }

      @Override
      public void dispose() {}

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    });

    /* Label Provider */
    fViewer.setLabelProvider(new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        ISearchFilter filter = (ISearchFilter) cell.getElement();
        Display display = fViewer.getControl().getDisplay();
        if (filter.isEnabled())
          cell.setText(filter.getName());
        else
          cell.setText(NLS.bind(Messages.NewsFiltersListDialog_FILTER_DISABLED, filter.getName()));
        cell.setImage(fFilterIcon);
        if (!OwlUI.isHighContrast())
          cell.setForeground(filter.isEnabled() ? display.getSystemColor(SWT.COLOR_BLACK) : display.getSystemColor(SWT.COLOR_DARK_GRAY));
      }
    });

    /* Sort */
    fViewer.setComparator(new ViewerComparator() {
      @Override
      public int compare(Viewer viewer, Object e1, Object e2) {
        ISearchFilter filter1 = (ISearchFilter) e1;
        ISearchFilter filter2 = (ISearchFilter) e2;

        return filter1.getOrder() < filter2.getOrder() ? -1 : 1;
      }
    });

    /* Selection */
    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        fEditButton.setEnabled(!selection.isEmpty());
        fDeleteButton.setEnabled(!selection.isEmpty());
        fApplySelectedFilter.setEnabled(!selection.isEmpty() && selection.size() == 1);

        updateMoveEnablement();
      }
    });

    /* Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        onEdit();
      }
    });

    /* Set input (ignored by ContentProvider anyways) */
    fViewer.setInput(this);
    updateCheckedState();

    /* Listen on Check State Changes */
    fViewer.addCheckStateListener(new ICheckStateListener() {
      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        ISearchFilter filter = (ISearchFilter) event.getElement();
        filter.setEnabled(event.getChecked());
        fSearchFilterDao.save(filter);
        fViewer.update(filter, null);
        updateTitle();
      }
    });

    /* Container for the Buttons to Manage Filters */
    Composite buttonContainer = new Composite(composite, SWT.None);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));

    /* Adds a new Filter */
    Button addButton = new Button(buttonContainer, SWT.PUSH);
    addButton.setText(Messages.NewsFiltersListDialog_NEW);
    addButton.setFocus();
    applyDialogFont(addButton);
    setButtonLayoutData(addButton);
    addButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onAdd();
      }
    });

    /* Edits a selected Filter */
    fEditButton = new Button(buttonContainer, SWT.PUSH);
    fEditButton.setText(Messages.NewsFiltersListDialog_EDIT);
    applyDialogFont(fEditButton);
    setButtonLayoutData(fEditButton);
    fEditButton.setEnabled(false);
    fEditButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onEdit();
      }
    });

    /* Deletes the selected Filter */
    fDeleteButton = new Button(buttonContainer, SWT.PUSH);
    fDeleteButton.setText(Messages.NewsFiltersListDialog_DELETE);
    applyDialogFont(fDeleteButton);
    setButtonLayoutData(fDeleteButton);
    fDeleteButton.setEnabled(false);
    fDeleteButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onDelete();
      }
    });

    /* Move Filter Up */
    fMoveUpButton = new Button(buttonContainer, SWT.PUSH);
    fMoveUpButton.setText(Messages.NewsFiltersListDialog_MOVE_UP);
    fMoveUpButton.setEnabled(false);
    applyDialogFont(fMoveUpButton);
    setButtonLayoutData(fMoveUpButton);
    ((GridData) fMoveUpButton.getLayoutData()).verticalIndent = 10;
    fMoveUpButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onMove(true);
      }
    });

    /* Move Filter Down */
    fMoveDownButton = new Button(buttonContainer, SWT.PUSH);
    fMoveDownButton.setText(Messages.NewsFiltersListDialog_MOVE_DOWN);
    fMoveDownButton.setEnabled(false);
    applyDialogFont(fMoveDownButton);
    setButtonLayoutData(fMoveDownButton);
    fMoveDownButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onMove(false);
      }
    });

    Composite buttonBar = new Composite(composite, SWT.NONE);
    buttonBar.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

    /* Button to apply filter on all News */
    fApplySelectedFilter = new Button(buttonBar, SWT.PUSH);
    fApplySelectedFilter.setText(Messages.NewsFiltersListDialog_RUN_SELECTED_FILTER);
    fApplySelectedFilter.setEnabled(false);
    applyDialogFont(fApplySelectedFilter);
    setButtonLayoutData(fApplySelectedFilter);
    ((GridData) fApplySelectedFilter.getLayoutData()).grabExcessHorizontalSpace = false;
    ((GridData) fApplySelectedFilter.getLayoutData()).horizontalAlignment = SWT.BEGINNING;
    fApplySelectedFilter.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onApplySelectedFilter();
      }
    });

    /* Close */
    Button closeButton = new Button(buttonBar, SWT.PUSH);
    closeButton.setText(Messages.NewsFiltersListDialog_CLOSE);
    applyDialogFont(closeButton);
    setButtonLayoutData(closeButton);
    ((GridData) closeButton.getLayoutData()).grabExcessHorizontalSpace = true;
    ((GridData) closeButton.getLayoutData()).horizontalAlignment = SWT.END;
    closeButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        close();
      }
    });

    /* Update Title Message */
    updateTitle();

    /* Set Selection if provided */
    if (fSelectedFilter != null)
      fViewer.setSelection(new StructuredSelection(fSelectedFilter), true);

    applyDialogFont(composite);

    return composite;
  }

  private void onApplySelectedFilter() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    if (!selection.isEmpty()) {
      ISearchFilter filter = (ISearchFilter) selection.getFirstElement();

      /* Retrieve those actions that are forcable to run */
      List<IFilterAction> actions = filter.getActions();
      List<IFilterAction> forcableActions = new ArrayList<IFilterAction>(actions.size());
      for (IFilterAction action : actions) {
        NewsActionDescriptor newsActionDescriptor = fNewsActionPresentationManager.getNewsActionDescriptor(action.getActionId());
        if (newsActionDescriptor != null && newsActionDescriptor.isForcable())
          forcableActions.add(action);
      }

      /* Return early if selected Action is not forcable */
      if (forcableActions.isEmpty()) {
        MessageDialog.openWarning(getShell(), NLS.bind(Messages.NewsFiltersListDialog_RUN_SELECTED_FILTER_N, filter.getName()), NLS.bind(Messages.NewsFiltersListDialog_NO_ACTIONS_TO_RUN, filter.getName()));
        return;
      }

      IModelSearch search = Owl.getPersistenceService().getModelSearch();
      List<SearchHit<NewsReference>> targetNews = null;

      /* Search for all Visible News */
      Set<State> visibleStates = INews.State.getVisible();
      if (filter.getSearch() == null) {
        ISearchField stateField = Owl.getModelFactory().createSearchField(INews.STATE, INews.class.getName());
        ISearchCondition stateCondition = Owl.getModelFactory().createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(State.NEW, State.UNREAD, State.UPDATED, State.READ));
        targetNews = search.searchNews(Collections.singleton(stateCondition), true);
      }

      /* Use Search from Filter */
      else {
        List<SearchHit<NewsReference>> result = search.searchNews(filter.getSearch());
        targetNews = new ArrayList<SearchHit<NewsReference>>(result.size());

        /* Filter out those that are not visible */
        for (SearchHit<NewsReference> resultItem : result) {
          INews.State state = (State) resultItem.getData(INews.STATE);
          if (visibleStates.contains(state))
            targetNews.add(resultItem);
        }
      }

      /* Return early if there is no matching News */
      if (targetNews.isEmpty()) {
        MessageDialog.openWarning(getShell(), NLS.bind(Messages.NewsFiltersListDialog_RUN_SELECTED_FILTER_N, filter.getName()), NLS.bind(Messages.NewsFiltersListDialog_NO_FILTER_MATCH, filter.getName()));
        return;
      }

      /* Ask for Confirmation */
      boolean multipleActions = forcableActions.size() > 1;
      String title = NLS.bind(Messages.NewsFiltersListDialog_RUN_SELECTED_FILTER_N, filter.getName());
      StringBuilder message = new StringBuilder();
      if (multipleActions)
        message.append(NLS.bind(Messages.NewsFiltersListDialog_PERFORM_ACTIONS, targetNews.size())).append("\n"); //$NON-NLS-1$
      else
        message.append(NLS.bind(Messages.NewsFiltersListDialog_PERFORM_ACTION, targetNews.size())).append("\n"); //$NON-NLS-1$

      for (IFilterAction action : forcableActions) {
        NewsActionDescriptor newsActionDescriptor = fNewsActionPresentationManager.getNewsActionDescriptor(action.getActionId());
        String label = newsActionDescriptor.getNewsAction() != null ? newsActionDescriptor.getNewsAction().getLabel(action.getData()) : null;
        if (StringUtils.isSet(label))
          message.append("\n").append(NLS.bind(Messages.NewsFiltersListDialog_FILTER_LIST_ELEMENT, label)); //$NON-NLS-1$
        else
          message.append("\n").append(NLS.bind(Messages.NewsFiltersListDialog_FILTER_LIST_ELEMENT, newsActionDescriptor.getName())); //$NON-NLS-1$
      }

      message.append("\n\n").append(Messages.NewsFiltersListDialog_CONFIRM); //$NON-NLS-1$

      ConfirmDialog dialog = new ConfirmDialog(getShell(), title, Messages.NewsFiltersListDialog_NO_UNDO, message.toString(), IDialogConstants.OK_LABEL, null) {
        @Override
        protected String getTitleImage() {
          return "icons/wizban/filter_wiz.png"; //$NON-NLS-1$
        }

        @Override
        public void setTitle(String newTitle) {
          super.setTitle(Messages.NewsFiltersListDialog_RUN_SELECTED_FILTER_TITLE);
        }
      };

      /* Apply Actions in chunks of N Items to avoid Memory issues */
      if (dialog.open() == IDialogConstants.OK_ID) {
        applyFilter(targetNews, filter);
      }
    }
  }

  private void applyFilter(final List<SearchHit<NewsReference>> news, final ISearchFilter filter) {
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      @Override
      public void run(IProgressMonitor monitor) {
        List<List<SearchHit<NewsReference>>> chunks = CoreUtils.toChunks(news, FILTER_CHUNK_SIZE);
        monitor.beginTask(NLS.bind(Messages.NewsFiltersListDialog_WAIT_FILTER_APPLIED, filter.getName()), chunks.size());

        if (monitor.isCanceled())
          return;

        int counter = 0;
        for (List<SearchHit<NewsReference>> chunk : chunks) {
          if (monitor.isCanceled())
            return;

          monitor.subTask(NLS.bind(Messages.NewsFiltersListDialog_FILTERED_N_OF_M_NEWS, (counter * FILTER_CHUNK_SIZE), news.size()));
          List<INews> newsItemsToFilter = new ArrayList<INews>(FILTER_CHUNK_SIZE);
          for (SearchHit<NewsReference> chunkItem : chunk) {
            INews newsItemToFilter = chunkItem.getResult().resolve();
            if (newsItemToFilter != null && newsItemToFilter.isVisible())
              newsItemsToFilter.add(newsItemToFilter);
            else
              CoreUtils.reportIndexIssue();
          }

          applyFilterOnChunks(newsItemsToFilter, filter);
          monitor.worked(1);
          counter++;
        }

        monitor.done();
      }
    };

    /* Show progress and allow for cancellation */
    ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
    dialog.setBlockOnOpen(false);
    dialog.setCancelable(true);
    dialog.setOpenOnRun(true);
    try {
      dialog.run(true, true, runnable);
    } catch (InvocationTargetException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    } catch (InterruptedException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }
  }

  private void applyFilterOnChunks(final List<INews> news, ISearchFilter filter) {
    Collection<IFilterAction> actions = CoreUtils.getActions(filter); //Need to sort structural actions to end
    final Set<IEntity> entitiesToSave = new HashSet<IEntity>(news.size());
    final Map<INews, INews> replacements = new HashMap<INews, INews>();

    for (final IFilterAction action : actions) {
      NewsActionDescriptor newsActionDescriptor = fNewsActionPresentationManager.getNewsActionDescriptor(action.getActionId());
      if (newsActionDescriptor != null && newsActionDescriptor.isForcable()) {
        final INewsAction newsAction = newsActionDescriptor.getNewsAction();
        if (newsAction != null) {
          SafeRunnable.run(new ISafeRunnable() {
            @Override
            public void handleException(Throwable e) {
              Activator.getDefault().logError(e.getMessage(), e);
            }

            @Override
            public void run() throws Exception {
              List<IEntity> changedEntities = newsAction.run(news, replacements, action.getData());
              entitiesToSave.addAll(changedEntities);
            }
          });
        }
      }
    }

    /* Make sure that changed entities are saved for all actions */
    if (!entitiesToSave.isEmpty())
      DynamicDAO.saveAll(entitiesToSave);
  }

  private void updateTitle() {
    ISearchFilter problematicFilter = null;

    Table table = fViewer.getTable();
    TableItem[] items = table.getItems();
    for (TableItem item : items) {
      ISearchFilter filter = (ISearchFilter) item.getData();
      if (filter.getSearch() == null && filter.isEnabled()) {
        int index = table.indexOf(item);
        if (index < table.getItemCount() - 1) {
          problematicFilter = filter;
          break;
        }
      }
    }

    if (problematicFilter != null)
      setMessage(NLS.bind(Messages.NewsFiltersListDialog_FILTER_MATCHES_ALL_NEWS, problematicFilter.getName()), IMessageProvider.WARNING);
    else
      setMessage(Messages.NewsFiltersListDialog_ENABLED_FILTERS, IMessageProvider.INFORMATION);
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {
    return null;
  }

  private void updateMoveEnablement() {
    boolean enableMoveUp = true;
    boolean enableMoveDown = true;
    int[] selectionIndices = fViewer.getTable().getSelectionIndices();
    if (selectionIndices.length == 1) {
      enableMoveUp = selectionIndices[0] != 0;
      enableMoveDown = selectionIndices[0] != fViewer.getTable().getItemCount() - 1;
    } else {
      enableMoveUp = false;
      enableMoveDown = false;
    }

    fMoveUpButton.setEnabled(enableMoveUp);
    fMoveDownButton.setEnabled(enableMoveDown);
  }

  private void onMove(boolean up) {
    TableItem[] items = fViewer.getTable().getItems();
    List<ISearchFilter> sortedFilters = new ArrayList<ISearchFilter>(items.length);
    for (TableItem item : items) {
      sortedFilters.add((ISearchFilter) item.getData());
    }

    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    ISearchFilter selectedFilter = (ISearchFilter) selection.getFirstElement();
    int selectedFilterOrder = selectedFilter.getOrder();
    ISearchFilter otherFilter = null;
    int index = sortedFilters.indexOf(selectedFilter);

    /* Move Up */
    if (up && index > 0) {
      otherFilter = sortedFilters.get(index - 1);
      selectedFilter.setOrder(otherFilter.getOrder());
      otherFilter.setOrder(selectedFilterOrder);
    }

    /* Move Down */
    else if (!up && index < sortedFilters.size() - 1) {
      otherFilter = sortedFilters.get(index + 1);
      selectedFilter.setOrder(otherFilter.getOrder());
      otherFilter.setOrder(selectedFilterOrder);
    }

    fSearchFilterDao.saveAll(Arrays.asList(new ISearchFilter[] { selectedFilter, otherFilter }));
    fViewer.refresh();
    fViewer.getTable().showSelection();
    updateCheckedState();
    updateMoveEnablement();
    updateTitle();
  }

  private void updateCheckedState() {
    TableItem[] items = fViewer.getTable().getItems();
    for (TableItem item : items) {
      ISearchFilter filter = (ISearchFilter) item.getData();
      fViewer.setChecked(filter, filter.isEnabled());
    }
  }

  private void onAdd() {
    NewsFilterDialog dialog = new NewsFilterDialog(getShell());
    Table table = fViewer.getTable();
    dialog.setFilterPosition(table.getItemCount());
    if (dialog.open() == IDialogConstants.OK_ID) {
      fViewer.refresh();
      updateCheckedState();
      fViewer.setSelection(new StructuredSelection(table.getItem(table.getItemCount() - 1).getData()));
      fViewer.getTable().setFocus();
      updateTitle();
    }
  }

  private void onEdit() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    ISearchFilter filter = (ISearchFilter) selection.getFirstElement();

    NewsFilterDialog dialog = new NewsFilterDialog(getShell(), filter);
    if (dialog.open() == IDialogConstants.OK_ID) {
      fViewer.refresh(true);
      fViewer.getTable().setFocus();
      updateTitle();
    }
  }

  private void onDelete() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();

    List<?> selectedFilters = selection.toList();
    ConfirmDialog dialog = new ConfirmDialog(getShell(), Messages.NewsFiltersListDialog_CONFIRM_DELETE, Messages.NewsFiltersListDialog_NO_UNDO, getMessage(selectedFilters), null);
    if (dialog.open() == IDialogConstants.OK_ID) {
      List<ISearchFilter> filtersToDelete = new ArrayList<ISearchFilter>(selectedFilters.size());
      for (Iterator<?> iterator = selectedFilters.iterator(); iterator.hasNext();) {
        ISearchFilter filter = (ISearchFilter) iterator.next();
        filtersToDelete.add(filter);
      }

      fSearchFilterDao.deleteAll(filtersToDelete);
      fViewer.remove(selection.toArray());
      fixOrderAfterDelete();
      updateTitle();
    }
  }

  /* Ensure that after Delete, the orders are in sync again */
  private void fixOrderAfterDelete() {
    List<ISearchFilter> filtersToSave = new ArrayList<ISearchFilter>();

    TableItem[] items = fViewer.getTable().getItems();
    for (int i = 0; i < items.length; i++) {
      TableItem item = items[i];
      ISearchFilter filter = (ISearchFilter) item.getData();
      filter.setOrder(i);

      filtersToSave.add(filter);
    }

    DynamicDAO.saveAll(filtersToSave);
  }

  private String getMessage(List<?> elements) {
    StringBuilder message = new StringBuilder();

    /* One Element */
    if (elements.size() == 1) {
      ISearchFilter filter = (ISearchFilter) elements.get(0);
      message.append(NLS.bind(Messages.NewsFiltersListDialog_CONFIRM_DELETE_FILTER_N, filter.getName()));
    }

    /* N Elements */
    else {
      message.append(Messages.NewsFiltersListDialog_CONFIRM_DELETE_FILTERS);
    }

    return message.toString();
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.NewsFiltersListDialog_NEWS_FILTERS);
  }

  /**
   * @param filter the {@link ISearchFilter} to select.
   */
  public void setSelection(ISearchFilter filter) {
    fSelectedFilter = filter;
    if (fViewer != null)
      fViewer.setSelection(new StructuredSelection(fSelectedFilter), true);
  }

  /**
   * Refresh the list of displayed Filters.
   */
  public void refresh() {
    if (fViewer != null) {
      fViewer.refresh();
      updateCheckedState();
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable() {
    return true;
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.TITLE | SWT.BORDER | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | getDefaultOrientation();

    return style;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
   */
  @Override
  protected int getDialogBoundsStrategy() {
    return DIALOG_PERSISTSIZE;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
   */
  @Override
  protected IDialogSettings getDialogBoundsSettings() {
    IDialogSettings settings = Activator.getDefault().getDialogSettings();
    IDialogSettings section = settings.getSection(SETTINGS_SECTION);
    if (section != null)
      return section;

    return settings.addNewSection(SETTINGS_SECTION);
  }
}