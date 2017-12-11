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

package org.rssowl.ui.internal.dialogs.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.ReparentInfo;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.search.LocationControl;
import org.rssowl.ui.internal.search.SearchConditionList;
import org.rssowl.ui.internal.util.FolderChooser;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class SearchMarkPropertyPage implements IEntityPropertyPage {
  private IPropertyDialogSite fSite;
  private Text fNameInput;
  private Button fMatchAllRadio;
  private LocationControl fLocationControl;
  private SearchConditionList fSearchConditionList;
  private Button fMatchAnyRadio;
  private FolderChooser fFolderChooser;
  private boolean fSearchChanged;
  private List<IEntity> fEntities;

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#init(org.rssowl.ui.dialogs.properties.IPropertyDialogSite,
   * java.util.List)
   */
  @Override
  public void init(IPropertyDialogSite site, List<IEntity> entities) {
    fSite = site;
    fEntities = entities;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createContents(Composite parent) {

    /* Create contents for single selection */
    if (fEntities.size() == 1)
      return createContentsSingleSearch(parent);

    /* Create contents for multi selection */
    return createContentsMultiSearch(parent);
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    return null;
  }

  private Control createContentsSingleSearch(Composite parent) {
    ISearchMark mark = (ISearchMark) fEntities.get(0);
    Pair<ISearchCondition, List<ISearchCondition>> conditions = CoreUtils.splitScope(mark.getSearchConditions());

    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 10, 10));

    /* Name */
    Label nameLabel = new Label(container, SWT.None);
    nameLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
    nameLabel.setText(Messages.SearchMarkPropertyPage_NAME);

    Composite nameContainer = new Composite(container, Application.IS_MAC ? SWT.NONE : SWT.BORDER);
    nameContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    nameContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    if (!Application.IS_MAC)
      nameContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    fNameInput = new Text(nameContainer, Application.IS_MAC ? SWT.BORDER : SWT.NONE);
    OwlUI.makeAccessible(fNameInput, nameLabel);
    fNameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fNameInput.setText(mark.getName());

    ToolBar generateTitleBar = new ToolBar(nameContainer, SWT.FLAT);
    OwlUI.makeAccessible(generateTitleBar, Messages.SearchMarkPropertyPage_NAME_FROM_CONDITION);
    if (!Application.IS_MAC)
      generateTitleBar.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    ToolItem generateTitleItem = new ToolItem(generateTitleBar, SWT.PUSH);
    generateTitleItem.setImage(OwlUI.getImage(fSite.getResourceManager(), "icons/etool16/info.gif")); //$NON-NLS-1$
    generateTitleItem.setToolTipText(Messages.SearchMarkPropertyPage_NAME_FROM_CONDITION);
    generateTitleItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onGenerateName();
      }
    });

    /* Location */
    Label locationLabel = new Label(container, SWT.None);
    locationLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
    locationLabel.setText(Messages.SearchMarkPropertyPage_LOCATION);

    fFolderChooser = new FolderChooser(container, mark.getParent(), SWT.BORDER, true);
    fFolderChooser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fFolderChooser.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 5, false));
    fFolderChooser.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    Composite topControlsContainer = new Composite(container, SWT.None);
    topControlsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    topControlsContainer.setLayout(LayoutUtils.createGridLayout(4, 5, 0));
    ((GridLayout) topControlsContainer.getLayout()).marginTop = 10;

    fMatchAllRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAllRadio.setText(Messages.SearchMarkPropertyPage_MATCH_ALL);
    fMatchAllRadio.setSelection(mark.matchAllConditions());

    fMatchAnyRadio = new Button(topControlsContainer, SWT.RADIO);
    fMatchAnyRadio.setText(Messages.SearchMarkPropertyPage_MATCH_ANY);
    fMatchAnyRadio.setSelection(!mark.matchAllConditions());

    /* Separator */
    Label sep = new Label(topControlsContainer, SWT.SEPARATOR | SWT.VERTICAL);
    sep.setLayoutData(new GridData(SWT.DEFAULT, 16));

    /* Scope */
    Composite scopeContainer = new Composite(topControlsContainer, SWT.None);
    scopeContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    scopeContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 5, false));
    ((GridLayout)scopeContainer.getLayout()).marginLeft = 2;

    Label scopeLabel = new Label(scopeContainer, SWT.NONE);
    scopeLabel.setText(Messages.SearchMarkPropertyPage_SEARCH_IN);

    fLocationControl = new LocationControl(scopeContainer, SWT.WRAP) {
      @Override
      protected String getDefaultLabel() {
        return Messages.SearchMarkPropertyPage_ALL_NEWS;
      }
    };
    fLocationControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    ((GridData) fLocationControl.getLayoutData()).widthHint = 100;
    fLocationControl.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));

    if (conditions.getFirst() != null && conditions.getFirst().getValue() instanceof Long[][])
      fLocationControl.select((Long[][]) conditions.getFirst().getValue());

    Composite conditionsContainer = new Composite(container, SWT.BORDER);
    conditionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    conditionsContainer.setLayout(LayoutUtils.createGridLayout(1));
    conditionsContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    conditionsContainer.setBackgroundMode(SWT.INHERIT_FORCE);

    /* Search Conditions List */
    fSearchConditionList = new SearchConditionList(conditionsContainer, SWT.None, conditions.getSecond());
    fSearchConditionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    if (conditions.getSecond().size() <= 3)
      fSearchConditionList.setVisibleItemCount(3);
    else //Workaround for Bug 1544: State Condition not enough width in propertes when scrollbar showing
      fSearchConditionList.setVisibleItemCount(Math.min(7, conditions.getSecond().size()));

    if (CoreUtils.isLocationConflict(mark.getSearchConditions()))
      fSite.setMessage(Messages.SearchMarkPropertyPage_LOCATION_WARNING, IPropertyDialogSite.MessageType.WARNING);

    return container;
  }

  private Control createContentsMultiSearch(Composite parent) {
    boolean separateFromTop = false;

    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 10, 10));

    /* Location */
    IFolder sameParent = getSameParent(fEntities);
    if (sameParent != null) {
      separateFromTop = true;

      Label locationLabel = new Label(container, SWT.None);
      locationLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
      locationLabel.setText(Messages.SearchMarkPropertyPage_LOCATION);

      fFolderChooser = new FolderChooser(container, sameParent, null, SWT.BORDER, true);
      fFolderChooser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      fFolderChooser.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 5, false));
      fFolderChooser.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    }

    /* Other Settings */
    Composite otherSettingsContainer = new Composite(container, SWT.NONE);
    otherSettingsContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    otherSettingsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1));

    if (separateFromTop)
      ((GridLayout) otherSettingsContainer.getLayout()).marginTop = 15;

    /* Name */
    Label nameLabel = new Label(otherSettingsContainer, SWT.None);
    nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
    nameLabel.setText(NLS.bind(Messages.SearchMarkPropertyPage_ADD_TO_ALL, fEntities.size()));

    Composite conditionsContainer = new Composite(otherSettingsContainer, SWT.BORDER);
    conditionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    conditionsContainer.setLayout(LayoutUtils.createGridLayout(1));
    conditionsContainer.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    conditionsContainer.setBackgroundMode(SWT.INHERIT_FORCE);

    /* Search Conditions List */
    fSearchConditionList = new SearchConditionList(conditionsContainer, SWT.None, getDefaultConditions());
    fSearchConditionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fSearchConditionList.setVisibleItemCount(3);

    return container;
  }

  private IFolder getSameParent(List<IEntity> entities) {
    IFolder parent = null;

    for (IEntity entity : entities) {
      if (!(entity instanceof IFolderChild))
        return null;

      IFolderChild folderChild = (IFolderChild) entity;
      IFolder folder = folderChild.getParent();
      if (parent == null)
        parent = folder;
      else if (parent != folder)
        return null;
    }

    return parent;
  }

  private List<ISearchCondition> getDefaultConditions() {
    List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(1);
    IModelFactory factory = Owl.getModelFactory();

    ISearchField field = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    ISearchCondition condition = factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, ""); //$NON-NLS-1$

    conditions.add(condition);

    return conditions;
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

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#setFocus()
   */
  @Override
  public void setFocus() {
    if (fNameInput != null) {
      fNameInput.setFocus();
      fNameInput.selectAll();
    }
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#performOk(java.util.Set)
   */
  @Override
  public boolean performOk(Set<IEntity> entitiesToSave) {

    /* Perform OK for single selection */
    if (fEntities.size() == 1)
      return performOkSingleSearch(entitiesToSave);

    /* Perform OK for multi selection */
    return performOkMultiSearch(entitiesToSave);
  }

  private boolean performOkSingleSearch(Set<IEntity> entitiesToSave) {
    ISearchMark mark = (ISearchMark) fEntities.get(0);

    /* Require a Name */
    if (fNameInput.getText().length() == 0) {
      fSite.select(this);
      fNameInput.setFocus();
      fSite.setMessage(Messages.SearchMarkPropertyPage_SEARCH_NAME, IPropertyDialogSite.MessageType.ERROR);

      return false;
    }

    /* Require a Condition */
    if (fSearchConditionList.isEmpty()) {
      fSite.select(this);
      fNameInput.setFocus();
      fSite.setMessage(Messages.SearchMarkPropertyPage_DEFINE_SEARCH, IPropertyDialogSite.MessageType.ERROR);

      return false;
    }

    /* Check for changed Name */
    if (!mark.getName().equals(fNameInput.getText())) {
      mark.setName(fNameInput.getText());
      entitiesToSave.add(mark);
    }

    /* Update match-all-condition */
    if (mark.matchAllConditions() != fMatchAllRadio.getSelection()) {
      mark.setMatchAllConditions(fMatchAllRadio.getSelection());
      entitiesToSave.add(mark);
      fSearchChanged = true;
    }

    /* Update Conditions (TODO Could be optimized to not replace all conditions) */
    if (fSearchConditionList.isModified() || fLocationControl.isModified()) {
      entitiesToSave.add(mark);
      fSearchChanged = true;

      /* Remove Old Conditions */
      List<ISearchCondition> oldConditions = mark.getSearchConditions();
      for (ISearchCondition oldCondition : oldConditions) {
        mark.removeSearchCondition(oldCondition);
      }

      /* Delete from DB */
      DynamicDAO.deleteAll(oldConditions);

      /* Add New Conditions */
      fSearchConditionList.createConditions(mark);
      ISearchCondition locationCondition = fLocationControl.toScopeCondition();
      if (locationCondition != null)
        mark.addSearchCondition(locationCondition);
    }

    /* Re-Run search if conditions changed */
    if (fSearchChanged)
      Controller.getDefault().getSavedSearchService().updateSavedSearches(Collections.singleton(mark), true);

    return true;
  }

  private boolean performOkMultiSearch(Set<IEntity> entitiesToSave) {
    Set<ISearchMark> searchesToUpdate = new HashSet<ISearchMark>(fEntities.size());

    for (IEntity entity : fEntities) {
      ISearchMark mark = (ISearchMark) entity;
      List<ISearchCondition> conditions = fSearchConditionList.createConditions(mark);

      if (!conditions.isEmpty()) {
        entitiesToSave.add(entity);
        searchesToUpdate.add(mark);
      }
    }

    /* Force Update if changed */
    if (!searchesToUpdate.isEmpty())
      Controller.getDefault().getSavedSearchService().updateSavedSearches(searchesToUpdate, true);

    return true;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#finish()
   */
  @Override
  public void finish() {

    /* Reparent if necessary */
    for (IEntity entity : fEntities) {
      ISearchMark mark = (ISearchMark) entity;
      if (mark.getParent() != fFolderChooser.getFolder()) {
        ReparentInfo<IFolderChild, IFolder> reparent = new ReparentInfo<IFolderChild, IFolder>(mark, fFolderChooser.getFolder(), null, null);
        CoreUtils.reparentWithProperties(Collections.singletonList(reparent));
      }
    }
  }
}