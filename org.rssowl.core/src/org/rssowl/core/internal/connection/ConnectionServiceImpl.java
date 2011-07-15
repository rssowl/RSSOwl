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

package org.rssowl.core.internal.connection;

import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.IConnectionService;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.connection.IProxyCredentials;
import org.rssowl.core.connection.UnknownProtocolException;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.FeedAdapter;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FeedListener;
import org.rssowl.core.util.ExtensionUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.Triple;

import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * The ConnectionManager is the main class of the connection plugin. It is
 * responsible to retrieve the contents of a Feed by supplying an InputStream.
 *
 * @author bpasero
 */
public class ConnectionServiceImpl implements IConnectionService {

  /* ID of the contributed ProtocolHandlers */
  private static final String PROTHANDLER_EXTENSION_POINT = "org.rssowl.core.ProtocolHandler"; //$NON-NLS-1$

  /* ID of the contributed CredentialsProviders */
  private static final String CREDPROVIDER_EXTENSION_POINT = "org.rssowl.core.CredentialsProvider"; //$NON-NLS-1$

  /* Extension Point: SSL Handler */
  private static final String SSL_HANDLER_EXTENSION_POINT = "org.rssowl.core.SSLHandler"; //$NON-NLS-1$

  private final Map<String, IProtocolHandler> fProtocolHandler;
  private final Map<String, ICredentialsProvider> fCredentialsProvider;
  private final SecureProtocolSocketFactory fSecureProtocolSocketFactory;
  private final FeedListener fFeedListener;

  /** Default Constructor */
  public ConnectionServiceImpl() {
    fProtocolHandler = new HashMap<String, IProtocolHandler>();
    fCredentialsProvider = new HashMap<String, ICredentialsProvider>();

    /* Init */
    fFeedListener = createFeedListener();
    startup();
    fSecureProtocolSocketFactory = loadSSLHandler();
  }

  private void startup() {

    /* Load Contributions */
    loadProtocolHandlers();
    loadCredentialsProvider();

    /* Register URL Stream Handlers to OSGI */
    registerURLStreamHandlers();

    /* Register Listeners */
    registerListeners();
  }

  private FeedListener createFeedListener() {
    return new FeedAdapter() {
      @Override
      public void entitiesDeleted(Set<FeedEvent> events) {
        for (FeedEvent feedEvent : events) {
          URI feedLink = feedEvent.getEntity().getLink();
          try {
            String protocol = feedLink.getScheme();
            if (protocol != null) {
              ICredentialsProvider credentialsProvider = fCredentialsProvider.get(protocol);

              /* Delete Auth Credentials if provided */
              if (credentialsProvider != null && credentialsProvider.getAuthCredentials(feedLink, null) != null)
                credentialsProvider.deleteAuthCredentials(feedLink, null);
            }
          } catch (CredentialsException e) {
            Activator.getDefault().getLog().log(e.getStatus());
          }
        }
      }
    };
  }

  private void registerListeners() {
    /* We register listeners as part of initialisation, we must use InternalOwl */
    InternalOwl.getDefault().getPersistenceService().getDAOService().getFeedDAO().addEntityListener(fFeedListener);
  }

  /*
   * @see org.rssowl.core.connection.IConnectionService#shutdown()
   */
  public void shutdown() {
    unregisterListeners();
  }

  private void unregisterListeners() {
    if (Owl.isStarted())
      DynamicDAO.removeEntityListener(IFeed.class, fFeedListener);
  }

