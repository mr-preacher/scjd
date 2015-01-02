package suncertify.gui.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Class <code>CustomDialog</code> provides functionality of a modal dialog with
 * two options represented with buttons.<br>
 * When user clicks button OK validation of the text fields starts and dialog
 * cannot be closed until validation is passed. <br>
 * When user clicks button Cancel or closes dialog, dialog just closes. Dialog
 * uses panel as its content pane. This panel may contain many
 * {@link ValidatingTextField}, the values of those are supposed to be
 * validated.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class CustomDialog extends JDialog {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;
	/**
	 * Option pane used in dialog
	 */
	private JOptionPane optionPane;
	/**
	 * Content pane of dialog
	 */
	private final JPanel contentPane;
	/**
	 * OK option
	 */
	final String okBtnString = "OK";
	/**
	 * array of options: OK and Cancel
	 */
	final Object[] options = { okBtnString, "Cancel" };
	/**
	 * flag that defines that OK option is chosen
	 */
	private boolean pressedOK = false;

	/**
	 * Constructor of dialog with empty title.<br>
	 * Creates and shows the modal dialog with two options.
	 * 
	 * @param contentPane
	 *         panel that is used as a content pane of dialog
	 */
	public CustomDialog(JPanel contentPane) {
		this("", contentPane);
	}

	/**
	 * Constructor of dialog with a specified title.<br>
	 * Creates and shows the modal dialog with two options.
	 * 
	 * @param title
	 *         specified title of dialog
	 * 
	 * @param contentPane
	 *         panel that is used as a content pane of dialog
	 * 
	 */
	public CustomDialog(String title, JPanel contentPane) {
		this.setTitle(title);
		this.contentPane = contentPane;

		optionPane = new JOptionPane(contentPane, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_OPTION, null, options, okBtnString);
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {

			/**
			 * {@inheritDoc} <br>
			 * Depending on which button is clicked OK or Cancel validation of text
			 * values in the text fields starts or dialog is just closing
			 */
			public void propertyChange(PropertyChangeEvent evt) {
				String prop = evt.getPropertyName();
				if (isVisible()
						&& (evt.getSource() == optionPane)
						&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
								.equals(prop))) {
					Object value = optionPane.getValue();
					if (value == JOptionPane.UNINITIALIZED_VALUE) {
						return;
					}
					optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					if (okBtnString.equals(value)) {
						if (!validateValues())
							return;
						pressedOK = true;
						setVisible(false);
						dispose();
					} else {
						pressedOK = false;
						setVisible(false);
						dispose();
					}
				}
			}
		});
		this.setContentPane(optionPane);

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			/**
			 * Handles click on the Close button of the system menu of dialog
			 */
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window, we're going to change the
				 * JOptionPane's value property.
				 */
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});
		pack();
		centerDialog();
		setModal(true);
		setVisible(true);
	}

	/**
	 * Returns <code>true</code> if user clicked button OK
	 * 
	 * @return <code>true</code> if user clicked OK
	 */
	public boolean isOKPressed() {
		return pressedOK;
	}

	/**
	 * Validates values in text fields
	 * 
	 * @return <code>true</code> if all values in {@link ValidatingTextField} is
	 *         validated, <code>false</code> if validation of some of the values in
	 *         {@link ValidatingTextField} is failed.
	 */
	private boolean validateValues() {
		Component[] comps = this.contentPane.getComponents();
		for (Component comp : comps) {
			if (comp.isVisible() && comp instanceof ValidatingTextField) {
				if (!((ValidatingTextField) comp).hasValidValue()) {
					comp.requestFocusInWindow();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Method performs clean up for components {@link ValidatingTextField} because
	 * some times they cannot be finalized.
	 */
	public void dispose() {
		super.dispose();
		Component[] comps = this.contentPane.getComponents();
		for (Component comp : comps) {
			if (comp instanceof ValidatingTextField) {
				((ValidatingTextField) comp).finalize();
			}
		}
	}

	/**
	 * Centers dialog on the screen<br>
	 * This method should be called after pack method
	 */
	private void centerDialog() {
		Dimension dd = this.getSize();
		Dimension sd = this.getToolkit().getScreenSize();
		this.setLocation((sd.width - dd.width) / 2, (sd.height - dd.height) / 2);
	}
}
