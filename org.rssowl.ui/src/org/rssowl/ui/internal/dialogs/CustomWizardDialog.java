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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * A subclass of {@link WizardDialog} with some application specific behavior.
 *
 * @author bpasero
 */
public class CustomWizardDialog extends WizardDialog {

  /**
   * @param parentShell the {@link Shell} to open the {@link Wizard} in.
   * @param wizard the {@link IWizard} making the contents of the wizard.
   */
  public CustomWizardDialog(Shell parentShell, IWizard wizard) {
    super(parentShell, wizard);
  }

  /*
   * @see org.eclipse.jface.wizard.WizardDialog#getButton(int)
   */
  @Override
  public Button getButton(int id) {
    return super.getButton(id);
  }
}