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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "org.rssowl.ui.internal.actions.messages"; //$NON-NLS-1$
  public static String ArchiveNewsAction_ARCHIVE;
  public static String ArchiveNewsAction_ARCHIVE_NEWS;
  public static String ArchiveNewsAction_ARCHIVE_NEWS_BINDING;
  public static String AssignLabelsAction_ASSIGN_LABELS;
  public static String AutomateFilterAction_AUTOMATE_COPY;
  public static String AutomateFilterAction_AUTOMATE_DOWNLOAD;
  public static String AutomateFilterAction_AUTOMATE_LABEL;
  public static String AutomateFilterAction_AUTOMATE_MOVE;
  public static String CopyLinkAction_COPY_LINK;
  public static String DeleteTypesAction_CONFIRM_DELETE;
  public static String DeleteTypesAction_CONFIRM_DELETE_ARCHIVE;
  public static String DeleteTypesAction_CONFIRM_DELETE_ARCHIVE_N;
  public static String DeleteTypesAction_CONFIRM_DELETE_BOOKMARK;
  public static String DeleteTypesAction_CONFIRM_DELETE_ELEMENTS;
  public static String DeleteTypesAction_CONFIRM_DELETE_FOLDER;
  public static String DeleteTypesAction_CONFIRM_DELETE_GROUP;
  public static String DeleteTypesAction_CONFIRM_DELETE_NEWS;
  public static String DeleteTypesAction_CONFIRM_DELETE_NEWSBIN;
  public static String DeleteTypesAction_CONFIRM_DELETE_SEARCH;
  public static String DeleteTypesAction_NO_UNDO;
  public static String DeleteTypesAction_NOTE_FOLDER_ARCHIVE;
  public static String DeleteTypesAction_NOTE_GROUP_ARCHIVE;
  public static String DeleteTypesAction_NOTE_SELECTION_ARCHIVE;
  public static String DeleteTypesAction_WAIT_DELETE;
  public static String EntityPropertyDialogAction_N_BIN;
  public static String EntityPropertyDialogAction_N_BINS;
  public static String EntityPropertyDialogAction_N_BOOKMARK;
  public static String EntityPropertyDialogAction_N_BOOKMARKS;
  public static String EntityPropertyDialogAction_N_FOLDER;
  public static String EntityPropertyDialogAction_N_FOLDERS;
  public static String EntityPropertyDialogAction_N_SEARCH;
  public static String EntityPropertyDialogAction_N_SEARCHES;
  public static String EntityPropertyDialogAction_PROPERTIES_FOR_N;
  public static String FindAction_FIND;
  public static String FindExtensionsAction_FIND_ADDONS;
  public static String FindExtensionsAction_NO_ADDONS_FOUND;
  public static String FindExtensionsAction_NO_UPDATES_FOUND;
  public static String FindExtensionsAction_RSSOWL_ADDONS;
  public static String FindExtensionsAction_SEARCHING_EXTENSIONS;
  public static String FindExtensionsAction_UPDATE_IN_PROGRESS;
  public static String FindUpdatesAction_CHECK_UPDATES;
  public static String FindUpdatesAction_DOWNLOADING_UPDATES;
  public static String FindUpdatesAction_NO_UPDATES_AVAILABLE;
  public static String FindUpdatesAction_REASON;
  public static String FindUpdatesAction_RESTART_AFTER_UPDATE;
  public static String FindUpdatesAction_RESTART_RSSOWL;
  public static String FindUpdatesAction_UPDATE_SEARCH;
  public static String FindUpdatesAction_WARNING_SEARCH_FAILED;
  public static String FindUpdatesAction_WARNING_UPDATE_FAILED;

  public static String LabelAction_LABEL_BINDING;
  public static String LabelAction_REMOVE_ALL_LABELS;

  public static String MakeNewsStickyAction_NEWS_STICKY;
  public static String MakeNewsStickyAction_NEWS_STICKY_BINDING;
  public static String ManageConfigurationAction_MANAGE_ADDONS;
  public static String MarkAllNewsReadAction_MARK_ALL_READ;
  public static String MoveCopyNewsToBinAction_NEW_NEWSBIN;

  public static String NavigationActionFactory_NEXT_FEED;
  public static String NavigationActionFactory_NEXT_NEWS;
  public static String NavigationActionFactory_NEXT_TAB;
  public static String NavigationActionFactory_NEXT_UNREAD_FEED;
  public static String NavigationActionFactory_NEXT_UNREAD_NEWS;
  public static String NavigationActionFactory_PREVIOUS_FEED;
  public static String NavigationActionFactory_PREVIOUS_NEWS;
  public static String NavigationActionFactory_PREVIOUS_TAB;
  public static String NavigationActionFactory_PREVIOUS_UNREAD_FEED;
  public static String NavigationActionFactory_PREVIOUS_UNREAD_NEWS;

  public static String NewFolderAction_FOLDER;
  public static String NewFolderAction_LOCATION;
  public static String NewFolderAction_NAME;
  public static String NewFolderAction_NEW_FOLDER;
  public static String NewFolderAction_NEW_FOLDER_MSG;
  public static String NewFolderAction_NEW_SET;
  public static String NewFolderAction_NEW_SET_MSG;

  public static String NewNewsBinAction_LOCATION;
  public static String NewNewsBinAction_NAME;
  public static String NewNewsBinAction_NEW_NEWSBIN;
  public static String NewNewsBinAction_NEW_NEWSBIN_MSG;
  public static String NewNewsBinAction_NEWSBIN;
  public static String NewTypeDropdownAction_BOOKMARK;
  public static String NewTypeDropdownAction_FOLDER;
  public static String NewTypeDropdownAction_LABEL_BINDING;
  public static String NewTypeDropdownAction_NEWSBIN;
  public static String NewTypeDropdownAction_SAVED_SEARCH;

  public static String OpenAction_OPEN;
  public static String OpenInBrowserAction_LOADING;
  public static String OpenInBrowserAction_OPEN_IN_BROWSER;
  public static String OpenInExternalBrowserAction_OPEN_IN_EXTERNAL_BROWSER;
  public static String OpenInNewTabAction_OPEN_ALL_IN_TABS;
  public static String OpenInNewTabAction_OPEN_IN_NEW_TAB;
  public static String OpenInNewTabAction_OPEN_IN_NEW_TABS;
  public static String OpenNewsAction_OPEN;
  public static String ReloadAllAction_UPDATE_ALL;
  public static String SearchInTypeAction_SEARCH_NEWS;
  public static String SearchNewsAction_SEARCH_NEWS;

  public static String ToggleReadStateAction_NEWS_READ;
  public static String ToggleReadStateAction_NEWS_READ_BINDING;

  private Messages() {}

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}
