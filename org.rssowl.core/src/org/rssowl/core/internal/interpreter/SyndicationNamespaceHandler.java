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
import org.rssowl.core.interpreter.INamespaceHandler;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.util.DateUtils;

/**
 * Handler for the Syndication Namespace
 * <p>
 * Namespace Prefix: sy<br>
 * Namespace URI: http://purl.org/rss/1.0/modules/syndication/
 * </p>
 *
 * @author bpasero
 */
public class SyndicationNamespaceHandler implements INamespaceHandler {

  /*
   * @see org.rssowl.core.interpreter.INamespaceHandler#processElement(org.jdom.Element,
   * org.rssowl.core.interpreter.types.IExtendableType)
   */
  public void processElement(Element element, IPersistable type) {
    String name = element.getName().toLowerCase();

    /* Update Period */
    if ("updateperiod".equals(name) && type instanceof IFeed) { //$NON-NLS-1$
      String period = element.getText().toLowerCase();
      int index = IFeed.PERIODS.indexOf(period);
      if (index >= 0)
        ((IFeed) type).setUpdatePeriod(index);
    }

    /* Update Frequency */
    else if ("updatefrequency".equals(name) && type instanceof IFeed) { //$NON-NLS-1$
      try {
        int value = Integer.parseInt(element.getText());
        ((IFeed) type).setUpdateFrequency(value);
      } catch (NumberFormatException e) {
        /* Ignore */
      }
    }

    /* Update Base */
    else if ("updatebase".equals(name) && type instanceof IFeed) //$NON-NLS-1$
      ((IFeed) type).setUpdateBase(DateUtils.parseDate(element.getText()));
  }

  /*
   * @see org.rssowl.core.interpreter.INamespaceHandler#processAttribute(org.jdom.Attribute,
   * org.rssowl.core.interpreter.types.IExtendableType)
   */
  public void processAttribute(Attribute attribute, IPersistable type) {}
}