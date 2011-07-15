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

package org.rssowl.core.internal;

import org.rssowl.core.IApplicationService;
import org.rssowl.core.Owl.StartLevel;
import org.rssowl.core.connection.IConnectionService;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.internal.connection.ConnectionServiceImpl;
import org.rssowl.core.internal.interpreter.InterpreterServiceImpl;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.internal.persist.service.PreferenceServiceImpl;
import org.rssowl.core.interpreter.IElementHandler;
import org.rssowl.core.interpreter.IFormatInterpreter;
import org.rssowl.core.interpreter.IInterpreterService;
import org.rssowl.core.interpreter.INamespaceHandler;
import org.rssowl.core.interpreter.IXMLParser;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferencesInitializer;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.persist.service.IPersistenceService;
import org.rssowl.core.persist.service.IPreferenceService;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ExtensionUtils;
import org.rssowl.core.util.LongOperationMonitor;
import org.rssowl.core.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * The <code>InternalOwl</code> is being used from the public <code>Owl</code>
 * facade.
 *
 * @author bpasero
 */
public final class InternalOwl {

  /* The Singleton Instance */
  private static final InternalOwl INSTANCE = new InternalOwl();

  /* Extension Point: Factory for Model Types */
  private static final String MODEL_TYPESFACTORY_EXTENSION_POINT = "org.rssowl.core.ModelFactory"; //$NON-NLS-1$

  /* Extension Point: Persistence Service */
  private static final String PERSISTENCE_SERVICE_EXTENSION_POINT = "org.rssowl.core.PersistenceService"; //$NON-NLS-1$

  /* ID for Application Service Contribution */
  private static final String MODEL_APPLICATION_SERVICE_EXTENSION_POINT = "org.rssowl.core.ApplicationService"; //$NON-NLS-1$

  private volatile IPreferenceService fPreferencesService;
  private volatile IConnectionService fConnectionService;
  private volatile IInterpreterService fInterpreterService;
  private volatile IPersistenceService fPersistenceService;
  private volatile IApplicationService fApplicationService;
  private volatile IModelFactory fModelFactory;
  private volatile boolean fShuttingDown;
  private volatile boolean fStarted;
  private volatile StartLevel fStartLevel = StartLevel.NOT_STARTED;

  /** Flag indicating Performance-Tests are running */
  public volatile static boolean PERF_TESTING = false;

  /** Flag indicating JUnit-Tests are running */
  public volatile static boolean TESTING = false;

  /** Flag indicating we are running from Eclipse */
  public static final boolean IS_ECLIPSE = false;

  private InternalOwl() {}

  /**
   * <em>Never</em> change the ordering of this method's calls!
   *
   * @param monitor A progress monitor to report progress on long running
   * operations (e.g. migration).
   * @param emergency if <code>true</code> indicates this startup method is
   * called from an emergency situation like restoring a backup.
   * @param forRestore if <code>true</code> will open the restore DB as profile
   * and <code>false</code> to open the default profile location.
   */
  public void startup(LongOperationMonitor monitor, boolean emergency, boolean forRestore) {

    /* Increment Start Level */
    if (fStartLevel == StartLevel.NOT_STARTED)
      fStartLevel = StartLevel.STARTING;

    /* Make sure that any error gets logged to the global log */
    System.setErr(new PrintStream(new ByteArrayOutputStream()) {
      @Override
      public void write(byte[] buf, int off, int len) {
        if (buf != null && len >= 0 && off >= 0 && off <= buf.length - len)
          CoreUtils.appendLogMessage(new String(buf, off, len));
      }
    });

    /* Create Model Factory */
    if (fModelFactory == null)
      fModelFactory = loadTypesFactory();

    /* Create Persistence Service */
    if (fPersistenceService == null)
      fPersistenceService = loadPersistenceService();

    /* Create Application Service */
    if (fApplicationService == null)
      fApplicationService = loadApplicationService();

    /* Persistence Layer has its own startup routine */
    fPersistenceService.startup(monitor, emergency, forRestore);

    /* Create Connection Service */
    if (fConnectionService == null)
      fConnectionService = new ConnectionServiceImpl();

    /* Create Interpreter Service */
    if (fInterpreterService == null)
      fInterpreterService = new InterpreterServiceImpl();

    /* Create Preferences Service */
    if (fPreferencesService == null)
      fPreferencesService = new PreferenceServiceImpl();

    /* Flag as started */
    fStarted = true;
    fStartLevel = StartLevel.STARTED;
  }

