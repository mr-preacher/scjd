package suncertify.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;
import suncertify.gui.utils.CustomDialog;
import suncertify.gui.utils.ValidatingTextField;
import suncertify.gui.utils.ValidationHandler;

/**
 * Class <code>SettingsDialogPanel</code> extends <code>JPanel</code> and
 * behaves like {@link OperationDialogPanel}.<br>
 * Panel <code>SettingsDialogPanel</code> allows user to edit values of
 * application settings.<br>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class SettingsDialogPanel extends JPanel {
	/**
	 * Enumeration of modes (types) of settings panel representation.
	 */
	public enum SettingsType {
		/** Mode that allows only text edit controls for Client settings be shown. */
		CLIENT,
		/** Mode that allows only text edit controls for Server settings be shown. */
		SERVER,
		/** Mode that allows only text edit controls for Standalone settring be shown. */
		STANDALONE
	}

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
	 * button component for text edit control of path field
	 */
	private JButton btnPath;
	/**
	 * label component for path field
	 */
	private JLabel labPath;
	/**
	 * label component for host field
	 */
	private JLabel labHost;
	/**
	 * label component for port field
	 */
	private JLabel labPort;
	/**
	 * label component for lock timeout field
	 */
	private JLabel labLocktimeout;

	/**
	 * text edit control component for path field
	 */
	private JTextField fieldPath;

	/**
	 * text edit control component for host field
	 */
	private JTextField fieldHost;
	/**
	 * text edit control component for port field
	 */
	private JTextField fieldPort;
	/**
	 * text edit control component for lock timeout field
	 */
	private JTextField fieldLocktimeout;

	/**
	 * Mapping of text edit controls and constants
	 */
	private Map<String, JTextField> mapping;

	/**
	 * Constructor creates panel instance.<br>
	 * Some text edit controls must be hidden according the type of application.<br>
	 * Parameter type defines which of the text edit controls are hidden.
	 * 
	 * @param type
	 *         mode of the panel representation
	 */
	public SettingsDialogPanel(SettingsType type) {
		initComponents();
		initMapping();
		initToolTips();
		switch (type) {
		case CLIENT:
			labPath.setVisible(false);
			fieldPath.setVisible(false);
			btnPath.setVisible(false);
			labLocktimeout.setVisible(false);
			fieldLocktimeout.setVisible(false);
			break;
		case STANDALONE:
			labHost.setVisible(false);
			fieldHost.setVisible(false);
			labPort.setVisible(false);
			fieldPort.setVisible(false);
			break;
		default:
			labHost.setVisible(false);
			fieldHost.setVisible(false);
			break;
		}
	}
	
	/**
	 * Return text values from text edit controls. <br>
	 * Array <code>keys</code> defines the sequence of returned values.
	 * 
	 * @param keys
	 *         array that defines values sequence
	 * @return array of values
	 */
	public String[] getTextValues(String[] keys) {
		String[] res = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			String fi = keys[i];
			JTextComponent comp = mapping.get(fi);
			if (comp == null)
				continue;
			res[i] = comp.getText();
		}
		return res;
	}

	/**
	 * Sets text values to the text edit controls.<br>
	 * Array <code>values</code> contains text values the text controls will be
	 * filled with. Array <code>keys</code> defines the sequence of text values.
	 * 
	 * @param values
	 *         array of values
	 * @param keys
	 *         array that defines sequence of values
	 */
	public void setTextValues(String[] values, String[] keys) {
		if (values.length != keys.length)
			throw new IllegalArgumentException("invalid keys length");
		for (int i = 0; i < keys.length; i++) {
			String fi = keys[i];
			JTextField comp = mapping.get(fi);
			if (comp == null)
				continue;
			String value = values[i];
			if (value == null) {
				value = "";
			} else
				value = value.trim();
			comp.setText(value);
		}
	}

	/**
	 * Shows dialog accordingly application type.<br>
	 * Text edit controls will be filled with text values. <br>
	 * Dialog that contains this panel can be displayed when some application
	 * settings are needed to be done. Dialog has two options to commit or cancel
	 * possible changes.
	 * 
	 * @param values
	 *         array of String values to fill text edit controls with, elements of
	 *         array can be changed
	 * @param fields
	 *         array of String defines a sequence of text values
	 * @param type
	 *         the type of settings panel
	 * @return <code>true</code> if user clicked OK to commit changes and
	 *         <code>false</code> if user clicked cancel to discard changes
	 */
	public static boolean showDialog(final String[] values, final String[] fields,
			SettingsType type) {
		SettingsDialogPanel dialogPane = new SettingsDialogPanel(type);
		String title = null;
		switch (type) {
		case SERVER:
			title = "Server Settings";
			break;
		case CLIENT:
			title = "Client Settings";
			break;
		case STANDALONE:
			title = "Standalone Settings";
			break;
		}
		dialogPane.setTextValues(values, fields);
		CustomDialog cd = new CustomDialog(title, dialogPane);
		if (cd.isOKPressed()) {
			System.arraycopy(dialogPane.getTextValues(fields), 0, values, 0,
					values.length);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Initializes text edit control mapping.
	 */
	private void initMapping() {
		mapping = new HashMap<String, JTextField>();
		mapping.put("path", fieldPath);
		mapping.put("host", fieldHost);
		mapping.put("port", fieldPort);
		mapping.put("locktimeout", fieldLocktimeout);
	}

	/**
	 * Initializes tooltips for components.
	 */
	private void initToolTips() {
		fieldPath.setToolTipText("path to file that can be read and write");
		fieldHost.setToolTipText("existing hostname or IP adress of server");
		fieldPort.setToolTipText("number between 1024 and 65535");
		fieldLocktimeout.setToolTipText("number, 5000 minimum");
		btnPath.setToolTipText("press button to choose database file");
	}



	/**
	 * Initializes text edit controls for the settings and defines their position
	 * in the panel
	 */
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		fieldPath = new ValidatingTextField(new ValidationHandler() {
			public boolean validate(String text) {
				try {
					File f = new File(text);
					return f.canRead() && f.canWrite() && f.isFile();
				} catch (Exception e) {
					return false;
				}
			}
		});
		fieldHost = new ValidatingTextField(new ValidationHandler() {
			public boolean validate(String text) {
				try {
					if (text.trim().length() == 0)
						return false;
					InetAddress.getByName(text);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		});
		btnPath = new JButton();
		fieldPort = new ValidatingTextField(new ValidationHandler() {
			public boolean validate(String text) {
				try {
					int val = Integer.parseInt(text);
					return (val >= 1024 && val <= 65535);
				} catch (Exception e) {
					return false;
				}
			}
		});
		labPath = new JLabel();
		labHost = new JLabel();
		labPort = new JLabel();
		labLocktimeout = new JLabel();
		fieldLocktimeout = new ValidatingTextField(new ValidationHandler() {
			public boolean validate(String text) {
				try {
					int val = Integer.parseInt(text);
					return (val >= 5000);
				} catch (Exception e) {
					return false;
				}
			}
		});

		setLayout(new GridBagLayout());

		Dimension fieldSize = new Dimension(250, 26);
		fieldPath.setMinimumSize(fieldSize);
		fieldPath.setPreferredSize(fieldSize);
		fieldPath.setEditable(false);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		double horzWeight = 1.0;
		gridBagConstraints.weightx = horzWeight;
		add(fieldPath, gridBagConstraints);

		fieldHost.setMinimumSize(fieldSize);
		fieldHost.setPreferredSize(fieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horzWeight;
		add(fieldHost, gridBagConstraints);

		btnPath.setText("...");
		Dimension buttonSize = new Dimension(26, 26);
		btnPath.setMaximumSize(buttonSize);
		btnPath.setMinimumSize(buttonSize);
		btnPath.setPreferredSize(buttonSize);
		btnPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				pathBtnActionPerformed();
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		add(btnPath, gridBagConstraints);

		fieldPort.setMinimumSize(fieldSize);
		fieldPort.setPreferredSize(fieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horzWeight;
		add(fieldPort, gridBagConstraints);

		labPath.setHorizontalAlignment(SwingConstants.TRAILING);
		labPath.setText("database path");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		Insets rightInset = new Insets(0, 0, 0, 6);
		gridBagConstraints.insets = rightInset;
		add(labPath, gridBagConstraints);

		labHost.setText("server host");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInset;
		add(labHost, gridBagConstraints);

		labPort.setText("server port");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInset;
		add(labPort, gridBagConstraints);

		labLocktimeout.setText("lock timeout");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInset;
		add(labLocktimeout, gridBagConstraints);

		fieldLocktimeout.setMinimumSize(fieldSize);
		fieldLocktimeout.setPreferredSize(fieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horzWeight;
		add(fieldLocktimeout, gridBagConstraints);

	}

	/**
	 * Invokes file choose dialog to fill text edit control with appropriate path
	 * value
	 * 
	 * @throws IOException
	 *          on error of getting file path
	 */
	private void pathBtnActionPerformed() {
		JFileChooser fc = new JFileChooser();
		File curDir = new File(System.getProperty("user.dir"));
		fc.setCurrentDirectory(curDir);
		if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
			String filePath = fc.getSelectedFile().getAbsolutePath();
			String curDirPath = curDir.getAbsolutePath() + File.separator;
			if (filePath.startsWith(curDirPath)) {
				filePath = filePath.substring(curDirPath.length());
				while (filePath.startsWith(File.separator)) {
					filePath = filePath.substring(1);
				}
			}
			fieldPath.setText(filePath);
		}
	}
}
