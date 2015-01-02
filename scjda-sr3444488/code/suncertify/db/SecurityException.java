package suncertify.db;

/**
 * A <code>SecurityException</code> class of exception that must be thrown when
 * user tries to modify or delete record and uses invalid locking cookie. There
 * are two reasons in that case:
 * <ul>
 * <li>User not locked record, calling previously locking routine</li>
 * <li>Locking cookie that user uses is not a valid actual cookie</li>
 * </ul>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class SecurityException extends Exception {

	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * Default constructor creates new instance of <code>SecurityException</code>
	 */
	public SecurityException() {

	}

	/**
	 * Constructor creates new instance of <code>SecurityException</code>.
	 * 
	 * @param description
	 *         description of the exception, points to the cause of exception
	 */
	public SecurityException(String description) {
		super(description);
	}
}
