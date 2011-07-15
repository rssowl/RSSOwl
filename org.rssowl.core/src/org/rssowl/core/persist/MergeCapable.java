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

package org.rssowl.core.persist;

import org.rssowl.core.internal.persist.MergeResult;

/**
 * Types that are capable of being merged, provide an implementation of
 * <code>T merge(T objectToMerge)</code> that merges all fields of the given
 * Object <code>objectToMerge</code> into the actual one. A popular use-case
 * is a Feed being loaded from the Internet that needs to be merged into an
 * already existing, persisted Feed with the same URL.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 * @param <T> The Type that is capable of Merging.
 */
public interface MergeCapable<T> {

  /**
   * Merge all Fields of the given Type into the actual one.
   * 
   * @param objectToMerge The Type which state is to merged into the actual
   * Type.
   * @return Returns the merged Type.
   */
  public MergeResult merge(T objectToMerge);
}