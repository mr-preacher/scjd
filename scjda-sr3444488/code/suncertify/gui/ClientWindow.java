package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.TableModel;
import suncertify.Application;
import suncertify.ApplicationWindow;
import suncertify.Application.ApplicationType;
import suncertify.db.DBAccess;
import suncertify.gui.OperationDialogPanel.OperationType;

/**
 * Class <code>ClientWindow</code> extends {@link ApplicationWindow}. This is a
 * window that appears when application starts in the client mode (network
 * client or standalone client).
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class ClientWindow extends ApplicationWindow {
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
	 * options menu item
	 */
	private JMenuItem optionsMI;
	/**
	 * about menu item
	 */
	private JMenuItem aboutMI;
	/**
	 * action menu
	 */
	private JMenu actionMenu;
	/**
	 * book menu item
	 */
	private JMenuItem bookMI;
	/**
	 * create menu item
	 */
	private JMenuItem createMI;
	/**
	 * delete menu item
	 */
	private JMenuItem deleteMI;
	/**
	 * edit menu item
	 */
	private JMenuItem editMI;
	/**
	 * exit menu item
	 */
	private JMenuItem exitMI;
	/**
	 * find menu item
	 */
	private JMenuItem findMI;
	/**
	 * file menu
	 */
	private JMenu fileMenu;
	/**
	 * help menu item
	 */
	private JMenuItem helpMI;
	/**
	 * help menu
	 */
	private JMenu helpMenu;
	/**
	 * menu separator
	 */
	private JSeparator fileMenuSeparator;
	/**
	 * main menu bar
	 */
	private JMenuBar mainMenuBar;
	/**
	 * table where records are shown
	 */
	private JTable mainTable;
	/**
	 * popup book menu item
	 */
	private JMenuItem popBookMI;
	/**
	 * popup create menu item
	 */
	private JMenuItem popCreateMI;
	/**
	 * popup delete menu item
	 */
	private JMenuItem popDeleteMI;
	/**
	 * popup edit menu item
	 */
	private JMenuItem popEditMI;
	/**
	 * popup find menu item
	 */
	private JMenuItem popFindMI;
	/**
	 * popup menu
	 */
	private JPopupMenu popupMenu;
	/**
	 * status label
	 */
	private JLabel statusLabel;
	/**
	 * status panel
	 */
	private JPanel statusPane;
	/**
	 * scroll panel
	 */
	private JScrollPane tableScrollPane;

	/**
	 * Creates new <code>ClientWindow<code> object.
	 * 
	 * @param application
	 *         reference to the application object
	 */
	public ClientWindow(Application application) {
		super(application);
		initComponents();
		initOpenListener();
	}

	/**
	 * {@inheritDoc} <br>
	 * Updates window content when application settings are changed.
	 */
	public void updateWindowContent() {
		String dbURL = application.getDBUrl();
		statusLabel.setText(dbURL == null ? "disconnected from database"
				: "connected to database on [" + dbURL + "]");
		initTableFill();
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
				updateWindowContent();
				optionsMIActionPerformed();
			}
		});
	}

	/**
	 * Initializes frame components.
	 */
	private void initComponents() {

		popupMenu = new JPopupMenu();
		popBookMI = new JMenuItem();
		popCreateMI = new JMenuItem();
		popEditMI = new JMenuItem();
		popDeleteMI = new JMenuItem();
		popFindMI = new JMenuItem();
		tableScrollPane = new JScrollPane();
		mainTable = new JTable();
		statusPane = new JPanel();
		statusLabel = new JLabel();
		mainMenuBar = new JMenuBar();
		fileMenu = new JMenu();
		fileMenuSeparator = new JSeparator();
		exitMI = new JMenuItem();
		actionMenu = new JMenu();
		bookMI = new JMenuItem();
		createMI = new JMenuItem();
		editMI = new JMenuItem();
		deleteMI = new JMenuItem();
		findMI = new JMenuItem();
		helpMenu = new JMenu();
		helpMI = new JMenuItem();
		aboutMI = new JMenuItem();
		optionsMI = new JMenuItem();
		popBookMI.setText("Book");
		popupMenu.add(popBookMI);
		popCreateMI.setText("Create");
		popupMenu.add(popCreateMI);
		popEditMI.setText("Edit");
		popupMenu.add(popEditMI);
		popDeleteMI.setText("Delete");
		popupMenu.add(popDeleteMI);
		popFindMI.setText("Find");
		popupMenu.add(popFindMI);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainTable.setModel(new RecordTableModel());
		mainTable.setComponentPopupMenu(popupMenu);
		mainTable.setFillsViewportHeight(true);
		mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mainTable.setRowSelectionAllowed(true);
		tableScrollPane.setViewportView(mainTable);
		getContentPane().add(tableScrollPane, BorderLayout.CENTER);
		statusPane.setPreferredSize(new Dimension(400, 16));
		statusPane.setLayout(new BorderLayout());
		statusLabel.setText("");
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
		statusPane.add(statusLabel, BorderLayout.CENTER);
		getContentPane().add(statusPane, BorderLayout.SOUTH);
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
		bookMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				InputEvent.CTRL_MASK));
		bookMI.setText("Book/Unbook");
		actionMenu.add(bookMI);
		createMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				InputEvent.ALT_MASK | InputEvent.CTRL_MASK));
		createMI.setText("Create");
		actionMenu.add(createMI);
		editMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				InputEvent.CTRL_MASK));
		editMI.setText("Edit");
		actionMenu.add(editMI);
		deleteMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				InputEvent.CTRL_MASK));
		deleteMI.setText("Delete");
		actionMenu.add(deleteMI);
		findMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				InputEvent.CTRL_MASK));
		findMI.setText("Find");
		actionMenu.add(findMI);
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
		setupMenuActionListeners();
	}

	/**
	 * Fills table with records.
	 */
	private void initTableFill() {
		TableModel model = null;
		try {
			model = this.getRecords();
		} catch (GuiException e) {
			model = new RecordTableModel();
		} finally {
			this.mainTable.setModel(model);
			updateRowSelection(0);
		}
	}

	/**
	 * Initializes action listeners for menu items.<br>
	 * Menu items need action listeners to work properly.
	 */
	private void setupMenuActionListeners() {
		BookUnbookActionListener bal = new BookUnbookActionListener();
		bookMI.addActionListener(bal);
		popBookMI.addActionListener(bal);
		CreateActionListener cal = new CreateActionListener();
		createMI.addActionListener(cal);
		popCreateMI.addActionListener(cal);
		EditActionListener eal = new EditActionListener();
		editMI.addActionListener(eal);
		popEditMI.addActionListener(eal);
		DeleteActionListener dal = new DeleteActionListener();
		deleteMI.addActionListener(dal);
		popDeleteMI.addActionListener(dal);
		FindActionListener fal = new FindActionListener();
		findMI.addActionListener(fal);
		popFindMI.addActionListener(fal);
	}

	/**
	 * Shows options dialog.<br>
	 * User can change options while application runs.
	 */
	private void optionsMIActionPerformed() {
		if (application.showOptionsDialog()) {
			application.cleanUp();
			application.applyOptions();
		}
	}

	/**
	 * Closes application.
	 */
	private void exitMIActionPerformed() {
		this.close();
	}

	/**
	 * Shows application help.
	 */
	private void helpMIActionPerformed() {
		String fileName = (application.getApplicationType() == ApplicationType.STANDALONE ? "alone.htm"
				: "client.htm");
		fileName = "docs/user_guide/" + fileName;
		JOptionPane.showMessageDialog(this, "See help file at " + fileName, "Help",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shows about dialog.
	 */
	private void aboutMIActionPerformed() {
		JOptionPane.showMessageDialog(this,
				"Client Bodgitt and Scarper, LLC.\nPetr Shilkin 2009", "About",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Requests database for records in database that matches search criteria.
	 * 
	 * @param criteriaArr
	 *         search criteria array of string
	 * @param exChecks
	 *         exact search flags for name and location fields, can be
	 *         <code>null</code> if no exact search requested
	 * @return table model to use in {@link JTable} component
	 * @throws GuiException
	 *          on any error
	 */
	private RecordTableModel findRecords(String[] criteriaArr, boolean[] exChecks)
			throws GuiException {
		try {
			RecordTableModel out = new RecordTableModel();
			long[] res = application.getDBAccess().findByCriteria(criteriaArr);
			nextRecNo: for (long recNo : res) {
				try {
					String[] dataArr = readRecord(recNo);
					if (exChecks != null) {
						for (int i = 0; i < exChecks.length; i++) {
							String data = dataArr[i];
							if (exChecks[i] && !data.equals(criteriaArr[i])) {
								continue nextRecNo;
							}
						}
					}
					out.addRecord(recNo, dataArr);
				} catch (Exception e) {
					log.log(Level.SEVERE, "record already not found " + recNo);
				}
			}
			return out;
		} catch (Exception e) {
			throw new GuiException(e);
		}
	}

	/**
	 * Requests database for all records in database.
	 * 
	 * @return table model to use in {@link JTable} component
	 * @throws GuiException
	 *          on any error
	 */
	private RecordTableModel getRecords() throws GuiException {
		return findRecords(new String[DBAccess.FIELD_SEQUENCE.length], null);
	}

	/**
	 * Requests database to remove record with number <code>recNo</code>.
	 * 
	 * @param recNo
	 *         record number
	 * @param lockCookie
	 *         locking cookie
	 *@throws GuiException
	 *          on any error
	 */
	private void deleteRecord(long recNo, long lockCookie) throws GuiException {
		try {
			application.getDBAccess().deleteRecord(recNo, lockCookie);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error occured during deleting record " + recNo, e);
			throw new GuiException(e);
		}
	}

	/**
	 * Requests database for record creation with specified data.
	 * 
	 * @param data
	 *         field's array of the new record
	 * @throws GuiException
	 *          on any error
	 */
	private long createRecord(String[] data) throws GuiException {
		try {
			return application.getDBAccess().createRecord(data);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error occured during creating record", e);
			throw new GuiException(e);
		}
	}

	/**
	 * Request database to lock record with number <code>recNo</code>.
	 * 
	 * @throws GuiException
	 *          on any error
	 */
	private long lockRecord(long recNo) throws GuiException {
		try {
			return application.getDBAccess().lockRecord(recNo);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error occured during locking record " + recNo, e);
			throw new GuiException(e);
		}
	}

	/**
	 * Request database to unlock record using record number <code>recNo</code>,
	 * and locking descriptor <code>lockCookie</code>.
	 * 
	 * @param recNo
	 *         record number
	 * @param lockCookie
	 *         locking descriptor
	 * @throws GuiException
	 *          on any error
	 */
	private void unlockRecord(long recNo, long lockCookie) throws GuiException {
		try {
			application.getDBAccess().unlock(recNo, lockCookie);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error occured during record unlock " + recNo, e);
			throw new GuiException(e);
		}
	}

	/**
	 * Request database to read record using record number <code>recNo</code>.
	 * 
	 * @param recNo
	 *         record number
	 * @return fields of record
	 * @throws GuiException
	 *          on any error
	 */
	private String[] readRecord(long recNo) throws GuiException {
		try {
			String[] data = application.getDBAccess().readRecord(recNo);
			return data;
		} catch (Exception e) {
			log.log(Level.SEVERE, "error occured during record read " + recNo, e);
			throw new GuiException(e);
		}
	}

	/**
	 * Request database to update record with record number <code>recNo</code>,
	 * using modified fields <code>data</code> and locking descriptor
	 * <code>lockCookie</code>.
	 * 
	 * @param recNo
	 *         record number of record
	 * @param data
	 *         modified fields of record
	 * @param lockCookie
	 *         locking descriptor
	 * @throws GuiException
	 *          on any error
	 */
	private void updateRecord(long recNo, String[] data, long lockCookie)
			throws GuiException {
		try {
			application.getDBAccess().updateRecord(recNo, data, lockCookie);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error occured during record update " + recNo, e);
			throw new GuiException(e);
		}
	}

	/**
	 * Updates row selection in table after table model changed
	 * 
	 * @param rowNo
	 *         preferred row number
	 */
	private void updateRowSelection(int rowNo) {
		int rcount = 0;
		if ((rcount = mainTable.getModel().getRowCount()) > 0) {
			if (rcount > rowNo) {
				mainTable.getSelectionModel().setSelectionInterval(rowNo, rowNo);
			} else {
				mainTable.getSelectionModel().setSelectionInterval(rcount - 1, rcount - 1);
			}
		}
	}

	/*
	 * ACTION LISTENERS CLASSES FOR MENU ITEMS
	 */
	/**
	 * ActionListener class for Book (Unbook) operation.
	 */
	private class BookUnbookActionListener implements ActionListener {
		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			int row;
			if ((row = mainTable.getSelectedRow()) != -1) {
				row = mainTable.convertRowIndexToModel(row);
				RecordTableModel tableData = (RecordTableModel) mainTable.getModel();
				long recNo = tableData.getRecNoByRow(row);
				long lockCookie = 0;
				try {
					int res = JOptionPane.showConfirmDialog(ClientWindow.this,
							"Do you really want to book/unbook record", "Are you sure?",
							JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.YES_OPTION) {
						lockCookie = lockRecord(recNo);
						String[] data = readRecord(recNo);
						if (OperationDialogPanel.showDialog(data, DBAccess.FIELD_SEQUENCE,
								OperationType.BOOK)) {
							updateRecord(recNo, data, lockCookie);
							tableData.update(row, readRecord(recNo));
						}
					}
				} catch (GuiException ex) {
					application.handleException("Error occured when book/unbook record\n"
							+ ex.getMessage());
				} finally {
					if (lockCookie != 0) {
						try {
							unlockRecord(recNo, lockCookie);
						} catch (GuiException ex) {
							application.handleException("Error occured when unlocking record\n"
									+ ex.getMessage());
						}
					}
				}
			}
		}
	}

	/**
	 * ActionListener class for Edit (Modify) operation.
	 */
	private class EditActionListener implements ActionListener {
		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			int row;
			if ((row = mainTable.getSelectedRow()) != -1) {
				row = mainTable.convertRowIndexToModel(row);
				RecordTableModel tableData = (RecordTableModel) mainTable.getModel();
				long recNo = tableData.getRecNoByRow(row);
				long lockCookie = 0;
				try {
					int res = JOptionPane.showConfirmDialog(ClientWindow.this,
							"Do you really want to edit record", "Are you sure?",
							JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.YES_OPTION) {
						lockCookie = lockRecord(recNo);
						String[] data = readRecord(recNo);

						if (OperationDialogPanel.showDialog(data, DBAccess.FIELD_SEQUENCE,
								OperationType.MODIFY)) {
							updateRecord(recNo, data, lockCookie);
							tableData.update(row, readRecord(recNo));
						}
					}
				} catch (GuiException ex) {
					application.handleException("Error occured when editing record\n"
							+ ex.getMessage());
				} finally {
					if (lockCookie != 0) {
						try {
							unlockRecord(recNo, lockCookie);
						} catch (GuiException ex) {
							application.handleException("Error occured when unlocking record\n"
									+ ex.getMessage());
						}
					}
				}
			}
		}
	}

	/**
	 * ActionListener class for Create operation.
	 */
	private class CreateActionListener implements ActionListener {
		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				int res = JOptionPane.showConfirmDialog(ClientWindow.this,
						"Do you really want to create record", "Are you sure?",
						JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.YES_OPTION) {
					String[] data = new String[DBAccess.FIELD_SEQUENCE.length];
					if (OperationDialogPanel.showDialog(data, DBAccess.FIELD_SEQUENCE,
							OperationType.CREATE)) {
						long recNo = createRecord(data);
						data = readRecord(recNo);
						RecordTableModel tableData = (RecordTableModel) mainTable.getModel();
						tableData.addRecord(recNo, data);
					}
				}
			} catch (GuiException ex) {
				application.handleException("Error occured when creating record\n"
						+ ex.getMessage());
			}
		}
	}

	/**
	 * AñtionListener class for Delete operation.
	 */
	private class DeleteActionListener implements ActionListener {
		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			int row;
			if ((row = mainTable.getSelectedRow()) != -1) {
				row = mainTable.convertRowIndexToModel(row);
				RecordTableModel tableData = (RecordTableModel) mainTable.getModel();
				long recNo = tableData.getRecNoByRow(row);
				long lockCookie = 0;
				try {
					int res = JOptionPane.showConfirmDialog(ClientWindow.this,
							"Do you really want to delete record", "Are you sure?",
							JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.YES_OPTION) {
						lockCookie = lockRecord(recNo);
						deleteRecord(recNo, lockCookie);
						tableData.deleteRow(row);
						updateRowSelection(row);
					}
				} catch (GuiException ex) {
					application.handleException("Error occured when deleting record\n"
							+ ex.getMessage());
				} finally {
					if (lockCookie != 0) {
						try {
							unlockRecord(recNo, lockCookie);
						} catch (GuiException ex) {
							application.handleException("Error occured when unlocking record\n"
									+ ex.getMessage());
						}
					}
				}
			}
		}
	}

	/**
	 * ActionListener class for Find operation.
	 */
	private class FindActionListener implements ActionListener {
		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			int res = JOptionPane.showConfirmDialog(ClientWindow.this,
					"Do you really want to find record", "Are you sure?",
					JOptionPane.YES_NO_OPTION);
			try {
				if (res == JOptionPane.YES_OPTION) {
					final boolean[] checks = new boolean[] { false, false };
					String[] data = new String[DBAccess.FIELD_SEQUENCE.length];
					if (OperationDialogPanel.showDialog(data, DBAccess.FIELD_SEQUENCE, checks,
							OperationType.FIND)) {
						TableModel tableData = findRecords(data, checks);
						mainTable.setModel(tableData);
						updateRowSelection(0);
					}
				}
			} catch (GuiException ex) {
				application.handleException("Error occured finding records\n"
						+ ex.getMessage());
			}
		}
	}
}
