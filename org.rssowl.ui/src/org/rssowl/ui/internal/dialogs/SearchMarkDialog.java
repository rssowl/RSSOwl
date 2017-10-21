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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.search.LocationControl;
import org.rssowl.ui.internal.search.SearchConditionList;
import org.rssowl.ui.internal.util.FolderChooser;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dialog to add saved searches.
 *
 * @author bpasero
 */
public class SearchMarkDialog extends TitleAreaDialog {

  /* Section for Dialogs Settings */
  private static final String SETTINGS_SECTION = "org.rssowl.ui.internal.actions.NewSearchMarkAction"; //$NON-NLS-1$

  private Text fNameInput;
  private SearchConditionList fSearchConditionList;
  private Button fMatchAnyRadio;
  private Button fMatchAllRadio;
  private LocalResourceManager fResources;
  private IDialogSettings fDialogSettings;
  private boolean fFirstTimeOpen;
  private IFolder fParent;
  private IFolderChild fPosition;
  private ISearchCondition fInitialLocation;
  private List<ISearchCondition> fInitialSearchConditions;
  private boolean fInitialMatchAllConditions;
  private FolderChooser fFolderChooser;
  private LocationControl fLocationControl;
  private boolean fShowLocationConflict;
  private ISearchMark fCreatedSearchMark;
  private Map<String, Serializable> fProperties;

  /**
   * @param shell
   * @param parent
   * @param position
   */
  public SearchMarkDialog(Shell shell, IFolder parent, IMark position) {
    this(shell, parent, position, null, true);
  }

  /**
   * @param shell
   * @param parent
   * @param position
   * @param initialConditions
   * @param matchAllConditions
   */
  public SearchMarkDialog(Shell shell, IFolder parent, IFolderChild position, List<ISearchCondition> initialConditions, boolean matchAllConditions) {
    this(shell, parent, position, initialConditions, matchAllConditions, null);
  }

