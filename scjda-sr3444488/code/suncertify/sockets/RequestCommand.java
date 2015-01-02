package suncertify.sockets;

import java.io.Serializable;

/**
 * Class <code>RequestCommand</code> purposed for data transfer between
 * {@link RecordSocketClient} and {@link RecordSocketServer}.<br>
 * It consists of two basic parts:
 * <ul>
 * <li>Type of the command request for remote operating of database which is
 * {@link SocketCommand}</li>
 * <li>Parameters of the command request: arrays and numbers</li>
 * </ul>
 * It wraps transferred data of request, which can be one or two of:
 * <ul>
 * <li>Array of long, that used to represent locking info for record</li>
 * <li>Array of String, that used to represent record fields</li>
 * <li>Long, that used to represent record number</li>
 * </ul>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class RequestCommand implements Serializable {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * The type of action this command object represents. This is the action that
	 * the database server will execute.
	 */
	private SocketCommand commandId;

	/**
	 * Internal reference of transferred object
	 */
	private Object data;

	/**
	 * Constructor creates <code>RequestCommand</code> object with specified
	 * <code>SocketCommand</code>.
	 * 
	 * @param commandId
	 *         type of action this command object represents
	 */
	public RequestCommand(SocketCommand commandId) {
		this.commandId = commandId;
	}

	/**
	 * Returns socket command, type of action this command represents.
	 * 
	 * @return type of action this command represents
	 */
	public SocketCommand getCommand() {
		return this.commandId;
	}

	/**
	 * Returns array of long that is a part of the parameter list for this command.
	 * 
	 * @return array of long, part of parameter list
	 */
	public long[] getLongArray() {
		if (data instanceof long[])
			return (long[]) data;
		if (data instanceof Object[])
			return (long[]) ((Object[]) data)[0];
		throw new IllegalStateException(
				"transferred object does not contains long array");
	}

	/**
	 * Returns array of String that is a part of the parameter list for this
	 * command.
	 * 
	 * @return array of String, part of parameter list
	 */
	public String[] getStringArray() {
		if (data instanceof String[])
			return (String[]) data;
		if (data instanceof Object[])
			return (String[]) ((Object[]) data)[1];
		throw new IllegalStateException(
				"transferred object does not contains string array");
	}

	/**
	 * Sets array of string as a parameter list.
	 * 
	 * @param data
	 *         array of string
	 */
	public void setArray(String[] data) {
		this.data = data;
	}

	/**
	 * Sets array of object as parameter list.<br>
	 * This method requires two arrays wrapped with one array of objects.<br>
	 * Zero element of array must be an array of long and first elements must be an
	 * array of String.
	 * 
	 * 
	 * @param data
	 *         parameter list, array of objects
	 */
	public void setArray(Object[] data) {
		if (!(data[0] instanceof long[] && data[1] instanceof String[]))
			throw new IllegalArgumentException(
					"first array must by array of long, second array must be array of string");
		this.data = data;
	}

	/**
	 * Sets array of long as a parameter list.<br>
	 * 
	 * @param data
	 *         array of long
	 */
	public void setArray(long[] data) {
		this.data = data;
	}

	/**
	 * Returns long value as a parameter value.
	 * 
	 * @return long value that is a parameter of command
	 */
	public long getLong() {
		return (Long) data;
	}

	/**
	 * Sets long value as a parameter value.
	 * 
	 * @param data
	 *         long value that is a parameter of command
	 */
	public void setLong(long data) {
		this.data = data;
	}

}
