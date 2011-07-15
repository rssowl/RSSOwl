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

package org.rssowl.ui.internal.dialogs.bookmark;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ICategoryDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.ImportAction;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author bpasero
 */
public class FeedDefinitionPage extends WizardPage {
  private Text fFeedLinkInput;
  private Text fKeywordInput;
  private Button fLoadTitleFromFeedButton;
  private Button fFeedByLinkButton;
  private Button fFeedByKeywordButton;
  private String fInitialLink;
  private IPreferenceScope fGlobalScope = Owl.getPreferenceService().getGlobalScope();
  private boolean fIsAutoCompleteKeywordHooked;
  private Map<String, IBookMark> fExistingFeeds = new HashMap<String, IBookMark>();

  /**
   * @param pageName
   * @param initialLink
   */
  protected FeedDefinitionPage(String pageName, String initialLink) {
    super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/bkmrk_wiz.gif")); //$NON-NLS-1$
    setMessage(Messages.FeedDefinitionPage_CREATE_BOOKMARK);
    fInitialLink = initialLink;

    Collection<IBookMark> bookmarks = DynamicDAO.loadAll(IBookMark.class);
    for (IBookMark bookMark : bookmarks) {
      fExistingFeeds.put(bookMark.getFeedLinkReference().getLinkAsText(), bookMark);
    }
  }

  boolean loadTitleFromFeed() {
    return fLoadTitleFromFeedButton.getSelection();
  }

  private String loadInitialLinkFromClipboard() {
    String initial = URIUtils.HTTP;

    Clipboard cb = new Clipboard(getShell().getDisplay());
    TextTransfer transfer = TextTransfer.getInstance();
    String data = (String) cb.getContents(transfer);
    data = (data != null) ? data.trim() : null;
    cb.dispose();

    if (URIUtils.looksLikeLink(data))
      initial = URIUtils.ensureProtocol(data);

    return initial;
  }

  String getLink() {
    return fFeedByLinkButton.getSelection() ? fFeedLinkInput.getText().trim() : null;
  }

  void setLink(String link) {
    fFeedLinkInput.setText(link);
    onLinkChange();
  }

  String getKeyword() {
    return fFeedByKeywordButton.getSelection() ? fKeywordInput.getText() : null;
  }

  boolean isKeywordSubscription() {
    return StringUtils.isSet(getKeyword());
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    if (visible && !isKeywordSubscription())
      fFeedLinkInput.setFocus();
    else if (visible)
      fKeywordInput.setFocus();
  }

  /*
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete() {

    /* Checked for proper Link */
    if (fFeedByLinkButton.getSelection())
      return fFeedLinkInput.getText().length() > 0;

