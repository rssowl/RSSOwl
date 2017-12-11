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

import org.eclipse.osgi.util.NLS;
import org.rssowl.core.INewsAction;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.services.DownloadService.DownloadRequest;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link INewsAction} to download all attachments of the
 * news.
 *
 * @author bpasero
 */
public class DownloadAttachmentsNewsAction implements INewsAction {

  /** Unique ID of this Action */
  public static final String ID = "org.rssowl.ui.DownloadAttachmentsNewsAction"; //$NON-NLS-1$

  /*
   * @see org.rssowl.core.INewsAction#run(java.util.List, java.util.Map, java.lang.Object)
   */
  @Override
  public List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data) {

    /* Ensure to Pickup Replaces */
    news = CoreUtils.replace(news, replacements);

    /* Run Filter */
    if (data != null && data instanceof String) {
      File folder = new File((String) data);
      if (folder.exists()) {
        for (INews newsitem : news) {

          /* Download Attachments */
          List<IAttachment> attachments = newsitem.getAttachments();
          for (IAttachment attachment : attachments) {
            URI link = attachment.getLink();
            if (link != null) {
              if (!link.isAbsolute()) {
                try {
                  link = URIUtils.resolve(URIUtils.toHTTP(newsitem.getFeedReference().getLink()), link);
                } catch (URISyntaxException e) {
                  Activator.safeLogError(e.getMessage(), e);
                  continue; //Proceed with other Attachments
                }
              }
            }

            if (link != null)
              Controller.getDefault().getDownloadService().download(DownloadRequest.createAttachmentDownloadRequest(attachment, link, folder, false, null));
          }

          /* In case of no Attachments, consider the news link itself */
          if (attachments.isEmpty()) {
            String newslink = CoreUtils.getLink(newsitem);
            if (StringUtils.isSet(newslink) && !newslink.endsWith(".html")) { //$NON-NLS-1$
              try {
                Controller.getDefault().getDownloadService().download(DownloadRequest.createNewsDownloadRequest(newsitem, new URI(newslink), folder));
              } catch (URISyntaxException e) {
                /* Fail gracefully in this case as this is not a normal attachment anyway */
              }
            }
          }
        }
      }
    }

    /* Nothing to Save */
    return Collections.emptyList();
  }

  /*
   * @see org.rssowl.core.INewsAction#conflictsWith(org.rssowl.core.INewsAction)
   */
  @Override
  public boolean conflictsWith(INewsAction otherAction) {
    return false;
  }

  /*
   * @see org.rssowl.core.INewsAction#getLabel(java.lang.Object)
   */
  @Override
  public String getLabel(Object data) {
    if (data != null && data instanceof String)
      return NLS.bind(Messages.DownloadAttachmentsNewsAction_DOWNLOAD_TO_N, data);

    return null;
  }
}