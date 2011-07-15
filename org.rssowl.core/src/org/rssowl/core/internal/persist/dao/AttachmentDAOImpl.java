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

import org.rssowl.core.internal.persist.Attachment;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.IAttachmentDAO;
import org.rssowl.core.persist.event.AttachmentEvent;
import org.rssowl.core.persist.event.AttachmentListener;
import org.rssowl.core.persist.event.NewsEvent;

/**
 * A data-access-object for <code>IAttachment</code>s.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class AttachmentDAOImpl extends AbstractEntityDAO<IAttachment, AttachmentListener, AttachmentEvent> implements IAttachmentDAO {

  /** Default constructor using the specific IPersistable for this DAO */
  public AttachmentDAOImpl() {
    super(Attachment.class, false);
  }

  /*
   * @see org.rssowl.core.internal.persist.dao.AbstractEntityDAO#
   * createDeleteEventTemplate(org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final AttachmentEvent createDeleteEventTemplate(IAttachment entity) {
    return createSaveEventTemplate(entity);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractEntityDAO#createSaveEventTemplate
   * (org.rssowl.core.persist.IEntity)
   */
  @Override
  protected final AttachmentEvent createSaveEventTemplate(IAttachment entity) {
    return new AttachmentEvent(entity, true);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.dao.AbstractPersistableDAO#doDelete(org
   * .rssowl.core.persist.IPersistable)
   */
  @Override
  public final void doDelete(IAttachment entity) {

    //Not sure about this, but let's do it for now to help us track a bug
    //in NewsService where never having a newsUpdated with a null oldNews is helpful
    INews news = entity.getNews();
    INews oldNews = fDb.ext().peekPersisted(news, 2, true);
    NewsEvent newsEvent = new NewsEvent(oldNews, news, false);
    DBHelper.putEventTemplate(newsEvent);

    super.doDelete(entity);
  }
}