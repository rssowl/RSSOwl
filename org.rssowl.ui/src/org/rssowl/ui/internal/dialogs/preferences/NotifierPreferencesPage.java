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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.NewsFiltersListDialog;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkLabelProvider;
import org.rssowl.ui.internal.views.explorer.BookMarkSorter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class NotifierPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

  /** ID of the Page */
  public static String ID = "org.rssowl.ui.NotifierPreferences"; //$NON-NLS-1$

  private IPreferenceScope fGlobalScope = Owl.getPreferenceService().getGlobalScope();
  private Button fNotificationOnlyFromTray;
  private Button fShowNotificationPopup;
  private Button fLimitNotificationCheck;
  private Spinner fLimitNotificationSpinner;
  private CheckboxTreeViewer fViewer;
  private Button fDeselectAll;
  private Button fSelectAll;
  private Button fLimitNotifierToSelectionCheck;
  private Spinner fAutoCloseNotifierSpinner;
  private Button fAutoCloseNotifierCheck;
  private Button fShowExcerptCheck;
  private Button fCloseNotifierOnOpen;
  private Button fFadeNotifierCheck;
  private LocalResourceManager fResources = new LocalResourceManager(JFaceResources.getResources());

  /** Leave for reflection */
  public NotifierPreferencesPage() {
    setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/notification.gif")); //$NON-NLS-1$
  }

  /**
   * @param title
   */
  public NotifierPreferencesPage(String title) {
    super(title);
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  @Override
  public void init(IWorkbench workbench) {}

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    updateApplyEnablement(false);
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    fResources.dispose();
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = createComposite(parent);

    /* Misc. Options */
    createNotificationOptions(container);

    /* Info Container */
    Composite infoContainer = new Composite(container, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));

    Label infoImg = new Label(infoContainer, SWT.NONE);
    infoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/info.gif")); //$NON-NLS-1$
    infoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    Link infoText = new Link(infoContainer, SWT.WRAP);
    infoText.setText(Messages.NotifierPreferencesPage_NOTIFIER_TIP);
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

    /* Enable Apply Button on Selection Changes */
    OwlUI.runOnSelection(new Runnable() {
      @Override
      public void run() {
        updateApplyEnablement(true);
      }
    }, container);

    return container;
  }

  private void createNotifierViewer(Composite container) {

    /* Check Button to enable Limitation */
    fLimitNotifierToSelectionCheck = new Button(container, SWT.CHECK);
    fLimitNotifierToSelectionCheck.setText(Messages.NotifierPreferencesPage_SHOW_FOR_SELECTED);
    fLimitNotifierToSelectionCheck.setSelection(fGlobalScope.getBoolean(DefaultPreferences.LIMIT_NOTIFIER_TO_SELECTION));
    fLimitNotifierToSelectionCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setLimitNotificationEnabled(fLimitNotifierToSelectionCheck.getSelection());
      }
    });

    /* Viewer to select particular Folders/Marks */
    fViewer = new CheckboxTreeViewer(container, SWT.BORDER);
    fViewer.setAutoExpandLevel(2);
    fViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    ((GridData) fViewer.getTree().getLayoutData()).heightHint = 190;
    fViewer.getTree().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    /* Sort by Name if set so */
    if (Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.BE_SORT_BY_NAME)) {
      BookMarkSorter sorter = new BookMarkSorter();
      sorter.setType(BookMarkSorter.Type.SORT_BY_NAME);
      fViewer.setComparator(sorter);
    }

    /* ContentProvider */
    fViewer.setContentProvider(new ITreeContentProvider() {
      @Override
      public Object[] getElements(Object inputElement) {
        Collection<IFolder> rootFolders = CoreUtils.loadRootFolders();
        return rootFolders.toArray();
      }

      @Override
      public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IFolder) {
          IFolder folder = (IFolder) parentElement;
          return folder.getChildren().toArray();
        }

        return new Object[0];
      }

      @Override
      public Object getParent(Object element) {
        if (element instanceof IFolder) {
          IFolder folder = (IFolder) element;
          return folder.getParent();
        }

        return null;
      }

      @Override
      public boolean hasChildren(Object element) {
        if (element instanceof IFolder) {
          IFolder folder = (IFolder) element;
          return !folder.isEmpty();
        }

        return false;
      }

      @Override
      public void dispose() {}

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    });

    /* LabelProvider */
    fViewer.setLabelProvider(new BookMarkLabelProvider(false, true));

    /* Viewer Filter */
    fViewer.addFilter(new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        return !(element instanceof INewsBin);
      }
    });

    /* Listen on Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        IFolder folder = selection.getFirstElement() instanceof IFolder ? (IFolder) selection.getFirstElement() : null;

        /* Expand / Collapse Folder */
        if (folder != null && !folder.isEmpty()) {
          boolean expandedState = !fViewer.getExpandedState(folder);
          fViewer.setExpandedState(folder, expandedState);

          if (expandedState && fViewer.getChecked(folder))
            setChildsChecked(folder, true, true);
        }
      }
    });

    /* Dummy Input */
    fViewer.setInput(new Object());

    /* Set Checked Elements */
    Collection<IFolder> rootFolders = DynamicDAO.getDAO(IFolderDAO.class).loadRoots();
    for (IFolder folder : rootFolders) {
      setCheckedElements(folder, false);
    }

    /* Update Checks on Selection */
    fViewer.getTree().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (e.detail == SWT.CHECK) {
          TreeItem item = (TreeItem) e.item;
          setChildsChecked((IFolderChild) item.getData(), item.getChecked(), false);

          if (!item.getChecked())
            setParentsChecked((IFolderChild) item.getData(), false);
        }
      }
    });

    /* Update Checks on Expand */
    fViewer.addTreeListener(new ITreeViewerListener() {
      @Override
      public void treeExpanded(TreeExpansionEvent event) {
        boolean isChecked = fViewer.getChecked(event.getElement());
        if (isChecked)
          setChildsChecked((IFolderChild) event.getElement(), isChecked, false);
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event) {}
    });

    /* Select All / Deselect All */
    Composite buttonContainer = new Composite(container, SWT.NONE);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fSelectAll = new Button(buttonContainer, SWT.PUSH);
    fSelectAll.setText(Messages.NotifierPreferencesPage_SELECT_ALL);
    Dialog.applyDialogFont(fSelectAll);
    setButtonLayoutData(fSelectAll);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        OwlUI.setAllChecked(fViewer.getTree(), true);
      }
    });

    fDeselectAll = new Button(buttonContainer, SWT.PUSH);
    fDeselectAll.setText(Messages.NotifierPreferencesPage_DESELECT_ALL);
    Dialog.applyDialogFont(fDeselectAll);
    setButtonLayoutData(fDeselectAll);
    fDeselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        OwlUI.setAllChecked(fViewer.getTree(), false);
      }
    });

    setLimitNotificationEnabled(fLimitNotifierToSelectionCheck.getSelection());
  }

  private void setCheckedElements(IFolderChild entity, boolean parentChecked) {

    /* Check for Preference */
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(entity);
    if (prefs.getBoolean(DefaultPreferences.ENABLE_NOTIFIER)) {
      if (!parentChecked) {
        setParentsExpanded(entity);
        parentChecked = true;
      }
      fViewer.setChecked(entity, true);
      setChildsChecked(entity, true, true);
    }

    /* Check for Childs */
    if (entity instanceof IFolder) {
      List<IFolderChild> children = ((IFolder) entity).getChildren();
      for (IFolderChild child : children) {
        setCheckedElements(child, parentChecked);
      }
    }
  }

  private void setParentsExpanded(IFolderChild folderChild) {
    IFolder parent = folderChild.getParent();
    if (parent != null) {
      fViewer.setExpandedState(parent, true);
      setParentsExpanded(parent);
    }
  }

  private void setLimitNotificationEnabled(boolean selection) {
    fViewer.getTree().setEnabled(selection);
    fSelectAll.setEnabled(selection);
    fDeselectAll.setEnabled(selection);
  }

  private void setChildsChecked(IFolderChild folderChild, boolean checked, boolean onlyExpanded) {
    if (folderChild instanceof IFolder && (!onlyExpanded || fViewer.getExpandedState(folderChild))) {
      List<IFolderChild> children = ((IFolder) folderChild).getChildren();
      for (IFolderChild child : children) {
        fViewer.setChecked(child, checked);
        setChildsChecked(child, checked, onlyExpanded);
      }
    }
  }

  private void setParentsChecked(IFolderChild folderChild, boolean checked) {
    IFolder parent = folderChild.getParent();
    if (parent != null) {
      fViewer.setChecked(parent, checked);
      setParentsChecked(parent, checked);
    }
  }

  private void createNotificationOptions(Composite container) {
    Composite notificationGroup = new Composite(container, SWT.None);
    notificationGroup.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    notificationGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* General */
    {
      Label label = new Label(notificationGroup, SWT.NONE);
      label.setText(Messages.NotifierPreferencesPage_GENERAL);
      label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

      /* Group */
      Composite group = new Composite(notificationGroup, SWT.None);
      group.setLayout(LayoutUtils.createGridLayout(1, 7, 3));
      ((GridLayout) group.getLayout()).marginBottom = 5;
      group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      /* Limit number of Items showing in Notification */
      Composite limitItemsContainer = new Composite(group, SWT.None);
      limitItemsContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0, 0, 2, false));

      int notificationLimit = fGlobalScope.getInteger(DefaultPreferences.LIMIT_NOTIFICATION_SIZE);

      fLimitNotificationCheck = new Button(limitItemsContainer, SWT.CHECK);
      fLimitNotificationCheck.setText(Messages.NotifierPreferencesPage_SHOW_MAX_NEWS_START);
      fLimitNotificationCheck.setSelection(notificationLimit >= 0);
      fLimitNotificationCheck.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          fLimitNotificationSpinner.setEnabled(fLimitNotificationCheck.getSelection());
        }
      });

      fLimitNotificationSpinner = new Spinner(limitItemsContainer, SWT.BORDER);
      fLimitNotificationSpinner.setMinimum(1);
      fLimitNotificationSpinner.setMaximum(30);
      fLimitNotificationSpinner.setEnabled(fLimitNotificationCheck.getSelection());
      if (notificationLimit > 0)
        fLimitNotificationSpinner.setSelection(notificationLimit);
      else
        fLimitNotificationSpinner.setSelection(notificationLimit * -1);

      label = new Label(limitItemsContainer, SWT.None);
      label.setText(Messages.NotifierPreferencesPage_SHOW_MAX_NEWS_END);

      /* Full Content */
      fShowExcerptCheck = new Button(group, SWT.CHECK);
      fShowExcerptCheck.setText(Messages.NotifierPreferencesPage_SHOW_EXCERPT);
      fShowExcerptCheck.setSelection(fGlobalScope.getBoolean(DefaultPreferences.SHOW_EXCERPT_IN_NOTIFIER));

      /* Only from Tray */
      fNotificationOnlyFromTray = new Button(group, SWT.CHECK);
      fNotificationOnlyFromTray.setText(Messages.NotifierPreferencesPage_SHOW_WHEN_MINIMIZED);
      fNotificationOnlyFromTray.setSelection(fGlobalScope.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED));

      /* Animate Notifier */
      fFadeNotifierCheck = new Button(group, SWT.CHECK);
      fFadeNotifierCheck.setText(Messages.NotifierPreferencesPage_ANIMATE_NOTIFIER);
      fFadeNotifierCheck.setSelection(fGlobalScope.getBoolean(DefaultPreferences.FADE_NOTIFIER));

      /* Close Notifier when opening Item */
      fCloseNotifierOnOpen = new Button(group, SWT.CHECK);
      fCloseNotifierOnOpen.setText(Messages.NotifierPreferencesPage_CLOSE_NOTIFIER_ON_OPEN);
      fCloseNotifierOnOpen.setSelection(fGlobalScope.getBoolean(DefaultPreferences.CLOSE_NOTIFIER_ON_OPEN));

      /* Auto Close Notifier */
      Composite autoCloseContainer = new Composite(group, SWT.None);
      autoCloseContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0, 0, 2, false));

      fAutoCloseNotifierCheck = new Button(autoCloseContainer, SWT.CHECK);
      fAutoCloseNotifierCheck.setText(Messages.NotifierPreferencesPage_CLOSE_AUTOMATICALLY);
      fAutoCloseNotifierCheck.setSelection(!fGlobalScope.getBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP));
      fAutoCloseNotifierCheck.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          fAutoCloseNotifierSpinner.setEnabled(fAutoCloseNotifierCheck.getSelection());
        }
      });

      int notificationAutoCloseValue = fGlobalScope.getInteger(DefaultPreferences.AUTOCLOSE_NOTIFICATION_VALUE);

      fAutoCloseNotifierSpinner = new Spinner(autoCloseContainer, SWT.BORDER);
      fAutoCloseNotifierSpinner.setMinimum(1);
      fAutoCloseNotifierSpinner.setMaximum(99);
      fAutoCloseNotifierSpinner.setEnabled(fAutoCloseNotifierCheck.getSelection());
      fAutoCloseNotifierSpinner.setSelection(notificationAutoCloseValue);

      label = new Label(autoCloseContainer, SWT.None);
      label.setText(Messages.NotifierPreferencesPage_SECONDS);
    }

    /* Incoming News */
    {
      Label label = new Label(notificationGroup, SWT.NONE);
      label.setText(Messages.NotifierPreferencesPage_NOTIFICATION_FOR_INCOMING_NEWS);
      label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

      /* Group */
      Composite group = new Composite(notificationGroup, SWT.None);
      group.setLayout(LayoutUtils.createGridLayout(1, 7, 3));
      ((GridLayout) group.getLayout()).marginBottom = 5;
      group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      /* Show Notification Popup */
      fShowNotificationPopup = new Button(group, SWT.CHECK);
      fShowNotificationPopup.setText(Messages.NotifierPreferencesPage_SHOW_NOTIFIER);
      fShowNotificationPopup.setSelection(fGlobalScope.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP));
      fShowNotificationPopup.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          setLimitNotificationEnabled(fLimitNotifierToSelectionCheck.getSelection());
        }
      });

      /* Viewer to select Folders/Marks for the Notifier */
      createNotifierViewer(group);
    }
  }

  private Composite createComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
    composite.setFont(parent.getFont());
    return composite;
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    fGlobalScope.putBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP, fShowNotificationPopup.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED, fNotificationOnlyFromTray.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.FADE_NOTIFIER, fFadeNotifierCheck.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.SHOW_EXCERPT_IN_NOTIFIER, fShowExcerptCheck.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.CLOSE_NOTIFIER_ON_OPEN, fCloseNotifierOnOpen.getSelection());

    fGlobalScope.putBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP, !fAutoCloseNotifierCheck.getSelection());
    fGlobalScope.putInteger(DefaultPreferences.AUTOCLOSE_NOTIFICATION_VALUE, fAutoCloseNotifierSpinner.getSelection());

    if (fLimitNotificationCheck.getSelection())
      fGlobalScope.putInteger(DefaultPreferences.LIMIT_NOTIFICATION_SIZE, fLimitNotificationSpinner.getSelection());
    else
      fGlobalScope.putInteger(DefaultPreferences.LIMIT_NOTIFICATION_SIZE, fLimitNotificationSpinner.getSelection() * -1);

    fGlobalScope.putBoolean(DefaultPreferences.LIMIT_NOTIFIER_TO_SELECTION, fLimitNotifierToSelectionCheck.getSelection());

    /* Entity Scopes from Selected Elements */
    if (fLimitNotifierToSelectionCheck.getSelection()) {
      Collection<IFolder> rootFolders = DynamicDAO.getDAO(IFolderDAO.class).loadRoots();
      List<?> checkedElements = Arrays.asList(fViewer.getCheckedElements());
      final Set<IFolderChild> entitiesToSave = new HashSet<IFolderChild>();

      for (IFolder folder : rootFolders) {
        boolean checked = checkedElements.contains(folder);
        performOk(folder, checkedElements, entitiesToSave, checked);
      }

      /* Save */
      BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
        @Override
        public void run() {

          /* Save Entities */
          DynamicDAO.saveAll(entitiesToSave);

          /* Inform Notification Service */
          if (!entitiesToSave.isEmpty())
            Controller.getDefault().getNotificationService().notifySettingsChanged();
        }
      });
    }

    return super.performOk();
  }

  private void performOk(IFolderChild entity, List<?> checkedElements, Set<IFolderChild> entitiesToSave, boolean parentChecked) {
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(entity);
    boolean save = false;

    /* Folder */
    boolean checked = checkedElements.contains(entity) || parentChecked;

    /* Now Checked and previously not */
    if (checked && !prefs.getBoolean(DefaultPreferences.ENABLE_NOTIFIER)) {
      prefs.putBoolean(DefaultPreferences.ENABLE_NOTIFIER, true);
      save = true;
    }

    /* Now unchecked but previously checked */
    else if (!checked && prefs.getBoolean(DefaultPreferences.ENABLE_NOTIFIER)) {
      prefs.delete(DefaultPreferences.ENABLE_NOTIFIER);
      save = true;
    }

    /* Remember to save if required */
    if (save)
      entitiesToSave.add(entity);

    /* Childs */
    if (entity instanceof IFolder) {
      List<IFolderChild> children = ((IFolder) entity).getChildren();
      for (IFolderChild child : children) {
        performOk(child, checkedElements, entitiesToSave, checked);
      }
    }
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performApply()
   */
  @Override
  protected void performApply() {
    super.performApply();
    updateApplyEnablement(false);
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults() {
    super.performDefaults();

    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();

    fShowNotificationPopup.setSelection(defaultScope.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP));
    fNotificationOnlyFromTray.setSelection(defaultScope.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED));
    fFadeNotifierCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.FADE_NOTIFIER));
    fShowExcerptCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.SHOW_EXCERPT_IN_NOTIFIER));
    fCloseNotifierOnOpen.setSelection(defaultScope.getBoolean(DefaultPreferences.CLOSE_NOTIFIER_ON_OPEN));

    fAutoCloseNotifierCheck.setSelection(!defaultScope.getBoolean(DefaultPreferences.STICKY_NOTIFICATION_POPUP));
    fAutoCloseNotifierSpinner.setSelection(defaultScope.getInteger(DefaultPreferences.AUTOCLOSE_NOTIFICATION_VALUE));
    fAutoCloseNotifierSpinner.setEnabled(fAutoCloseNotifierCheck.getSelection());

    /* Show a maximum of N News */
    int limitNotificationValue = defaultScope.getInteger(DefaultPreferences.LIMIT_NOTIFICATION_SIZE);
    fLimitNotificationCheck.setSelection(limitNotificationValue >= 0);
    if (limitNotificationValue >= 0)
      fLimitNotificationSpinner.setSelection(limitNotificationValue);
    fLimitNotificationCheck.setEnabled(fShowNotificationPopup.getSelection());
    fLimitNotificationSpinner.setEnabled(fShowNotificationPopup.getSelection());

    /* Limit to Selected Elements */
    fLimitNotifierToSelectionCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.LIMIT_NOTIFIER_TO_SELECTION));
    setLimitNotificationEnabled(fLimitNotifierToSelectionCheck.getSelection());

    updateApplyEnablement(true);
  }

  private void updateApplyEnablement(boolean enable) {
    Button applyButton = getApplyButton();
    if (applyButton != null && !applyButton.isDisposed() && applyButton.isEnabled() != enable)
      applyButton.setEnabled(enable);
  }
}