  /**
   * @param level the new {@link StartLevel}
   */
  public void setStartLevel(StartLevel level) {
    if (level.ordinal() > fStartLevel.ordinal())
      fStartLevel = level;
  }

  /**
   * @return the {@link StartLevel} from the
   * {@link #startup(LongOperationMonitor, boolean, boolean)} sequence.
   */
  public StartLevel getStartLevel() {
    return fStartLevel;
  }

  /**
   * @return Returns the singleton instance of <code>InternalOwl</code>.
   */
  public static InternalOwl getDefault() {
    return INSTANCE;
  }

  /**
   * @return Returns <code>TRUE</code> if this facade has been started and
   * finished initialization.
   */
  public boolean isStarted() {
    return fStarted;
  }

  /**
   * Shutdown the Services managed by this Facade
   *
   * @param emergency If set to <code>TRUE</code>, this method is called from a
   * shutdown hook that got triggered from a non-normal shutdown (e.g. System
   * Shutdown).
   */
  public void shutdown(boolean emergency) {
    fShuttingDown = true;

    /* Shutdown Connection Manager (safely) */
    if (!emergency && fConnectionService != null) {
      try {
        fConnectionService.shutdown();
      } catch (Exception e) {
        Activator.safeLogError(e.getMessage(), e);
      }
    }

    /* Shutdown Persistence Service */
    if (fPersistenceService != null)
      fPersistenceService.shutdown(emergency);

    fStartLevel = StartLevel.NOT_STARTED;
  }

  /**
   * @return Returns <code>TRUE</code> if {@link InternalOwl#shutdown(boolean)}
   * has been called.
   */
  public boolean isShuttingDown() {
    return fShuttingDown;
  }

  /**
   * <p>
   * Get the Implementation of <code>IApplicationService</code> that contains
   * special Methods which are used through the Application and access the
   * persistence layer. The implementation is looked up using the
   * "org.rssowl.core.model.ApplicationService" Extension Point.
   * </p>
   * Subclasses may override to provide their own implementation.
   *
   * @return Returns the Implementation of <code>IApplicationService</code> that
   * contains special Methods which are used through the Application and access
   * the persistence layer.
   */
  public IApplicationService getApplicationService() {
    return fApplicationService;
  }

  private IApplicationService loadApplicationService() {
    return (IApplicationService) ExtensionUtils.loadSingletonExecutableExtension(MODEL_APPLICATION_SERVICE_EXTENSION_POINT);
  }

  /**
   * <p>
   * Provides access to the scoped preferences service in RSSOwl. There is three
   * levels of preferences: Default, Global and Entity. Any preference that is
   * not set at the one scope will be looked up in the parent scope until the
   * Default scope is reached. This allows to easily override the preferences
   * for all entities without having to define the preferences per entity.
   * </p>
   * <p>
   * You can define default preferences by using the PreferencesInitializer
   * extension point provided by this plugin.
   * </p>
   *
   * @return Returns the IPreferenceService that provides access to the scoped
   * preferences system in RSSOwl.
   * @see IPreferenceScope
   * @see IPreferencesInitializer
   */
  public IPreferenceService getPreferenceService() {
    return fPreferencesService;
  }

  /**
   * Provides access to ther persistence layer of RSSOwl. This layer is
   * contributable via the PersistenceService extension point provided by this
   * plugin. The work that is done by the layer includes:
   * <ul>
   * <li>Controlling the lifecycle of the persistence layer</li>
   * <li>Providing the DAOService that contains DAOs for each persistable entity
   * </li>
   * <li>Providing the model search to perform full-text searching</li>
   * </ul>
   *
   * @return Returns the service responsible for all persistence related tasks.
   * @see DAOService
   * @see IModelSearch
   */
  public IPersistenceService getPersistenceService() {
    return fPersistenceService;
  }

  /* Load the contributed persistence service */
  private IPersistenceService loadPersistenceService() {
    return (IPersistenceService) ExtensionUtils.loadSingletonExecutableExtension(PERSISTENCE_SERVICE_EXTENSION_POINT);
  }

