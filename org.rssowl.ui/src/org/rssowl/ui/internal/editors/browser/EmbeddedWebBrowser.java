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
import org.eclipse.ui.browser.IWebBrowser;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.BrowserUtils;

import java.net.URI;
import java.net.URL;

/**
 * The embedded web browser used from {@link WebBrowserSupport} in RSSOwl.
 *
 * @author bpasero
 */
public class EmbeddedWebBrowser implements IWebBrowser {
  private final String fBrowserId;
  private WebBrowserContext fContext;

  /**
   * @param browserId
   */
  public EmbeddedWebBrowser(String browserId) {
    this(browserId, null);
  }

  /**
   * @param browserId
   * @param context
   */
  public EmbeddedWebBrowser(String browserId, WebBrowserContext context) {
    fBrowserId = browserId;
    fContext = context;
  }

  /**
   * @param context the context from which this browser was created or
   * <code>null</code> if none.
   */
  public void setContext(WebBrowserContext context) {
    fContext = context;
  }

  /*
   * @see org.eclipse.ui.browser.IWebBrowser#openURL(java.net.URL)
   */
  @Override
  public void openURL(URL url) {
    openURL(url, false);
  }

  /**
   * @param url the {@link URL} to open in either the external or internal
   * browser.
   * @param forceOpenInBackground if <code>true</code>, forces to open the
   * browser in the background and <code>false</code> otherwise asking the
   * global preferences.
   */
  public void openURL(URL url, boolean forceOpenInBackground) {
    Assert.isNotNull(url);

    /* Open externally */
    if (OwlUI.useExternalBrowser())
      openExternal(url);

    /* Open internally */
    else
      BrowserUtils.openLinkInternal(url.toExternalForm(), fContext, forceOpenInBackground);
  }

  /**
   * @param uri the {@link URI} to open in either the external or internal
   * browser.
   */
  public void openURL(URI uri) {
    openURL(uri, false);
  }

  /**
   * @param uri the {@link URI} to open in either the external or internal
   * browser.
   * @param forceOpenInBackground if <code>true</code>, forces to open the
   * browser in the background and <code>false</code> otherwise asking the
   * global preferences.
   */
  public void openURL(URI uri, boolean forceOpenInBackground) {
    Assert.isNotNull(uri);

    /* Open externally */
    if (OwlUI.useExternalBrowser())
      openExternal(uri);

    /* Open internally */
    else
      BrowserUtils.openLinkInternal(uri.toString(), fContext, forceOpenInBackground);
  }

  private void openExternal(URL url) {
    BrowserUtils.openLinkExternal(url.toExternalForm());
  }

  private void openExternal(URI uri) {
    BrowserUtils.openLinkExternal(uri.toString());
  }

  /*
   * @see org.eclipse.ui.browser.IWebBrowser#close()
   */
  @Override
  public boolean close() {
    return true;
  }

  /*
   * @see org.eclipse.ui.browser.IWebBrowser#getId()
   */
  @Override
  public String getId() {
    return fBrowserId;
  }
}