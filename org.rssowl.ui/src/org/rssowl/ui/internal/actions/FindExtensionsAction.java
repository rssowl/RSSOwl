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

package org.rssowl.ui.internal.actions;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.search.BackLevelFilter;
import org.eclipse.update.search.EnvironmentFilter;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.rssowl.core.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author bpasero
 */
public class FindExtensionsAction extends Action implements IWorkbenchWindowActionDelegate {

  /* RSSOwl.org Update Site */
  private static final String UPDATE_SITE = "http://boreal.rssowl.org/update/addons/"; //$NON-NLS-1$

  /* System Property to Control Sites for Extensions to look for */
  private static final String EXTENSION_SITES_PROPERTY = "addonSites"; //$NON-NLS-1$

  /* Used in the Extension Sites System Property to distinguish mutliple sites from each other */
  private static final String EXTENSION_SITES_DIVIDER = "\\|"; //$NON-NLS-1$

  private Shell fShell;

  /** Keep default constructor for reflection. */
  public FindExtensionsAction() {}

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    BusyIndicator.showWhile(fShell.getDisplay(), new Runnable() {
      public void run() {
        UpdateJob job = new UpdateJob(Messages.FindExtensionsAction_SEARCHING_EXTENSIONS, getSearchRequest());
        job.setUser(true);
        job.setPriority(Job.INTERACTIVE);
        new InstallWizardOperation().run(fShell, job);
      }
    });
  }

  UpdateSearchRequest getSearchRequest() {
    UpdateSearchScope scope = new UpdateSearchScope();

    /* Check for User Defined Sites from System Property */
    String extensionSites = System.getProperty(EXTENSION_SITES_PROPERTY);
    if (StringUtils.isSet(extensionSites)) {
      String[] sites = extensionSites.split(EXTENSION_SITES_DIVIDER);
      for (String site : sites) {
        try {
          URL url = new URL(site);
          scope.addSearchSite(url.toString(), url, null);
        } catch (MalformedURLException e) {
          // skip bad URLs
        }
      }
    }

    /* Add RSSOwl.org if user did not define any other sites */
    if (scope.getSearchSites().length == 0) {
      try {
        URL url = new URL(UPDATE_SITE);
        scope.addSearchSite("RSSOwl.org", url, null); //$NON-NLS-1$
      } catch (MalformedURLException e) {
        // skip bad URLs
      }
    }

    UpdateSearchRequest result = new UpdateSearchRequest(UpdateSearchRequest.createDefaultSiteSearchCategory(), scope);
    result.addFilter(new BackLevelFilter());
    result.addFilter(new EnvironmentFilter());
    return result;
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    fShell = window.getShell();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    run();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {}

  /*
   * Class copied intentionally to override some UI specific settings.
   */
  @SuppressWarnings("restriction")
  private static class InstallWizardOperation {
    private UpdateJob fJob;
    private IJobChangeListener fJobListener;
    private Shell fShell;
    private Shell fParentShell;

    void run(Shell parent, UpdateJob task) {
      fShell = parent;

      if (fShell.getParent() != null && fShell.getParent() instanceof Shell)
        fParentShell = (Shell) fShell.getParent();

      if (fJobListener != null)
        Job.getJobManager().removeJobChangeListener(fJobListener);

      if (fJob != null)
        Job.getJobManager().cancel(fJob);

      fJob = task;
      fJobListener = new UpdateJobChangeListener();
      Job.getJobManager().addJobChangeListener(fJobListener);
      fJob.schedule();
    }

    private Shell getValidShell() {
      if (fShell.isDisposed())
        return fParentShell;

      return fShell;
    }

    private class UpdateJobChangeListener extends JobChangeAdapter {
      @Override
      public void done(final IJobChangeEvent event) {
        final Shell validShell = getValidShell();

        if (event.getJob() == fJob) {
          Job.getJobManager().removeJobChangeListener(this);
          Job.getJobManager().cancel(fJob);
          if (fJob.getStatus() == Status.CANCEL_STATUS)
            return;

          if (fJob.getStatus() != Status.OK_STATUS)
            getValidShell().getDisplay().syncExec(new Runnable() {
              public void run() {
                org.eclipse.update.internal.ui.UpdateUI.log(fJob.getStatus(), true);
              }
            });

          validShell.getDisplay().asyncExec(new Runnable() {
            public void run() {
              validShell.getDisplay().beep();
              BusyIndicator.showWhile(validShell.getDisplay(), new Runnable() {
                public void run() {
                  openInstallWizard2();
                }
              });
            }
          });
        }
      }

      private void openInstallWizard2() {
        if (org.eclipse.update.internal.ui.wizards.InstallWizard2.isRunning()) {
          MessageDialog.openInformation(getValidShell(), Messages.FindExtensionsAction_FIND_ADDONS, Messages.FindExtensionsAction_UPDATE_IN_PROGRESS);
          return;
        }

        if (fJob.getUpdates() == null || fJob.getUpdates().length == 0) {
          if (fJob.isUpdate())
            MessageDialog.openInformation(getValidShell(), Messages.FindExtensionsAction_FIND_ADDONS, Messages.FindExtensionsAction_NO_UPDATES_FOUND);
          else
            MessageDialog.openInformation(getValidShell(), Messages.FindExtensionsAction_FIND_ADDONS, Messages.FindExtensionsAction_NO_ADDONS_FOUND);

          return;
        }

        org.eclipse.update.internal.ui.wizards.InstallWizard2 wizard = new org.eclipse.update.internal.ui.wizards.InstallWizard2(fJob.getSearchRequest(), fJob.getUpdates(), fJob.isUpdate());
        WizardDialog dialog = new org.eclipse.update.internal.ui.wizards.ResizableInstallWizardDialog(getValidShell(), wizard, Messages.FindExtensionsAction_RSSOWL_ADDONS);
        dialog.create();

        /* A little hack to improve the UI of the Add-on Wizard */
        if (dialog.getCurrentPage() != null) {
          Control control = dialog.getCurrentPage().getControl();
          if (control != null && !control.isDisposed() && control instanceof Composite) {
            Composite container = ((Composite) control);
            Control[] children = container.getChildren();
            if (children != null && children.length == 1 && children[0] instanceof Composite) {
              container = (Composite) children[0];
              children = container.getChildren();
              if (children.length > 2) {

                /* Sash and Tree */
                if (children[1] instanceof SashForm) {
                  SashForm form = (SashForm) children[1];
                  form.setWeights(new int[] { 70, 30 });

                  Control[] formChilds = form.getChildren();
                  if (formChilds.length != 0 && formChilds[0] instanceof Tree) {
                    Tree tree = (Tree) formChilds[0];
                    if (tree.getItemCount() != 0) {
                      TreeItem root = tree.getItem(0);
                      root.setExpanded(true);
                    }
                  }
                }
              }
            }
          }
        }

        dialog.open();
      }
    }
  }
}