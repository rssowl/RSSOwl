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
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.ICloud;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.persist.ITextInput;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Interpreter for the three major RSS Formats 0.91, 0.92 and 2.0.
 *
 * @author bpasero
 */
public class RSSInterpreter extends BasicInterpreter {
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
    feed.setFormat("RSS"); //$NON-NLS-1$
    processFeed(root, feed);
  }

  private void processFeed(Element element, IFeed feed) {

    /* Interpret Attributes */
    List<?> attributes = element.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, feed))
        continue;

      /* Version */
      else if ("version".equals(name)) //$NON-NLS-1$
        feed.setFormat(buildFormat("RSS", attribute.getValue())); //$NON-NLS-1$
    }

    /* Interpret Children */
    List<?> feedChildren = element.getChildren();
    for (Iterator<?> iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Process Channel */
      else if ("channel".equals(name)) //$NON-NLS-1$
        processChannel(child, feed);
    }
  }

  private void processChannel(Element element, IFeed feed) {

    /* Interpret Attributes */
    List<?> attributes = element.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();

      /* Check wether this Attribute is to be processed by a Contribution */
      processAttributeExtern(attribute, feed);
    }

    /* Interpret Children */
    List<?> channelChildren = element.getChildren();
    for (Iterator<?> iter = channelChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Check wether this Element is to be processed by a Contribution */
      if (processElementExtern(child, feed))
        continue;

      /* Item */
      else if ("item".equals(name)) //$NON-NLS-1$
        processItems(child, feed);

      /* Title */
      else if ("title".equals(name)) { //$NON-NLS-1$
        feed.setTitle(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());

        /*
         * Do not use the URI if it is empty. This is a workaround for
         * FeedBurner feeds that use a Atom 1.0 Link Element in place of an RSS
         * feed which RSSOwl 2 is not yet able to handle on this scope.
         */
        if (uri != null && StringUtils.isSet(uri.toString()))
          feed.setHomepage(uri);
        processNamespaceAttributes(child, feed);
      }

      /* Description */
      else if ("description".equals(name)) { //$NON-NLS-1$
        feed.setDescription(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Publish Date */
      else if ("pubdate".equals(name)) { //$NON-NLS-1$
        feed.setPublishDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, feed);
      }

      /* Image */
      else if ("image".equals(name)) //$NON-NLS-1$
        processImage(child, feed);

      /* Language */
      else if ("language".equals(name)) { //$NON-NLS-1$
        feed.setLanguage(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Copyright */
      else if ("copyright".equals(name)) { //$NON-NLS-1$
        feed.setCopyright(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Webmaster */
      else if ("webmaster".equals(name)) { //$NON-NLS-1$
        feed.setWebmaster(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Managing Editor */
      else if ("managingeditor".equals(name)) { //$NON-NLS-1$
        IPerson person = Owl.getModelFactory().createPerson(null, feed);
        person.setName(child.getText());

        processNamespaceAttributes(child, person);
      }

      /* Last Build Date */
      else if ("lastbuilddate".equals(name)) { //$NON-NLS-1$
        feed.setLastBuildDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, feed);
      }

      /* Category */
      else if ("category".equals(name)) //$NON-NLS-1$
        processCategory(child, feed);

      /* Generator */
      else if ("generator".equals(name)) { //$NON-NLS-1$
        feed.setGenerator(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* Docs */
      else if ("docs".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(child.getText());
        if (uri != null)
          feed.setDocs(uri);
        processNamespaceAttributes(child, feed);
      }

      /* Rating */
      else if ("rating".equals(name)) { //$NON-NLS-1$
        feed.setRating(child.getText());
        processNamespaceAttributes(child, feed);
      }

      /* TTL */
      else if ("ttl".equals(name)) {//$NON-NLS-1$
        int ttl = StringUtils.stringToInt(child.getTextNormalize());
        if (ttl >= 0)
          feed.setTTL(ttl);
        processNamespaceAttributes(child, feed);
      }

      /* Skip Hours */
      else if ("skiphours".equals(name)) { //$NON-NLS-1$
        processNamespaceAttributes(child, feed);
        List<?> skipHoursChildren = child.getChildren("hour"); //$NON-NLS-1$

        /* For each <hour> Element */
        for (Iterator<?> iterator = skipHoursChildren.iterator(); iterator.hasNext();) {
          Element skipHour = (Element) iterator.next();
          processNamespaceAttributes(skipHour, feed);

          int hour = StringUtils.stringToInt(skipHour.getTextNormalize());
          if (0 <= hour && hour < 24)
            feed.addHourToSkip(hour);
        }
      }

      /* Skip Days */
      else if ("skipdays".equals(name)) { //$NON-NLS-1$
        processNamespaceAttributes(child, feed);
        List<?> skipDaysChildren = child.getChildren("day"); //$NON-NLS-1$

        /* For each <day> Element */
        for (Iterator<?> iterator = skipDaysChildren.iterator(); iterator.hasNext();) {
          Element skipDay = (Element) iterator.next();
          processNamespaceAttributes(skipDay, feed);

          String day = skipDay.getText().toLowerCase();
          int index = IFeed.DAYS.indexOf(day);
          if (index >= 0)
            feed.addDayToSkip(index);
        }
      }

      /* TextInput */
      else if ("textinput".equals(name)) //$NON-NLS-1$
        processTextInput(child, feed);

      /* Cloud */
      else if ("cloud".equals(name)) //$NON-NLS-1$
        processCloud(child, feed);
    }
  }

  private void processCloud(Element element, IFeed feed) {
    ICloud cloud = Owl.getModelFactory().createCloud(feed);

    /* Interpret Attributes */
    List<?> cloudAttributes = element.getAttributes();
    for (Iterator<?> iter = cloudAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, cloud))
        continue;

      /* Domain */
      else if ("domain".equals(name)) //$NON-NLS-1$
        cloud.setDomain(attribute.getValue());

      /* Path */
      else if ("path".equals(name)) //$NON-NLS-1$
        cloud.setPath(attribute.getValue());

      /* Port */
      else if ("port".equals(name)) {//$NON-NLS-1$
        int port = StringUtils.stringToInt(attribute.getValue());
        if (port >= 0)
          cloud.setPort(port);
      }

      /* Procedure Call */
      else if ("registerprocedure".equals(name)) //$NON-NLS-1$
        cloud.setRegisterProcedure(attribute.getValue());

      /* Path */
      else if ("protocol".equals(name)) //$NON-NLS-1$
        cloud.setProtocol(attribute.getValue());
    }
  }

  private void processCategory(Element element, IEntity type) {
    ICategory category = Owl.getModelFactory().createCategory(null, type);
    category.setName(element.getText());

    /* Interpret Attributes */
    List<?> categoryAttributes = element.getAttributes();
    for (Iterator<?> iter = categoryAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, category))
        continue;

      /* Domain */
      else if ("domain".equals(name)) //$NON-NLS-1$
        category.setDomain(attribute.getValue());
    }
  }

  private void processImage(Element element, IFeed feed) {
    IImage image = Owl.getModelFactory().createImage(feed);

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, image);

    /* Interpret Children */
    List<?> imageChilds = element.getChildren();
    for (Iterator<?> iter = imageChilds.iterator(); iter.hasNext();) {
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

      /* Description */
      else if ("description".equals(name)) { //$NON-NLS-1$
        image.setDescription(child.getText());
        processNamespaceAttributes(child, image);
      }

      /* Width */
      else if ("width".equals(name)) {//$NON-NLS-1$
        int width = StringUtils.stringToInt(child.getTextNormalize());
        if (width >= 0)
          image.setWidth(width);
        processNamespaceAttributes(child, image);
      }

      /* Height */
      else if ("height".equals(name)) {//$NON-NLS-1$
        int height = StringUtils.stringToInt(child.getTextNormalize());
        if (height >= 0)
          image.setHeight(height);
        processNamespaceAttributes(child, image);
      }
    }
  }

  private void processItems(Element element, IFeed feed) {
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
        news.setTitle(child.getText());
        processNamespaceAttributes(child, news);
      }

      /* Link */
      else if ("link".equals(name)) { //$NON-NLS-1$
        if (child.getText().length() > 0) {
          URI uri = URIUtils.createURI(child.getText());
          if (uri != null)
            news.setLink(uri);
        }

        processNamespaceAttributes(child, news);
      }

      /* Description */
      else if ("description".equals(name)) { //$NON-NLS-1$
        news.setDescription(child.getText());
        processNamespaceAttributes(child, news);
      }

      /* Publish Date */
      else if ("pubdate".equals(name)) { //$NON-NLS-1$
        news.setPublishDate(DateUtils.parseDate(child.getText()));
        processNamespaceAttributes(child, news);
      }

      /* Author */
      else if ("author".equals(name)) { //$NON-NLS-1$
        IPerson person = Owl.getModelFactory().createPerson(null, news);
        person.setName(child.getText());

        processNamespaceAttributes(child, person);
      }

      /* Comments */
      else if ("comments".equals(name)) { //$NON-NLS-1$
        news.setComments(child.getText());
        processNamespaceAttributes(child, news);
      }

      /* Attachment */
      else if ("enclosure".equals(name)) //$NON-NLS-1$
        processEnclosure(child, news);

      /* Category */
      else if ("category".equals(name)) //$NON-NLS-1$
        processCategory(child, news);

      /* GUID */
      else if ("guid".equals(name)) //$NON-NLS-1$
        processGuid(child, news);

      /* Source */
      else if ("source".equals(name)) //$NON-NLS-1$
        processSource(child, news);
    }
  }

  private void processEnclosure(Element element, INews news) {
    URI attachmentUri = null;
    String attachmentType = null;
    int attachmentLength = -1;

    /* Interpret Attributes */
    List<?> attachmentAttributes = element.getAttributes();
    for (Iterator<?> iter = attachmentAttributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* URL */
      if ("url".equals(name)) {//$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null)
          attachmentUri = uri;
      }

      /* Type */
      else if ("type".equals(name)) //$NON-NLS-1$
        attachmentType = attribute.getValue();

      /* Length */
      else if ("length".equals(name)) //$NON-NLS-1$
        attachmentLength = StringUtils.stringToInt(attribute.getValue());
    }

    /* Create Attachment only if valid */
    if (attachmentUri != null && !CoreUtils.hasAttachment(news, attachmentUri)) {
      IAttachment attachment = Owl.getModelFactory().createAttachment(null, news);
      attachment.setLink(attachmentUri);
      if (StringUtils.isSet(attachmentType))
        attachment.setType(attachmentType);
      if (attachmentLength != -1)
        attachment.setLength(attachmentLength);

      /* Check wether this Attribute is to be processed by a Contribution */
      for (Iterator<?> iter = attachmentAttributes.iterator(); iter.hasNext();) {
        Attribute attribute = (Attribute) iter.next();
        processAttributeExtern(attribute, attachment);
      }
    }
  }

  private void processSource(Element element, INews news) {
    ISource source = Owl.getModelFactory().createSource(news);
    source.setName(element.getText());

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, source);

    /* Interpret Attributes */
    List<?> attributes = element.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Check wether this Attribute is to be processed by a Contribution */
      if (processAttributeExtern(attribute, source))
        continue;

      /* URL */
      else if ("url".equals(name)) { //$NON-NLS-1$
        URI uri = URIUtils.createURI(attribute.getValue());
        if (uri != null)
          source.setLink(uri);
      }
    }
  }

  private void processGuid(Element element, INews news) {
    Boolean permaLink = null;

    /* Interpret Attributes */
    List<?> attributes = element.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName().toLowerCase();

      /* Is Permalink */
      if ("ispermalink".equals(name)) //$NON-NLS-1$
        permaLink = Boolean.parseBoolean(attribute.getValue());
    }
    Owl.getModelFactory().createGuid(news, element.getText(), permaLink);
  }

  private void processTextInput(Element element, IFeed feed) {
    ITextInput input = Owl.getModelFactory().createTextInput(feed);

    /* Check wether the Attributes are to be processed by a Contribution */
    processNamespaceAttributes(element, input);

    /* Interpret Attributes */
    List<?> inputChilds = element.getChildren();
    for (Iterator<?> iter = inputChilds.iterator(); iter.hasNext();) {
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
}