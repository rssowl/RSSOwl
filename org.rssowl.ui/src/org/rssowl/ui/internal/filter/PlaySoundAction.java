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

package org.rssowl.ui.internal.filter;

import org.rssowl.core.INewsAction;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.ui.internal.util.AudioUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link INewsAction} to play a sound.
 *
 * @author bpasero
 */
public class PlaySoundAction implements INewsAction {

  /* Remember when the last Sound was played */
  private static final Map<String, Long> fgLastPlayedSoundsMap = Collections.synchronizedMap(new HashMap<String, Long>());

  /* Millies before playing a same Sound again */
  private static final long BLOCK_SOUND_REPEAT_VALUE = 5000;

  /*
   * @see org.rssowl.core.INewsAction#run(java.util.List, java.util.Map, java.lang.Object)
   */
  public List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data) {

    /* Run Notifier */
    if (AudioUtils.isSupported() && data != null && data instanceof String) {
      Long lastPlayed = fgLastPlayedSoundsMap.get(data);
      if (lastPlayed == null || System.currentTimeMillis() - lastPlayed > BLOCK_SOUND_REPEAT_VALUE) {
        AudioUtils.play((String) data);
        fgLastPlayedSoundsMap.put((String) data, System.currentTimeMillis());
      }
    }

    /* Nothing to Save */
    return Collections.emptyList();
  }

  /*
   * @see org.rssowl.core.INewsAction#conflictsWith(org.rssowl.core.INewsAction)
   */
  public boolean conflictsWith(INewsAction otherAction) {
    return false;
  }

  /*
   * @see org.rssowl.core.INewsAction#getLabel(java.lang.Object)
   */
  public String getLabel(Object data) {
    return null;
  }
}