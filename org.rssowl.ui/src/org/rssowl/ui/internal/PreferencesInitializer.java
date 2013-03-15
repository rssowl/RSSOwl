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

package org.rssowl.ui.internal;

import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferencesInitializer;
import org.rssowl.core.persist.pref.Preference;
import org.rssowl.ui.internal.actions.NavigationActionFactory.NavigationActionType;
import org.rssowl.ui.internal.editors.feed.NewsColumn;
import org.rssowl.ui.internal.editors.feed.NewsFilter;
import org.rssowl.ui.internal.views.explorer.BookMarkFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of <code>IPreferencesInitializer</code> responsible for defining
 * the default preferences for the UI of RSSOwl.
 *
 * @author bpasero
 */
public class PreferencesInitializer implements IPreferencesInitializer {

  /*
   * @see
   * org.rssowl.core.model.preferences.IPreferencesInitializer#initialize(org
   * .rssowl.core.model.preferences.IPreferencesScope)
   */
  public void initialize(IPreferenceScope defaultScope) {

    /* Default Globals */
    initGlobalDefaults(defaultScope);

    /* Default Eclipse Globals */
    initGlobalEclipseDefaults(defaultScope);

    /* Default News Column Settings */
    initNewsColumnsDefaults(defaultScope);

    /* Default Retention Policy */
    initRetentionDefaults(defaultScope);

    /* Default Clean Up */
    initCleanUpDefaults(defaultScope);

    /* Default Display Settings */
    initDisplayDefaults(defaultScope);

    /* Default BookMark Explorer */
    initBookMarkExplorerDefaults(defaultScope);

    /* Default Feed View */
    initFeedViewDefaults(defaultScope);

    /* Default Reload/Open Settings */
    initReloadOpenDefaults(defaultScope);

    /* Toolbar Item Settings */
    initToolbarDefaults(defaultScope);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initGlobalDefaults(IPreferenceScope defaultScope) {
    defaultScope.putBoolean(Preference.USE_OS_PASSWORD.id(), true);
    defaultScope.putBoolean(Preference.REMEMBER_PASSWORD.id(), true);
    defaultScope.putBoolean(Preference.MARK_READ_ON_SCROLLING.id(), true);
    defaultScope.putBoolean(Preference.MARK_READ_ON_MINIMIZE.id(), false);
    defaultScope.putBoolean(Preference.MARK_READ_ON_CHANGE.id(), false);
    defaultScope.putBoolean(Preference.MARK_READ_ON_TAB_CLOSE.id(), false);
    defaultScope.putBoolean(Preference.MARK_READ_DUPLICATES.id(), true);
    defaultScope.putBoolean(Preference.DISABLE_JAVASCRIPT.id(), Application.IS_WINDOWS);
    defaultScope.putBoolean(Preference.USE_DEFAULT_EXTERNAL_BROWSER.id(), true);
    defaultScope.putBoolean(Preference.TRAY_ON_MINIMIZE.id(), false);
    defaultScope.putBoolean(Preference.RESTORE_TRAY_DOUBLECLICK.id(), Application.IS_WINDOWS);
    defaultScope.putBoolean(Preference.MARK_READ_STATE.id(), true);
    defaultScope.putInteger(Preference.MARK_READ_IN_MILLIS.id(), 0);
    defaultScope.putBoolean(Preference.BM_OPEN_SITE_FOR_EMPTY_NEWS.id(), false);
    defaultScope.putBoolean(Preference.FADE_NOTIFIER.id(), true);
    defaultScope.putBoolean(Preference.CLOSE_NOTIFIER_ON_OPEN.id(), true);
    defaultScope.putInteger(Preference.LIMIT_NOTIFICATION_SIZE.id(), 5);
    defaultScope.putBoolean(Preference.SHOW_NOTIFICATION_POPUP.id(), true);
    defaultScope.putBoolean(Preference.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED.id(), true);
    defaultScope.putBoolean(Preference.SEARCH_DIALOG_PREVIEW_VISIBLE.id(), true);
    defaultScope.putInteger(Preference.AUTOCLOSE_NOTIFICATION_VALUE.id(), 8);
    defaultScope.putBoolean(Preference.SHOW_TOOLBAR.id(), true);
    defaultScope.putBoolean(Preference.SHOW_STATUS.id(), true);
    defaultScope.putBoolean(Preference.BM_LOAD_TITLE_FROM_FEED.id(), true);
    defaultScope.putBoolean(Preference.UPDATE_ON_STARTUP.id(), true);
    defaultScope.putString(Preference.BM_TRANSFORMER_ID.id(), "org.rssowl.ui.InstapaperTransformer"); //$NON-NLS-1$
    defaultScope.putInteger(Preference.NEWS_BROWSER_PAGE_SIZE.id(), 50);

    defaultScope.putIntegers(Preference.SEARCH_DIALOG_NEWS_COLUMNS.id(), new int[] {
      NewsColumn.RELEVANCE.ordinal(),
      NewsColumn.TITLE.ordinal(),
      NewsColumn.FEED.ordinal(),
      NewsColumn.DATE.ordinal(),
      NewsColumn.AUTHOR.ordinal(),
      NewsColumn.CATEGORY.ordinal(),
      NewsColumn.STICKY.ordinal()
    });

    defaultScope.putInteger(Preference.SEARCH_DIALOG_NEWS_SORT_COLUMN.id(), NewsColumn.RELEVANCE.ordinal());
    defaultScope.putBoolean(Preference.SEARCH_DIALOG_NEWS_SORT_ASCENDING.id(), false);

    defaultScope.putIntegers(Preference.SHARE_PROVIDER_STATE.id(), new int[] { 3, 5, 1, 6, 7, 24, -8, -19, -9, -10, -12, -13, -4, -14, -15, -16, -17, -11, -18, -20, -21, -2, -22, -23, -25, -26 });
    defaultScope.putInteger(Preference.BM_MENU_FILTER.id(), BookMarkFilter.Type.SHOW_ALL.ordinal());
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initGlobalEclipseDefaults(IPreferenceScope defaultScope) {
    defaultScope.putBoolean(Preference.ECLIPSE_SINGLE_CLICK_OPEN.id(), true);
    defaultScope.putBoolean(Preference.ECLIPSE_RESTORE_TABS.id(), true);
    defaultScope.putBoolean(Preference.ECLIPSE_MULTIPLE_TABS.id(), true);
    defaultScope.putInteger(Preference.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD.id(), 5);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initRetentionDefaults(IPreferenceScope defaultScope) {
    defaultScope.putBoolean(Preference.DEL_NEWS_BY_COUNT_STATE.id(), true);
    defaultScope.putInteger(Preference.DEL_NEWS_BY_COUNT_VALUE.id(), 200);
    defaultScope.putInteger(Preference.DEL_NEWS_BY_AGE_VALUE.id(), 30);
    defaultScope.putBoolean(Preference.NEVER_DEL_LABELED_NEWS_STATE.id(), true);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initCleanUpDefaults(IPreferenceScope defaultScope) {
    defaultScope.putBoolean(Preference.CLEAN_UP_BM_BY_LAST_UPDATE_STATE.id(), true);
    defaultScope.putInteger(Preference.CLEAN_UP_BM_BY_LAST_UPDATE_VALUE.id(), 30);

    defaultScope.putBoolean(Preference.CLEAN_UP_BM_BY_LAST_VISIT_STATE.id(), true);
    defaultScope.putInteger(Preference.CLEAN_UP_BM_BY_LAST_VISIT_VALUE.id(), 30);

    defaultScope.putInteger(Preference.CLEAN_UP_NEWS_BY_COUNT_VALUE.id(), 200);
    defaultScope.putInteger(Preference.CLEAN_UP_NEWS_BY_AGE_VALUE.id(), 30);

    defaultScope.putBoolean(Preference.CLEAN_UP_REMINDER_STATE.id(), true);
    defaultScope.putInteger(Preference.CLEAN_UP_REMINDER_DAYS_VALUE.id(), 30);

    defaultScope.putBoolean(Preference.CLEAN_UP_BM_BY_SYNCHRONIZATION.id(), true);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initDisplayDefaults(IPreferenceScope defaultScope) {
    defaultScope.putInteger(Preference.BM_NEWS_FILTERING.id(), -1);
    defaultScope.putInteger(Preference.BM_NEWS_GROUPING.id(), -1);
    defaultScope.putBoolean(Preference.BM_LOAD_IMAGES.id(), true);
    defaultScope.putBoolean(Preference.ENABLE_IMAGES.id(), true);
    defaultScope.putBoolean(Preference.ENABLE_MEDIA.id(), true);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initReloadOpenDefaults(IPreferenceScope defaultScope) {
    defaultScope.putBoolean(Preference.BM_UPDATE_INTERVAL_STATE.id(), true);
    defaultScope.putLong(Preference.BM_UPDATE_INTERVAL.id(), 60 * 30); // 30 Minutes
    defaultScope.putBoolean(Preference.BM_OPEN_ON_STARTUP.id(), false);
    defaultScope.putBoolean(Preference.BM_RELOAD_ON_STARTUP.id(), false);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initNewsColumnsDefaults(IPreferenceScope defaultScope) {
    defaultScope.putIntegers(Preference.BM_NEWS_COLUMNS.id(), new int[] {
      NewsColumn.TITLE.ordinal(),
      NewsColumn.DATE.ordinal(),
      NewsColumn.AUTHOR.ordinal(),
      NewsColumn.CATEGORY.ordinal(),
      NewsColumn.STICKY.ordinal()
    });
    defaultScope.putInteger(Preference.BM_NEWS_SORT_COLUMN.id(), NewsColumn.DATE.ordinal());
    defaultScope.putBoolean(Preference.BM_NEWS_SORT_ASCENDING.id(), false);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initBookMarkExplorerDefaults(IPreferenceScope defaultScope) {
    defaultScope.putBoolean(Preference.BE_BEGIN_SEARCH_ON_TYPING.id(), true);
    defaultScope.putBoolean(Preference.BE_SORT_BY_NAME.id(), false);
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  protected void initFeedViewDefaults(IPreferenceScope defaultScope) {
    defaultScope.putIntegers(Preference.FV_SASHFORM_WEIGHTS.id(), new int[] { 50, 50 });
    defaultScope.putBoolean(Preference.BM_OPEN_SITE_FOR_NEWS.id(), false);
    defaultScope.putInteger(Preference.FV_SEARCH_TARGET.id(), NewsFilter.SearchTarget.ALL.ordinal());
  }

  /**
   * @param defaultScope the container for preferences to fill.
   */
  private void initToolbarDefaults(IPreferenceScope defaultScope) {
    List<Integer> items = new ArrayList<Integer>();

    /* New | Import | Export */
    items.add(CoolBarAdvisor.CoolBarItem.NEW.ordinal());

    /* Undo | Redo */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.UNDO.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.REDO.ordinal());

    /* Update | Update All */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.UPDATE.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.UPDATE_ALL.ordinal());

    /* Search */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.SEARCH.ordinal());

    /* Mark Read | Mark All Read */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.MARK_READ.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.MARK_ALL_READ.ordinal());

    /* Archive */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.ARCHIVE.ordinal());

    /* Label | Sticky */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.LABEL.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.STICKY.ordinal());

    /* Share */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.SHARE.ordinal());

    /* Next | Previous */
    items.add(CoolBarAdvisor.CoolBarItem.SEPARATOR.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.NEXT.ordinal());
    items.add(CoolBarAdvisor.CoolBarItem.PREVIOUS.ordinal());

    int[] intArray= new int[items.size()];
    for(int i = 0; i < items.size(); i++)
      intArray[i] = items.get(i);

    defaultScope.putIntegers(Preference.TOOLBAR_ITEMS.id(), intArray);
    defaultScope.putInteger(Preference.TOOLBAR_MODE.id(), CoolBarAdvisor.CoolBarMode.IMAGE_TEXT_VERTICAL.ordinal());
    defaultScope.putInteger(Preference.DEFAULT_NEXT_ACTION.id(), NavigationActionType.NEXT_UNREAD_NEWS.ordinal());
    defaultScope.putInteger(Preference.DEFAULT_PREVIOUS_ACTION.id(), NavigationActionType.PREVIOUS_UNREAD_NEWS.ordinal());
  }
}