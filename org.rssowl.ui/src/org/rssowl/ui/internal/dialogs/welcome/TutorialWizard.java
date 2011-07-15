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
import org.rssowl.ui.internal.dialogs.welcome.TutorialPage.Chapter;

/**
 * A {@link Wizard} that helps the user understand how to use RSSOwl.
 *
 * @author bpasero
 */
public class TutorialWizard extends Wizard {
  private final Chapter fStartingPage;

  public TutorialWizard() {
    this(Chapter.INTRO);
  }

  public TutorialWizard(Chapter startingPage) {
    fStartingPage = startingPage;
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    setWindowTitle(Messages.TutorialWizard_RSSOWL_TUTORIAL);

    /* Add Tutorial Chapters */
    addPage(new TutorialPage(Chapter.INTRO));
    addPage(new TutorialPage(Chapter.LAYOUT));
    addPage(new TutorialPage(Chapter.NEWS));
    addPage(new TutorialPage(Chapter.SAVEDSEARCH));
    addPage(new TutorialPage(Chapter.NEWSBIN));
    addPage(new TutorialPage(Chapter.NEWSFILTER));
    addPage(new TutorialPage(Chapter.NOTIFIER));
    addPage(new TutorialPage(Chapter.SHARING));
    addPage(new TutorialPage(Chapter.IMPORT_EXPORT));
    addPage(new TutorialPage(Chapter.SYNCHRONIZATION));
    addPage(new TutorialPage(Chapter.PREFERENCES));
    addPage(new TutorialPage(Chapter.TIPS));
    addPage(new TutorialPage(Chapter.FINISH));
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
   */
  @Override
  public IWizardPage getStartingPage() {
    if (fStartingPage != Chapter.INTRO) {
      IWizardPage[] pages = getPages();
      if (pages.length > fStartingPage.ordinal())
        return pages[fStartingPage.ordinal()];
    }

    return super.getStartingPage();
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    return true;
  }
}