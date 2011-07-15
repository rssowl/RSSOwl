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

package org.rssowl.ui.internal.editors.browser;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport;
import org.eclipse.ui.browser.IWebBrowser;

/**
 * RSSOwl's own support for an embedded Browser. Will respect the "Use external
 * Browser" setting to open a Link externally if set.
 *
 * @author bpasero
 */
public class WebBrowserSupport extends AbstractWorkbenchBrowserSupport {

  /** Leave Default Constructor for Reflection */
  public WebBrowserSupport() {}

  /*
   * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(java.lang.String)
   */
  public IWebBrowser createBrowser(final String browserId) {
    Assert.isNotNull(browserId);

    /* Create WebBrowser and return */
    return new EmbeddedWebBrowser(browserId);
  }

  /*
   * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(int,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public IWebBrowser createBrowser(int style, String browserId, String name, String tooltip) {
    return createBrowser(browserId);
  }

  /*
   * @see org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport#isInternalWebBrowserAvailable()
   */
  @Override
  public boolean isInternalWebBrowserAvailable() {
    return true;
  }
}