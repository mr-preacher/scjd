package suncertify.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import suncertify.gui.utils.CustomDialog;
import suncertify.gui.utils.JRegexTextField;

/**
 * Class <code>OperationDialogPanel</code> extends <code>JPanel</code> Swing
 * class to provide specific panel that has text edit controls used for data
 * editing. <br>
 * Dialog that contains this panel can be displayed when some database operation
 * are needed to be done.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class OperationDialogPanel extends JPanel {
	/**
	 * Enumeration of modes (types) of data editing.
	 */
	public enum OperationType {
		/**
		 * Mode for which text edit controls of Find operation must be displayed only.
		 */
		FIND,
		/**
		 * Mode for which text edit controls of Modify operation must be displayed.
		 * only
		 */
		MODIFY,
		/**
		 * Mode for which text edit controls of Book operation must be displayed only.
		 */
		BOOK,
		/**
		 * Mode for which text edit controls of Create operation must be displayed.
		 * only
		 */
		CREATE
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
	 * Mapping constant for text edit control "owner"
	 */
	private static final String OWNER = "owner";
	/**
	 * Mapping constant for text edit control "rate"
	 */
	private static final String RATE = "rate";
	/**
	 * Mapping constant for text edit control "size"
	 */
	private static final String SIZE = "size";
	/**
	 * Mapping constant for text edit control "specialties"
	 */
	private static final String SPECIALTIES = "specialties";
	/**
	 * Mapping constant for text edit control "location"
	 */
	private static final String LOCATION = "location";
	/**
	 * Mapping constant for text edit control "name"
	 */
	private static final String NAME = "name";

	/**
	 * check box for exact "by name" search
	 */
	private JCheckBox checkName;
	/**
	 * check box for exact "by location" search
	 */
	private JCheckBox checkLoc;
	/**
	 * label for name field
	 */
	private JLabel labName;
	/**
	 * label for location field
	 */
	private JLabel labLoc;
	/**
	 * label for specialties field
	 */
	private JLabel labSpec;
	/**
	 * label for size field
	 */
	private JLabel labSize;
	/**
	 * label for rate field
	 */
	private JLabel labRate;
	/**
	 * label for owner field
	 */
	private JLabel labOwnr;
	/**
	 * text edit control for namJ field
	 */
	private JTextField fieldName;
	/**
	 * text edit control for location field
	 */
	private JTextField fieldLoc;
	/**
	 * text edit control for specialties field
	 */
	private JTextField fieldSpec;
	/**
	 * text edit control for size field
	 */
	private JTextField fieldSize;
	/**
	 * text edit control for rate field
	 */
	private JTextField fieldRate;
	/**
	 * text edit control for owner field
	 */
	private JTextField fieldOwnr;

	/**
	 * Mapping of text edit controls and constants, i.e. field names
	 */
	private Map<String, JTextField> mapping = new HashMap<String, JTextField>();

	/**
	 * Constructor of <code>OperationDialogPanel</code> object.<br>
	 * Some text edit controls can be displayed or not, it depends on the type of
	 * operation:
	 * <ul>
	 * <li><code>OperationType.FIND</code> displays all text edit controls and
	 * check boxes for "exact" search functionality.</li>
	 * <li><code>OperationType.BOOK</code> displays only text edit control for
	 * owner field <code>owner</code>.</li>
	 * <li><code>OperationType.CREATE</code> and <code>OperationType.MODIFY</code>
	 * check boxes and all text edit controls except text edit control for owner
	 * field are not displayed.</li>
	 * </ul>
	 * 
	 * @param type
	 *         type of operation
	 */
	public OperationDialogPanel(OperationType type) {
		initComponents();
		initMapping();
		initToolTips();
		switch (type) {
		case CREATE:
		case MODIFY:
			checkName.setVisible(false);
			checkLoc.setVisible(false);
			labOwnr.setVisible(false);
			fieldOwnr.setVisible(false);
			break;
		case BOOK:
			labName.setVisible(false);
			fieldName.setVisible(false);
			checkName.setVisible(false);
			labLoc.setVisible(false);
			fieldLoc.setVisible(false);
			checkLoc.setVisible(false);
			labSpec.setVisible(false);
			fieldSpec.setVisible(false);
			labSize.setVisible(false);
			fieldSize.setVisible(false);
			labRate.setVisible(false);
			fieldRate.setVisible(false);
			break;
		}
	}

	/**
	 * Returns text values of the text edit controls according to field names
	 * constants.
	 * 
	 * @param fieldNames
	 *         array of field names
	 * @return array of String values in text controls according to field names
	 */
	public String[] getTextValues(String[] fieldNames) {
		String[] res = new String[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			JTextComponent comp = mapping.get(fieldName);
			if (comp == null)
				continue;
			res[i] = comp.getText();

		}
		return res;
	}

	/**
	 * Sets text values to text edit controls according to field names array.
	 * 
	 * @param values
	 *         value to set to the text controls
	 * @param fieldNames
	 *         array of field names, defining sequence of values
	 */
	public void setTextValues(String[] values, String[] fieldNames) {
		if (values.length != fieldNames.length)
			throw new IllegalArgumentException("invalid fieldname count ");
		for (int i = 0; i < fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			JTextField comp = mapping.get(fieldName);
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
	 * Returns values of checks.
	 * 
	 * @return array of boolean according check boxes
	 */
	public boolean[] getFlagsValues() {
		return new boolean[] { checkName.isSelected(), checkLoc.isSelected() };
	}

	/**
	 * Shows dialog containing text edit controls filled with <code>values</code>.<br>
	 * Array <code>fields</code> defines the sequence of values in the
	 * <code>values</code> array.<br>
	 * Dialog has two options to allow user commit or cancel possible changes.<br>
	 * 
	 * @param values
	 *         text values to display in text edit controls, elements of array can
	 *         be changed
	 * @param fields
	 *         field names defining the sequence of text values
	 * @param type
	 *         operation type of current operation
	 * @return <code>true</code> if user clicked OK to commit changes,
	 *         <code>false</code> if user is canceled changes
	 */
	public static boolean showDialog(final String[] values, final String[] fields,
			OperationType type) {
		boolean[] b = new boolean[] { false, false };
		return showDialog(values, fields, b, type);
	}

	/**
	 * Shows dialog containing text edit controls filled with <code>values</code>.<br>
	 * Array <code>fields</code> defines the sequence of values in the
	 * <code>values</code> array.<br>
	 * Dialog has two options to allow user commit or cancel possible changes.<br>
	 * 
	 * @param values
	 *         text values to display in text edit controls, elements of array can
	 *         be changed
	 * @param fields
	 *         field names defining the sequence of text values
	 * @param check
	 *         check box values which define "exact" search conditions, elements of
	 *         array can be changed
	 * @param type
	 *         operation type for current operation
	 * @return <code>true</code> if user clicked OK to commit changes,
	 *         <code>false</code> if user is canceled changes
	 */
	public static boolean showDialog(final String[] values, final String[] fields,
			final boolean[] check, OperationType type) {
		if (check == null || check.length != 2)
			throw new IllegalArgumentException(
					"check must be not null. array of 2 boolean expected");
		OperationDialogPanel dialogPane = new OperationDialogPanel(type);
		String title = null;
		switch (type) {
		case BOOK:
			title = "Book";
			break;
		case FIND:
			title = "Find";
			break;
		case CREATE:
			title = "Create";
			break;
		case MODIFY:
			title = "Modify";
			break;
		}
		dialogPane.setTextValues(values, fields);
		CustomDialog cd = new CustomDialog(title, dialogPane);
		if (cd.isOKPressed()) {
			System.arraycopy(dialogPane.getTextValues(fields), 0, values, 0,
					values.length);
			System.arraycopy(dialogPane.getFlagsValues(), 0, check, 0, check.length);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Initializes mapping for text edit controls.
	 */
	private void initMapping() {
		mapping.put(NAME, fieldName);
		mapping.put(LOCATION, fieldLoc);
		mapping.put(SPECIALTIES, fieldSpec);
		mapping.put(SIZE, fieldSize);
		mapping.put(RATE, fieldRate);
		mapping.put(OWNER, fieldOwnr);
	}

	/**
	 * Initializes tooltips for components.
	 */
	private void initToolTips() {
		fieldName.setToolTipText("string, 32 symbols maximum");
		fieldLoc.setToolTipText("string, 64 symbols maximum");
		fieldSpec.setToolTipText("comma separeted strings, 64 symbols maximum");
		fieldSize.setToolTipText("number, 6 digits maximum");
		fieldRate
				.setToolTipText("currency symbol $ at the first, then one or more digits, dot, and 2 digits after dot exactly");
		fieldOwnr.setToolTipText("number, 8 digits exactly");
		checkName
				.setToolTipText("set up check to use exact-search for name instead of start-search");
		checkLoc
				.setToolTipText("set up check to use exact-search for location instead of start-search");
	}

	/**
	 * Initializes text edit controls components and defines their position in the
	 * panel.
	 */
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		fieldName = new JRegexTextField("\\p{ASCII}{0,32}");
		checkName = new JCheckBox();
		fieldLoc = new JRegexTextField("\\p{ASCII}{0,64}");
		checkLoc = new JCheckBox();
		labName = new JLabel();
		labLoc = new JLabel();
		fieldSpec = new JRegexTextField("\\p{ASCII}{0,64}");
		fieldSize = new JRegexTextField("\\d{0,6}");
		fieldRate = new JRegexTextField("(\\$\\d+\\.\\d\\d)?");
		fieldOwnr = new JRegexTextField("(\\d{8})?");
		labSpec = new JLabel();
		labSize = new JLabel();
		labRate = new JLabel();
		labOwnr = new JLabel();
		final Insets rightInsets = new Insets(2, 10, 2, 2);
		final Insets eastInsets = new Insets(2, 2, 2, 10);
		final Insets centerInsets = eastInsets;
		setLayout(new GridBagLayout());
		labName.setText("name");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInsets;
		add(labName, gridBagConstraints);

		fieldName.setMinimumSize(new Dimension(6, 29));
		Dimension textFieldSize = new Dimension(200, 29);
		fieldName.setPreferredSize(textFieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		double horizHeight = 1.0;
		gridBagConstraints.weightx = horizHeight;
		gridBagConstraints.insets = centerInsets;
		add(fieldName, gridBagConstraints);

		checkName.setText("exact");
		Dimension checkBoxSize = new Dimension(61, 21);
		checkName.setMaximumSize(checkBoxSize);
		checkName.setMinimumSize(checkBoxSize);
		checkName.setPreferredSize(checkBoxSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.anchor = GridBagConstraints.EAST;

		gridBagConstraints.insets = eastInsets;
		add(checkName, gridBagConstraints);

		fieldLoc.setMinimumSize(textFieldSize);
		fieldLoc.setPreferredSize(textFieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horizHeight;
		gridBagConstraints.insets = centerInsets;
		add(fieldLoc, gridBagConstraints);

		checkLoc.setText("exact");
		checkLoc.setMaximumSize(checkBoxSize);
		checkLoc.setMinimumSize(checkBoxSize);
		checkLoc.setPreferredSize(checkBoxSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.insets = eastInsets;
		add(checkLoc, gridBagConstraints);

		labLoc.setText("location");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInsets;
		add(labLoc, gridBagConstraints);

		fieldSpec.setMinimumSize(textFieldSize);
		fieldSpec.setPreferredSize(textFieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horizHeight;
		gridBagConstraints.insets = eastInsets;
		add(fieldSpec, gridBagConstraints);

		fieldSize.setMinimumSize(textFieldSize);
		fieldSize.setPreferredSize(textFieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horizHeight;
		gridBagConstraints.insets = eastInsets;
		add(fieldSize, gridBagConstraints);

		fieldRate.setMinimumSize(textFieldSize);
		fieldRate.setPreferredSize(textFieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horizHeight;
		gridBagConstraints.insets = eastInsets;
		add(fieldRate, gridBagConstraints);

		fieldOwnr.setMinimumSize(textFieldSize);
		fieldOwnr.setPreferredSize(textFieldSize);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = horizHeight;
		gridBagConstraints.insets = eastInsets;
		add(fieldOwnr, gridBagConstraints);

		labSpec.setText("specialties");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInsets;
		add(labSpec, gridBagConstraints);

		labSize.setText("size");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInsets;
		add(labSize, gridBagConstraints);

		labRate.setText("rate");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInsets;
		add(labRate, gridBagConstraints);

		labOwnr.setText("owner");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = rightInsets;
		add(labOwnr, gridBagConstraints);
	}

}
