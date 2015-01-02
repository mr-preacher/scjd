package suncertify.db;

import java.io.IOException;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class <code>DataAU</code> extends {@link Data} class to implement
 * auto-unlocking feature.<br>
 * Auto-unlocking feature is useful when user forgets or can not unlock already
 * locked record. In that case record becomes unavailable to modify for other
 * users for a long time. Auto-unlocking uses time limit of locking to determine
 * maximum amount of time record can be locked at once.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class DataAU extends Data implements Finalizable {

	/**
	 * Logger for debugging issues
	 */
	private Logger log = Logger.getLogger(this.getClass().getPackage().getName());

	/**
	 * Auto-unlock queue. Delay queue that contains the pairs of recNo and
	 * lockCookie to used in auto-unlocking feature
	 */
	private DelayQueue<DelayedUnlockParams> unlockQueue;

	/**
	 * Auto-unlock timeout in milliseconds for auto-unlock feature. Maximum amount
	 * of time record can be locked at once.
	 */
	private long timeout;
	/**
	 * defines thread processing queue is running
	 */
	private boolean running;
	/**
	 * reference to the thread processing queue
	 */
	private Thread queueThread;

	/**
	 * Constructor. Creates new database operating object with auto-unlocking
	 * feature.
	 * 
	 * @param path
	 *         local path to database file
	 * @param locktimeout
	 *         maximum amount of time in milliseconds record can be locked
	 * @throws IOException
	 *          error reading or writing database file
	 */
	public DataAU(String path, long locktimeout) throws IOException {
		super(path);
		if (locktimeout <= 0) {
			throw new IllegalArgumentException("locktimeout must be more than 0");
		}
		this.unlockQueue = new DelayQueue<DelayedUnlockParams>();
		this.timeout = locktimeout;
		this.running = true;
		this.queueThread = new Thread() {
			/**
			 * Overrides method run of java.lang.Thread to implement queue processing
			 */
			public void run() {
				while (running) {
					long recNo = FIRST_RECNO - 1;
					long cookie = recNo;
					try {
						DelayedUnlockParams ulckParams = unlockQueue.take();
						recNo = ulckParams.recNo;
						cookie = ulckParams.cookie;
						unlock(recNo, cookie);
						log.log(Level.SEVERE, "Record " + recNo + " auto-unlocked using cookie "
								+ cookie);
					} catch (InterruptedException e) {

					} catch (SecurityException e) {
						log.log(Level.SEVERE, "\nAutounlocking fault. \nInvalid cookie " + cookie
								+ " for record " + recNo + ".\n Perhaps record already unlocked.");
					} catch (RuntimeException e) {
						log.log(Level.SEVERE, "Runtime exception occured", e);
					}
				}
			}
		};
		queueThread.setName("AUTOUNLOCKING_THREAD");
		queueThread.setDaemon(true);
		queueThread.start();
	}

	/**
	 * {@inheritDoc}<br>
	 * After each lock operation the new "unlock parameters" pair offered to unlock
	 * queue, so auto-unlocking thread can use these parameters for auto-unlock
	 * feature.
	 */
	public long lockRecord(long recNo) throws RecordNotFoundException {
		long cookie = super.lockRecord(recNo);
		unlockQueue.offer(new DelayedUnlockParams(recNo, cookie, timeout));
		return cookie;
	}

	/**
	 * {@inheritDoc} Thread that processing queue must be stopped to make object
	 * available for garbage collector.
	 */
	public void finalize() {
		this.running = false;
		try {
			queueThread.interrupt();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error interrupting thread", e);
		}
		super.finalize();
	}

	/**
	 * Nested class describing unlock parameters.<br>
	 * Unlock parameters used to unlock record when time is up.
	 */
	private static class DelayedUnlockParams implements Delayed {
		/**
		 * record number - the first parameter of the pair "unlock parameters"
		 */
		private long recNo;
		/**
		 * cookie - the second parameter of the pair "unlock parameters"
		 */
		private long cookie;
		/**
		 * timeout for auto-unlocking feature in milliseconds
		 */
		private long delay;
		/**
		 * time in milliseconds when the object created
		 */
		private long initialTime;

		/**
		 * Constructor. Creates delayed queue item with the lock info inside.
		 * 
		 * @param recNo
		 *         record number of locked record
		 * @param cookie
		 *         locking descriptor
		 * @param delay
		 *         maximum amount of time in milliseconds record can be locked
		 */
		DelayedUnlockParams(long recNo, long cookie, long delay) {
			this.initialTime = System.currentTimeMillis();
			this.recNo = recNo;
			this.cookie = cookie;
			this.delay = delay;
		}

		/**
		 * {@inheritDoc}
		 */
		public long getDelay(TimeUnit unit) {
			long now = System.currentTimeMillis();
			long result = unit.convert(delay - (now - initialTime),
					TimeUnit.MILLISECONDS);
			return result;
		}

		/**
		 * Implemented method of Delayed interface means that no sorting occurs while
		 * new object offered to a queue
		 * 
		 * @param o
		 *         object comparing to
		 * @return 0 always
		 */
		public int compareTo(Delayed o) {
			return 0;
		}
	}
}
