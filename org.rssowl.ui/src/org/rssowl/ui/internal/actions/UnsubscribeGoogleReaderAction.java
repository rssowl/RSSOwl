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

package org.rssowl.ui.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ReparentInfo;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.ConfirmDialog;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author bpasero
 */
public class UnsubscribeGoogleReaderAction implements IWorkbenchWindowActionDelegate {

  private IWorkbenchWindow fWindow;

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    fWindow = window;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    ConfirmDialog dialog = new ConfirmDialog(fWindow.getShell(), Messages.UnsubscribeGoogleReaderAction_0, Messages.UnsubscribeGoogleReaderAction_1, Messages.UnsubscribeGoogleReaderAction_2, Messages.UnsubscribeGoogleReaderAction_3, null) {

      @Override
      protected String getTitleLabel() {
        return Messages.UnsubscribeGoogleReaderAction_4;
      }

      @Override
      protected String getTitleImage() {
        return "/icons/wizban/reader_wiz.png"; //$NON-NLS-1$
      }
    };

    if (dialog.open() == IDialogConstants.OK_ID) {
      try {

        // Stop all pending reloads first to ensure smooth action run
        Controller.getDefault().stopUpdate();

        // Perform action
        List<IBookMark> feedsToReload = this.internalRun();
        if (feedsToReload.size() > 0) {
          new ReloadTypesAction(new StructuredSelection(feedsToReload), fWindow.getShell()).run();
        }
      } catch (PersistenceException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (URISyntaxException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }
  }

  private List<IBookMark> internalRun() throws URISyntaxException {
    List<IBookMark> feedsToReload = new ArrayList<IBookMark>();
    IFolder root = OwlUI.getSelectedBookMarkSet();
    IFolder archive = null;

    // Iterate over all bookmarks that are synchronized
    Collection<IBookMark> bookmarks = DynamicDAO.loadAll(IBookMark.class);
    for (IBookMark oldBookMark : bookmarks) {
      if (SyncUtils.isSynchronized(oldBookMark)) {
        String oldFeedLink = oldBookMark.getFeedLinkReference().getLinkAsText();

        // Ignore special Google Reader feeds
        if (!oldFeedLink.equals(SyncUtils.GOOGLE_READER_SHARED_ITEMS_FEED) && !oldFeedLink.equals(SyncUtils.GOOGLE_READER_RECOMMENDED_ITEMS_FEED) && !oldFeedLink.equals(SyncUtils.GOOGLE_READER_NOTES_FEED)) {

          // Create new bookmark with http/https link at the same position of the old bookmark
          URI newFeedLink;
          if (oldFeedLink.indexOf(SyncUtils.READER_HTTPS_SCHEME) == 0) {
            newFeedLink = new URI(StringUtils.replaceAll(oldFeedLink, SyncUtils.READER_HTTPS_SCHEME + "://", "https://")); //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            newFeedLink = new URI(StringUtils.replaceAll(oldFeedLink, SyncUtils.READER_HTTP_SCHEME + "://", "http://")); //$NON-NLS-1$ //$NON-NLS-2$
          }

          IBookMark newBookMark = Owl.getModelFactory().createBookMark(null, oldBookMark.getParent(), new FeedLinkReference(newFeedLink), oldBookMark.getName(), oldBookMark, true);
          feedsToReload.add(newBookMark);

          // Copy over properties
          Map<String, Serializable> properties = oldBookMark.getProperties();
          Set<String> keySet = properties.keySet();
          for (String key : keySet) {
            newBookMark.setProperty(key, properties.get(key));
          }
          newBookMark.setProperty(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE, true);
          newBookMark.setCreationDate(oldBookMark.getCreationDate());
          newBookMark.setLastVisitDate(oldBookMark.getLastVisitDate());
          newBookMark.setPopularity(oldBookMark.getPopularity());

          // Create the feed if it does not yet exist
          DAOService daoService = Owl.getPersistenceService().getDAOService();
          if (!daoService.getFeedDAO().exists(newFeedLink)) {
            IFeed feed = Owl.getModelFactory().createFeed(null, newFeedLink);
            feed = DynamicDAO.save(feed);
          }

          // Save folder where new bookmark is in
          DynamicDAO.save(oldBookMark.getParent());
        }

          // Disable automatic load for synchronized feeds
          oldBookMark.setProperty(DefaultPreferences.BM_RELOAD_ON_STARTUP, false);
          oldBookMark.setProperty(DefaultPreferences.BM_UPDATE_INTERVAL_STATE, false);
          DynamicDAO.save(oldBookMark);

        // Move old bookmark into archive
        if (archive == null) {
          archive = Owl.getModelFactory().createFolder(null, root, "Google Reader Archive"); //$NON-NLS-1$
          archive.setProperty(DefaultPreferences.BM_RELOAD_ON_STARTUP, false);
          archive.setProperty(DefaultPreferences.BM_UPDATE_INTERVAL_STATE, false);
          DynamicDAO.save(root);
        }

        List<ReparentInfo<IFolderChild, IFolder>> reparenting = new ArrayList<ReparentInfo<IFolderChild, IFolder>>();
        reparenting.add(ReparentInfo.create((IFolderChild) oldBookMark, archive, null, null));
        CoreUtils.reparentWithProperties(reparenting);
      }
    }

    // Sort by name
    if (archive != null) {
      archive.sort();
      DynamicDAO.save(archive);
    }

    return feedsToReload;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {}
}