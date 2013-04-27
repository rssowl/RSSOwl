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

package org.rssowl.ui.internal.dialogs.importer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ICategoryDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.dao.IPersonDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.welcome.WelcomeWizard;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link WizardPage} to select the source of import.
 *
 * @author bpasero
 */
public class ImportSourcePage extends WizardPage {

  /* Max number of Sources to remember */
  private static final int MAX_REMEMBER_SOURCES = 20;

  private Button fImportFromResourceRadio;
  private Combo fResourceInput;
  private Button fBrowseFileButton;
  private Button fImportFromKeywordRadio;
  private Combo fKeywordInput;
  private Button fImportGoogleReaderRadio;
  private Button fImportFromRecommendedRadio;
  private Button fImportNoneRadio;
  private IPreferenceScope fPreferences;
  private boolean fIsAutoCompleteKeywordHooked;
  private final String fFileOrWebsite;
  private final boolean fIsKewordSearch;
  private Button fLocalizedFeedSearch;

  /** Sources for Import */
  public enum Source {

    /** User has decided against importing */
    NONE,

    /** User has provided a resource (either local or online) */
    RESOURCE,

    /** User wants to search feeds matching a keyword */
    KEYWORD,

    /** User wants to import from recommended */
    RECOMMENDED,

    /** User wants to import from Google Reader */
    GOOGLE
  }

  ImportSourcePage(String fileOrWebsite, boolean isKewordSearch) {
    super(Messages.ImportSourcePage_CHOOSE_SOURCE, Messages.ImportSourcePage_CHOOSE_SOURCE, null);
    fFileOrWebsite = fileOrWebsite;
    fIsKewordSearch = isKewordSearch;
    fPreferences = Owl.getPreferenceService().getGlobalScope();
  }

  /**
   * @return the type of Source to use for the Import
   */
  public Source getSource() {
    if (fImportFromResourceRadio.getSelection())
      return Source.RESOURCE;

    if (SyncUtils.ENABLED && fImportGoogleReaderRadio.getSelection())
      return Source.GOOGLE;

    if (fImportFromKeywordRadio.getSelection())
      return Source.KEYWORD;

    if (fImportNoneRadio != null && fImportNoneRadio.getSelection())
      return Source.NONE;

    return Source.RECOMMENDED;
  }

  /* Returns the Resource to Import from or null if none */
  String getImportResource() {
    return fImportFromResourceRadio.getSelection() ? fResourceInput.getText() : null;
  }

  /* Returns the Keywords to Import from or null if none */
  String getImportKeywords() {
    return fImportFromKeywordRadio.getSelection() ? fKeywordInput.getText() : null;
  }

  /* Returns whether the source is only remotely accessible */
  boolean isRemoteSource() {
    Source source = getSource();
    if (source == Source.KEYWORD)
      return true;

    if (source == Source.GOOGLE)
      return true;

    if (source == Source.RESOURCE) {
      String resource = getImportResource();
      if (!new File(resource).exists() && URIUtils.looksLikeLink(resource, false))
        return true;
    }

    return false;
  }

  /* Returns whether the feed search should respect the client language */
  boolean isLocalizedFeedSearch() {
    return fLocalizedFeedSearch.getSelection();
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    boolean isWelcome = isWelcome();

    /* Title Image */
    setImageDescriptor(OwlUI.getImageDescriptor(isWelcome ? "icons/wizban/welcome_wiz.gif" : "icons/wizban/import_wiz.png")); //$NON-NLS-1$ //$NON-NLS-2$

    /* Override Title and Message if this is the Welcome Wizard */
    if (isWelcome) {
      setTitle(Messages.ImportSourcePage_WELCOME);
      setMessage(Messages.ImportSourcePage_WELCOME_INFO);
    } else
      setMessage(Messages.ImportSourcePage_CHOOSE_SOURCE_FOR_IMPORT);

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* Welcome Wizard */
    if (isWelcome) {

      /* Import from Recommended Feeds */
      createImportRecommendedControls(container);

      /* Import from Google Reader */
      if (SyncUtils.ENABLED) {
        createImportGoogleReaderControls(container);
      }

      /* Import from File or Website */
      createImportResourceControls(container);

      /* Import from Keyword Search */
      createImportKeywordControls(container);

      /* Import None */
      createImportNoneControls(container);
    }

    /*  Import Wizard */
    else {

      /* Import from File or Website */
      createImportResourceControls(container);

      /* Import from Keyword Search */
      createImportKeywordControls(container);

      /* Import from Google Reader */
      if (SyncUtils.ENABLED) {
        createImportGoogleReaderControls(container);
      }

      /* Import from Recommended Feeds */
      createImportRecommendedControls(container);
    }

    Dialog.applyDialogFont(container);

    setControl(container);
    updatePageComplete();
  }

