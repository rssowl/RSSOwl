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

package org.rssowl.core.internal.persist.pref;

import org.rssowl.core.persist.pref.Preference;

/**
 * Static access to {@link Preference}. Held for history reasons due to many
 * references from org.rssowl.ui bundle.
 *
 * @author bpasero
 */
public class DefaultPreferences {

  /** Global: Token to indicate if RSSOwl has been started before or not */
  public static final String FIRST_START_TOKEN = Preference.FIRST_START_TOKEN.id();

  /** Global: Use Master Password to encrypt passwords to feeds */
  public static final String USE_MASTER_PASSWORD = Preference.USE_MASTER_PASSWORD.id();

  /** Global: Use OS Password to encrypt passwords to feeds */
  public static final String USE_OS_PASSWORD = Preference.USE_OS_PASSWORD.id();

  /** Global: Remember Password */
  public static final String REMEMBER_PASSWORD = Preference.REMEMBER_PASSWORD.id();

  /** Global: Mark all news as read on minimize */
  public static final String MARK_READ_ON_MINIMIZE = Preference.MARK_READ_ON_MINIMIZE.id();

  /** Global: Mark read while scrolling in newspaper layout */
  public static final String MARK_READ_ON_SCROLLING = Preference.MARK_READ_ON_SCROLLING.id();

  /** Global: Mark feed as read when feed changes */
  public static final String MARK_READ_ON_CHANGE = Preference.MARK_READ_ON_CHANGE.id();

  /** Global: Mark all news as read on tab close */
  public static final String MARK_READ_ON_TAB_CLOSE = Preference.MARK_READ_ON_TAB_CLOSE.id();

  /** Global: Update read state of duplicates */
  public static final String MARK_READ_DUPLICATES = Preference.MARK_READ_DUPLICATES.id();

  /** Global: Disable JavaScript */
  public static final String DISABLE_JAVASCRIPT = Preference.DISABLE_JAVASCRIPT.id();

  /** Global: Disable JavaScript Exceptions */
  public static final String DISABLE_JAVASCRIPT_EXCEPTIONS = Preference.DISABLE_JAVASCRIPT_EXCEPTIONS.id();

  /** Global: Use default external browser */
  public static final String USE_DEFAULT_EXTERNAL_BROWSER = Preference.USE_DEFAULT_EXTERNAL_BROWSER.id();

  /** Global: Use custom external browser */
  public static final String USE_CUSTOM_EXTERNAL_BROWSER = Preference.USE_CUSTOM_EXTERNAL_BROWSER.id();

  /** Global: Path to the custom Browser */
  public static final String CUSTOM_BROWSER_PATH = Preference.CUSTOM_BROWSER_PATH.id();

  /** Global: Re-Open last opened Browser on Startup */
  public static final String REOPEN_BROWSER_TABS = Preference.REOPEN_BROWSER_TABS.id();

  /** Global: Minimize to the system tray */
  public static final String TRAY_ON_MINIMIZE = Preference.TRAY_ON_MINIMIZE.id();

  /** Global: Minimize to the system tray on Shell Close */
  public static final String TRAY_ON_CLOSE = Preference.TRAY_ON_CLOSE.id();

  /** Global: Minimize to the system tray on Application Start */
  public static final String TRAY_ON_START = Preference.TRAY_ON_START.id();

  /** Global: Restore from Tray with a Double Click */
  public static final String RESTORE_TRAY_DOUBLECLICK = Preference.RESTORE_TRAY_DOUBLECLICK.id();

  /** Global: Mark Read state */
  public static final String MARK_READ_STATE = Preference.MARK_READ_STATE.id();

  /** Global: Mark Read after X seconds */
  public static final String MARK_READ_IN_MILLIS = Preference.MARK_READ_IN_MILLIS.id();

  /** Retention Policy: Delete News > N (boolean) */
  public static final String DEL_NEWS_BY_COUNT_STATE = Preference.DEL_NEWS_BY_COUNT_STATE.id();

  /** Retention Policy: Delete News > N (int) */
  public static final String DEL_NEWS_BY_COUNT_VALUE = Preference.DEL_NEWS_BY_COUNT_VALUE.id();

  /** Retention Policy: Delete News > N Days (boolean) */
  public static final String DEL_NEWS_BY_AGE_STATE = Preference.DEL_NEWS_BY_AGE_STATE.id();

  /** Retention Policy: Delete News > N Days (int) */
  public static final String DEL_NEWS_BY_AGE_VALUE = Preference.DEL_NEWS_BY_AGE_VALUE.id();

