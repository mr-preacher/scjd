package suncertify.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.DBAccess;
import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.db.SecurityException;

/**
 * Class RecordSocketClient implements <code>DBAccess</code> interface. This
 * class purposed to operate database remotely.<br>
 * Each of <code>DBAccess</code> implemented methods:
 * <ol>
 * <li>Sends {@link RequestCommand} object to the server</li>
 * <li>Receives {@link ResponseResult} object from server for each of the
 * commands {@link SocketCommand}.</li>
 * </ol>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class RecordSocketClient implements DBAccess {
	/**
	 * The logger for debugging issues
	 */
	private Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	/**
	 * The socket client that establishes the socket connection.
	 */
	private final Socket socket;
	/**
	 * The outputstream used to write a serialized object to a socket server.
	 */
	private final ObjectOutputStream oos;
	/**
	 * The inputstream used to read a serialized object (a response) from the
	 * socket server.
	 */
	private final ObjectInputStream ois;

	/**
	 * Constructor takes in a hostname or IP address of the server to connect.
	 * 
	 * @param hostname
	 *         hostname to connect to
	 * @param portNumber
	 *         number of port to connect to server
	 * @throws UnknownHostException
	 *          if unable to connect to server
	 * @throws IOException
	 *          on network error
	 */
	public RecordSocketClient(String hostname, int portNumber)
			throws UnknownHostException, IOException {
		this.socket = new Socket(hostname, portNumber);
		this.oos = new ObjectOutputStream(socket.getOutputStream());
		this.ois = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Method sends request wrapped by <code>RequestCommand</code> object to the
	 * server and gets response as wrapped by <code>ResponseResult</code> object.
	 * 
	 * @param command
	 *         request object, command to operate with database remotely
	 * @return response of server for the command
	 * @throws Exception
	 *          on any request processing error
	 */
	private ResponseResult getResultFor(RequestCommand command) throws Exception {
		try {
			oos.writeObject(command);
			ResponseResult result = (ResponseResult) ois.readObject();
			if (result == null)
				return result;
			if (!result.isException()) {
				return result;
			} else {
				throw result.getException();
			}
		} catch (ClassNotFoundException cnfe) {
			IOException ioe = new IOException("problem with demarshelling Record)");
			ioe.initCause(cnfe);
			throw ioe;
		}
	}

	/**
	 * Performs any clean-up necessary when this connection is no longer used.
	 */
	protected void finalize() {
		close();
	}

	/**
	 * A helper method which closes the socket connection. Needs to be called from
	 * within a try-catch.
	 */
	private void close() {
		try {
			oos.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "error closing output stream", e);
		}
		try {
			ois.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "error closing input stream", e);
		}
		try {
			socket.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "error closing socket", e);
		}
	}

	/**
	 * {@inheritDoc} <br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.ADD</code> as a socket command.
	 */
	public long createRecord(String[] data) throws DuplicateKeyException {
		RequestCommand command = new RequestCommand(SocketCommand.ADD);
		command.setArray(data);
		try {
			ResponseResult result = getResultFor(command);
			return result.getLong();
		} catch (DuplicateKeyException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}<br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.DELETE</code> as a socket command.
	 */
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		RequestCommand command = new RequestCommand(SocketCommand.DELETE);
		command.setArray(new long[] { recNo, lockCookie });
		try {
			getResultFor(command);
		} catch (RecordNotFoundException e) {
			throw e;
		} catch (SecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}<br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.FIND</code> as a socket command.
	 */
	public long[] findByCriteria(String[] criteria) {
		RequestCommand command = new RequestCommand(SocketCommand.FIND);
		command.setArray(criteria);
		try {
			ResponseResult result = getResultFor(command);
			return result.getLongArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}<br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.LOCK</code> as socket command.
	 */
	public long lockRecord(long recNo) throws RecordNotFoundException {
		RequestCommand command = new RequestCommand(SocketCommand.LOCK);
		command.setLong(recNo);
		try {
			ResponseResult result = getResultFor(command);
			return result.getLong();
		} catch (RecordNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}<br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.READ</code> as a socket command.
	 */
	public String[] readRecord(long recNo) throws RecordNotFoundException {
		RequestCommand command = new RequestCommand(SocketCommand.READ);
		command.setLong(recNo);
		try {
			ResponseResult result = getResultFor(command);
			return result.getStringArray();
		} catch (RecordNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * {@inheritDoc}<br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.UNLOCK</code> as a socket command.
	 */
	public void unlock(long recNo, long cookie) throws SecurityException {
		RequestCommand command = new RequestCommand(SocketCommand.UNLOCK);
		command.setArray(new long[] { recNo, cookie });
		try {
			getResultFor(command);
		} catch (SecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * {@inheritDoc}<br>
	 * Performs remote request to the server by sending
	 * <code>SocketCommand.MODIFY</code> as a socket command.
	 */
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		RequestCommand command = new RequestCommand(SocketCommand.MODIFY);
		long[] lockarr = new long[] { recNo, lockCookie };
		command.setArray(new Object[] { lockarr, data });
		try {
			getResultFor(command);
		} catch (RecordNotFoundException e) {
			throw e;
		} catch (SecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
