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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.MonitorCanceledException;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.dao.IPreferenceDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.dialogs.CustomWizardDialog;
import org.rssowl.ui.internal.dialogs.LoginDialog;
import org.rssowl.ui.internal.editors.browser.WebBrowserInput;
import org.rssowl.ui.internal.editors.browser.WebBrowserView;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;
import org.rssowl.ui.internal.editors.feed.PerformAfterInputSet;
import org.rssowl.ui.internal.util.ContentAssistAdapter;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central Facade for UI-related tasks.
 *
 * @author bpasero
 */
public class OwlUI {

  /** Top-Level Menu ID for "Tools" */
  public static final String M_TOOLS = "tools"; //$NON-NLS-1$

  /** Top-Level Menu ID for "Mark" */
  public static final String M_MARK = "mark"; //$NON-NLS-1$

  /** Top-Level Menu ID for "Open" */
  public static final String M_OPEN = "open"; //$NON-NLS-1$

  /** Default */
  public static final ImageDescriptor UNKNOWN = Activator.getImageDescriptor("icons/obj16/default.gif"); //$NON-NLS-1$

  /** Folder */
  public static final ImageDescriptor FOLDER = Activator.getImageDescriptor("icons/obj16/folder.gif"); //$NON-NLS-1$

  /** Folder with new News */
  public static final ImageDescriptor FOLDER_NEW = Activator.getImageDescriptor("icons/obj16/folder_new.gif"); //$NON-NLS-1$

  /** Bookmark Set */
  public static final ImageDescriptor BOOKMARK_SET = Activator.getImageDescriptor("icons/obj16/bkmrk_set.gif"); //$NON-NLS-1$

  /** BookMark */
  public static final ImageDescriptor BOOKMARK = Activator.getImageDescriptor("icons/obj16/bookmark.gif"); //$NON-NLS-1$

  /** BookMark (Error) */
  public static final ImageDescriptor BOOKMARK_ERROR = Activator.getImageDescriptor("icons/obj16/bkmrk_error.gif"); //$NON-NLS-1$

  /** NewsBin */
  public static final ImageDescriptor NEWSBIN = Activator.getImageDescriptor("icons/obj16/newsbin.gif"); //$NON-NLS-1$

  /** NewsBin (New) */
  public static final ImageDescriptor NEWSBIN_NEW = Activator.getImageDescriptor("icons/obj16/newsbin_new.gif"); //$NON-NLS-1$

  /** NewsBin (Empty) */
  public static final ImageDescriptor NEWSBIN_EMPTY = Activator.getImageDescriptor("icons/obj16/newsbin_empty.gif"); //$NON-NLS-1$

  /** SearchMark */
  public static final ImageDescriptor SEARCHMARK = Activator.getImageDescriptor("icons/obj16/searchmark.gif"); //$NON-NLS-1$

  /** SearchMark (New) */
  public static final ImageDescriptor SEARCHMARK_NEW = Activator.getImageDescriptor("icons/obj16/searchmark_new.gif"); //$NON-NLS-1$

  /** SearchMark (Empty) */
  public static final ImageDescriptor SEARCHMARK_EMPTY = Activator.getImageDescriptor("icons/obj16/searchmark_empty.gif"); //$NON-NLS-1$

  /** Group */
  public static final ImageDescriptor GROUP = Activator.getImageDescriptor("icons/obj16/group.gif"); //$NON-NLS-1$

  /** News: Unread */
  public static final ImageDescriptor NEWS_STATE_UNREAD = Activator.getImageDescriptor("icons/obj16/news_unread.gif"); //$NON-NLS-1$

  /** News: Read */
  public static final ImageDescriptor NEWS_STATE_READ = Activator.getImageDescriptor("icons/obj16/news_read.gif"); //$NON-NLS-1$

  /** News: New */
  public static final ImageDescriptor NEWS_STATE_NEW = Activator.getImageDescriptor("icons/obj16/news_new.gif"); //$NON-NLS-1$

  /** News: Updated */
  public static final ImageDescriptor NEWS_STATE_UPDATED = Activator.getImageDescriptor("icons/obj16/news_updated.gif"); //$NON-NLS-1$

  /** News: Pin */
  public static final ImageDescriptor NEWS_PIN = Activator.getImageDescriptor("icons/obj16/news_pin.gif"); //$NON-NLS-1$

  /** News: Pinned */
  public static final ImageDescriptor NEWS_PINNED = Activator.getImageDescriptor("icons/obj16/news_pinned.gif"); //$NON-NLS-1$

  /** Tray Icon: Not Teasing */
  public static final ImageDescriptor TRAY_OWL = Activator.getImageDescriptor("icons/elcl16/trayowl.png"); //$NON-NLS-1$

  /** Tray Icon: Teasing */
  public static final ImageDescriptor TRAY_OWL_TEASING = Activator.getImageDescriptor("icons/elcl16/trayowl_tease.png"); //$NON-NLS-1$

  /** Info */
  public static final ImageDescriptor INFO = Activator.getImageDescriptor("icons/obj16/info.gif"); //$NON-NLS-1$

  /** Warning */
  public static final ImageDescriptor WARNING = Activator.getImageDescriptor("icons/obj16/warning.gif"); //$NON-NLS-1$

  /** Error */
  public static final ImageDescriptor ERROR = Activator.getImageDescriptor("icons/obj16/error.gif"); //$NON-NLS-1$

  /** Attachment */
  public static final ImageDescriptor ATTACHMENT = Activator.getImageDescriptor("icons/obj16/attachment.gif"); //$NON-NLS-1$

  /** Columns */
  public static final ImageDescriptor COLUMNS = Activator.getImageDescriptor("icons/etool16/columns.gif"); //$NON-NLS-1$

  /** Share */
  public static final ImageDescriptor SHARE = Activator.getImageDescriptor("icons/elcl16/share.gif"); //$NON-NLS-1$

  /** Filter */
  public static final ImageDescriptor FILTER = Activator.getImageDescriptor("icons/etool16/filter.gif"); //$NON-NLS-1$

  /** Archive */
  public static final ImageDescriptor ARCHIVE = Activator.getImageDescriptor("icons/etool16/archive.gif"); //$NON-NLS-1$

  /** Archive (New) */
  public static final ImageDescriptor ARCHIVE_NEW = Activator.getImageDescriptor("icons/obj16/archive_new.gif"); //$NON-NLS-1$

  /** Archive (Disabled) */
  public static final ImageDescriptor ARCHIVE_DISABLED = Activator.getImageDescriptor("icons/dtool16/archive.gif"); //$NON-NLS-1$

  /** Group Foreground Color */
  public static final RGB GROUP_FG_COLOR = new RGB(0, 0, 128);

  /** Group Background Color (non Custom Owner Drawn) */
  public static final RGB GROUP_BG_COLOR = new RGB(235, 235, 235);

  /** Group Gradient Foreground Color */
  public static final RGB GROUP_GRADIENT_FG_COLOR = new RGB(250, 250, 250);

  /** Group Gradient Background Color */
  public static final RGB GROUP_GRADIENT_BG_COLOR = new RGB(220, 220, 220);

  /** Group Gradient End Color */
  public static final RGB GROUP_GRADIENT_END_COLOR = new RGB(200, 200, 200);

  /** Minimum width of Dialogs in Dialog Units */
  public static final int MIN_DIALOG_WIDTH_DLU = 320;

  /** News-Text Font Id */
  public static final String NEWS_TEXT_FONT_ID = "org.rssowl.ui.NewsTextFont"; //$NON-NLS-1$

  /** Headlines Font Id */
  public static final String HEADLINES_FONT_ID = "org.rssowl.ui.HeadlinesFont"; //$NON-NLS-1$

  /** BookMark Explorer Font Id */
  public static final String BKMRK_EXPLORER_FONT_ID = "org.rssowl.ui.BookmarkExplorerFont"; //$NON-NLS-1$

  /** Notification Popup Font Id */
  public static final String NOTIFICATION_POPUP_FONT_ID = "org.rssowl.ui.NotificationPopupFont"; //$NON-NLS-1$

  /** Dialog Font Id */
  public static final String DIALOG_FONT_ID = "org.eclipse.jface.dialogfont"; //$NON-NLS-1$

  /** Sticky Background Color */
  public static final String STICKY_BG_COLOR_ID = "org.rssowl.ui.StickyBGColor"; //$NON-NLS-1$

  /** Search Highlight Background Color */
  public static final String SEARCH_HIGHLIGHT_BG_COLOR_ID = "org.rssowl.ui.SearchHighlightBGColor"; //$NON-NLS-1$

  /** News Background Color */
  public static final String NEWS_LIST_BG_COLOR_ID = "org.rssowl.ui.NewsListBackgroundColor"; //$NON-NLS-1$

  /** Link Color */
  public static final String LINK_FG_COLOR_ID = "org.rssowl.ui.LinkFGColor"; //$NON-NLS-1$

  /* ID of the High Contrast Theme */
  private static final String HIGH_CONTRAST_THEME = "org.eclipse.ui.ide.systemDefault"; //$NON-NLS-1$

  /* Used to cache Image-Descriptors for Favicons */
  private static final Map<Long, ImageDescriptor> FAVICO_CACHE = new HashMap<Long, ImageDescriptor>();

  /* Used to cache Image-Descriptors obtained from a file-path */
  private static final Map<String, ImageDescriptor> DESCRIPTOR_CACHE = new HashMap<String, ImageDescriptor>();

  /* Used to cache the path of Images used in the embedded Browser */
  private static final Map<String, String> fgImageUriMap = new ConcurrentHashMap<String, String>();

  /* Name of Folder for storing Icons */
  private static final String ICONS_FOLDER = "icons"; //$NON-NLS-1$

  /* Shared Clipboard instance */
  private static Clipboard fgClipboard;

  /* Cache the OSTheme once retrieved */
  private static OSTheme fgCachedOSTheme;

  /* Workaround for unknown Date Width */
  private static int DATE_WIDTH = -1;

  /* Workaround for unknown State Width */
  private static int STATE_WIDTH = -1;

  /* Default News Text Font Height */
  private static final int DEFAULT_NEWS_TEXT_FONT_HEIGHT = 10;

  /* System Properties for Date Format */
  private static final String SHORT_DATE_FORMAT_PROPERTY = "shortDateFormat"; //$NON-NLS-1$
  private static final String LONG_DATE_FORMAT_PROPERTY = "longDateFormat"; //$NON-NLS-1$
  private static final String SHORT_TIME_FORMAT_PROPERTY = "shortTimeFormat"; //$NON-NLS-1$

  /* Packed Wizard Width per OS (in DLUs) */
  private static final int WINDOWS_PACKED_WIZARD_WIDTH = 380;
  private static final int LINUX_PACKED_WIZARD_WIDTH = 370;
  private static final int MAC_PACKED_WIZARD_WIDTH = 300;

  /* Map Common Label Colors to RGB Values */
  private static final Map<String, RGB> fgMapCommonColorToRGB = new HashMap<String, RGB>();

