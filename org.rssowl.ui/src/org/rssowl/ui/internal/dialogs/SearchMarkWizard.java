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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
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
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.search.LocationControl;
import org.rssowl.ui.internal.search.SearchConditionList;
import org.rssowl.ui.internal.util.FolderChooser;
import org.rssowl.ui.internal.util.FolderChooser.ExpandStrategy;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The {@link SearchMarkWizard} is only used in the Eclipse Integration to
 * create new Folders.
 *
 * @author bpasero
 */
public class SearchMarkWizard extends Wizard implements INewWizard {
  private NewSearchMarkWizardPage fPage;
  private IFolderChild fPosition;
  private IFolder fFolder;
  private ResourceManager fResources = new LocalResourceManager(JFaceResources.getResources());

  /* Page for Wizard */
  private class NewSearchMarkWizardPage extends WizardPage {
    private Text fNameInput;
    private FolderChooser fFolderChooser;
    private Button fMatchAllRadio;
    private Button fMatchAnyRadio;
    private SearchConditionList fSearchConditionList;
    private LocationControl fLocationControl;

    NewSearchMarkWizardPage(String pageName) {
      super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/search.gif")); //$NON-NLS-1$
      setMessage(Messages.SearchMarkWizard_SEARCH_WIZ_TITLE);
    }

    public void createControl(Composite parent) {
      Composite control = new Composite(parent, SWT.NONE);
      control.setLayout(new GridLayout(2, false));

      Label nameLabel = new Label(control, SWT.NONE);
      nameLabel.setText(Messages.SearchMarkWizard_NAME);

      Composite nameContainer = new Composite(control, SWT.BORDER);
      nameContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      nameContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
      nameContainer.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

      fNameInput = new Text(nameContainer, SWT.SINGLE);
      OwlUI.makeAccessible(fNameInput, nameLabel);
      fNameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      fNameInput.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          setErrorMessage(null);
        }
      });

      ToolBar generateTitleBar = new ToolBar(nameContainer, SWT.FLAT);
      OwlUI.makeAccessible(generateTitleBar, Messages.SearchMarkDialog_CREATE_NAME_FROM_CONDITIONS);
      generateTitleBar.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

      ToolItem generateTitleItem = new ToolItem(generateTitleBar, SWT.PUSH);
      generateTitleItem.setImage(OwlUI.getImage(fResources, "icons/etool16/info.gif")); //$NON-NLS-1$
      generateTitleItem.setToolTipText(Messages.SearchMarkDialog_CREATE_NAME_FROM_CONDITIONS);
      generateTitleItem.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onGenerateName();
        }
      });

      Label folderLabel = new Label(control, SWT.NONE);
      folderLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
      folderLabel.setText(Messages.SearchMarkWizard_LOCATION);

      /* Folder Chooser */
      fFolderChooser = new FolderChooser(control, fFolder, SWT.BORDER, true);
      fFolderChooser.setExpandStrategy(ExpandStrategy.PACK);
      fFolderChooser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      fFolderChooser.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 5, false));
      fFolderChooser.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_WHITE));

      Composite topControlsContainer = new Composite(control, SWT.None);
      topControlsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
      topControlsContainer.setLayout(LayoutUtils.createGridLayout(4, 5, 0));
      ((GridLayout) topControlsContainer.getLayout()).marginTop = 10;

      fMatchAllRadio = new Button(topControlsContainer, SWT.RADIO);
      fMatchAllRadio.setText(Messages.SearchMarkDialog_MATCH_ALL_CONDITIONS);
      fMatchAllRadio.setSelection(true);

      fMatchAnyRadio = new Button(topControlsContainer, SWT.RADIO);
      fMatchAnyRadio.setText(Messages.SearchMarkDialog_MATCH_ANY_CONDITION);

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
          return Messages.SearchMarkWizard_ALL_NEWS;
        }
      };
      fLocationControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      ((GridData) fLocationControl.getLayoutData()).widthHint = 100;
      fLocationControl.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));

      Composite conditionsContainer = new Composite(control, SWT.BORDER);
      conditionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
      conditionsContainer.setLayout(LayoutUtils.createGridLayout(2));
      conditionsContainer.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      conditionsContainer.setBackgroundMode(SWT.INHERIT_FORCE);

      /* Search Conditions List */
      fSearchConditionList = new SearchConditionList(conditionsContainer, SWT.None, getDefaultConditions());
      fSearchConditionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
      fSearchConditionList.setVisibleItemCount(3);

      setControl(control);
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
      ISearchCondition condition = factory.createSearchCondition(field, SearchSpecifier.CONTAINS, ""); //$NON-NLS-1$

      conditions.add(condition);

      return conditions;
    }

    String getSearchMarkName() {
      return fNameInput.getText();
    }

    IFolder getFolder() {
      return fFolderChooser.getFolder();
    }

    ISearchCondition getScopeCondition() {
      return fLocationControl.toScopeCondition();
    }
  }

  /** Leave for Reflection */
  public SearchMarkWizard() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    Pair<IFolder, IFolderChild> pair = ModelUtils.getLocationAndPosition(selection);
    fFolder = pair.getFirst();
    fPosition = pair.getSecond();
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    fPage = new NewSearchMarkWizardPage(Messages.SearchMarkWizard_NEW_SEARCH);
    setWindowTitle(Messages.SearchMarkWizard_SAVED_SEARCH);
    setHelpAvailable(false);
    addPage(fPage);
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#dispose()
   */
  @Override
  public void dispose() {
    fResources.dispose();
    super.dispose();
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    String name = fPage.getSearchMarkName();
    IModelFactory factory = Owl.getModelFactory();

    /* Generate Name if necessary */
    if (!StringUtils.isSet(name)) {
      fPage.onGenerateName();
      name = fPage.getSearchMarkName();
    }

    /* Make sure Conditions are provided */
    if (fPage.fSearchConditionList.isEmpty()) {
      fPage.setErrorMessage(Messages.SearchMarkWizard_SPECIFY_SEARCH);
      return false;
    }

    IFolder folder = fPage.getFolder();

    ISearchMark searchMark = factory.createSearchMark(null, folder, name, fPosition, fPosition != null ? true : null);
    searchMark.setMatchAllConditions(fPage.fMatchAllRadio.getSelection());

    fPage.fSearchConditionList.createConditions(searchMark);
    ISearchCondition locationCondition = fPage.getScopeCondition();
    if (locationCondition != null)
      searchMark.addSearchCondition(locationCondition);

    /* Copy all Properties from Parent or as Specified into this Mark */
    Map<String, Serializable> properties = folder.getProperties();
    for (Map.Entry<String, Serializable> property : properties.entrySet())
      searchMark.setProperty(property.getKey(), property.getValue());

    DynamicDAO.save(folder);

    /* Update the Search */
    Controller.getDefault().getSavedSearchService().updateSavedSearches(Collections.singleton(searchMark));

    return true;
  }
}