package suncertify.db;

/**
 * Interface <code>DBAccess</code> is a Data Access Interface<br>
 * Classes which work with the database must implement this interface.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public interface DBAccess {
	/**
	 * Field sequence of database record. Array of field names.
	 */
	String[] FIELD_SEQUENCE = new String[] { "name", "location", "specialties",
			"size", "rate", "owner" };
	/**
	 * Array defines length for each field in field sequence
	 */
	int[] FIELD_LENGTH = new int[] { 32, 64, 64, 6, 8, 8 };

	/**
	 * Reads a record from the file. Returns an array where each element is a
	 * record value.
	 * 
	 * @param recNo
	 *         number of record in database file
	 * @return String[] values of record with number <code>recNo</code>
	 * @throws RecordNotFoundException
	 *          record with specified number <code>recNo</code> not found
	 */
	public String[] readRecord(long recNo) throws RecordNotFoundException;

	/**
	 * Modifies the fields of a record. The new value for field n appears in
	 * data[n]. Throws <code>SecurityException</code> if the record is locked with
	 * a cookie other than <code>lockCookie</code>.
	 * 
	 * @param recNo
	 *         number of record
	 * @param data
	 *         values for fields of record
	 * @param lockCookie
	 *         locking descriptor
	 * @throws RecordNotFoundException
	 *          record with specified number <code>recNo</code> not found
	 * @throws SecurityException
	 *          invalid locking descriptor <code>lockCookie</code>
	 */
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException;

	/**
	 * Deletes a record, making the record number and associated disk storage
	 * available for reuse. Throws <code>SecurityException</code> if the record is
	 * locked with a cookie other than <code>lockCookie</code>.
	 * 
	 * @param recNo
	 *         number of record
	 * @param lockCookie
	 *         locking descriptor
	 * @throws RecordNotFoundException
	 *          record with specified number <code>recNo</code> not found
	 * @throws SecurityException
	 *          invalid locking descriptor <code>lockCookie</code>
	 */
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException;

	/**
	 * Returns an array of record numbers that match the specified criteria. Field
	 * n in the database file is described by criteria[n]. A null value in
	 * criteria[n] matches any field value. A non-null value in criteria[n] matches
	 * any field value that begins with criteria[n]. (For example, "Fred" matches
	 * "Fred" or "Freddy".)
	 * 
	 * @param criteria
	 *         criteria to search records
	 * @return numbers of records matching criteria
	 */
	public long[] findByCriteria(String[] criteria);

	/**
	 * Creates a new record in the database (possibly reusing a deleted entry).
	 * Inserts the given data, and returns the record number of the new record.
	 * 
	 * @param data
	 *         values for fields of new record
	 * @return record number of newly created record
	 * @throws DuplicateKeyException
	 *          record number is already in use
	 */
	public long createRecord(String[] data) throws DuplicateKeyException;

	/**
	 * Locks a record so that it can only be updated or deleted by this client.
	 * Returned value is a cookie that must be used when the record is unlocked,
	 * updated, or deleted. If the specified record is already locked by a
	 * different client, the current thread gives up the CPU and consumes no CPU
	 * cycles until the record is unlocked.
	 * 
	 * @param recNo
	 *         number of record
	 * @return value of locking descriptor
	 * @throws RecordNotFoundException
	 *          record with specified number <code>recNo</code> not found
	 */
	public long lockRecord(long recNo) throws RecordNotFoundException;

	/**
	 * Releases the lock on a record. Cookie must be the cookie returned when the
	 * record was locked; otherwise throws <code>SecurityException</code>.
	 * 
	 * @param recNo
	 *         record number
	 * @param cookie
	 *         locking descriptor
	 * @throws SecurityException
	 *          invalid locking descriptor <code>cookie</code>
	 */
	public void unlock(long recNo, long cookie) throws SecurityException;
}
