package suncertify.db;

/**
 * A <code>RecordNotFoundException</code> is a class of exception that must be
 * thrown when user wants to gain access to the record with number that does not
 * exist in database file.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class RecordNotFoundException extends Exception {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * Default constructor creates new instance of
	 * <code>RecordNotFoundException</code>.
	 */
	public RecordNotFoundException() {

	}

	/**
	 * Constructor creates new instance of <code>RecordNotFoundException</code>.
	 * 
	 * @param description
	 *         description of the exception, points to the cause of exception
	 */
	public RecordNotFoundException(String description) {
		super(description);
	}
}
