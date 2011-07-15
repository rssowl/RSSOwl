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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.AuthenticationRequiredException;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.HttpConnectionInputStream;
import org.rssowl.core.connection.IAbortable;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.connection.SyncConnectionException;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.interpreter.ITypeImporter;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.interpreter.ParserException;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.RegExUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.CustomWizardDialog;
import org.rssowl.ui.internal.dialogs.LoginDialog;
import org.rssowl.ui.internal.dialogs.PreviewFeedDialog;
import org.rssowl.ui.internal.dialogs.importer.ImportSourcePage.Source;
import org.rssowl.ui.internal.dialogs.welcome.WelcomeWizard;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.FolderChildCheckboxTree;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link WizardPage} to select the elements to import.
 *
 * @author bpasero
 */
public class ImportElementsPage extends WizardPage {

  /* Initial Connection Timeout when looking for Feeds remotely */
  private static final int INITIAL_CON_TIMEOUT = 30000;

  /* Connection Timeout when testing for Feeds remotely */
  private static final int FEED_CON_TIMEOUT = 7000;

  /* Default Feed Search Language */
  private static final String DEFAULT_LANGUAGE = "en"; //$NON-NLS-1$

  private CheckboxTreeViewer fViewer;
  private FolderChildCheckboxTree fFolderChildTree;
  private Button fDeselectAll;
  private Button fSelectAll;
  private Button fPreviewButton;
  private Button fFlattenCheck;
  private Button fHideExistingCheck;
  private ExistingBookmarkFilter fExistingFilter = new ExistingBookmarkFilter();
  private Map<URI, IFeed> fLoadedFeedCache = new ConcurrentHashMap<URI, IFeed>();
  private IProgressMonitor fCurrentProgressMonitor;

  /* Remember Current Import Values */
  private Source fCurrentSourceKind;
  private String fCurrentSourceResource;
  private String fCurrentSourceKeywords;
  private boolean fCurrentSourceLocalizedFeedSearch;
  private long fCurrentSourceFileModified;

  /* Imported Entities */
  private List<ILabel> fLabels = Collections.synchronizedList(new ArrayList<ILabel>());
  private List<ISearchFilter> fFilters = Collections.synchronizedList(new ArrayList<ISearchFilter>());
  private List<IPreference> fPreferences = Collections.synchronizedList(new ArrayList<IPreference>());

  /* Filter to Exclude Existing Bookmarks (empty folders are excluded as well) */
  private static class ExistingBookmarkFilter extends ViewerFilter {
    private IBookMarkDAO dao = DynamicDAO.getDAO(IBookMarkDAO.class);
    private Map<IFolderChild, Boolean> cache = new IdentityHashMap<IFolderChild, Boolean>();

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (element instanceof IFolderChild)
        return select((IFolderChild) element);

      return true;
    }

    void clear() {
      cache.clear();
    }