  /** Retention Policy: Delete read News (boolean) */
  public static final String DEL_READ_NEWS_STATE = Preference.DEL_READ_NEWS_STATE.id();

  /** Retention Policy: Never Delete Unread News (boolean) */
  public static final String NEVER_DEL_UNREAD_NEWS_STATE = Preference.NEVER_DEL_UNREAD_NEWS_STATE.id();

  /** Retention Policy: Never Delete Labeled News (boolean) */
  public static final String NEVER_DEL_LABELED_NEWS_STATE = Preference.NEVER_DEL_LABELED_NEWS_STATE.id();

  /** BookMarks: Visible Columns */
  public static final String BM_NEWS_COLUMNS = Preference.BM_NEWS_COLUMNS.id();

  /** BookMarks: Sorted Column */
  public static final String BM_NEWS_SORT_COLUMN = Preference.BM_NEWS_SORT_COLUMN.id();

  /** BookMarks: Ascended / Descended Sorting */
  public static final String BM_NEWS_SORT_ASCENDING = Preference.BM_NEWS_SORT_ASCENDING.id();

  /** BookMarks: Auto-Update Interval (integer) */
  public static final String BM_UPDATE_INTERVAL = Preference.BM_UPDATE_INTERVAL.id();

  /** BookMarks: Auto-Update Interval State (boolean) */
  public static final String BM_UPDATE_INTERVAL_STATE = Preference.BM_UPDATE_INTERVAL_STATE.id();

  /** BookMarks: Open on Startup */
  public static final String BM_OPEN_ON_STARTUP = Preference.BM_OPEN_ON_STARTUP.id();

  /** BookMarks: Reload on Startup */
  public static final String BM_RELOAD_ON_STARTUP = Preference.BM_RELOAD_ON_STARTUP.id();

  /** Feed View: Search Target */
  public static final String FV_SEARCH_TARGET = Preference.FV_SEARCH_TARGET.id();

  /** Feed View: Selected Grouping (Deprecated as of RSSOwl 2.0.2) */
  public static final String FV_GROUP_TYPE = Preference.FV_GROUP_TYPE.id();

  /** Feed View: Selected Filter (Deprecated as of RSSOwl 2.0.2) */
  public static final String FV_FILTER_TYPE = Preference.FV_FILTER_TYPE.id();

  /** Feed View: SashForm Weights */
  public static final String FV_SASHFORM_WEIGHTS = Preference.FV_SASHFORM_WEIGHTS.id();

  /** Feed View: Layout */
  public static final String FV_LAYOUT = Preference.FV_LAYOUT.id();

  /** Feed View: Highlight Search Results */
  public static final String FV_HIGHLIGHT_SEARCH_RESULTS = Preference.FV_HIGHLIGHT_SEARCH_RESULTS.id();

  /** Feed View: Feed Toolbar Visibility */
  public static final String FV_FEED_TOOLBAR_HIDDEN = Preference.FV_FEED_TOOLBAR_HIDDEN.id();

  /** Feed View: Browser Toolbar Visibility */
  public static final String FV_BROWSER_TOOLBAR_HIDDEN = Preference.FV_BROWSER_TOOLBAR_HIDDEN.id();

  /** BookMark Explorer */
  public static final String BE_BEGIN_SEARCH_ON_TYPING = Preference.BE_BEGIN_SEARCH_ON_TYPING.id();

  /** BookMark Explorer */
  public static final String BE_ALWAYS_SHOW_SEARCH = Preference.BE_ALWAYS_SHOW_SEARCH.id();

  /** BookMark Explorer */
  public static final String BE_SORT_BY_NAME = Preference.BE_SORT_BY_NAME.id();

  /** BookMark Explorer */
  public static final String BE_FILTER_TYPE = Preference.BE_FILTER_TYPE.id();

  /** BookMark Explorer */
  public static final String BE_GROUP_TYPE = Preference.BE_GROUP_TYPE.id();

  /** BookMark Explorer */
  public static final String BE_ENABLE_LINKING = Preference.BE_ENABLE_LINKING.id();

  /** BookMark Explorer */
  public static final String BE_DISABLE_FAVICONS = Preference.BE_DISABLE_FAVICONS.id();

  /** BookMark News-Grouping */
  public static final String BM_NEWS_FILTERING = Preference.BM_NEWS_FILTERING.id();

  /** BookMark News-Filtering */
  public static final String BM_NEWS_GROUPING = Preference.BM_NEWS_GROUPING.id();

  /** BookMark Load Images (deprecated as of 2.1) */
  public static final String BM_LOAD_IMAGES = Preference.BM_LOAD_IMAGES.id();

