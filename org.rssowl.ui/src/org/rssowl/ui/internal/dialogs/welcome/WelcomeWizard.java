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

package org.rssowl.ui.internal.dialogs.welcome;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.rssowl.ui.internal.dialogs.importer.ImportSourcePage;
import org.rssowl.ui.internal.dialogs.importer.ImportWizard;
import org.rssowl.ui.internal.dialogs.importer.ImportSourcePage.Source;

/**
 * A {@link Wizard} that shows upon first start of RSSOwl introducing to the
 * user. It is basically the {@link ImportWizard} with some additional pages for
 * guidance.
 *
 * @author bpasero
 */
public class WelcomeWizard extends ImportWizard {

  /** Default Constructor */
  public WelcomeWizard() {
    super(true);
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    super.addPages();
    setWindowTitle(Messages.WelcomeWizard_WELCOME);
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.importer.ImportWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
   */
  @Override
  public IWizardPage getNextPage(IWizardPage page) {

    /* Directly Finish if user is not selecting to import Feeds */
    if (page instanceof ImportSourcePage && ((ImportSourcePage) page).getSource() == Source.NONE)
      return null;

    return super.getNextPage(page);
  }
}