    /* Check for Keyword */
    return fKeywordInput.getText().length() > 0;
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* 1) Feed by Link */
    if (!StringUtils.isSet(fInitialLink))
      fInitialLink = loadInitialLinkFromClipboard();

    boolean loadTitleFromFeed = fGlobalScope.getBoolean(DefaultPreferences.BM_LOAD_TITLE_FROM_FEED);

    fFeedByLinkButton = new Button(container, SWT.RADIO);
    fFeedByLinkButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fFeedByLinkButton.setText(loadTitleFromFeed ? Messages.FeedDefinitionPage_CREATE_FEED : Messages.FeedDefinitionPage_CREATE_FEED_DIRECT);
    fFeedByLinkButton.setSelection(true);
    fFeedByLinkButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFeedLinkInput.setEnabled(fFeedByLinkButton.getSelection());
        fLoadTitleFromFeedButton.setEnabled(fFeedByLinkButton.getSelection());
        fFeedLinkInput.setFocus();
        getContainer().updateButtons();
      }
    });

    Composite textIndent = new Composite(container, SWT.NONE);
    textIndent.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    textIndent.setLayout(new GridLayout(1, false));
    ((GridLayout) textIndent.getLayout()).marginLeft = 10;
    ((GridLayout) textIndent.getLayout()).marginBottom = 10;

    fFeedLinkInput = new Text(textIndent, SWT.BORDER);
    fFeedLinkInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    OwlUI.makeAccessible(fFeedLinkInput, fFeedByLinkButton);

    GC gc = new GC(fFeedLinkInput);
    gc.setFont(JFaceResources.getDialogFont());
    FontMetrics fontMetrics = gc.getFontMetrics();
    int entryFieldWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.ENTRY_FIELD_WIDTH);
    gc.dispose();

    ((GridData) fFeedLinkInput.getLayoutData()).widthHint = entryFieldWidth; //Required to avoid large spanning dialog for long Links
    fFeedLinkInput.setFocus();

    if (StringUtils.isSet(fInitialLink) && !fInitialLink.equals(URIUtils.HTTP)) {
      fFeedLinkInput.setText(fInitialLink);
      fFeedLinkInput.selectAll();
      onLinkChange();
    } else {
      fFeedLinkInput.setText(URIUtils.HTTP);
      fFeedLinkInput.setSelection(URIUtils.HTTP.length());
    }

    fFeedLinkInput.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        getContainer().updateButtons();
        onLinkChange();
      }
    });

    fLoadTitleFromFeedButton = new Button(textIndent, SWT.CHECK);
    fLoadTitleFromFeedButton.setText(Messages.FeedDefinitionPage_USE_TITLE_OF_FEED);
    fLoadTitleFromFeedButton.setSelection(loadTitleFromFeed);
    fLoadTitleFromFeedButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getContainer().updateButtons();
      }
    });

    /* 2) Feed by Keyword */
    fFeedByKeywordButton = new Button(container, SWT.RADIO);
    fFeedByKeywordButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fFeedByKeywordButton.setText(Messages.FeedDefinitionPage_CREATE_KEYWORD_FEED);
    fFeedByKeywordButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fKeywordInput.setEnabled(fFeedByKeywordButton.getSelection());

        if (fKeywordInput.isEnabled())
          hookKeywordAutocomplete();

        fKeywordInput.setFocus();
        getContainer().updateButtons();
      }
    });

    textIndent = new Composite(container, SWT.NONE);
    textIndent.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    textIndent.setLayout(new GridLayout(1, false));
    ((GridLayout) textIndent.getLayout()).marginLeft = 10;

    fKeywordInput = new Text(textIndent, SWT.BORDER);
    OwlUI.makeAccessible(fKeywordInput, fFeedByKeywordButton);
    fKeywordInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fKeywordInput.setEnabled(false);
    fKeywordInput.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        getContainer().updateButtons();
      }
    });

    /* Info Container */
    Composite infoContainer = new Composite(container, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 5));

    Label infoImg = new Label(infoContainer, SWT.NONE);
    infoImg.setImage(OwlUI.getImage(infoImg, "icons/obj16/info.gif")); //$NON-NLS-1$
    infoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    Link infoLink = new Link(infoContainer, SWT.NONE);
    infoLink.setText(Messages.FeedDefinitionPage_IMPORT_WIZARD_TIP);
    infoLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    infoLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        new ImportAction().openWizardForKeywordSearch(getShell());
      }
    });

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  private void onLinkChange() {
    IBookMark existingBookMark = fExistingFeeds.get(fFeedLinkInput.getText());

    if (existingBookMark != null)
      setMessage(NLS.bind(Messages.FeedDefinitionPage_BOOKMARK_EXISTS, existingBookMark.getName()), WARNING);
    else
      setMessage(Messages.FeedDefinitionPage_CREATE_BOOKMARK);
  }

  private void hookKeywordAutocomplete() {

    /* Only perform once */
    if (fIsAutoCompleteKeywordHooked)
      return;
    fIsAutoCompleteKeywordHooked = true;

    final Pair<SimpleContentProposalProvider, ContentProposalAdapter> autoComplete = OwlUI.hookAutoComplete(fKeywordInput, null, true, false);

    /* Load proposals in the Background */
    JobRunner.runInBackgroundThread(new Runnable() {
      public void run() {
        if (!fKeywordInput.isDisposed()) {
          Set<String> values = new TreeSet<String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
              return o1.compareToIgnoreCase(o2);
            }
          });

          values.addAll(DynamicDAO.getDAO(ICategoryDAO.class).loadAllNames());

          Collection<ILabel> labels = DynamicDAO.getDAO(ILabelDAO.class).loadAll();
          for (ILabel label : labels) {
            values.add(label.getName());
          }

          /* Apply Proposals */
          if (!fKeywordInput.isDisposed())
            OwlUI.applyAutoCompleteProposals(values, autoComplete.getFirst(), autoComplete.getSecond(), false);
        }
      }
    });
  }
}