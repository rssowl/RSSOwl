/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.dialogs.cleanup.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

import java.util.Collection;

/**
 * Delete BookMarks no longer subscribed to in Google Reader
 */
public class DeleteFeedsWithBrokenSynchronizationOperation extends DeleteFeedsAbstractOperation {
  private boolean isEnabled;

  @SuppressWarnings("unused")
  public DeleteFeedsWithBrokenSynchronizationOperation(boolean isEnabled, Object data) {
    this.isEnabled = isEnabled;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void savePreferences(IPreferenceScope preferences) {
    if (SyncUtils.hasSyncCredentials())
      preferences.putBoolean(DefaultPreferences.CLEAN_UP_BM_BY_SYNCHRONIZATION, isEnabled);
  }

  @Override
  public Collection<IBookMark> filter(Collection<IBookMark> bookmarks, IProgressMonitor monitor) {
//    Collection<IBookMark> result = new ArrayList<IBookMark>();
//
//    Set<String> googleReaderFeeds = loadGoogleReaderFeeds(monitor);
//    for (IBookMark mark : bookmarks) {
//      if (!SyncUtils.isSynchronized(mark))
//        continue;
//
//      String feedLink = URIUtils.toHTTP(mark.getFeedLinkReference().getLinkAsText());
//      if (!googleReaderFeeds.contains(feedLink)) {
//        result.add(mark);
//      }
//    }
//    return result;
    return null;
  }

  @Override
  protected SummaryModelTaskGroup createGroup() {
    return new SummaryModelTaskGroup(Messages.CleanUpModel_DELETE_UNSUBSCRIBED_FEEDS);
  }

  //  private Set<String> loadGoogleReaderFeeds(IProgressMonitor monitor) {
  //    InputStream inS = null;
  //    boolean isCanceled = false;
  //    try {
  //
  //      /* Obtain Google Credentials */
  //      ICredentials credentials = Owl.getConnectionService().getAuthCredentials(URI.create(SyncUtils.GOOGLE_LOGIN_URL), null);
  //      if (credentials == null)
  //        return null;
  //
  //      /* Load Google Auth Token */
  //      String authToken = SyncUtils.getGoogleAuthToken(credentials.getUsername(), credentials.getPassword(), false, monitor);
  //      if (authToken == null)
  //        authToken = SyncUtils.getGoogleAuthToken(credentials.getUsername(), credentials.getPassword(), true, monitor);
  //
  //      /* Return on Cancellation */
  //      if (monitor.isCanceled() || !StringUtils.isSet(authToken))
  //        return null;
  //
  //      /* Import from Google */
  //      URI opmlImportUri = URI.create(SyncUtils.GOOGLE_READER_OPML_URI);
  //      IProtocolHandler handler = Owl.getConnectionService().getHandler(opmlImportUri);
  //
  //      Map<Object, Object> properties = new HashMap<Object, Object>();
  //      Map<String, String> headers = new HashMap<String, String>();
  //      headers.put("Authorization", SyncUtils.getGoogleAuthorizationHeader(authToken)); //$NON-NLS-1$
  //      properties.put(IConnectionPropertyConstants.HEADERS, headers);
  //
  //      inS = handler.openStream(opmlImportUri, monitor, properties);
  //
  //      /* Return on Cancellation */
  //      if (monitor.isCanceled()) {
  //        isCanceled = true;
  //        return null;
  //      }
  //
  //      /* Find Bookmarks */
  //      List<IEntity> types = Owl.getInterpreter().importFrom(inS);
  //      Set<IBookMark> bookmarks = new HashSet<IBookMark>();
  //      for (IEntity type : types) {
  //        if (type instanceof IBookMark)
  //          bookmarks.add((IBookMark) type);
  //        else if (type instanceof IFolder)
  //          CoreUtils.fillBookMarks(bookmarks, Collections.singleton((IFolder) type));
  //      }
  //
  //      Set<String> feeds = new HashSet<String>();
  //      for (IBookMark bookmark : bookmarks) {
  //        feeds.add(bookmark.getFeedLinkReference().getLinkAsText());
  //      }
  //
  //      feeds.add(URIUtils.toHTTP(SyncUtils.GOOGLE_READER_NOTES_FEED));
  //      feeds.add(URIUtils.toHTTP(SyncUtils.GOOGLE_READER_SHARED_ITEMS_FEED));
  //      feeds.add(URIUtils.toHTTP(SyncUtils.GOOGLE_READER_RECOMMENDED_ITEMS_FEED));
  //
  //      return feeds;
  //    } catch (CoreException e) {
  //      Activator.getDefault().logError(e.getMessage(), e);
  //    } finally {
  //      if (inS != null) {
  //        try {
  //          if ((isCanceled && inS instanceof IAbortable))
  //            ((IAbortable) inS).abort();
  //          else
  //            inS.close();
  //        } catch (IOException e) {
  //          Activator.getDefault().logError(e.getMessage(), e);
  //        }
  //      }
  //    }
  //
  //    return null;
  //  }

}
