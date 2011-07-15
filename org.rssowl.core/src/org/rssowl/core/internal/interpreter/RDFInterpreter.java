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
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ITextInput;
import org.rssowl.core.util.URIUtils;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Interpreter for all RDF Formats.
 *
 * @author bpasero
 */
public class RDFInterpreter extends BasicInterpreter {
  private int fNewsCounter;

  /*
   * @see org.rssowl.core.interpreter.IFormatInterpreter#interpret(org.jdom.Document,
   * org.rssowl.core.interpreter.types.IFeed)
   */
  public void interpret(Document document, IFeed feed) {
    Element root = document.getRootElement();
    setDefaultNamespaceUri(root.getNamespace().getURI());
    setRootElementName(root.getName());
    feed.setFormat("RDF"); //$NON-NLS-1$
    processFeed(root, feed);
  }

  private void processFeed(Element element, IFeed feed) {

    /* Interpret Attributes */
    processNamespaceAttributes(element, feed);

    /* Interpret Children */
    List< ? > feedChildren = element.getChildren();
    for (Iterator< ? > iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Process Channel */
      else if ("channel".equals(name)) //$NON-NLS-1$
        processChannel(child, feed);

      /* Process Item */
      else if ("item".equals(name)) //$NON-NLS-1$
        processItem(child, feed);

      /* Process Image */
      else if ("image".equals(name)) //$NON-NLS-1$
        processImage(child, feed);

      /* Process TextInput */
      else if ("textinput".equals(name)) //$NON-NLS-1$
        processTextInput(child, feed);
    }
  }

  private void processChannel(Element element, IFeed feed) {

    /* Interpret Attributes */
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

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          feed.setHomepage(uri);
        processNamespaceAttributes(child, feed);
      }

      /* Description */
      else if ("description".equals(name)) { //$NON-NLS-1$
        feed.setDescription(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Process Image */
      else if ("image".equals(name)) //$NON-NLS-1$
        processImage(child, feed);

      /* Process TextInput */
      else if ("textinput".equals(name)) //$NON-NLS-1$
        processTextInput(child, feed);
    }
  }

  private void processTextInput(Element element, IFeed feed) {
    ITextInput input = Owl.getModelFactory().createTextInput(feed);

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, input);

    /* Interpret Children */
    List< ? > inputChilds = element.getChildren();
    for (Iterator< ? > iter = inputChilds.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, input))
        continue;

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        input.setTitle(child.getText());
        processNamespaceAttributes(child, input);
      }

      /* Description */
      else if ("description".equals(name)) { //$NON-NLS-1$
        input.setDescription(child.getText());
        processNamespaceAttributes(child, input);
      }

      /* Name */
      else if ("name".equals(name)) { //$NON-NLS-1$
        input.setName(child.getText());
        processNamespaceAttributes(child, input);
      }

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          input.setLink(uri);
        processNamespaceAttributes(child, input);
      }
    }
  }

  private void processImage(Element element, IFeed feed) {
    IImage image = Owl.getModelFactory().createImage(feed);

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, image);

    /* Interpret Children */
    List< ? > imageChilds = element.getChildren();
    for (Iterator< ? > iter = imageChilds.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, image))
        continue;

      /* URL */
      else if ("url".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          image.setLink(uri);
        processNamespaceAttributes(child, image);
      }

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        image.setTitle(child.getText());
        processNamespaceAttributes(child, image);
      }

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          image.setHomepage(uri);
        processNamespaceAttributes(child, image);
      }
    }
  }

  private void processItem(Element element, IFeed feed) {
    INews news = Owl.getModelFactory().createNews(null, feed, new Date(System.currentTimeMillis() - (fNewsCounter++ * 1)));
    news.setBase(feed.getBase());

    /* Interpret Attributes */
    List<?> itemAttributes = element.getAttributes();
    for (Iterator<?> iter = itemAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, news))
        continue;

      /* About */
      else if ("about".equals(name)) //$NON-NLS-1$
        Owl.getModelFactory().createGuid(news, attribute.getValue(), true);
    }

    /* Interpret Children */
    List<?> newsChilds = element.getChildren();
    for (Iterator<?> iter = newsChilds.iterator(); iter.hasNext();) {
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

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          news.setLink(uri);
        processNamespaceAttributes(child, news);
      }

      /* Description */
      else if ("description".equals(name)) { //$NON-NLS-1$
        news.setDescription(child.getText());
        processNamespaceAttributes(child, news);
      }
    }
  }
}