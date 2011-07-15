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

import org.eclipse.osgi.util.NLS;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.URIUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Interpreter for the bugzilla format.
 *
 * @author kay.patzwald
 */
public class BugzillaInterpreter extends BasicInterpreter {
  private int fNewsCounter;

  public void interpret(Document document, IFeed feed) {
    Element root = document.getRootElement();
    setDefaultNamespaceUri(root.getNamespace().getURI());
    setRootElementName(root.getName());
    feed.setFormat("Bugzilla"); //$NON-NLS-1$
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
        feed.setFormat(buildFormat("Bugzilla", attribute.getValue())); //$NON-NLS-1$

      /* Homepage */
      else if ("urlbase".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null)
          feed.setBase(uri);
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

      /* Process Bug */
      else if ("bug".equals(name)) //$NON-NLS-1$
        processBug(child, feed);

    }
  }

  private void processBug(Element element, IFeed feed) {

    /* Interpret Children */
    List< ? > channelChildren = element.getChildren();
    for (Iterator< ? > iter = channelChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Short description */
      else if ("short_desc".equals(name)) { //$NON-NLS-1$
        feed.setTitle(child.getText());
      }

      else if ("reporter".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null) {
          IPerson person = Owl.getModelFactory().createPerson(null, feed);
          person.setEmail(uri);
        }
      }

      /* Bug-ID */
      else if ("bug_id".equals(name)) { //$NON-NLS-1$
        URI baseUri = feed.getBase();

        try {
          feed.setHomepage(new URI(baseUri.getScheme(), baseUri.getAuthority(), "/show_bug.cgi", "id=" + child.getText(), baseUri.getFragment())); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (URISyntaxException e) {
          /* Ignore */
        }
      }

      /* Long description */
      else if ("long_desc".equals(name)) { //$NON-NLS-1$
        processDescription(child, feed);
      }
    }
  }

  private void processDescription(Element element, IFeed feed) {
    INews news = Owl.getModelFactory().createNews(null, feed, new Date(System.currentTimeMillis() - (fNewsCounter++ * 1)));
    news.setBase(feed.getBase());

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, feed);

    /* Interpret Children */
    List< ? > channelChildren = element.getChildren();
    for (Iterator< ? > iter = channelChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      news.setLink(feed.getHomepage());

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Who */
      else if ("who".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null) {
          IPerson person = Owl.getModelFactory().createPerson(null, news);
          person.setEmail(uri);
        }
        news.setTitle(NLS.bind(Messages.BugzillaInterpreter_COMMENT_FROM, child.getText()));
      }

      /* Date of the comment */
      else if ("bug_when".equals(name)) { //$NON-NLS-1$ {
        news.setModifiedDate(DateUtils.parseDate(child.getText()));
      }

      /* Text of the comment */
      else if ("thetext".equals(name)) { //$NON-NLS-1$ {
        news.setDescription(child.getText());
      }
    }
  }
}