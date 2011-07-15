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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * A Dialog to show activity, such as the progress of updating feeds or
 * downloading attachments.
 * <p>
 * TODO This dialog uses internal Eclipse classes (Progress Viewer) that can
 * break over future releases.
 * </p>
 *
 * @author bpasero
 */
public class ActivityDialog extends TitleAreaDialog {

  /* Keep the visible instance saved */
  private static ActivityDialog fgVisibleInstance;

  /* Section for Dialogs Settings */
  private static final String SETTINGS_SECTION = "org.rssowl.ui.internal.dialogs.ActivityDialog"; //$NON-NLS-1$

  /* Minimum Height in DLUs */
  private static final int MIN_DIALOG_HEIGHT_DLU = 160;

  @SuppressWarnings("restriction")
  private org.eclipse.ui.internal.progress.DetailedProgressViewer fViewer;
  private LocalResourceManager fResources;
  private IDialogSettings fDialogSettings;
  private boolean fFirstTimeOpen;
  private Button fHideCompletedCheck;
  private IPreferenceScope fPreferences;

  /**
   * @param parentShell
   */
  public ActivityDialog(Shell parentShell) {
    super(parentShell);
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fDialogSettings = Activator.getDefault().getDialogSettings();
    fFirstTimeOpen = (fDialogSettings.getSection(SETTINGS_SECTION) == null);
    fPreferences = Owl.getPreferenceService().getGlobalScope();
  }

  /**
   * @return Returns an instance of <code>ActivityDialog</code> or
   * <code>NULL</code> in case no instance is currently open.
   */
  public static ActivityDialog getVisibleInstance() {
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
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#close()
   */
  @Override
  public boolean close() {
    fgVisibleInstance = null;
    fResources.dispose();
    return super.close();
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.ActivityDialog_DOWNLOADS_ACTIVITY);
    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        fgVisibleInstance = null;
      }
    });
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);

    getButton(IDialogConstants.OK_ID).setFocus();

    return c;
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @SuppressWarnings("restriction")
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Title */
    setTitle(Messages.ActivityDialog_DOWNLOADS_AND_ACTIVITY);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, "icons/wizban/activity_wiz.png")); //$NON-NLS-1$

    /* Title Message */
    setMessage(Messages.ActivityDialog_ACTIVITY_INFO);

    /* Separator */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Composite to hold all components */
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Progress Viewer */
    fViewer = new org.eclipse.ui.internal.progress.DetailedProgressViewer(composite, SWT.NONE);
    fViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Content Provider */
    boolean hideCompleted = fPreferences.getBoolean(DefaultPreferences.HIDE_COMPLETED_DOWNLOADS);
    fViewer.setContentProvider(new org.eclipse.ui.internal.progress.ProgressViewerContentProvider(fViewer, false, !hideCompleted));

    /* Comparator */
    fViewer.setComparator(new ViewerComparator() {
      @SuppressWarnings("unchecked")
      @Override
      public int compare(Viewer viewer, Object obj1, Object obj2) {
        if (obj1 instanceof Comparable && obj2 instanceof Comparable)
          return ((Comparable) obj1).compareTo(obj2);

        return super.compare(viewer, obj1, obj2);
      }
    });

    /* Input */
    fViewer.setInput(org.eclipse.ui.internal.progress.ProgressManager.getInstance());

    /* Bug: The initial size is not set properly for the List */
    updateViewerSize();

    /* Separator */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    applyDialogFont(composite);

    return composite;
  }

  @SuppressWarnings("restriction")
  private void updateViewerSize() {
    Point size = fViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    size.x += IDialogConstants.HORIZONTAL_SPACING;
    size.y += IDialogConstants.VERTICAL_SPACING;
    ((ScrolledComposite) fViewer.getControl()).setMinSize(size);
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.MIN | SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | getDefaultOrientation();

    return style;
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

    /* Keep or Hide Completed Downloads */
    fHideCompletedCheck = new Button(buttonBar, SWT.CHECK);
    fHideCompletedCheck.setText(Messages.ActivityDialog_REMOVE_COMPLETED);
    fHideCompletedCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fHideCompletedCheck.setSelection(fPreferences.getBoolean(DefaultPreferences.HIDE_COMPLETED_DOWNLOADS));
    fHideCompletedCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        final boolean hideCompleted = fHideCompletedCheck.getSelection();
        refreshProgressViewer(hideCompleted);
        JobRunner.runInBackgroundThread(new Runnable() {
          public void run() {
            fPreferences.putBoolean(DefaultPreferences.HIDE_COMPLETED_DOWNLOADS, hideCompleted);
          }
        });
      }
    });

    /* OK */
    createButton(buttonBar, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);

    return buttonBar;
  }

  @SuppressWarnings("restriction")
  private void refreshProgressViewer(boolean hideCompleted) {
    fViewer.setContentProvider(new org.eclipse.ui.internal.progress.ProgressViewerContentProvider(fViewer, false, !hideCompleted));
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
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();

    /* No dialog settings stored */
    if (fFirstTimeOpen) {
      Shell shell = getShell();

      /* Minimum Size */
      int minWidth = convertHorizontalDLUsToPixels(OwlUI.MIN_DIALOG_WIDTH_DLU);
      int minHeight = convertVerticalDLUsToPixels(MIN_DIALOG_HEIGHT_DLU);

      /* Required Size */
      Point requiredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

      shell.setSize(Math.max(minWidth, requiredSize.x), Math.max(minHeight, requiredSize.y));
      LayoutUtils.positionShell(shell);
    }
  }
}