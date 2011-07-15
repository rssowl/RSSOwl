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
import org.jdom.Element;
import org.rssowl.core.Owl;
import org.rssowl.core.interpreter.INamespaceHandler;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

/**
 * Handler for the Media Namespace.
 * <p>
 * Namespace Prefix: media<br>
 * Namespace URI: http://search.yahoo.com/mrss/
 * </p>
 *
 * @author bpasero
 */
public class MediaNamespaceHandler implements INamespaceHandler {

  /*
   * @see
   * org.rssowl.core.interpreter.INamespaceHandler#processAttribute(org.jdom
   * .Attribute, org.rssowl.core.persist.IPersistable)
   */
  public void processAttribute(Attribute attribute, IPersistable type) {}

  /*
   * @see
   * org.rssowl.core.interpreter.INamespaceHandler#processElement(org.jdom.Element
   * , org.rssowl.core.persist.IPersistable)
   */
  public void processElement(Element element, IPersistable type) {

    /* Contribution only valid for news */
    if (!(type instanceof INews))
      return;

    /* Media Group */
    String name = element.getName().toLowerCase();
    if ("group".equals(name)) { //$NON-NLS-1$
      List<?> groupChilds = element.getChildren();
      for (Iterator<?> iter = groupChilds.iterator(); iter.hasNext();) {
        Element child = (Element) iter.next();
        if ("content".equals(child.getName().toLowerCase())) //$NON-NLS-1$
          processContent(child, (INews) type);
      }
    }

    /* Media Content */
    else if ("content".equals(name)) { //$NON-NLS-1$
      processContent(element, (INews) type);
    }
  }

  private void processContent(Element element, INews news) {

    /* In case no Attributes present to interpret */
    if (element.getAttributes().isEmpty())
      return;

    URI attachmentUri = null;
    String attachmentType = null;
    int attachmentLength = -1;

    /* Interpret Attributes */
    List<?> attributes = element.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName();

      /* URL */
      if ("url".equals(name)) //$NON-NLS-1$
        attachmentUri = URIUtils.createURI(attribute.getValue());

      /* Type */
      else if ("type".equals(name)) //$NON-NLS-1$
        attachmentType = attribute.getValue();

      /* Length */
      else if ("fileSize".equals(name))//$NON-NLS-1$
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
    }
  }
}