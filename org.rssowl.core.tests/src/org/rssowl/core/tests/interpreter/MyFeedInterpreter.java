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

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.rssowl.core.Owl;
import org.rssowl.core.interpreter.IFormatInterpreter;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Part of the Interpreter Test Suite.
 *
 * @author bpasero
 */
public class MyFeedInterpreter implements IFormatInterpreter {

  /*
   * @see org.rssowl.core.interpreter.IFormatInterpreter#interpret(org.jdom.Document,
   * org.rssowl.core.model.types.IFeed)
   */
  @SuppressWarnings("unused")
  public void interpret(Document document, IFeed feed) throws InterpreterException {
    Element root = document.getRootElement();
    feed.setFormat("MyFeed"); //$NON-NLS-1$
    processMyFeed(root, feed);
  }

  private void processMyFeed(Element element, IFeed feed) {

    /* Interpret Children */
    List< ? > feedChildren = element.getChildren();
    for (Iterator< ? > iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Process Meta */
      if ("meta".equals(name)) //$NON-NLS-1$
        processMeta(child, feed);

      /* Process MyChannel */
      else if ("mychannel".equals(name)) //$NON-NLS-1$
        processMyChannel(child, feed);
    }
  }

  private void processMeta(Element child, IFeed feed) {
    Element title = child.getChild("titel"); //$NON-NLS-1$
    feed.setTitle(title.getText());
  }

  private void processMyChannel(Element child, IFeed feed) {
    Attribute lang = child.getAttribute("sprache"); //$NON-NLS-1$
    feed.setLanguage(lang.getValue());

    Element newsElement = child.getChild("news"); //$NON-NLS-1$
    INews news = Owl.getModelFactory().createNews(null, feed, new Date());
    news.setTitle(newsElement.getChildText("titel")); //$NON-NLS-1$
    try {
      news.setLink(new URI(newsElement.getChildText("verweis"))); //$NON-NLS-1$
    } catch (URISyntaxException e) {
    }

    news.setDescription(newsElement.getChildText("beschreibung")); //$NON-NLS-1$
  }
}