  /**
   * @param shell
   * @param parent
   * @param position
   * @param initialConditions
   * @param matchAllConditions
   * @param properties
   */
  public SearchMarkDialog(Shell shell, IFolder parent, IFolderChild position, List<ISearchCondition> initialConditions, boolean matchAllConditions, Map<String, Serializable> properties) {
    super(shell);
    fParent = parent;
    fPosition = position;
    fInitialMatchAllConditions = matchAllConditions;
    fProperties= properties;
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fDialogSettings = Activator.getDefault().getDialogSettings();
    fFirstTimeOpen = (fDialogSettings.getSection(SETTINGS_SECTION) == null);

    /* Use default Parent if required */
    if (fParent == null)
      fParent = getDefaultParent();

    /* Look for initial conditions and scope */
    if (initialConditions != null) {
      Pair<ISearchCondition, List<ISearchCondition>> conditions = CoreUtils.splitScope(initialConditions);
      fInitialLocation = conditions.getFirst();
      fInitialSearchConditions = conditions.getSecond();
      fShowLocationConflict = CoreUtils.isLocationConflict(initialConditions);
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {

    /* Make sure Conditions are provided */
    if (fSearchConditionList.isEmpty()) {
      setErrorMessage(Messages.SearchMarkDialog_ERROR_MISSING_CONDITIONS);
      return;
    }

    /* Get selected Folder */
    fParent = fFolderChooser.getFolder();

    /* Generate Name if necessary */
    if (!StringUtils.isSet(fNameInput.getText()))
      onGenerateName();

    /* Create New Search */
    fCreatedSearchMark = Owl.getModelFactory().createSearchMark(null, fParent, fNameInput.getText(), fPosition, fPosition != null ? true : null);
    fCreatedSearchMark.setMatchAllConditions(fMatchAllRadio.getSelection());

    /* Create Conditions and save in DB */
    fSearchConditionList.createConditions(fCreatedSearchMark);
    ISearchCondition locationCondition = fLocationControl.toScopeCondition();
    if (locationCondition != null)
      fCreatedSearchMark.addSearchCondition(locationCondition);

    /* Copy all Properties from Parent or as Specified into this Mark */
    Map<String, Serializable> properties = (fProperties != null) ? fProperties : fParent.getProperties();
    for (Map.Entry<String, Serializable> property : properties.entrySet())
      fCreatedSearchMark.setProperty(property.getKey(), property.getValue());

    DynamicDAO.save(fParent);

    /* Update the Search */
    Controller.getDefault().getSavedSearchService().updateSavedSearches(Collections.singleton(fCreatedSearchMark), true);

    super.okPressed();
  }

  /**
   * @return the newly created {@link ISearchMark}.
   */
  public ISearchMark getSearchMark() {
    return fCreatedSearchMark;
  }

  private IFolder getDefaultParent() {
    return OwlUI.getSelectedBookMarkSet();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#close()
   */
  @Override
  public boolean close() {
    boolean res = super.close();
    fResources.dispose();

    return res;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
   */
  @Override
  protected IDialogSettings getDialogBoundsSettings() {
    IDialogSettings section = fDialogSettings.getSection(SETTINGS_SECTION);
    if (section != null)
      return section;

    return fDialogSettings.addNewSection(SETTINGS_SECTION);
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    newShell.setText(Messages.SearchMarkDialog_NEW_SAVED_SEARCH);
    super.configureShell(newShell);
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    return super.getShellStyle() | SWT.RESIZE | SWT.MAX;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Separator */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Title */
    setTitle(Messages.SearchMarkDialog_SAVED_SEARCH);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, "icons/wizban/search.gif")); //$NON-NLS-1$

    /* Title Message */
    setMessage(Messages.SearchMarkDialog_SEARCH_HELP, IMessageProvider.INFORMATION);

    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 5, 5, 5, 5, false));
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Label nameLabel = new Label(container, SWT.NONE);
    nameLabel.setText(Messages.SearchMarkDialog_NAME);

    Composite nameContainer = new Composite(container, SWT.BORDER);
    nameContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    nameContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    nameContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    fNameInput = new Text(nameContainer, SWT.SINGLE);
    OwlUI.makeAccessible(fNameInput, nameLabel);
    fNameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fNameInput.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setErrorMessage(null);
      }
    });

    ToolBar generateTitleBar = new ToolBar(nameContainer, SWT.FLAT);
    OwlUI.makeAccessible(generateTitleBar, Messages.SearchMarkDialog_CREATE_NAME_FROM_CONDITIONS);
    generateTitleBar.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    ToolItem generateTitleItem = new ToolItem(generateTitleBar, SWT.PUSH);
    generateTitleItem.setImage(OwlUI.getImage(fResources, "icons/etool16/info.gif")); //$NON-NLS-1$
    generateTitleItem.setToolTipText(Messages.SearchMarkDialog_CREATE_NAME_FROM_CONDITIONS);
    generateTitleItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onGenerateName();
      }
    });

    Label folderLabel = new Label(container, SWT.NONE);
    folderLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    folderLabel.setText(Messages.SearchMarkDialog_LOCATION);

    /* Folder Chooser */
    fFolderChooser = new FolderChooser(container, fParent, SWT.BORDER, true);
    fFolderChooser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fFolderChooser.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 5, false));
    fFolderChooser.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    Composite topControlsContainer = new Composite(container, SWT.None);
    topControlsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    topControlsContainer.setLayout(LayoutUtils.createGridLayout(4, 5, 0));
    ((GridLayout) topControlsContainer.getLayout()).marginTop = 10;

    fMatchAllRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAllRadio.setText(Messages.SearchMarkDialog_MATCH_ALL_CONDITIONS);
    fMatchAllRadio.setSelection(fInitialMatchAllConditions);

    fMatchAnyRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAnyRadio.setText(Messages.SearchMarkDialog_MATCH_ANY_CONDITION);
    fMatchAnyRadio.setSelection(!fInitialMatchAllConditions);

    /* Separator */
    Label sep = new Label(topControlsContainer, SWT.SEPARATOR | SWT.VERTICAL);
    sep.setLayoutData(new GridData(SWT.DEFAULT, 16));

    /* Scope */
    Composite scopeContainer = new Composite(topControlsContainer, SWT.None);
    scopeContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    scopeContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 5, false));
    ((GridLayout)scopeContainer.getLayout()).marginLeft = 2;

    Label locationLabel = new Label(scopeContainer, SWT.NONE);
    locationLabel.setText(Messages.SearchMarkDialog_SEARCH_IN);

    fLocationControl = new LocationControl(scopeContainer, SWT.WRAP) {
      @Override
      protected String getDefaultLabel() {
        return Messages.SearchMarkDialog_ALL_NEWS;
      }
    };
    fLocationControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    ((GridData) fLocationControl.getLayoutData()).widthHint = 100;
    fLocationControl.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));

    if (fInitialLocation != null && fInitialLocation.getValue() instanceof Long[][])
      fLocationControl.select((Long[][]) fInitialLocation.getValue());

    Composite conditionsContainer = new Composite(container, SWT.BORDER);
    conditionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    conditionsContainer.setLayout(LayoutUtils.createGridLayout(2));
    conditionsContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    conditionsContainer.setBackgroundMode(SWT.INHERIT_FORCE);

    /* Search Conditions List */
    if (fInitialSearchConditions == null)
      fInitialSearchConditions = getDefaultConditions();
    fSearchConditionList = new SearchConditionList(conditionsContainer, SWT.None, fInitialSearchConditions);
    fSearchConditionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    fSearchConditionList.setVisibleItemCount(3);

    /* Prefill Name out of Conditions if provided */
    if (fInitialSearchConditions != null && !fInitialSearchConditions.isEmpty())
      onGenerateName();

    /* Show Warning in case of location conflict */
    if (fShowLocationConflict)
      setMessage(Messages.SearchMarkDialog_LOCATION_WARNING, IMessageProvider.WARNING);

    applyDialogFont(container);

    return container;
  }

  void onGenerateName() {
    List<ISearchCondition> conditions = fSearchConditionList.createConditions();
    ISearchCondition locationCondition = fLocationControl.toScopeCondition();
    if (locationCondition != null)
      conditions.add(locationCondition);

    String name = CoreUtils.getName(conditions, fMatchAllRadio.getSelection());
    if (name.length() > 0) {
      fNameInput.setText(name);
      fNameInput.selectAll();
    }
  }

  private List<ISearchCondition> getDefaultConditions() {
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);
    IModelFactory factory = Owl.getModelFactory();

    ISearchField field = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    ISearchCondition condition = factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, ""); //$NON-NLS-1$
    conditions.add(condition);

    return conditions;
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

    /* Status Label */
    Link previewLink = new Link(buttonBar, SWT.NONE);
    previewLink.setText(Messages.SearchMarkDialog_PREVIEW_RESULTS);
    applyDialogFont(previewLink);
    previewLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    previewLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onPreview();
      }
    });

    /* OK */
    Button okButton = createButton(buttonBar, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    ((GridData) okButton.getLayoutData()).horizontalAlignment = SWT.END;

    /* Cancel */
    createButton(buttonBar, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

    return buttonBar;
  }

  private void onPreview() {
    final List<ISearchCondition> conditions = fSearchConditionList.createConditions();
    if (!conditions.isEmpty()) {
      ISearchCondition locationCondition = fLocationControl.toScopeCondition();
      if (locationCondition != null)
        conditions.add(locationCondition);

      JobRunner.runInUIThread(getShell(), new Runnable() {
        @Override
        public void run() {
          SearchNewsDialog dialog = new SearchNewsDialog(getShell(), conditions, fMatchAllRadio.getSelection(), true);
          dialog.setBlockOnOpen(false);
          dialog.open();
        }
      });
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {}

  /*
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();

    if (fFirstTimeOpen) {
      Point requiredSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

      /* Bug in SWT: The preferred width of the state condition is wrong */
      if (Application.IS_LINUX)
        requiredSize.x = requiredSize.x + 100;
      else if (Application.IS_MAC)
        requiredSize.x = requiredSize.x + 50;

      getShell().setSize(requiredSize);
      LayoutUtils.positionShell(getShell());
    }
  }
}