package suncertify.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.utils.WeakHashSet;

/**
 * Class <code>Data</code> implements interface {@link DBAccess} provides
 * methods to operate a database file on local hard drive.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class Data implements DBAccess {
	/**
	 * "field name" size, length in the header of file for each record
	 */
	private static int fn_len = 1;
	/**
	 * "field size" size, length in the header of file for each record
	 */
	private static int fl_len = 1;

	/**
	 * "number of fields" size, length in the header of file for entire database
	 * file
	 */
	private static final int nof_len = 2;

	/**
	 * magic cookie value captioned from database file
	 */
	private static final byte[] mc_value = new byte[] { 0x00, 0x00, 0x02, 0x03 };

	/**
	 * character encoding of database file
	 */
	private final static String encoding = "US-ASCII";
	/**
	 * defined number of first record in database file
	 */
	protected final static int FIRST_RECNO = 0;
	/**
	 * space byte for fields that have not enough length
	 */
	private final static byte space_byte = (byte) 0x20;
	/*
	 * calculating length of record
	 */
	static {
		int temp = 0;
		for (int fl : FIELD_LENGTH) {
			temp += fl;
		}
		record_length = temp;
	}

	/*
	 * calculating header size of database file
	 */
	static {
		int temp = 0;
		temp += mc_value.length;
		temp += nof_len;
		for (String fn : FIELD_SEQUENCE) {
			temp += fn_len;
			try {
				temp += fn.getBytes(encoding).length;
			} catch (UnsupportedEncodingException e) {
			}
			temp += fl_len;

		}
		header_offset = temp;
	}

	/**
	 * logger is for debugging purposes
	 */
	private Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	/**
	 * set of record numbers of the deleted records
	 */
	private final Set<Long> deleted = new HashSet<Long>();
	/**
	 * deleted flag byte value
	 */
	private final static byte deletedrecbyte = (byte) 0xFF;
	/**
	 * random access file reference to operate database file
	 */
	private RandomAccessFile file;
	/**
	 * "deleted flag" size, length for each record
	 */
	private final static int flaglength = 1;
	/**
	 * data section offset, length between beginning on database file and first
	 * record
	 */
	private static int header_offset;
	/**
	 * Map of locks for locked record numbers. Key of map is unique object for
	 * record number, value is locking descriptor ("cookie").
	 */
	private final Map<Long, Long> locks = new ConcurrentHashMap<Long, Long>();
	/**
	 * Cache of unique objects that is used in the functionality
	 * "one lock"-"one record". Unique objects needed to perform valid blocking on
	 * the same record numbers. Using this cache allows to lock/unlock records
	 * separately each from other.
	 */
	private final WeakHashSet<Long> numberCache = new WeakHashSet<Long>();
	/**
	 * reference to helper object for file reading/writing
	 */
	private final ReaderWriter readerwriter = new ReaderWriter();
	/**
	 * record length without "deleted flag"
	 */
	private final static int record_length;
	/**
	 * Read/write blocking for <code>valid</code> and <code>deleted</code> sets.
	 * Many clients can read these sets but only one at time can write to sets.
	 * While writing is in progress reading can not be done.
	 */
	private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
	/**
	 * Valid, i.e. not deleted, record numbers set
	 */
	private final Set<Long> valid = new HashSet<Long>();

	/**
	 * valid flag byte value
	 */
	private final static byte validrecbyte = 0x00;

	/**
	 * amount of all (deleted and valid) records in database file
	 */
	private long recordCount;

	/**
	 * Constructor creates database operating object to operate database file on
	 * local hard drive.
	 * 
	 * @param path
	 *         local path to a database file
	 * @throws IOException
	 *          on read or write error
	 */
	public Data(String path) throws IOException {

		File file = new File(path);
		this.file = new RandomAccessFile(file, "rw");
		try {
			readerwriter.checkheader();
		} catch (IOException e) {
			log.log(Level.SEVERE, "wrong data file format");
			throw e;
		}
		buildIndex();
	}

	/**
	 * {@inheritDoc}
	 */
	public long createRecord(String[] data) {
		rwlock.writeLock().lock();
		try {
			Iterator<Long> it = deleted.iterator();
			boolean reuse;
			Long recNo;
			if (reuse = it.hasNext()) {
				recNo = it.next();
			} else {
				recNo = recordCount;
			}
			long pos = getRecPos(recNo);
			readerwriter.write(pos, validrecbyte);
			readerwriter.write(pos + flaglength, data);
			valid.add(recNo);
			if (reuse) {
				deleted.remove(recNo);
			} else {
				recordCount++;
			}
			return recNo;
		} catch (IOException e) {
			throw new RuntimeException("error writing file", e);
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteRecord(long recNo, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		Object recNoMutex = getMutexForRecNo(recNo);
		checkLockCookie(recNo, recNoMutex, lockCookie);
		rwlock.writeLock().lock();
		try {
			if (!valid.contains(recNo))
				throw new RecordNotFoundException();
			long pos = getRecPos(recNo);
			readerwriter.write(pos, deletedrecbyte);
			valid.remove(recNo);
			deleted.add(recNo);
		} catch (IOException e) {
			throw new RuntimeException("error writing file", e);
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * Performs clean-up of unused object.
	 */
	protected void finalize() {
		close();
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] findByCriteria(String[] criteria) {
		long[] result = new long[valid.size()];
		int count = 0;
		for (Long recNo : valid) {
			try {
				String[] data = readRecord(recNo);
				if (checkCriteria(data, criteria)) {
					result[count] = recNo;
					count++;
				}
			} catch (RecordNotFoundException e) {
				log.log(Level.SEVERE, "record was valid but now not found");
			}
		}
		long[] cutresult = new long[count];
		System.arraycopy(result, 0, cutresult, 0, count);
		return cutresult;
	}

	/**
	 * {@inheritDoc}
	 */
	public long lockRecord(long recNo) throws RecordNotFoundException {
		log.log(Level.SEVERE, "lock record: " + recNo);
		Object recNoMutex = getMutexForRecNo(recNo);
		long lockCookie = 0;
		synchronized (recNoMutex) {
			while (locks.containsKey(recNo)) {
				try {
					recNoMutex.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			rwlock.readLock().lock();
			try {
				if (!valid.contains(recNo))
					throw new RecordNotFoundException("invalid record number " + recNo);
				lockCookie = System.nanoTime();
				locks.put(recNo, lockCookie);
			} finally {
				rwlock.readLock().unlock();
			}
		}
		log
				.log(Level.SEVERE, "record " + recNo + " locked with cookie " + lockCookie);
		return lockCookie;

	}

	/**
	 * {@inheritDoc}
	 */
	public String[] readRecord(long recNo) throws RecordNotFoundException {
		rwlock.readLock().lock();
		try {
			if (!valid.contains(recNo))
				throw new RecordNotFoundException();
			long pos = getRecPos(recNo);
			String[] data = readerwriter.readData(pos + flaglength);
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			rwlock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unlock(long recNo, long cookie) throws SecurityException {
		log.log(Level.SEVERE, "unlock: " + recNo + ", " + cookie);
		Object recNoMutex = getMutexForRecNo(recNo);
		synchronized (recNoMutex) {
			Long lock = locks.get(recNo);
			if (lock != null && lock.longValue() == cookie) {
				locks.remove(recNo);
				recNoMutex.notifyAll();
			} else {
				throw new SecurityException("invalid unlock cookie");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateRecord(long recNo, String[] data, long lockCookie)
			throws RecordNotFoundException, SecurityException {
		log.log(Level.SEVERE, "update: " + recNo + ", " + lockCookie);
		Object recNoMutex = getMutexForRecNo(recNo);
		checkLockCookie(recNo, recNoMutex, lockCookie);
		rwlock.readLock().lock();
		try {
			if (!valid.contains(recNo))
				throw new RecordNotFoundException("invalid record number");
		} finally {
			rwlock.readLock().unlock();
		}

		long pos = getRecPos(recNo);
		try {
			readerwriter.write(pos + flaglength, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method fills <code>valid</code> and <code>deleted</code> sets with values,
	 * according data in database file.
	 * 
	 * @throws IOException
	 *          on file reading error
	 */
	private void buildIndex() throws IOException {
		rwlock.writeLock().lock();
		try {
			valid.clear();
			deleted.clear();
			for (long pos = header_offset, recNo = FIRST_RECNO; pos < file.length(); pos += (record_length + flaglength), recNo++) {
				byte b = readerwriter.read(pos);
				if (b == validrecbyte) {
					valid.add(recNo);
				} else if (b == deletedrecbyte) {
					deleted.add(recNo);
				} else {
					throw new RuntimeException("flag expected but other found: " + b);
				}
			}
			recordCount = valid.size() + deleted.size();
		} finally {
			rwlock.writeLock().unlock();
		}

	}

	/**
	 * Method checks if record is accepted by filter criteria.
	 * 
	 * @param dataArr
	 *         record fields
	 * @param criteriaArr
	 *         filter criteria
	 * @return <code>true</code> if record is accepted, <code>false</code> if
	 *         record is not accepted
	 */
	private boolean checkCriteria(String[] dataArr, String[] criteriaArr) {
		try {
			for (int i = 0; i < FIELD_SEQUENCE.length; i++) {
				String data = dataArr[i];
				String criteria = criteriaArr[i];
				if (criteria == null || data == null)
					continue;
				if (!data.startsWith(criteria))
					return false;
			}
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}

	/**
	 * Validates locking descriptor for specified record.
	 * 
	 * @param recNoRef
	 *         "unique per record number" object
	 * @param lockCookie
	 *         locking descriptor
	 * @throws SecurityException
	 *          if locking descriptor is invalid
	 */
	private void checkLockCookie(Long recNo, Object recNoMutex, long lockCookie)
			throws SecurityException {
		synchronized (recNoMutex) {
			log.log(Level.SEVERE, "check lock: " + recNo + ", " + lockCookie);
			Long lock = locks.get(recNo);
			if (lock == null || lock.longValue() != lockCookie)
				throw new SecurityException("invalid cookie passed");
		}
	}

	/**
	 * Closes this random access file stream and releases any system resources
	 * associated with the stream when garbage collection determines that there are
	 * no more references to the object.
	 */
	private void close() {
		if (file != null)
			try {
				file.close();
				file = null;
			} catch (IOException e) {
				log.log(Level.SEVERE, "Error closing file", e);
			}
	}

	/**
	 * Returns unique object for record number. Unique object used to gain access
	 * to the record index in sets: <code>valid, deleted</code>. Also it provides
	 * ability to lock and unlock records independently each from other.
	 * 
	 * @param recNo
	 *         record number
	 * @return "unique per record number" object
	 */
	private Object getMutexForRecNo(long recNo) {
		Long number = numberCache.get(recNo);
		if (number == null) {
			number = new Long(recNo);
			numberCache.add(number);
		}
		return number;
	}

	/**
	 * Returns position of record in file.
	 * 
	 * @param recNo
	 *         record number
	 * @return position of record in database file
	 */
	private long getRecPos(long recNo) {
		return header_offset + (recNo - FIRST_RECNO) * (record_length + flaglength);
	}

	/**
	 * Helper class for reading and writing random access file that is database
	 * file.
	 */
	private class ReaderWriter {

		/**
		 * Checks if existing header of file is valid
		 * 
		 * @throws IOException
		 *          on file reading error or if header is not valid
		 */
		public void checkheader() throws IOException {
			if (file.length() < header_offset) {
				throw new IOException("database file is too small to be valid");
			}
			synchronized (file) {
				file.seek(0);
				byte[] hbytes = new byte[mc_value.length];
				file.read(hbytes);
				if (!Arrays.equals(hbytes, mc_value))
					throw new IOException("wrong database file format");
			}
		}

		/**
		 * Reads one byte in position <code>pos</code> of random access file
		 * 
		 * @param pos
		 *         position of byte
		 * @return byte method reads
		 * @throws IOException
		 *          on error during reading
		 */
		public byte read(long pos) throws IOException {
			byte[] b = new byte[1];
			synchronized (file) {
				file.seek(pos);
				file.read(b);
			}
			return b[0];
		}

		/**
		 * Returns field values based on database record scheme as array of String
		 * 
		 * @param pos
		 *         position of data in random access file
		 * @param fields
		 *         database record scheme
		 * @return array of String representing fields of record
		 * @throws IOException
		 *          on file reading error
		 */
		public String[] readData(long pos) throws IOException {
			String[] result = new String[FIELD_SEQUENCE.length];
			synchronized (file) {
				file.seek(pos);
				for (int i = 0; i < FIELD_LENGTH.length; i++) {
					byte[] fielddata = new byte[FIELD_LENGTH[i]];
					file.read(fielddata, 0, fielddata.length);
					result[i] = new String(fielddata, encoding).trim();
				}
			}
			return result;
		}

		/**
		 * Writes only one byte to a specified position <code>pos</code> in the random
		 * access file
		 * 
		 * @param pos
		 *         position in random access file
		 * @param b
		 *         one byte to write
		 * @throws IOException
		 *          on file writing error
		 */
		public void write(long pos, byte b) throws IOException {
			synchronized (file) {
				file.seek(pos);
				file.write(b);
			}
		}

		/**
		 * Writes data to a specified position <code>pos</code> in the random access
		 * file, according database record scheme.
		 * 
		 * @param pos
		 *         position in a random access file
		 * @param data
		 *         array of String, representing fields of record
		 * @throws IOException
		 *          on file writing error
		 */
		public void write(long pos, String[] data) throws IOException {
			synchronized (file) {
				file.seek(pos);
				for (int i = 0; i < FIELD_LENGTH.length; i++) {
					byte[] fielddata = new byte[FIELD_LENGTH[i]];
					byte[] outdata = data[i].getBytes(encoding);
					if (fielddata.length > outdata.length)
						Arrays.fill(fielddata, space_byte);
					System.arraycopy(outdata, 0, fielddata, 0, Math.min(fielddata.length,
							outdata.length));
					file.write(fielddata, 0, fielddata.length);
				}
			}
		}
	}

}
