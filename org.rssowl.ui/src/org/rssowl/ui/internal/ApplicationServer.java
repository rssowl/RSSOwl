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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.reference.ModelReference;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.reference.SearchMarkReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.FolderNewsMark.FolderNewsMarkReference;
import org.rssowl.ui.internal.editors.feed.NewsBrowserLabelProvider;
import org.rssowl.ui.internal.editors.feed.NewsBrowserViewer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The <code>NewsServer</code> is a Singleton that serves HTML for a request of
 * News. A Browser can navigate to a local URL with Port 8795 and use some
 * special parameters to request either a List of News or complete Feeds.
 * <p>
 * TODO As more and more stuff is handled by this server, it should be
 * considered to make it extensible by allowing to register handlers for certain
 * operations.
 * </p>
 *
 * @author bpasero
 */
public class ApplicationServer {

  /* The Singleton Instance */
  private static ApplicationServer fgInstance = new ApplicationServer();

  /* Local URL Default Values */
  static final String PROTOCOL = "http"; //$NON-NLS-1$
  public static final String DEFAULT_LOCALHOST = "127.0.0.1"; //$NON-NLS-1$
  @SuppressWarnings("all")
  static final int DEFAULT_SOCKET_PORT = Application.IS_ECLIPSE ? 8775 : 8795;

  /* Local URL Parts */
  static String LOCALHOST = DEFAULT_LOCALHOST;
  static int SOCKET_PORT = DEFAULT_SOCKET_PORT;

  /* Handshake Message */
  static final String STARTUP_HANDSHAKE = "org.rssowl.ui.internal.StartupHandshake"; //$NON-NLS-1$

  /* DWord controlling the startup-handshake */
  private static final String MULTI_INSTANCE_PROPERTY = "multiInstance"; //$NON-NLS-1$

  /* DWord controlling the localhost value */
  private static final String LOCALHOST_PROPERTY = "localhost"; //$NON-NLS-1$

  /* DWord controlling the port value */
  private static final String PORT_PROPERTY = "port"; //$NON-NLS-1$

  /* Identifies the Viewer providing the Content */
  private static final String ID = "id="; //$NON-NLS-1$

  /* Used after all HTTP-Headers */
  private static final String CRLF = "\r\n"; //$NON-NLS-1$

  /* Registry of known Viewer */
  private static Map<String, ContentViewer> fRegistry = new ConcurrentHashMap<String, ContentViewer>();

  /* Supported Operations */
  private static final String OP_DISPLAY_FOLDER = "displayFolder="; //$NON-NLS-1$
  private static final String OP_DISPLAY_BOOKMARK = "displayBookMark="; //$NON-NLS-1$
  private static final String OP_DISPLAY_NEWSBIN = "displayNewsBin="; //$NON-NLS-1$
  private static final String OP_DISPLAY_SEARCHMARK = "displaySearchMark="; //$NON-NLS-1$
  private static final String OP_DISPLAY_NEWS = "displayNews="; //$NON-NLS-1$
  private static final String OP_RESOURCE = "resource="; //$NON-NLS-1$

  /* Windows only: Mark of the Web */
  private static final String IE_MOTW = "<!-- saved from url=(0014)about:internet -->"; //$NON-NLS-1$

