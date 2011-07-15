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

package org.rssowl.core.tests.interpreter;

import org.jdom.Element;
import org.rssowl.core.interpreter.IElementHandler;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IPersistable;

/**
 * @author bpasero
 */
public class CustomCDFElementHandler implements IElementHandler {

  /*
   * @see org.rssowl.core.interpreter.IElementHandler#processElement(org.jdom.Element,
   * org.rssowl.core.model.types.IExtendableType)
   */
  public void processElement(Element element, IPersistable type) {
    if (type instanceof IEntity) {
      ((IEntity) type).setProperty(element.getText(), element.getText());
    }
  }
}
