package suncertify.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.DBAccess;

/**
 * Class <code>RecordSocketServer</code> handles socket client requests.
 * Instance of <code>RecordSocketServer</code> creates a separate processing
 * thread for each request and starts that thread.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class RecordSocketServer {
	/**
	 * Logger for debugging issues
	 */
	private Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	/**
	 * database operating object reference
	 */
	private DBAccess dbAccess;
	/**
	 * server port
	 */
	private int port;
	/**
	 * socket timeout
	 */
	private int sotimeout;
	/**
	 * server socket
	 */
	private ServerSocket ssocket;
	/**
	 * server working thread
	 */
	private Thread workingThread;
	/**
	 * flag determines servers is running
	 */
	private boolean running;
	/**
	 * opened client connections list
	 */
	private Vector<Socket> connectionList = new Vector<Socket>();

	/**
	 * Constructor creates instance of <code>RecordSocketServer</code>.
	 * 
	 * @param dba
	 *         database operating object reference
	 * @param port
	 *         port number
	 * @param sotimeout
	 *         socket timeout
	 * @throws IOException
	 *          on server creation error
	 */
	public RecordSocketServer(DBAccess dba, int port, int sotimeout) {
		this.dbAccess = dba;
		this.port = port;
		this.sotimeout = sotimeout;
	}

	/**
	 * Listens for new client connections, creating a new thread to handle the
	 * requests.
	 */
	public void start() throws IOException {
		if (running)
			return;
		this.ssocket = createSocket();
		running = true;
		workingThread = new Thread() {
			public void run() {
				log.log(Level.SEVERE, "Server started on port " + port);
				while (running) {
					try {
						Socket csocket = ssocket.accept();
						addConnection(csocket);
						log.log(Level.SEVERE, "client connected");
						RequestProcessingThread requestThread = new RequestProcessingThread(
								dbAccess, csocket);
						requestThread.start();
					} catch (SocketTimeoutException e) {

					} catch (IOException e) {
						log.log(Level.SEVERE, "error occured with socket " + e.getMessage());
					} catch (RuntimeException e) {
						log.log(Level.SEVERE, "error occured ", e.getMessage());
					}
				}
				running = false;
				log.log(Level.SEVERE, "Server stopped");
			}
		};
		workingThread.setName("SERVERSOCKET_WORKING_THREAD");
		workingThread.start();
	}

	/**
	 * Adds new client connection to connections list.
	 * 
	 * @param s
	 *         connection socket created for client
	 */
	private void addConnection(Socket s) {
		synchronized (connectionList) {
			connectionList.add(s);
		}
	}

	/**
	 * Closes all created connections with clients.
	 */
	private void closeConnections() {
		synchronized (connectionList) {
			for (Socket s : connectionList) {
				try {
					s.close();
				} catch (Exception e) {
					log.log(Level.SEVERE, "error closing connection", e);
				}
			}
			connectionList.clear();
		}
	}

	/**
	 * Stops server.<br>
	 * Server closes all client connections and terminates listening of the port.
	 */
	public void stop() {
		if (!running)
			return;
		running = false;
		if (workingThread != null) {
			try {
				workingThread.join();
				ssocket.close();
				ssocket = null;
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "Working thread is interrupted", e);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Error closing server socket", e);
			}
			closeConnections();
		}
	}

	/**
	 * Creates server socket.
	 * 
	 * @return created <code>ServerSocket</code> object reference
	 * @throws IOException
	 *          on socket creation error
	 */
	private ServerSocket createSocket() throws IOException {
		ServerSocket aServerSocket = new ServerSocket(this.port);
		aServerSocket.setSoTimeout(this.sotimeout);
		return aServerSocket;
	}

	/**
	 * Determines if server running.<br>
	 * While server is running it can accept connections from clients.
	 * 
	 * @return <code>true</code> if server running, or <code>false</code> otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Returns server port used in server.
	 * 
	 * @return port number of server used to listen to incoming connections
	 */
	public int getPort() {
		return port;
	}
}