  /** Enable Images in Article Content */
  public static final String ENABLE_IMAGES = Preference.ENABLE_IMAGES.id();

  /** Enable Media in Article Content */
  public static final String ENABLE_MEDIA = Preference.ENABLE_MEDIA.id();

  /** NewsMark Selected News */
  public static final String NM_SELECTED_NEWS = Preference.NM_SELECTED_NEWS.id();

  /** Global: Open Website instead of showing News */
  public static final String BM_OPEN_SITE_FOR_NEWS = Preference.BM_OPEN_SITE_FOR_NEWS.id();

  /** Global: Open Website instead of showing News when description is empty */
  public static final String BM_OPEN_SITE_FOR_EMPTY_NEWS = Preference.BM_OPEN_SITE_FOR_EMPTY_NEWS.id();

  /** Global: Use Link Transformer */
  public static final String BM_USE_TRANSFORMER= Preference.BM_USE_TRANSFORMER.id();

  /** Global: Used Link Transformer Identifier */
  public static final String BM_TRANSFORMER_ID = Preference.BM_TRANSFORMER_ID.id();

  /** Global: Show Notification Popup */
  public static final String SHOW_NOTIFICATION_POPUP = Preference.SHOW_NOTIFICATION_POPUP.id();

  /** Global: Show Notification Popup only from Tray */
  public static final String SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED = Preference.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED.id();

  /** Global: Leave Notification Popup open until closed */
  public static final String STICKY_NOTIFICATION_POPUP = Preference.STICKY_NOTIFICATION_POPUP.id();

  /** Global: Auto Close Time */
  public static final String AUTOCLOSE_NOTIFICATION_VALUE = Preference.AUTOCLOSE_NOTIFICATION_VALUE.id();

  /** Global: Limit number of News in notification */
  public static final String LIMIT_NOTIFICATION_SIZE = Preference.LIMIT_NOTIFICATION_SIZE.id();

  /** Global: Limit Notifier to Selected Elements */
  public static final String LIMIT_NOTIFIER_TO_SELECTION = Preference.LIMIT_NOTIFIER_TO_SELECTION.id();

  /** Global: Close Notifier after clicking on Item */
  public static final String CLOSE_NOTIFIER_ON_OPEN = Preference.CLOSE_NOTIFIER_ON_OPEN.id();

  /** Global: Enable Notifier for Element */
  public static final String ENABLE_NOTIFIER = Preference.ENABLE_NOTIFIER.id();

  /** Global: Use transparency fade in / fade out */
  public static final String FADE_NOTIFIER = Preference.FADE_NOTIFIER.id();

  /** Global: Show Description Excerpt in Notifier */
  public static final String SHOW_EXCERPT_IN_NOTIFIER = Preference.SHOW_EXCERPT_IN_NOTIFIER.id();

  /** Global: Always reuse feed view */
  public static final String ALWAYS_REUSE_FEEDVIEW = Preference.ALWAYS_REUSE_FEEDVIEW.id();

  /** Global: Always reuse Browser */
  public static final String ALWAYS_REUSE_BROWSER = Preference.ALWAYS_REUSE_BROWSER.id();

  /** Global: Open Links in New Tab */
  public static final String OPEN_LINKS_IN_NEW_TAB = Preference.OPEN_LINKS_NEW_TAB.id();

  /** Global: Clean Up: Delete BMs by last visit (state) */
  public static final String CLEAN_UP_BM_BY_LAST_VISIT_STATE = Preference.CLEAN_UP_BM_BY_LAST_VISIT_STATE.id();

  /** Global: Clean Up: Delete BMs by last visit (value) */
  public static final String CLEAN_UP_BM_BY_LAST_VISIT_VALUE = Preference.CLEAN_UP_BM_BY_LAST_VISIT_VALUE.id();

  /** Global: Clean Up: Delete BMs by last update (state) */
  public static final String CLEAN_UP_BM_BY_LAST_UPDATE_STATE = Preference.CLEAN_UP_BM_BY_LAST_UPDATE_STATE.id();

  /** Global: Clean Up: Delete BMs by last update (value) */
  public static final String CLEAN_UP_BM_BY_LAST_UPDATE_VALUE = Preference.CLEAN_UP_BM_BY_LAST_UPDATE_VALUE.id();

  /** Global: Clean Up: Delete BMs with a connection error */
  public static final String CLEAN_UP_BM_BY_CON_ERROR = Preference.CLEAN_UP_BM_BY_CON_ERROR.id();

  /** Global: Clean Up: Delete BMs no longer subscribed to in Google Reader */
  public static final String CLEAN_UP_BM_BY_SYNCHRONIZATION = Preference.CLEAN_UP_BM_BY_SYNCHRONIZATION.id();

