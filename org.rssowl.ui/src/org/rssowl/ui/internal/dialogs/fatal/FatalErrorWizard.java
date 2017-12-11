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

package org.rssowl.ui.internal.dialogs.fatal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.rssowl.core.Owl;
import org.rssowl.core.Owl.StartLevel;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.persist.service.ProfileLockedException;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.ImportUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link FatalErrorWizard} shows up when RSSOwl crashed during startup in a
 * fatal, unrecoverable way.
 *
 * @author bpasero
 */
public class FatalErrorWizard extends Wizard {
  private ErrorInfoPage fErrorInfoPage;
  private RestoreBackupPage fRestoreBackupPage;
  private CleanProfilePage fCleanProfilePage;
  private RecreateSearchIndexPage fReindexSearchPage;
  private final IStatus fErrorStatus;
  private int fReturnCode = IApplication.EXIT_OK;
  private final List<File> fProfileBackups = new ArrayList<File>();
  private final List<File> fOPMLBackups = new ArrayList<File>();
  private boolean fOfferRestorePages;

  public FatalErrorWizard(IStatus errorStatus) {
    this(errorStatus, false);
  }

  public FatalErrorWizard(IStatus errorStatus, boolean forceAllowRestore) {
    fErrorStatus = errorStatus;

    boolean isOOMError = (fErrorStatus.getException() instanceof OutOfMemoryError);
    boolean isProfileLockedError = (fErrorStatus.getException() instanceof ProfileLockedException);
    boolean canUsePersistenceService = (InternalOwl.getDefault().getPersistenceService() != null);
    StartLevel startLevel = InternalOwl.getDefault().getStartLevel();
    fOfferRestorePages = !isOOMError && !isProfileLockedError && canUsePersistenceService && startLevel != StartLevel.STARTED && startLevel != StartLevel.SEARCH_INDEX_OPENED;

    /* Log State */
    if (forceAllowRestore)
      Activator.safeLogInfo("Opening Fatal Error Wizard (forced by user)"); //$NON-NLS-1$
    else if (!isOOMError && !isProfileLockedError)
      Activator.safeLogInfo(fOfferRestorePages ? "Opening Fatal Error Wizard (offering restore options)" : "Opening Fatal Error Wizard (without restore options)"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Check if caller wants to force profile restore pages */
    if (!fOfferRestorePages && forceAllowRestore && canUsePersistenceService)
      fOfferRestorePages = true;

    /* Search for backups as necessary */
    if (fOfferRestorePages)
      findBackups();
  }

  private void findBackups() {

    /* Collect Profile Backups */
    fProfileBackups.addAll(InternalOwl.getDefault().getProfileBackups());

    /* Collect OPML Backups if no profile backups can be found */
    if (fProfileBackups.isEmpty()) {
      IPath backPath = Platform.getLocation();
      File backupDir = backPath.toFile();
      if (backupDir.exists()) {

        /* Daily OPML Backup */
        File dailyBackupFile = backPath.append(Controller.DAILY_BACKUP).toFile();
        if (dailyBackupFile.exists())
          fOPMLBackups.add(dailyBackupFile);

        /* Weekly OPML Backup */
        File weeklyBackupFile = backPath.append(Controller.WEEKLY_BACKUP).toFile();
        if (weeklyBackupFile.exists())
          fOPMLBackups.add(weeklyBackupFile);
      }
    }
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    setWindowTitle(fErrorStatus.isOK() ? Messages.FatalErrorWizard_PROFILE_RECOVERY : Messages.FatalErrorWizard_CRASH_REPORTER);
    setHelpAvailable(false);

    /* Error Info (not if wizard was forced to open by user) */
    if (!fErrorStatus.isOK() || !fOfferRestorePages) {
      fErrorInfoPage = new ErrorInfoPage(Messages.FatalErrorWizard_WE_ARE_SORRY, fErrorStatus, fOfferRestorePages);
      addPage(fErrorInfoPage);
    }

    /* Add Restore Pages as necessary */
    if (fOfferRestorePages) {

      /* Not an actual issue with the DB, rather search, so provide reindex page */
      if (Owl.getStartLevel() == StartLevel.DB_OPENED) {
        fReindexSearchPage = new RecreateSearchIndexPage(Messages.FatalErrorWizard_RECREATE_SEARCH_INDEX);
        addPage(fReindexSearchPage);
      }

      /* Restore Profile Backup (if profile backups are present) */
      else if (!fProfileBackups.isEmpty()) {
        fRestoreBackupPage = new RestoreBackupPage(Messages.FatalErrorWizard_RESTORE_BACKUP, fErrorStatus, fProfileBackups);
        addPage(fRestoreBackupPage);
      }

      /* Otherwise allow to restore from OPML Backup or clean start */
      else {
        fCleanProfilePage = new CleanProfilePage(fOPMLBackups.isEmpty() ? Messages.FatalErrorWizard_START_OVER : Messages.FatalErrorWizard_RESTORE_SUBSCRIPTIONS_SETTINGS, fErrorStatus, !fOPMLBackups.isEmpty());
        addPage(fCleanProfilePage);
      }
    }
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {

    /* Finish */
    try {
      BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
        @Override
        public void run() {
          internalPerformFinish();
        }
      });
    } catch (Throwable e) {
      Activator.getDefault().logError(e.getMessage(), e);

      /* Show Error to the User */
      String msg;
      if (StringUtils.isSet(e.getMessage()))
        msg = NLS.bind(Messages.FatalErrorWizard_RESTORE_ERROR_N, e.getMessage());
      else
        msg = Messages.FatalErrorWizard_RESTORE_ERROR;

      ((WizardPage) getContainer().getCurrentPage()).setMessage(msg, IMessageProvider.ERROR);

      return false;
    }

    /* Windows: Support to restart from dialog */
    if (Application.IS_WINDOWS && !fErrorStatus.isOK()) //Status only OK if user forced dialog, quit in this case
      fReturnCode = IApplication.EXIT_RESTART;

    return true;
  }

