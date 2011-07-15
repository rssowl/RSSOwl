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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reading Properties.
 *
 * @author bpasero
 */
public class ReadingPropertyPage implements IEntityPropertyPage {
  private List<IEntity> fEntities;
  private ArrayList<IPreferenceScope> fEntityPreferences;
  private boolean fPrefMarkReadState;
  private int fPrefMarkReadVal;
  private boolean fPrefMarkReadOnMinimize;
  private boolean fPrefMarkReadOnScrolling;
  private boolean fPrefMarkReadOnTabClose;
  private boolean fPrefMarkReadOnFeedChange;
  private Button fMarkReadStateCheck;
  private Spinner fMarkReadAfterSpinner;
  private Button fMarkReadOnMinimize;
  private Button fMarkReadOnScrolling;
  private Button fMarkReadOnChange;
  private boolean fSettingsChanged;
  private Button fMarkReadOnTabClose;

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

    fPrefMarkReadState = firstScope.getBoolean(DefaultPreferences.MARK_READ_STATE);
    fPrefMarkReadVal = firstScope.getInteger(DefaultPreferences.MARK_READ_IN_MILLIS);
    fPrefMarkReadOnMinimize = firstScope.getBoolean(DefaultPreferences.MARK_READ_ON_MINIMIZE);
    fPrefMarkReadOnScrolling = firstScope.getBoolean(DefaultPreferences.MARK_READ_ON_SCROLLING);
    fPrefMarkReadOnTabClose = firstScope.getBoolean(DefaultPreferences.MARK_READ_ON_TAB_CLOSE);
    fPrefMarkReadOnFeedChange = firstScope.getBoolean(DefaultPreferences.MARK_READ_ON_CHANGE);

