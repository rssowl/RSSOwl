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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.ui.internal.FolderNewsMark;

/**
 * @author bpasero
 */
public class FeedViewFactory implements IElementFactory {

  /*
   * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
   */
  public IAdaptable createElement(IMemento memento) {

    /* Check for IMark */
    String inputId = memento.getString(FeedViewInput.MARK_INPUT_ID);
    if (inputId != null) {
      String inputClass = memento.getString(FeedViewInput.MARK_INPUT_CLASS);

      /* Special Case Folders */
      if (FolderNewsMark.class.getName().equals(inputClass)) {
        IFolder folder = DynamicDAO.load(IFolder.class, Long.valueOf(inputId));
        if (folder != null)
          return new FeedViewInput(new FolderNewsMark(folder));
      }

      /* Normal NewsMark */
      else {
        try {
          Class<?> klass = Class.forName(inputClass, true, Thread.currentThread().getContextClassLoader());
          if (INewsMark.class.isAssignableFrom(klass)) {
            Class<? extends INewsMark> markClass = klass.asSubclass(INewsMark.class);
            INewsMark mark = DynamicDAO.load(markClass, Long.valueOf(inputId));
            if (mark != null)
              return new FeedViewInput(mark);
          } else
            throw new IllegalStateException(FeedViewInput.MARK_INPUT_CLASS + " does not implement IMark: " + inputClass); //$NON-NLS-1$
        } catch (ClassNotFoundException e) {
          /* Should never happen */
          throw new IllegalStateException(e);
        }
      }
    }

    return null;
  }
}