  /** Global: Clean Up: Delete duplicate BMs */
  public static final String CLEAN_UP_BM_BY_DUPLICATES = Preference.CLEAN_UP_BM_BY_DUPLICATES.id();

  /** Global: Clean Up: Delete News > N (boolean) */
  public static final String CLEAN_UP_NEWS_BY_COUNT_STATE = Preference.CLEAN_UP_NEWS_BY_COUNT_STATE.id();

  /** Global: Clean Up: Delete News > N (int) */
  public static final String CLEAN_UP_NEWS_BY_COUNT_VALUE = Preference.CLEAN_UP_NEWS_BY_COUNT_VALUE.id();

  /** Global: Clean Up: Delete News > N Days (boolean) */
  public static final String CLEAN_UP_NEWS_BY_AGE_STATE = Preference.CLEAN_UP_NEWS_BY_AGE_STATE.id();

  /** Global: Clean Up: Delete News > N Days (int) */
  public static final String CLEAN_UP_NEWS_BY_AGE_VALUE = Preference.CLEAN_UP_NEWS_BY_AGE_VALUE.id();

  /** Global: Clean Up: Delete read News (boolean) */
  public static final String CLEAN_UP_READ_NEWS_STATE = Preference.CLEAN_UP_READ_NEWS_STATE.id();

  /** Global: Clean Up: Never Delete Unread News (boolean) */
  public static final String CLEAN_UP_NEVER_DEL_UNREAD_NEWS_STATE = Preference.CLEAN_UP_NEVER_DEL_UNREAD_NEWS_STATE.id();

  /** Global: Clean Up: Never Delete Labeled News (boolean) */
  public static final String CLEAN_UP_NEVER_DEL_LABELED_NEWS_STATE = Preference.CLEAN_UP_NEVER_DEL_LABELED_NEWS_STATE.id();

  /** Global: Clean Up: The Date of the next reminder for Clean-Up as Long */
  public static final String CLEAN_UP_REMINDER_DATE_MILLIES = Preference.CLEAN_UP_REMINDER_DATE_MILLIES.id();

  /** Global: Clean Up: Enabled state for the reminder for Clean-Up */
  public static final String CLEAN_UP_REMINDER_STATE = Preference.CLEAN_UP_REMINDER_STATE.id();

  /** Global: Clean Up: Number of days before showing the reminder for Clean-Up */
  public static final String CLEAN_UP_REMINDER_DAYS_VALUE = Preference.CLEAN_UP_REMINDER_DAYS_VALUE.id();

  /** Global: Clean Up: Search Index after restart */
  public static final String CLEAN_UP_INDEX = Preference.CLEAN_UP_INDEX.id();

  /** Global: Search Dialog: State of showing Preview */
  public static final String SEARCH_DIALOG_PREVIEW_VISIBLE = Preference.SEARCH_DIALOG_PREVIEW_VISIBLE.id();

  /** Global: Visible Columns in Search Dialog */
  public static final String SEARCH_DIALOG_NEWS_COLUMNS = Preference.SEARCH_DIALOG_NEWS_COLUMNS.id();

  /** Global: Sorted Column in Search Dialog */
  public static final String SEARCH_DIALOG_NEWS_SORT_COLUMN = Preference.SEARCH_DIALOG_NEWS_SORT_COLUMN.id();

  /** Global: Ascended / Descended Sorting in Search Dialog */
  public static final String SEARCH_DIALOG_NEWS_SORT_ASCENDING = Preference.SEARCH_DIALOG_NEWS_SORT_ASCENDING.id();

  /** Global: Show Toolbar */
  public static final String SHOW_TOOLBAR = Preference.SHOW_TOOLBAR.id();

  /** Global: Show Statusbar */
  public static final String SHOW_STATUS = Preference.SHOW_STATUS.id();

  /** Global: Load Title from Feed in Bookmark Wizard */
  public static final String BM_LOAD_TITLE_FROM_FEED = Preference.BM_LOAD_TITLE_FROM_FEED.id();

  /** Global: Last used Keyword Feed */
  public static final String LAST_KEYWORD_FEED = Preference.LAST_KEYWORD_FEED.id();

  /** Global: Open Browser Tabs in the Background */
  public static final String OPEN_BROWSER_IN_BACKGROUND = Preference.OPEN_BROWSER_IN_BACKGROUND.id();

  /** Global: Share Provider Order and Enablement */
  public static final String SHARE_PROVIDER_STATE = Preference.SHARE_PROVIDER_STATE.id();

