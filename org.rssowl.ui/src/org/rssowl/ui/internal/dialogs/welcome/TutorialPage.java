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

package org.rssowl.ui.internal.dialogs.welcome;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance of {@link WizardPage} that is used from the
 * {@link TutorialWizard} and {@link WelcomeWizard} to guide the user.
 *
 * @author bpasero
 */
public class TutorialPage extends WizardPage {
  private static final char BOLD = '#';
  private static final char BLUE = '%';

  private final Chapter fChapter;

  /** Chapters of the Tutorial */
  public enum Chapter {
    INTRO,
    LAYOUT,
    NEWS,
    SAVEDSEARCH,
    NEWSBIN,
    NEWSFILTER,
    NOTIFIER,
    SHARING,
    IMPORT_EXPORT,
    SYNCHRONIZATION,
    PREFERENCES,
    TIPS,
    FINISH
  }

  TutorialPage(Chapter chapter) {
    super(getTitle(chapter), getTitle(chapter), null);
    fChapter = chapter;
    setMessage(getMessage(chapter));
  }

  private static String getTitle(Chapter chapter) {
    switch (chapter) {
      case INTRO:
        return Messages.TutorialPage_INTRO;
      case LAYOUT:
        return Messages.TutorialPage_OVERVIEW;
      case NEWS:
        return Messages.TutorialPage_NEWS;
      case SAVEDSEARCH:
        return Messages.TutorialPage_SAVED_SEARCHES;
      case NEWSBIN:
        return Messages.TutorialPage_NEWS_BINS;
      case NEWSFILTER:
        return Messages.TutorialPage_NEWS_FILTERS;
      case NOTIFIER:
        return Messages.TutorialPage_NOTIFICATIONS;
      case SHARING:
        return Messages.TutorialPage_SHARING;
      case IMPORT_EXPORT:
        return Messages.TutorialPage_IMPORT_EXPORT;
      case SYNCHRONIZATION:
        return Messages.TutorialPage_SYNCHRONIZATION;
      case PREFERENCES:
        return Messages.TutorialPage_PREFERENCES;
      case TIPS:
        return Messages.TutorialPage_TIPS_AND_TRICKS;
      case FINISH:
        return Messages.TutorialPage_FINISH;
    }

    return null;
  }

  private static String getMessage(Chapter chapter) {
    switch (chapter) {
      case INTRO:
        return Messages.TutorialPage_WELCOME_TUTORIAL;
      case LAYOUT:
        return Messages.TutorialPage_OVERVIEW_TITLE;
      case NEWS:
        return Messages.TutorialPage_WORKING_WITH_NEWS;
      case SAVEDSEARCH:
        return Messages.TutorialPage_SAVING_SEARCH_RESULTS;
      case NEWSBIN:
        return Messages.TutorialPage_STORING_NEWS_BINS;
      case NEWSFILTER:
        return Messages.TutorialPage_NEWS_FILTER_POWER;
      case NOTIFIER:
        return Messages.TutorialPage_NOTIFIER_TITLE;
      case SHARING:
        return Messages.TutorialPage_SHARE_FEEDS_TITLE;
      case IMPORT_EXPORT:
        return Messages.TutorialPage_IMPORT_EXPORT_TITLE;
      case SYNCHRONIZATION:
        return Messages.TutorialPage_SYNCHRONIZATION_TITLE;
      case PREFERENCES:
        return Messages.TutorialPage_CONFIGURE_TITLE;
      case TIPS:
        return Messages.TutorialPage_TIPS_TITLE;
      case FINISH:
        return Messages.TutorialPage_FINISH_TITLE;
    }

    return null;
  }

