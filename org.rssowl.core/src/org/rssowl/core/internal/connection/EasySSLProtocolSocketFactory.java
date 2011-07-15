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

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * EasySSLProtocolSocketFactory can be used to create SSL {@link Socket}s that
 * accept self-signed certificates.
 * 
 * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski </a>
 */
public class EasySSLProtocolSocketFactory implements SecureProtocolSocketFactory {
  private SSLContext fSslcontext;

  /**
   * Create the SSL Context.
   * 
   * @return The SSLContext
   */
  private static SSLContext createEasySSLContext() {
    try {
      SSLContext context = SSLContext.getInstance("SSL"); //$NON-NLS-1$
      context.init(null, new TrustManager[] { new EasyX509TrustManager(null) }, null);
      return context;
    } catch (Exception e) {
      throw new HttpClientError(e.toString());
    }
  }

  /*
   * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.net.Socket,
   * java.lang.String, int, boolean)
   */
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
    return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
  }

  /*
   * @see ProtocolSocketFactory#createSocket(java.lang.String,int)
   */
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return getSSLContext().getSocketFactory().createSocket(host, port);
  }

  /*
   * @see ProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
   */
  public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
    return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
  }

  /**
   * Attempts to get a new socket connection to the given host within the given
   * time limit.
   * 
   * @param host the host name/IP
   * @param port the port on the host
   * @param params {@link HttpConnectionParams Http connection parameters}
   * @return Socket a new socket
   * @throws IOException if an I/O error occurs while creating the socket
   * @throws UnknownHostException if the IP address of the host cannot be
   * determined
   */
  public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
    if (params == null)
      throw new IllegalArgumentException("Parameters may not be null"); //$NON-NLS-1$

    /* Determine Connection Timeout */
    int timeout = params.getConnectionTimeout();
    SocketFactory socketfactory = getSSLContext().getSocketFactory();

    /* Timeout is unlimited */
    if (timeout == 0)
      return socketfactory.createSocket(host, port, localAddress, localPort);

    /* Timeout is defined */
    Socket socket = socketfactory.createSocket();
    SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
    SocketAddress remoteaddr = new InetSocketAddress(host, port);
    socket.bind(localaddr);
    socket.connect(remoteaddr, timeout);
    return socket;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return EasySSLProtocolSocketFactory.class.hashCode();
  }

  /*
   * @return The SSLContext
   */
  private SSLContext getSSLContext() {
    if (fSslcontext == null)
      fSslcontext = createEasySSLContext();

    return fSslcontext;
  }
}