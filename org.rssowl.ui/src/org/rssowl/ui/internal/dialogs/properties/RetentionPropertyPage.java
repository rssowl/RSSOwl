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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.RetentionStrategy;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Retention Properties.
 *
 * @author bpasero
 */
public class RetentionPropertyPage implements IEntityPropertyPage {
  private IPropertyDialogSite fSite;
  private List<IEntity> fEntities;
  private Spinner fMaxCountSpinner;
  private Spinner fMaxAgeSpinner;
  private Button fDeleteNewsByCountCheck;
  private Button fDeleteNewsByAgeCheck;
  private Button fDeleteReadNewsCheck;
  private Button fNeverDeleteUnreadNewsCheck;
  private Button fNeverDeleteLabeledNewsCheck;
  private boolean fSettingsChanged;

  /* Settings */
  private List<IPreferenceScope> fEntityPreferences;
  private boolean fPrefDeleteNewsByCountState;
  private int fPrefDeleteNewsByCountValue;
  private boolean fPrefDeleteNewsByAgeState;
  private int fPrefDeleteNewsByAgeValue;
  private boolean fPrefDeleteReadNews;
  private boolean fPrefNeverDeleteUnReadNews;
  private boolean fPrefNeverDeleteLabeledNews;

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#init(org.rssowl.ui.dialogs.properties.IPropertyDialogSite,
   * java.util.List)
   */
  public void init(IPropertyDialogSite site, List<IEntity> entities) {
    Assert.isTrue(!entities.isEmpty());
    fSite = site;
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
    fPrefDeleteNewsByCountState = firstScope.getBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE);
    fPrefDeleteNewsByCountValue = firstScope.getInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE);
    fPrefDeleteNewsByAgeState = firstScope.getBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE);
    fPrefDeleteNewsByAgeValue = firstScope.getInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE);
    fPrefDeleteReadNews = firstScope.getBoolean(DefaultPreferences.DEL_READ_NEWS_STATE);
    fPrefNeverDeleteUnReadNews = firstScope.getBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE);
    fPrefNeverDeleteLabeledNews = firstScope.getBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE);

    /* For any other scope not sharing the initial values, use the default */
    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();
    for (int i = 1; i < fEntityPreferences.size(); i++) {
      IPreferenceScope otherScope = fEntityPreferences.get(i);

      if (otherScope.getBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE) != fPrefDeleteNewsByCountState)
        fPrefDeleteNewsByCountState = defaultScope.getBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE);

      if (otherScope.getInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE) != fPrefDeleteNewsByCountValue)
        fPrefDeleteNewsByCountValue = defaultScope.getInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE);

      if (otherScope.getBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE) != fPrefDeleteNewsByAgeState)
        fPrefDeleteNewsByAgeState = defaultScope.getBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE);

      if (otherScope.getInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE) != fPrefDeleteNewsByAgeValue)
        fPrefDeleteNewsByAgeValue = defaultScope.getInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE);

      if (otherScope.getBoolean(DefaultPreferences.DEL_READ_NEWS_STATE) != fPrefDeleteReadNews)
        fPrefDeleteReadNews = defaultScope.getBoolean(DefaultPreferences.DEL_READ_NEWS_STATE);

      if (otherScope.getBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE) != fPrefNeverDeleteUnReadNews)
        fPrefNeverDeleteUnReadNews = defaultScope.getBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE);

      if (otherScope.getBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE) != fPrefNeverDeleteLabeledNews)
        fPrefNeverDeleteLabeledNews = defaultScope.getBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE);
    }
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.properties.IEntityPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent) {
    boolean isSynchronized = isSynchronized(fEntities);

    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 10, 10, 5, 5, false));

    /* Explanation Label */
    Label explanationLabel = new Label(container, SWT.WRAP);
    explanationLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
    explanationLabel.setText(Messages.RetentionPropertyPage_CLEANUP_INFO);

    /* Delete by Count */
    fDeleteNewsByCountCheck = new Button(container, SWT.CHECK);
    fDeleteNewsByCountCheck.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    fDeleteNewsByCountCheck.setSelection(fPrefDeleteNewsByCountState);
    fDeleteNewsByCountCheck.setText(isSynchronized ? Messages.RetentionPropertyPage_MAX_NUMBER_SYNCHRONIZED : Messages.RetentionPropertyPage_MAX_NUMBER);
    fDeleteNewsByCountCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fMaxCountSpinner.setEnabled(fDeleteNewsByCountCheck.getSelection());
      }
    });

    fMaxCountSpinner = new Spinner(container, SWT.BORDER);
    fMaxCountSpinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    fMaxCountSpinner.setEnabled(fDeleteNewsByCountCheck.getSelection());
    fMaxCountSpinner.setMinimum(0);
    fMaxCountSpinner.setMaximum(99999);
    fMaxCountSpinner.setSelection(fPrefDeleteNewsByCountValue);

    /* Delete by Age */
    fDeleteNewsByAgeCheck = new Button(container, SWT.CHECK);
    fDeleteNewsByAgeCheck.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    fDeleteNewsByAgeCheck.setSelection(fPrefDeleteNewsByAgeState);
    fDeleteNewsByAgeCheck.setText(isSynchronized ? Messages.RetentionPropertyPage_MAX_AGE_SYNCHRONIZED : Messages.RetentionPropertyPage_MAX_AGE);
    fDeleteNewsByAgeCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fMaxAgeSpinner.setEnabled(fDeleteNewsByAgeCheck.getSelection());
      }
    });

    fMaxAgeSpinner = new Spinner(container, SWT.BORDER);
    fMaxAgeSpinner.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    fMaxAgeSpinner.setEnabled(fDeleteNewsByAgeCheck.getSelection());
    fMaxAgeSpinner.setMinimum(0);
    fMaxAgeSpinner.setMaximum(99999);
    fMaxAgeSpinner.setSelection(fPrefDeleteNewsByAgeValue);

    /* Delete by State */
    fDeleteReadNewsCheck = new Button(container, SWT.CHECK);
    fDeleteReadNewsCheck.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
    fDeleteReadNewsCheck.setText(Messages.RetentionPropertyPage_DELETE_READ);
    fDeleteReadNewsCheck.setSelection(fPrefDeleteReadNews);

    /* Never Delete Unread News State */
    fNeverDeleteUnreadNewsCheck = new Button(container, SWT.CHECK);
    fNeverDeleteUnreadNewsCheck.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
    fNeverDeleteUnreadNewsCheck.setText(Messages.RetentionPropertyPage_DELETE_UNREAD);
    fNeverDeleteUnreadNewsCheck.setSelection(fPrefNeverDeleteUnReadNews);

    /* Never Delete Labeled News State */
    fNeverDeleteLabeledNewsCheck = new Button(container, SWT.CHECK);
    fNeverDeleteLabeledNewsCheck.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
    fNeverDeleteLabeledNewsCheck.setText(Messages.RetentionPropertyPage_NEVER_DELETE_LABELED);
    fNeverDeleteLabeledNewsCheck.setSelection(fPrefNeverDeleteLabeledNews);

    /* Info Container */
    Composite infoContainer = new Composite(container, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    ((GridLayout) infoContainer.getLayout()).marginTop = 5;

    Label infoImg = new Label(infoContainer, SWT.NONE);
    infoImg.setImage(OwlUI.getImage(fSite.getResourceManager(), "icons/obj16/info.gif")); //$NON-NLS-1$
    infoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    Label infoText = new Label(infoContainer, SWT.WRAP);
    infoText.setText(Messages.RetentionPropertyPage_CLEANUP_NOTE);
    infoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    return container;
  }

  private boolean isSynchronized(List<IEntity> entities) {
    for (IEntity entity : entities) {

      /* Folder */
      if (entity instanceof IFolder) {
        IFolder folder = (IFolder) entity;
        if (!isSynchronized(folder))
          return false;
      }

      /* Bookmark */
      else if (entity instanceof IBookMark) {
        IBookMark bm = (IBookMark) entity;
        if (!SyncUtils.isSynchronized(bm))
          return false;
      }

      /* Anything Else */
      else
        return false;
    }

    return true;
  }

  private boolean isSynchronized(IFolder folder) {
    for (IFolderChild child : folder.getChildren()) {

      /* Folder */
      if (child instanceof IFolder) {
        IFolder childFolder = (IFolder) child;
        if (!isSynchronized(childFolder))
          return false;
      }

      /* Bookmark */
      else if (child instanceof IBookMark) {
        IBookMark bm = (IBookMark) child;
        if (!SyncUtils.isSynchronized(bm))
          return false;
      }

      /* Anything Else */
      else
        return false;
    }

    return true;
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

    /* Update changes to Child-BookMarks */
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark) {
        IPreferenceScope scope = Owl.getPreferenceService().getEntityScope(mark);
        updatePreferences(scope);
      }
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

    /* Delete by Count */
    boolean bVal = fDeleteNewsByCountCheck.getSelection();
    if (fPrefDeleteNewsByCountState != bVal) {
      scope.putBoolean(DefaultPreferences.DEL_NEWS_BY_COUNT_STATE, bVal);
      changed = true;
    }

    int iVal = fMaxCountSpinner.getSelection();
    if (fPrefDeleteNewsByCountValue != iVal) {
      scope.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, iVal);
      changed = true;
    }

    /* Delete by Age */
    bVal = fDeleteNewsByAgeCheck.getSelection();
    if (fPrefDeleteNewsByAgeState != bVal) {
      scope.putBoolean(DefaultPreferences.DEL_NEWS_BY_AGE_STATE, bVal);
      changed = true;
    }

    iVal = fMaxAgeSpinner.getSelection();
    if (fPrefDeleteNewsByAgeValue != iVal) {
      scope.putInteger(DefaultPreferences.DEL_NEWS_BY_AGE_VALUE, iVal);
      changed = true;
    }

    /* Delete Read News */
    bVal = fDeleteReadNewsCheck.getSelection();
    if (fPrefDeleteReadNews != bVal) {
      scope.putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, bVal);
      changed = true;
    }

    /* Never Delete Unread News */
    bVal = fNeverDeleteUnreadNewsCheck.getSelection();
    if (fPrefNeverDeleteUnReadNews != bVal) {
      scope.putBoolean(DefaultPreferences.NEVER_DEL_UNREAD_NEWS_STATE, bVal);
      changed = true;
    }

    /* Never Delete Labeled News */
    bVal = fNeverDeleteLabeledNewsCheck.getSelection();
    if (fPrefNeverDeleteLabeledNews != bVal) {
      scope.putBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE, bVal);
      changed = true;
    }

    return changed;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.properties.IEntityPropertyPage#finish()
   */
  public void finish() {

    /* Run Retention since settings changed */
    if (fSettingsChanged) {
      final INewsMark activeFeedViewNewsMark = OwlUI.getActiveFeedViewNewsMark();
      Job retentionJob = new Job(Messages.RetentionPropertyPage_PERFORMING_CLEANUP) {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
          try {
            Set<IBookMark> bookmarks = new HashSet<IBookMark>();
            for (IEntity entity : fEntities) {
              if (entity instanceof IBookMark)
                bookmarks.add((IBookMark) entity);
              else if (entity instanceof IFolder)
                CoreUtils.fillBookMarks(bookmarks, Collections.singleton((IFolder) entity));
            }

            monitor.beginTask(Messages.RetentionPropertyPage_PERFORMING_CLEANUP, bookmarks.size());

            for (IBookMark bookmark : bookmarks) {
              if (Controller.getDefault().isShuttingDown() || monitor.isCanceled())
                break;

              /* Check if retention should run or not */
              if (activeFeedViewNewsMark != null) {
                if (activeFeedViewNewsMark.equals(bookmark))
                  continue; //Avoid clean up on feed the user is reading on
                else if (activeFeedViewNewsMark instanceof FolderNewsMark && ((FolderNewsMark) activeFeedViewNewsMark).contains(bookmark))
                  continue; //Avoid clean up on folder the user is reading on if feed contained
              }

              monitor.subTask(bookmark.getName());
              RetentionStrategy.process(bookmark);
              monitor.worked(1);
            }
          } finally {
            monitor.done();
          }

          return Status.OK_STATUS;
        }
      };

      retentionJob.schedule();
    }
  }
}