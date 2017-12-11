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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.actions.NewBookMarkAction;
import org.rssowl.ui.internal.dialogs.fatal.FatalErrorWizard;
import org.rssowl.ui.internal.util.JobRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
  private ApplicationWorkbenchAdvisor fWorkbenchAdvisor;

  /** Constant for the application being run on Windows or not */
  public static final boolean IS_WINDOWS = "win32".equals(SWT.getPlatform()); //$NON-NLS-1$

  /** Constant for the application being run on Linux or not */
  public static final boolean IS_LINUX = "gtk".equals(SWT.getPlatform()); //$NON-NLS-1$

  /** Constant for the application being run on Mac or not */
  public static final boolean IS_MAC = "carbon".equals(SWT.getPlatform()); //$NON-NLS-1$

  /** Flag to indicate RSSOwl integrated to Eclipse or not */
  public static final boolean IS_ECLIPSE = InternalOwl.IS_ECLIPSE;

  /* System Property to force opening of the restore profile wizard */
  private static final String OPEN_RESTORE_WIZARD_PROPERTY = "restoreOwlProfile"; //$NON-NLS-1$

  /*
   * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
   */
  @Override
  public Object start(IApplicationContext context) throws Exception {

    /* Set Handshake-Handler to Application Server */
    ApplicationServer server = ApplicationServer.getDefault();
    server.setHandshakeHandler(new ApplicationServer.HandshakeHandler() {
      @Override
      public void handle(String token) {
        if (StringUtils.isSet(token)) {
          restoreApplication();

          if (hasProtocolHandler(token))
            handleLinkSupplied(token);
        }
      }
    });

    /* Proceed normally */
    Display display = PlatformUI.createDisplay();
    try {

      /* Handle possible Link supplied after startup */
      Runnable runAfterUIStartup = new Runnable() {
        @Override
        public void run() {
          String link = parseLink(Platform.getCommandLineArgs());
          if (StringUtils.isSet(link))
            handleLinkSupplied(link);
        }
      };

      /* Check Startup Status */
      Activator activator = Activator.getDefault();
      IStatus startupStatus = activator.getStartupStatus();
      if (startupStatus.getSeverity() == IStatus.ERROR)
        return handleStartupError(startupStatus, false);

      /* Open Fatal Error Wizard if user explicitly asked for it (to restore a backup) */
      if (System.getProperty(OPEN_RESTORE_WIZARD_PROPERTY) != null)
        return handleStartupError(startupStatus, true);

      /* Create the Workbench */
      fWorkbenchAdvisor = new ApplicationWorkbenchAdvisor(runAfterUIStartup);
      int returnCode = PlatformUI.createAndRunWorkbench(display, fWorkbenchAdvisor);
      if (returnCode == PlatformUI.RETURN_RESTART)
        return IApplication.EXIT_RESTART;

      return IApplication.EXIT_OK;
    } finally {
      display.dispose();
    }
  }

  private int handleStartupError(IStatus errorStatus, boolean forceAllowRestore) {
    FatalErrorWizard wizard = new FatalErrorWizard(errorStatus, forceAllowRestore);
    OwlUI.openWizard(null, wizard, true, false, null, true, IS_WINDOWS && !forceAllowRestore ? Messages.Application_RESTART_RSSOWL : Messages.Application_QUIT_RSSOWL);

    return wizard.getReturnCode();
  }

  private boolean hasProtocolHandler(String link) {

    /* Is empty or null? */
    if (!StringUtils.isSet(link))
      return false;

    try {
      return Owl.getConnectionService().getHandler(new URI(URIUtils.fastEncode(link))) != null;
    } catch (ConnectionException e) {
      return false;
    } catch (URISyntaxException e) {
      return false;
    }
  }

  /*
   * @see org.eclipse.equinox.app.IApplication#stop()
   */
  @Override
  public void stop() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    if (workbench == null)
      return;

    final Display display = workbench.getDisplay();
    display.syncExec(new Runnable() {
      @Override
      public void run() {
        if (!display.isDisposed())
          workbench.close();
      }
    });
  }

  /* Return the first Link in this Array or NULL otherwise */
  private String parseLink(String[] commandLineArgs) {
    for (String arg : commandLineArgs) {
      if (hasProtocolHandler(arg))
        return arg;
    }

    return null;
  }

  /* Focus the Application */
  private void restoreApplication() {
    final Shell shell = OwlUI.getPrimaryShell();
    if (shell != null) {
      JobRunner.runInUIThread(shell, new Runnable() {
        @Override
        public void run() {

          /* Restore from Tray */
          ApplicationWorkbenchWindowAdvisor advisor = fWorkbenchAdvisor.getPrimaryWorkbenchWindowAdvisor();
          if (advisor != null && advisor.isMinimizedToTray()) {
            advisor.restoreFromTray(shell);
          }

          /* Force Active and De-Iconify */
          else {
            shell.forceActive();
            shell.setMinimized(false);
          }
        }
      });
    }
  }

  /* Handle the supplied Link */
  private void handleLinkSupplied(final String link) {

    /* Need a Shell */
    final Shell shell = OwlUI.getPrimaryShell();
    if (shell == null)
      return;

    /* Bug with Firefox: HTTPS feeds start with "feed:https://" */
    final String normalizedLink;
    if (link.startsWith(URIUtils.FEED_IDENTIFIER + URIUtils.HTTPS))
      normalizedLink = URIUtils.HTTPS + link.substring((URIUtils.FEED_IDENTIFIER + URIUtils.HTTPS).length());
    else
      normalizedLink = link;

    /* Check for existing BookMark */
    final IBookMark existingBookMark = getBookMark(normalizedLink);
    JobRunner.runInUIThread(shell, new Runnable() {
      @Override
      public void run() {

        /* Open Dialog to add this new BookMark */
        if (existingBookMark == null) {
          new NewBookMarkAction(shell, null, null, normalizedLink).run(null);
        }

        /* Display selected Feed since its existing already */
        else {
          IWorkbenchPage page = OwlUI.getPage();
          if (page != null)
            OwlUI.openInFeedView(page, new StructuredSelection(existingBookMark));
        }
      }
    });
  }

  private IBookMark getBookMark(String link) {

    /* Need a URI */
    URI linkAsURI;
    try {
      linkAsURI = new URI(URIUtils.fastEncode(link));
    } catch (URISyntaxException e) {
      return null;
    }

    /* Check if a BookMark exists for the Link */
    Collection<IBookMark> existingBookmarks = DynamicDAO.getDAO(IBookMarkDAO.class).loadAll(new FeedLinkReference(linkAsURI));
    if (existingBookmarks.size() > 0)
      return existingBookmarks.iterator().next();

    /* Try again swapping feed:// with http:// and vice versa */
    if (link.startsWith(URIUtils.FEED) || link.startsWith(URIUtils.HTTP)) {
      if (link.startsWith(URIUtils.FEED))
        link = URIUtils.HTTP + link.substring(URIUtils.FEED.length());
      else if (link.startsWith(URIUtils.HTTP))
        link = URIUtils.FEED + link.substring(URIUtils.HTTP.length());

      try {
        linkAsURI = new URI(URIUtils.fastEncode(link));
      } catch (URISyntaxException e) {
        return null;
      }

      existingBookmarks = DynamicDAO.getDAO(IBookMarkDAO.class).loadAll(new FeedLinkReference(linkAsURI));
      if (existingBookmarks.size() > 0)
        return existingBookmarks.iterator().next();
    }

    return null;
  }

  /**
   * @return <code>true</code> if the host os is Windows 7.
   */
  public static boolean isWindows7() {
    return "6.1".equals(System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
  }
}