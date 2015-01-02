package suncertify.sockets;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.DBAccess;

/**
 * Class <code>RequestProcessingThread</code> is a thread class than handles
 * request processing.<br>
 * Given from client {@link RequestCommand} object is parsed, and object that
 * implements database operating interface {@link DBAccess} performs execution
 * of command. Result of the execution wrapped by {@link ResponseResult} object
 * returns to client.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class RequestProcessingThread extends Thread {
	/**
	 * Logger of the debugging issues
	 */
	private Logger log = Logger.getLogger(this.getClass().getPackage().getName());

	/**
	 * Reference to the database operating interface
	 */
	private DBAccess dbAccess;

	/**
	 * Reference for outputstream of socket connection with client
	 */
	private ObjectOutputStream out;

	/**
	 * Reference for inputstream of socket connection with client
	 */
	private ObjectInputStream in;

	/**
	 * Reference to socket that keeps connection with client
	 */
	private final Socket socket;

	/**
	 * Constructor of the <code>RequestProcessingThread</code>.
	 * 
	 * @param dba
	 *         database operating object
	 * @param socket
	 *         connection to the client
	 */
	public RequestProcessingThread(DBAccess dba, Socket socket) {
		this.dbAccess = dba;
		this.socket = socket;
		try {
			out = new ObjectOutputStream(this.socket.getOutputStream());
			in = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error getting socket streams", e);
		}
	}

	/**
	 * Overrides method <code>run()</code> to perform the request processing in a
	 * separate thread.
	 */
	public void run() {
		try {
			while (true) {
				RequestCommand cmdObj = (RequestCommand) in.readObject();
				Object respObj = execCmdObject(cmdObj);
				out.writeObject(respObj);
			}
		} catch (SocketException e) {
			log
					.log(
							Level.SEVERE,
							"socket exception occured in thread processing request. perhaps connection closed",
							e);
		} catch (EOFException e) {
			log.log(Level.SEVERE, "socket closed by client", e);
		} catch (Exception e) {
			log.log(Level.SEVERE, "general exception in thread processing request", e);
		}
		close();
	}

	/**
	 * Helper method that takes the command object from the client and hands it to
	 * the database.
	 * 
	 * @param request
	 *         command object incoming from the <code>RecordSocketClient</code>.
	 * @return response to the command <code>request</code>
	 */
	private ResponseResult execCmdObject(RequestCommand request) {

		ResponseResult result = null;

		try {
			switch (request.getCommand()) {
			case FIND: {
				String[] criteria = request.getStringArray();
				long[] res = dbAccess.findByCriteria(criteria);
				result = new ResponseResult(res);
				break;
			}
			case ADD: {
				String[] data = request.getStringArray();
				long recNo = dbAccess.createRecord(data);
				result = new ResponseResult(recNo);
				break;
			}
			case READ: {
				long recNo = request.getLong();
				String[] data = dbAccess.readRecord(recNo);
				result = new ResponseResult(data);
				break;
			}
			case DELETE: {
				long[] args = request.getLongArray();
				long recNo = args[0];
				long lockCookie = args[1];
				dbAccess.deleteRecord(recNo, lockCookie);
				break;
			}

			case MODIFY: {
				long[] args = request.getLongArray();
				long recNo = args[0];
				long lockCookie = args[1];
				String[] data = request.getStringArray();
				dbAccess.updateRecord(recNo, data, lockCookie);
				break;
			}

			case LOCK: {
				long recNo = request.getLong();
				long cookie = dbAccess.lockRecord(recNo);
				result = new ResponseResult(cookie);
				break;
			}
			case UNLOCK: {
				long[] args = request.getLongArray();
				long recNo = args[0];
				long lockCookie = args[1];
				dbAccess.unlock(recNo, lockCookie);
				break;
			}
			}
		} catch (Exception e) {
			result = new ResponseResult(e);
		}
		return result;
	}

	/**
	 * Method closes streams of connection and socket itself.
	 */
	private void close() {
		try {
			out.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error closing output stream", e);
		}
		try {
			in.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error closing input stream", e);
		}
		try {
			socket.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error closing socket", e);
		}
	}
}
