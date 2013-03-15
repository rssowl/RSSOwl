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

package org.rssowl.ui.internal.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressConstants;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.AuthenticationRequiredException;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.HttpConnectionInputStream;
import org.rssowl.core.connection.IAbortable;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StreamGobbler;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.LoginDialog;
import org.rssowl.ui.internal.util.DownloadJobQueue;
import org.rssowl.ui.internal.util.JobRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service to download files in a {@link DownloadJobQueue} with proper
 * progress reporting.
 *
 * @author bpasero
 */
public class DownloadService {

  /* Max. number of concurrent Jobs for downloading files */
  private static final int MAX_CONCURRENT_DOWNLOAD_JOBS = 3;

  /* Connection Timeouts in MS */
  private static final int DEFAULT_CON_TIMEOUT = 30000;

  /* Default Length for Download Tasks */
  private static final int DEFAULT_TASK_LENGTH = 1000000;

  /* Default Progress for Download Tasks */
  private static final int DEFAULT_WORKED = 200;

  /* List of invalid characters for a file name */
  private static final List<Character> INVALID_FILENAME_CHAR = Arrays.asList('\\', '/', ':', '?', '|', '*', '<', '>', '\"');

  /* A simple date format used to produce unique download names if necessary */
  private static final SimpleDateFormat DOWNLOAD_FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US); //$NON-NLS-1$

  /* A suffix for download parts */
  private static final String DOWNLOAD_PART_SUFFIX = ".part"; //$NON-NLS-1$

  /* Filename portion of content disposition header */
  private static final String CONTENT_DISPOSITION_FILENAME = "filename="; //$NON-NLS-1$

  /* Some Content Types that identify a HTML content */
  private static final List<String> HTML_CONTENT_TYPES = Arrays.asList(new String[] { "text/html", "application/xhtml+xml" }); //$NON-NLS-1$ //$NON-NLS-2$

  private DownloadJobQueue fDownloadQueue;
  private Map<OutputStream, OutputStream> fOutputStreamMap = new ConcurrentHashMap<OutputStream, OutputStream>();
  private IPreferenceScope fPreferences = Owl.getPreferenceService().getGlobalScope();

  /* Task for a Download */
  private class AttachmentDownloadTask extends DownloadJobQueue.DownloadTask {
    private final DownloadRequest fRequest;

    private AttachmentDownloadTask(DownloadRequest request) {
      fRequest = request;
    }

    @Override
    public IStatus run(Job job, IProgressMonitor monitor) {
      return internalDownload(fRequest, job, monitor);
    }

    public String getName() {
      return NLS.bind(Messages.DownloadService_DOWNLOADING_N, fRequest.getLink().toString());
    }

    public Priority getPriority() {
      return Priority.DEFAULT;
    }

    @Override
    public int hashCode() {
      return fRequest.getLink().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;

      if (obj == null)
        return false;

      if (getClass() != obj.getClass())
        return false;

      final AttachmentDownloadTask other = (AttachmentDownloadTask) obj;
      return fRequest.getLink().equals(other.fRequest.getLink());
    }
  }

  /* A download request to process by the service */
  public static class DownloadRequest {
    private final URI fLink;
    private final File fTargetFolder;
    private final IAttachment fAttachment;
    private final INews fNews;
    private final boolean fIsUserInitiated;
    private final String fUserChosenFilename;

    /* Download an Attachment */
    public static DownloadRequest createAttachmentDownloadRequest(IAttachment attachment, URI link, File targetFolder, boolean isUserInitiated, String userChosenFilename) {
      return new DownloadRequest(link, targetFolder, attachment, null, isUserInitiated, userChosenFilename);
    }

    /* Download the content that is behind the News Link if any */
    public static DownloadRequest createNewsDownloadRequest(INews news, URI link, File targetFolder) {
      return new DownloadRequest(link, targetFolder, null, news, false, null);
    }

    private DownloadRequest(URI link, File targetFolder, IAttachment attachment, INews news, boolean isUserInitiated, String userChosenFilename) {
      fLink = link;
      fTargetFolder = targetFolder;
      fAttachment = attachment;
      fNews = news;
      fIsUserInitiated = isUserInitiated;
      fUserChosenFilename = userChosenFilename;
    }

    URI getLink() {
      return fLink;
    }

    File getTargetFolder() {
      return fTargetFolder;
    }

    IAttachment getAttachment() {
      return fAttachment;
    }

    INews getNews() {
      return fNews;
    }

    String getUserChosenFilename() {
      return fUserChosenFilename;
    }

    boolean isUserInitiated() {
      return fIsUserInitiated;
    }

    String getType() {
      return fAttachment != null ? fAttachment.getType() : null;
    }

    int getLength() {
      return fAttachment != null ? fAttachment.getLength() : 0;
    }

    boolean isAttachmentDownloadRequest() {
      return fAttachment != null;
    }

    boolean isNewsDownloadRequest() {
      return fNews != null;
    }
  }

  /** Default Constructor to create a Download Queue */
  public DownloadService() {
    fDownloadQueue = new DownloadJobQueue(Messages.DownloadService_DOWNLOADING_TITLE, MAX_CONCURRENT_DOWNLOAD_JOBS, Integer.MAX_VALUE);
  }

  /**
   * @param download the requested file to download from the service.
   */
  public void download(DownloadRequest download) {
    AttachmentDownloadTask task = new AttachmentDownloadTask(download);
    if (InternalOwl.TESTING) //Support to test the download service from JUnit
      internalDownload(download, new StreamGobbler(null), new NullProgressMonitor());
    else if (!fDownloadQueue.isQueued(task))
      fDownloadQueue.schedule(task);
  }

  private IStatus internalDownload(final DownloadRequest request, Job job, final IProgressMonitor monitor) {

    /* Do not download in Offline Mode */
    if (Controller.getDefault().isOffline())
      return Status.OK_STATUS;

    /* Find Download Name */
    String downloadFileName;
    if (StringUtils.isSet(request.getUserChosenFilename()))
      downloadFileName = request.getUserChosenFilename();
    else
      downloadFileName = URIUtils.getFile(request.fLink, OwlUI.getExtensionForMime(request.getType()));

    job.setProperty(IProgressConstants.ICON_PROPERTY, OwlUI.getAttachmentImage(downloadFileName, request.getType()));

    int bytesConsumed = 0;
    try {
      IProtocolHandler handler = Owl.getConnectionService().getHandler(request.getLink());
      if (handler != null) {
        Map<Object, Object> properties = new HashMap<Object, Object>();
        properties.put(IConnectionPropertyConstants.CON_TIMEOUT, DEFAULT_CON_TIMEOUT);

        /* Check for Cancellation and Shutdown */
        if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
          return Status.CANCEL_STATUS;

        /* Initialize Fields */
        long bytesPerSecond = 0;
        long lastTaskNameUpdate = 0;
        long lastBytesCheck = 0;
        int length = request.getLength();
        byte[] buffer = new byte[8192];

        /* First Download to a temporary File */
        int contentLength = length;
        InputStream in = null;
        FileOutputStream out = null;
        File partFile = null;
        boolean canceled = false;
        Exception error = null;
        try {

          /* Open Stream */
          in = handler.openStream(request.getLink(), monitor, properties);

          /* Obtain real Content Length from Stream if available */
          if (in instanceof HttpConnectionInputStream) {
            int len = ((HttpConnectionInputStream) in).getContentLength();
            if (len > 0)
              contentLength = len;
          }

          /* If we download a News Link, now is a good time to check for the Content Type making any sense */
          if (request.isNewsDownloadRequest() && in instanceof HttpConnectionInputStream) {
            String contentType = ((HttpConnectionInputStream) in).getContentType();
            if (isTextualContent(contentType)) {
              canceled = true;
              return Status.CANCEL_STATUS;
            }
          }

          /* Begin Task (now because the real content length is known at this point) */
          job.setName(NLS.bind(Messages.DownloadService_DOWNLOADING, downloadFileName));
          monitor.beginTask(formatTask(bytesConsumed, contentLength, -1), contentLength > 0 ? contentLength : DEFAULT_TASK_LENGTH);

          /* Create tmp part File */
          partFile = getPartFile(request.getTargetFolder(), downloadFileName);

          /* Maybe the chosen directory is not writeable */
          if (partFile == null) {
            canceled = true;
            return Status.CANCEL_STATUS;
          }

          /* Keep Outputstream for later */
          out = new FileOutputStream(partFile);
          fOutputStreamMap.put(out, out);

          /* Download */
          while (true) {

            /* Check for Cancellation and Shutdown */
            if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
              canceled = true;
              return Status.CANCEL_STATUS;
            }

            /* Read from Stream */
            int read = in.read(buffer);
            bytesConsumed += read;
            if (read == -1)
              break;

            /* Write to File */
            out.write(buffer, 0, read);

            /* Update Task Name once per Second */
            long now = System.currentTimeMillis();
            long timeDiff = (now - lastTaskNameUpdate);
            if (timeDiff > 1000) {
              long bytesDiff = bytesConsumed - lastBytesCheck;
              bytesPerSecond = bytesDiff / (timeDiff / 1000);
              monitor.setTaskName(formatTask(bytesConsumed, contentLength, (int) bytesPerSecond));
              lastTaskNameUpdate = now;
              lastBytesCheck = bytesConsumed;
            }

            /* Report accurate progress */
            if (request.getLength() > 0)
              monitor.worked(read);

            /* Report calculated progress if possible */
            else if (contentLength > 0) {
              float relWorked = read / (float) contentLength;
              monitor.worked((int) (relWorked * DEFAULT_TASK_LENGTH));
            }

            /* Use a generic Progress Value */
            else
              monitor.worked(DEFAULT_WORKED);
          }
        } catch (FileNotFoundException e) {
          error = e;
          return Activator.getDefault().createErrorStatus(e.getMessage(), e);
        } catch (IOException e) {
          error = e;
          return Activator.getDefault().createErrorStatus(e.getMessage(), e);
        } catch (ConnectionException e) {
          final boolean showError[] = new boolean[] { true };

          /* Offer a Login Dialog if Authentication is Required */
          if (request.isUserInitiated() && e instanceof AuthenticationRequiredException && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
            final Shell shell = OwlUI.getActiveShell();
            if (shell != null && !shell.isDisposed()) {
              Controller.getDefault().getLoginDialogLock().lock();
              try {
                final AuthenticationRequiredException authEx = (AuthenticationRequiredException) e;
                JobRunner.runSyncedInUIThread(shell, new Runnable() {
                  public void run() {

                    /* Return on Cancelation or shutdown or deletion */
                    if (monitor.isCanceled() || Controller.getDefault().isShuttingDown())
                      return;

                    /* Credentials might have been provided meanwhile in another dialog */
                    try {
                      URI normalizedUri = URIUtils.normalizeUri(request.getLink(), true);
                      if (Owl.getConnectionService().getAuthCredentials(normalizedUri, authEx.getRealm()) != null) {
                        fDownloadQueue.schedule(new AttachmentDownloadTask(request));
                        showError[0] = false;
                        return;
                      }
                    } catch (CredentialsException exe) {
                      Activator.getDefault().getLog().log(exe.getStatus());
                    }

                    /* Show Login Dialog */
                    LoginDialog login = new LoginDialog(shell, request.getLink(), authEx.getRealm());
                    if (login.open() == Window.OK && !monitor.isCanceled() && !Controller.getDefault().isShuttingDown()) {
                      fDownloadQueue.schedule(new AttachmentDownloadTask(request));
                      showError[0] = false;
                    }
                  }
                });
              } finally {
                Controller.getDefault().getLoginDialogLock().unlock();
              }
            }
          }

          /* User has not Provided Login Credentials or any other error */
          if (showError[0]) {
            error = e;
            return Activator.getDefault().createErrorStatus(e.getMessage(), e);
          }

          /* User has Provided Login Credentials - cancel this Task */
          monitor.setCanceled(true);
          canceled = true;
          return Status.CANCEL_STATUS;
        } finally {
          monitor.done();

          /* Indicate Error Message if any and offer Action to download again */
          if (error != null) {
            String errorMessage = CoreUtils.toMessage(error);
            if (StringUtils.isSet(errorMessage))
              job.setName(NLS.bind(Messages.DownloadService_ERROR_DOWNLOADING_N, downloadFileName, errorMessage));
            else
              job.setName(NLS.bind(Messages.DownloadService_ERROR_DOWNLOADING, downloadFileName));

            job.setProperty(IProgressConstants.ICON_PROPERTY, OwlUI.ERROR);

            DownloadRequest redownloadRequest = new DownloadRequest(request.getLink(), request.getTargetFolder(), request.getAttachment(), request.getNews(), true, request.getUserChosenFilename());
            job.setProperty(IProgressConstants.ACTION_PROPERTY, getRedownloadAction(new AttachmentDownloadTask(redownloadRequest)));
            monitor.setTaskName(Messages.DownloadService_TRY_AGAIN);
          }

          /* Close Output Stream */
          if (out != null) {
            try {
              out.close();
              fOutputStreamMap.remove(out);
              if (partFile != null && (canceled || error != null))
                partFile.delete();
            } catch (IOException e) {
              return Activator.getDefault().createErrorStatus(e.getMessage(), e);
            }
          }

          /* Close Input Stream */
          if (in != null) {
            try {
              if ((canceled || error != null) && in instanceof IAbortable)
                ((IAbortable) in).abort();
              else
                in.close();
            } catch (IOException e) {
              return Activator.getDefault().createErrorStatus(e.getMessage(), e);
            }
          }
        }

        /* Check for Cancellation and Shutdown */
        if (monitor.isCanceled() || Controller.getDefault().isShuttingDown()) {
          if (partFile != null)
            partFile.delete();
          return Status.CANCEL_STATUS;
        }

        /* Something was not working right if the part file is null */
        if (partFile == null)
          return Status.CANCEL_STATUS;

        /* Now copy over the part file to the actual file in an atomic operation */
        String finalFileName;
        if (StringUtils.isSet(request.getUserChosenFilename()))
          finalFileName = request.getUserChosenFilename();
        else
          finalFileName = getDownloadFileName(request, in);

        File downloadFile = new File(request.getTargetFolder(), finalFileName);
        if (!partFile.renameTo(downloadFile)) {
          downloadFile.delete();
          partFile.renameTo(downloadFile);
        }

        /* Offer Action to Open Attachment by keeping Job in Viewer if set */
        if (!fPreferences.getBoolean(DefaultPreferences.HIDE_COMPLETED_DOWNLOADS)) {
          job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
          job.setProperty(IProgressConstants.ACTION_PROPERTY, getOpenAction(downloadFile));
        }
      }
    } catch (ConnectionException e) {
      return Activator.getDefault().createErrorStatus(e.getMessage(), e);
    }

    /* Update Job Name */
    if (bytesConsumed > 0)
      job.setName(NLS.bind(Messages.DownloadService_N_OF_M, downloadFileName, OwlUI.getSize(bytesConsumed)));
    else
      job.setName(downloadFileName);

    /* The Label of the Status is used as Link for Action */
    return new Status(IStatus.OK, Activator.PLUGIN_ID, Messages.DownloadService_OPEN_FOLDER);
  }

  private boolean isTextualContent(String contentType) {
    if (StringUtils.isSet(contentType)) {
      for (String htmlContentType : HTML_CONTENT_TYPES) {
        if (contentType.contains(htmlContentType))
          return true;
      }
    }

    return false;
  }

  private File getPartFile(File targetFolder, String name) throws IOException {
    name = toValidFileName(name);
    File partFile = null;

    /* Up to 10 attempts to create a non existing file */
    for (int i = 0; i < 10; i++) {
      if (i == 0)
        partFile = new File(targetFolder, name + DOWNLOAD_PART_SUFFIX);
      else
        partFile = new File(targetFolder, name + "_" + i + DOWNLOAD_PART_SUFFIX); //$NON-NLS-1$

      if (!partFile.exists() && partFile.createNewFile())
        break;
    }

    if (partFile != null)
      partFile.deleteOnExit();

    return partFile;
  }

  private String toValidFileName(String fileName) {
    for (Character invalidChar : INVALID_FILENAME_CHAR) {
      fileName = fileName.replace(invalidChar, '_');
    }

    return fileName;
  }

  private String getDownloadFileName(DownloadRequest request, InputStream inS) {
    String downloadFileName = null;

    /* Try to read out the Content-Disposition header first */
    if (inS instanceof HttpConnectionInputStream && StringUtils.isSet(((HttpConnectionInputStream) inS).getContentDisposition())) {
      String contentDisposition = ((HttpConnectionInputStream) inS).getContentDisposition();

      int indexOfFileName = contentDisposition.indexOf(CONTENT_DISPOSITION_FILENAME);
      if (indexOfFileName != -1) {
        contentDisposition = contentDisposition.substring(indexOfFileName + CONTENT_DISPOSITION_FILENAME.length());
        contentDisposition = StringUtils.replaceAll(contentDisposition, "\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
        downloadFileName = contentDisposition.trim();
      }
    }

    /* Otherwise retrieve a good name from the URI */
    if (!StringUtils.isSet(downloadFileName))
      downloadFileName = URIUtils.getFile(request.getLink(), OwlUI.getExtensionForMime(request.getType()));

    /* Make sure the file name is valid for the OS */
    downloadFileName = toValidFileName(downloadFileName);

    /* If the file already exists, add the news date as suffix to the file name */
    File proposedFile = new File(request.getTargetFolder(), downloadFileName);
    if (proposedFile.exists()) {
      INews news = request.getNews();
      if (news == null)
        news = request.getAttachment().getNews();

      Date date = DateUtils.getRecentDate(news);
      if (date != null) {
        String fileNameSuffix = DOWNLOAD_FILE_DATE_FORMAT.format(date);

        int index = downloadFileName.lastIndexOf('.');
        if (index == -1)
          downloadFileName += "_" + fileNameSuffix; //$NON-NLS-1$
        else {
          String pre = downloadFileName.substring(0, index);
          String post = downloadFileName.substring(index);

          downloadFileName = pre + "_" + fileNameSuffix + post; //$NON-NLS-1$
        }
      }
    }

    return downloadFileName;
  }

  private String formatTask(int bytesConsumed, int totalBytes, int bytesPerSecond) {
    StringBuilder str = new StringBuilder();

    /* "Time Remaining" */
    int bytesToGo = totalBytes - bytesConsumed;
    if (bytesToGo > 0 && bytesPerSecond > 0) {
      int secondsRemaining = bytesToGo / bytesPerSecond;
      String period = OwlUI.getPeriod(secondsRemaining);
      if (period != null)
        str.append(NLS.bind(Messages.DownloadService_BYTES_REMAINING, period)).append(" - "); //$NON-NLS-1$
    }

    /* "X MB of Y MB "*/
    String consumed = OwlUI.getSize(bytesConsumed);
    if (consumed == null)
      consumed = "0"; //$NON-NLS-1$

    String total = OwlUI.getSize(totalBytes);
    if (total != null)
      str.append(NLS.bind(Messages.DownloadService_BYTES_OF_BYTES, consumed, total));
    else
      str.append(NLS.bind(Messages.DownloadService_BYTES_OF_UNKNOWN, consumed));

    /* "(X MB/sec)" */
    if (bytesPerSecond > 0) {
      str.append(" "); //$NON-NLS-1$
      str.append(NLS.bind(Messages.DownloadService_BYTES_PER_SECOND, OwlUI.getSize(bytesPerSecond)));
    }

    return str.toString();
  }

  private IAction getOpenAction(final File downloadFile) {
    return new Action(Messages.DownloadService_OPEN_FOLDER) {
      @Override
      public void run() {
        Program.launch(downloadFile.getParent());
      }
    };
  }

  private IAction getRedownloadAction(final AttachmentDownloadTask task) {
    return new Action(Messages.DownloadService_RE_DOWNLOAD) {
      @Override
      public void run() {
        fDownloadQueue.schedule(task);
      }
    };
  }

  /**
   * Stops this Service and cancels all pending downloads.
   */
  public void stopService() {
    fDownloadQueue.cancel(false);

    /* Need to properly close yet opened Streams */
    Set<OutputStream> openStreams = fOutputStreamMap.keySet();
    for (OutputStream out : openStreams) {
      try {
        out.close();
      } catch (IOException e) {
        /* Ignore */}
    }
  }

  /**
   * @return <code>true</code> if there are active download jobs running and
   * <code>false</code> otherwise.
   */
  public boolean isActive() {
    return fDownloadQueue.isWorking();
  }
}