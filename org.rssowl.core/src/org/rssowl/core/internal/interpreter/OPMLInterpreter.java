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
import org.rssowl.core.persist.ISource;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.URIUtils;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Interpreter for all OPML Formats.
 *
 * @author bpasero
 */
public class OPMLInterpreter extends BasicInterpreter {
  private int fNewsCounter;

  /*
   * @see org.rssowl.core.interpreter.IFormatInterpreter#interpret(org.jdom.Document,
   * org.rssowl.core.interpreter.types.IFeed)
   */
  public void interpret(Document document, IFeed feed) {
    Element root = document.getRootElement();
    setDefaultNamespaceUri(root.getNamespace().getURI());
    setRootElementName(root.getName());
    feed.setFormat("OPML"); //$NON-NLS-1$
    processFeed(root, feed);
  }

  private void processFeed(Element element, IFeed feed) {

    /* Interpret Attributes */
    List< ? > attributes = element.getAttributes();
    for (Iterator< ? > iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, feed))
        continue;

      /* Version */
      else if ("version".equals(name)) //$NON-NLS-1$
        feed.setFormat(buildFormat("OPML", attribute.getValue())); //$NON-NLS-1$
    }

    /* Interpret Children */
    List< ? > feedChildren = element.getChildren();
    for (Iterator< ? > iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Process Head */
      else if ("head".equals(name)) //$NON-NLS-1$
        processHead(child, feed);

      /* Process Body */
      else if ("body".equals(name)) //$NON-NLS-1$
        processBody(child, feed);
    }
  }

  private void processHead(Element element, IFeed feed) {

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, feed);

    /* Interpret Children */
    List< ? > channelChildren = element.getChildren();
    for (Iterator< ? > iter = channelChildren.iterator(); iter.hasNext();) {
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

      /* Date Created */
      else if ("datecreated".equals(name)) { //$NON-NLS-1$
        feed.setLastBuildDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, feed);
      }

      /* Date Modified */
      else if ("datemodified".equals(name)) { //$NON-NLS-1$
        feed.setLastModifiedDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, feed);
      }

      /* Owner EMail */
      else if ("owneremail".equals(name)) { //$NON-NLS-1$
        if (feed.getAuthor() == null)
          Owl.getModelFactory().createPerson(null, feed);

        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          feed.getAuthor().setEmail(uri);

        processNamespaceAttributes(child, feed.getAuthor());
      }

      /* Owner Name - Dont set if EMail present already */
      else if ("ownername".equals(name)) { //$NON-NLS-1$
        if (feed.getAuthor() == null)
          Owl.getModelFactory().createPerson(null, feed);

        feed.getAuthor().setName(child.getText());

        processNamespaceAttributes(child, feed.getAuthor());
      }
    }
  }

  private void processBody(Element element, IFeed feed) {

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, feed);

    /* Interpret Children */
    List< ? > channelChildren = element.getChildren();
    for (Iterator< ? > iter = channelChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Outline */
      else if ("outline".equals(name)) //$NON-NLS-1$
        processOutline(child, feed);
    }
  }

  private void processOutline(Element element, IFeed feed) {
    INews news = Owl.getModelFactory().createNews(null, feed, new Date(System.currentTimeMillis() - (fNewsCounter++ * 1)));
    news.setBase(feed.getBase());

    /* Interpret Attributes */
    List< ? > outlineAttributes = element.getAttributes();
    for (Iterator< ? > iter = outlineAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, news))
        continue;

      /* Title */
      else if ("title".equals(name)) //$NON-NLS-1$
        news.setTitle(attribute.getValue());

      /* URL */
      else if ("url".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null)
          news.setLink(uri);
      }

      /* HTML URL - If not yet set as Link */
      else if ("htmlurl".equals(name) && news.getLinkAsText() == null) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null)
          news.setLink(uri);
      }

      /* XML URL */
      else if ("xmlurl".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null) {
          ISource source = Owl.getModelFactory().createSource(news);
          source.setLink(uri);
        }
      }

      /* Text */
      else if ("text".equals(name)) //$NON-NLS-1$
        news.setDescription(attribute.getValue());

      /* Description */
      else if ("description".equals(name)) //$NON-NLS-1$
        news.setDescription(attribute.getValue());
    }

    /* Interpret Children */
    List< ? > channelChildren = element.getChildren();
    for (Iterator< ? > iter = channelChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Child Outlines */
      else if ("outline".equals(name)) //$NON-NLS-1$
        processOutline(child, feed);
    }
  }
}