    private boolean select(IFolderChild element) {

      /* Bookmark (exclude if another Bookmark with same Link exists) */
      if (element instanceof IBookMark) {
        IBookMark bm = (IBookMark) element;
        Boolean select = cache.get(bm);
        if (select == null) {
          select = !dao.exists(bm.getFeedLinkReference());
          cache.put(bm, select);
        }

        return select;
      }

      /* Bin (exclude if another Bin with same name Exists at same Location) */
      else if (element instanceof INewsBin) {
        INewsBin bin = (INewsBin) element;
        Boolean select = cache.get(bin);
        if (select == null) {
          select = !CoreUtils.existsNewsBin(bin);
          cache.put(bin, select);
        }

        return select;
      }

      /* Search (exclude if another Search with same name Exists at same Location and same Conditions) */
      else if (element instanceof ISearchMark) {
        ISearchMark searchmark = (ISearchMark) element;
        Boolean select = cache.get(searchmark);
        if (select == null) {
          select = !CoreUtils.existsSearchMark(searchmark);
          cache.put(searchmark, select);
        }

        return select;
      }

      /* Folder */
      else if (element instanceof IFolder) {
        IFolder folder = (IFolder) element;
        Boolean select = cache.get(folder);
        if (select == null) {
          List<IFolderChild> children = folder.getChildren();
          for (IFolderChild child : children) {
            select = select(child);
            if (select)
              break;
          }

          cache.put(folder, select);
        }

        return select != null ? select : false;
      }

      return true;
    }
  }

  ImportElementsPage() {
    super(Messages.ImportElementsPage_CHOOSE_ELEMENTS, Messages.ImportElementsPage_CHOOSE_ELEMENTS, null);
    setMessage(Messages.ImportElementsPage_CHOOSE_ELEMENTS_MESSAGE);
  }

  /* Get Elements to Import */
  List<IFolderChild> getFolderChildsToImport() {
    doImportSource(); //Ensure to be in sync with Source
    return fFolderChildTree.getCheckedElements();
  }

  /* Returns Labels available for Import */
  List<ILabel> getLabelsToImport() {
    doImportSource(); //Ensure to be in sync with Source
    return fLabels;
  }

  /* Returns Filters available for Import */
  List<ISearchFilter> getFiltersToImport() {
    doImportSource(); //Ensure to be in sync with Source
    return fFilters;
  }

  /* Returns the Preferences available for Import */
  List<IPreference> getPreferencesToImport() {
    doImportSource(); //Ensure to be in sync with Source
    return fPreferences;
  }

  /* Returns whether existing bookmarks should be ignored for the Import */
  boolean excludeExisting() {
    return fHideExistingCheck.getSelection();
  }

  /* Check if the Options Page should be shown from the Wizard */
  boolean showOptionsPage() {
    return !fLabels.isEmpty() || !fFilters.isEmpty() || !fPreferences.isEmpty();
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    boolean isWelcome = (getWizard() instanceof WelcomeWizard);

    /* Title Image */
    setImageDescriptor(OwlUI.getImageDescriptor(getWizard() instanceof WelcomeWizard ? "icons/wizban/welcome_wiz.gif" : "icons/wizban/import_wiz.png")); //$NON-NLS-1$ //$NON-NLS-2$

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* Viewer for Folder Child Selection */
    fFolderChildTree = new FolderChildCheckboxTree(container);
    if (!isWelcome)
      ((GridData) fFolderChildTree.getViewer().getTree().getLayoutData()).heightHint = 140;
    fViewer = fFolderChildTree.getViewer();

    /* Open Preview on Doubleclick */
    fViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        openPreview(event.getSelection());
      }
    });

    /* Control Preview Button Enablement */
    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        fPreviewButton.setEnabled(!selection.isEmpty() && ((IStructuredSelection) selection).getFirstElement() instanceof IBookMark);
      }
    });

    /* Filter (exclude existing) */
    fViewer.addFilter(fExistingFilter);

    /* Update Page Complete on Selection */
    fViewer.getTree().addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updatePageComplete();
      }
    });

    /* Select All / Deselect All */
    Composite buttonContainer = new Composite(container, SWT.NONE);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(isWelcome ? 5 : 6, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fSelectAll = new Button(buttonContainer, SWT.PUSH);
    fSelectAll.setText(Messages.ImportElementsPage_SELECT_ALL);
    Dialog.applyDialogFont(fSelectAll);
    setButtonLayoutData(fSelectAll);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setAllChecked(true);
        updatePageComplete();
      }
    });

    fDeselectAll = new Button(buttonContainer, SWT.PUSH);
    fDeselectAll.setText(Messages.ImportElementsPage_DESELECT_ALL);
    Dialog.applyDialogFont(fDeselectAll);
    setButtonLayoutData(fDeselectAll);
    fDeselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setAllChecked(false);
        updatePageComplete();
      }
    });

    if (!Application.IS_MAC) {
      Label sep = new Label(buttonContainer, SWT.SEPARATOR | SWT.VERTICAL);
      sep.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
      ((GridData) sep.getLayoutData()).heightHint = 20;
    }

    fPreviewButton = new Button(buttonContainer, SWT.PUSH);
    fPreviewButton.setText(Messages.ImportElementsPage_PREVIEW);
    fPreviewButton.setEnabled(false);
    fPreviewButton.setToolTipText(Messages.ImportElementsPage_SHOW_PREVIEW);
    Dialog.applyDialogFont(fPreviewButton);
    setButtonLayoutData(fPreviewButton);
    fPreviewButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        openPreview(fViewer.getSelection());
      }
    });

    /* Show as Flat List of News Marks */
    fFlattenCheck = new Button(buttonContainer, SWT.CHECK);
    fFlattenCheck.setText(Messages.ImportElementsPage_FLATTEN);
    Dialog.applyDialogFont(fFlattenCheck);
    setButtonLayoutData(fFlattenCheck);
    ((GridData) fFlattenCheck.getLayoutData()).horizontalAlignment = SWT.END;
    ((GridData) fFlattenCheck.getLayoutData()).horizontalIndent = 30;
    ((GridData) fFlattenCheck.getLayoutData()).grabExcessHorizontalSpace = true;
    fFlattenCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setFlat(fFlattenCheck.getSelection());
        fViewer.expandToLevel(2);
        fFolderChildTree.setAllChecked(true);
      }
    });

    /* Hide Existing News Marks */
    fHideExistingCheck = new Button(buttonContainer, SWT.CHECK);
    fHideExistingCheck.setText(Messages.ImportElementsPage_HIDE_EXISTING);
    fHideExistingCheck.setSelection(true);
    Dialog.applyDialogFont(fHideExistingCheck);
    setButtonLayoutData(fHideExistingCheck);
    ((GridData) fHideExistingCheck.getLayoutData()).exclude = isWelcome;
    fHideExistingCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (fHideExistingCheck.getSelection())
          fViewer.addFilter(fExistingFilter);
        else
          fViewer.removeFilter(fExistingFilter);

        fViewer.expandToLevel(2);
        updateMessage(false);
      }
    });

    /* React on user clicking Cancel if a progress is showing */
    if (getContainer() instanceof CustomWizardDialog) {
      Button cancelButton = ((CustomWizardDialog) getContainer()).getButton(IDialogConstants.CANCEL_ID);
      if (cancelButton != null) {
        cancelButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            onCancel();
          }
        });
      }
    }

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  private void onCancel() {
    if (fCurrentProgressMonitor != null && getShell() != null && !getShell().isDisposed()) {
      IProgressMonitor monitor = fCurrentProgressMonitor;
      monitor.setTaskName(Messages.ImportElementsPage_CANCEL_SEARCH);
      monitor.subTask(""); //$NON-NLS-1$
    }
  }

  private void openPreview(ISelection selection) {
    IStructuredSelection sel = (IStructuredSelection) selection;
    if (!sel.isEmpty()) {
      Object[] elements = sel.toArray();
      int offset = 0;
      for (Object element : elements) {
        if (element instanceof IBookMark) {
          IBookMark bookmark = (IBookMark) element;
          IFeed loadedFeed = fLoadedFeedCache.get(bookmark.getFeedLinkReference().getLink());

          PreviewFeedDialog dialog = new PreviewFeedDialog(getShell(), bookmark, loadedFeed);
          dialog.setBlockOnOpen(false);
          dialog.open();

          if (offset != 0) {
            Point location = dialog.getShell().getLocation();
            dialog.getShell().setLocation(location.x + offset, location.y + offset);
          }

          offset += 20;
        }
      }
    }
  }

  private void updateMessage(boolean clearErrors) {
    List<?> input = (List<?>) fViewer.getInput();
    if (!input.isEmpty() && fViewer.getTree().getItemCount() == 0 && fViewer.getFilters().length > 0)
      setMessage(Messages.ImportElementsPage_HIDDEN_ELEMENTS_INFO, IMessageProvider.INFORMATION);
    else
      setMessage(Messages.ImportElementsPage_CHOOSE_ELEMENTS_MESSAGE);

    if (clearErrors)
      setErrorMessage(null);

    updatePageComplete();
  }

  private void updatePageComplete() {
    boolean complete = (showOptionsPage() || fViewer.getCheckedElements().length > 0);
    setPageComplete(complete);
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible)
      return;

    fViewer.getControl().setFocus();

    /* Load Elements to Import from Source on first time */
    doImportSource();
  }

  @SuppressWarnings("unchecked")
  private void doImportSource() {
    ImportSourcePage importSourcePage = (ImportSourcePage) getPreviousPage();
    final Source source = importSourcePage.getSource();

    /* Return if the Source did not Change */
    if (source == Source.RECOMMENDED && fCurrentSourceKind == Source.RECOMMENDED)
      return;
    else if (source == Source.GOOGLE && fCurrentSourceKind == Source.GOOGLE)
      return;
    else if (source == Source.KEYWORD && fCurrentSourceKind == Source.KEYWORD && importSourcePage.getImportKeywords().equals(fCurrentSourceKeywords) && fCurrentSourceLocalizedFeedSearch == importSourcePage.isLocalizedFeedSearch())
      return;
    else if (source == Source.RESOURCE && fCurrentSourceKind == Source.RESOURCE) {
      String importResource = importSourcePage.getImportResource();

      /* Same URL */
      if (importSourcePage.isRemoteSource() && importResource.equals(fCurrentSourceResource))
        return;

      /* Same Unmodified File */
      else if (importResource.equals(fCurrentSourceResource)) {
        File file = new File(importResource);
        if (file.exists() && file.lastModified() == fCurrentSourceFileModified)
          return;
      }
    }

    /* Remember Source */
    fCurrentSourceKind = source;
    fCurrentSourceResource = importSourcePage.getImportResource();
    final File sourceFile = (source == Source.RESOURCE) ? new File(importSourcePage.getImportResource()) : null;
    fCurrentSourceFileModified = (sourceFile != null && sourceFile.exists()) ? sourceFile.lastModified() : 0;
    fCurrentSourceKeywords = importSourcePage.getImportKeywords();
    fCurrentSourceLocalizedFeedSearch = importSourcePage.isLocalizedFeedSearch();

    /* Reset Fields */
    fLabels.clear();
    fFilters.clear();
    fPreferences.clear();

    /* Reset Messages */
    setErrorMessage(null);
    setMessage(Messages.ImportElementsPage_CHOOSE_ELEMENTS_MESSAGE);

    /* Clear Viewer before loading */
    setImportedElements(Collections.EMPTY_LIST);

    /* Ask for Username and Password if importing from Google */
    if (source == Source.GOOGLE) {
      if (!SyncUtils.hasSyncCredentials() && OwlUI.openSyncLogin(getShell()) != IDialogConstants.OK_ID) {
        setErrorMessage(Messages.ImportElementsPage_MISSING_ACCOUNT);
        setPageComplete(false);
        fCurrentSourceKind = null;
        return;
      }
    }

    /* Import Runnable */
    Runnable runnable = new Runnable() {
      public void run() {
        try {

          /* Import from Supplied File */
          if (source == Source.RESOURCE && sourceFile != null && sourceFile.exists())
            importFromLocalResource(sourceFile);

          /* Import from Supplied Online Resource */
          else if (source == Source.RESOURCE && URIUtils.looksLikeLink(fCurrentSourceResource, false))
            importFromOnlineResource(new URI(URIUtils.ensureProtocol(fCurrentSourceResource)));

          /* Import from Google */
          else if (source == Source.GOOGLE)
            importFromGoogleReader();

          /* Import by Keyword Search */
          else if (source == Source.KEYWORD)
            importFromKeywordSearch(fCurrentSourceKeywords, fCurrentSourceLocalizedFeedSearch);

          /* Import from Default OPML File */
          else if (source == Source.RECOMMENDED)
            importFromLocalResource(getClass().getResourceAsStream("/default_feeds.xml")); //$NON-NLS-1$;
        }

        /* Log and Show any Exception during Import */
        catch (Exception e) {

          /* Offer a Login Dialog in case Google Reader import fails with provided credentials */
          if (e instanceof InvocationTargetException && e.getCause() instanceof AuthenticationRequiredException && source == Source.GOOGLE) {
            if (OwlUI.openSyncLogin(getShell()) == IDialogConstants.OK_ID) {
              try {
                importFromGoogleReader();
                return;
              } catch (InvocationTargetException ex1) {
                e = ex1; //Pass exception on to outer handling
              } catch (InterruptedException ex2) {
                e = ex2; //Pass exception on to outer handling
              }
            }
          }

          /* Handle SyncConnectionException */
          if (e instanceof InvocationTargetException && e.getCause() instanceof SyncConnectionException) {
            String userLink = ((SyncConnectionException) e.getCause()).getUserUrl();
            if (StringUtils.isSet(userLink)) {
              MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
              box.setText(Messages.ImportElementsPage_ERROR_IMPORT_GR);
              String msg = NLS.bind(Messages.ImportElementsPage_ERROR_IMPORT_GR_DETAILS, e.getCause().getMessage());

              box.setMessage(msg);
              if (box.open() == SWT.OK)
                BrowserUtils.openLinkExternal(userLink);
            }
          }

          /* Log Message */
          String logMessage = e.getMessage();
          if (e instanceof InvocationTargetException && e.getCause() != null && StringUtils.isSet(e.getCause().getMessage()))
            logMessage = e.getCause().getMessage();

          if (StringUtils.isSet(logMessage))
            logMessage = NLS.bind(Messages.ImportElementsPage_UNABLE_TO_IMPORT_REASON, logMessage);
          else
            logMessage = Messages.ImportElementsPage_UNABLE_TO_IMPORT;

          /* User Message */
          String userMessage = CoreUtils.toMessage(e);
          if (StringUtils.isSet(userMessage))
            userMessage = NLS.bind(Messages.ImportElementsPage_UNABLE_TO_IMPORT_REASON, userMessage);
          else
            userMessage = Messages.ImportElementsPage_UNABLE_TO_IMPORT;

          Activator.getDefault().logError(logMessage, e);
          setErrorMessage(userMessage);
          setPageComplete(false);

          /* Give a chance to try again */
          fCurrentSourceKind = null;
        }
      }
    };

    /* Perform delayed if potential remote import to give Viewer a chance to show */
    if (importSourcePage.isRemoteSource())
      JobRunner.runInUIThread(50, getShell(), runnable);
    else
      runnable.run();
  }

  /* Import from a Local Input Stream (no progress required) */
  private void importFromLocalResource(InputStream in) throws InterpreterException, ParserException {

    /* Show Folder Childs in Viewer */
    List<? extends IEntity> types = Owl.getInterpreter().importFrom(in);
    setImportedElements(types);
    updateMessage(true);
  }

  /* Import from a Local File */
  private void importFromLocalResource(final File file) throws FileNotFoundException, InvocationTargetException, InterruptedException {
    boolean bruteForce = false;

    /* First Try to Import as OPML */
    try {
      importFromLocalResource(new FileInputStream(file));
    } catch (ParserException e) {
      bruteForce = true;
    } catch (InterpreterException e) {
      bruteForce = true;
    }

    /* Then try to parse links found in the file as Feeds */
    if (bruteForce) {
      setMessage(Messages.ImportElementsPage_INVALID_OPML_WARNING, IMessageProvider.WARNING);

      IRunnableWithProgress runnable = new IRunnableWithProgress() {
        public void run(final IProgressMonitor monitor) throws InvocationTargetException {
          monitor.beginTask(Messages.ImportElementsPage_SEARCHING_FOR_FEEDS, IProgressMonitor.UNKNOWN);
          fCurrentProgressMonitor = monitor;

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
            return;

          /* Read Content */
          Reader reader = null;
          try {
            reader = new FileReader(file);
            String content = StringUtils.readString(reader);

            /* Extract Links from Content */
            List<String> links = new ArrayList<String>();
            if (StringUtils.isSet(content))
              links.addAll(RegExUtils.extractLinksFromText(content, false));

            /* Check Links for valid Feeds */
            importFromLinksBruteforce(links, monitor);
          } catch (Exception e) {
            throw new InvocationTargetException(e);
          } finally {
            fCurrentProgressMonitor = null;
            monitor.done();

            /* Close Reader */
            if (reader != null) {
              try {
                reader.close();
              } catch (IOException e) {
                throw new InvocationTargetException(e);
              }
            }
          }
        }
      };

      /* Run Operation in Background and allow for Cancellation */
      getContainer().run(true, true, runnable);
    }
  }

  private void importFromOnlineResource(final URI link) throws InvocationTargetException, InterruptedException {
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException {
        InputStream in = null;
        boolean canceled = false;
        Exception error = null;
        boolean bruteForce = false;
        try {
          monitor.beginTask(Messages.ImportElementsPage_SEARCHING_FOR_FEEDS, IProgressMonitor.UNKNOWN);
          monitor.subTask(Messages.ImportElementsPage_CONNECTING);
          fCurrentProgressMonitor = monitor;

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
            canceled = true;
            return;
          }

          /* Open Stream */
          in = openStream(link, monitor, INITIAL_CON_TIMEOUT, false, false, null);

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
            canceled = true;
            return;
          }

          /* Try to Import */
          try {
            final List<? extends IEntity> types = Owl.getInterpreter().importFrom(in);

            /* Return on Cancellation */
            if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
              canceled = true;
              return;
            }

            /* Show in UI */
            JobRunner.runInUIThread(getShell(), new Runnable() {
              public void run() {
                setImportedElements(types);
                updateMessage(true);
              }
            });
          }

          /* Error Importing from File - Try Bruteforce then */
          catch (Exception e) {
            error = e;
            bruteForce = true;
          }
        }

        /* Error finding a Handler for the Link - Rethrow */
        catch (Exception e) {
          final boolean showError[] = new boolean[] { true };

          /* Give user a chance to log in */
          if (e instanceof AuthenticationRequiredException && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
            final Shell shell = getShell();
            if (shell != null && !shell.isDisposed()) {
              boolean locked = Controller.getDefault().getLoginDialogLock().tryLock();
              if (locked) {
                try {
                  final AuthenticationRequiredException authEx = (AuthenticationRequiredException) e;
                  JobRunner.runSyncedInUIThread(shell, new Runnable() {
                    public void run() {
                      try {

                        /* Return on Cancelation or shutdown or deletion */
                        if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
                          return;

                        /* Credentials might have been provided meanwhile in another dialog */
                        try {
                          URI normalizedUri = URIUtils.normalizeUri(link, true);
                          if (Owl.getConnectionService().getAuthCredentials(normalizedUri, authEx.getRealm()) != null) {
                            importFromOnlineResource(link);
                            showError[0] = false;
                            return;
                          }
                        } catch (CredentialsException exe) {
                          Activator.getDefault().getLog().log(exe.getStatus());
                        }

                        /* Show Login Dialog */
                        LoginDialog login = new LoginDialog(shell, link, authEx.getRealm());
                        if (login.open() == Window.OK && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
                          importFromOnlineResource(link);
                          showError[0] = false;
                        }
                      } catch (InvocationTargetException e) {
                        /* Ignore - Error will be handled outside already */
                      } catch (InterruptedException e) {
                        /* Ignore - Error will be handled outside already */
                      }
                    }
                  });
                } finally {
                  Controller.getDefault().getLoginDialogLock().unlock();
                }
              }
            }
          }

          /* Rethrow Exception */
          if (showError[0])
            throw new InvocationTargetException(e);
        } finally {

          /* Reset Field in case of error or cancellation */
          if (canceled || error != null)
            fCurrentProgressMonitor = null;

          /* Close Input Stream */
          if (in != null) {
            try {
              if ((canceled || error != null) && in instanceof IAbortable)
                ((IAbortable) in).abort();
              else
                in.close();
            } catch (IOException e) {
              throw new InvocationTargetException(e);
            }
          }
        }

        /* Scan remote Resource for Links and valid Feeds */
        if (bruteForce && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
          try {
            importFromOnlineResourceBruteforce(link, monitor, false, false);
          } catch (Exception e) {
            throw new InvocationTargetException(e);
          } finally {
            fCurrentProgressMonitor = null;
          }
        }

        /* Done */
        monitor.done();
        fCurrentProgressMonitor = null;
      }
    };

    /* Run Operation in Background and allow for Cancellation */
    getContainer().run(true, true, runnable);
  }

  private void importFromGoogleReader() throws InvocationTargetException, InterruptedException {
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException {
        InputStream in = null;
        boolean canceled = false;
        Exception error = null;
        try {
          monitor.beginTask(Messages.ImportElementsPage_IMPORT_GOOGLE_READER, IProgressMonitor.UNKNOWN);
          monitor.subTask(Messages.ImportElementsPage_CONNECTING);
          fCurrentProgressMonitor = monitor;

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
            canceled = true;
            return;
          }

          /* Obtain Google Account Credentials */
          ICredentials credentials = Owl.getConnectionService().getAuthCredentials(URI.create(SyncUtils.GOOGLE_LOGIN_URL), null);
          if (credentials == null) {
            canceled = true;
            return;
          }

          /* Obtain Auth Token */
          String googleAuthToken = SyncUtils.getGoogleAuthToken(credentials.getUsername(), credentials.getPassword(), true, monitor);

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
            canceled = true;
            return;
          }

          /* Open Stream */
          in = openStream(URI.create(SyncUtils.GOOGLE_READER_OPML_URI), monitor, INITIAL_CON_TIMEOUT, false, false, googleAuthToken);

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
            canceled = true;
            return;
          }

          /* Try to Import */
          try {
            final List<IEntity> types = Owl.getInterpreter().importFrom(in);
            enableSynchronization(types);

            /* Return on Cancellation */
            if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
              canceled = true;
              return;
            }

            /* Show in UI */
            JobRunner.runInUIThread(getShell(), new Runnable() {
              public void run() {
                setImportedElements(types);
                updateMessage(true);
              }
            });
          }

          /* Error Importing from File - Try Bruteforce then */
          catch (Exception e) {
            error = e;
          }
        }

        /* Error finding a Handler for the Link - Rethrow */
        catch (Exception e) {
          throw new InvocationTargetException(e);
        } finally {

          /* Reset Field in case of error or cancellation */
          if (canceled || error != null)
            fCurrentProgressMonitor = null;

          /* Close Input Stream */
          if (in != null) {
            try {
              if ((canceled || error != null) && in instanceof IAbortable)
                ((IAbortable) in).abort();
              else
                in.close();
            } catch (IOException e) {
              throw new InvocationTargetException(e);
            }
          }
        }

        /* Done */
        monitor.done();
        fCurrentProgressMonitor = null;
      }
    };

    /* Run Operation in Background and allow for Cancellation */
    getContainer().run(true, true, runnable);
  }

  private void enableSynchronization(List<IEntity> types) throws URISyntaxException {

    /* Convert to Synchronized Feeds */
    for (IEntity entity : types) {
      if (entity instanceof IFolder)
        enableSynchronization((IFolder) entity);
      else if (entity instanceof IBookMark)
        enableSynchronization((IBookMark) entity);
    }

    /* Add Special Google Reader Feeds */
    if (!types.isEmpty() && types.get(0) instanceof IFolder) {
      IModelFactory factory = Owl.getModelFactory();
      IFolder root = (IFolder) types.get(0);

      /* Shared Items */
      FeedLinkReference feedLinkRef = new FeedLinkReference(URI.create(SyncUtils.GOOGLE_READER_SHARED_ITEMS_FEED));
      IBookMark bm = factory.createBookMark(null, root, feedLinkRef, Messages.ImportElementsPage_GR_SHARED_ITEMS);
      setSynchronizationProperties(bm);

      /* Recommended Items */
      feedLinkRef = new FeedLinkReference(URI.create(SyncUtils.GOOGLE_READER_RECOMMENDED_ITEMS_FEED));
      bm = factory.createBookMark(null, root, feedLinkRef, Messages.ImportElementsPage_GR_RECOMMENDED_ITEMS);
      setSynchronizationProperties(bm);

      /* Notes */
      feedLinkRef = new FeedLinkReference(URI.create(SyncUtils.GOOGLE_READER_NOTES_FEED));
      bm = factory.createBookMark(null, root, feedLinkRef, Messages.ImportElementsPage_GR_NOTES);
      setSynchronizationProperties(bm);
    }
  }

  private void enableSynchronization(IFolder folder) throws URISyntaxException {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IFolder)
        enableSynchronization((IFolder) child);
      else if (child instanceof IBookMark)
        enableSynchronization((IBookMark) child);
    }
  }

  private void enableSynchronization(IBookMark bm) throws URISyntaxException {

    /* Convert the Feed Link to enable Synchronization */
    FeedLinkReference feedLinkReference = bm.getFeedLinkReference();
    bm.setFeedLinkReference(new FeedLinkReference(enableSynchronization(feedLinkReference.getLink())));

    /* Add some specific settings that improve the sync experience */
    setSynchronizationProperties(bm);
  }

  private void setSynchronizationProperties(IBookMark bm) {
    IPreferenceScope preferences = Owl.getPreferenceService().getEntityScope(bm);
    preferences.putBoolean(DefaultPreferences.BM_RELOAD_ON_STARTUP, true);
    preferences.putBoolean(DefaultPreferences.NEVER_DEL_LABELED_NEWS_STATE, false);
  }

  private URI enableSynchronization(URI uri) throws URISyntaxException {
    String scheme = URIUtils.HTTPS_SCHEME.equals(uri.getScheme()) ? SyncUtils.READER_HTTPS_SCHEME : SyncUtils.READER_HTTP_SCHEME;
    return new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
  }

  private void importFromKeywordSearch(final String keywords, final boolean isLocalizedSearch) throws Exception {
    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          monitor.beginTask(Messages.ImportElementsPage_SEARCHING_FOR_FEEDS, IProgressMonitor.UNKNOWN);
          monitor.subTask(Messages.ImportElementsPage_CONNECTING);
          fCurrentProgressMonitor = monitor;

          /* Build Link for Keyword-Feed Search */
          String linkVal = Controller.getDefault().toFeedSearchLink(keywords);

          /* Return on Cancellation */
          if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
            return;

          /* Scan remote Resource for Links and valid Feeds */
          importFromOnlineResourceBruteforce(new URI(linkVal), monitor, true, isLocalizedSearch);
        } catch (Exception e) {
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
          fCurrentProgressMonitor = null;
        }
      }
    };

    /* Run Operation in Background and allow for Cancellation */
    getContainer().run(true, true, runnable);
  }

  private void importFromOnlineResourceBruteforce(URI resourceLink, IProgressMonitor monitor, final boolean isKeywordSearch, boolean isLocalizedSearch) throws ConnectionException, IOException {

    /* Read Content */
    Pair<String, URI> result = readContent(resourceLink, isLocalizedSearch, monitor);
    if (result == null)
      return;

    String content = result.getFirst();
    resourceLink = result.getSecond();

    /* Add the used Link at first if the user has typed in a valid feed address */
    List<String> links = new ArrayList<String>();
    if (!isKeywordSearch)
      links.add(resourceLink.toString());

    /* Extract Links from Content */
    if (StringUtils.isSet(content))
      links.addAll(RegExUtils.extractLinksFromText(content, false));

    /* Sort List: First process likely feeds, then others */
    final String resourceLinkValue = resourceLink.toString();
    Collections.sort(links, new Comparator<String>() {
      public int compare(String o1, String o2) {

        /* Check common feed patterns in URL */
        if (URIUtils.looksLikeFeedLink(o1, false))
          return -1;
        else if (URIUtils.looksLikeFeedLink(o2, false))
          return 1;

        /* Check Origin from same Domain */
        if (!isKeywordSearch) {
          if (o1.contains(resourceLinkValue))
            return -1;
          else if (o2.contains(resourceLinkValue))
            return 1;
        }

        return -1;
      }
    });

    /* If this is not a keyword search, add the top most feed entry first */
    if (!isKeywordSearch) {
      URI feed = CoreUtils.findFeed(new BufferedReader(new StringReader(content)), resourceLink, monitor);
      if (feed != null) {
        String feedStr = feed.toString();
        if (links.contains(feedStr))
          links.remove(feedStr);
        links.add(0, feedStr);
      }
    }

    /* Look for Links */
    importFromLinksBruteforce(links, monitor);
  }

  @SuppressWarnings("null")
  private void importFromLinksBruteforce(List<String> links, IProgressMonitor monitor) {

    /* Return on Cancellation */
    if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
      return;

    /* Update Task Information */
    monitor.beginTask(Messages.ImportElementsPage_SEARCHING_FOR_FEEDS, links.size());
    monitor.subTask(Messages.ImportElementsPage_FETCHING_RESULTS);

    /* A Root to add Found Bookmarks into */
    final IFolder defaultRootFolder = Owl.getModelFactory().createFolder(null, null, Messages.ImportElementsPage_BOOKMARKS);
    defaultRootFolder.setProperty(ITypeImporter.TEMPORARY_FOLDER, true);

    /* For Each Link of the Queue - try to interpret as Feed */
    int counter = 0;
    final List<String> foundBookMarkNames = new ArrayList<String>();
    IBookMarkDAO dao = DynamicDAO.getDAO(IBookMarkDAO.class);
    for (String feedLinkVal : links) {
      monitor.worked(1);

      InputStream in = null;
      boolean canceled = false;
      Exception error = null;
      try {
        URI feedLink = new URI(feedLinkVal);

        /* Report Progress Back To User */
        if (counter == 1)
          monitor.subTask(Messages.ImportElementsPage_SINGLE_RESULT);
        else if (counter > 1)
          monitor.subTask(NLS.bind(Messages.ImportElementsPage_N_RESULTS, counter));

        /* Ignore if already present in Subscriptions List (ignoring trailing slashes) */
        if (dao.exists(new FeedLinkReference(feedLink)))
          continue;
        else if (feedLinkVal.endsWith("/") && dao.exists(new FeedLinkReference(new URI(feedLinkVal.substring(0, feedLinkVal.length() - 1))))) //$NON-NLS-1$
          continue;
        else if (!feedLinkVal.endsWith("/") && dao.exists(new FeedLinkReference(new URI(feedLinkVal + "/")))) //$NON-NLS-1$ //$NON-NLS-2$
          continue;

        /* Return on Cancellation */
        if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
          break;

        /* Open Stream to potential Feed */
        in = openStream(feedLink, monitor, FEED_CON_TIMEOUT, false, false, null);

        /* Return on Cancellation */
        if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
          canceled = true;
          break;
        }

        /* Try to interpret as Feed */
        IFeed feed = Owl.getModelFactory().createFeed(null, feedLink);
        Owl.getInterpreter().interpret(in, feed, null);
        fLoadedFeedCache.put(feedLink, feed);

        /* Return on Cancellation */
        if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
          canceled = true;
          break;
        }

        /* Add as Result if Feed contains News */
        if (!feed.getNews().isEmpty() && StringUtils.isSet(feed.getTitle())) {
          String title = feed.getTitle();
          boolean sameTitleExists = foundBookMarkNames.contains(title);
          if (sameTitleExists && StringUtils.isSet(feed.getFormat()))
            title = NLS.bind(Messages.ImportElementsPage_FEED_TITLE, title, feed.getFormat());

          final IBookMark bookmark = Owl.getModelFactory().createBookMark(null, defaultRootFolder, new FeedLinkReference(feedLink), title);
          foundBookMarkNames.add(bookmark.getName());
          counter++;

          if (StringUtils.isSet(feed.getDescription()))
            bookmark.setProperty(ITypeImporter.DESCRIPTION_KEY, feed.getDescription());

          if (feed.getHomepage() != null)
            bookmark.setProperty(ITypeImporter.HOMEPAGE_KEY, feed.getHomepage());

          /* Directly show in Viewer */
          JobRunner.runInUIThread(getShell(), new Runnable() {
            public void run() {
              addImportedElement(bookmark);
            }
          });
        }
      }

      /* Ignore Errors (likely not a Feed then) */
      catch (Exception e) {
        error = e;
      }

      /* Close Stream */
      finally {

        /* Close Input Stream */
        if (in != null) {
          try {
            if ((canceled || error != null) && in instanceof IAbortable)
              ((IAbortable) in).abort();
            else
              in.close();
          } catch (IOException e) {
            /* Ignore Silently */
          }
        }
      }
    }

    /* Inform if no feeds have been found */
    if (counter == 0) {
      JobRunner.runInUIThread(getShell(), new Runnable() {
        public void run() {
          setMessage(Messages.ImportElementsPage_NO_FEEDS_FOUND, IMessageProvider.INFORMATION);
        }
      });
    }
  }

  private Pair<String, URI> readContent(URI link, boolean isLocalizedSearch, IProgressMonitor monitor) throws ConnectionException, IOException {
    InputStream in = null;
    try {

      /* Return on Cancellation */
      if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
        return null;

      /* Open Stream */
      in = openStream(link, monitor, INITIAL_CON_TIMEOUT, true, isLocalizedSearch, null);

      /* Return on Cancellation */
      if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
        return null;

      /* Read Content */
      String content = StringUtils.readString(new InputStreamReader(in));

      /* Return actual URI that was connected to (supporting redirects) */
      if (in instanceof HttpConnectionInputStream)
        return Pair.create(content, ((HttpConnectionInputStream) in).getLink());

      /* Otherwise just use input URI */
      return Pair.create(content, link);
    } finally {
      if (in instanceof IAbortable)
        ((IAbortable) in).abort(); //Abort the stream to avoid downloading the full content if error or cancelled
      else if (in != null)
        in.close();
    }
  }

  private InputStream openStream(URI link, IProgressMonitor monitor, int timeout, boolean setAcceptLanguage, boolean isLocalized, String authToken) throws ConnectionException {
    IProtocolHandler handler = Owl.getConnectionService().getHandler(link);

    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, timeout);

    /* Set Authorization Header if required */
    if (StringUtils.isSet(authToken)) {
      Map<String, String> headers = new HashMap<String, String>();
      headers.put("Authorization", SyncUtils.getGoogleAuthorizationHeader(authToken)); //$NON-NLS-1$
      properties.put(IConnectionPropertyConstants.HEADERS, headers);
    }

    /* Set the Accept-Language Header */
    if (setAcceptLanguage) {
      StringBuilder languageHeader = new StringBuilder();
      String clientLanguage = Locale.getDefault().getLanguage();

      /* Set Both English and Client Locale */
      if (!isLocalized) {
        languageHeader.append(DEFAULT_LANGUAGE);
        if (StringUtils.isSet(clientLanguage) && !DEFAULT_LANGUAGE.equals(clientLanguage))
          languageHeader.append(",").append(clientLanguage); //$NON-NLS-1$
      }

      /* Only set Client Locale */
      else {
        if (StringUtils.isSet(clientLanguage))
          languageHeader.append(clientLanguage);
        else
          languageHeader.append(DEFAULT_LANGUAGE);
      }

      properties.put(IConnectionPropertyConstants.ACCEPT_LANGUAGE, languageHeader.toString());
    }

    return handler.openStream(link, monitor, properties);
  }

  /* Updates Caches and Shows Elements */
  private void setImportedElements(List<? extends IEntity> types) {
    List<IFolderChild> folderChilds = new ArrayList<IFolderChild>();
    for (IEntity type : types) {
      if (type instanceof IFolderChild)
        folderChilds.add((IFolderChild) type);
      else if (type instanceof ILabel)
        fLabels.add((ILabel) type);
      else if (type instanceof ISearchFilter)
        fFilters.add((ISearchFilter) type);
      else if (type instanceof IPreference)
        fPreferences.add((IPreference) type);
    }

    /* Re-Add Filter if necessary */
    if (!fHideExistingCheck.getSelection()) {
      fHideExistingCheck.setSelection(true);
      fViewer.addFilter(fExistingFilter);
    }

    /* Apply as Input */
    fViewer.setInput(folderChilds);
    OwlUI.setAllChecked(fViewer.getTree(), true);
    fExistingFilter.clear();
  }

  /* Adds a IFolderChild to the Viewer and updates caches */
  @SuppressWarnings("unchecked")
  private void addImportedElement(IFolderChild child) {
    Object input = fViewer.getInput();
    ((List) input).add(child);
    fViewer.add(input, child);
    fViewer.setChecked(child, true);
    fViewer.reveal(child);
  }
}