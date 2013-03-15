/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.core.internal.interpreter.json;

import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Interpreter that will fill a {@link IFeed} with values obtained from a
 * {@link JSONObject}.
 *
 * @author bpasero
 */
public class JSONInterpreter {

  /* Constants used to obtain data from the JSON Objects */
  private static final String STREAM_ID = "streamId"; //$NON-NLS-1$
  private static final String ORIGIN = "origin"; //$NON-NLS-1$
  private static final String AUTHOR = "author"; //$NON-NLS-1$
  private static final String TYPE = "type"; //$NON-NLS-1$
  private static final String ALTERNATE = "alternate"; //$NON-NLS-1$
  private static final String LENGTH = "length"; //$NON-NLS-1$
  private static final String HREF = "href"; //$NON-NLS-1$
  private static final String ENCLOSURE = "enclosure"; //$NON-NLS-1$
  private static final String CATEGORIES = "categories"; //$NON-NLS-1$
  private static final String ID = "id"; //$NON-NLS-1$
  private static final String SUMMARY = "summary"; //$NON-NLS-1$
  private static final String CONTENT = "content"; //$NON-NLS-1$
  private static final String ITEMS = "items"; //$NON-NLS-1$
  private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$
  private static final String UPDATED = "updated"; //$NON-NLS-1$
  private static final String DESCRIPTION = "description"; //$NON-NLS-1$
  private static final String TITLE = "title"; //$NON-NLS-1$
  private static final String REPLIES = "replies"; //$NON-NLS-1$

  /* Google Category Constants */
  private static final String GOOGLE_LABEL_PART = "/label/"; //$NON-NLS-1$
  private static final String GOOGLE_CATEGORY_PREFIX = "user/"; //$NON-NLS-1$
  private static final String GOOGLE_STATE_READ = "/state/com.google/read"; //$NON-NLS-1$
  private static final String GOOGLE_STATE_UNREAD = "/state/com.google/kept-unread"; //$NON-NLS-1$
  private static final String GOOGLE_STATE_STARRED = "/state/com.google/starred"; //$NON-NLS-1$

  /**
   * @param json the {@link JSONObject} to obtain the values from.
   * @param feed the {@link IFeed} to fill with the values from the
   * {@link JSONObject}
   * @throws InterpreterException in case of an error interpreting.
   */
  public void interpret(JSONObject json, IFeed feed) throws InterpreterException {
    try {
      processFeed(json, feed);
    } catch (JSONException e) {
      throw new InterpreterException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    } catch (URISyntaxException e) {
      throw new InterpreterException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    }
  }

