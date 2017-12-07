import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

/**
 * https://www.eclipse.org/jetty/documentation/9.4.x/embedded-examples.html
 */
public class TestWebServer {
  public static final String someFeedName = "some_feed.xml";

  public static void main(String[] args) {

    NCSARequestLog requestLog = new NCSARequestLog();
//    requestLog.setFilename("/path/to/my/logs/yyyy_mm_dd.request.log");
//    requestLog.setFilenameDateFormat("yyyy_MM_dd");
//    requestLog.setAppend(true);
//    requestLog.setRetainDays(1);
    requestLog.setExtended(true);
    requestLog.setLogCookies(false);
    requestLog.setLogTimeZone("GMT");
    RequestLogHandler requestLogHandler = new RequestLogHandler();
    requestLogHandler.setRequestLog(requestLog);

    try {
      String jettyDistKeystore = "unimportant_weak.keystore";
      File keystoreFile = new File(jettyDistKeystore);
      if (!keystoreFile.exists())
        throw new FileNotFoundException(keystoreFile.getAbsolutePath());

      final Server server = new Server();
      server.setStopAtShutdown(true);
      server.setStopTimeout(1000);

      //server ignores aliases instead of using real location
      String sdir = "./data";
      Resource dir = Resource.newResource(sdir);
      if (dir.isAlias())
        sdir = dir.getAlias().toString();

      ResourceHandler resourceHandler = new ResourceHandler();
      resourceHandler.setDirAllowed(true);
      resourceHandler.setDirectoriesListed(true);
      resourceHandler.setResourceBase(sdir);

      ContextHandler feedContextHandler = new ContextHandler("/feed/");
      feedContextHandler.setHandler(resourceHandler);

      ContextHandler authContextHandler = new ContextHandler("/auth/");
      authContextHandler.setHandler(resourceHandler);

      {
        String realmProperties = "realm.properties";
        File realmPropertiesFile = new File(realmProperties);
        if (!realmPropertiesFile.exists())
          throw new FileNotFoundException(realmPropertiesFile.getAbsolutePath());

        UserStore userStore = new UserStore();
        userStore.addUser("usr", new Password("pw"), new String[] { "user" });
        userStore.addUser("admi", new Password("1234"), new String[] { "user", "admin" });

        HashLoginService loginService = new HashLoginService();
        loginService.setName("TestRealm");
//        loginService.setConfig(realmPropertiesFile.getAbsolutePath());
        loginService.setUserStore(userStore);
        loginService.setHotReload(false);
        server.addBean(loginService);

        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        server.setHandler(security);

        Constraint constraint = new Constraint();
        constraint.setAuthenticate(true);
//        constraint.setName("usr");
//        constraint.setName("Secure resources");
//        constraint.setRoles(new String[] { "user", "admin" });
        constraint.setRoles(new String[] { "admin" });

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/auth/*");
        mapping.setConstraint(constraint);

//        security.setRealmName("TestRealm");
        security.setConstraintMappings(Collections.singletonList(mapping));
        //auth method
        security.setAuthenticator(new BasicAuthenticator());
        //TODO other authentication methods
        // http://www.eclipse.org/jetty/documentation/current/configuring-security.html#configuring-security-authentication
//        security.setAuthenticator(new DigestAuthenticator());
//        security.setAuthenticator(new FormAuthenticator());
//        security.setAuthenticator(new ClientCertAuthenticator());
//        security.setAuthenticator(new SpnegoAuthenticator());
        security.setLoginService(loginService);

        security.setHandler(resourceHandler);
      }
      {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(8080);
        http.setIdleTimeout(5000);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
        sslContextFactory.setKeyStorePassword("somepassstore");
        sslContextFactory.setKeyManagerPassword("somepasskey");

        // OPTIONAL: Un-comment the following to use Conscrypt for SSL instead of
        // the native JSSE implementation.

//      Security.addProvider(new OpenSSLProvider());
//      sslContextFactory.setProvider("Conscrypt");

        HttpConfiguration https_config = new HttpConfiguration(http_config);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);
        https_config.addCustomizer(src);

        ServerConnector https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(https_config));
        https.setPort(8443);
        https.setIdleTimeout(500000);

        server.setConnectors(new Connector[] { http, https });
      }

      ContextHandler quitContextHandler = new ContextHandler("/manual-quit/");
      quitContextHandler.setHandler(new DefaultHandler() {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
          new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                server.stop();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }).start();
          super.handle(target, baseRequest, request, response);
        }
      });

      ContextHandler updateProgramCH = new ContextHandler("/rssowl/");
      {
        String updateSitePath = "file:///S:\\CODE\\ProjectsMy\\java\\rssowl\\rssowlprj\\update_site_2_3_1\\rssowl";
        Resource updateSiteResource = Resource.newResource(updateSitePath);
        if (updateSiteResource.isAlias())
          updateSitePath = updateSiteResource.getAlias().toString();

        ResourceHandler updateProgramRH = new ResourceHandler();
        updateProgramRH.setDirAllowed(true);
        updateProgramRH.setDirectoriesListed(true);
        updateProgramRH.setResourceBase(updateSitePath);

        updateProgramCH.setHandler(updateProgramRH);
      }

      DefaultHandler defaultHandler = new DefaultHandler();

      HandlerList handlers = new HandlerList();
      handlers.setHandlers(new Handler[] { requestLogHandler, updateProgramCH, feedContextHandler, authContextHandler, quitContextHandler, defaultHandler });
      server.setHandler(handlers);

//    System.out.println(server.dump());
      server.start();
      server.join();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
