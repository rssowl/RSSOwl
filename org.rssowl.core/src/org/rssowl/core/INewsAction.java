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

package org.rssowl.core;

import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Instances of {@link INewsAction} can be contributed from the
 * org.rssowl.core.NewsAction extension point. Their purpose is to perfrom
 * certain operations on a List of {@link INews}, e.g. marking them read.
 * </p>
 * The news filter facility in RSSOwl makes use of {@link INewsAction} to
 * perform certain operations based on search conditions.
 * <p>
 * Implementors are asked to not save any news that is passed to the action but
 * rather return the list of entities that have been changed from the
 * {@link #run(List, Map, Object)} method.
 * </p>
 * <p>
 * Contributed via <code>org.rssowl.core.NewsAction</code> Extension Point.
 * </p>
 *
 * @author bpasero
 */
public interface INewsAction {

  /**
   * Runs the operation on the list of news.
   * <p>
   * Implementors are asked to not save any news that is passed to the action
   * but rather return the list of entities that have been changed from the
   * {@link #run(List, Map, Object)} method.
   * </p>
   *
   * @param news the list of news to perform the operation on.
   * @param replacements a {@link Map} that is filled with replaced versions of
   * news items from previous filters. The implementor of this method must
   * ensure that he operates on the replaced versions if available. Use
   * {@link CoreUtils#replace(List, Map)} as convinient method.
   * @param data arbitrary data associated with the action.
   * @return a {@link List} of {@link IEntity} that has been changed as a result
   * of the action. The caller must ensure to save these entities. Never
   * <code>null</code>.
   */
  List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data);

  /**
   * Checks whether the two operations can be used together or not. E.g. an
   * operation to delete a list of news is likely not compatible with another
   * operation to mark the news as read.
   *
   * @param otherAction another news action to test for conflicting operations.
   * @return <code>true</code> in case the two operations can not be used
   * together and <code>false</code> otherwise.
   */
  boolean conflictsWith(INewsAction otherAction);

  /**
   * @param data arbitrary data associated with the action.
   * @return a human readable for the news action with the given data or
   * <code>null</code> to use the label of the action itself.
   */
  String getLabel(Object data);
}