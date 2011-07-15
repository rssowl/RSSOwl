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

package org.rssowl.ui.internal;

import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.provider.IPreferencesContainer;
import org.eclipse.equinox.security.storage.provider.PasswordProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.rssowl.ui.internal.dialogs.MasterPasswordDialog;
import org.rssowl.ui.internal.util.JobRunner;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.PBEKeySpec;

/**
 * The default password provider will receive the master password by asking the
 * user from a dialog.
 *
 * @author bpasero
 */
@SuppressWarnings("restriction")
public class DefaultPasswordProvider extends PasswordProvider {
  private static final String DIGEST_ALGORITHM = "MD5"; //$NON-NLS-1$

  /*
   * @see org.eclipse.equinox.security.storage.provider.PasswordProvider#getPassword(org.eclipse.equinox.security.storage.provider.IPreferencesContainer, int)
   */
  @Override
  public PBEKeySpec getPassword(IPreferencesContainer container, final int passwordType) {
    if (!PlatformUI.isWorkbenchRunning())
      return null;

    final PBEKeySpec[] spec = new PBEKeySpec[1];
    final Shell activeShell = OwlUI.getActiveShell();

    JobRunner.runSyncedInUIThread(activeShell, new Runnable() {
      public void run() {
        MasterPasswordDialog dialog = new MasterPasswordDialog(activeShell, passwordType);
        if (dialog.open() == IDialogConstants.OK_ID) {
          String masterPassword = dialog.getMasterPassword();
          String internalPassword;

          /* Try using digest of what was entered */
          try {
            MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM);
            byte[] digested = digest.digest(masterPassword.getBytes());
            internalPassword = EncodingUtils.encodeBase64(digested);
          }

          /* Use the password as is bug log a warning */
          catch (NoSuchAlgorithmException e) {
            Activator.safeLogError(e.getMessage(), e);
            internalPassword = masterPassword;
          }

          spec[0] = new PBEKeySpec(internalPassword.toCharArray());
        }
      }
    });

    return spec[0];
  }
}