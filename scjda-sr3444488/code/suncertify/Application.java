package suncertify;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import suncertify.db.DBAccess;
import suncertify.db.DataAU;
import suncertify.db.Finalizable;
import suncertify.gui.ClientWindow;
import suncertify.gui.ServerWindow;
import suncertify.gui.SettingsDialogPanel;
import suncertify.gui.SettingsDialogPanel.SettingsType;
import suncertify.sockets.RecordSocketClient;
import suncertify.sockets.RecordSocketServer;

/**
 * Class <code>Application</code> is a controller of application. It is a
 * startup application class also.<br>
 * It has references to the most important objects:
 * <ul>
 * <li>{@link DBAccess} for database operating</li>
 * <li>{@link ApplicationOptions} for options operating</li>
 * <li>{@link RecordSocketServer} for networking interaction</li>
 * </ul>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class Application {
	/**
	 * Enumeration of application types.
	 */
	public enum ApplicationType {
		/**
		 * Server type of application, application works as network server waiting for
		 * client connections via TCP/IP protocol.
		 */
		SERVER,
		/**
		 * Standalone type of application, application works as standalone client with
		 * local database file.
		 */
		STANDALONE,
		/**
		 * Client type of application, application works as network client, connects
		 * to network server via TCP/IP protocol.
		 */
		CLIENT
	}

	/**
	 * logger for the debugging
	 */
	private Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	/**
	 * main window reference of this application
	 */
	private ApplicationWindow mainWindow;
	/**
	 * type of this application
	 */
	private ApplicationType type;
	/**
	 * properties filename
	 */
	private final String settingsFileName = "suncertify.properties";
	/**
	 * database editing object reference
	 */
	private DBAccess dbaccess;
	/**
	 * socket server reference, it equals null if application does not is client
	 * application
	 */
	private RecordSocketServer socketServer;
	/**
	 * application options reference
	 */
	private ApplicationOptions appOptions;

	/**
	 * connection string to database for client and standalone application
	 */
	private String connectionString;

	/**
	 * Starts the program.<br>
	 * Parses program arguments and starts application with a certain type.
	 * 
	 * @param args
	 *         program (command line) arguments
	 */
	public static void main(String... args) {
		Application app = new Application(args);
		app.showMainWindow();
	}

	/**
	 * Constructor of <code>Application</code> object.<br>
	 * Constructor parses command line arguments to define a type of application
	 * started.
	 * 
	 * @param args
	 *         command line arguments
	 */
	public Application(String... args) {
		initOptions();
		parseApplicationType(args);
		if (this.type == null) {
			String msg = "usage: " + "java -jar <path_and_filename> [<mode>]\n"
					+ "The mode flag must be either \"server\", "
					+ "indicating the server program must run, \n"
					+ "\"alone\", indicating standalone mode, \nor left out entirely, "
					+ "in which case the network client and gui must run.";
			System.out.println(msg);
			JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Applies currently assigned application options for this application
	 * according application type.
	 */
	public void applyOptions() {
		switch (type) {
		case SERVER:
			applyServerSettings();
			break;
		case STANDALONE:
			applyStandaloneSettings();
			break;
		case CLIENT:
			applyClientSettings();
			break;
		}
		log.log(Level.SEVERE, "new options applied: " + appOptions);
		mainWindow.updateWindowContent();
	}

	/**
	 * Performs cleanup of application.<br>
	 * Cleanup used to release system resources from objects.
	 */
	public synchronized void cleanUp() {
		if (dbaccess != null) {
			if (dbaccess instanceof Finalizable) {
				((Finalizable) dbaccess).finalize();
			}
			dbaccess = null;
		}
		if (socketServer != null) {
			socketServer.stop();
			socketServer = null;
		}
		connectionString = null;
		mainWindow.updateWindowContent();
		try {
			this.appOptions.saveOptions();
		} catch (IOException e) {
			handleException(e.getMessage());
		}
	}

	/**
	 * Returns type of this application.
	 * 
	 * @return one of types of application
	 */
	public ApplicationType getApplicationType() {
		return this.type;
	}

	/**
	 * Returns reference to database operating class.
	 * 
	 * @return DB reference to database operating class
	 */
	public DBAccess getDBAccess() {
		return dbaccess;
	}

	/**
	 * Returns connection string to database.
	 * 
	 * @return connection string to database if connection exists or
	 *         <code>null</code> if connection not established
	 */
	public String getDBUrl() {
		return connectionString;
	}

	/**
	 * Returns port of running server.
	 * 
	 * @return port number of server or <code>-1</code> if server is not running
	 */
	public int getPortServerRunning() {
		if (socketServer == null || !socketServer.isRunning())
			return -1;
		else
			return socketServer.getPort();
	}

	/**
	 * Handles exception, displays error style dialog.
	 * 
	 * @param msg
	 *         text message shown in dialog
	 */
	public void handleException(String msg) {
		JOptionPane alert = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE,
				JOptionPane.DEFAULT_OPTION);
		JDialog dialog = alert.createDialog(null, "Alert");
		// Center on screen
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((d.getWidth() - dialog.getWidth()) / 2);
		int y = (int) ((d.getHeight() - dialog.getHeight()) / 2);
		dialog.setLocation(x, y);
		dialog.setVisible(true);
	}

	/**
	 * Shows settings dialog to edit state of {@link ApplicationOptions} object.<br>
	 * Returns status of dialog closing.
	 * 
	 * @return <code>true</code> if user pressed OK button on the settings dialog,
	 *         <code>false</code> if user cancels.
	 */
	public boolean showOptionsDialog() {
		String[] opnames = this.appOptions.getOptionsNames();
		String[] opvals = this.appOptions.getOptionsValues();
		if (SettingsDialogPanel.showDialog(opvals, opnames, getSettingsType())) {
			this.appOptions.setOptionsValues(opvals);
			return true;
		}
		return false;
	}

	/**
	 * Applies client settings.
	 */
	private void applyClientSettings() {
		String host = appOptions.getHostName();
		int port = appOptions.getPortNumber();
		try {
			this.dbaccess = new RecordSocketClient(host, port);
			connectionString = (host + ":" + port);
		} catch (Exception e) {
			handleException(e.getMessage());
		}
	}

	/**
	 * Applies server settings.
	 */
	private void applyServerSettings() {
		try {
			int port = appOptions.getPortNumber();
			String path = appOptions.getDBPath();
			int locktimeout = appOptions.getLockTimeout();
			this.dbaccess = new DataAU(path, locktimeout);
			this.socketServer = new RecordSocketServer(dbaccess, port, 5000);
			this.socketServer.start();
		} catch (Exception e) {
			handleException(e.getMessage());
		}
	}

	/**
	 * Applies standalone client settings.
	 */
	private void applyStandaloneSettings() {
		try {
			String path = this.appOptions.getDBPath();
			int locktimeout = this.appOptions.getLockTimeout();
			this.dbaccess = new DataAU(path, locktimeout);
			connectionString = path;
		} catch (Exception e) {
			handleException(e.getMessage());
		}
	}

	/**
	 * Returns settings type for {@link SettingsDialogPanel} that is used in
	 * application options dialog.
	 * 
	 * @return SettingsType for <code>SettingsDialogPanel</code> panel.
	 */
	private SettingsType getSettingsType() {
		switch (type) {
		case SERVER:
			return SettingsType.SERVER;
		case CLIENT:
			return SettingsType.CLIENT;
		case STANDALONE:
			return SettingsType.STANDALONE;
		}
		return null;
	}

	/**
	 * Initializes main window.
	 */
	private void initMainWindow() {
		if (mainWindow == null) {
			switch (type) {
			case SERVER:
				mainWindow = new ServerWindow(this);
				break;
			case CLIENT:
			case STANDALONE:
				mainWindow = new ClientWindow(this);
				break;
			}
		}
	}

	/**
	 * Initializes options of application.
	 */
	private void initOptions() {
		appOptions = new ApplicationOptions(this, settingsFileName);
		try {
			appOptions.loadOptions();
		} catch (Exception e) {
			appOptions.initOptionsManually();
			handleException(e.getMessage());
		}
	}

	/**
	 * Parses type of application using parameters.
	 * 
	 * @param args
	 *         command line arguments
	 */
	private void parseApplicationType(String[] args) {
		if (args.length == 0) {
			this.type = ApplicationType.CLIENT;
		} else {
			if (args.length == 1) {
				if ("alone".equals(args[0])) {
					this.type = ApplicationType.STANDALONE;
				} else if ("server".equals(args[0])) {
					this.type = ApplicationType.SERVER;
				}
			}
		}
	}

	/**
	 * Shows main application window.<br>
	 * For each application type only one main window defined.
	 */
	private void showMainWindow() {
		initMainWindow();
		mainWindow.setVisible(true);
	}
}
