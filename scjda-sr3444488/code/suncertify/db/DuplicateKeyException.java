package suncertify.db;

/**
 * A <code>DuplicateKeyException</code> is class of exception that must be
 * thrown when newly created record in data file has the same unique key that
 * another existing record has.<br>
 * It happens in cases when key is composite and defined by user, however it
 * almost never happens when key is a counter and defined by data access system.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class DuplicateKeyException extends Exception {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * Default constructor creates new instance of
	 * <code>DuplicateKeyException</code>.
	 */
	public DuplicateKeyException() {

	}

	/**
	 * Constructor creates new instance of <code>DuplicateKeyException</code>.
	 * 
	 * @param description
	 *         description of the exception, points to the cause of exception
	 */
	public DuplicateKeyException(String description) {
		super(description);
	}

}
