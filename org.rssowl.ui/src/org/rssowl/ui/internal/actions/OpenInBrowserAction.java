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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.editors.browser.EmbeddedWebBrowser;
import org.rssowl.ui.internal.editors.browser.WebBrowserContext;
import org.rssowl.ui.internal.editors.browser.WebBrowserView;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author bpasero
 */
public class OpenInBrowserAction extends Action implements IWorkbenchWindowActionDelegate {

  /** Action ID */
  public static final String ID = "org.rssowl.ui.OpenInBrowserAction"; //$NON-NLS-1$

  private IStructuredSelection fSelection;
  private WebBrowserContext fContext;
  private boolean fForceOpenInBackground;

  /** Default Constructor for Reflection */
  public OpenInBrowserAction() {
    this(StructuredSelection.EMPTY);
  }

  /**
   * @param selection
   */
  public OpenInBrowserAction(IStructuredSelection selection) {
    this(selection, null);
  }

  /**
   * @param forceOpenInBackground if <code>true</code>, forces to open the
   * browser in the background and <code>false</code> otherwise asking the
   * global preferences.
   */
  public void setForceOpenInBackground(boolean forceOpenInBackground) {
    fForceOpenInBackground = forceOpenInBackground;
  }

  /**
   * @param selection
   * @param context
   */
  public OpenInBrowserAction(IStructuredSelection selection, WebBrowserContext context) {
    fSelection = selection;
    fContext = context;
    setText(Messages.OpenInBrowserAction_OPEN_IN_BROWSER);
    setId(ID);
    setActionDefinitionId(ID);
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  @Override
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  @Override
  public void init(IWorkbenchWindow window) {}

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run(IAction action) {
    run();
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    try {
      internalRun();
    } catch (URISyntaxException e) {
      Activator.getDefault().getLog().log(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    }
  }

  private void internalRun() throws URISyntaxException {
    String title = Messages.OpenInBrowserAction_LOADING;
    List<?> selection = fSelection.toList();
    for (Object object : selection) {
      URI link = null;

      /* News */
      if (object instanceof INews) {
        INews news = (INews) object;
        title = CoreUtils.getHeadline(news, true);

        String linkStr = CoreUtils.getLink(news);
        if (StringUtils.isSet(linkStr))
          link = new URI(linkStr);
      }

      /* URI */
      else if (object instanceof URI)
        link = (URI) object;

      /* String */
      else if (object instanceof String)
        link = new URI(URIUtils.fastEncode((String) object));

      if (link != null && link.isAbsolute()) {
        IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
        try {
          IWebBrowser browser = browserSupport.createBrowser(WebBrowserView.EDITOR_ID);

          /* Our own Web Browser Support is used */
          if (browser instanceof EmbeddedWebBrowser) {
            if (fContext != null)
              ((EmbeddedWebBrowser) browser).setContext(fContext);
            else
              ((EmbeddedWebBrowser) browser).setContext(WebBrowserContext.createFrom(title));

            try {
              ((EmbeddedWebBrowser) browser).openURL(link.toURL(), fForceOpenInBackground);
            } catch (MalformedURLException e) { //see Bug 1441
              ((EmbeddedWebBrowser) browser).openURL(link, fForceOpenInBackground);
            }
          }

          /* Any other Web Browser Support */
          else
            browser.openURL(link.toURL());
        } catch (PartInitException e) {
          Activator.getDefault().getLog().log(e.getStatus());
        } catch (MalformedURLException e) {
          Activator.getDefault().getLog().log(Activator.getDefault().createErrorStatus(e.getMessage(), e));
        }
      }
    }
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection)
      fSelection = (IStructuredSelection) selection;
  }
}