package suncertify.gui.utils;

/**
 * Interface <code>ValidationHandler</code> purposed for the validation handling
 * of text values.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public interface ValidationHandler {
	/**
	 * Method must return <code>true</code> if text value is validated, and
	 * <code>false</code> if validation of text value failed.
	 * 
	 * @param text
	 *         text value to be validated
	 * @return <code>true</code> if text value is validated, <code>false</code> if
	 *         validation failed
	 */
	public boolean validate(String text);

}
