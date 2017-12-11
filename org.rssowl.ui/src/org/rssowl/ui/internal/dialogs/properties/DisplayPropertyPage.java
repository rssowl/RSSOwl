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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.LinkTransformer;
import org.rssowl.ui.internal.OwlUI.Layout;
import org.rssowl.ui.internal.OwlUI.PageSize;
import org.rssowl.ui.internal.editors.feed.NewsFilter;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Display Properties.
 *
 * @author bpasero
 */
public class DisplayPropertyPage implements IEntityPropertyPage {
  private List<IEntity> fEntities;
  private Combo fFilterCombo;
  private Combo fGroupCombo;
  private Combo fLayoutCombo;
  private Combo fPageSizeCombo;
  private Button fLoadImagesForNewsCheck;
  private Button fLoadMediaForNewsCheck;
  private Button fDisplayContentsOfNewsRadio;
  private Button fOpenLinkOfNewsRadio;
  private Button fOpenSiteForEmptyNewsCheck;
  private Button fUseTransformerCheck;
  private ComboViewer fLinkTransformerViewer;

  /* Settings */
  private List<IPreferenceScope> fEntityPreferences;
  private int fPrefSelectedFilter;
  private int fPrefSelectedGroup;
  private int fPrefSelectedLayout;
  private int fPrefSelectedPageSize;
  private boolean fPrefOpenSiteForNews;
  private boolean fPrefOpenSiteForEmptyNews;
  private boolean fPrefUseLinkTransformer;
  private String fPrefLinkTransformerId;
  private boolean fPrefLoadImagesForNews;
  private boolean fPrefLoadMediaForNews;
  private boolean fSettingsChanged;

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#init(org.rssowl.ui.dialogs.properties.IPropertyDialogSite,
   * java.util.List)
   */
  @Override
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
    IPreferenceScope globalScope = Owl.getPreferenceService().getGlobalScope();
    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();

    /* Take the first scope as initial values */
    IPreferenceScope firstScope = fEntityPreferences.get(0);
    fPrefSelectedFilter = ModelUtils.loadIntegerValueWithFallback(firstScope, DefaultPreferences.BM_NEWS_FILTERING, globalScope, DefaultPreferences.FV_FILTER_TYPE);
    fPrefSelectedGroup = ModelUtils.loadIntegerValueWithFallback(firstScope, DefaultPreferences.BM_NEWS_GROUPING, globalScope, DefaultPreferences.FV_GROUP_TYPE);
    fPrefOpenSiteForNews = firstScope.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_NEWS);
    fPrefOpenSiteForEmptyNews = firstScope.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_EMPTY_NEWS);
    fPrefUseLinkTransformer = firstScope.getBoolean(DefaultPreferences.BM_USE_TRANSFORMER);
    fPrefLinkTransformerId = firstScope.getString(DefaultPreferences.BM_TRANSFORMER_ID);
    fPrefLoadImagesForNews = firstScope.getBoolean(DefaultPreferences.ENABLE_IMAGES);
    fPrefLoadMediaForNews = firstScope.getBoolean(DefaultPreferences.ENABLE_MEDIA);
    fPrefSelectedLayout = firstScope.getInteger(DefaultPreferences.FV_LAYOUT);
    fPrefSelectedPageSize = firstScope.getInteger(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE);

    /* For any other scope not sharing the initial values, use the default */
    for (int i = 1; i < fEntityPreferences.size(); i++) {
      IPreferenceScope otherScope = fEntityPreferences.get(i);

      if (ModelUtils.loadIntegerValueWithFallback(otherScope, DefaultPreferences.BM_NEWS_FILTERING, globalScope, DefaultPreferences.FV_FILTER_TYPE) != fPrefSelectedFilter)
        fPrefSelectedFilter = ModelUtils.loadIntegerValueWithFallback(defaultScope, DefaultPreferences.BM_NEWS_FILTERING, defaultScope, DefaultPreferences.FV_FILTER_TYPE);

      if (ModelUtils.loadIntegerValueWithFallback(otherScope, DefaultPreferences.BM_NEWS_GROUPING, globalScope, DefaultPreferences.FV_GROUP_TYPE) != fPrefSelectedGroup)
        fPrefSelectedGroup = ModelUtils.loadIntegerValueWithFallback(defaultScope, DefaultPreferences.BM_NEWS_GROUPING, defaultScope, DefaultPreferences.FV_GROUP_TYPE);

      if (otherScope.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_NEWS) != fPrefOpenSiteForNews)
        fPrefOpenSiteForNews = defaultScope.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_NEWS);

      if (otherScope.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_EMPTY_NEWS) != fPrefOpenSiteForEmptyNews)
        fPrefOpenSiteForEmptyNews = defaultScope.getBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_EMPTY_NEWS);

      if (otherScope.getBoolean(DefaultPreferences.BM_USE_TRANSFORMER) != fPrefUseLinkTransformer)
        fPrefUseLinkTransformer = defaultScope.getBoolean(DefaultPreferences.BM_USE_TRANSFORMER);

      if (fPrefLinkTransformerId != null && !fPrefLinkTransformerId.equals(otherScope.getString(DefaultPreferences.BM_TRANSFORMER_ID)))
        fPrefLinkTransformerId = defaultScope.getString(DefaultPreferences.BM_TRANSFORMER_ID);

      if (otherScope.getBoolean(DefaultPreferences.ENABLE_IMAGES) != fPrefLoadImagesForNews)
        fPrefLoadImagesForNews = defaultScope.getBoolean(DefaultPreferences.ENABLE_IMAGES);

      if (otherScope.getBoolean(DefaultPreferences.ENABLE_MEDIA) != fPrefLoadMediaForNews)
        fPrefLoadMediaForNews = defaultScope.getBoolean(DefaultPreferences.ENABLE_MEDIA);

      if (otherScope.getInteger(DefaultPreferences.FV_LAYOUT) != fPrefSelectedLayout)
        fPrefSelectedLayout = defaultScope.getInteger(DefaultPreferences.FV_LAYOUT);

      if (otherScope.getInteger(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE) != fPrefSelectedPageSize)
        fPrefSelectedPageSize = defaultScope.getInteger(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE);
    }
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 10, 10));

    Composite topContainer = new Composite(container, SWT.None);
    topContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 5, 15, false));
    topContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));

    /* Layout Settings */
    Label layoutLabel = new Label(topContainer, SWT.None);
    layoutLabel.setText(Messages.DisplayPropertyPage_LAYOUT);

    Composite layoutContainer = new Composite(topContainer, SWT.None);
    layoutContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
    layoutContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 5, false));

    fLayoutCombo = new Combo(layoutContainer, SWT.BORDER | SWT.READ_ONLY);
    fLayoutCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    for (Layout layout : Layout.values()) {
      fLayoutCombo.add(layout.getName());
    }

    fLayoutCombo.select(fPrefSelectedLayout);
    fLayoutCombo.setVisibleItemCount(fLayoutCombo.getItemCount());

    fLayoutCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateDisplayButtons();
      }
    });

    /* Layout Page Size */
    fPageSizeCombo = new Combo(layoutContainer, SWT.BORDER | SWT.READ_ONLY);
    fPageSizeCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

    for (PageSize pageSize : PageSize.values()) {
      fPageSizeCombo.add(pageSize.getName());
    }

    fPageSizeCombo.select(PageSize.from(fPrefSelectedPageSize).ordinal());
    fPageSizeCombo.setVisibleItemCount(fPageSizeCombo.getItemCount());

    /* Filter Settings */
    Label filterLabel = new Label(topContainer, SWT.None);
    filterLabel.setText(Messages.DisplayPropertyPage_FILTER);

    fFilterCombo = new Combo(topContainer, SWT.BORDER | SWT.READ_ONLY);
    fFilterCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

    NewsFilter.Type[] filters = NewsFilter.Type.values();
    for (NewsFilter.Type filter : filters)
      fFilterCombo.add(filter.getName());

    fFilterCombo.select(fPrefSelectedFilter);
    fFilterCombo.setVisibleItemCount(fFilterCombo.getItemCount());

    /* Group Settings */
    Label groupLabel = new Label(topContainer, SWT.None);
    groupLabel.setText(Messages.DisplayPropertyPage_GROUP);

    fGroupCombo = new Combo(topContainer, SWT.BORDER | SWT.READ_ONLY);
    fGroupCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

    NewsGrouping.Type[] groups = NewsGrouping.Type.values();
    for (NewsGrouping.Type group : groups)
      fGroupCombo.add(group.getName());

    fGroupCombo.select(fPrefSelectedGroup);
    fGroupCombo.setVisibleItemCount(fGroupCombo.getItemCount());

    Composite bottomContainer = new Composite(container, SWT.None);
    bottomContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 10));
    bottomContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));

    /* Display Content of News */
    fDisplayContentsOfNewsRadio = new Button(bottomContainer, SWT.RADIO);
    fDisplayContentsOfNewsRadio.setText(Messages.DisplayPropertyPage_DISPLAY_NEWS_CONTENT);
    fDisplayContentsOfNewsRadio.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
    fDisplayContentsOfNewsRadio.setSelection(!fPrefOpenSiteForNews);

    Composite bottomSubContainer = new Composite(bottomContainer, SWT.None);
    bottomSubContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 5));
    bottomSubContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
    ((GridLayout) bottomSubContainer.getLayout()).marginLeft = 15;

    /* Load Images */
    fLoadImagesForNewsCheck = new Button(bottomSubContainer, SWT.CHECK);
    fLoadImagesForNewsCheck.setText(Messages.DisplayPropertyPage_LOAD_IMAGES);
    fLoadImagesForNewsCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
    fLoadImagesForNewsCheck.setSelection(fPrefLoadImagesForNews);

    /* Load Media and Flash Content */
    fLoadMediaForNewsCheck = new Button(bottomSubContainer, SWT.CHECK);
    fLoadMediaForNewsCheck.setText(Messages.DisplayPropertyPage_LOAD_MEDIA);
    fLoadMediaForNewsCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
    fLoadMediaForNewsCheck.setSelection(fPrefLoadMediaForNews);

    /* Open Link of News */
    fOpenLinkOfNewsRadio = new Button(bottomContainer, SWT.RADIO);
    fOpenLinkOfNewsRadio.setText(Messages.DisplayPropertyPage_OPEN_NEWS_LINK);
    fOpenLinkOfNewsRadio.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
    fOpenLinkOfNewsRadio.setSelection(fPrefOpenSiteForNews);
    fOpenLinkOfNewsRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateDisplayButtons();
      }
    });

    bottomSubContainer = new Composite(bottomContainer, SWT.None);
    bottomSubContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 5));
    bottomSubContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
    ((GridLayout) bottomSubContainer.getLayout()).marginLeft = 15;

    /* Open Link only when content is empty */
    fOpenSiteForEmptyNewsCheck = new Button(bottomSubContainer, SWT.CHECK);
    fOpenSiteForEmptyNewsCheck.setText(Messages.DisplayPropertyPage_ONLY_EMPTY_CONTENT);
    fOpenSiteForEmptyNewsCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
    fOpenSiteForEmptyNewsCheck.setSelection(fPrefOpenSiteForEmptyNews);
    fOpenSiteForEmptyNewsCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateDisplayButtons();
      }
    });

    /* Use Link Transformer */
    fUseTransformerCheck = new Button(bottomSubContainer, SWT.CHECK);
    fUseTransformerCheck.setText(Messages.DisplayPropertyPage_USE_LINK_TRANSFORMER);
    fUseTransformerCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
    fUseTransformerCheck.setSelection(fPrefUseLinkTransformer);
    fUseTransformerCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateDisplayButtons();
      }
    });

    /* Selected Link Transformer */
    Combo linkTransformerCombo = new Combo(bottomSubContainer, SWT.READ_ONLY | SWT.BORDER);
    linkTransformerCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));

    fLinkTransformerViewer = new ComboViewer(linkTransformerCombo);
    fLinkTransformerViewer.setContentProvider(new ArrayContentProvider());
    fLinkTransformerViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return ((LinkTransformer) element).getName();
      }
    });

    List<LinkTransformer> linkTransformers = Controller.getDefault().getLinkTransformers();
    fLinkTransformerViewer.setInput(linkTransformers);

    LinkTransformer selectedTransformer = Controller.getDefault().getLinkTransformer(fPrefLinkTransformerId);
    if (selectedTransformer == null)
      selectedTransformer = linkTransformers.get(0);

    fLinkTransformerViewer.setSelection(new StructuredSelection(selectedTransformer));

    updateDisplayButtons(false);

    return container;
  }

  private void updateDisplayButtons() {
    updateDisplayButtons(true);
  }

  private void updateDisplayButtons(boolean layout) {
    boolean isNewspaperLayout = (fLayoutCombo.getSelectionIndex() == Layout.NEWSPAPER.ordinal() || fLayoutCombo.getSelectionIndex() == Layout.HEADLINES.ordinal());
    boolean isListLayout = (fLayoutCombo.getSelectionIndex() == Layout.LIST.ordinal());

    /* Force selection to first radio if using newspaper layout */
    if (isNewspaperLayout && !fDisplayContentsOfNewsRadio.getSelection()) {
      fDisplayContentsOfNewsRadio.setSelection(true);
      fOpenLinkOfNewsRadio.setSelection(false);
    }

    /* Update Enablement */
    fDisplayContentsOfNewsRadio.setEnabled(!isListLayout);
    fLoadImagesForNewsCheck.setEnabled(!isListLayout && (fDisplayContentsOfNewsRadio.getSelection() || fOpenSiteForEmptyNewsCheck.getSelection()));
    fLoadMediaForNewsCheck.setEnabled(!isListLayout && (fDisplayContentsOfNewsRadio.getSelection() || fOpenSiteForEmptyNewsCheck.getSelection()));
    fOpenLinkOfNewsRadio.setEnabled(!isListLayout && !isNewspaperLayout);
    fOpenSiteForEmptyNewsCheck.setEnabled(!isListLayout && !isNewspaperLayout && fOpenLinkOfNewsRadio.getSelection());
    fUseTransformerCheck.setEnabled(!isListLayout && !isNewspaperLayout && fOpenLinkOfNewsRadio.getSelection());
    fLinkTransformerViewer.getCombo().setEnabled(!isListLayout && !isNewspaperLayout && fOpenLinkOfNewsRadio.getSelection() && fUseTransformerCheck.getSelection());

    /* Update Layout */
    GridData data = (GridData) fLayoutCombo.getLayoutData();
    data.horizontalSpan = isNewspaperLayout ? 1 : 2;
    data = (GridData) fPageSizeCombo.getLayoutData();
    data.exclude = !isNewspaperLayout;

    if (layout)
      fPageSizeCombo.getParent().getParent().layout(true, true);
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    return null;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#setFocus()
   */
  @Override
  public void setFocus() {}

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#performOk(java.util.Set)
   */
  @Override
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

    /* Filter */
    int iVal = fFilterCombo.getSelectionIndex();
    if (fPrefSelectedFilter != iVal) {
      scope.putInteger(DefaultPreferences.BM_NEWS_FILTERING, iVal);
      changed = true;
    }

    /* Grouping */
    iVal = fGroupCombo.getSelectionIndex();
    if (fPrefSelectedGroup != iVal) {
      scope.putInteger(DefaultPreferences.BM_NEWS_GROUPING, iVal);
      changed = true;
    }

    /* Layout */
    iVal = fLayoutCombo.getSelectionIndex();
    if (fPrefSelectedLayout != iVal) {
      scope.putInteger(DefaultPreferences.FV_LAYOUT, iVal);
      changed = true;
    }

    /* Page Size */
    iVal = fPageSizeCombo.getSelectionIndex();
    PageSize size = PageSize.values()[iVal];
    if (fPrefSelectedPageSize != size.getPageSize()) {
      scope.putInteger(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE, size.getPageSize());
      changed = true;
    }

    /* Find out if any other display properties changed */
    boolean otherDisplayChanges = false;
    if (fOpenLinkOfNewsRadio.getSelection() != fPrefOpenSiteForNews)
      otherDisplayChanges = true;
    else if (fOpenSiteForEmptyNewsCheck.getSelection() != fPrefOpenSiteForEmptyNews)
      otherDisplayChanges = true;
    else if (fLoadImagesForNewsCheck.getSelection() != fPrefLoadImagesForNews)
      otherDisplayChanges = true;
    else if (fLoadMediaForNewsCheck.getSelection() != fPrefLoadMediaForNews)
      otherDisplayChanges = true;
    else if (fUseTransformerCheck.getSelection() != fPrefUseLinkTransformer)
      otherDisplayChanges = true;
    else {
      IStructuredSelection selection = (IStructuredSelection) fLinkTransformerViewer.getSelection();
      if (!selection.isEmpty()) {
        LinkTransformer transformer = (LinkTransformer) selection.getFirstElement();
        if (!transformer.getId().equals(fPrefLinkTransformerId))
          otherDisplayChanges = true;
      }
    }

    /*
     * We can not simply store the one display property that has changed if any
     * of the properties changed because as soon as the global scope changes in
     * any way, these changes here would be overridden otherwise. The fix is to
     * store all display properties into the news mark in case any has changed.
     */
    if (otherDisplayChanges) {
      changed = true;

      scope.putBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_NEWS, fOpenLinkOfNewsRadio.getSelection());
      scope.putBoolean(DefaultPreferences.BM_OPEN_SITE_FOR_EMPTY_NEWS, fOpenSiteForEmptyNewsCheck.getSelection());
      scope.putBoolean(DefaultPreferences.ENABLE_IMAGES, fLoadImagesForNewsCheck.getSelection());
      scope.putBoolean(DefaultPreferences.ENABLE_MEDIA, fLoadMediaForNewsCheck.getSelection());
      scope.putBoolean(DefaultPreferences.BM_USE_TRANSFORMER, fUseTransformerCheck.getSelection());

      IStructuredSelection selection = (IStructuredSelection) fLinkTransformerViewer.getSelection();
      if (!selection.isEmpty()) {
        LinkTransformer transformer = (LinkTransformer) selection.getFirstElement();
        scope.putString(DefaultPreferences.BM_TRANSFORMER_ID, transformer.getId());
      }
    }

    return changed;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IEntityPropertyPage#finish()
   */
  @Override
  public void finish() {

    /* Propagate change to open Editors */
    if (fSettingsChanged) {
      EditorUtils.updateLayout();
      EditorUtils.updateFilterAndGrouping();
    }
  }
}