  /* Map Common Mime Types to Extensions (used for Attachments) */
  private static final Map<String, String> fgMapMimeToExtension = new HashMap<String, String>();
  static {

    /* Audio */
    fgMapMimeToExtension.put("audio/mpeg", "mp3"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("audio/mpeg3", "mp3"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("audio/x-mpeg3", "mp3"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("audio/mpeg4", "mp4"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("audio/x-mpeg4", "mp4"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("audio/aac", "aac"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("audio/aacp", "aac"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Image */
    fgMapMimeToExtension.put("image/bmp", "bmp"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("image/x-windows-bmp", "bmp"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("image/gif", "gif"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("image/jpeg", "jpg"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("image/pjpeg", "jpg"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("image/png", "png"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("image/x-quicktime", "qti"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Video */
    fgMapMimeToExtension.put("video/x-ms-asf", "asd"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("application/x-troff-msvideo", "avi"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("video/avi", "avi"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("video/msvideo", "avi"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("video/x-msvideo", "avi"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("video/x-flv", "flv"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("video/quicktime", "mov"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Application */
    fgMapMimeToExtension.put("application/msword", "doc"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("application/pdf", "pdf"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("application/rtf", "rtf"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("text/richtext", "rtf"); //$NON-NLS-1$ //$NON-NLS-2$
    fgMapMimeToExtension.put("application/x-rtf", "rtf"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Common Colors to RGB */
    fgMapCommonColorToRGB.put("0,0,0", new RGB(0, 0, 0)); // "Black",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("124,10,2", new RGB(124, 10, 2)); // "Barn Red",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("163,21,2", new RGB(163, 21, 2)); // "Salem Red",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("214,148,99", new RGB(214, 148, 99)); // "Salmon",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("200,118,10", new RGB(200, 118, 10)); // "Pumpkin",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("240,177,12", new RGB(240, 177, 12)); // "Marigold Yellow",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("209,161,17", new RGB(209, 161, 17)); // "Mustard",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("136,128,54", new RGB(136, 128, 54)); // "Bayberry Green",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("129,150,93", new RGB(129, 150, 93)); // "Tavern Green",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("82,92,58", new RGB(82, 92, 58)); // "Lexington Green",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("126,135,130", new RGB(126, 135, 130)); // "Sea Green",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("111,121,174", new RGB(111, 121, 174)); // "Federal Blue",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("92,101,126", new RGB(92, 101, 126)); // "Soldier Blue",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("144,152,163", new RGB(144, 152, 163)); // "Slate",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("25,16,17", new RGB(25, 16, 17)); // "Pitch Black",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("82,66,41", new RGB(82, 66, 41)); // "Driftwood",//$NON-NLS-1$
    fgMapCommonColorToRGB.put("82,16,0", new RGB(82, 16, 0)); // "Chocolate Brown" //$NON-NLS-1$
    fgMapCommonColorToRGB.put("255,0,0", new RGB(255, 0, 0)); // "Red" //$NON-NLS-1$
    fgMapCommonColorToRGB.put("0,255,0", new RGB(0, 255, 0)); // "Greeen" //$NON-NLS-1$
    fgMapCommonColorToRGB.put("0,0,255", new RGB(0, 0, 255)); // "Blue" //$NON-NLS-1$
  }

  /** An enumeration of Operating System Themes */
  public enum OSTheme {

    /** Windows XP Blue */
    WINDOWS_BLUE,

    /** Windows XP Silver */
    WINDOWS_SILVER,

    /** Windows XP Olive */
    WINDOWS_OLIVE,

    /** Windows Classic */
    WINDOWS_CLASSIC,

    /** High Contrast */
    HIGH_CONTRAST,

    /** Any other Theme */
    OTHER
  }

  /** An enumeration of Open Modes when opening something in the Feed View */
  public enum FeedViewOpenMode {

    /** Force to Activate the Feed */
    FORCE_ACTIVATE,

    /** Ignore Feed if already opened */
    IGNORE_ALREADY_OPENED,

    /** Ignore Tab reuse for Feeds */
    IGNORE_REUSE;
  }

  /** Supported Feedview Layouts */
  public enum Layout {
    CLASSIC(Messages.OwlUI_CLASSIC_LAYOUT),
    VERTICAL(Messages.OwlUI_VERTICAL_LAYOUT),
    LIST(Messages.OwlUI_LIST_LAYOUT),
    NEWSPAPER(Messages.OwlUI_NEWSPAPER_LAYOUT),
    HEADLINES(Messages.OwlUI_HEADLINES_LAYOUT);

    private final String fName;

    private Layout(String name) {
      fName = name;
    }

    /**
     * @return the name of this layout option.
     */
    public String getName() {
      return fName;
    }
  }

  /** Supported Page Sizes for Newspaper/Headlines Layout */
  public enum PageSize {
    TEN(Messages.OwlUI_T_ARTICLES, 10),
    TWENTY_FIVE(Messages.OwlUI_TF_ARTICLES, 25),
    FIFTY(Messages.OwlUI_F_ARTICLES, 50),
    HUNDRED(Messages.OwlUI_H_ARTICLES, 100),
    NO_PAGING(Messages.OwlUI_ALL_ARTICLES, 0);

    private final String fName;
    private final int fPageSize;

    private PageSize(String name, int pageSize) {
      fName = name;
      fPageSize = pageSize;
    }

    /**
     * @return the name of the page size option.
     */
    public String getName() {
      return fName;
    }

    /**
     * @return the page size.
     */
    public int getPageSize() {
      return fPageSize;
    }

    /**
     * @param pageSize the configured page size.
     * @return the matching {@link PageSize} value from the enum or
     * <code>NO_PAGING</code> if none.
     */
    public static PageSize from(int pageSize) {
      switch (pageSize) {
        case 10:
          return TEN;
        case 25:
          return TWENTY_FIVE;
        case 50:
          return FIFTY;
        case 100:
          return HUNDRED;
      }

      return NO_PAGING;
    }
  }

  /* Helper to ensure favicons cause no errors if corrupt */
  private static class FavIconImageDescriptor extends ImageDescriptor {
    private final ImageDescriptor fDescriptor;
    private final File fFaviconFile;

    private FavIconImageDescriptor(File faviconFile, ImageDescriptor descriptor) {
      Assert.isNotNull(faviconFile);
      Assert.isNotNull(descriptor);
      fFaviconFile = faviconFile;
      fDescriptor = descriptor;
    }

    /*
     * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
     */
    @Override
    public ImageData getImageData() {
      return fDescriptor.getImageData();
    }

    /*
     * @see org.eclipse.jface.resource.ImageDescriptor#createImage(boolean, org.eclipse.swt.graphics.Device)
     */
    @Override
    public Image createImage(boolean returnMissingImageOnError, Device device) {
      try {
        return internalCreateImage(returnMissingImageOnError, device);
      } catch (SWTException e) {
        //Fallback to default Image
      } catch (SWTError error) {
        //Fallback to default Image
      }

      return BOOKMARK.createImage(returnMissingImageOnError, device);
    }

    private Image internalCreateImage(boolean returnMissingImageOnError, Device device) {
      try {
        if (Application.IS_LINUX) //Use native loading on Linux to support alpha in ICO
          return new Image(device, fFaviconFile.toString());

        ImageLoader loader = new ImageLoader();
        ImageData[] datas = loader.load(fFaviconFile.toString());
        if (datas != null && datas.length > 0)
          return new Image(device, datas[0]);
      } catch (SWTException e) {
        //Fallback to alternative method to load Image
      } catch (SWTError error) {
        //Fallback to alternative method to load Image
      }

      return fDescriptor.createImage(returnMissingImageOnError, device);
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      return fDescriptor.equals(obj);
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return fDescriptor.hashCode();
    }

    /*
     * @see org.eclipse.jface.resource.ImageDescriptor#destroyResource(java.lang.Object)
     */
    @Override
    public void destroyResource(Object previouslyCreatedObject) {
      fDescriptor.destroyResource(previouslyCreatedObject);
    }
  }

  /**
   * Returns the <code>OSTheme</code> that is currently being used.
   *
   * @param display An instance of the SWT <code>Display</code> used for
   * determining the used theme.
   * @return Returns the <code>OSTheme</code> that is currently being used.
   */
  public static OSTheme getOSTheme(Display display) {

    /* Check Cached version first */
    if (fgCachedOSTheme != null)
      return fgCachedOSTheme;

    ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
    if (HIGH_CONTRAST_THEME.equals(currentTheme.getId())) {
      fgCachedOSTheme = OSTheme.HIGH_CONTRAST;
      return fgCachedOSTheme;
    }

    RGB widgetBackground = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB();
    RGB listSelection = display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();

    /* Theme: Windows Blue */
    if (widgetBackground.equals(new RGB(236, 233, 216)) && listSelection.equals(new RGB(49, 106, 197)))
      fgCachedOSTheme = OSTheme.WINDOWS_BLUE;

    /* Theme: Windows Classic */
    else if (widgetBackground.equals(new RGB(212, 208, 200)) && listSelection.equals(new RGB(10, 36, 106)))
      fgCachedOSTheme = OSTheme.WINDOWS_CLASSIC;

    /* Theme: Windows Silver */
    else if (widgetBackground.equals(new RGB(224, 223, 227)) && listSelection.equals(new RGB(178, 180, 191)))
      fgCachedOSTheme = OSTheme.WINDOWS_SILVER;

    /* Theme: Windows Olive */
    else if (widgetBackground.equals(new RGB(236, 233, 216)) && listSelection.equals(new RGB(147, 160, 112)))
      fgCachedOSTheme = OSTheme.WINDOWS_OLIVE;

    /* Any other Theme */
    else
      fgCachedOSTheme = OSTheme.OTHER;

    return fgCachedOSTheme;
  }

  /**
   * @return <code>true</code> if the display settings is set to high contrast
   * mode and <code>false</code> otherwise.
   */
  public static boolean isHighContrast() {
    return getOSTheme(Display.getDefault()) == OSTheme.HIGH_CONTRAST;
  }

  /**
   * Get the shared instance of <code>Clipboard</code>.
   *
   * @return the shared instance of <code>Clipboard</code>.
   */
  public static Clipboard getClipboard() {
    return getClipboard(PlatformUI.getWorkbench().getDisplay());
  }

  /**
   * Get the shared instance of <code>Clipboard</code>.
   *
   * @param display the {@link Display} the clipboard is operating on.
   * @return the shared instance of <code>Clipboard</code>.
   */
  public static Clipboard getClipboard(Display display) {
    if (fgClipboard == null)
      fgClipboard = new Clipboard(display);

    return fgClipboard;
  }

  /**
   * @param path
   * @return ImageDescriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return getImageDescriptor(Activator.PLUGIN_ID, path);
  }

  /**
   * @param pluginId
   * @param path
   * @return ImageDescriptor
   */
  public static ImageDescriptor getImageDescriptor(String pluginId, String path) {
    ImageDescriptor desc = DESCRIPTOR_CACHE.get(pluginId + path);
    if (desc == null) {
      desc = Activator.getImageDescriptor(pluginId, path);
      DESCRIPTOR_CACHE.put(pluginId + path, desc);
    }

    return desc;
  }

  /**
   * @param manager
   * @param descriptor
   * @return Image
   */
  public static Image getImage(ResourceManager manager, ImageDescriptor descriptor) {
    try {
      return manager.createImage(descriptor);
    } catch (DeviceResourceException e) {
      return getDefaultImage(manager);
    } catch (SWTException e) {
      return getDefaultImage(manager);
    }
  }

  /* Returns the default Image or NULL if unable to create */
  private static Image getDefaultImage(ResourceManager manager) {
    try {
      return manager.createImage(UNKNOWN);
    } catch (DeviceResourceException e1) {
      return null; // Should not happen
    }
  }

  /**
   * @param manager
   * @param path
   * @return Image
   */
  public static Image getImage(ResourceManager manager, String path) {
    return getImage(manager, getImageDescriptor(path));
  }

  /**
   * @param owner
   * @param path
   * @return Image
   */
  public static Image getImage(Control owner, String path) {
    LocalResourceManager manager = new LocalResourceManager(JFaceResources.getResources(), owner);
    return getImage(manager, path);
  }

  /**
   * @param owner
   * @param descriptor
   * @return Image
   */
  public static Image getImage(Control owner, ImageDescriptor descriptor) {
    LocalResourceManager manager = new LocalResourceManager(JFaceResources.getResources(), owner);
    return getImage(manager, descriptor);
  }

  /**
   * @param path the path to the image as absolute path from the plugin root.
   * @param name the name of the image.
   * @return an {@link URI} to a file where the image has been saved to.
   */
  public static String getImageUri(String path, String name) {

    /* Check Cache */
    String imgUri = fgImageUriMap.get(path);
    if (imgUri != null)
      return imgUri;

    /* Check Filesystem */
    File imgFile = getImageFile(name);
    if (imgFile.exists()) {
      imgUri = getImageUri(imgFile);
      fgImageUriMap.put(path, imgUri);
      return imgUri;
    }

    /* Copy to Filesystem */
    try {
      CoreUtils.copy(OwlUI.class.getResourceAsStream(path), new FileOutputStream(imgFile));
      imgUri = getImageUri(imgFile);
      fgImageUriMap.put(path, imgUri);
      return imgUri;
    } catch (IOException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

    return null;
  }

  private static String getImageUri(File file) {
    URI uri = file.toURI();
    String s = uri.toString();
    return s.replaceFirst("/", "///"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @param manager
   * @param rgb
   * @return Color
   */
  public static Color getColor(ResourceManager manager, RGB rgb) {
    try {
      return manager.createColor(rgb);
    } catch (DeviceResourceException e) {
      return manager.getDevice().getSystemColor(SWT.COLOR_BLACK);
    }
  }

  /**
   * @param manager
   * @param descriptor
   * @return Color
   */
  public static Color getColor(ResourceManager manager, ColorDescriptor descriptor) {
    try {
      return manager.createColor(descriptor);
    } catch (DeviceResourceException e) {
      return manager.getDevice().getSystemColor(SWT.COLOR_BLACK);
    }
  }

  /**
   * @param resources
   * @param label
   * @return Color
   */
  public static Color getColor(ResourceManager resources, ILabel label) {
    RGB rgb = getRGB(label);

    return getColor(resources, rgb);
  }

  /**
   * @param label
   * @return RGB
   */
  public static RGB getRGB(ILabel label) {
    return getRGB(label.getColor());
  }

  /**
   * @param rgb
   * @return RGB
   */
  public static RGB getRGB(String rgb) {
    if (!StringUtils.isSet(rgb))
      return null;

    RGB commonRGB = fgMapCommonColorToRGB.get(rgb);
    if (commonRGB != null)
      return commonRGB;

    String color[] = rgb.split(","); //$NON-NLS-1$
    return new RGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
  }

  /**
   * @param rgb
   * @return String
   */
  public static String toString(RGB rgb) {
    return rgb.red + "," + rgb.green + "," + rgb.blue; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @param key
   * @return Font
   */
  public static Font getFont(String key) {
    return JFaceResources.getFontRegistry().get(key);
  }

  /**
   * @param key
   * @return Font
   */
  public static Font getBold(String key) {
    return JFaceResources.getFontRegistry().getBold(key);
  }

  /**
   * @param key
   * @return Font
   */
  public static Font getItalic(String key) {
    return JFaceResources.getFontRegistry().getItalic(key);
  }

  /**
   * @param key
   * @param style
   * @return Font
   */
  public static Font getThemeFont(String key, int style) {
    FontRegistry fontRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
    if (fontRegistry != null) {
      if (style == SWT.NORMAL)
        return fontRegistry.get(key);
      else if ((style & SWT.BOLD) != 0)
        return fontRegistry.getBold(key);
      else if ((style & SWT.ITALIC) != 0)
        return fontRegistry.getItalic(key);
    }

    return getFont(key);
  }

  /**
   * @param key
   * @param manager
   * @param defaultColor
   * @return Font
   */
  public static Color getThemeColor(String key, ResourceManager manager, RGB defaultColor) {
    ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    if (colorRegistry != null)
      return getColor(manager, colorRegistry.getColorDescriptor(key));

    return getColor(manager, defaultColor);
  }

  /**
   * @param key
   * @param defaultRGB
   * @return Font
   */
  public static RGB getThemeRGB(String key, RGB defaultRGB) {
    ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    if (colorRegistry != null)
      return colorRegistry.getRGB(key);

    return defaultRGB;
  }

  /**
   * @param drawable
   * @param text
   * @param font
   * @return The size of the Text as Point.
   */
  public static Point getTextSize(Drawable drawable, Font font, String text) {
    GC gc = new GC(drawable);
    gc.setFont(font);
    Point p = gc.textExtent(text);
    gc.dispose();

    return p;
  }

  /**
   * @param bookmark
   * @return ImageDescriptor
   */
  public static ImageDescriptor getFavicon(IBookMark bookmark) {
    if (bookmark.getId() == null)
      return null;

    /* 1.) Check if ImageDescriptor exists in Memory */
    ImageDescriptor descriptor = FAVICO_CACHE.get(bookmark.getId());
    if (descriptor != null)
      return descriptor;

    /* 2.) Check if ImageDescriptor exists in File System */
    File favicon = getImageFile(bookmark.getId());
    if (favicon != null && favicon.exists()) {
      try {
        descriptor = new FavIconImageDescriptor(favicon, ImageDescriptor.createFromURL(favicon.toURI().toURL()));
        FAVICO_CACHE.put(bookmark.getId(), descriptor);
        return descriptor;
      } catch (MalformedURLException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    return null;
  }

  /**
   * @param id
   */
  public static void deleteImage(long id) {

    /* Delete from Cache */
    FAVICO_CACHE.remove(id);

    /* Delete from Disk */
    File file = getImageFile(id);
    if (file != null && file.exists())
      file.delete();
  }

  /**
   * Deletes all stored icons from the org.rssowl.ui icons folder.
   */
  public static void clearFavicons() {
    Activator activator = Activator.getDefault();
    if (activator == null)
      return;

    IPath path = new Path(activator.getStateLocation().toOSString());
    path = path.append(ICONS_FOLDER);
    File iconsFolder = new File(path.toOSString());
    if (!iconsFolder.exists())
      return;

    File[] files = iconsFolder.listFiles();
    for (File file : files) {
      if (file.getName().endsWith(".ico")) //$NON-NLS-1$
        file.delete();
    }
  }

  /**
   * @param id
   * @param bytes
   * @param defaultImage
   * @param wHint
   * @param hHint
   */
  public static void storeImage(long id, byte[] bytes, ImageDescriptor defaultImage, int wHint, int hHint) {
    Assert.isNotNull(defaultImage);
    Assert.isLegal(wHint > 0);
    Assert.isLegal(hHint > 0);

    ImageData imgData = null;

    /* Bytes Provided */
    if (bytes != null && bytes.length > 0) {
      ByteArrayInputStream inS = null;
      try {
        inS = new ByteArrayInputStream(bytes);
        ImageLoader loader = new ImageLoader();
        ImageData[] imageDatas = loader.load(inS);

        /* Look for the Icon with the best quality */
        if (imageDatas != null)
          imgData = getBestQuality(imageDatas, wHint, hHint);
      } catch (SWTException e) {
        /* Ignore any Image-Format exceptions */
      } finally {
        if (inS != null) {
          try {
            inS.close();
          } catch (IOException e) {
            if (!(e instanceof MonitorCanceledException))
              Activator.getDefault().logError(e.getMessage(), e);
          }
        }
      }
    }

    /* Use default Image if img-data is null */
    if (imgData == null)
      imgData = defaultImage.getImageData();

    /* Save Image into Cache-Area on File-System */
    if (imgData != null) {
      File imageFile = getImageFile(id);
      if (imageFile == null)
        return;

      /* Scale if required */
      if (imgData.width != 16 || imgData.height != 16)
        imgData = imgData.scaledTo(16, 16);

      /* Try using native Image Format */
      try {
        if (storeImage(imgData, imageFile, imgData.type))
          return;
      } catch (SWTException e) {
        /* Ignore any Image-Format exceptions */
      }

      /* Try using various other Image-Formats */
      int formats[] = new int[] { SWT.IMAGE_PNG, SWT.IMAGE_ICO, SWT.IMAGE_GIF, SWT.IMAGE_BMP };
      for (int format : formats) {
        if (format != imgData.type) {
          try {
            if (storeImage(imgData, imageFile, format))
              return;
          } catch (SWTException e) {
            /* Ignore any Image-Format exceptions */
          }
        }
      }
    }
  }

  /* Returns the ImageData with best Depth or Size */
  private static ImageData getBestQuality(ImageData datas[], int wHint, int hHint) {
    ImageData bestSize = null;
    ImageData bestDepth = null;
    int maxDepth = -1;
    int maxSize = -1;

    /* Foreach Image: Check best Depth */
    for (ImageData data : datas) {
      if (data.depth > maxDepth) {
        maxDepth = data.depth;
        bestDepth = data;
      }
    }

    /* Foreach Image: Check best Size */
    for (ImageData data : datas) {

      /* Only consider best depth */
      if (data.depth == maxDepth) {

        /* Return if Size matches Hint */
        if (data.width == wHint && data.height == hHint)
          return data;

        /* Otherwise look for bigges */
        if (data.width * data.height > maxSize) {
          maxSize = data.width * data.height;
          bestSize = data;
        }
      }
    }

    return (bestDepth != null) ? bestDepth : bestSize;
  }

  /* Saves the Image to the given File with the given Image-Format */
  private static boolean storeImage(ImageData imgData, File file, int format) {
    ImageLoader loader = new ImageLoader();
    loader.data = new ImageData[] { imgData };
    FileOutputStream fOs = null;
    try {
      fOs = new FileOutputStream(file);
      loader.save(fOs, format);
    } catch (FileNotFoundException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    } finally {
      if (fOs != null)
        try {
          fOs.close();
        } catch (IOException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
    }

    return true;
  }

  private static File getImageFile(String fileName) {
    boolean res = false;

    Activator activator = Activator.getDefault();
    if (activator == null)
      return null;

    IPath path = new Path(activator.getStateLocation().toOSString());
    path = path.append(ICONS_FOLDER);
    File root = new File(path.toOSString());
    if (!root.exists())
      res = root.mkdir();
    else
      res = true;

    path = path.append(fileName);

    if (!res)
      return null;

    return new File(path.toOSString());
  }

  private static File getImageFile(long id) {
    return getImageFile(id + ".ico"); //$NON-NLS-1$
  }

  /**
   * Attempts to find the primary <code>IWorkbenchWindow</code> from the
   * PlatformUI facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @return the primary <code>IWorkbenchWindow</code> from the PlatformUI
   * facade or <code>NULL</code> if none.
   */
  public static IWorkbenchWindow getPrimaryWindow() {

    /* Return the first Window of the Workbench */
    IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
    if (windows.length > 0)
      return windows[0];

    return null;
  }

  /**
   * Attempts to find the first <code>IWorkbenchWindow</code> from the
   * PlatformUI facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @return the first <code>IWorkbenchWindow</code> from the PlatformUI facade
   * or <code>NULL</code> if none.
   */
  public static IWorkbenchWindow getWindow() {

    /* First try active Window */
    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (activeWorkbenchWindow != null)
      return activeWorkbenchWindow;

    /* Finally try any Window */
    IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
    if (windows.length > 0)
      return windows[0];

    return null;
  }

  /**
   * Attempts to find the <code>IWorkbenchWindow</code> from the PlatformUI
   * facade that is located at where the mouse is pointing at. Otherwise,
   * returns <code>NULL</code> if none.
   *
   * @return the first <code>IWorkbenchWindow</code> from the PlatformUI facade
   * that is located at where the mouse is pointing at or <code>NULL</code> if
   * none.
   */
  public static IWorkbenchWindow getWindowAtCursor() {

    /* Get the Control at the Cursor position */
    Control cursorControl = Display.getDefault().getCursorControl();
    if (cursorControl == null)
      return null;

    /* Return Window that belongs to Cursor-Shell */
    Shell cursorShell = cursorControl.getShell();
    IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (IWorkbenchWindow workbenchWindow : windows) {
      if (workbenchWindow.getShell().equals(cursorShell))
        return workbenchWindow;
    }

    return null;
  }

  /**
   * Attempts to find the first <code>IWorkbenchPage</code> from the PlatformUI
   * facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @return the first <code>IWorkbenchPage</code> from the PlatformUI facade or
   * <code>NULL</code> if none.
   */
  public static IWorkbenchPage getPage() {
    IWorkbenchWindow window = getWindow();
    return getPage(window);
  }

  /**
   * Attempts to find the first <code>IWorkbenchPage</code> from the PlatformUI
   * facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @param window the {@link IWorkbenchWindow} to search for a
   * {@link IWorkbenchPage}.
   * @return the first <code>IWorkbenchPage</code> from the PlatformUI facade or
   * <code>NULL</code> if none.
   */
  public static IWorkbenchPage getPage(IWorkbenchWindow window) {
    if (window != null) {

      /* First try active Page */
      if (window.getActivePage() != null)
        return window.getActivePage();

      /* Finally try any Page */
      IWorkbenchPage[] pages = window.getPages();
      if (pages.length > 0)
        return pages[0];
    }

    return null;
  }

  /**
   * Attempts to find the active <code>IWorkbenchPart</code> from the PlatformUI
   * facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @param window the {@link IWorkbenchWindow} to search in.
   * @return the active <code>IWorkbenchPart</code> from the PlatformUI facade
   * or <code>NULL</code> if none.
   */
  public static IWorkbenchPart getActivePart(IWorkbenchWindow window) {
    if (window != null) {

      /* First try active Page */
      if (window.getActivePage() != null)
        return window.getActivePage().getActivePart();

      /* Finally try any Page */
      IWorkbenchPage[] pages = window.getPages();
      for (IWorkbenchPage page : pages) {
        if (page.getActivePart() != null)
          return page.getActivePart();
      }
    }

    return null;
  }

  /**
   * Attempts to return the index of the given workbench window or
   * <code>-1</code> if none.
   *
   * @param window the {@link IWorkbenchWindow} to get the index in the stack of
   * windows that are open.
   * @return the index of the given workbench window or <code>-1</code> if none.
   */
  public static int getWindowIndex(IWorkbenchWindow window) {
    if (window != null) {
      IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
      for (int i = 0; i < windows.length; i++)
        if (windows[i].equals(window))
          return i;
    }

    return 0;
  }

  /**
   * Attempts to find the <code>IWorkbenchPage</code> from the Workbench-Window
   * the mouse is currently over from the PlatformUI facade. Otherwise, returns
   * <code>NULL</code> if none.
   *
   * @return the first <code>IWorkbenchPage</code> from the Workbench-Window the
   * mouse is currently over from the PlatformUI facade or <code>NULL</code> if
   * none.
   */
  public static IWorkbenchPage getPageAtCursor() {
    IWorkbenchWindow window = getWindowAtCursor();
    if (window != null) {

      /* First try active Page */
      if (window.getActivePage() != null)
        return window.getActivePage();

      /* Finally try any Page */
      IWorkbenchPage[] pages = window.getPages();
      if (pages.length > 0)
        return pages[0];
    }

    return null;
  }

  /**
   * Attempts to find the first active <code>IEditorPart</code> from the
   * PlatformUI facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @return the first active <code>IEditorPart</code> from the PlatformUI
   * facade or <code>NULL</code> if none.
   */
  public static IEditorPart getActiveEditor() {
    IWorkbenchPage page = getPage();
    if (page != null)
      return page.getActiveEditor();

    return null;
  }

  /**
   * @return a list of all editors currently open in the UI as references.
   */
  public static List<IEditorReference> getEditorReferences() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IEditorReference[] references = page.getEditorReferences();
      return Arrays.asList(references);
    }

    return Collections.emptyList();
  }

  /**
   * @return the number of opened feed views (will not trigger editor
   * restoring).
   */
  public static int getOpenFeedViewCount() {
    int count = 0;
    List<IEditorReference> editors = getEditorReferences();
    for (IEditorReference reference : editors) {
      if (FeedView.ID.equals(reference.getId()))
        count++;
    }

    return count;
  }

  /**
   * Attempts to find the first active <code>FeedView</code> from the PlatformUI
   * facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @return the first active <code>FeedView</code> from the PlatformUI facade
   * or <code>NULL</code> if none.
   */
  public static FeedView getActiveFeedView() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IEditorPart activeEditor = page.getActiveEditor();
      if (activeEditor != null && activeEditor instanceof FeedView)
        return (FeedView) activeEditor;
    }

    return null;
  }

  /**
   * Attempts to find the first active <code>FeedView</code> from the PlatformUI
   * facade and then will return the feed view input preferences. Otherwise,
   * returns <code>NULL</code> if none.
   *
   * @return the first active <code>FeedView</code> input preferences from the
   * PlatformUI facade or <code>NULL</code> if none.
   */
  public static IPreferenceScope getActiveFeedViewPreferences() {
    FeedView feedView = getActiveFeedView();
    if (feedView != null) {
      IEditorInput input = feedView.getEditorInput();
      if (input instanceof FeedViewInput) {
        FeedViewInput feedViewInput = (FeedViewInput) input;
        if (feedViewInput.getMark() != null) {
          INewsMark mark = feedViewInput.getMark();
          if (mark instanceof FolderNewsMark)
            return Owl.getPreferenceService().getEntityScope(((FolderNewsMark) mark).getFolder());

          return Owl.getPreferenceService().getEntityScope(feedViewInput.getMark());
        }
      }
    }

    return null;
  }

  /**
   * Attempts to find all open <code>FeedView</code>s from the PlatformUI
   * facade. Otherwise, returns an empty list if none.
   *
   * @return all open <code>FeedView</code>s from the PlatformUI facade or an
   * empty list if none.
   */
  public static List<FeedView> getFeedViews() {
    List<FeedView> feedViews = new ArrayList<FeedView>();

    List<IEditorReference> references = getEditorReferences();
    for (IEditorReference reference : references) {
      if (FeedView.ID.equals(reference.getId())) {
        IEditorPart editor = reference.getEditor(true);
        if (editor instanceof FeedView)
          feedViews.add((FeedView) editor);
      }
    }

    return feedViews;
  }

  /**
   * Attempts to find the selection from the first active <code>FeedView</code>
   * from the PlatformUI facade. Otherwise, returns
   * <code>StructuredSelection.EMPTY</code> if none.
   *
   * @return the selection from the first active <code>FeedView</code> from the
   * PlatformUI facade or <code>StructuredSelection.EMPTY</code> if none.
   */
  public static IStructuredSelection getActiveFeedViewSelection() {
    FeedView feedview = getActiveFeedView();
    if (feedview == null)
      return StructuredSelection.EMPTY;

    ISelectionProvider selectionProvider = feedview.getSite().getSelectionProvider();
    if (selectionProvider == null)
      return StructuredSelection.EMPTY;

    return (IStructuredSelection) selectionProvider.getSelection();
  }

  /**
   * Attempts to find the selection from the first active <code>Part</code> from
   * the PlatformUI facade. Otherwise, returns
   * <code>StructuredSelection.EMPTY</code> if none.
   *
   * @return the selection from the first active <code>Part</code> from the
   * PlatformUI facade or <code>StructuredSelection.EMPTY</code> if none.
   */
  public static IStructuredSelection getActiveSelection() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IWorkbenchPart part = page.getActivePart();
      if (part != null && part.getSite() != null) {
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider != null) {
          ISelection selection = selectionProvider.getSelection();
          if (!selection.isEmpty() && selection instanceof IStructuredSelection)
            return (IStructuredSelection) selection;
        }
      }
    }

    return StructuredSelection.EMPTY;
  }

  /**
   * Attempts to find the first <code>FeedView</code> from the active Workbench
   * Window of the PlatformUI facade. Otherwise, returns <code>NULL</code> if
   * none.
   *
   * @return the first <code>FeedView</code> from the active Workbench Window of
   * the PlatformUI facade or <code>NULL</code> if none.
   */
  public static FeedView getFirstActiveFeedView() {
    IWorkbenchPage page = getPage();
    if (page != null) {

      /* First try current active editor */
      IEditorPart activeEditor = page.getActiveEditor();
      if (activeEditor instanceof FeedView)
        return (FeedView) activeEditor;

      /* Then navigate through all from first to last */
      IEditorReference[] editorReferences = page.getEditorReferences();
      for (IEditorReference editorReference : editorReferences) {
        if (FeedView.ID.equals(editorReference.getId()))
          return (FeedView) editorReference.getEditor(true);
      }
    }

    return null;
  }

  /**
   * Attempts to find the first <code>WebBrowserView</code> from the active
   * Workbench Window of the PlatformUI facade. Otherwise, returns
   * <code>NULL</code> if none.
   *
   * @return the first <code>WebBrowserView</code> from the active Workbench
   * Window of the PlatformUI facade or <code>NULL</code> if none.
   */
  public static WebBrowserView getFirstActiveBrowser() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IEditorReference[] editorReferences = page.getEditorReferences();
      for (IEditorReference editorReference : editorReferences) {
        try {
          if (editorReference.getEditorInput() instanceof WebBrowserInput)
            return (WebBrowserView) editorReference.getEditor(true);
        } catch (PartInitException e) {
          /* Ignore Silently */
        }
      }
    }

    return null;
  }

  /**
   * Attempts to find the opened <code>BookMarkExplorer</code> from the
   * PlatformUI facade. Otherwise, returns <code>NULL</code> if none.
   *
   * @return the <code>BookMarkExplorer</code> from the PlatformUI facade or
   * <code>NULL</code> if not opened.
   */
  public static BookMarkExplorer getOpenedBookMarkExplorer() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IViewReference[] viewReferences = page.getViewReferences();
      for (IViewReference viewRef : viewReferences) {
        if (viewRef.getId().equals(BookMarkExplorer.VIEW_ID)) {
          IViewPart view = viewRef.getView(true);
          if (view instanceof BookMarkExplorer)
            return (BookMarkExplorer) view;
        }
      }
    }

    return null;
  }

  /**
   * Attempts to find the primary <code>Shell</code> from the PlatformUI facade.
   * Otherwise, returns <code>NULL</code> if none.
   *
   * @return the primary <code>Shell</code> from the PlatformUI facade or
   * <code>NULL</code> if none.
   */
  public static Shell getPrimaryShell() {
    IWorkbenchWindow window = getPrimaryWindow();
    if (window != null)
      return window.getShell();

    return null;
  }

  /**
   * Attempts to find the active <code>Shell</code> from the PlatformUI facade.
   * Otherwise, returns <code>NULL</code> if none.
   *
   * @return the active <code>Shell</code> from the PlatformUI facade or
   * <code>NULL</code> if none.
   */
  public static Shell getActiveShell() {
    IWorkbenchWindow window = getWindow();
    if (window != null)
      return window.getShell();

    return null;
  }

  /**
   * Update the current active window title based on the given array of
   * {@link IMark}.
   *
   * @param input the input that is currently visible in RSSOwl.
   */
  public static void updateWindowTitle(IMark input) {
    if (input != null)
      updateWindowTitle(input.getName());
  }

  /**
   * Update the current active window title based on the given title.
   *
   * @param title the name of the input that is currently visible in RSSOwl.
   */
  public static void updateWindowTitle(String title) {
    IWorkbenchWindow window = getWindow();
    if (window != null) {
      String appTitle = "RSSOwl"; //$NON-NLS-1$
      if (StringUtils.isSet(title))
        title = NLS.bind(Messages.OwlUI_TITLE, title, appTitle);
      else
        title = appTitle;

      String shellText = window.getShell().getText();
      if (shellText == null || !shellText.equals(title))
        window.getShell().setText(title);
    }
  }

  /**
   * A helper method that can be used to restore the application when its
   * minimized.
   *
   * @param page the workbench page the application is running in.
   */
  public static void restoreWindow(IWorkbenchPage page) {
    Shell applicationShell = page.getWorkbenchWindow().getShell();
    restoreWindow(applicationShell);
  }

  /**
   * A helper method that can be used to restore the application when its
   * minimized.
   *
   * @param applicationShell the main {@link Shell} of the application.
   */
  public static void restoreWindow(Shell applicationShell) {
    ApplicationWorkbenchWindowAdvisor advisor = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;

    /* Restore From Tray */
    if (advisor != null && advisor.isMinimizedToTray())
      advisor.restoreFromTray(applicationShell);

    /* Restore from being Minimized */
    else if (applicationShell.getMinimized()) {
      applicationShell.setMinimized(false);
      applicationShell.forceActive();
    }

    /* Otherwise force Active */
    else
      applicationShell.forceActive();
  }

  /**
   * @return the current selected {@link IFolder} of the bookmark explorer or
   * the parent of the current selected {@link IMark} or <code>null</code> if
   * none.
   */
  public static IFolder getBookMarkExplorerSelection() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IViewPart viewPart = page.findView(BookMarkExplorer.VIEW_ID);
      if (viewPart != null) {
        IStructuredSelection selection = (IStructuredSelection) viewPart.getSite().getSelectionProvider().getSelection();
        if (!selection.isEmpty()) {
          Object selectedEntity = selection.iterator().next();
          if (selectedEntity instanceof IFolder)
            return (IFolder) selectedEntity;
          else if (selectedEntity instanceof IMark)
            return ((IMark) selectedEntity).getParent();
        }
      }
    }

    return null;
  }

  /**
   * Opens a selection of {@link INewsMark} inside the feed view.
   *
   * @param page
   * @param selection
   */
  public static void openInFeedView(IWorkbenchPage page, IStructuredSelection selection) {
    openInFeedView(page, selection, false);
  }

  /**
   * Opens a selection of {@link INewsMark} inside the feed view.
   *
   * @param page
   * @param selection
   * @param forceActivate
   */
  public static void openInFeedView(IWorkbenchPage page, IStructuredSelection selection, boolean forceActivate) {
    openInFeedView(page, selection, forceActivate, false);
  }

  /**
   * Opens a selection of {@link INewsMark} inside the feed view.
   *
   * @param page
   * @param selection
   * @param forceActivate
   * @param ignoreAlreadyOpened
   */
  public static void openInFeedView(IWorkbenchPage page, IStructuredSelection selection, boolean forceActivate, boolean ignoreAlreadyOpened) {
    openInFeedView(page, selection, forceActivate, ignoreAlreadyOpened, null);
  }

  /**
   * Opens a selection of {@link INewsMark} inside the feed view.
   *
   * @param page
   * @param selection
   * @param perform
   * @param forceActivate
   * @param ignoreAlreadyOpened
   */
  public static void openInFeedView(IWorkbenchPage page, IStructuredSelection selection, boolean forceActivate, boolean ignoreAlreadyOpened, PerformAfterInputSet perform) {
    try {
      internalOpenInFeedView(page, selection, forceActivate, ignoreAlreadyOpened, false, perform);
    } finally {
      FeedView.setBlockFeedChangeEvent(false);
    }
  }

  /**
   * @param page
   * @param selection
   * @param openModes
   */
  public static void openInFeedView(IWorkbenchPage page, IStructuredSelection selection, EnumSet<FeedViewOpenMode> openModes) {
    boolean forceActivate = openModes.contains(FeedViewOpenMode.FORCE_ACTIVATE);
    boolean ignoreAlreadyOpened = openModes.contains(FeedViewOpenMode.IGNORE_ALREADY_OPENED);
    boolean ignoreReuse = openModes.contains(FeedViewOpenMode.IGNORE_REUSE);

    try {
      internalOpenInFeedView(page, selection, forceActivate, ignoreAlreadyOpened, ignoreReuse, null);
    } finally {
      FeedView.setBlockFeedChangeEvent(false);
    }
  }

  private static void internalOpenInFeedView(IWorkbenchPage page, IStructuredSelection selection, boolean forceActivate, boolean ignoreAlreadyOpened, boolean ignoreReuse, PerformAfterInputSet perform) {
    List<?> list = selection.toList();
    boolean activateEditor = forceActivate || OpenStrategy.activateOnOpen();
    int openedEditors = 0;
    int maxOpenEditors = EditorUtils.getOpenEditorLimit();
    boolean reuseFeedView = !ignoreReuse && Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.ALWAYS_REUSE_FEEDVIEW);

    /* Open Editors for the given Selection */
    for (int i = 0; i < list.size() && openedEditors < maxOpenEditors; i++) {
      Object object = list.get(i);

      /* Convert folder to news mark in case folder selected */
      if (object instanceof IFolder)
        object = new FolderNewsMark((IFolder) object);

      /* Only news marks supported at this point */
      if (object instanceof INewsMark) {
        INewsMark mark = ((INewsMark) object);
        FeedViewInput input = new FeedViewInput(mark, perform);

        /* Start Blocking Feed Change Events if we open more than one Feed */
        if (i == 1)
          FeedView.setBlockFeedChangeEvent(true);

        /* Open in existing Feedview if set */
        if (reuseFeedView) {

          /* Feed could be already open in editor (avoid duplicates) */
          IEditorPart existingEditor = page.findEditor(input);
          if (existingEditor != null) {
            if (activateEditor)
              page.activate(existingEditor);
            else
              page.bringToTop(existingEditor);

            if (perform != null && existingEditor instanceof FeedView)
              ((FeedView) existingEditor).perform(perform);

            break;
          }

          /* Otherwise replace the input in the first active feed view */
          FeedView activeFeedView = OwlUI.getFirstActiveFeedView();
          if (activeFeedView != null) {
            activeFeedView.setInput(input);
            if (activateEditor)
              page.activate(activeFeedView);
            else
              page.bringToTop(activeFeedView);
            break;
          }
        }

        /* Otherwise simply open */
        try {
          boolean explicitPerform = false;
          IEditorPart existingEditor = null;
          if (perform != null) {
            existingEditor = page.findEditor(input);
            explicitPerform = (existingEditor != null);
          }

          /* Open Editor (check for already opened if set) */
          if (!ignoreAlreadyOpened || page.findEditor(input) == null)
            page.openEditor(input, FeedView.ID, activateEditor);

          openedEditors++;

          /* Pass in Perform Code */
          if (explicitPerform && existingEditor instanceof FeedView)
            ((FeedView) existingEditor).perform(perform);

          /* Break loop if we reuse feed views (thus can only display a single feed) */
          if (reuseFeedView)
            break;
        } catch (PartInitException e) {
          Activator.getDefault().getLog().log(e.getStatus());
        }
      }
    }
  }

  /**
   * Set's the checked state of all visible items to the suplied one.
   *
   * @param tree
   * @param state
   */
  public static void setAllChecked(Tree tree, boolean state) {
    setAllChecked(state, tree.getItems());
  }

  private static void setAllChecked(boolean state, TreeItem[] items) {
    for (int i = 0; i < items.length; i++) {
      items[i].setChecked(state);
      TreeItem[] children = items[i].getItems();
      setAllChecked(state, children);
    }
  }

  /** Identical with ActionFactory.CLOSE_OTHERS */
  public static void closeOtherEditors() {
    IWorkbenchPage page = getPage();
    if (page != null) {
      IEditorReference[] refArray = page.getEditorReferences();
      if (refArray != null && refArray.length > 1) {
        IEditorReference[] otherEditors = new IEditorReference[refArray.length - 1];
        IEditorReference activeEditor = (IEditorReference) page.getReference(page.getActiveEditor());
        for (int i = 0; i < refArray.length; i++) {
          if (refArray[i] != activeEditor)
            continue;
          System.arraycopy(refArray, 0, otherEditors, 0, i);
          System.arraycopy(refArray, i + 1, otherEditors, i, refArray.length - 1 - i);
          break;
        }
        page.closeEditors(otherEditors, true);
      }
    }
  }

  /**
   * @param text the {@link Text} to hook auto complete into.
   * @param values the values that show up as proposal.
   * @param decorate if <code>true</code>, decorate the control to indicate
   * content assist is available.
   * @param autoActivate
   * @return Pair
   */
  public static Pair<SimpleContentProposalProvider, ContentProposalAdapter> hookAutoComplete(final Text text, Collection<String> values, boolean decorate, boolean autoActivate) {
    return hookAutoComplete(text, new ContentAssistAdapter(text, ' ', false), values, decorate, autoActivate);
  }

  /**
   * @param combo the {@link Combo} to hook auto complete into.
   * @param values the values that show up as proposal.
   * @param decorate if <code>true</code>, decorate the control to indicate
   * content assist is available.
   * @return Pair
   */
  public static Pair<SimpleContentProposalProvider, ContentProposalAdapter> hookAutoComplete(final Combo combo, Collection<String> values, boolean decorate) {
    return hookAutoComplete(combo, new ContentAssistAdapter(combo, ' ', false), values, decorate, true);
  }

  /**
   * @param control the {@link Control} to hook auto complete into.
   * @param contentAdapter a {@link IControlContentAdapter} for the content
   * @param values the values that show up as proposal.
   * @param decorate if <code>true</code>, decorate the control to indicate
   * content assist is available.
   * @param autoActivate
   * @return Pair
   */
  public static Pair<SimpleContentProposalProvider, ContentProposalAdapter> hookAutoComplete(final Control control, IControlContentAdapter contentAdapter, Collection<String> values, boolean decorate, final boolean autoActivate) {

    /* Show UI Hint that Content Assist is available */
    if (decorate) {
      ControlDecoration controlDeco = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
      controlDeco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());
      controlDeco.setDescriptionText(Messages.OwlUI_CONTENT_ASSIST);
      controlDeco.setShowOnlyOnFocus(true);
    }

    /* Auto-Activate on Key-Down */
    KeyStroke activationKey = KeyStroke.getInstance(SWT.ARROW_DOWN);

    /* Create Content Proposal Adapter */
    SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(new String[0]) {
      @Override
      public IContentProposal[] getProposals(String contents, int position) {
        if (Display.getCurrent() != null && !control.isVisible())
          return new IContentProposal[0];

        return super.getProposals(contents, position);
      }
    };
    proposalProvider.setFiltering(true);
    final ContentProposalAdapter adapter = new ContentProposalAdapter(control, contentAdapter, proposalProvider, activationKey, null);
    adapter.setPropagateKeys(true);
    adapter.setAutoActivationDelay(1500);
    adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);

    /* Apply Proposals */
    if (values != null)
      applyAutoCompleteProposals(values, proposalProvider, adapter, autoActivate);

    /*
     * TODO: This is a hack but there doesnt seem to be any API to set the size
     * of the popup to match the actual size of the Text widget being used.
     */
    control.getDisplay().timerExec(100, new Runnable() {
      public void run() {
        if (!control.isDisposed()) {
          adapter.setPopupSize(new Point(control.getSize().x, 120));
        }
      }
    });

    return Pair.create(proposalProvider, adapter);
  }

  /**
   * @param values
   * @param provider
   * @param adapter
   * @param autoActivate
   */
  public static void applyAutoCompleteProposals(Collection<String> values, SimpleContentProposalProvider provider, ContentProposalAdapter adapter, boolean autoActivate) {

    /* Extract Proposals */
    final String[] proposals = new String[values.size()];
    Set<Character> charSet = new HashSet<Character>();
    int i = 0;
    for (String value : values) {
      proposals[i] = value;

      char c = value.charAt(0);
      charSet.add(Character.toLowerCase(c));
      charSet.add(Character.toUpperCase(c));
      i++;
    }

    /* Auto-Activate on first Key typed */
    char[] activationChars = new char[charSet.size()];
    i = 0;
    for (char c : charSet) {
      activationChars[i] = c;
      i++;
    }

    /* Apply proposals and auto-activation chars */
    provider.setProposals(proposals);
    if (autoActivate)
      adapter.setAutoActivationCharacters(activationChars);
  }

  /**
   * @param display
   * @param rgb the color value to use in the image
   * @return an {@link Image} for the color that must be disposed when no longer
   * used.
   */
  public static Image createColorImage(Display display, RGB rgb) {
    Color color = new Color(display, rgb);

    Image image = new Image(display, 12, 12);

    GC gc = new GC(image);

    gc.setBackground(color);
    gc.fillRectangle(0, 0, 12, 12);

    gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
    gc.drawRectangle(0, 0, 11, 11);

    gc.dispose();
    color.dispose();

    return image;
  }

  /**
   * @return the width for displaying a date.
   */
  public static int getDateWidth() {

    /* Check if Cached already */
    if (DATE_WIDTH > 0)
      return DATE_WIDTH;

    /* Calculate and Cache */
    DateFormat dF = getShortDateFormat();
    Calendar cal = Calendar.getInstance();
    cal.set(2006, Calendar.DECEMBER, 12, 12, 12, 12);
    String sampleDate = dF.format(cal.getTime());

    DATE_WIDTH = OwlUI.getTextSize(Display.getDefault(), OwlUI.getBold(HEADLINES_FONT_ID), sampleDate).x;
    DATE_WIDTH += Application.IS_WINDOWS ? 15 : 30; // Bounds of Column requires more space

    return DATE_WIDTH;
  }

  /**
   * @return the width for displaying a state.
   */
  public static int getStateWidth() {

    /* Check if Cached already */
    if (STATE_WIDTH > 0)
      return STATE_WIDTH;

    /* Calculate and Cache */
    String sampleState = Messages.OwlUI_UPDATED;

    STATE_WIDTH = OwlUI.getTextSize(Display.getDefault(), OwlUI.getBold(HEADLINES_FONT_ID), sampleState).x;
    STATE_WIDTH += Application.IS_WINDOWS ? 25 : 30; // Bounds of Column requires more space (arrow indicator)

    return STATE_WIDTH;
  }

  /**
   * Custom Owner Drawn helper to draw a gradient across a Scrollable item.
   *
   * @param event the erase event.
   * @param fg gradient foreground.
   * @param bg gradient background.
   * @param end gradient end.
   */
  public static void codDrawGradient(Event event, Color fg, Color bg, Color end) {
    Scrollable scrollable = (Scrollable) event.widget;
    GC gc = event.gc;

    Rectangle area = scrollable.getClientArea();
    Rectangle rect = event.getBounds();

    /* Paint the selection beyond the end of last column */
    codExpandRegion(event, scrollable, gc, area);

    /* Draw Gradient Rectangle */
    Color oldForeground = gc.getForeground();
    Color oldBackground = gc.getBackground();

    /* Gradient */
    gc.setForeground(fg);
    gc.setBackground(bg);
    gc.fillGradientRectangle(0, rect.y, area.width, rect.height, true);

    /* Bottom Line */
    gc.setForeground(end);
    gc.drawLine(0, rect.y + rect.height - 1, area.width, rect.y + rect.height - 1);

    gc.setForeground(oldForeground);
    gc.setBackground(oldBackground);

    /* Mark as Background being handled */
    event.detail &= ~SWT.BACKGROUND;
  }

  /**
   * Custom Owner Draw helper to expand a drawn region over a scrollable item.
   *
   * @param event the erase event.
   * @param scrollable the scrollable to paint on.
   * @param gc the gc to paint on.
   * @param area the drawable area.
   */
  public static void codExpandRegion(Event event, Scrollable scrollable, GC gc, Rectangle area) {
    int columnCount;
    if (scrollable instanceof Table)
      columnCount = ((Table) scrollable).getColumnCount();
    else
      columnCount = ((Tree) scrollable).getColumnCount();

    if (event.index == columnCount - 1 || columnCount == 0) {
      int width = area.x + area.width - event.x;
      if (width > 0) {
        Region region = new Region();
        gc.getClipping(region);
        region.add(event.x, event.y, width, event.height);
        gc.setClipping(region);
        region.dispose();
      }
    }
  }

  /**
   * @param shell the {@link Shell} as parent of the {@link WizardDialog}.
   * @param wizard the {@link Wizard} to use in the {@link WizardDialog}.
   * @param modal if <code>false</code>, the wizard will not be a modal dialog.
   * @param needsProgressPart <code>true</code> to leave some room for the
   * {@link ProgressMonitorPart} and <code>false</code> otherwise.
   * @param dialogSettingsKey the key to use to store dialog settings.
   */
  public static void openWizard(Shell shell, Wizard wizard, final boolean modal, final boolean needsProgressPart, final String dialogSettingsKey) {
    openWizard(shell, wizard, modal, needsProgressPart, dialogSettingsKey, false, null);
  }

  /**
   * @param shell the {@link Shell} as parent of the {@link WizardDialog}.
   * @param wizard the {@link Wizard} to use in the {@link WizardDialog}.
   * @param modal if <code>false</code>, the wizard will not be a modal dialog.
   * @param needsProgressPart <code>true</code> to leave some room for the
   * {@link ProgressMonitorPart} and <code>false</code> otherwise.
   * @param dialogSettingsKey the key to use to store dialog settings.
   * @param pack if <code>true</code>, make the wizard as compact as possible
   * and <code>false</code> otherwise.
   * @param finishLabel the label for the finish button or <code>null</code> to
   * use the default.
   */
  public static void openWizard(Shell shell, Wizard wizard, final boolean modal, final boolean needsProgressPart, final String dialogSettingsKey, final boolean pack, final String finishLabel) {
    CustomWizardDialog dialog = new CustomWizardDialog(shell, wizard) {
      private ProgressMonitorPart progressMonitorPart;

      @Override
      protected boolean isResizable() {
        return true;
      }

      @Override
      protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        if (progressMonitorPart != null && !needsProgressPart)
          ((GridData) progressMonitorPart.getLayoutData()).exclude = true;
        return control;
      }

      @Override
      public boolean close() {
        progressMonitorPart = null;
        return super.close();
      }

      @Override
      protected ProgressMonitorPart createProgressMonitorPart(Composite composite, GridLayout pmlayout) {
        progressMonitorPart = super.createProgressMonitorPart(composite, pmlayout);
        return progressMonitorPart;
      }

      @Override
      protected IDialogSettings getDialogBoundsSettings() {
        if (dialogSettingsKey != null) {
          IDialogSettings settings = Activator.getDefault().getDialogSettings();
          IDialogSettings section = settings.getSection(dialogSettingsKey);
          if (section != null)
            return section;

          return settings.addNewSection(dialogSettingsKey);
        }

        return super.getDialogBoundsSettings();
      }

      @Override
      protected int getShellStyle() {
        if (modal)
          return super.getShellStyle();

        return SWT.TITLE | SWT.BORDER | SWT.MIN | SWT.RESIZE | SWT.CLOSE | getDefaultOrientation();
      }

      @Override
      protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTSIZE;
      }

      @Override
      protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (IDialogConstants.FINISH_ID == id && StringUtils.isSet(finishLabel))
          label = finishLabel;

        return super.createButton(parent, id, label, defaultButton);
      }

      @Override
      protected Point getInitialSize() {
        if (pack) {
          int width = Application.IS_WINDOWS ? WINDOWS_PACKED_WIZARD_WIDTH : Application.IS_LINUX ? LINUX_PACKED_WIZARD_WIDTH : MAC_PACKED_WIZARD_WIDTH;
          return getShell().computeSize(convertHorizontalDLUsToPixels(width), SWT.DEFAULT, true);
        }

        return super.getInitialSize();
      }
    };
    dialog.setMinimumPageSize(0, 0);
    dialog.create();
    dialog.open();
  }

  /**
   * @param folder a selected {@link IFolder}
   * @return the selected {@link IFolder} if part of the currently selected
   * Bookmark Set, or the currently selected Bookmark Set otherwise.
   * @throws PersistenceException in case of an error while loading.
   */
  public static IFolder getSelectedParent(IFolder folder) throws PersistenceException {
    String selectedBookMarkSetPref = BookMarkExplorer.getSelectedBookMarkSetPref(getWindow());
    IPreference preference = DynamicDAO.getDAO(IPreferenceDAO.class).load(selectedBookMarkSetPref);
    if (preference != null) {
      Long selectedRootFolderID = preference.getLong();

      /* Check if available Parent is still valid */
      if (folder != null) {
        if (hasParent(folder, new FolderReference(selectedRootFolderID)))
          return folder;
      }

      /* Otherwise return visible root-folder */
      return new FolderReference(selectedRootFolderID).resolve();
    }

    Set<IFolder> roots = CoreUtils.loadRootFolders();
    if (!roots.isEmpty())
      return roots.iterator().next();

    return null;
  }

  private static boolean hasParent(IFolder folder, FolderReference folderRef) {
    if (folder == null)
      return false;

    if (folderRef.references(folder))
      return true;

    return hasParent(folder.getParent(), folderRef);
  }

  /**
   * Adjust the bounds of the given Shell to respect the addition or removal of
   * the vertical bar.
   *
   * @param shell the Shell of the container.
   * @param verticalBar the vertical {@link ScrollBar} of the container.
   * @param wasScrollbarShowing <code>true</code> if the vertical scrollbar was
   * showing and <code>false</code> otherwise.
   */
  public static void adjustSizeForScrollbar(Shell shell, ScrollBar verticalBar, boolean wasScrollbarShowing) {
    if (verticalBar == null)
      return;

    /* Ignore for application window */
    if (shell.getParent() == null)
      return;

    int barWidth = verticalBar.getSize().x;
    if (Application.IS_MAC && barWidth == 0)
      barWidth = 16; //Can be 0 on Mac

    if (wasScrollbarShowing != verticalBar.isVisible()) {
      Rectangle shellBounds = shell.getBounds();

      /* Increase if Scrollbar now Visible */
      if (!wasScrollbarShowing)
        shell.setBounds(shellBounds.x, shellBounds.y, shellBounds.width + barWidth, shellBounds.height);

      /* Reduce if Scrollbar now Invisible */
      else
        shell.setBounds(shellBounds.x, shellBounds.y, shellBounds.width - barWidth, shellBounds.height);
    }
  }

  /**
   * @param name the name of the attachment.
   * @param mimeType the mime type of the attachment or <code>null</code> if
   * none.
   * @return an {@link ImageDescriptor} for the attachment. Never
   * <code>null</code>.
   */
  public static ImageDescriptor getAttachmentImage(String name, String mimeType) {

    /* First try to lookup image from Mime Type */
    ImageDescriptor descriptor = getImageForMime(mimeType);
    if (descriptor != null)
      return descriptor;

    /* Second try to lookup image from File Name */
    descriptor = getImageForFile(name);
    if (descriptor != null)
      return descriptor;

    /* Return Default */
    return ATTACHMENT;
  }

  /* Find a Image for the given File Name using Program API from SWT */
  private static ImageDescriptor getImageForFile(String file) {
    if (StringUtils.isSet(file)) {
      int lastIndexOfDot = file.lastIndexOf('.');
      if (lastIndexOfDot != -1 && !file.endsWith(".")) { //$NON-NLS-1$
        String extension = file.substring(lastIndexOfDot + 1);
        return getImageForExtension(extension.toLowerCase());
      }
    }

    return null;
  }

  /* Find a Image for the given Mime Type using Program API from SWT */
  private static ImageDescriptor getImageForMime(String mime) {
    if (StringUtils.isSet(mime)) {
      String extension = getExtensionForMime(mime);
      return getImageForExtension(extension);
    }

    return null;
  }

  /**
   * @param mime the mime type
   * @return the extension for the mime type or <code>null</code> if none
   */
  public static String getExtensionForMime(String mime) {
    if (StringUtils.isSet(mime))
      return fgMapMimeToExtension.get(mime.toLowerCase());

    return null;
  }

  /* Find a Image for the given Extension using Program API from SWT */
  @SuppressWarnings("restriction")
  private static ImageDescriptor getImageForExtension(String extension) {
    if (StringUtils.isSet(extension)) {
      Program p = Program.findProgram(extension);
      if (p != null)
        return new org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor(p);
    }

    return null;
  }

  /**
   * @param seconds the number of seconds.
   * @return the period spanned by the seconds as human readable label.
   */
  public static String getPeriod(int seconds) {
    if (seconds > 0) {
      int hours = seconds / 3600;
      int minutes = (seconds / 60) % 60;

      /* X Hours, Y Minutes */
      if (hours > 0 && minutes > 0) {
        if (hours == 1) {
          if (minutes == 1)
            return NLS.bind(Messages.OwlUI_HOUR_MINUTE, hours, minutes);

          return NLS.bind(Messages.OwlUI_HOUR_MINUTES, hours, minutes);
        }

        if (minutes == 1)
          return NLS.bind(Messages.OwlUI_HOURS_MINUTE, hours, minutes);

        return NLS.bind(Messages.OwlUI_HOURS_MINUTES, hours, minutes);
      }

      /* X Hours */
      else if (hours > 0)
        return (hours == 1) ? NLS.bind(Messages.OwlUI_HOUR, hours) : NLS.bind(Messages.OwlUI_HOURS, hours);

      /* X Minutes */
      else if (hours == 0 && minutes > 0)
        return (minutes == 1) ? NLS.bind(Messages.OwlUI_MINUTE, minutes) : NLS.bind(Messages.OwlUI_MINUTES, minutes);

      /* X Seconds */
      else if (seconds < 60)
        return (seconds == 1) ? NLS.bind(Messages.OwlUI_SECOND, seconds) : NLS.bind(Messages.OwlUI_SECONDS, seconds);
    }

    return null;
  }

  /**
   * @param bytes the number of bytes.
   * @return a human readable representation of the bytes.
   */
  public static String getSize(long bytes) {
    if (bytes > 0) {
      double gb = bytes / (1024d * 1024d * 1024d);
      double mb = bytes / (1024d * 1024d);
      double kb = bytes / 1024d;

      NumberFormat format = new DecimalFormat(Messages.OwlUI_SIZE_FORMAT);

      if (gb >= 1)
        return NLS.bind(Messages.OwlUI_OwlUI_N_GB, format.format(gb));

      if (mb >= 1)
        return NLS.bind(Messages.OwlUI_N_MB, format.format(mb));

      if (kb >= 1)
        return NLS.bind(Messages.OwlUI_N_KB, format.format(kb));

      return NLS.bind(Messages.OwlUI_N_BYTES, bytes);
    }

    return null;
  }

  /**
   * @return the Size of the {@link Monitor} if only a single monitor is used or
   * <code>null</code> if none.
   */
  public static Point getFirstMonitorSize() {
    Display display = Display.getDefault();
    if (display != null) {
      Monitor[] monitors = display.getMonitors();
      if (monitors.length == 1) {
        Rectangle clientArea = monitors[0].getClientArea();
        return new Point(clientArea.width, clientArea.height);
      }
    }

    return null;
  }

  /**
   * Switch between full-screen and normal screen.
   */
  public static void toggleFullScreen() {
    Shell shell = OwlUI.getActiveShell();
    if (shell != null) {
      shell.setFullScreen(!shell.getFullScreen());

      /* Shell got restored */
      if (!shell.getFullScreen()) {
        ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;
        configurer.setStatusVisible(Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.SHOW_STATUS), false);

        shell.layout(); //Need to layout to avoid screen cheese
      }

      /* Shell got fullscreen */
      else {
        ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;
        configurer.setStatusVisible(false, true);
      }
    }
  }

  /**
   * Switch between showing and hiding the Bookmarks View.
   */
  public static void toggleBookmarks() {
    IWorkbenchPage page = OwlUI.getPage();
    if (page != null) {
      IViewPart explorerView = page.findView(BookMarkExplorer.VIEW_ID);

      /* Hide Bookmarks */
      if (explorerView != null)
        page.hideView(explorerView);

      /* Show Bookmarks */
      else {
        try {
          page.showView(BookMarkExplorer.VIEW_ID);
        } catch (PartInitException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * @param action the dropdown action.
   * @param manager the toolbar containing the action.
   */
  public static void positionDropDownMenu(Action action, ToolBarManager manager) {
    Menu menu = action.getMenuCreator().getMenu(manager.getControl());
    if (menu != null) {

      /* Adjust Location */
      IContributionItem contributionItem = manager.find(action.getId());
      if (contributionItem != null && contributionItem instanceof ActionContributionItem) {
        Widget widget = ((ActionContributionItem) contributionItem).getWidget();
        if (widget != null && widget instanceof ToolItem) {
          ToolItem item = (ToolItem) widget;
          Rectangle rect = item.getBounds();
          Point pt = new Point(rect.x, rect.y + rect.height);
          pt = manager.getControl().toDisplay(pt);
          if (Application.IS_MAC)
            pt.y += 5;
          menu.setLocation(pt.x, pt.y);
        }
      }

      /* Set Visible */
      menu.setVisible(true);
    }
  }

  /**
   * @param zoomIn
   * @param reset
   */
  @SuppressWarnings("restriction")
  public static void zoomNewsText(boolean zoomIn, boolean reset) {

    /* Retrieve Font */
    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
    FontRegistry registry = theme.getFontRegistry();
    FontData[] oldFontDatas = registry.getFontData(NEWS_TEXT_FONT_ID);
    FontData[] newFontDatas = new FontData[oldFontDatas.length];

    /* Set Height */
    for (int i = 0; i < oldFontDatas.length; i++) {
      FontData oldFontData = oldFontDatas[i];
      int oldHeight = oldFontData.getHeight();

      if (reset)
        newFontDatas[i] = new FontData(oldFontData.getName(), DEFAULT_NEWS_TEXT_FONT_HEIGHT, oldFontData.getStyle());
      else
        newFontDatas[i] = new FontData(oldFontData.getName(), zoomIn ? oldHeight + 1 : Math.max(oldHeight - 1, 0), oldFontData.getStyle());
    }

    registry.put(NEWS_TEXT_FONT_ID, newFontDatas);

    /* Store in Preferences */
    String key = org.eclipse.ui.internal.themes.ThemeElementHelper.createPreferenceKey(theme, NEWS_TEXT_FONT_ID);
    String fdString = PreferenceConverter.getStoredRepresentation(newFontDatas);
    String storeString = org.eclipse.ui.internal.util.PrefUtil.getInternalPreferenceStore().getString(key);
    if (!fdString.equals(storeString))
      org.eclipse.ui.internal.util.PrefUtil.getInternalPreferenceStore().setValue(key, fdString);
  }

  /**
   * @param run the {@link Runnable} to run on selection changes.
   * @param control the control to add selection listener to. Will recursively
   * go into child controls for Composites.
   */
  public static void runOnSelection(final Runnable run, Control... control) {
    for (Control c : control) {

      /* Button */
      if (c instanceof Button) {
        Button button = (Button) c;
        button.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            run.run();
          }
        });
      }

      /* Combo */
      else if (c instanceof Combo) {
        Combo combo = (Combo) c;
        combo.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            run.run();
          }
        });
      }

      /* Tree */
      else if (c instanceof Tree) {
        Tree tree = (Tree) c;
        tree.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if ((e.detail & SWT.CHECK) != 0)
              run.run();
          }
        });

        tree.addDragDetectListener(new DragDetectListener() {
          public void dragDetected(DragDetectEvent e) {
            run.run();
          }
        });
      }

      /* Table */
      else if (c instanceof Table) {
        Table table = (Table) c;
        table.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if ((e.detail & SWT.CHECK) != 0)
              run.run();
          }
        });

        table.addDragDetectListener(new DragDetectListener() {
          public void dragDetected(DragDetectEvent e) {
            run.run();
          }
        });
      }

      /* Spinner */
      else if (c instanceof Spinner) {
        Spinner spinner = (Spinner) c;
        spinner.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            run.run();
          }
        });
      }

      /* Text */
      else if (c instanceof Text) {
        Text text = (Text) c;
        text.addModifyListener(new ModifyListener() {
          public void modifyText(ModifyEvent e) {
            run.run();
          }
        });
      }

      /* Composite */
      else if (c instanceof Composite) {
        Composite composite = (Composite) c;
        runOnSelection(run, composite.getChildren());
      }
    }
  }

  /**
   * @return the {@link DateFormat} used for short dates. Respects the system
   * property to override this value from default.
   */
  public static DateFormat getShortDateFormat() {
    String format = System.getProperty(SHORT_DATE_FORMAT_PROPERTY);
    if (StringUtils.isSet(format)) {
      try {
        return new SimpleDateFormat(format);
      } catch (Exception e) {
        /* Ignore and use Default */
      }
    }

    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  }

  /**
   * @return the {@link DateFormat} used for long dates. Respects the system
   * property to override this value from default.
   */
  public static DateFormat getLongDateFormat() {
    String format = System.getProperty(LONG_DATE_FORMAT_PROPERTY);
    if (StringUtils.isSet(format)) {
      try {
        return new SimpleDateFormat(format);
      } catch (Exception e) {
        /* Ignore and use Default */
      }
    }

    return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
  }

  /**
   * @return the {@link DateFormat} used for short times. Respects the system
   * property to override this value from default.
   */
  public static DateFormat getShortTimeFormat() {
    String format = System.getProperty(SHORT_TIME_FORMAT_PROPERTY);
    if (StringUtils.isSet(format)) {
      try {
        return new SimpleDateFormat(format);
      } catch (Exception e) {
        /* Ignore and use Default */
      }
    }

    return DateFormat.getTimeInstance(DateFormat.SHORT);
  }

  /**
   * @param control the control to provide an accessible name for.
   * @param label the label control to take the label from.
   */
  public static void makeAccessible(Control control, Control label) {
    if (label == null || label.isDisposed())
      return;

    if (label instanceof Button)
      makeAccessible(control, ((Button) label).getText());
    else if (label instanceof Label)
      makeAccessible(control, ((Label) label).getText());
    else if (label instanceof CLabel)
      makeAccessible(control, ((CLabel) label).getText());
  }

  /**
   * @param control the control to provide an accessible name for.
   * @param name the name for the control to be used in accessible environments.
   */
  public static void makeAccessible(final Control control, String name) {

    /* Strip Mnemonics */
    final String accessibleName;
    if (name.contains("&")) //$NON-NLS-1$
      accessibleName = StringUtils.replaceAll(name, "&", ""); //$NON-NLS-1$ //$NON-NLS-2$
    else
      accessibleName = name;

    /* Apply Accessible Name */
    if (control != null && !control.isDisposed()) {
      control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
        @Override
        public void getName(AccessibleEvent e) {
          if (control instanceof Tree || control instanceof Table) {
            if (e.childID == ACC.CHILDID_SELF)
              e.result = accessibleName;
            else if (!control.isDisposed()) {
              Widget widget = control.getDisplay().findWidget(control, e.childID);
              if (widget != null && widget instanceof Item)
                e.result = NLS.bind(Messages.OwlUI_ACCESSIBLE_NAME, ((Item) widget).getText());
            }
          } else
            e.result = accessibleName;
        }
      });
    }
  }

  /**
   * @return <code>true</code> if tabbed browsing is enabled and
   * <code>false</code> otherwise.
   */
  public static boolean isTabbedBrowsingEnabled() {
    IPreferenceScope preferences = Owl.getPreferenceService().getEclipseScope();
    boolean autoCloseTabs = preferences.getBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS);
    int autoCloseTabsThreshold = preferences.getInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD);
    return !autoCloseTabs || autoCloseTabsThreshold > 1;
  }

  /**
   * @return <code>true</code> if RSSOwl is minimized (either its Shell or
   * minimized to tray) and <code>false</code> otherwise.
   */
  public static boolean isMinimized() {
    ApplicationWorkbenchWindowAdvisor advisor = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;
    if (advisor != null && (advisor.isMinimizedToTray() || advisor.isMinimized()))
      return true;

    return false;
  }

  /**
   * @return <code>true</code> if the user has configured to use an external
   * browser and <code>false</code> otherwise.
   */
  public static boolean useExternalBrowser() {
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
    return preferences.getBoolean(DefaultPreferences.USE_DEFAULT_EXTERNAL_BROWSER) || preferences.getBoolean(DefaultPreferences.USE_CUSTOM_EXTERNAL_BROWSER);
  }

  /**
   * @return the currently selected {@link IFolder} as bookmark set from the
   * feeds view or the first root folder otherwise. Falls back to
   * <code>null</code> if neither can be resolved.
   */
  public static IFolder getSelectedBookMarkSet() {
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
    IFolderDAO folderDAO = DynamicDAO.getDAO(IFolderDAO.class);

    String selectedBookMarkSetPref = BookMarkExplorer.getSelectedBookMarkSetPref(getWindow());
    long selectedFolderID = preferences.getLong(selectedBookMarkSetPref);
    IFolder selectedSet = folderDAO.load(selectedFolderID);
    if (selectedSet != null)
      return selectedSet;

    Set<IFolder> rootFolders = CoreUtils.loadRootFolders();
    if (!rootFolders.isEmpty())
      return rootFolders.iterator().next();

    return null;
  }

  /**
   * @return <code>true</code> if the preference tell to update duplicate news
   * states when marking as read and <code>false</code> otherwise.
   */
  public static boolean markReadDuplicates() {
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
    return preferences.getBoolean(DefaultPreferences.MARK_READ_DUPLICATES);
  }

  /**
   * @param scope the preferences scope to look for the defined layout.
   * @return the selected {@link Layout} from the given preferences scope.
   */
  public static Layout getLayout(IPreferenceScope scope) {
    int layoutOrdinal = scope.getInteger(DefaultPreferences.FV_LAYOUT);
    Layout[] layouts = Layout.values();
    return layoutOrdinal < layouts.length ? layouts[layoutOrdinal] : Layout.CLASSIC;
  }

  /**
   * @param scope the preferences scope to look for the defined page size.
   * @return the selected {@link PageSize} from the given preferences scope.
   */
  public static PageSize getPageSize(IPreferenceScope scope) {
    int pageSize = scope.getInteger(DefaultPreferences.NEWS_BROWSER_PAGE_SIZE);
    return PageSize.from(pageSize);
  }

  /**
   * Safely disposes the provided {@link Menu}.
   *
   * @param menu the {@link Menu} to dispose.
   */
  public static void safeDispose(Menu menu) {
    try {
      menu.dispose();
    } catch (NegativeArraySizeException e) {
      /* Bug in SWT that we can safely ignore */
    }
  }

  /**
   * Opens a file dialog to save the crash report.
   *
   * @param shell the parent {@link Shell} of the dialog that opens.
   * @throws FileNotFoundException in case of an error
   */
  public static void saveCrashReport(Shell shell) throws FileNotFoundException {
    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    dialog.setText(Messages.OwlUI_SAVE_CRASH_REPORT);
    dialog.setFilterExtensions(new String[] { "*.log" }); //$NON-NLS-1$
    dialog.setFileName("rssowl.log"); //$NON-NLS-1$
    dialog.setOverwrite(true);

    String file = dialog.open();
    if (StringUtils.isSet(file)) {

      /* Check for Log Message from Core to have a complete log */
      String logMessages = CoreUtils.getAndFlushLogMessages();
      if (logMessages != null && logMessages.length() > 0)
        Activator.safeLogError(logMessages, null);

      /* Help to find out where the log is coming from */
      Activator.safeLogInfo("Crash Report Exported"); //$NON-NLS-1$

      /* Export Log File */
      File logFile = Platform.getLogFileLocation().toFile();
      InputStream inS;
      if (logFile.exists())
        inS = new FileInputStream(logFile);
      else
        inS = new ByteArrayInputStream(new byte[0]);

      FileOutputStream outS = new FileOutputStream(new File(file));
      CoreUtils.copy(inS, outS);
    }
  }

  /**
   * Opens the Login Dialog to authenticate against sync services.
   *
   * @param shell the {@link Shell} as parent of the dialog or <code>null</code>
   * if none.
   * @return one of the {@link IDialogConstants} depending on the users choice
   * of closing the dialog with OK or Cancel.
   */
  public static int openSyncLogin(Shell shell) {
    if (shell == null)
      shell = getActiveShell();

    if (shell != null) {
      URI googleLoginUri = URI.create(SyncUtils.GOOGLE_LOGIN_URL);
      LoginDialog dialog = new LoginDialog(shell, googleLoginUri, null, true);
      dialog.setHeader(Messages.OwlUI_SYNC_LOGIN);
      dialog.setSubline(Messages.OwlUI_SYNC_LOGIN_TEXT);
      dialog.setTitleImageDescriptor(OwlUI.getImageDescriptor("icons/wizban/reader_wiz.png")); //$NON-NLS-1$

      return dialog.open();
    }

    return IDialogConstants.CANCEL_ID;
  }

  /**
   * @return <code>true</code> in case the a text control needs an extra cancel
   * control to clear a search and <code>false</code> if the OS provides a
   * native one already.
   */
  public static boolean needsCancelControl() {
    if (Application.IS_WINDOWS)
      return true; //Windows does not support a native cancel button in text fields

    if (Application.IS_MAC)
      return false; //Mac supports native cancel button in text fields

    return SWT.getVersion() < 3700; //Some Linux distros support it with recent SWT version
  }
}