  /*
   * @see org.rssowl.core.connection.IConnectionService#getHandler(java.net.URI)
   */
  public IProtocolHandler getHandler(URI link) throws ConnectionException {
    String protocol = link.getScheme();
    IProtocolHandler handler = fProtocolHandler.get(protocol);

    /* Handler present */
    if (handler != null)
      return handler;

    /* No Handler present */
    throw new UnknownProtocolException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.ConnectionServiceImpl_ERROR_NO_PROTOCOL_HANDLER, protocol), null), protocol);
  }

  /*
   * @see org.rssowl.core.connection.IConnectionService#getLabel(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public String getLabel(URI link, IProgressMonitor monitor) throws ConnectionException {
    String protocol = link.getScheme();
    IProtocolHandler handler = fProtocolHandler.get(protocol);

    /* Handler present */
    if (handler != null)
      return handler.getLabel(link, monitor);

    /* No Handler present */
    throw new UnknownProtocolException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.ConnectionServiceImpl_ERROR_NO_PROTOCOL_HANDLER, protocol), null), protocol);
  }

  /*
   * @see org.rssowl.core.connection.IConnectionService#reload(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor, java.util.Map)
   */
  public Triple<IFeed, IConditionalGet, URI> reload(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws CoreException {
    String protocol = link.getScheme();
    IProtocolHandler handler = fProtocolHandler.get(protocol);

    /* Make sure to provide a Monitor */
    if (monitor == null)
      monitor = new NullProgressMonitor();

    /* Handler present */
    if (handler != null)
      return handler.reload(link, monitor, properties);

    /* No Handler present */
    throw new UnknownProtocolException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.ConnectionServiceImpl_ERROR_NO_PROTOCOL_HANDLER, protocol), null), protocol);
  }

  /*
   * @see
   * org.rssowl.core.connection.IConnectionService#getFeedIcon(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public byte[] getFeedIcon(URI link, IProgressMonitor monitor) throws ConnectionException {
    String protocol = link.getScheme();
    IProtocolHandler handler = fProtocolHandler.get(protocol);

    /* Handler present */
    if (handler != null)
      return handler.getFeedIcon(link, monitor);

    /* No Handler present */
    throw new UnknownProtocolException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.ConnectionServiceImpl_ERROR_NO_PROTOCOL_HANDLER, protocol), null), protocol);
  }

  /*
   * @see org.rssowl.core.connection.IConnectionService#getFeed(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public URI getFeed(URI link, IProgressMonitor monitor) throws ConnectionException {
    String protocol = link.getScheme();
    IProtocolHandler handler = fProtocolHandler.get(protocol);

    /* Handler present */
    if (handler != null)
      return handler.getFeed(link, monitor);

    /* No Handler present */
    throw new UnknownProtocolException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.ConnectionServiceImpl_ERROR_NO_PROTOCOL_HANDLER, protocol), null), protocol);
  }

  /*
   * @see
   * org.rssowl.core.connection.IConnectionService#getCredentialsProvider(java
   * .net.URI)
   */
  public ICredentialsProvider getCredentialsProvider(URI link) {
    return fCredentialsProvider.get(link.getScheme());
  }

  /*
   * @see
   * org.rssowl.core.connection.IConnectionService#getSecureProtocolSocketFactory
   * ()
   */
  public SecureProtocolSocketFactory getSecureProtocolSocketFactory() {
    return fSecureProtocolSocketFactory;
  }

  /*
   * @see
   * org.rssowl.core.connection.IConnectionService#getAuthCredentials(java.net
   * .URI, java.lang.String)
   */
  public ICredentials getAuthCredentials(URI link, String realm) throws CredentialsException {

    /* Require credentials provider */
    ICredentialsProvider credentialsProvider = internalGetCredentialsProvider(link);

    /* Retrieve Credentials */
    ICredentials credentials = credentialsProvider.getAuthCredentials(link, realm);
    return credentials;
  }

  /*
   * @see
   * org.rssowl.core.connection.IConnectionService#getProxyCredentials(java.
   * net.URI)
   */
  public IProxyCredentials getProxyCredentials(URI link) throws CredentialsException {

    /* Require credentials provider */
    ICredentialsProvider credentialsProvider = internalGetCredentialsProvider(link);

    /* Retrieve Credentials */
    IProxyCredentials credentials = credentialsProvider.getProxyCredentials(link);
    return credentials;
  }

  private ICredentialsProvider internalGetCredentialsProvider(URI link) throws CredentialsException {
    String protocol = link.getScheme();

    /* Require protocol */
    if (!StringUtils.isSet(protocol))
      throw new CredentialsException(Activator.getDefault().createErrorStatus(Messages.ConnectionServiceImpl_ERROR_UNKNOWN_PROTOCOL, null));

    /* Require credentials provider */
    ICredentialsProvider credentialsProvider = fCredentialsProvider.get(protocol);
    if (credentialsProvider == null)
      throw new CredentialsException(Activator.getDefault().createErrorStatus(NLS.bind(Messages.ConnectionServiceImpl_ERROR_NO_CREDENTIAL_PROVIDER, protocol), null));

    return credentialsProvider;
  }

  private void loadProtocolHandlers() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IConfigurationElement elements[] = reg.getConfigurationElementsFor(PROTHANDLER_EXTENSION_POINT);
    for (IConfigurationElement element : elements) {
      try {
        String protocol = element.getAttribute("protocol"); //$NON-NLS-1$

        /* Let 3d-Party contributions override our contributions */
        if (fProtocolHandler.containsKey(protocol) && element.getNamespaceIdentifier().contains(ExtensionUtils.RSSOWL_NAMESPACE))
          continue;

        fProtocolHandler.put(protocol, (IProtocolHandler) element.createExecutableExtension("class"));//$NON-NLS-1$
      } catch (InvalidRegistryObjectException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (CoreException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }
  }

  /* Load Credentials Provider contribution */
  private void loadCredentialsProvider() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IConfigurationElement elements[] = reg.getConfigurationElementsFor(CREDPROVIDER_EXTENSION_POINT);
    for (IConfigurationElement element : elements) {
      try {
        String protocol = element.getAttribute("protocol"); //$NON-NLS-1$

        /* Let 3d-Party contributions override our contributions */
        String nsId = element.getNamespaceIdentifier();
        if (fCredentialsProvider.containsKey(protocol) && nsId.contains(ExtensionUtils.RSSOWL_NAMESPACE) && !nsId.contains(ExtensionUtils.RSSOWL_TESTS_NAMESPACE))
          continue;

        fCredentialsProvider.put(protocol, (ICredentialsProvider) element.createExecutableExtension("class"));//$NON-NLS-1$
      } catch (InvalidRegistryObjectException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (CoreException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }
  }

  /* Load SSLHandler Contribution */
  private SecureProtocolSocketFactory loadSSLHandler() {
    return (SecureProtocolSocketFactory) ExtensionUtils.loadSingletonExecutableExtension(SSL_HANDLER_EXTENSION_POINT);
  }

  private void registerURLStreamHandlers() {

    /* Foreach Contributed Protocol */
    for (String protocol : fProtocolHandler.keySet()) {
      IProtocolHandler protocolHandler = fProtocolHandler.get(protocol);

      /* A URLStreamHandler is provided */
      try {
        if (protocolHandler.getURLStreamHandler() != null) {
          Hashtable<String, String[]> properties = new Hashtable<String, String[]>(1);
          properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { protocol });
          Activator.getDefault().getContext().registerService(URLStreamHandlerService.class.getName(), protocolHandler.getURLStreamHandler(), properties);
        }
      } catch (ConnectionException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }
  }
}