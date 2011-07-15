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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.newsaction.MoveNewsAction;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.ContextMenuCreator;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.filter.NewsActionDescriptor;
import org.rssowl.ui.internal.filter.NewsActionList;
import org.rssowl.ui.internal.filter.NewsActionPresentationManager;
import org.rssowl.ui.internal.search.LocationControl;
import org.rssowl.ui.internal.search.SearchConditionList;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A dialog to add and update a news filter with search conditions and actions.
 *
 * @author bpasero
 */
public class NewsFilterDialog extends TitleAreaDialog {

  /* Section for Dialogs Settings */
  private static final String SETTINGS_SECTION = "org.rssowl.ui.internal.dialogs.NewsFilterDialog"; //$NON-NLS-1$

  /* Action Searches */
  private static final String SEARCHES_ACTION = "org.rssowl.ui.internal.dialogs.filter.SearchesAction"; //$NON-NLS-1$

  /* Action Filters */
  private static final String FILTERS_ACTION = "org.rssowl.ui.internal.dialogs.filter.FiltersAction"; //$NON-NLS-1$

  private LocalResourceManager fResources;
  private final ISearchFilter fEditedFilter;
  private final List<Integer> fExcludedConditions = getExcludedConditions();
  private LocationControl fLocationControl;
  private SearchConditionList fSearchConditionList;
  private NewsActionPresentationManager fNewsActionPresentationManager = NewsActionPresentationManager.getInstance();
  private Button fMatchAllRadio;
  private Button fMatchAnyRadio;
  private Button fMatchAllNewsRadio;
  private NewsActionList fFilterActionList;
  private Text fNameInput;
  private int fFilterPosition;
  private ISearch fPresetSearch;
  private boolean fPresetMatchAll;
  private List<IFilterAction> fPresetActions;
  private ISearchFilter fAddedFilter;

  /**
   * @param parentShell the Shell to create this Dialog on.
   */
  public NewsFilterDialog(Shell parentShell) {
    this(parentShell, (ISearchFilter) null);
  }

  /**
   * @param parentShell the Shell to create this Dialog on.
   * @param filter the {@link ISearchFilter} to edit or <code>null</code> if
   * none.
   */
  public NewsFilterDialog(Shell parentShell, ISearchFilter filter) {
    super(parentShell);

    fEditedFilter = filter;
    fResources = new LocalResourceManager(JFaceResources.getResources());
  }

  /**
   * @param parentShell the Shell to create this Dialog on.
   * @param presetSearch a search that is preset in the condition area.
   */
  public NewsFilterDialog(Shell parentShell, ISearch presetSearch) {
    this(parentShell, presetSearch, null, false);
  }

  /**
   * @param parentShell the Shell to create this Dialog on.
   * @param presetSearch a search that is preset in the condition area.
   * @param presetActions a list of {@link IFilterAction} that is preset
   * @param matchAll <code>true</code> to all news or <code>false</code>
   * otherwise.
   */
  public NewsFilterDialog(Shell parentShell, ISearch presetSearch, List<IFilterAction> presetActions, boolean matchAll) {
    super(parentShell);

    fPresetSearch = presetSearch;
    fPresetActions = presetActions;
    fPresetMatchAll = matchAll;
    fEditedFilter = null;
    fResources = new LocalResourceManager(JFaceResources.getResources());
  }

