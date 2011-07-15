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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.editors.feed.NewsColumn;
import org.rssowl.ui.internal.editors.feed.NewsColumnViewModel;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.NewsColumnSelectionControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Columns Properties.
 *
 * @author bpasero
 */
public class ColumnsPropertyPage implements IEntityPropertyPage {
  private List<IEntity> fEntities;
  private NewsColumnSelectionControl fColumnSelectionControl;

  /* Settings */
  private List<IPreferenceScope> fEntityPreferences;
  private int[] fPrefSelectedColumns;
  private int fPrefSortColumn;
  private boolean fPrefAscending;
  private boolean fSettingsChanged;

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#init(org.rssowl.ui.dialogs.properties.IPropertyDialogSite, java.util.List)
   */
  public void init(IPropertyDialogSite site, List<IEntity> entities) {
    Assert.isTrue(!entities.isEmpty());
    fEntities = entities;

    /* Load Entity Preferences */
    fEntityPreferences = new ArrayList<IPreferenceScope>(fEntities.size());
    for (IEntity entity : entities)
      fEntityPreferences.add(Owl.getPreferenceService().getEntityScope(entity));

    /* Load initial Settings */
    loadInitialSettings();
  }

  private void loadInitialSettings() {

    /* Take the first scope as initial values */
    IPreferenceScope firstScope = fEntityPreferences.get(0);
    fPrefSelectedColumns = firstScope.getIntegers(DefaultPreferences.BM_NEWS_COLUMNS);
    fPrefSortColumn = firstScope.getInteger(DefaultPreferences.BM_NEWS_SORT_COLUMN);
    fPrefAscending = firstScope.getBoolean(DefaultPreferences.BM_NEWS_SORT_ASCENDING);

    /* For any other scope not sharing the initial values, use the default */
    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();
    for (int i = 1; i < fEntityPreferences.size(); i++) {
      IPreferenceScope otherScope = fEntityPreferences.get(i);

      if (!Arrays.equals(otherScope.getIntegers(DefaultPreferences.BM_NEWS_COLUMNS), fPrefSelectedColumns))
        fPrefSelectedColumns = defaultScope.getIntegers(DefaultPreferences.BM_NEWS_COLUMNS);

      if (otherScope.getInteger(DefaultPreferences.BM_NEWS_SORT_COLUMN) != fPrefSortColumn)
        fPrefSortColumn = defaultScope.getInteger(DefaultPreferences.BM_NEWS_SORT_COLUMN);

      if (otherScope.getBoolean(DefaultPreferences.BM_NEWS_SORT_ASCENDING) != fPrefAscending)
        fPrefAscending = defaultScope.getBoolean(DefaultPreferences.BM_NEWS_SORT_ASCENDING);
    }
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 10, 10));

    fColumnSelectionControl = new NewsColumnSelectionControl(container, SWT.NONE);
    fColumnSelectionControl.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fColumnSelectionControl.setInput(NewsColumnViewModel.createFrom(fPrefSelectedColumns, fPrefSortColumn, fPrefAscending));
    return container;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#getImage()
   */
  public ImageDescriptor getImage() {
    return null;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#setFocus()
   */
  public void setFocus() {}

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#performOk(java.util.Set)
   */
  public boolean performOk(Set<IEntity> entitiesToSave) {
    fSettingsChanged = false;

    /* Update this Entity */
    for (IPreferenceScope scope : fEntityPreferences) {
      if (updatePreferences(scope)) {
        IEntity entityToSave = fEntities.get(fEntityPreferences.indexOf(scope));
        entitiesToSave.add(entityToSave);
        fSettingsChanged = true;
      }
    }

    /* Update changes in all Childs as well if Folder */
    for (IEntity entity : fEntities) {
      if (fSettingsChanged && entity instanceof IFolder)
        updateChildPreferences((IFolder) entity);
    }

    return true;
  }

  private void updateChildPreferences(IFolder folder) {

    /* Update changes to Child-Marks */
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      IPreferenceScope scope = Owl.getPreferenceService().getEntityScope(mark);
      updatePreferences(scope);
    }

    /* Update changes to Child-Folders */
    List<IFolder> folders = folder.getFolders();
    for (IFolder childFolder : folders) {
      IPreferenceScope scope = Owl.getPreferenceService().getEntityScope(childFolder);
      updatePreferences(scope);

      /* Recursively Proceed */
      updateChildPreferences(childFolder);
    }
  }

  private boolean updatePreferences(IPreferenceScope scope) {
    boolean changed = false;
    NewsColumnViewModel model = fColumnSelectionControl.getModel();

    /* Columns */
    List<NewsColumn> columns = model.getColumns();
    int[] columnsInt = new int[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      columnsInt[i] = columns.get(i).ordinal();
    }

    if (!Arrays.equals(fPrefSelectedColumns, columnsInt))
      changed = true;

    /* Sort Column */
    int sortColumn = model.getSortColumn().ordinal();
    if (fPrefSortColumn != sortColumn)
      changed = true;

    /* Sort Order */
    boolean ascending = model.isAscending();
    if (fPrefAscending != ascending)
      changed = true;

    /* Save if changed */
    if (changed)
      model.saveTo(scope);

    return changed;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#finish()
   */
  public void finish() {

    /* Propagate change to open Editors */
    if (fSettingsChanged)
      EditorUtils.updateColumns();
  }
}