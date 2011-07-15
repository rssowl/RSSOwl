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
import org.rssowl.core.interpreter.IElementHandler;
import org.rssowl.core.interpreter.IFormatInterpreter;
import org.rssowl.core.interpreter.INamespaceHandler;
import org.rssowl.core.persist.IPersistable;

import java.util.Iterator;
import java.util.List;

/**
 * Super-Type of all Interpreters coming by default with RSSOwl. Implementing
 * some usefull utility methods to use by all.
 * 
 * @author bpasero
 */
public abstract class BasicInterpreter implements IFormatInterpreter {
  private String fDefaultNamespaceUri;
  private String fRootElementName;

  /**
   * Get the default NamespaceURI of the parsed Document.
   * 
   * @return The default NamespaceURI of the parsed Document.
   */
  protected String getDefaultNamespaceUri() {
    return fDefaultNamespaceUri;
  }

  /**
   * Set the default NamespaceURI of the parsed Document.
   * 
   * @param defaultNamespaceUri The default NamespaceURI of the parsed Document
   * to set.
   */
  protected void setDefaultNamespaceUri(String defaultNamespaceUri) {
    fDefaultNamespaceUri = defaultNamespaceUri;
  }

  /**
   * Get the Root-Element's name of the parsed Document.
   * 
   * @return Returns the Root-Element's name of the parsed Document.
   */
  protected String getRootElementName() {
    return fRootElementName;
  }

  /**
   * Set the Root-Element's name of the parsed Document.
   * 
   * @param rootElementName The Root-Element's name of the parsed Document to
   * set.
   */
  protected void setRootElementName(String rootElementName) {
    fRootElementName = rootElementName;
  }

  /**
   * Build a Format identifier from the given Format and Version.
   * 
   * @param format The Format of the Feed.
   * @param version The Version of the Feed.
   * @return String Format identifier based on both.
   */
  protected String buildFormat(String format, String version) {
    StringBuilder strBuf = new StringBuilder();
    strBuf.append(format).append(' ').append(version);
    return strBuf.toString();
  }

  /**
   * Check all Attributes of the given Element for contributed Namespace
   * Handler.
   * 
   * @param element The Element to check.
   * @param parent The Type this Element is belonging to.
   */
  protected void processNamespaceAttributes(Element element, IPersistable parent) {

    /* In case no Attributes present to interpret */
    if (element.getAttributes().isEmpty())
      return;

    /* Interpret Attributes */
    List< ? > attributes = element.getAttributes();
    for (Iterator< ? > iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();

      /* Check for contributed Namespace Handlers */
      processAttributeExtern(attribute, parent);
    }
  }

  /**
   * Check the Element for contributed Namespace Handler.
   * 
   * @param element The Element to check.
   * @param parent The Type this Element is belonging to.
   * @return TRUE in case a Handler is provided for this Element, FALSE
   * otherwise.
   */
  protected boolean processElementExtern(Element element, IPersistable parent) {
    String name = element.getName().toLowerCase();
    String namespaceURI = element.getNamespaceURI();

    /* First check for contributed Element Handlers */
    if (getDefaultNamespaceUri().equals(namespaceURI)) {
      IElementHandler elementHandler = Owl.getInterpreter().getElementHandler(name, getRootElementName());
      if (elementHandler != null) {
        elementHandler.processElement(element, parent);
        return true;
      }
    }

    /* Second check for contributed Namespace Handlers */
    else if (!getDefaultNamespaceUri().equals(namespaceURI) && namespaceURI != null) {
      INamespaceHandler handler = Owl.getInterpreter().getNamespaceHandler(namespaceURI);
      if (handler != null) {
        handler.processElement(element, parent);
        return true;
      }
    }

    /* This Element has not been processed externally */
    return false;
  }

  /**
   * Check the Attribute for contributed Namespace Handler.
   * 
   * @param attribute The Attribute to check.
   * @param parent The Type this Attribute is belonging to.
   * @return TRUE in case a Handler is provided for this Attribute, FALSE
   * otherwise.
   */
  protected boolean processAttributeExtern(Attribute attribute, IPersistable parent) {
    String namespaceURI = attribute.getNamespaceURI();

    /* Check for contributed Namespace Handlers */
    if (!getDefaultNamespaceUri().equals(namespaceURI)) {
      INamespaceHandler handler = Owl.getInterpreter().getNamespaceHandler(namespaceURI);
      if (handler != null) {
        handler.processAttribute(attribute, parent);
        return true;
      }
    }

    /* This Attribute has not been processed externally */
    return false;
  }
}