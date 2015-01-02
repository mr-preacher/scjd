package suncertify.gui.utils;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * 
 * Class <code>ValidatingTextField</code> extends JTextField.<br>
 * This text field component validates text value and reflects status of
 * validation changing its background color.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class ValidatingTextField extends JTextField {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;
	/**
	 * Logger for debugging purposes
	 */
	private transient Logger log = Logger.getLogger(this.getClass().getPackage()
			.getName());
	/**
	 * flag defines the value in text field component is valid
	 */
	private boolean isValid;
	/**
	 * validation handler to validate text value is text field component
	 */
	protected ValidationHandler vh;
	/**
	 * background color of text field component that used when validation failed
	 */
	private Color faultColor;
	/**
	 * background color of text field component that used when validation passed
	 */
	private Color validColor;
	/**
	 * how much time in milliseconds the validation consumes
	 */
	private long invokeTime;
	/**
	 * thread for lazy validation
	 */
	private ValidationThread vThread;

	/**
	 * Constructor creates new instance of <code>ValidatingTextField</code>.
	 * 
	 * @param vh
	 *         validation handler that used for validation
	 */
	public ValidatingTextField(ValidationHandler vh) {
		this.vh = vh;
		initColors();
		initListeners();
		validateValue(getText());
	}

	/**
	 * Method initializes and sets listeners for text field component.
	 */
	private void initListeners() {
		AbstractDocument doc = (AbstractDocument) this.getDocument();
		doc.setDocumentFilter(new ValidationDocumentFilter());
	}

	/**
	 * Method initializes colors by default.
	 */
	private void initColors() {
		faultColor = new Color(255, 153, 153);
		validColor = super.getBackground();
	}

	/**
	 * Returns validation status of component.
	 * 
	 * @return <code>true</code> if text value in the component is validated or
	 *         <code>false</code> if validation failed
	 */
	public boolean hasValidValue() {
		refreshStatus();
		return isValid;
	}

	/**
	 * Retrieves most recent validation status.
	 */
	private void refreshStatus() {
		if (vThread != null) {
			vThread.waitValidationResult();
		}
	}

	/**
	 * Sets text value in text component and validates it.
	 * 
	 * @param value
	 *         text value to set in text component
	 */
	public void setText(String value) {
		super.setText(value);
		validateValue(value);
	}

	/**
	 * Validates text value in the component.
	 * 
	 * @param text
	 *         text value will be validated
	 */
	private void validateValue(final String text) {
		long now = System.currentTimeMillis();
		if (invokeTime < 1000) {
			validateOnFly(text);
			invokeTime = System.currentTimeMillis() - now;
		} else {
			validateLazy(text);
		}
	}

	/**
	 * On-Fly validation method.<br>
	 * Method used to validate text value when validation consumes not too much
	 * time.
	 * 
	 * @param text
	 *         text value to validate
	 */
	private void validateOnFly(final String text) {
		isValid = vh.validate(text);
		log.log(Level.OFF, "text value: " + text + " validation passed: " + isValid);
		reflectValidation();
	}

	/**
	 * Lazy validation method.<br>
	 * Method used to validate text value when validation consumes a long time.
	 * 
	 * @param text
	 *         text value to validate
	 */
	private void validateLazy(final String text) {
		if (vThread == null) {
			vThread = new ValidationThread();
		}
		vThread.offer2validate(text);
	}

	/**
	 * {@inheritDoc}<br>
	 * Clean up routine for validation field.
	 */
	public void finalize() {
		if (vThread != null) {
			vThread.finalized = true;
		}
	}

	/**
	 * Class <code>ValidationThread</code> purposed for lazy validation.<br>
	 * When text values is validating too slow, but changing too frequently (like
	 * host name) it's needed to group text values to improve performance.<br>
	 * <code>ValidationThread</code> class helps to discard "adjusting values" and
	 * perform validation for "finished values" only.
	 */
	private class ValidationThread extends Thread {
		/**
		 * last time in milliseconds when text value was offered for validation
		 */
		private long addtime;
		/**
		 * text value was offered for validation
		 */
		private String text;
		/**
		 * mutex for consuming - "operating of validation process"
		 */
		private final Object consume_mutex;
		/**
		 * mutex for producing - "returning validation results"
		 */
		private final Object produce_mutex;
		/**
		 * timeout in milliseconds for validation loop. If timeout occurs then the
		 * last offered text value will be validated
		 */
		private final int loopTimeout = 300;
		/**
		 * flag determinates that component working with thread is finalizing and
		 * thread must be stopped
		 */
		private boolean finalized;

		/**
		 * Constructor create a lazy validation thread and starts the one.
		 */
		public ValidationThread() {
			setDaemon(true);
			text = null;
			consume_mutex = new Object();
			produce_mutex = new Object();
			start();
		}

		/**
		 * {@inheritDoc} <br>
		 * Method runs lazy validation thread.
		 */
		public void run() {
			while (true) {
				while (System.currentTimeMillis() - addtime < loopTimeout || text == null) {
					synchronized (produce_mutex) {
						produce_mutex.notifyAll();
					}
					synchronized (consume_mutex) {
						try {
							consume_mutex.wait(loopTimeout);
						} catch (InterruptedException e) {
							log.log(Level.OFF, "lazy validation thread is interrupted", e);
						}
					}
					if (finalized) {
						notifyThreadWaitingResult();
						return;
					}
				}
				String text = this.text;
				this.text = null;
				validateOnFly(text);
				notifyThreadWaitingResult();
			}
		}

		/**
		 * Method notifies thread waiting for result of validation.
		 */
		private void notifyThreadWaitingResult() {
			synchronized (produce_mutex) {
				produce_mutex.notifyAll();
			}
		}

		/**
		 * Offers the text value for lazy validation. If no new text value was offered
		 * at the certain time then last offered value will be validated.
		 * 
		 * @param text
		 *         text value to validate
		 */
		public void offer2validate(String text) {
			if (text != null) {
				if (!text.equals(this.text)) {
					addtime = System.currentTimeMillis();
					this.text = text;
				}
			}
			synchronized (consume_mutex) {
				consume_mutex.notifyAll();
			}
		}

		/**
		 * Joins invoking thread until validation done.
		 */
		public void waitValidationResult() {
			synchronized (produce_mutex) {
				try {
					produce_mutex.wait();
				} catch (InterruptedException e) {
					log.log(Level.OFF,
							"thread that waiting for lazy validation thread is interrupted", e);
				}
			}
		}
	}

	/**
	 * Reflects validation status on text field component.
	 */
	private void reflectValidation() {
		this.setBackground(isValid ? validColor : faultColor);
	}

	/**
	 * Helper class that listens changes of text in text field component.
	 */
	private class ValidationDocumentFilter extends DocumentFilter {

		/**
		 * {@inheritDoc}<br>
		 * After this action new text value in the text field component will be
		 * validated.
		 */
		public void insertString(FilterBypass fb, int offset, String string,
				AttributeSet attr) throws BadLocationException {
			super.insertString(fb, offset, string, attr);
			validateValue(getText());
		}

		/**
		 * {@inheritDoc}<br>
		 * After this action new text value in the text field component will be
		 * validated.
		 */
		public void remove(FilterBypass fb, int offset, int length)
				throws BadLocationException {
			super.remove(fb, offset, length);
			validateValue(getText());
		}

		/**
		 * {@inheritDoc}<br>
		 * After this action new text value in the text field component will be
		 * validated.
		 */
		public void replace(FilterBypass fb, int offset, int length, String text,
				AttributeSet attrs) throws BadLocationException {
			super.replace(fb, offset, length, text, attrs);
			validateValue(getText());
		}
	}
}
