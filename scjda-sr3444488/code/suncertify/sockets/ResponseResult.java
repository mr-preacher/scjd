package suncertify.sockets;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class <code>ResponseResult</code> purposed for data transfer between
 * {@link RecordSocketServer} and {@link RecordSocketClient}.<br>
 * It wraps transferred data object of response, which can be one of:
 * <ul>
 * <li>Exception, that may occur on the server side</li>
 * <li>Array of long, that used to represent numbers of the found records</li>
 * <li>Array of String, that used to represent record fields</li>
 * <li>Long, that used to represent record number</li>
 * </ul>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class ResponseResult implements Serializable {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * An internal reference to a transferring data object.
	 */
	private Object data = null;

	/**
	 * Constructor of <code>ResponseResult</code> object.<br>
	 * 
	 * @param o
	 *         transferring data object to wrap
	 */
	public ResponseResult(Object o) {
		if (!(o instanceof Exception || o instanceof long[] || o instanceof String[] || o instanceof Long)) {
			throw new IllegalArgumentException(
					"incompatible transferring data object passed");
		}
		this.data = o;
	}

	/**
	 * Return the array of long as transfered data.
	 * 
	 * @return array of long if transferred object is array of long
	 */
	public long[] getLongArray() {
		return (long[]) data;
	}

	/**
	 * Returns the array of String as transfered data.
	 * 
	 * @return array of String if transferred object is array of String
	 */
	public String[] getStringArray() {
		return (String[]) data;
	}

	/**
	 * Returns long as a transferred data.
	 * 
	 * @return long object if transfered object is {@link Long}
	 */
	public long getLong() {
		return (Long) data;
	}

	/**
	 * Returns exception as a transferred data.
	 * 
	 * @return exception object if transferred object is {@link Exception}
	 */
	public Exception getException() {
		return (Exception) this.data;
	}

	/**
	 * Returns <code>true</code> if transferred data is {@link Exception}
	 * 
	 * @return <code>true</code> if transferred object is exception,
	 *         <code>false</code> if transferred object is not exception
	 */
	public boolean isException() {
		return data instanceof Exception;
	}

	/**
	 * Creates a string representing this result for debugging and logging
	 * purposes.
	 * 
	 * @return a string representing this {@link ResponseResult}
	 */
	public String toString() {
		if (data instanceof Object[]) {
			return Arrays.toString((Object[]) data);
		} else if (data != null) {
			return data.toString();
		}
		return "null";
	}
}