  /* RFC 1123 Date Format for the respond header */
  private static final DateFormat RFC_1123_DATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH); //$NON-NLS-1$

  /* Interface used to handle a startup-handshake */
  static interface HandshakeHandler {

    /**
     * Handler for the hand-shake done on startup in case the application server
     * was found already running by another RSSOwl process.
     *
     * @param token A message to pass via hand-shake.
     */
    void handle(String token);
  }

  private ServerSocket fSocket;
  private Job fServerJob;
  private int fPort;
  private HandshakeHandler fHandshakeHandler;

  /**
   * Returns the singleton instance of the ApplicationServer.
   *
   * @return the singleton instance of the ApplicationServer.
   */
  public static ApplicationServer getDefault() {
    return fgInstance;
  }

  /**
   * Attempts to start the server. Will throw an IOException in case of a
   * problem.
   *
   * @throws IOException in case of a problem starting the server.
   * @throws UnknownHostException in case the host is unknown.
   * @throws BindException in case of a failure binding the server to a port.
   */
  public void startup() throws IOException {

    /* Server already running */
    if (isRunning())
      return;

    /* Determine Localhost Value */
    String localhostProperty = System.getProperty(LOCALHOST_PROPERTY);
    if (localhostProperty != null && localhostProperty.length() > 0)
      LOCALHOST = localhostProperty;

    /* Determine Port Value */
    String portProperty = System.getProperty(PORT_PROPERTY);
    if (portProperty != null && portProperty.length() > 0) {
      try {
        SOCKET_PORT = Integer.parseInt(portProperty);
      } catch (NumberFormatException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    /* Server not yet running */
    boolean usePortRange = Application.IS_ECLIPSE || System.getProperty(MULTI_INSTANCE_PROPERTY) != null;
    fSocket = createServerSocket(usePortRange);
    if (fSocket != null)
      listen();
  }

  /** Stop the Application Server */
  public void shutdown() {
    fServerJob.cancel();
    try {
      if (fSocket != null)
        fSocket.close();
    } catch (IOException e) {
      if (Activator.getDefault() != null)
        Activator.getDefault().logError(e.getMessage(), e);
    }
  }

  /**
   * Check if the server is running or not.
   *
   * @return <code>TRUE</code> in case the server is running and
   * <code>FALSE</code> otherwise.
   */
  public boolean isRunning() {
    return fSocket != null;
  }

  /* Registers the Handler for Hand-Shaking on startup */
  void setHandshakeHandler(HandshakeHandler handler) {
    fHandshakeHandler = handler;
  }

  /* Attempt to create Server-Socket with retry-option */
  private ServerSocket createServerSocket(boolean usePortRange) throws IOException {

    /* Ports to try */
    List<Integer> ports = new ArrayList<Integer>();
    ports.add(SOCKET_PORT);

    /* Try up to 10 different ports if set */
    if (usePortRange) {
      for (int i = 1; i < 10; i++)
        ports.add(SOCKET_PORT + i);
    }

    /* Attempt to open Port */
    for (int i = 0; i < ports.size(); i++) {
      try {
        int port = ports.get(i);
        fPort = port;
        return new ServerSocket(fPort, 50, InetAddress.getByName(LOCALHOST));
      } catch (UnknownHostException e) {
        throw e;
      } catch (BindException e) {
        if (i == (ports.size() - 1))
          throw e;
      }
    }

    return null;
  }

  /**
   * Returns <code>TRUE</code> if the given URL is a local NewsServer URL.
   *
   * @param url The URL to check for being a NewsServer URL.
   * @return <code>TRUE</code> if the given URL is a local NewsServer URL.
   */
  public boolean isNewsServerUrl(String url) {
    return url.contains(LOCALHOST) && url.contains(String.valueOf(fPort));
  }

  /**
   * Registers a Viewer under a certain ID to the Registry. Viewers need to
   * register if they want to use the Server. Based on the ID, the Server is
   * asking the correct Viewer for the Content.
   *
   * @param id The unique ID under which the Viewer is stored in the registry.
   * @param viewer The Viewer to store in the registry.
   */
  public void register(String id, ContentViewer viewer) {
    fRegistry.put(id, viewer);
  }

  /**
   * Removes a Viewer from the registry.
   *
   * @param id The ID of the Viewer to remove from the registry.
   */
  public void unregister(String id) {
    fRegistry.remove(id);
  }

  /**
   * Check wether the given URL contains one of the display-operations of this
   * Server.
   *
   * @param url The URL to Test for a Display Operation.
   * @return Returns <code>TRUE</code> if the given URL is a display-operation.
   */
  public boolean isDisplayOperation(String url) {
    if (!StringUtils.isSet(url))
      return false;

    return url.contains(OP_DISPLAY_FOLDER) || url.contains(OP_DISPLAY_BOOKMARK) || url.contains(OP_DISPLAY_NEWSBIN) || url.contains(OP_DISPLAY_NEWS) || url.contains(OP_DISPLAY_SEARCHMARK) || URIUtils.ABOUT_BLANK.equals(url);
  }

  /**
   * Check wether the given URL contains one of the resource-operations of this
   * Server.
   *
   * @param url The URL to Test for a Resource Operation.
   * @return Returns <code>TRUE</code> if the given URL is a resource-operation.
   */
  public boolean isResourceOperation(String url) {
    if (!StringUtils.isSet(url))
      return false;

    return url.contains(OP_RESOURCE);
  }

  /**
   * @param path the path to the resource in the plugin.
   * @return a url that can be used to access the resource.
   */
  public String toResourceUrl(String path) {
    StringBuilder url = new StringBuilder();
    url.append(PROTOCOL).append("://").append(LOCALHOST).append(':').append(fPort).append("/"); //$NON-NLS-1$ //$NON-NLS-2$
    url.append("?").append(OP_RESOURCE).append(path); //$NON-NLS-1$

    return url.toString();
  }

  /**
   * Creates a valid URL for the given Input
   *
   * @param id The ID of the Viewer
   * @param input The Input of the Viewer
   * @return a valid URL for the given Input
   */
  public String toUrl(String id, Object input) {

    /* Handle this Case */
    if (input == null)
      return URIUtils.ABOUT_BLANK;

    StringBuilder url = new StringBuilder();
    url.append(PROTOCOL).append("://").append(LOCALHOST).append(':').append(fPort).append("/"); //$NON-NLS-1$ //$NON-NLS-2$

    /* Append the ID */
    url.append("?").append(ID).append(id); //$NON-NLS-1$

    /* Wrap into Object Array */
    if (!(input instanceof Object[]))
      input = new Object[] { input };

    /* Input is an Array of Objects */
    List<Long> news = new ArrayList<Long>();
    List<Long> bookmarks = new ArrayList<Long>();
    List<Long> newsbins = new ArrayList<Long>();
    List<Long> searchmarks = new ArrayList<Long>();
    List<Long> folders = new ArrayList<Long>();

    /* Split into BookMarks, NewsBins, SearchMarks and News */
    for (Object obj : (Object[]) input) {
      if (obj instanceof FolderNewsMarkReference)
        folders.add(getId(obj));
      else if (obj instanceof IBookMark || obj instanceof BookMarkReference)
        bookmarks.add(getId(obj));
      else if (obj instanceof INewsBin || obj instanceof NewsBinReference)
        newsbins.add(getId(obj));
      else if (obj instanceof ISearchMark || obj instanceof SearchMarkReference)
        searchmarks.add(getId(obj));
      else if (obj instanceof INews || obj instanceof NewsReference)
        news.add(getId(obj));
      else if (obj instanceof EntityGroup) {
        List<EntityGroupItem> items = ((EntityGroup) obj).getItems();
        for (EntityGroupItem item : items) {
          IEntity entity = item.getEntity();
          if (entity instanceof INews)
            news.add(getId(entity));
        }
      }
    }

    /* Append Parameter for Folders */
    appendParameters(url, folders, OP_DISPLAY_FOLDER);

    /* Append Parameter for Bookmarks */
    appendParameters(url, bookmarks, OP_DISPLAY_BOOKMARK);

    /* Append Parameter for Newsbins */
    appendParameters(url, newsbins, OP_DISPLAY_NEWSBIN);

    /* Append Parameter for SearchMarks */
    appendParameters(url, searchmarks, OP_DISPLAY_SEARCHMARK);

    /* Append Parameter for News */
    appendParameters(url, news, OP_DISPLAY_NEWS);

    return url.toString();
  }

  private void appendParameters(StringBuilder url, List<Long> ids, String op) {
    if (!ids.isEmpty()) {
      url.append("&").append(op); //$NON-NLS-1$
      for (Long id : ids)
        url.append(id).append(',');

      /* Remove the last added ',' */
      url.deleteCharAt(url.length() - 1);
    }
  }

  private Long getId(Object obj) {
    if (obj instanceof IEntity)
      return ((IEntity) obj).getId();
    else if (obj instanceof ModelReference)
      return ((ModelReference) obj).getId();

    return null;
  }

  private void listen() {

    /* Create a Job to listen for Requests */
    fServerJob = new Job("Local News Viewer Server") { //$NON-NLS-1$
      @Override
      protected IStatus run(IProgressMonitor monitor) {

        /* Listen as long not canceled */
        while (!monitor.isCanceled()) {
          BufferedReader buffReader = null;
          Socket socket = null;
          try {

            /* Blocks until Socket accepted */
            socket = fSocket.accept();

            /* Read Incoming Message */
            buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = buffReader.readLine();

            /* Process Message */
            if (StringUtils.isSet(message))
              safeProcess(socket, message);
          } catch (IOException e) {
            /* Ignore */
          }

          /* Cleanup */
          finally {

            /* Close the Reader */
            try {
              if (buffReader != null)
                buffReader.close();
            } catch (Exception e) {
              /* Ignore */
            }

            /* Close the Socket */
            try {
              if (socket != null)
                socket.close();
            } catch (Exception e) {
              /* Ignore */
            }
          }
        }
        return Status.OK_STATUS;
      }
    };

    /* Set as System-Job and Schedule */
    fServerJob.setSystem(true);
    fServerJob.schedule();
  }

  /* Process Message in Safe-Runnable */
  private void safeProcess(final Socket socket, final String message) {
    final boolean isDisplayOperation = isDisplayOperation(message);
    final boolean isResourceOperation = !isDisplayOperation && isResourceOperation(message);

    LoggingSafeRunnable runnable = new LoggingSafeRunnable() {
      public void run() throws Exception {

        /* This is a Display-Operation */
        if (isDisplayOperation)
          processDisplayOperation(socket, message);

        /* This is a Resource-Operation */
        else if (isResourceOperation)
          processResourceOperation(socket, message);

        /* This is a startup handshake */
        else
          processHandshake(message);
      }
    };

    /*
     * For some reason using SafeRunner from this method can cause in a Classloader deadlock
     * where Equinox will terminate classloading after 5000 ms to avoid it. This happens when
     * two instances of RSSOwl start at the same time, e.g. when clicking the icon twice.
     */
    if (!isDisplayOperation && !isResourceOperation) {
      try {
        runnable.run();
      } catch (Exception e) {
        runnable.handleException(e);
      }
    } else
      SafeRunner.run(runnable);
  }

  /* Process Handshake-Message */
  private void processHandshake(String message) {
    if (fHandshakeHandler != null)
      fHandshakeHandler.handle(message);
  }

  private void processResourceOperation(Socket socket, String message) {

    /* Substring to get the Parameters String */
    int start = message.indexOf(OP_RESOURCE) + OP_RESOURCE.length();
    int end = message.indexOf(' ', start);
    String parameter = message.substring(start, end);

    /* Write HTML to the Receiver */
    BufferedOutputStream outS = null;
    try {
      outS = new BufferedOutputStream(socket.getOutputStream());
      CoreUtils.copy(OwlUI.class.getResourceAsStream(parameter), outS);
    } catch (IOException e) {
      /* Ignore */
    }

    /* Cleanup */
    finally {
      if (outS != null) {
        try {
          outS.close();
        } catch (IOException e) {
          /* Ignore */
        }
      }
    }
  }

  /* Process Message by looking for operations */
  private void processDisplayOperation(Socket socket, String message) {
    List<Object> elements = new ArrayList<Object>();

    /* Substring to get the Parameters String */
    int start = message.indexOf('/');
    int end = message.indexOf(' ', start);
    String parameters = message.substring(start, end);

    /* Retrieve the ID */
    String viewerId = null;
    int idIndex = parameters.indexOf(ID);
    if (idIndex >= 0) {
      start = idIndex + ID.length();
      end = parameters.indexOf('&', start);
      if (end < 0)
        end = parameters.length();

      viewerId = parameters.substring(start, end);
    }

    /* Ask for ContentProvider of Viewer */
    ContentViewer viewer = fRegistry.get(viewerId);
    if (viewer instanceof NewsBrowserViewer && viewer.getContentProvider() != null) {
      IStructuredContentProvider newsContentProvider = (IStructuredContentProvider) viewer.getContentProvider();

      /* Look for Folders that are to displayed */
      int displayFolderIndex = parameters.indexOf(OP_DISPLAY_FOLDER);
      if (displayFolderIndex >= 0) {
        start = displayFolderIndex + OP_DISPLAY_FOLDER.length();
        end = parameters.indexOf('&', start);
        if (end < 0)
          end = parameters.length();

        StringTokenizer tokenizer = new StringTokenizer(parameters.substring(start, end), ",");//$NON-NLS-1$
        while (tokenizer.hasMoreElements()) {
          FolderReference ref = new FolderReference(Long.valueOf((String) tokenizer.nextElement()));
          elements.addAll(Arrays.asList(newsContentProvider.getElements(ref)));
        }
      }

      /* Look for BookMarks that are to displayed */
      int displayBookMarkIndex = parameters.indexOf(OP_DISPLAY_BOOKMARK);
      if (displayBookMarkIndex >= 0) {
        start = displayBookMarkIndex + OP_DISPLAY_BOOKMARK.length();
        end = parameters.indexOf('&', start);
        if (end < 0)
          end = parameters.length();

        StringTokenizer tokenizer = new StringTokenizer(parameters.substring(start, end), ",");//$NON-NLS-1$
        while (tokenizer.hasMoreElements()) {
          BookMarkReference ref = new BookMarkReference(Long.valueOf((String) tokenizer.nextElement()));
          elements.addAll(Arrays.asList(newsContentProvider.getElements(ref)));
        }
      }

      /* Look for NewsBins that are to displayed */
      int displayNewsBinsIndex = parameters.indexOf(OP_DISPLAY_NEWSBIN);
      if (displayNewsBinsIndex >= 0) {
        start = displayNewsBinsIndex + OP_DISPLAY_NEWSBIN.length();
        end = parameters.indexOf('&', start);
        if (end < 0)
          end = parameters.length();

        StringTokenizer tokenizer = new StringTokenizer(parameters.substring(start, end), ",");//$NON-NLS-1$
        while (tokenizer.hasMoreElements()) {
          NewsBinReference ref = new NewsBinReference(Long.valueOf((String) tokenizer.nextElement()));
          elements.addAll(Arrays.asList(newsContentProvider.getElements(ref)));
        }
      }

      /* Look for SearchMarks that are to displayed */
      int displaySearchMarkIndex = parameters.indexOf(OP_DISPLAY_SEARCHMARK);
      if (displaySearchMarkIndex >= 0) {
        start = displaySearchMarkIndex + OP_DISPLAY_SEARCHMARK.length();
        end = parameters.indexOf('&', start);
        if (end < 0)
          end = parameters.length();

        StringTokenizer tokenizer = new StringTokenizer(parameters.substring(start, end), ",");//$NON-NLS-1$
        while (tokenizer.hasMoreElements()) {
          SearchMarkReference ref = new SearchMarkReference(Long.valueOf((String) tokenizer.nextElement()));
          elements.addAll(Arrays.asList(newsContentProvider.getElements(ref)));
        }
      }

      /* Look for News that are to displayed */
      int displayNewsIndex = parameters.indexOf(OP_DISPLAY_NEWS);
      if (displayNewsIndex >= 0) {
        start = displayNewsIndex + OP_DISPLAY_NEWS.length();
        end = parameters.indexOf('&', start);
        if (end < 0)
          end = parameters.length();

        StringTokenizer tokenizer = new StringTokenizer(parameters.substring(start, end), ",");//$NON-NLS-1$
        while (tokenizer.hasMoreElements()) {
          NewsReference ref = new NewsReference(Long.valueOf((String) tokenizer.nextElement()));
          elements.addAll(Arrays.asList(newsContentProvider.getElements(ref)));
        }
      }
    }

    /* Reply to the Socket */
    reply(socket, viewerId, elements.toArray());
  }

  /* Create HTML out of the Elements and reply to the Socket */
  private void reply(Socket socket, String viewerId, Object[] elements) {

    /* Only responsible for Viewer-Concerns */
    if (viewerId == null)
      return;

    /* Retrieve Viewer */
    ContentViewer viewer = fRegistry.get(viewerId);

    /* Might be bad timing */
    if (viewer == null)
      return;

    /* Ask for sorted Elements */
    NewsBrowserLabelProvider labelProvider = (NewsBrowserLabelProvider) viewer.getLabelProvider();
    Object[] children = new Object[0];
    if (viewer instanceof NewsBrowserViewer) {
      children = ((NewsBrowserViewer) viewer).getFlattendChildren(elements);
      ((NewsBrowserViewer) viewer).updateViewModel(children);
    }

    /* Write HTML to the Receiver */
    BufferedWriter writer = null;
    try {
      boolean portable = Controller.getDefault().isPortable();
      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      if (Application.IS_WINDOWS && portable)
        writer.append("HTTP/1.1 205 OK").append(CRLF); //$NON-NLS-1$
      else
        writer.append("HTTP/1.1 200 OK").append(CRLF); //$NON-NLS-1$

      synchronized (RFC_1123_DATE) {
        writer.append("Date: ").append(RFC_1123_DATE.format(new Date())).append(CRLF); //$NON-NLS-1$
      }

      writer.append("Server: RSSOwl Local Server").append(CRLF); //$NON-NLS-1$
      writer.append("Content-Type: text/html; charset=UTF-8").append(CRLF); //$NON-NLS-1$
      writer.append("Connection: close").append(CRLF); //$NON-NLS-1$
      writer.append("Expires: 0").append(CRLF); //$NON-NLS-1$
      writer.write(CRLF);

      /* Begin HTML */
      writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"); //$NON-NLS-1$

      /* Windows only: Mark of the Web */
      if (Application.IS_WINDOWS) {
        writer.write(IE_MOTW);
        writer.write("\n"); //$NON-NLS-1$
      }

      writer.write("<html>\n  <head>\n"); //$NON-NLS-1$

      /* Append Base URI if available */
      String base = getBase(children);
      if (base != null) {
        writer.write("  <base href=\""); //$NON-NLS-1$
        writer.write(base);
        writer.write("\">"); //$NON-NLS-1$
      }

      writer.write("\n  <title></title>"); //$NON-NLS-1$
      writer.write("\n  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"); //$NON-NLS-1$

      /* CSS */
      labelProvider.writeCSS(writer);

      /* Open Body */
      writer.write("  </head>\n  <body id=\"owlbody\">\n"); //$NON-NLS-1$

      /* Output each Element as HTML */
      for (int i = 0; i < children.length; i++) {
        String html = unicodeToEntities(labelProvider.getText(children[i], true, true, i));
        writer.write(html);
      }

      /* End HTML */
      writer.write("\n  </body>\n</html>"); //$NON-NLS-1$
    } catch (IOException e) {
      /* Ignore */
    }

    /* Cleanup */
    finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          /* Ignore */
        }
      }
    }
  }

  /* Find BASE-Information from Elements */
  private String getBase(Object elements[]) {
    for (Object object : elements) {
      if (object instanceof INews) {
        INews news = (INews) object;

        /* Base-Information explicitly set */
        if (news.getBase() != null)
          return URIUtils.toHTTP(news.getBase()).toString();

        /* Use Feed's Link as fallback */
        return URIUtils.toHTTP(news.getFeedLinkAsText());
      }
    }

    return null;
  }

  private String unicodeToEntities(String str) {
    StringBuilder strBuf = new StringBuilder(str.length());

    /* For each character */
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);

      /* This is a non ASCII, non Whitespace character */
      if (!((ch >= 0x0020) && (ch <= 0x007e)) && !Character.isWhitespace(ch)) {
        strBuf.append("&#x"); //$NON-NLS-1$
        String hex = Integer.toHexString(ch & 0xFFFF);

        if (hex.length() == 2)
          strBuf.append("00"); //$NON-NLS-1$

        strBuf.append(hex).append(";"); //$NON-NLS-1$
      }

      /* This is an ASCII character */
      else {
        strBuf.append(ch);
      }
    }

    return strBuf.toString();
  }

  /**
   * @return the port used by this server.
   */
  public int getPort() {
    return SOCKET_PORT;
  }

  /**
   * @return the host used by this server.
   */
  public String getHost() {
    return LOCALHOST;
  }
}