  private void processFeed(JSONObject json, IFeed feed) throws JSONException, URISyntaxException {

    /* Title */
    feed.setTitle(getString(json, TITLE));

    /* Publish Date */
    feed.setPublishDate(getDate(json, UPDATED));

    /* Description */
    feed.setDescription(getString(json, DESCRIPTION));

    /* Homepage */
    feed.setHomepage(getAlternateLink(json, TEXT_HTML));

    /* News Items */
    if (json.has(ITEMS)) {
      JSONArray items = json.getJSONArray(ITEMS);
      for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.getJSONObject(i);
        processItem(item, feed);
      }
    }
  }

  private void processItem(JSONObject item, IFeed feed) throws JSONException, URISyntaxException {
    IModelFactory factory = Owl.getModelFactory();
    INews news = factory.createNews(null, feed, new Date());
    news.setBase(feed.getBase());

    /* GUID */
    if (item.has(ID))
      news.setGuid(factory.createGuid(news, item.getString(ID), true));

    /* Origin */
    if (item.has(ORIGIN)) {
      JSONObject origin = item.getJSONObject(ORIGIN);
      news.setInReplyTo(getString(origin, STREAM_ID));
    }

    /* Title */
    news.setTitle(getString(item, TITLE));

    /* Publish Date */
    news.setPublishDate(getDate(item, UPDATED));

    /* Description */
    news.setDescription(getContent(item));

    /* Link */
    news.setLink(getAlternateLink(item, TEXT_HTML));

    /* Comments */
    URI commentsLink = getCommentsLink(item, TEXT_HTML);
    if (commentsLink != null)
      news.setComments(commentsLink.toString());

    /* Author */
    if (item.has(AUTHOR)) {
      String author = getString(item, AUTHOR);
      if (StringUtils.isSet(author)) {
        IPerson person = factory.createPerson(null, news);
        person.setName(author);
      }
    }

    /* Attachments */
    if (item.has(ENCLOSURE)) {
      JSONArray attachments = item.getJSONArray(ENCLOSURE);
      for (int i = 0; i < attachments.length(); i++) {
        JSONObject attachment = attachments.getJSONObject(i);
        if (attachment.has(HREF)) {
          IAttachment att = factory.createAttachment(null, news);
          att.setLink(new URI(attachment.getString(HREF)));

          if (attachment.has(LENGTH)) {
            try {
              att.setLength(attachment.getInt(LENGTH));
            } catch (JSONException e) {
              // Can happen if the length is larger than Integer.MAX_VALUE, in that case just ignore
            }
          }

          if (attachment.has(TYPE))
            att.setType(attachment.getString(TYPE));
        }
      }
    }

    /* Categories / Labels / State */
    Set<String> labels = new HashSet<String>(1);
    if (item.has(CATEGORIES)) {
      JSONArray categories = item.getJSONArray(CATEGORIES);
      for (int i = 0; i < categories.length(); i++) {
        if (categories.isNull(i))
          continue;

        String category = categories.getString(i);
        if (!StringUtils.isSet(category))
          continue;

        /* Normal user chosen category */
        if (!category.startsWith(GOOGLE_CATEGORY_PREFIX)) {
          ICategory cat = factory.createCategory(null, news);
          cat.setName(category);
        }

        /* News is marked read */
        else if (category.endsWith(GOOGLE_STATE_READ)) {
          news.setProperty(SyncUtils.GOOGLE_MARKED_READ, true); //Can not use state here for core reasons
        }

        /* News is marked unread */
        else if (category.endsWith(GOOGLE_STATE_UNREAD)) {
          news.setProperty(SyncUtils.GOOGLE_MARKED_UNREAD, true); //Can not use state here for core reasons
        }

        /* News is starred */
        else if (category.endsWith(GOOGLE_STATE_STARRED)) {
          news.setFlagged(true);
        }

        /* News is Labeled */
        else if (category.contains(GOOGLE_LABEL_PART)) {
          String label = category.substring(category.indexOf(GOOGLE_LABEL_PART) + GOOGLE_LABEL_PART.length());
          if (StringUtils.isSet(label))
            labels.add(label);
        }
      }

      /*
       * Store Labels as Properties first and create them in ApplicationService
       * with a single Thread to avoid that Labels are created as duplicates.
       */
      if (!labels.isEmpty())
        news.setProperty(SyncUtils.GOOGLE_LABELS, labels.toArray(new String[labels.size()]));
    }
  }

  private URI getAlternateLink(JSONObject obj, String type) throws JSONException, URISyntaxException {
    if (obj.has(ALTERNATE)) {
      JSONArray alternates = obj.getJSONArray(ALTERNATE);
      for (int i = 0; i < alternates.length(); i++) {
        JSONObject alternate = alternates.getJSONObject(i);
        if (type.equals(getString(alternate, TYPE)))
          return getURI(alternate, HREF);
      }
    }

    return null;
  }

  private URI getCommentsLink(JSONObject obj, String type) throws JSONException, URISyntaxException {
    if (obj.has(REPLIES)) {
      JSONArray replies = obj.getJSONArray(REPLIES);
      for (int i = 0; i < replies.length(); i++) {
        JSONObject reply = replies.getJSONObject(i);
        if (type.equals(getString(reply, TYPE)))
          return getURI(reply, HREF);
      }
    }

    return null;
  }

  private String getString(JSONObject object, String key) throws JSONException {
    return object.has(key) ? object.getString(key) : null;
  }

  private String getContent(JSONObject item) throws JSONException {
    JSONObject contentObj = null;
    if (item.has(CONTENT))
      contentObj = item.getJSONObject(CONTENT);
    else if (item.has(SUMMARY))
      contentObj = item.getJSONObject(SUMMARY);

    if (contentObj != null && contentObj.has(CONTENT))
      return contentObj.getString(CONTENT);

    return null;
  }

  private URI getURI(JSONObject object, String key) throws URISyntaxException, JSONException {
    return object.has(key) ? new URI(object.getString(key)) : null;
  }

  private Date getDate(JSONObject object, String key) throws JSONException {
    return object.has(key) ? new Date(object.getLong(key) * 1000) : null;
  }
}