  /** Global: Hide Completed Downloads */
  public static final String HIDE_COMPLETED_DOWNLOADS = Preference.HIDE_COMPLETED_DOWNLOADS.id();

  /** Global: List of Import Resources */
  public static final String IMPORT_RESOURCES = Preference.IMPORT_RESOURCES.id();

  /** Global: List of Import Keywords */
  public static final String IMPORT_KEYWORDS = Preference.IMPORT_KEYWORDS.id();

  /** Global: List of Items in Toolbar */
  public static final String TOOLBAR_ITEMS = Preference.TOOLBAR_ITEMS.id();

  /** Global: Toolbar Mode */
  public static final String TOOLBAR_MODE = Preference.TOOLBAR_MODE.id();

  /** Global: Default Next Action (Toolbar) */
  public static final String DEFAULT_NEXT_ACTION = Preference.DEFAULT_NEXT_ACTION.id();

  /** Global: Default Previous Action (Toolbar) */
  public static final String DEFAULT_PREVIOUS_ACTION = Preference.DEFAULT_PREVIOUS_ACTION.id();

  /** Global: Bookmark Menu Filter */
  public static final String BM_MENU_FILTER = Preference.BM_MENU_FILTER.id();

  /** Global: Check for Updates on Startup */
  public static final String UPDATE_ON_STARTUP = Preference.UPDATE_ON_STARTUP.id();

  /** Global: Remember selection for Aggregate News */
  public static final String REMEMBER_AGGREGATE_NEWS_OPTION = Preference.REMEMBER_AGGREGATE_NEWS_OPTION.id();

  /** Global: Aggregate News as Saved Search */
  public static final String AGGREGATE_NEWS_AS_SEARCH = Preference.AGGREGATE_NEWS_AS_SEARCH.id();

  /** Global: Localized Feed Search */
  public static final String LOCALIZED_FEED_SEARCH = Preference.LOCALIZED_FEED_SEARCH.id();

  /** Global: Last Used Folder for Downloads */
  public static final String DOWNLOAD_FOLDER = Preference.DOWNLOAD_FOLDER.id();

  /** Global: JavaScript Warning Closed */
  public static final String JS_INFOBAR_CLOSED = Preference.JS_INFOBAR_CLOSED.id();

  /** Global: Internet Explorer Popup Blocker */
  public static final String ENABLE_IE_POPUP_BLOCKER= Preference.ENABLE_IE_POPUP_BLOCKER.id();

  /** Global: Marker for the Archive Bin */
  public static final String ARCHIVE_BIN_MARKER = Preference.ARCHIVE_BIN_MARKER.id();

  /** Global: Number of Elements in Browser before starting to Page */
  public static final String NEWS_BROWSER_PAGE_SIZE = Preference.NEWS_BROWSER_PAGE_SIZE.id();

  /** Global: Deleted Labels (to ignore from synced feeds) */
  public static final String DELETED_LABELS = Preference.DELETED_LABELS.id();

  /**
   * Eclipse Preferences Follow
   */

  /** Global Eclipse: Open on Single Click */
  public static final String ECLIPSE_SINGLE_CLICK_OPEN = Preference.ECLIPSE_SINGLE_CLICK_OPEN.id();

  /** Global Eclipse: Restore Tabs on startup */
  public static final String ECLIPSE_RESTORE_TABS = Preference.ECLIPSE_RESTORE_TABS.id();

  /** Global Eclipse: Use multiple Tabs */
  public static final String ECLIPSE_MULTIPLE_TABS = Preference.ECLIPSE_MULTIPLE_TABS.id();

  /** Global Eclipse: Autoclose Tabs */
  public static final String ECLIPSE_AUTOCLOSE_TABS = Preference.ECLIPSE_AUTOCLOSE_TABS.id();

  /** Global Eclipse: Autoclose Tabs Threshold */
  public static final String ECLIPSE_AUTOCLOSE_TABS_THRESHOLD = Preference.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD.id();

  /** Global Eclipse: Use Proxy */
  public static final String ECLIPSE_USE_PROXY = Preference.ECLIPSE_USE_PROXY.id();

  /** Global Eclipse: Use System Proxy */
  public static final String ECLIPSE_USE_SYSTEM_PROXY = Preference.ECLIPSE_USE_SYSTEM_PROXY.id();

  /** Global Eclipse: Proxy Host */
  public static final String ECLIPSE_PROXY_HOST = Preference.ECLIPSE_PROXY_HOST_HTTP.id();

  /** Global Eclipse: Proxy Port */
  public static final String ECLIPSE_PROXY_PORT = Preference.ECLIPSE_PROXY_PORT_HTTP.id();
}