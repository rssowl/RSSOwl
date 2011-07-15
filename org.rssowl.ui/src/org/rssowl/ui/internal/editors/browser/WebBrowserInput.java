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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.OwlUI;

/**
 * The <code>WebBrowserInput</code> is used as Input to the
 * <code>WebBrowserView</code> in order to display a website.
 *
 * @author bpasero
 */
public class WebBrowserInput implements IEditorInput {
  private static final String FACTORY_ID = "org.rssowl.ui.WebBrowserViewFactory"; //$NON-NLS-1$
  static final String URL = "org.rssowl.ui.internal.editors.browser.Url"; //$NON-NLS-1$

  private final String fUrl;
  private final WebBrowserContext fContext;
  private String fCurrentUrl;

  /**
   * @param url
   */
  public WebBrowserInput(String url) {
    this(url, null);
  }

  /**
   * @param url
   * @param context
   */
  public WebBrowserInput(String url, WebBrowserContext context) {
    fUrl = url;
    fContext = context;
  }

  /**
   * @return The URL that is to to open as <code>String</code> or
   * <code>null</code> if none.
   */
  public String getUrl() {
    return fUrl;
  }

  /**
   * @param url The current URL as <code>String</code> or <code>null</code> if
   * none.
   */
  public void setCurrentUrl(String url) {
    fCurrentUrl = url;
  }

  /**
   * @return the context from which this web browser input was created from or
   * <code>null</code> if none.
   */
  public WebBrowserContext getContext() {
    return fContext;
  }

  /*
   * @see org.eclipse.ui.IEditorInput#exists()
   */
  public boolean exists() {
    return true;
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
   */
  public ImageDescriptor getImageDescriptor() {
    return OwlUI.getImageDescriptor("icons/eview16/webbrowser.gif"); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getName()
   */
  public String getName() {
    return fUrl != null ? fUrl : Messages.WebBrowserInput_LOADING;
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getPersistable()
   */
  public IPersistableElement getPersistable() {
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();

    if (OwlUI.useExternalBrowser())
      return null;

    boolean restore = preferences.getBoolean(DefaultPreferences.REOPEN_BROWSER_TABS);
    if (!restore)
      return null;

    return new IPersistableElement() {
      public String getFactoryId() {
        return FACTORY_ID;
      }

      public void saveState(IMemento memento) {
        memento.putString(URL, fCurrentUrl != null ? fCurrentUrl : fUrl);
      }
    };
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getToolTipText()
   */
  public String getToolTipText() {
    return ""; //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }
}