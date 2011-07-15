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

package org.rssowl.core.persist.event;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.event.runnable.PreferenceEventRunnable;

/**
 * An Event-Object being used to notify Listeners, whenever a Preference was
 * added, updated or deleted in the persistance layer.
 *
 * @author bpasero
 */
public class PreferenceEvent extends ModelEvent {

  /**
   * @param preference The preference affected by this event.
   */
  public PreferenceEvent(IPreference preference) {
    super(preference);
    Assert.isNotNull(preference, "The preference must not be null"); //$NON-NLS-1$
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#getEntity()
   */
  @Override
  public final IPreference getEntity() {
    return (IPreference) super.getEntity();
  }

  /*
   * @see org.rssowl.core.persist.event.ModelEvent#createEventRunnable()
   */
  @Override
  public PreferenceEventRunnable createEventRunnable() {
    return new PreferenceEventRunnable();
  }
}