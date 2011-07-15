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
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;

import java.util.Iterator;
import java.util.List;

/**
 * Handler for the iTunes Podcast Namespace (not all elements are covered!).
 * <p>
 * Namespace Prefix: itunes<br>
 * Namespace URI: http://www.itunes.com/dtds/podcast-1.0.dtd
 * </p>
 *
 * @author bpasero
 */
public class PodcastNamespaceHandler implements INamespaceHandler {

  /*
   * @see org.rssowl.core.interpreter.INamespaceHandler#processAttribute(org.jdom.Attribute,
   * org.rssowl.core.model.types.IExtendableType)
   */
  public void processAttribute(Attribute attribute, IPersistable type) {}

  /*
   * @see org.rssowl.core.interpreter.INamespaceHandler#processElement(org.jdom.Element,
   * org.rssowl.core.model.types.IExtendableType)
   */
  public void processElement(Element element, IPersistable type) {
    IModelFactory factory = Owl.getModelFactory();

    /* Category */
    if ("category".equals(element.getName())) { //$NON-NLS-1$

      /* Process Top-Category (Level 1) */
      processCategory(element, type);

      /* Look for Subcategories (Level 2) */
      List< ? > children = element.getChildren();
      for (Iterator< ? > iter = children.iterator(); iter.hasNext();) {
        Element child = (Element) iter.next();
        if ("category".equals(child.getName())) //$NON-NLS-1$
          processCategory(child, type);
      }
    }

    /* Author */
    else if ("author".equals(element.getName())) { //$NON-NLS-1$
      IPerson person = factory.createPerson(null, type);
      person.setName(element.getText());
    }
  }

  private void processCategory(Element element, IPersistable type) {
    IModelFactory factory = Owl.getModelFactory();

    ICategory category = factory.createCategory(null, (IEntity) type);
    List< ? > attributes = element.getAttributes();
    for (Iterator< ? > iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName();

      if ("text".equals(name)) { //$NON-NLS-1$
        category.setName(attribute.getValue());
        break;
      }
    }
  }
}