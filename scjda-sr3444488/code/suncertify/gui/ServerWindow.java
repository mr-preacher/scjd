package suncertify.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import suncertify.Application;
import suncertify.ApplicationWindow;

/**
 * Class <code>ServerWindow</code> extends {@link ApplicationWindow}. This is a
 * window that appears when application starts in the server mode.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class ServerWindow extends ApplicationWindow {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;
	/**
	 * Logger for debugging issues
	 */
	private transient Logger log = Logger.getLogger(this.getClass().getPackage()
			.getName());

	/**
	 * about menu item
	 */
	private JMenuItem aboutMI;
	/**
	 * action menu
	 */
	private JMenu actionMenu;
	/**
	 * clear log menu item
	 */
	private JMenuItem clearLogMI;
	/**
	 * exit application menu item
	 */
	private JMenuItem exitMI;
	/**
	 * file menu
	 */
	private JMenu fileMenu;
	/**
	 * file menu separator
	 */
	private JSeparator fileMenuSeparator;

	/**
	 * options menu item
	 */
	private JMenuItem optionsMI;
	/**
	 * help menu item
	 */
	private JMenuItem helpMI;
	/**
	 * help menu
	 */
	private JMenu helpMenu;
	/**
	 * main menu bar
	 */
	private JMenuBar mainMenuBar;
	/**
	 * log file text area
	 */
	private JTextArea mainTextArea;
	/**
	 * status label
	 */
	private JLabel statusLabel;
	/**
	 * status panel
	 */
	private JPanel statusPane;
	/**
	 * scroll pane for text area
	 */
	private JScrollPane textScrollPane;

	/**
	 * Creates new <code>ServerWindow</code> object
	 * 
	 * @param application
	 *         reference to the application object
	 */
	public ServerWindow(final Application application) {
		super(application);
		initComponents();
		initLoggerHandler();
		initOpenListener();
	}

	/**
	 * Initializes listener to display option dialog on startup
	 */
	private void initOpenListener() {
		this.addWindowListener(new WindowAdapter() {
			/**
			 * Invokes option menu handler to show dialog with options i.e. settings
			 */
			public void windowOpened(WindowEvent e) {
				SwingUtilities.invokeLater(new Thread() {
					public void run() {
						updateWindowContent();
						optionsMIActionPerformed();
					}
				});
			}
		});
	}
	
	/**
	 * {@inheritDoc} <br>
	 * Changes status label text according server state when settings are changed.
	 */
	public void updateWindowContent() {
		int port = application.getPortServerRunning();
		statusLabel.setText(port > -1 ? "server running on port " + port
				: "server not running");
	}

	/**
	 * Initializes logging handler to log into text area
	 */
	private void initLoggerHandler() {
		ServerLoggerHandler handler = new ServerLoggerHandler();
		handler.setFormatter(new SimpleFormatter());
		handler.setLevel(Level.SEVERE);

		Logger log = Logger.getLogger("suncertify.db");
		log.addHandler(handler);
		log = Logger.getLogger("suncertify.db.utils");
		log.setLevel(Level.OFF);
		log = Logger.getLogger("suncertify.sockets");
		log.addHandler(handler);

	}

	/**
	 * Initiates components of frame
	 */
	private void initComponents() {
		statusPane = new JPanel();
		statusLabel = new JLabel();
		textScrollPane = new JScrollPane();
		mainTextArea = new JTextArea();
		mainMenuBar = new JMenuBar();
		fileMenu = new JMenu();
		optionsMI = new JMenuItem();
		fileMenuSeparator = new JSeparator();
		exitMI = new JMenuItem();
		actionMenu = new JMenu();
		clearLogMI = new JMenuItem();
		helpMenu = new JMenu();
		helpMI = new JMenuItem();
		aboutMI = new JMenuItem();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		statusPane.setPreferredSize(new java.awt.Dimension(400, 17));
		statusPane.setLayout(new java.awt.BorderLayout());
		statusLabel.setText("");
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
		statusPane.add(statusLabel, java.awt.BorderLayout.CENTER);
		getContentPane().add(statusPane, java.awt.BorderLayout.SOUTH);
		textScrollPane.setPreferredSize(new java.awt.Dimension(106, 200));
		mainTextArea.setColumns(20);
		mainTextArea.setRows(5);
		textScrollPane.setViewportView(mainTextArea);
		getContentPane().add(textScrollPane, java.awt.BorderLayout.CENTER);
		fileMenu.setText("File");
		optionsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
		optionsMI.setText("Options...");
		optionsMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				optionsMIActionPerformed();
			}
		});
		fileMenu.add(optionsMI);
		fileMenu.add(fileMenuSeparator);
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_MASK));
		exitMI.setText("Exit");
		exitMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exitMIActionPerformed();
			}
		});
		fileMenu.add(exitMI);
		mainMenuBar.add(fileMenu);
		actionMenu.setText("Action");
		actionMenu.setActionCommand("Action");
		clearLogMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				InputEvent.ALT_MASK | InputEvent.CTRL_MASK));
		clearLogMI.setText("Clear Log");
		clearLogMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				clearLogMIActionPerformed();
			}
		});
		actionMenu.add(clearLogMI);
		mainMenuBar.add(actionMenu);
		helpMenu.setText("Help");
		helpMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpMI.setText("Help");
		helpMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				helpMIActionPerformed();
			}
		});
		helpMenu.add(helpMI);
		aboutMI.setText("About");
		aboutMI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aboutMIActionPerformed();
			}
		});
		helpMenu.add(aboutMI);
		mainMenuBar.add(helpMenu);
		setJMenuBar(mainMenuBar);
		pack();
	}

	/**
	 * Invokes settings dialog. Redefines settings for application.
	 */
	private void optionsMIActionPerformed() {
		if (application.showOptionsDialog()) {
			application.cleanUp();
			application.applyOptions();
		}
	}

	/**
	 * Clears text area from log messages.
	 */
	private void clearLog() {
		mainTextArea.setText("");
	}

	/**
	 * Adds log message to the text area.
	 * 
	 * @param msg
	 *         log message
	 */
	private void addLogMsg(String msg) {
		mainTextArea.append(msg);
	}

	/**
	 * Exits application, invoked from action listener of menu item.
	 */
	private void exitMIActionPerformed() {
		this.close();
	}

	/**
	 * Clears text area from logging, invoked from action listener of menu item.
	 */
	private void clearLogMIActionPerformed() {
		clearLog();
	}

	/**
	 * Shows help file.
	 */
	private void helpMIActionPerformed() {
		String fileName = "docs/user_guide/server.htm";
		JOptionPane.showMessageDialog(this, "See help file at " + fileName, "Help",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shows about dialog.
	 */
	private void aboutMIActionPerformed() {
		JOptionPane.showMessageDialog(this,
				"Server Bodgitt and Scarper, LLC.\nPetr Shilkin 2009", "About",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Server logging handler for logging into text area.
	 */
	private class ServerLoggerHandler extends Handler {

		/**
		 * {@inheritDoc}<br>
		 * Method not used.
		 */
		public void close() throws SecurityException {

		}

		/**
		 * {@inheritDoc}<br>
		 * Method not used.
		 */
		public void flush() {

		}

		/**
		 * {@inheritDoc}<br>
		 * Places log message into the text area.
		 */
		public void publish(LogRecord record) {
			try {
				String msg = getFormatter().format(record);
				addLogMsg(msg);
			} catch (Exception ex) {
				log.log(Level.SEVERE, "error logging into text area", ex);
				reportError(null, ex, 0);
			}
		}
	}
}
