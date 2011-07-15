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

package org.rssowl.core.tests.controller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.IConditionalGetCompatible;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.util.Triple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Protocol-Handler to return a Feed from In-Memory.
 *
 * @author bpasero
 */
public class InMemoryProtocolHandler implements IProtocolHandler {
  static String FEED;

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#reload(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor, java.util.Map)
   */
  public Triple<IFeed, IConditionalGet, URI> reload(URI link, IProgressMonitor monitor, Map<Object, Object> properties) throws CoreException {
    IModelFactory typesFactory = Owl.getModelFactory();

    /* Create a new empty feed from the existing one */
    IFeed feed = typesFactory.createFeed(null, link);

    /* Add Monitor to support early cancelation */
    properties.put(IConnectionPropertyConstants.PROGRESS_MONITOR, monitor);

    /* Retrieve the InputStream out of the Feed's Link */
    InputStream inS = new ByteArrayInputStream(FEED.getBytes());

    /* Retrieve Conditional Get if present */
    IConditionalGet conditionalGet = getConditionalGet(link, inS);

    /* Return on Cancelation or Shutdown */
    if (monitor.isCanceled()) {
      try {
        inS.close();
      } catch (IOException e) {
        /* Ignore */
      }
      return Triple.create(feed, conditionalGet, link);
    }

    /* Pass the Stream to the Interpreter */
    Owl.getInterpreter().interpret(inS, feed, null);

    return Triple.create(feed, conditionalGet, link);
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getFeedIcon(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public byte[] getFeedIcon(URI link, IProgressMonitor monitor) {
    return null;
  }

  private IConditionalGet getConditionalGet(URI link, InputStream inS) {
    IModelFactory typesFactory = Owl.getModelFactory();

    if (inS instanceof IConditionalGetCompatible) {
      String ifModifiedSince = ((IConditionalGetCompatible) inS).getIfModifiedSince();
      String ifNoneMatch = ((IConditionalGetCompatible) inS).getIfNoneMatch();

      if (ifModifiedSince != null || ifNoneMatch != null)
        return typesFactory.createConditionalGet(ifModifiedSince, link, ifNoneMatch);
    }

    return null;
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getURLStreamHandler()
   */
  @SuppressWarnings("unused")
  public URLStreamHandlerService getURLStreamHandler() throws ConnectionException {
    return new AbstractURLStreamHandlerService() {

      @SuppressWarnings("unused")
      @Override
      public URLConnection openConnection(URL u) throws IOException {
        return null;
      }
    };
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getLabel(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public String getLabel(URI link, IProgressMonitor monitor) {
    throw new UnsupportedOperationException();
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#getFeed(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public URI getFeed(URI website, IProgressMonitor monitor) {
    return website;
  }

  /*
   * @see org.rssowl.core.connection.IProtocolHandler#openStream(java.net.URI,
   * org.eclipse.core.runtime.IProgressMonitor, java.util.Map)
   */
  public InputStream openStream(URI link, IProgressMonitor monitor, Map<Object, Object> properties) {
    throw new UnsupportedOperationException();
  }
}