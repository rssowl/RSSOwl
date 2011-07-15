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

package org.rssowl.core.internal.persist.dao;

import org.rssowl.core.internal.persist.Category;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.dao.ICategoryDAO;
import org.rssowl.core.persist.event.CategoryEvent;
import org.rssowl.core.persist.event.CategoryListener;
import org.rssowl.core.util.StringUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * A data-access-object for <code>ICategory</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class CategoryDAOImpl extends AbstractEntityDAO<ICategory, CategoryListener, CategoryEvent> implements ICategoryDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public CategoryDAOImpl() {
    super(Category.class, false);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final CategoryEvent createDeleteEventTemplate(ICategory entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final CategoryEvent createSaveEventTemplate(ICategory entity) {
    return new CategoryEvent(entity, true);
  }

  /*
   * @see org.rssowl.core.persist.dao.ICategoryDAO#loadAllNames()
   */
  public final Set<String> loadAllNames() {
    Set<String> names = new TreeSet<String>();
    for (ICategory category : loadAll()) {
      String name = StringUtils.safeTrim(category.getName());
      if (StringUtils.isSet(name))
        names.add(name);
    }

    return names;
  }
}