  /**
   * Provides access to the connection service of RSSOwl. This service provides
   * API to load data from the internet (e.g. loading the contents of a feed).
   * It is also the central place to ask for credentials if a resource requires
   * authentication. Several extension points allow to customize the behavor of
   * this service, including the ability to register
   * <code>IProtocolHandler</code> to define the lookup process on per protocol
   * basis or contributing <code>ICredentialsProvider</code> to define how
   * credentials should be stored and retrieved.
   *
   * @return Returns the service responsible for all connection related tasks.
   * @see IProtocolHandler
   * @see ICredentialsProvider
   */
  public IConnectionService getConnectionService() {
    return fConnectionService;
  }

  /**
   * Provides access to the interpreter service of RSSOwl. This service provides
   * API to convert a stream of data into a model representation. In the common
   * case of a XML stream this involves using a XML-Parser and creating the
   * model out of the content. Various extension points allow to customize the
   * behavor of the interpreter:
   * <ul>
   * <li>Contribute a new format interpreter using the FormatInterpreter
   * extension point. This allows to display any XML in RSSOwl as Feed.</li>
   * <li>Contribute a new namespace handler using the NamespaceHandler extension
   * point. This allows to properly handle any new namespace in RSSOwl.</li>
   * <li>Contribute a new element handler using the ElementHandler extension
   * point. This makes RSSOwl understand new elements or even attributes.</li>
   * <li>Contribute a new xml parser using the XMLParser extension point if you
   * are not happy with the default one.</li>
   * </ul>
   *
   * @return Returns the service responsible for interpreting a resource.
   * @see IFormatInterpreter
   * @see IElementHandler
   * @see INamespaceHandler
   * @see IXMLParser
   */
  public IInterpreterService getInterpreter() {
    return fInterpreterService;
  }

  /**
   * Provides access to the model factory of RSSOwl. This factory is used
   * everywhere when new entities are created. The factory can be replaced using
   * the ModelFactory extension point.
   *
   * @return Returns the model factory that is used to create model types.
   */
  public IModelFactory getModelFactory() {
    return fModelFactory;
  }

  /**
   * Returns the profile {@link File} that contains all data and the
   * {@link Long} timestamp when it was last successfully used.
   *
   * @return the profile {@link File} and the {@link Long} timestamp when it was
   * last successfully used.
   */
  public Pair<File /* Profile File */, Long /* Timestamp of last successful use */> getProfile() {
    return ((PersistenceServiceImpl) fPersistenceService).getProfile();
  }

  /**
   * Provides a list of available backups for the user to restore from in case
   * of an unrecoverable error.
   *
   * @return a list of available backups for the user to restore from in case of
   * an unrecoverable error.
   */
  public List<File> getProfileBackups() {
    return ((PersistenceServiceImpl) fPersistenceService).getProfileBackups();
  }

  /**
   * Will rename the provided backup file to the operational RSSOwl profile
   * database and trigger search reindexing after next start.
   *
   * @param backup the backup {@link File} to restore from.
   * @throws PersistenceException in case a problem occurs while trying to
   * execute this operation.
   */
  public void restoreProfile(File backup) throws PersistenceException {
    ((PersistenceServiceImpl) fPersistenceService).restoreProfile(backup);
    fPersistenceService.getModelSearch().reIndexOnNextStartup();
  }

  /**
   * Recreate the Profile of the persistence layer. In case of a Database, this
   * would drop relations and create them again.
   *
   * @param needsEmergencyStartup if <code>true</code> causes this method to
   * also trigger an emergency startup so that other operations can be normally
   * done afterwards like importing from a OPML backup.
   * @throws PersistenceException In case of an error while starting up the
   * persistence layer.
   */
  public void recreateProfile(boolean needsEmergencyStartup) throws PersistenceException {
    ((PersistenceServiceImpl) fPersistenceService).recreateProfile(needsEmergencyStartup);
  }

  /* Load Model Types Factory contribution */
  private IModelFactory loadTypesFactory() {
    return (IModelFactory) ExtensionUtils.loadSingletonExecutableExtension(MODEL_TYPESFACTORY_EXTENSION_POINT);
  }
}