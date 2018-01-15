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

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.ISearchFilter;

/**
 * An implementation of {@link IFilterAction} that is stored in a
 * {@link ISearchFilter} and provides the ID and arbitrary data of an operation
 * being performed for entities matching the conditions as specified in the
 * {@link ISearchFilter}.
 *
 * @author bpasero
 */
public class FilterAction extends Persistable implements IFilterAction {
  private String fActionId;
  private Object fData;

  /**
   * @param actionId the ID of the action to perform.
   */
  public FilterAction(String actionId) {
    Assert.isNotNull(actionId);
    fActionId = actionId;
  }

  /**
   * Default constructor for deserialization
   */
  protected FilterAction() {
  // As per javadoc
  }

  /*
   * @see org.rssowl.core.persist.IFilterAction#getData()
   */
  @Override
  public synchronized Object getData() {
    return fData;
  }

  /*
   * @see org.rssowl.core.persist.IFilterAction#setData(java.lang.Object)
   */
  @Override
  public synchronized void setData(Object data) {
    fData = data;
  }

  /*
   * @see org.rssowl.core.persist.IFilterAction#getActionId()
   */
  @Override
  public synchronized String getActionId() {
    return fActionId;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public synchronized String toString() {
    return super.toString() + "Action ID = " + fActionId + ", "; //$NON-NLS-1$ //$NON-NLS-2$
  }
}