  /**
   * @param filterPosition the sort order for the resulting news filter.
   */
  public void setFilterPosition(int filterPosition) {
    fFilterPosition = filterPosition;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {

    /* Generate Name if necessary */
    if (!StringUtils.isSet(fNameInput.getText()))
      onGenerateName();

    /* Ensure that a Search Condition is specified if required */
    if (fSearchConditionList.isEmpty() && !fMatchAllNewsRadio.getSelection()) {
      setErrorMessage(Messages.NewsFilterDialog_ENTER_CONDITION);
      fSearchConditionList.focusInput();
      return;
    }

    /* Ensure that an Action is specified */
    if (fFilterActionList.isEmpty()) {
      setErrorMessage(Messages.NewsFilterDialog_CHOOSE_ACTION);
      fFilterActionList.focusInput();
      return;
    }

    /* Ensure that Actions are not conflicting */
    List<IFilterAction> actions = fFilterActionList.createActions();
    if (isConflicting(actions))
      return;

    /* Create new Filter and save */
    if (fEditedFilter == null) {
      fAddedFilter = createFilter(actions);
      DynamicDAO.save(fAddedFilter);
    }

    /* Update existing Filter */
    else {
      updateFilter(actions);
      DynamicDAO.save(fEditedFilter);
    }

    super.okPressed();
  }

  /**
   * @return the {@link ISearchFilter} that was added or updated.
   */
  public ISearchFilter getFilter() {
    return fEditedFilter != null ? fEditedFilter : fAddedFilter;
  }

  private ISearchFilter createFilter(List<IFilterAction> actions) {
    IModelFactory factory = Owl.getModelFactory();
    ISearch search = createSearch();

    /* Create Actions */
    ISearchFilter filter = factory.createSearchFilter(null, search, fNameInput.getText());
    filter.setEnabled(true);
    filter.setMatchAllNews(fMatchAllNewsRadio.getSelection());
    filter.setOrder(fFilterPosition);
    for (IFilterAction action : actions) {
      filter.addAction(action);
    }

    return filter;
  }

  private ISearch createSearch() {
    IModelFactory factory = Owl.getModelFactory();
    ISearch search = null;
    ISearchCondition locationCondition = fLocationControl.toScopeCondition();

    /* Only use Location Condition */
    if (locationCondition != null && fMatchAllNewsRadio.getSelection()) {
      search = factory.createSearch(null);
      search.addSearchCondition(locationCondition);
    }

    /* Build Conditions from Location and List */
    else if (!fMatchAllNewsRadio.getSelection()) {
      List<ISearchCondition> conditions = fSearchConditionList.createConditions();
      if (locationCondition != null)
        conditions.add(locationCondition);
      search = factory.createSearch(null);
      search.setMatchAllConditions(fMatchAllRadio.getSelection());
      for (ISearchCondition condition : conditions) {
        search.addSearchCondition(condition);
      }
    }

    return search;
  }

  private void updateFilter(List<IFilterAction> actions) {

    /* Name */
    fEditedFilter.setName(fNameInput.getText());

    /* Actions */
    if (fFilterActionList.isModified()) {

      /* Remove Old Actions */
      List<IFilterAction> oldActions = fEditedFilter.getActions();
      for (IFilterAction oldAction : oldActions) {
        fEditedFilter.removeAction(oldAction);
      }

      /* Add New Actions */
      for (IFilterAction action : actions) {
        fEditedFilter.addAction(action);
      }
    }

    /* Update Conditioner */
    fEditedFilter.setMatchAllNews(fMatchAllNewsRadio.getSelection());

    /* Update Search */
    ISearch oldSearch = fEditedFilter.getSearch();
    fEditedFilter.setSearch(createSearch());
    if (oldSearch != null)
      DynamicDAO.delete(oldSearch);
  }

  private boolean isConflicting(List<IFilterAction> actions) {
    for (IFilterAction action : actions) {
      NewsActionDescriptor newsAction = fNewsActionPresentationManager.getNewsActionDescriptor(action.getActionId());
      for (IFilterAction otherAction : actions) {
        if (action == otherAction)
          continue;

        NewsActionDescriptor otherNewsAction = fNewsActionPresentationManager.getNewsActionDescriptor(otherAction.getActionId());
        if (otherNewsAction.getNewsAction().conflictsWith(newsAction.getNewsAction())) {
          StringBuilder str = new StringBuilder();
          str.append(NLS.bind(Messages.NewsFilterDialog_REMOVE_ACTION_N, otherNewsAction.getName(), newsAction.getName()));

          setErrorMessage(str.toString());
          return true;
        }
      }
    }

    return false;
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#close()
   */
  @Override
  public boolean close() {
    boolean res = super.close();
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
    setTitle(Messages.NewsFilterDialog_NEWS_FILTER);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, "icons/wizban/filter_wiz.png")); //$NON-NLS-1$

    /* Title Message */
    setMessage(Messages.NewsFilterDialog_DEFINE_SEARCH);

    /* Name Input Filed */
    Composite container = new Composite(parent, SWT.None);
    container.setLayout(LayoutUtils.createGridLayout(2, 10, 5, 0, 5, false));
    container.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    Label nameLabel = new Label(container, SWT.NONE);
    nameLabel.setText(Messages.NewsFilterDialog_NAME);

    Composite nameContainer = new Composite(container, SWT.BORDER);
    nameContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    nameContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    nameContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    fNameInput = new Text(nameContainer, SWT.SINGLE);
    OwlUI.makeAccessible(fNameInput, nameLabel);
    fNameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    if (fEditedFilter != null) {
      fNameInput.setText(fEditedFilter.getName());
      fNameInput.selectAll();
    }

    fNameInput.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setErrorMessage(null);
      }
    });

    GC gc = new GC(fNameInput);
    gc.setFont(JFaceResources.getDialogFont());
    FontMetrics fontMetrics = gc.getFontMetrics();
    int entryFieldWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.ENTRY_FIELD_WIDTH);
    gc.dispose();

    ((GridData) fNameInput.getLayoutData()).widthHint = entryFieldWidth; //Required to avoid large spanning dialog for long Links

    ToolBar generateTitleBar = new ToolBar(nameContainer, SWT.FLAT);
    OwlUI.makeAccessible(generateTitleBar, Messages.NewsFilterDialog_CREATE_NAME_FROM_CONDITIONS);
    generateTitleBar.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    ToolItem generateTitleItem = new ToolItem(generateTitleBar, SWT.PUSH);
    generateTitleItem.setImage(OwlUI.getImage(fResources, "icons/etool16/info.gif")); //$NON-NLS-1$
    generateTitleItem.setToolTipText(Messages.NewsFilterDialog_CREATE_NAME_FROM_CONDITIONS);
    generateTitleItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onGenerateName();
      }
    });

    /* Sashform dividing search definition from actions */
    SashForm sashForm = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
    sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Top Sash */
    Composite topSash = new Composite(sashForm, SWT.NONE);
    topSash.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 0, false));
    createConditionControls(topSash);

    /* Bottom Sash */
    Composite bottomSash = new Composite(sashForm, SWT.NONE);
    bottomSash.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));

    /* Label in between */
    Composite labelContainer = new Composite(bottomSash, SWT.NONE);
    labelContainer.setLayout(LayoutUtils.createGridLayout(1, 10, 3, 0, 0, false));
    ((GridLayout) labelContainer.getLayout()).marginBottom = 2;
    labelContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    Label explanationLabel = new Label(labelContainer, SWT.NONE);
    explanationLabel.setText(Messages.NewsFilterDialog_PERFORM_ACTIONS);

    /* Action Controls */
    createActionControls(bottomSash);

    /* Separator */
    new Label(bottomSash, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));

    /* Set weights to even */
    sashForm.setWeights(new int[] { 50, 50 });

    applyDialogFont(parent);

    return parent;
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {
    GridLayout layout = new GridLayout(1, false);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

    Composite buttonBar = new Composite(parent, SWT.NONE);
    buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    buttonBar.setLayout(layout);

    /* Preview Link */
    Link previewLink = new Link(buttonBar, SWT.NONE);
    previewLink.setText(Messages.NewsFilterDialog_PREVIEW_SEARCH);
    applyDialogFont(previewLink);
    previewLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    previewLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onPreview();
      }
    });

    /* Create Buttons */
    createButtonsForButtonBar(buttonBar);

    return buttonBar;
  }

  private void onPreview() {
    final List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

    /* Create Condition from Scope */
    if (fMatchAllNewsRadio.getSelection()) {
      ISearchCondition locationCondition = fLocationControl.toScopeCondition();
      if (locationCondition != null) {
        locationCondition.setSpecifier(SearchSpecifier.IS);
        conditions.add(locationCondition);
      }
    }

    /* Create Conditions from List */
    else {
      conditions.addAll(fSearchConditionList.createConditions());
      if (!conditions.isEmpty()) {
        ISearchCondition locationCondition = fLocationControl.toScopeCondition();
        if (locationCondition != null)
          conditions.add(locationCondition);
      }
    }

    /* Show if conditions are present */
    if (!conditions.isEmpty()) {
      JobRunner.runInUIThread(getShell(), new Runnable() {
        public void run() {
          SearchNewsDialog dialog = new SearchNewsDialog(getShell(), conditions, fMatchAllRadio.getSelection(), true);
          dialog.setBlockOnOpen(false);
          dialog.open();
        }
      });
    }
  }

  void onGenerateName() {
    String name;
    ISearchCondition locationCondition = fLocationControl.toScopeCondition();
    if (fMatchAllNewsRadio.getSelection() && locationCondition == null) {
      name = Messages.NewsFilterDialog_ALL_NEWS;
    } else {
      List<ISearchCondition> conditions = fSearchConditionList.createConditions();
      if (locationCondition != null)
        conditions.add(locationCondition);
      name = CoreUtils.getName(conditions, fMatchAllRadio.getSelection());
    }

    if (name.length() > 0) {
      fNameInput.setText(name);
      fNameInput.selectAll();
    }
  }

  private void createConditionControls(Composite container) {
    Composite topControlsContainer = new Composite(container, SWT.None);
    topControlsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    topControlsContainer.setLayout(LayoutUtils.createGridLayout(6, 10, 3));

    boolean matchAllNews = (fEditedFilter != null) ? fEditedFilter.matchAllNews() : fPresetMatchAll;
    boolean matchAllConditions = !matchAllNews && (fEditedFilter != null) ? fEditedFilter.getSearch().matchAllConditions() : true;

    if (fPresetSearch != null)
      matchAllConditions = fPresetSearch.matchAllConditions();

    /* Radio to select Condition Matching */
    fMatchAllRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAllRadio.setText(Messages.NewsFilterDialog_MATCH_ALL_CONDITIONS);
    fMatchAllRadio.setSelection(matchAllConditions && !matchAllNews);

    fMatchAnyRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAnyRadio.setText(Messages.NewsFilterDialog_MATCH_ANY_CONDITION);
    fMatchAnyRadio.setSelection(!matchAllConditions && !matchAllNews);

    fMatchAllNewsRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAllNewsRadio.setText(Messages.NewsFilterDialog_MATCH_ALL);
    fMatchAllNewsRadio.setSelection(matchAllNews);
    fMatchAllNewsRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setControlEnabled(fSearchConditionList, !fMatchAllNewsRadio.getSelection());
      }
    });

    /* Separator */
    Label sep = new Label(topControlsContainer, SWT.SEPARATOR | SWT.VERTICAL);
    sep.setLayoutData(new GridData(SWT.DEFAULT, 16));

    /* Scope */
    Composite scopeContainer = new Composite(topControlsContainer, SWT.None);
    scopeContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    scopeContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 5, false));
    ((GridLayout)scopeContainer.getLayout()).marginLeft = 2;

    Label locationLabel = new Label(scopeContainer, SWT.NONE);
    locationLabel.setText(Messages.NewsFilterDialog_IN);

    fLocationControl = new LocationControl(scopeContainer, SWT.WRAP) {
      @Override
      protected String getDefaultLabel() {
        return Messages.NewsFilterDialog_ALL_NEWS;
      }
    };
    fLocationControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    ((GridData) fLocationControl.getLayoutData()).widthHint = 100;
    fLocationControl.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));

    /* ToolBar to add and select existing saved searches */
    final ToolBarManager dialogToolBar = new ToolBarManager(SWT.RIGHT | SWT.FLAT);

    /* Separator */
    dialogToolBar.add(new Separator());

    /* Existing Filters */
    {
      IAction existingFilters = new Action(Messages.NewsFilterDialog_SHOW_NEWS_FILTER, IAction.AS_DROP_DOWN_MENU) {
        @Override
        public void run() {
          OwlUI.positionDropDownMenu(this, dialogToolBar);
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
          return OwlUI.FILTER;
        }

        @Override
        public String getId() {
          return FILTERS_ACTION;
        }
      };

      existingFilters.setMenuCreator(new ContextMenuCreator() {

        @Override
        public Menu createMenu(Control parent) {
          Collection<ISearchFilter> filters = CoreUtils.loadSortedNewsFilters();
          Menu menu = new Menu(parent);

          /* Show Something if Collection is Empty */
          if (filters.isEmpty()) {
            MenuItem item = new MenuItem(menu, SWT.None);
            item.setText(Messages.NewsFilterDialog_NO_FILTER);
            item.setEnabled(false);
          }

          /* Show Existing News Filters */
          for (final ISearchFilter filter : filters) {
            MenuItem item = new MenuItem(menu, SWT.None);
            item.setText(filter.getName());
            item.setImage(OwlUI.getImage(fResources, OwlUI.FILTER));
            item.addSelectionListener(new SelectionAdapter() {

              @Override
              public void widgetSelected(SelectionEvent e) {

                /* Search */
                if (filter.getSearch() != null)
                  showSearch(filter.getSearch());

                /* Match All News */
                if (filter.matchAllNews()) {
                  fMatchAnyRadio.setSelection(false);
                  fMatchAllRadio.setSelection(false);
                  fMatchAllNewsRadio.setSelection(true);
                  setControlEnabled(fSearchConditionList, false);
                }

                /* Actions */
                fFilterActionList.showActions(filter.getActions());
              }
            });
          }

          return menu;
        }
      });

      dialogToolBar.add(existingFilters);
    }

    /* Existing Saved Searches */
    {
      IAction savedSearches = new Action(Messages.NewsFilterDialog_SHOW_SAVED_SEARCH, IAction.AS_DROP_DOWN_MENU) {
        @Override
        public void run() {
          OwlUI.positionDropDownMenu(this, dialogToolBar);
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
          return OwlUI.SEARCHMARK;
        }

        @Override
        public String getId() {
          return SEARCHES_ACTION;
        }
      };

      savedSearches.setMenuCreator(new ContextMenuCreator() {

        @Override
        public Menu createMenu(Control parent) {
          Collection<ISearchMark> searchMarks = CoreUtils.loadSortedSearchMarks();
          Menu menu = new Menu(parent);

          /* Show Something if Collection is Empty */
          if (searchMarks.isEmpty()) {
            MenuItem item = new MenuItem(menu, SWT.None);
            item.setText(Messages.NewsFilterDialog_NO_SAVED_SEARCH);
            item.setEnabled(false);
          }

          /* Show Existing Saved Searches */
          for (final ISearchMark searchMark : searchMarks) {
            if (isSupported(searchMark)) {
              MenuItem item = new MenuItem(menu, SWT.None);
              item.setText(searchMark.getName());
              item.setImage(OwlUI.getImage(fResources, OwlUI.SEARCHMARK));
              item.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                  showSearch(searchMark);
                }
              });
            }
          }

          return menu;
        }
      });

      dialogToolBar.add(savedSearches);
    }

    dialogToolBar.createControl(topControlsContainer);
    dialogToolBar.getControl().setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false));

    /* Container for Conditions */
    final Composite conditionsContainer = new Composite(container, SWT.NONE);
    conditionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    conditionsContainer.setLayout(LayoutUtils.createGridLayout(2, 5, 10));
    conditionsContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    conditionsContainer.setBackgroundMode(SWT.INHERIT_FORCE);
    conditionsContainer.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        Rectangle clArea = conditionsContainer.getClientArea();
        gc.setForeground(conditionsContainer.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        gc.drawLine(clArea.x, clArea.y, clArea.x + clArea.width, clArea.y);
        gc.drawLine(clArea.x, clArea.y + clArea.height - 1, clArea.x + clArea.width, clArea.y + clArea.height - 1);
      }
    });

    /* Search Conditions List */
    fSearchConditionList = new SearchConditionList(conditionsContainer, SWT.None, getDefaultConditions(), fExcludedConditions);
    fSearchConditionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    fSearchConditionList.setVisibleItemCount(3);

    /* Show Initial Conditions if present */
    List<ISearchCondition> initialConditions = null;
    if (fEditedFilter != null && fEditedFilter.getSearch() != null)
      initialConditions = fEditedFilter.getSearch().getSearchConditions();
    else if (fPresetSearch != null)
      initialConditions = fPresetSearch.getSearchConditions();

    if (initialConditions != null) {
      Pair<ISearchCondition, List<ISearchCondition>> conditions = CoreUtils.splitScope(initialConditions);
      Long[][] locationValue = null;
      if (conditions.getFirst() != null && conditions.getFirst().getValue() instanceof Long[][])
        locationValue = (Long[][]) conditions.getFirst().getValue();

      fLocationControl.select(locationValue);

      if (!conditions.getSecond().isEmpty())
        fSearchConditionList.showConditions(conditions.getSecond());

      if (CoreUtils.isLocationConflict(initialConditions))
        setMessage(Messages.NewsFilterDialog_LOCATION_IN_WARNING, IMessageProvider.WARNING);
    }

    /* Update Enable-State of Search Condition List */
    setControlEnabled(fSearchConditionList, !fMatchAllNewsRadio.getSelection());

    /* Generate Name if preset */
    if (fPresetSearch != null)
      onGenerateName();
  }

  /* Load a search into the UI */
  private void showSearch(ISearch search) {

    /* Match Conditions */
    fMatchAllRadio.setSelection(search.matchAllConditions());
    fMatchAnyRadio.setSelection(!search.matchAllConditions());
    fMatchAllNewsRadio.setSelection(false);
    setControlEnabled(fSearchConditionList, true);

    /* Location */
    Pair<ISearchCondition, List<ISearchCondition>> conditions = CoreUtils.splitScope(search.getSearchConditions());
    Long[][] location = null;
    if (conditions.getFirst() != null && conditions.getFirst().getValue() instanceof Long[][])
      location = (Long[][]) conditions.getFirst().getValue();
    fLocationControl.select(location);

    /* Show Conditions */
    fSearchConditionList.showConditions(conditions.getSecond());

    /* Layout */
    fLocationControl.getParent().getParent().getParent().layout(true, true);
  }

  private void setControlEnabled(Control control, boolean enabled) {
    control.setEnabled(enabled);
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      Control[] children = composite.getChildren();
      for (Control child : children) {
        setControlEnabled(child, enabled);
      }
    }
  }

  private boolean isSupported(ISearchMark searchmark) {
    List<ISearchCondition> conditions = searchmark.getSearchConditions();
    for (ISearchCondition condition : conditions) {
      if (fExcludedConditions.contains(condition.getField().getId()))
        return false;
    }

    return true;
  }

  /* We allow all conditions because a filter could also be run on existing news! */
  private List<Integer> getExcludedConditions() {
    return Collections.emptyList();
  }

  private List<ISearchCondition> getDefaultConditions() {
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);
    IModelFactory factory = Owl.getModelFactory();

    ISearchField field = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    ISearchCondition condition = factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, ""); //$NON-NLS-1$

    conditions.add(condition);

    return conditions;
  }

  private void createActionControls(Composite container) {

    /* Container for Actions */
    final Composite actionsContainer = new Composite(container, SWT.NONE);
    actionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    actionsContainer.setLayout(LayoutUtils.createGridLayout(2, 5, 10));
    actionsContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    actionsContainer.setBackgroundMode(SWT.INHERIT_FORCE);
    actionsContainer.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        Rectangle clArea = actionsContainer.getClientArea();
        gc.setForeground(actionsContainer.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        gc.drawLine(clArea.x, clArea.y, clArea.x + clArea.width, clArea.y);
      }
    });

    /* Action List */
    fFilterActionList = new NewsActionList(actionsContainer, SWT.NONE, getDefaultActions());
    fFilterActionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    fFilterActionList.setVisibleItemCount(3);

    /* Show initial Actions if present */
    if (fEditedFilter != null)
      fFilterActionList.showActions(fEditedFilter.getActions());
    else if (fPresetActions != null)
      fFilterActionList.showActions(fPresetActions);
  }

  private List<IFilterAction> getDefaultActions() {
    List<IFilterAction> defaultActions = new ArrayList<IFilterAction>(1);

    IModelFactory factory = Owl.getModelFactory();
    defaultActions.add(factory.createFilterAction(MoveNewsAction.ID));

    return defaultActions;
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    if (fEditedFilter == null)
      shell.setText(Messages.NewsFilterDialog_NEW_FILTER);
    else
      shell.setText(NLS.bind(Messages.NewsFilterDialog_EDIT_NEWS_FILTER_N, fEditedFilter.getName()));
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable() {
    return true;
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