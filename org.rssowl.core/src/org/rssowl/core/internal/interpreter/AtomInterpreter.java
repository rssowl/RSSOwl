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
import org.jdom.output.XMLOutputter;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Interpreter for all Atom Formats.
 *
 * @author bpasero
 */
public class AtomInterpreter extends BasicInterpreter {
  private int fNewsCounter;

  /*
   * @see
   * org.rssowl.core.interpreter.IFormatInterpreter#interpret(org.jdom.Document,
   * org.rssowl.core.interpreter.types.IFeed)
   */
  public void interpret(Document document, IFeed feed) {
    Element root = document.getRootElement();
    setDefaultNamespaceUri(root.getNamespace().getURI());
    setRootElementName(root.getName());
    feed.setFormat("Atom 1.0"); //$NON-NLS-1$
    processFeed(root, feed);
  }

  private void processFeed(Element element, IFeed feed) {

    /* Interpret Attributes */
    List<?> attributes = element.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName();

      /* Check whether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, feed))
        continue;

      /* Version */
      else if ("version".equals(name)) //$NON-NLS-1$
        feed.setFormat(buildFormat("Atom", attribute.getValue())); //$NON-NLS-1$

      /* Language */
      else if ("lang".equals(name)) //$NON-NLS-1$
        feed.setLanguage(attribute.getValue());
    }

    /* Interpret Children */
    List<?> channelChildren = element.getChildren();
    for (Iterator<?> iter = channelChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check whether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        feed.setTitle(getContent(child));
        processNamespaceAttributes(child, feed);
      }

      /* Tagline / Subtitle */
      else if ("tagline".equals(name) || "subtitle".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
        feed.setDescription(getContent(child));
        processNamespaceAttributes(child, feed);
      }

      /* Generator */
      else if ("generator".equals(name)) { //$NON-NLS-1$
        feed.setGenerator(getContent(child));
        processNamespaceAttributes(child, feed);
      }

      /* Copyright / Rights */
      else if ("copyright".equals(name) || "rights".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
        feed.setCopyright(getContent(child));
        processNamespaceAttributes(child, feed);
      }

      /* Logo */
      else if ("logo".equals(name)) { //$NON-NLS-1$
        IImage image = Owl.getModelFactory().createImage(feed);
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          image.setLink(uri);

        processNamespaceAttributes(child, image);
      }