  private void internalPerformFinish() throws PersistenceException {

    /* Recreate Search Index */
    if (fReindexSearchPage != null) {
      InternalOwl.getDefault().getPersistenceService().getModelSearch().reIndexOnNextStartup();
    }

    /* Restore Profile from Backup */
    else if (fRestoreBackupPage != null && fRestoreBackupPage.getSelectedBackup() != null) {
      InternalOwl.getDefault().restoreProfile(fRestoreBackupPage.getSelectedBackup());
    }

    /* Clean Profile */
    else if (fCleanProfilePage != null && fCleanProfilePage.doCleanProfile()) {

      /* Recreate the Profile */
      boolean needsEmergencyStartup = !fOPMLBackups.isEmpty();
      InternalOwl.getDefault().recreateProfile(needsEmergencyStartup);

      /* Try to Import from OPML backups if present */
      if (!fOPMLBackups.isEmpty()) {
        List<? extends IEntity> types = null;

        /* First Try Daily Backup */
        File recentBackup = fOPMLBackups.get(0);
        try {
          types = Owl.getInterpreter().importFrom(new FileInputStream(recentBackup));
        } catch (Exception e) {
          if (fOPMLBackups.size() == 1)
            throw new PersistenceException(e.getMessage(), e);
        }

        /* Second Try Weekly Backup */
        if (types == null && fOPMLBackups.size() == 2) {
          File weeklyBackup = fOPMLBackups.get(1);
          try {
            types = Owl.getInterpreter().importFrom(new FileInputStream(weeklyBackup));
          } catch (Exception e) {
            throw new PersistenceException(e.getMessage(), e);
          }
        }

        /* Do Import */
        if (types != null)
          ImportUtils.doImport(null, types, false);
      }

      /* Clear Stored Favicons (since Ids change after import) */
      OwlUI.clearFavicons();
    }
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#canFinish()
   */
  @Override
  public boolean canFinish() {

    /* Make sure user is on last page to Finish */
    if (fRestoreBackupPage != null && getContainer().getCurrentPage() != fRestoreBackupPage)
      return false;
    else if (fCleanProfilePage != null && getContainer().getCurrentPage() != fCleanProfilePage)
      return false;
    else if (fReindexSearchPage != null && getContainer().getCurrentPage() != fReindexSearchPage)
      return false;

    /* Other Pages decide on their own */
    return super.canFinish();
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#needsProgressMonitor()
   */
  @Override
  public boolean needsProgressMonitor() {
    return false;
  }

  /**
   * @return one of the {@link IApplication} return codes.
   */
  public int getReturnCode() {
    return fReturnCode;
  }
}