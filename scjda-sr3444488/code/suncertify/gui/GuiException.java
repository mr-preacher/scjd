package suncertify.gui;

/**
 * Class <code>GuiException</code> purposed to wrap all occurring exceptions,
 * which thrown on the gui side of application.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class GuiException extends Exception {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization. Not that we ever serialize this class of course, but
	 * Exception implements Serializable, so therefore by default we do as well.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * Creates a default <code>GuiException</code> instance.
	 */
	public GuiException() {
	}

	/**
	 * Creates a <code>GuiException</code> instance and chains an exception.
	 * 
	 * @param e
	 *         The exception to wrap and chain.
	 */
	public GuiException(Throwable e) {
		super(e);
	}
}