  private void createImportResourceControls(Composite container) {
    fImportFromResourceRadio = new Button(container, SWT.RADIO);
    fImportFromResourceRadio.setSelection(!fIsKewordSearch && !isWelcome());
    fImportFromResourceRadio.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    if (isWelcome())
      ((GridData) fImportFromResourceRadio.getLayoutData()).verticalIndent = 10;
    fImportFromResourceRadio.setText(Messages.ImportSourcePage_IMPORT_FROM_FILE_OR_WEBSITE);
    fImportFromResourceRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updatePageComplete();
        boolean importFromResource = fImportFromResourceRadio.getSelection();
        fResourceInput.setEnabled(importFromResource);
        fBrowseFileButton.setEnabled(importFromResource);
        if (importFromResource)
          fResourceInput.setFocus();
      }
    });

    Composite sourceInputContainer = new Composite(container, SWT.NONE);
    sourceInputContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    ((GridLayout) sourceInputContainer.getLayout()).marginLeft = 15;
    ((GridLayout) sourceInputContainer.getLayout()).marginBottom = 10;
    sourceInputContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fResourceInput = new Combo(sourceInputContainer, SWT.DROP_DOWN | SWT.BORDER);
    OwlUI.makeAccessible(fResourceInput, fImportFromResourceRadio);
    fResourceInput.setEnabled(fImportFromResourceRadio.getSelection());
    fResourceInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    if (StringUtils.isSet(fFileOrWebsite))
      fResourceInput.setText(fFileOrWebsite);
    else if (!isWelcome() && !fIsKewordSearch) {
      String cbLink = loadInitialLinkFromClipboard(sourceInputContainer.getDisplay());
      if (StringUtils.isSet(cbLink))
        fResourceInput.setText(cbLink);
    }

    if (fImportFromResourceRadio.getSelection())
      fResourceInput.setFocus();

    String[] previousResources = fPreferences.getStrings(DefaultPreferences.IMPORT_RESOURCES);
    if (previousResources != null) {
      fResourceInput.setVisibleItemCount(previousResources.length);
      for (String source : previousResources) {
        fResourceInput.add(source);
      }
    }

    fResourceInput.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updatePageComplete();
      }
    });

    fBrowseFileButton = new Button(sourceInputContainer, SWT.PUSH);
    fBrowseFileButton.setText(Messages.ImportSourcePage_BROWSE);
    fBrowseFileButton.setEnabled(fImportFromResourceRadio.getSelection());
    Dialog.applyDialogFont(fBrowseFileButton);
    setButtonLayoutData(fBrowseFileButton);
    fBrowseFileButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onBrowse();
      }
    });
  }

  private String loadInitialLinkFromClipboard(Display display) {
    Clipboard cb = new Clipboard(display);
    TextTransfer transfer = TextTransfer.getInstance();
    String data = (String) cb.getContents(transfer);
    data = (data != null) ? data.trim() : null;
    cb.dispose();

    if (URIUtils.looksLikeLink(data, false))
      return URIUtils.ensureProtocol(data);

    return null;
  }

  private void createImportKeywordControls(Composite container) {
    fImportFromKeywordRadio = new Button(container, SWT.RADIO);
    fImportFromKeywordRadio.setSelection(fIsKewordSearch && !isWelcome());
    fImportFromKeywordRadio.setText(Messages.ImportSourcePage_IMPORT_BY_KEYWORD);
    fImportFromKeywordRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updatePageComplete();
        boolean importFromKeyword = fImportFromKeywordRadio.getSelection();
        fKeywordInput.setEnabled(importFromKeyword);
        fLocalizedFeedSearch.setEnabled(importFromKeyword);
        if (importFromKeyword) {
          hookKeywordAutocomplete(false);
          fKeywordInput.setFocus();
        }
      }
    });

    Composite keywordInputContainer = new Composite(container, SWT.NONE);
    keywordInputContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    ((GridLayout) keywordInputContainer.getLayout()).marginLeft = 15;
    ((GridLayout) keywordInputContainer.getLayout()).marginBottom = 10;
    keywordInputContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fKeywordInput = new Combo(keywordInputContainer, SWT.DROP_DOWN | SWT.BORDER);
    OwlUI.makeAccessible(fKeywordInput, fImportFromKeywordRadio);
    fKeywordInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fKeywordInput.setEnabled(fImportFromKeywordRadio.getSelection());
    if (fImportFromKeywordRadio.getSelection()) {
      hookKeywordAutocomplete(true);
      fKeywordInput.setFocus();
    }

    String[] previousKeywords = fPreferences.getStrings(DefaultPreferences.IMPORT_KEYWORDS);
    if (previousKeywords != null) {
      fKeywordInput.setVisibleItemCount(previousKeywords.length);
      for (String keyword : previousKeywords) {
        fKeywordInput.add(keyword);
      }
    }

    fKeywordInput.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updatePageComplete();
      }
    });

    fLocalizedFeedSearch = new Button(keywordInputContainer, SWT.CHECK);
    fLocalizedFeedSearch.setSelection(fPreferences.getBoolean(DefaultPreferences.LOCALIZED_FEED_SEARCH));
    fLocalizedFeedSearch.setEnabled(fImportFromKeywordRadio.getSelection());

    String clientLanguage = Locale.getDefault().getDisplayLanguage(Locale.ENGLISH);
    if (StringUtils.isSet(clientLanguage))
      fLocalizedFeedSearch.setText(NLS.bind(Messages.ImportSourcePage_MATCH_LANGUAGE_N, clientLanguage));
    else
      fLocalizedFeedSearch.setText(Messages.ImportSourcePage_MATCH_LANGUAGE);
  }

  private void createImportGoogleReaderControls(Composite container) {
    fImportGoogleReaderRadio = new Button(container, SWT.RADIO);
    fImportGoogleReaderRadio.setText(Messages.ImportSourcePage_IMPORT_GOOGLE_READER);
    fImportGoogleReaderRadio.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    if (isWelcome())
      ((GridData) fImportGoogleReaderRadio.getLayoutData()).verticalIndent = 10;
    fImportGoogleReaderRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updatePageComplete();
      }
    });
  }

  private void createImportRecommendedControls(Composite container) {
    fImportFromRecommendedRadio = new Button(container, SWT.RADIO);
    fImportFromRecommendedRadio.setText(Messages.ImportSourcePage_IMPORT_RECOMMENDED);
    fImportFromRecommendedRadio.setSelection(isWelcome());
    fImportFromRecommendedRadio.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    if (!isWelcome())
      ((GridData) fImportFromRecommendedRadio.getLayoutData()).verticalIndent = 10;
    fImportFromRecommendedRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updatePageComplete();
      }
    });
  }

  private void createImportNoneControls(Composite container) {
    fImportNoneRadio = new Button(container, SWT.RADIO);
    fImportNoneRadio.setText(Messages.ImportSourcePage_NO_MPORT);
    fImportNoneRadio.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fImportNoneRadio.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updatePageComplete();
      }
    });
  }

  private void onBrowse() {
    FileDialog dialog = new FileDialog(getShell());
    dialog.setText(Messages.ImportSourcePage_CHOOSE_FILE);

    /* Set Export Formats also for Import (we assume this is supported) */
    List<String> filterExtensions = new ArrayList<String>();
    filterExtensions.add("*.opml"); //$NON-NLS-1$
    filterExtensions.add("*.xml"); //$NON-NLS-1$

    Collection<String> exportFormats = Owl.getInterpreter().getExportFormats();
    for (String exportFormat : exportFormats) {
      String format = "*." + exportFormat.toLowerCase(); //$NON-NLS-1$
      if (!filterExtensions.contains(format))
        filterExtensions.add(format);
    }

    if (!filterExtensions.contains("*.*")) //$NON-NLS-1$
      filterExtensions.add("*.*"); //$NON-NLS-1$

    dialog.setFilterExtensions(filterExtensions.toArray(new String[filterExtensions.size()]));
    if (StringUtils.isSet(fResourceInput.getText()))
      dialog.setFileName(fResourceInput.getText());

    String string = dialog.open();
    if (string != null)
      fResourceInput.setText(string);

    updatePageComplete();
  }

  private void updatePageComplete() {
    String errorMessage = null;

    /* Import Default */
    if (fImportFromRecommendedRadio.getSelection())
      setPageComplete(true);

    /* Import Google Reader */
    if (SyncUtils.ENABLED && fImportGoogleReaderRadio.getSelection())
      setPageComplete(true);

    /* Import None */
    else if (fImportNoneRadio != null && fImportNoneRadio.getSelection())
      setPageComplete(true);

    /* Import from Resource */
    else if (fImportFromResourceRadio.getSelection()) {
      if (!StringUtils.isSet(fResourceInput.getText()))
        setPageComplete(false);
      else {
        if (isRemoteSource())
          setPageComplete(true);
        else {
          String filePath = fResourceInput.getText();
          File fileToImport = new File(filePath);
          boolean fileExists = fileToImport.exists() && fileToImport.isFile();
          setPageComplete(fileExists);
          if (!fileExists)
            errorMessage = Messages.ImportSourcePage_CHOOSE_EXISTING_FILE;
        }
      }
    }

    /* Import From Keywords */
    else if (fImportFromKeywordRadio.getSelection())
      setPageComplete(StringUtils.isSet(fKeywordInput.getText()));

    /* Set Error Message */
    if (errorMessage != null)
      setErrorMessage(errorMessage);

    /* Restore Normal Message */
    else {
      setErrorMessage(null);
      setMessage(isWelcome() ? Messages.ImportSourcePage_WELCOME_INFO : Messages.ImportSourcePage_CHOOSE_SOURCE_FOR_IMPORT);
    }
  }

  private boolean isWelcome() {
    return getWizard() instanceof WelcomeWizard;
  }

  /* Save Import Sources */
  void saveSettings() {

    /* Import Resources */
    saveComboSettings(fImportFromResourceRadio.getSelection() ? fResourceInput.getText() : null, DefaultPreferences.IMPORT_RESOURCES);

    /* Import Keywords */
    saveComboSettings(fImportFromKeywordRadio.getSelection() ? fKeywordInput.getText() : null, DefaultPreferences.IMPORT_KEYWORDS);
    fPreferences.putBoolean(DefaultPreferences.LOCALIZED_FEED_SEARCH, fLocalizedFeedSearch.getSelection());
  }

  private void saveComboSettings(String valueToAdd, String prefKey) {

    /* First fill current as new one */
    List<String> newValues = new ArrayList<String>();
    if (StringUtils.isSet(valueToAdd))
      newValues.add(valueToAdd);

    /* Then add up to N more old ones to remember */
    String[] oldValues = fPreferences.getStrings(prefKey);
    if (oldValues != null) {
      for (int i = 0; i < oldValues.length && newValues.size() < MAX_REMEMBER_SOURCES; i++) {
        if (!newValues.contains(oldValues[i]))
          newValues.add(oldValues[i]);
      }
    }

    /* Save List */
    if (!newValues.isEmpty())
      fPreferences.putStrings(prefKey, newValues.toArray(new String[newValues.size()]));
  }

  private void hookKeywordAutocomplete(boolean delay) {

    /* No Content Assist on First Welcome Wizard */
    if (isWelcome())
      return;

    /* Only perform once */
    if (fIsAutoCompleteKeywordHooked)
      return;
    fIsAutoCompleteKeywordHooked = true;

    final Pair<SimpleContentProposalProvider, ContentProposalAdapter> autoComplete = OwlUI.hookAutoComplete(fKeywordInput, null, true);

    /* Load proposals in the Background */
    JobRunner.runInBackgroundThread(delay ? 100 : 0, new Runnable() {
      public void run() {
        if (!fKeywordInput.isDisposed()) {
          Set<String> values = new TreeSet<String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
              return o1.compareToIgnoreCase(o2);
            }
          });

          /* Add all Categories */
          values.addAll(DynamicDAO.getDAO(ICategoryDAO.class).loadAllNames());

          /* Add all Authors */
          values.addAll(DynamicDAO.getDAO(IPersonDAO.class).loadAllNames());

          /* Add all Labels */
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