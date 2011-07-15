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

package org.rssowl.core.internal.interpreter;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.URIUtils;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Interpreter for the Channel Definition Format.
 *
 * @author bpasero
 */
public class CDFInterpreter extends BasicInterpreter {
  private int fNewsCounter;

  /*
   * @see org.rssowl.core.interpreter.IFormatInterpreter#interpret(org.jdom.Document,
   * org.rssowl.core.interpreter.types.IFeed)
   */
  public void interpret(Document document, IFeed feed) {
    Element root = document.getRootElement();
    setDefaultNamespaceUri(root.getNamespace().getURI());
    setRootElementName(root.getName());
    feed.setFormat("CDF"); //$NON-NLS-1$
    processChannel(root, feed);
  }

  private void processChannel(Element element, IFeed feed) {

    /* Interpret Attributes */
    List< ? > channelAttributes = element.getAttributes();
    for (Iterator< ? > iter = channelAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, feed))
        continue;

      /* Last Modificated */
      else if ("lastmod".equals(name)) //$NON-NLS-1$
        feed.setLastModifiedDate(DateUtils.parseDate(attribute.getValue()));

      /* Base */
      else if ("base".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null)
          feed.setHomepage(uri);
      }
    }

    /* Interpret Children */
    List< ? > feedChildren = element.getChildren();
    for (Iterator< ? > iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        feed.setTitle(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Abstract */
      else if ("abstract".equals(name)) { //$NON-NLS-1$
        feed.setDescription(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Process Item */
      else if ("item".equals(name)) //$NON-NLS-1$
        processItem(child, feed);
    }
  }

  private void processItem(Element element, IFeed feed) {
    INews news = Owl.getModelFactory().createNews(null, feed, new Date(System.currentTimeMillis() - (fNewsCounter++ * 1)));
    news.setBase(feed.getBase());

    String baseUrl = feed.getHomepage() != null ? feed.getHomepage().toString() : ""; //$NON-NLS-1$

    /* Interpret Attributes */
    List< ? > itemAttributes = element.getAttributes();
    for (Iterator< ? > iter = itemAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, news))
        continue;

      /* Last Modificated */
      else if ("lastmod".equals(name)) //$NON-NLS-1$
        news.setPublishDate(DateUtils.parseDate(attribute.getValue()));

      /* Href - Append with Feed-Base */
      else if ("href".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(baseUrl + attribute.getValue());
        if (uri != null)
          news.setLink(uri);
      }
    }

    /* Interpret Children */
    List< ? > newsChilds = element.getChildren();
    for (Iterator< ? > iter = newsChilds.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, news))
        continue;

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        news.setTitle(child.getText());
        processNamespaceAttributes(child, news);
      }

      /* Abstract */
      else if ("abstract".equals(name)) { //$NON-NLS-1$
        news.setDescription(child.getText());
        processNamespaceAttributes(child, news);
      }
    }
  }
}