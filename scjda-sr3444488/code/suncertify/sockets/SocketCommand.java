package suncertify.sockets;

/**
 * The enumerated list of possible commands we can send from the client to the
 * server.<br>
 * Each of the commands represents a certain action for database, that is
 * operated remotely:
 * <ul>
 * <li>Find - finds matching records</li>
 * <li>Add - creates and persists new record</li>
 * <li>Delete - deletes the specified record</li>
 * <li>Modify - changes field of the specified record</li>
 * <li>Read - reads the specified record</li>
 * <li>Lock - locks the specified record for changing</li>
 * <li>Unlock - unlocks the specified record after locking</li>
 * </ul>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public enum SocketCommand {
	/** indicates that the command object has not been set. */
	UNSPECIFIED,
	/** request will be performing a Find action. */
	FIND,
	/** request will be performing a Create action. */
	ADD,
	/** request will be performing a Delete action. */
	DELETE,
	/** request will be performing a Update action. */
	MODIFY,
	/** request will be performing a Lock action. */
	LOCK,
	/** request will be performing a Unlock action. */
	UNLOCK,
	/** request will be performing a Read action. */
	READ
}