    /* For any other scope not sharing the initial values, use the default */
    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();
    for (int i = 1; i < fEntityPreferences.size(); i++) {
      IPreferenceScope otherScope = fEntityPreferences.get(i);

      if (otherScope.getBoolean(DefaultPreferences.MARK_READ_STATE) != fPrefMarkReadState)
        fPrefMarkReadState = defaultScope.getBoolean(DefaultPreferences.MARK_READ_STATE);

      if (otherScope.getInteger(DefaultPreferences.MARK_READ_IN_MILLIS) != fPrefMarkReadVal)
        fPrefMarkReadVal = defaultScope.getInteger(DefaultPreferences.MARK_READ_IN_MILLIS);

      if (otherScope.getBoolean(DefaultPreferences.MARK_READ_ON_MINIMIZE) != fPrefMarkReadOnMinimize)
        fPrefMarkReadOnMinimize = defaultScope.getBoolean(DefaultPreferences.MARK_READ_ON_MINIMIZE);

      if (otherScope.getBoolean(DefaultPreferences.MARK_READ_ON_CHANGE) != fPrefMarkReadOnFeedChange)
        fPrefMarkReadOnFeedChange = defaultScope.getBoolean(DefaultPreferences.MARK_READ_ON_CHANGE);

      if (otherScope.getBoolean(DefaultPreferences.MARK_READ_ON_TAB_CLOSE) != fPrefMarkReadOnTabClose)
        fPrefMarkReadOnTabClose = defaultScope.getBoolean(DefaultPreferences.MARK_READ_ON_TAB_CLOSE);

      if (otherScope.getBoolean(DefaultPreferences.MARK_READ_ON_SCROLLING) != fPrefMarkReadOnScrolling)
        fPrefMarkReadOnScrolling = defaultScope.getBoolean(DefaultPreferences.MARK_READ_ON_SCROLLING);
    }
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(1, 10, 10));

    /* Mark read after millis */
    Composite markReadAfterContainer = new Composite(container, SWT.None);
    markReadAfterContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0));

    /* Mark Read after Millis */
    fMarkReadStateCheck = new Button(markReadAfterContainer, SWT.CHECK);
    fMarkReadStateCheck.setText(Messages.ReadingPropertyPage_MARK_READ_AFTER);
    fMarkReadStateCheck.setSelection(fPrefMarkReadState);
    fMarkReadStateCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fMarkReadAfterSpinner.setEnabled(fMarkReadStateCheck.getSelection());
      }
    });

    fMarkReadAfterSpinner = new Spinner(markReadAfterContainer, SWT.BORDER);
    fMarkReadAfterSpinner.setMinimum(0);
    fMarkReadAfterSpinner.setMaximum(100);
    fMarkReadAfterSpinner.setSelection(fPrefMarkReadVal / 1000);
    fMarkReadAfterSpinner.setEnabled(fMarkReadStateCheck.getSelection());

    Label label = new Label(markReadAfterContainer, SWT.None);
    label.setText(Messages.ReadingPropertyPage_SECONDS);

    /* Mark Read on Scrolling */
    fMarkReadOnScrolling = new Button(container, SWT.CHECK);
    fMarkReadOnScrolling.setText(Messages.ReadingPropertyPage_MARK_READ_ON_SCROLLING);
    fMarkReadOnScrolling.setSelection(fPrefMarkReadOnScrolling);

    /* Mark Read on changing displayed Feed */
    fMarkReadOnChange = new Button(container, SWT.CHECK);
    fMarkReadOnChange.setText(Messages.ReadingPropertyPage_MARK_READ_ON_SWITCH);
    fMarkReadOnChange.setSelection(fPrefMarkReadOnFeedChange);

    /* Mark Read on closing Feed Tab */
    fMarkReadOnTabClose = new Button(container, SWT.CHECK);
    fMarkReadOnTabClose.setText(Messages.ReadingPropertyPage_MARK_READ_ON_CLOSE);
    fMarkReadOnTabClose.setSelection(fPrefMarkReadOnTabClose);

    /* Mark Read on Minimize */
    fMarkReadOnMinimize = new Button(container, SWT.CHECK);
    fMarkReadOnMinimize.setText(Messages.ReadingPropertyPage_MARK_READ_ON_MINIMIZE);
    fMarkReadOnMinimize.setSelection(fPrefMarkReadOnMinimize);

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

    /* Mark Read after Millies */
    boolean bVal = fMarkReadStateCheck.getSelection();
    if (fPrefMarkReadState != bVal) {
      scope.putBoolean(DefaultPreferences.MARK_READ_STATE, bVal);
      changed = true;
    }

    int iVal = fMarkReadAfterSpinner.getSelection();
    if (fPrefMarkReadVal != iVal) {
      scope.putInteger(DefaultPreferences.MARK_READ_IN_MILLIS, iVal * 1000);
      changed = true;
    }

    /* Mark Read on Minimize */
    bVal = fMarkReadOnMinimize.getSelection();
    if (fPrefMarkReadOnMinimize != bVal) {
      scope.putBoolean(DefaultPreferences.MARK_READ_ON_MINIMIZE, bVal);
      changed = true;
    }

    /* Mark Read on Feed Change */
    bVal = fMarkReadOnChange.getSelection();
    if (fPrefMarkReadOnFeedChange != bVal) {
      scope.putBoolean(DefaultPreferences.MARK_READ_ON_CHANGE, bVal);
      changed = true;
    }

    /* Mark Read on Tab Close */
    bVal = fMarkReadOnTabClose.getSelection();
    if (fPrefMarkReadOnTabClose != bVal) {
      scope.putBoolean(DefaultPreferences.MARK_READ_ON_TAB_CLOSE, bVal);
      changed = true;
    }

    /* Mark Read on Scrolling */
    bVal = fMarkReadOnScrolling.getSelection();
    if (fPrefMarkReadOnScrolling != bVal) {
      scope.putBoolean(DefaultPreferences.MARK_READ_ON_SCROLLING, bVal);
      changed = true;
    }

    return changed;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#finish()
   */
  public void finish() {}
}