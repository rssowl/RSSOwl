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

package org.rssowl.core.persist.pref;

/**
 * An enumeration listing the preferences stored via {@link IPreferenceScope}.
 *
 * @author bpasero
 */
public enum Preference {

  /** Global: Token to indicate if RSSOwl has been started before or not */
  FIRST_START_TOKEN("org.rssowl.ui.FirstStartToken", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Use Master Password to encrypt passwords to feeds */
  USE_MASTER_PASSWORD("org.rssowl.pref.UseMasterPassword", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Use OS Password to encrypt passwords to feeds */
  USE_OS_PASSWORD("org.rssowl.pref.UseOSPassword", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Remember Password */
  REMEMBER_PASSWORD("org.rssowl.pref.RememberPassword", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Mark all news as read on minimize */
  MARK_READ_ON_MINIMIZE("org.rssowl.pref.MarkNewsReadOnMinimize", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Mark news read on scrolling (Newspaper Layout only) */
  MARK_READ_ON_SCROLLING("org.rssowl.pref.MarkNewsReadOnScrolling", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Mark feed as read when feed changes */
  MARK_READ_ON_CHANGE("org.rssowl.pref.MarkFeedReadOnChange", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Mark all news as read on tab close */
  MARK_READ_ON_TAB_CLOSE("org.rssowl.pref.MarkNewsReadOnTabClose", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Update read and seen state of duplicates */
  MARK_READ_DUPLICATES("org.rssowl.pref.MarkDuplicateNewsRead", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Disable JavaScript */
  DISABLE_JAVASCRIPT("org.rssowl.pref.DisableJavaScript", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Disable JavaScript Exceptions */
  DISABLE_JAVASCRIPT_EXCEPTIONS("org.rssowl.pref.DisableJavaScriptExceptions", IPreferenceType.STRINGS), //$NON-NLS-1$

  /** Global: Use default external browser */
  USE_DEFAULT_EXTERNAL_BROWSER("org.rssowl.pref.UseExternalBrowser", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Use custom external browser */
  USE_CUSTOM_EXTERNAL_BROWSER("org.rssowl.pref.UseCustomExternalBrowser", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Path to the custom Browser */
  CUSTOM_BROWSER_PATH("org.rssowl.pref.CustomBrowserPath", IPreferenceType.STRING), //$NON-NLS-1$

  /** Global: Re-Open last opened Browser on Startup */
  REOPEN_BROWSER_TABS("org.rssowl.pref.ReopenBrowserTabs", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Minimize to the system tray */
  TRAY_ON_MINIMIZE("org.rssowl.pref.UseSystemTray", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Minimize to the system tray on Shell Close */
  TRAY_ON_CLOSE("org.rssowl.pref.TrayOnExit", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Minimize to the system tray on Application Start */
  TRAY_ON_START("org.rssowl.pref.TrayOnStart", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Restore from Tray with a Double Click */
  RESTORE_TRAY_DOUBLECLICK("org.rssowl.pref.RestoreTrayDoubleclick", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Mark Read state */
  MARK_READ_STATE("org.rssowl.pref.MarkReadState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Mark Read after X seconds */
  MARK_READ_IN_MILLIS("org.rssowl.pref.MarkReadInMillis", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Retention Policy: Delete News > N (boolean) */
  DEL_NEWS_BY_COUNT_STATE("org.rssowl.pref.DelNewsByCountState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Retention Policy: Delete News > N (int) */
  DEL_NEWS_BY_COUNT_VALUE("org.rssowl.pref.DelNewsByCountValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Retention Policy: Delete News > N Days (boolean) */
  DEL_NEWS_BY_AGE_STATE("org.rssowl.pref.DelNewsByAgeState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Retention Policy: Delete News > N Days (int) */
  DEL_NEWS_BY_AGE_VALUE("org.rssowl.pref.DelNewsByAgeValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Retention Policy: Delete read News (boolean) */
  DEL_READ_NEWS_STATE("org.rssowl.pref.DelReadNewsState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Retention Policy: Never Delete Unread News (boolean) */
  NEVER_DEL_UNREAD_NEWS_STATE("org.rssowl.pref.NeverDelUnreadNewsState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Retention Policy: Never Delete Labeled News (boolean) */
  NEVER_DEL_LABELED_NEWS_STATE("org.rssowl.pref.NeverDelLabeledNewsState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMarks: Visible Columns */
  BM_NEWS_COLUMNS("org.rssowl.pref.BMNewsColumns", IPreferenceType.INTEGERS), //$NON-NLS-1$

  /** BookMarks: Sorted Column */
  BM_NEWS_SORT_COLUMN("org.rssowl.pref.BMNewsSortColumn", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** BookMarks: Ascended / Descended Sorting */
  BM_NEWS_SORT_ASCENDING("org.rssowl.pref.BMNewsSortAscending", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMarks: Auto-Update Interval (integer) */
  BM_UPDATE_INTERVAL("org.rssowl.pref.BMUpdateInterval", IPreferenceType.LONG), //$NON-NLS-1$

  /** BookMarks: Auto-Update Interval State (boolean) */
  BM_UPDATE_INTERVAL_STATE("org.rssowl.pref.BMUpdateIntervalState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMarks: Open on Startup */
  BM_OPEN_ON_STARTUP("org.rssowl.pref.BMOpenOnStartup", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMarks: Reload on Startup */
  BM_RELOAD_ON_STARTUP("org.rssowl.pref.BMReloadOnStartup", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Feed View: Search Target */
  FV_SEARCH_TARGET("org.rssowl.ui.internal.editors.feed.SearchTarget", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Feed View: Selected Grouping (Deprecated as of RSSOwl 2.0.2) */
  FV_GROUP_TYPE("org.rssowl.ui.internal.editors.feed.GroupType", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Feed View: Selected Filter (Deprecated as of RSSOwl 2.0.2) */
  FV_FILTER_TYPE("org.rssowl.ui.internal.editors.feed.FilterType", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Feed View: SashForm Weights */
  FV_SASHFORM_WEIGHTS("org.rssowl.ui.internal.editors.feed.SashFormWeights", IPreferenceType.INTEGERS), //$NON-NLS-1$

  /** Feed View: Layout */
  FV_LAYOUT("org.rssowl.ui.internal.editors.feed.Layout", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Feed View: Highlight Search Results */
  FV_HIGHLIGHT_SEARCH_RESULTS("org.rssowl.ui.internal.editors.feed.HighlightSearchResults", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Feed View: Feed Toolbar Visibility */
  FV_FEED_TOOLBAR_HIDDEN("org.rssowl.ui.internal.editors.feed.FeedToolBarHidden", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Feed View: Browser Toolbar Visibility */
  FV_BROWSER_TOOLBAR_HIDDEN("org.rssowl.ui.internal.editors.feed.BrowserToolBarHidden", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_BEGIN_SEARCH_ON_TYPING("org.rssowl.ui.internal.views.explorer.BeginSearchOnTyping", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_ALWAYS_SHOW_SEARCH("org.rssowl.ui.internal.views.explorer.AlwaysShowSearch", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_SORT_BY_NAME("org.rssowl.ui.internal.views.explorer.SortByName", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_FILTER_TYPE("org.rssowl.ui.internal.views.explorer.FilterType", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_GROUP_TYPE("org.rssowl.ui.internal.views.explorer.GroupingType", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_ENABLE_LINKING("org.rssowl.ui.internal.views.explorer.EnableLinking", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMark Explorer */
  BE_DISABLE_FAVICONS("org.rssowl.ui.internal.views.explorer.DisableFavicons", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** BookMark News-Grouping */
  BM_NEWS_FILTERING("org.rssowl.pref.BMNewsFiltering", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** BookMark News-Filtering */
  BM_NEWS_GROUPING("org.rssowl.pref.BMNewsGrouping", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** BookMark Load Images (deprecated as of 2.1) */
  BM_LOAD_IMAGES("org.rssowl.pref.BMLoadImages", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Enable Images in Article Content */
  ENABLE_IMAGES("org.rssowl.pref.EnableImages", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Enable Media in Article Content */
  ENABLE_MEDIA("org.rssowl.pref.EnableMedia", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** NewsMark Selected News */
  NM_SELECTED_NEWS("org.rssowl.pref.NMSelectedNews", IPreferenceType.LONG, IPreferenceScope.Kind.ENTITY), //$NON-NLS-1$

  /** Mark: Open Website instead of showing News */
  BM_OPEN_SITE_FOR_NEWS("org.rssowl.pref.BMOpenSiteForNews", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Open Website instead of showing News when description is empty */
  BM_OPEN_SITE_FOR_EMPTY_NEWS("org.rssowl.pref.OpenSiteForEmptyNews", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Use Link Transformer */
  BM_USE_TRANSFORMER("org.rssowl.pref.UseTransformer", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Used Link Transformer Identifier */
  BM_TRANSFORMER_ID("org.rssowl.pref.TransformerIdentifier", IPreferenceType.STRING), //$NON-NLS-1$

  /** Global: Show Notification Popup */
  SHOW_NOTIFICATION_POPUP("org.rssowl.pref.ShowNotificationPoup", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Show Notification Popup only from Tray */
  SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED("org.rssowl.pref.ShowNotificationPoupOnlyWhenMinimized", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Leave Notification Popup open until closed */
  STICKY_NOTIFICATION_POPUP("org.rssowl.pref.StickyNotificationPoup", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Auto Close Time */
  AUTOCLOSE_NOTIFICATION_VALUE("org.rssowl.pref.AutoCloseNotificationPoupValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Limit number of News in notification */
  LIMIT_NOTIFICATION_SIZE("org.rssowl.pref.LimitNotificationPoup", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Limit Notifier to Selected Elements */
  LIMIT_NOTIFIER_TO_SELECTION("org.rssowl.pref.LimitNotifierToSelection", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Close Notifier after clicking on Item */
  CLOSE_NOTIFIER_ON_OPEN("org.rssowl.pref.CloseNotifierOnOpen", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Enable Notifier for Element */
  ENABLE_NOTIFIER("org.rssowl.pref.EnableNotifier", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ENTITY), //$NON-NLS-1$

  /** Global: Use transparency fade in / fade out */
  FADE_NOTIFIER("org.rssowl.pref.FadeNotifier", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Show Description Excerpt in Notifier */
  SHOW_EXCERPT_IN_NOTIFIER("org.rssowl.pref.ShowExcerptInNotifier", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Always reuse feed view */
  ALWAYS_REUSE_FEEDVIEW("org.rssowl.pref.AlwaysReuseFeedView", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Always reuse Browser */
  ALWAYS_REUSE_BROWSER("org.rssowl.pref.AlwaysReuseBrowser", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Open Links in New Tab */
  OPEN_LINKS_NEW_TAB("org.rssowl.pref.OpenLinksInNewTab", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete BMs by last visit (state) */
  CLEAN_UP_BM_BY_LAST_VISIT_STATE("org.rssowl.pref.CleanUpBMByLastVisitState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete BMs by last visit (value) */
  CLEAN_UP_BM_BY_LAST_VISIT_VALUE("org.rssowl.pref.CleanUpBMByLastVisitValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Clean Up: Delete BMs by last update (state) */
  CLEAN_UP_BM_BY_LAST_UPDATE_STATE("org.rssowl.pref.CleanUpBMByLastUpdateState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete BMs by last update (value) */
  CLEAN_UP_BM_BY_LAST_UPDATE_VALUE("org.rssowl.pref.CleanUpBMByLastUpdateValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Clean Up: Delete BMs with a connection error */
  CLEAN_UP_BM_BY_CON_ERROR("org.rssowl.pref.CleanUpBMByConError", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete BMs no longer subscribed to in Google Reader */
  CLEAN_UP_BM_BY_SYNCHRONIZATION("org.rssowl.pref.CleanUpBMBySynchronization", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete duplicate BMs */
  CLEAN_UP_BM_BY_DUPLICATES("org.rssowl.pref.CleanUpBMByDuplicates", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete News > N (boolean) */
  CLEAN_UP_NEWS_BY_COUNT_STATE("org.rssowl.pref.CleanUpNewsByCountState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete News > N (int) */
  CLEAN_UP_NEWS_BY_COUNT_VALUE("org.rssowl.pref.CleanUpNewsByCountValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Clean Up: Delete News > N Days (boolean) */
  CLEAN_UP_NEWS_BY_AGE_STATE("org.rssowl.pref.CleanUpNewsByAgeState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Delete News > N Days (int) */
  CLEAN_UP_NEWS_BY_AGE_VALUE("org.rssowl.pref.CleanUpNewsByAgeValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Clean Up: Delete read News (boolean) */
  CLEAN_UP_READ_NEWS_STATE("org.rssowl.pref.CleanUpReadNewsState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Never Delete Unread News (boolean) */
  CLEAN_UP_NEVER_DEL_UNREAD_NEWS_STATE("org.rssowl.pref.CleanUpNeverDelUnreadNewsState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Never Delete Labeled News (boolean) */
  CLEAN_UP_NEVER_DEL_LABELED_NEWS_STATE("org.rssowl.pref.CleanUpNeverDelLabeledNewsState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: The Date of the next reminder for Clean-Up as Long */
  CLEAN_UP_REMINDER_DATE_MILLIES("org.rssowl.pref.CleanUpReminderDateMillies", IPreferenceType.LONG), //$NON-NLS-1$

  /** Global: Clean Up: Enabled state for the reminder for Clean-Up */
  CLEAN_UP_REMINDER_STATE("org.rssowl.pref.CleanUpReminderState", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Clean Up: Number of days before showing the reminder for Clean-Up */
  CLEAN_UP_REMINDER_DAYS_VALUE("org.rssowl.pref.CleanUpReminderDaysValue", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Clean Up: Search Index after restart */
  CLEAN_UP_INDEX("org.rssowl.pref.CleanUpIndex", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Search Dialog: State of showing Preview */
  SEARCH_DIALOG_PREVIEW_VISIBLE("org.rssowl.pref.SearchDialogPreviewVisible", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Visible Columns in Search Dialog */
  SEARCH_DIALOG_NEWS_COLUMNS("org.rssowl.pref.SearchDialogNewsColumns", IPreferenceType.INTEGERS), //$NON-NLS-1$

  /** Global: Sorted Column in Search Dialog */
  SEARCH_DIALOG_NEWS_SORT_COLUMN("org.rssowl.pref.SearchDialogNewsSortColumn", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Ascended / Descended Sorting in Search Dialog */
  SEARCH_DIALOG_NEWS_SORT_ASCENDING("org.rssowl.pref.SearchDialogNewsSortAscending", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Show Toolbar */
  SHOW_TOOLBAR("org.rssowl.pref.ShowToolbar", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Show Statusbar */
  SHOW_STATUS("org.rssowl.pref.ShowStatus", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Load Title from Feed in Bookmark Wizard */
  BM_LOAD_TITLE_FROM_FEED("org.rssowl.pref.BMLoadTitleFromFeed", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Last used Keyword Feed */
  LAST_KEYWORD_FEED("org.rssowl.pref.LastKeywordFeed", IPreferenceType.STRING), //$NON-NLS-1$

  /** Global: Open Browser Tabs in the Background */
  OPEN_BROWSER_IN_BACKGROUND("org.rssowl.pref.OpenBrowserInBackground", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Share Provider Order and Enablement */
  SHARE_PROVIDER_STATE("org.rssowl.pref.ShareProviderState", IPreferenceType.INTEGERS), //$NON-NLS-1$

  /** Global: Hide Completed Downloads */
  HIDE_COMPLETED_DOWNLOADS("org.rssowl.pref.HideCompletedDownloads", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: List of Import Resources */
  IMPORT_RESOURCES("org.rssowl.pref.ImportResources", IPreferenceType.STRINGS), //$NON-NLS-1$

  /** Global: List of Import Keywords */
  IMPORT_KEYWORDS("org.rssowl.pref.ImportKeywords", IPreferenceType.STRINGS), //$NON-NLS-1$

  /** Global: List of Items in Toolbar */
  TOOLBAR_ITEMS("org.rssowl.pref.ToolbarItems", IPreferenceType.INTEGERS), //$NON-NLS-1$

  /** Global: Toolbar Mode */
  TOOLBAR_MODE("org.rssowl.pref.ToolbarMode", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Default Next Action (Toolbar) */
  DEFAULT_NEXT_ACTION("org.rssowl.pref.DefaultNextAction", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Default Previous Action (Toolbar) */
  DEFAULT_PREVIOUS_ACTION("org.rssowl.pref.DefaultPreviousAction", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Bookmark Menu Filter */
  BM_MENU_FILTER("org.rssowl.pref.BMMenuFilter", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Check for Updates on Startup */
  UPDATE_ON_STARTUP("org.rssowl.pref.UpdateOnStartup", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Remember selection for Aggregate News */
  REMEMBER_AGGREGATE_NEWS_OPTION("org.rssowl.pref.RememberAggregateNewsOption", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Aggregate News as Search */
  AGGREGATE_NEWS_AS_SEARCH("org.rssowl.pref.AggregateNewsAsSearch", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Language Specific Feed Search */
  LOCALIZED_FEED_SEARCH("org.rssowl.pref.LocalizedFeedSearch", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Last Used Folder for Downloads */
  DOWNLOAD_FOLDER("org.rssowl.pref.DownloadFolder", IPreferenceType.STRING), //$NON-NLS-1$

  /** Global: JavaScript Warning Closed */
  JS_INFOBAR_CLOSED("org.rssowl.pref.JSInfobarClosed", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Internet Explorer Popup Blocker */
  ENABLE_IE_POPUP_BLOCKER("org.rssowl.pref.EnableIEPopupBlocker", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Archive Bin Marker */
  ARCHIVE_BIN_MARKER("org.rssowl.pref.ArchiveBinMarker", IPreferenceType.BOOLEAN), //$NON-NLS-1$

  /** Global: Number of Elements in Browser before starting to Page */
  NEWS_BROWSER_PAGE_SIZE("org.rssowl.pref.NewsBrowserPageSize", IPreferenceType.INTEGER), //$NON-NLS-1$

  /** Global: Deleted Labels (to ignore from synced feeds) */
  DELETED_LABELS("org.rssowl.pref.DeletedLabels", IPreferenceType.STRINGS), //$NON-NLS-1$

  /******************************
   * Eclipse Preferences Follow *
   ******************************/

  /** Global Eclipse: Open on Single Click */
  ECLIPSE_SINGLE_CLICK_OPEN("instance/org.eclipse.ui.workbench/OPEN_ON_SINGLE_CLICK", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Restore Tabs on startup */
  ECLIPSE_RESTORE_TABS("instance/org.eclipse.ui.workbench/USE_IPERSISTABLE_EDITORS", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Use multiple Tabs */
  ECLIPSE_MULTIPLE_TABS("instance/org.eclipse.ui/SHOW_MULTIPLE_EDITOR_TABS", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Autoclose Tabs */
  ECLIPSE_AUTOCLOSE_TABS("instance/org.eclipse.ui.workbench/REUSE_OPEN_EDITORS_BOOLEAN", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Autoclose Tabs Threshold */
  ECLIPSE_AUTOCLOSE_TABS_THRESHOLD("instance/org.eclipse.ui.workbench/REUSE_OPEN_EDITORS", IPreferenceType.INTEGER, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Use Proxy */
  ECLIPSE_USE_PROXY("/configuration/org.eclipse.core.net/proxiesEnabled", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Use System Proxy */
  ECLIPSE_USE_SYSTEM_PROXY("/configuration/org.eclipse.core.net/systemProxiesEnabled", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Host (HTTP) */
  ECLIPSE_PROXY_HOST_HTTP("/configuration/org.eclipse.core.net/proxyData/HTTP/host", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Port (HTTP) */
  ECLIPSE_PROXY_PORT_HTTP("/configuration/org.eclipse.core.net/proxyData/HTTP/port", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Authentication (HTTP) */
  ECLIPSE_PROXY_HTTP_HAS_AUTH("/configuration/org.eclipse.core.net/proxyData/HTTP/hasAuth", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Host (HTTPS) */
  ECLIPSE_PROXY_HOST_HTTPS("/configuration/org.eclipse.core.net/proxyData/HTTPS/host", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Port (HTTPS) */
  ECLIPSE_PROXY_PORT_HTTPS("/configuration/org.eclipse.core.net/proxyData/HTTPS/port", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Authentication (HTTPS) */
  ECLIPSE_PROXY_HTTPS_HAS_AUTH("/configuration/org.eclipse.core.net/proxyData/HTTPS/hasAuth", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Host (SOCKS) */
  ECLIPSE_PROXY_HOST_SOCKS("/configuration/org.eclipse.core.net/proxyData/SOCKS/host", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Port (SOCKS) */
  ECLIPSE_PROXY_PORT_SOCKS("/configuration/org.eclipse.core.net/proxyData/SOCKS/port", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Proxy Authentication (SOCKS) */
  ECLIPSE_PROXY_SOCKS_HAS_AUTH("/configuration/org.eclipse.core.net/proxyData/SOCKS/hasAuth", IPreferenceType.BOOLEAN, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Non Proxied Hosts */
  ECLIPSE_NON_PROXIED_HOSTS("/configuration/org.eclipse.core.net/nonProxiedHosts", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Newstext Font */
  ECLIPSE_NEWSTEXT_FONT("instance/org.eclipse.ui.workbench/org.rssowl.ui.NewsTextFont", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Headlines Font */
  ECLIPSE_HEADLINES_FONT("instance/org.eclipse.ui.workbench/org.rssowl.ui.HeadlinesFont", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Dialog Font */
  ECLIPSE_DIALOG_FONT("instance/org.eclipse.ui.workbench/org.eclipse.jface.dialogfont", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Part Font */
  ECLIPSE_PART_FONT("instance/org.eclipse.ui.workbench/org.eclipse.ui.workbench.TAB_TEXT_FONT", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Bookmark Explorer Font */
  ECLIPSE_BOOKMARK_FONT("instance/org.eclipse.ui.workbench/org.rssowl.ui.BookmarkExplorerFont", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Notification Popup Font */
  ECLIPSE_NOTIFICATION_FONT("instance/org.eclipse.ui.workbench/org.rssowl.ui.NotificationPopupFont", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Sticky Background Color */
  ECLIPSE_STICKYBG_COLOR("instance/org.eclipse.ui.workbench/org.rssowl.ui.StickyBGColor", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Search Highlight Background Color */
  ECLIPSE_SEARCH_COLOR("instance/org.eclipse.ui.workbench/org.rssowl.ui.SearchHighlightBGColor", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: News List Background Color */
  ECLIPSE_NEWSLIST_BACKGROUND_COLOR("instance/org.eclipse.ui.workbench/org.rssowl.ui.NewsListBackgroundColor", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Link Foreground Color */
  ECLIPSE_LINK_FOREGROUND_COLOR("instance/org.eclipse.ui.workbench/org.rssowl.ui.LinkFGColor", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE), //$NON-NLS-1$

  /** Global Eclipse: Keybindings */
  ECLIPSE_KEYS("instance/org.eclipse.ui.workbench/org.eclipse.ui.commands", IPreferenceType.STRING, IPreferenceScope.Kind.ECLIPSE); //$NON-NLS-1$

  private String fId;
  private IPreferenceType fType;
  private IPreferenceScope.Kind fKind;

  Preference(String id, IPreferenceType type) {
    this(id, type, IPreferenceScope.Kind.GLOBAL);
  }

  Preference(String id, IPreferenceType type, IPreferenceScope.Kind kind) {
    fId = id;
    fType = type;
    fKind = kind;
  }

  /**
   * @return the identifier of the preference.
   */
  public String id() {
    return fId;
  }

  /**
   * @return the data type of this preference.
   * @see IPreferenceType
   */
  public IPreferenceType getType() {
    return fType;
  }

  /**
   * @return the scope of this preference.
   */
  public IPreferenceScope.Kind getKind() {
    return fKind;
  }
}