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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.rssowl.core.util.LoggingSafeRunnable;

/**
 * @author bpasero
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

  /* Default Perspective */
  private static final String PERSPECTIVE_ID = "org.rssowl.ui.perspective"; //$NON-NLS-1$

  /** Keep a static reference to the primary Workbench Window Advisor */
  public static ApplicationWorkbenchWindowAdvisor fgPrimaryApplicationWorkbenchWindowAdvisor;

  private final Runnable fRunAfterUIStartup;

  /**
   * @param runAfterUIStartup A <code>Runnable</code> to be executed after the
   * UI has been started and fully created.
   */
  public ApplicationWorkbenchAdvisor(Runnable runAfterUIStartup) {
    fRunAfterUIStartup = runAfterUIStartup;
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
   */
  @Override
  public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    ApplicationWorkbenchWindowAdvisor advisor = new ApplicationWorkbenchWindowAdvisor(configurer);

    /* Store primary advisor if not yet done */
    if (fgPrimaryApplicationWorkbenchWindowAdvisor == null)
      fgPrimaryApplicationWorkbenchWindowAdvisor = advisor;

    return advisor;
  }

  /* Provide access to the primary WorkbenchWindowAdvisor */
  ApplicationWorkbenchWindowAdvisor getPrimaryWorkbenchWindowAdvisor() {
    return fgPrimaryApplicationWorkbenchWindowAdvisor;
  }

  /**
   * The ID of the perspective that is initially shown when the Workbench shows.
   *
   * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
   */
  @Override
  public String getInitialWindowPerspectiveId() {
    return PERSPECTIVE_ID;
  }

  /*
   * @see org.eclipse.ui.application.WorkbenchAdvisor#getMainPreferencePageId()
   */
  @Override
  public String getMainPreferencePageId() {
    return "org.eclipse.ui.preferencePages.Workbench"; //$NON-NLS-1$
  }

  /**
   * !!! NOT YET USED !!! The default input for workbench pages if no input is
   * defined.
   *
   * @see org.eclipse.ui.application.WorkbenchAdvisor#getDefaultPageInput()
   */
  @Override
  public IAdaptable getDefaultPageInput() {
    return super.getDefaultPageInput();
  }

  /**
   * Possible to tweak the UI here before any Window has opened.
   * <p>
   * <cite>This marks the beginning of the advisor's lifecycle and is called
   * during Workbench initialization prior to any windows being opened. This is
   * a good place to parse the command line and register adaptors.</cite>
   * </p>
   *
   * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
   */
  @Override
  public void initialize(IWorkbenchConfigurer configurer) {
    IWorkbenchConfigurer workbenchConfigurer = getWorkbenchConfigurer();

    /* Save UI state and restore after restart */
    workbenchConfigurer.setSaveAndRestore(true);

    super.initialize(configurer);
  }

  /**
   * This method is called just after the windows have been opened.
   * <p>
   * <cite>Performs arbitrary actions after the Workbench windows have been
   * opened or restored, but before the main event loop is run. This is a good
   * place to start any background jobs such as auto-update daemons</cite>
   * </p>
   *
   * @see org.eclipse.ui.application.WorkbenchAdvisor#postStartup()
   */
  @Override
  public void postStartup() {
    super.postStartup();

    /* Run Runnable if provided */
    if (fRunAfterUIStartup != null) {
      SafeRunner.run(new LoggingSafeRunnable() {
        public void run() throws Exception {
          fRunAfterUIStartup.run();
        }
      });
    }
  }

  /**
   * This method is called immediately prior to workbench shutdown before any
   * windows have been closed.
   * <p>
   * <cite>Called immediately prior to Workbench shutdown before any windows
   * have been closed. The advisor may veto a regular shutdown by returning
   * false. Advisors should check
   * <code>IWorkbenchConfigurer.emergencyClosing()</code> before attempting to
   * communicate with the user. </cite>
   * </p>
   *
   * @see org.eclipse.ui.application.WorkbenchAdvisor#preShutdown()
   */
  @Override
  public boolean preShutdown() {
    final boolean res[] = new boolean[] { true };

    /* Pre-Shutdown Controller */
    SafeRunner.run(new LoggingSafeRunnable() {
      public void run() throws Exception {
        res[0] = Controller.getDefault().preUIShutdown();
      }
    });

    return res[0];
  }
}