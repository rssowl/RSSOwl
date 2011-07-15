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

package org.rssowl.ui.dialogs.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.rssowl.core.persist.IEntity;

import java.util.List;
import java.util.Set;

/**
 * Instances of <code>IEntityPropertyPage</code> provide a Property-Page for
 * entites. These pages are created in the Property-Dialog and participate in
 * the dialog lifecycle.
 * <p>
 * Contributed via <code>org.rssowl.ui.EntityPropertyPage</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface IEntityPropertyPage {

  /**
   * Initializes this page with the selected Entities of the Property-Dialog.
   *
   * @param site The dialog-site providing some useful methods.
   * @param entities The selected Entities of this Property-Dialog.
   */
  void init(IPropertyDialogSite site, List<IEntity> entities);

  /**
   * Creates and returns the SWT control for the customized body of this
   * property page under the given parent composite. This method is called
   * when the tab is selected this page belongs to.
   *
   * @param parent the parent composite
   * @return the new control
   */
  Control createContents(Composite parent);

  /**
   * @return the image to show for the property page or <code>null</code> if
   * none.
   */
  ImageDescriptor getImage();

  /**
   * Asks to focus the given <code>IEntityPropertyPage</code>.
   */
  void setFocus();

  /**
   * Notifies that the OK button of this page's container has been pressed.
   *
   * @param entitiesToSave A Set of <code>IEntity</code>s which are to be saved.
   * This way of collecting entities for saving avoids the situation where two
   * different pages wants to save the same entity.
   * @return <code>false</code> to abort the container's OK processing and
   * <code>true</code> to allow the OK to happen
   */
  boolean performOk(Set<IEntity> entitiesToSave);

  /**
   * Notifies that the Property-Dialog is closing after the user has pressed the
   * OK-Button. This Method is only called when there is no other page
   * preventing the Dialog from closing because of an Error.
   */
  void finish();
}