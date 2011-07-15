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

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.rssowl.ui.internal.Activator;

/**
 * An instance of <code>IEditorMatchingStrategy</code> that checks wether a
 * given Editor-Input is already showing in the Editor.
 * 
 * @author bpasero
 */
public class FeedViewMatcher implements IEditorMatchingStrategy {

  /*
   * @see org.eclipse.ui.IEditorMatchingStrategy#matches(org.eclipse.ui.IEditorReference,
   * org.eclipse.ui.IEditorInput)
   */
  public boolean matches(IEditorReference editorRef, IEditorInput input) {

    /* Require FeedViewInput */
    if (!(input instanceof FeedViewInput))
      return false;

    try {
      return editorRef.getEditorInput().equals(input);
    } catch (PartInitException e) {
      Activator.getDefault().getLog().log(e.getStatus());
      return false;
    }
  }
}