      /* Modified / Updated */
      else if ("modified".equals(name) || "updated".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
        feed.setLastModifiedDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, feed);
      }

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        String rel = child.getAttributeValue("rel"); //$NON-NLS-1$
        if ("alternate".equals(rel)) { //$NON-NLS-1$
          URI uri = URIUtils.createURI(child.getAttributeValue("href")); //$NON-NLS-1$
          if (uri != null)
            feed.setHomepage(uri);
        }

        processNamespaceAttributes(child, feed);
      }

      /* Entry */
      else if ("entry".equals(name)) //$NON-NLS-1$
        processEntry(child, feed);

      /* Category */
      else if ("category".equals(name)) //$NON-NLS-1$
        processCategory(child, feed);

      /* Author */
      else if ("author".equals(name)) //$NON-NLS-1$
        processAuthor(child, feed);
    }
  }

  private void processEntry(Element element, IFeed feed) {
    INews news = Owl.getModelFactory().createNews(null, feed, new Date(System.currentTimeMillis() - (fNewsCounter++ * 1)));
    news.setBase(feed.getBase());

    /* Check whether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, news);

    /* Interpret Children */
    List<?> newsChilds = element.getChildren();
    for (Iterator<?> iter = newsChilds.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check whether this Element is to be processed by a Contribution */
      if (processElementExtern(child, news))
        continue;

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        news.setTitle(getContent(child));
        processNamespaceAttributes(child, news);
      }

      /* Content / Summary */
      else if ("content".equals(name) || "summary".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
        news.setDescription(getContent(child));
        processNamespaceAttributes(child, news);
      }

      /* Modified / Updated */
      else if ("modified".equals(name) || "updated".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$
        news.setModifiedDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, news);
      }

      /* Issued / Created / Published */
      else if ("issued".equals(name) || "created".equals(name) || "published".equals(name)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        news.setPublishDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, news);
      }

      /* Id */
      else if ("id".equals(name)) { //$NON-NLS-1$
        IGuid guid = Owl.getModelFactory().createGuid(news, child.getText(), null);

        processNamespaceAttributes(child, guid);
      }

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        String rel = child.getAttributeValue("rel"); //$NON-NLS-1$

        /* News Link */
        if (rel == null || "alternate".equals(rel)) {//$NON-NLS-1$
          URI uri = URIUtils.createURI(child.getAttributeValue("href")); //$NON-NLS-1$
          if (uri != null)
            news.setLink(uri);
          processNamespaceAttributes(child, news);
        }

        /* Enclosure */
        else if ("enclosure".equals(rel)) { //$NON-NLS-1$
          URI attachmentUri = URIUtils.createURI(child.getAttributeValue("href")); //$NON-NLS-1$
          String attachmentType = child.getAttributeValue("type"); //$NON-NLS-1$
          int attachmentLength = StringUtils.stringToInt(child.getAttributeValue("length")); //$NON-NLS-1$

          /* Create Attachment only if valid */
          if (attachmentUri != null && !CoreUtils.hasAttachment(news, attachmentUri)) {
            IAttachment attachment = Owl.getModelFactory().createAttachment(null, news);
            attachment.setLink(attachmentUri);
            if (StringUtils.isSet(attachmentType))
              attachment.setType(attachmentType);
            if (attachmentLength != -1)
              attachment.setLength(attachmentLength);

            /* Allow Contributions */
            processNamespaceAttributes(child, attachment);
          }
        }
      }

      /* Category */
      else if ("category".equals(name)) //$NON-NLS-1$
        processCategory(child, news);

      /* Source */
      else if ("source".equals(name)) //$NON-NLS-1$
        processSource(child, news);

      /* Author */
      else if ("author".equals(name)) //$NON-NLS-1$
        processAuthor(child, news);
    }
  }

  private String getContent(Element element) {
    String type = element.getAttributeValue("type"); //$NON-NLS-1$

    /* XHTML Type makes use of a single <DIV> to surround the XHTML */
    if ("xhtml".equals(type) || "application/xhtml+xml".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
      List<?> children = element.getChildren();
      for (Iterator<?> iter = children.iterator(); iter.hasNext();) {
        Element contentChild = (Element) iter.next();
        String name = contentChild.getName();

        /* Expected DIV Child */
        if ("div".equals(name)) { //$NON-NLS-1$
          XMLOutputter out = new XMLOutputter();
          StringWriter writer = new StringWriter();

          try {
            out.output(contentChild.getContent(), writer);
            writer.close();
          } catch (IOException e) {
            /* This should not happen */
          }

          /* Get Text */
          String content = writer.toString();

          /*
           * Problem: This Method of writing the content of the xmlDiv into the
           * StringWriter is not taking care of any CDATA-Constructs inside, as
           * no XML-parsing is done. The workaround is to manually remove the
           * CDATA-Tags. This has the same effect as parsing, since CDATA is to
           * be taken as is, with no element or entitiy processing.
           */
          if (content.contains("<![CDATA[")) { //$NON-NLS-1$
            content = StringUtils.replaceAll(content, "<![CDATA[", ""); //$NON-NLS-1$ //$NON-NLS-2$
            content = StringUtils.replaceAll(content, "]]>", ""); //$NON-NLS-1$//$NON-NLS-2$
          }

          /* Return text */
          return content;
        }
      }
    }

    /* Return text and html as is */
    return element.getText();
  }

  private void processSource(Element element, INews news) {
    ISource source = Owl.getModelFactory().createSource(news);

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, source);

    /* Interpret Children */
    List<?> sourceChilds = element.getChildren();
    for (Iterator<?> iter = sourceChilds.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, source))
        continue;

      /* Name */
      else if ("title".equals(name)) { //$NON-NLS-1$
        source.setName(child.getText());
        processNamespaceAttributes(child, source);
      }

      /* EMail */
      else if ("link".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          source.setLink(uri);
        processNamespaceAttributes(child, source);
      }

      /* URI */
      else if ("id".equals(name) && source.getLink() == null) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          source.setLink(uri);
        processNamespaceAttributes(child, source);
      }
    }
  }

  private void processCategory(Element element, IEntity type) {
    ICategory category = Owl.getModelFactory().createCategory(null, type);

    /* Interpret Attributes */
    List<?> categoryAttributes = element.getAttributes();
    for (Iterator<?> iter = categoryAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, category))
        continue;

      /* Term */
      else if ("term".equals(name)) {//$NON-NLS-1$
        category.setDomain(attribute.getValue());

        /* Use as Name if not yet set */
        if (category.getName() == null)
          category.setName(attribute.getValue());
      }

      /* Label */
      else if ("label".equals(name)) //$NON-NLS-1$
        category.setName(attribute.getValue());
    }
  }

  private void processAuthor(Element element, IPersistable type) {
    IPerson person = Owl.getModelFactory().createPerson(null, type);

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, person);

    /* Interpret Children */
    List<?> personChilds = element.getChildren();
    for (Iterator<?> iter = personChilds.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, person))
        continue;

      /* Name */
      else if ("name".equals(name)) { //$NON-NLS-1$
        person.setName(child.getText());
        processNamespaceAttributes(child, person);
      }

      /* EMail */
      else if ("email".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          person.setEmail(uri);
        processNamespaceAttributes(child, person);
      }

      /* URI */
      else if ("uri".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          person.setUri(uri);
        processNamespaceAttributes(child, person);
      }
    }
  }
}