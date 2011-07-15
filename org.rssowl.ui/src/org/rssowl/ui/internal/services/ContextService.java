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

package org.rssowl.ui.internal.services;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * The <code>ContextService</code> is responsible to change the context
 * depending on certain events in the UI. Contexts control enablement of
 * key-bindings.
 * 
 * @author bpasero
 */
public class ContextService implements FocusListener {

  /* ID of the RSSOwl Navigation Context */
  private static final String RSSOWL_NAVIGATION_CONTEXT = "org.rssowl.ui.NavigationContext"; //$NON-NLS-1$

  private IContextActivation fActivateContext;
  private IContextService fContextService;
  private boolean fNavigationContextActive;

  /** Creates a new Instance of this Service */
  public ContextService() {
    fContextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);

    /* Activate the managed Context */
    activateNavigationContext();
  }

  /**
   * Registers an Input-Field to the service. Depending on the field having
   * focus or not, the Context may change.
   * 
   * @param control The Input-Field to register to the service.
   */
  public void registerInputField(Control control) {
    control.addFocusListener(this);
  }

  /*
   * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
   */
  public void focusGained(FocusEvent e) {
    deactivateNavigationContext();
  }

  /*
   * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
   */
  public void focusLost(FocusEvent e) {
    activateNavigationContext();
  }

  /* Activates the Navigation Context (Key Bindings) */
  private void activateNavigationContext() {
    if (fNavigationContextActive)
      return;

    fActivateContext = fContextService.activateContext(RSSOWL_NAVIGATION_CONTEXT);
    fNavigationContextActive = true;
  }

  /* Deactivates the Navigation Context (Key Bindings) */
  private void deactivateNavigationContext() {
    if (!fNavigationContextActive)
      return;

    fContextService.deactivateContext(fActivateContext);
    fNavigationContextActive = false;
  }
}