  private ImageDescriptor getTitleImage() {
    switch (fChapter) {
      case LAYOUT:
        return OwlUI.getImageDescriptor("icons/wizban/layout_wiz.png"); //$NON-NLS-1$
      case NEWS:
        return OwlUI.getImageDescriptor("icons/wizban/bkmrk_wiz.gif"); //$NON-NLS-1$
      case SAVEDSEARCH:
        return OwlUI.getImageDescriptor("icons/wizban/search.gif"); //$NON-NLS-1$
      case NEWSBIN:
        return OwlUI.getImageDescriptor("icons/wizban/newsbin_wiz.gif"); //$NON-NLS-1$
      case NEWSFILTER:
        return OwlUI.getImageDescriptor("icons/wizban/filter_wiz.png"); //$NON-NLS-1$
      case NOTIFIER:
        return OwlUI.getImageDescriptor("icons/wizban/notifier_wiz.gif"); //$NON-NLS-1$
      case SHARING:
        return OwlUI.getImageDescriptor("icons/wizban/sharing_wiz.gif"); //$NON-NLS-1$
      case IMPORT_EXPORT:
        return OwlUI.getImageDescriptor("icons/wizban/import_wiz.png"); //$NON-NLS-1$
      case SYNCHRONIZATION:
        return OwlUI.getImageDescriptor("icons/wizban/reader_wiz.png"); //$NON-NLS-1$
      case PREFERENCES:
        return OwlUI.getImageDescriptor("icons/wizban/preferences_wiz.gif"); //$NON-NLS-1$
      case TIPS:
        return OwlUI.getImageDescriptor("icons/wizban/tips_wiz.png"); //$NON-NLS-1$
      default:
        return OwlUI.getImageDescriptor("icons/wizban/welcome_wiz.gif"); //$NON-NLS-1$
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {

    /* Title Image */
    setImageDescriptor(getTitleImage());

    /* Container */
    final Composite container = new Composite(parent, SWT.BORDER);
    container.setLayout(new GridLayout(1, false));
    container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    /* Contents */
    SafeRunnable.run(new ISafeRunnable() {
      @Override
      public void run() throws Exception {
        createContents(container);
      }

      @Override
      public void handleException(Throwable th) {
        Activator.safeLogError(th.getMessage(), th);
      }
    });

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  private void createContents(Composite container) {
    switch (fChapter) {
      case INTRO:
        createIntroPage(container);
        break;
      case LAYOUT:
        createOverviewPage(container);
        break;
      case NEWS:
        createNewsPage(container);
        break;
      case SAVEDSEARCH:
        createSavedSearchPage(container);
        break;
      case NEWSBIN:
        createNewsBinPage(container);
        break;
      case NEWSFILTER:
        createNewsFilterPage(container);
        break;
      case NOTIFIER:
        createNotificationsPage(container);
        break;
      case SHARING:
        createSharingPage(container);
        break;
      case IMPORT_EXPORT:
        createImportExportPage(container);
        break;
      case SYNCHRONIZATION:
        createSynchronizationPage(container);
        break;
      case PREFERENCES:
        createPreferencesPage(container);
        break;
      case TIPS:
        createTipsPage(container);
        break;
      case FINISH:
        createFinishPage(container);
        break;
    }
  }

  private void createIntroPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_WELCOME_TEXT, text);
  }

  private void createOverviewPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_LAYOUT_TEXT, text);
  }

  private void createNewsPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_NEWS_TEXT, text);
  }

  private void createSavedSearchPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_SAVED_SEARCHES_TEXT, text);
  }

  private void createNewsBinPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_NEWS_BIN_TEXT, text);
  }

  private void createNewsFilterPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_NEWS_FILTER_TEXT, text);
  }

  private void createNotificationsPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_NOTIFIER_TEXT, text);
  }

  private void createSharingPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_SHARING_TEXT, text);
  }

  private void createImportExportPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_IMPORT_EXPORT_TEXT, text);
  }

  private void createSynchronizationPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_SYNCHRONIZATION_TEXT, text);
  }

  private void createPreferencesPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_PREFERENCES_TEXT, text);
  }

  private void createTipsPage(Composite container) {
    StyledText text = createStyledText(container);
    applyRichText(Messages.TutorialPage_TIPS_TEXT, text);
  }

  private void createFinishPage(Composite container) {
    StyledText text = createStyledText(container, false);
    applyRichText(Messages.TutorialPage_FINISH_TEXT, text);

    Composite linkContainer = new Composite(container, SWT.NONE);
    linkContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    linkContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    linkContainer.setBackground(container.getBackground());

    /* Further Links */
    createHyperLink(linkContainer, Messages.TutorialPage_FAQ, "http://www.rssowl.org/help"); //$NON-NLS-1$
    createHyperLink(linkContainer, Messages.TutorialPage_FORUMS, "http://sourceforge.net/projects/rssowl/forums"); //$NON-NLS-1$
    createHyperLink(linkContainer, Messages.TutorialPage_REPORT_BUGS, "http://dev.rssowl.org"); //$NON-NLS-1$
    createHyperLink(linkContainer, Messages.TutorialPage_CONTACT, "http://www.rssowl.org/contact"); //$NON-NLS-1$
    createHyperLink(linkContainer, Messages.TutorialPage_WEBSITE, "http://www.rssowl.org"); //$NON-NLS-1$
  }

  private StyledText createStyledText(Composite container) {
    return createStyledText(container, true);
  }

  private StyledText createStyledText(Composite container, boolean grabVertical) {
    StyledText text = new StyledText(container, SWT.WRAP | SWT.READ_ONLY);
    text.setEnabled(false);
    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, grabVertical));
    ((GridData) text.getLayoutData()).widthHint = 500;
    text.setLineSpacing(5);
    return text;
  }

  private Link createHyperLink(Composite container, String text, final String href) {
    Link link = new Link(container, SWT.NONE);
    link.setText("<a>" + text + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
    link.setBackground(container.getBackground());
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        BrowserUtils.openLinkExternal(href);
      }
    });
    return link;
  }

  private void applyRichText(String text, StyledText widget) {
    List<StyleRange> ranges = new ArrayList<StyleRange>();
    StringBuilder buffer = new StringBuilder();
    StringReader reader = new StringReader(text);
    int character;
    int index = 0;
    boolean inBold = false;
    boolean inBlue = false;
    int boldStartIndex = 0;
    int blueStartIndex = 0;
    try {
      while ((character = reader.read()) != -1) {

        /* Bold Start */
        if (character == BOLD && !inBold) {
          inBold = true;
          boldStartIndex = index;
        }

        /* Bold End */
        else if (character == BOLD && inBold) {
          inBold = false;
          ranges.add(new StyleRange(boldStartIndex, index - boldStartIndex, null, null, SWT.BOLD));
        }

        /* Blue Start */
        else if (character == BLUE && !inBlue) {
          inBlue = true;
          blueStartIndex = index;
        }

        /* Blue End */
        else if (character == BLUE && inBlue) {
          inBlue = false;
          StyleRange range = new StyleRange();
          range.foreground = widget.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
          range.start = blueStartIndex;
          range.length = index - blueStartIndex;

          ranges.add(range);
        }

        /* Normal Character */
        else {
          buffer.append((char) character);
          index++;
        }
      }
    } catch (IOException e) {
      /* Never Reached */
    }

    widget.setText(buffer.toString());
    widget.setStyleRanges(ranges.toArray(new StyleRange[ranges.size()]));
  }
}