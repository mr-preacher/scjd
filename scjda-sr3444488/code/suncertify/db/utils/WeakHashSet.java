package suncertify.db.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class <code>WeakHashSet</code> purposed for caching of unique objects.<br>
 * Some Java classes have cache for unique objects of fixed range:
 * {@link Integer}, {@link Long} classes have <code>valueOf</code> method
 * providing this functionality. <br>
 * <code>WeakHashSet</code> works like {@link WeakHashMap} but stored values are
 * the keys.<br>
 * Code example illustrates advantages of using WeakHashSet:<br>
 * <ul>
 * <li>Unused unique values will be removed by garbage collector
 * 
 * <pre>
 * WeakHashSet&lt;Long&gt; set = new WeakHashSet&lt;Long&gt;();
 * Long l1 = new Long(0);
 * Long l2 = new Long(0);
 * set.add(l1);
 * set.add(l2);//set.size() == 1
 * l1 = null; //make unused
 * l2 = null; //make unused
 * System.gc();//memory flush
 * ...
 * System.gc();//set.size() == 0
 * </pre>
 * 
 * </li>
 * <li>Set caches unique objects
 * 
 * <pre>
 * WeakHashSet&lt;Long&gt; set = new WeakHashSet&lt;Long&gt;();
 * Long l1 = new Long(0);
 * Long l2 = new Long(0);//l2 != l1, l2.equals(l1) returns true
 * Long l3 = new Long(0);//l3 != l1, l3.equals(l1) returns true
 * set.add(l1);
 * Long rl2 = set.get(l2);//r12 == l1
 * Long rl3 = set.get(l3);//r13 == l1
 * </pre>
 * 
 * </li>
 * </ul>
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class WeakHashSet<K> {
	/**
	 * The items in this hash table extend WeakReference, using its main reference
	 * field as the table value.
	 */
	static class Item<T> extends WeakReference<T> {
		/**
		 * hash code of this item
		 */
		int hash;
		/**
		 * reference to next item in the table row
		 */
		Item<T> next;

		/**
		 * Constructor creates new item of hash table.
		 * 
		 * @param referent
		 *         a referent object for weak reference
		 * @param q
		 *         reference queue for weak reference
		 * @param hash
		 *         hash code of this item
		 * @param next
		 *         next item in the table row
		 */
		public Item(T referent, ReferenceQueue<? super T> q, int hash, Item<T> next) {
			super(referent, q);
			this.hash = hash;
			this.next = next;
		}
	}

	/**
	 * Logger for the debugging issues
	 */
	private final Logger log = Logger.getLogger(this.getClass().getPackage()
			.getName());

	/**
	 * The default initial capacity -- MUST be a power of two.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The load fast used when none specified in constructor.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The maximum capacity, used if a higher value is implicitly specified by
	 * either of the constructors with arguments. MUST be a power of two <= 1<<30.
	 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * Checks for equality of non-null reference x and possibly-null y. By default
	 * uses Object.equals.
	 */
	private static boolean eq(Object x, Object y) {
		return x == y || x.equals(y);
	}

	/**
	 * Applies a supplemental hash function to a given hashCode, which defends
	 * against poor quality hash functions. This is critical because HashSet uses
	 * power-of-two length hash tables, that otherwise encounter collisions for
	 * hashCodes that do not differ in lower bits. Note: Null keys always map to
	 * hash 0, thus index 0.
	 */
	private static int hash(int h) {
		// This function ensures that hashCodes that differ only by
		// constant multiples at each bit position have a bounded
		// number of collisions (approximately 8 at default load factor).
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	/**
	 * Returns index for hash code h.
	 */
	private static int indexFor(int h, int length) {
		return h & (length - 1);
	}

	/**
	 * The load factor for the hash table.
	 */
	private final float loadFactor;

	/**
	 * Reference queue for cleared items
	 */
	private final ReferenceQueue<K> queue = new ReferenceQueue<K>();

	/**
	 * The number of values contained in this weak hash set.
	 */
	private int size;

	/**
	 * The table, resized as necessary. Length MUST Always be a power of two.
	 */
	private Item<K>[] table;

	/**
	 * The next size value at which to resize (capacity * load factor).
	 */
	private int threshold;

	/**
	 * Constructs a new, empty <code>WeakHashSet</code>.
	 */
	public WeakHashSet() {
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		threshold = (int) (DEFAULT_INITIAL_CAPACITY);
		table = new Item[DEFAULT_INITIAL_CAPACITY];
	}

	/**
	 * Adds a value in this set. If the set previously contained value or value is
	 * null, the new value is discarded.
	 * 
	 * @param value
	 *         value to add in this set
	 * @return <code>true</code> if value is added, not discarded, otherwise
	 *         <code>false</code>
	 */
	public synchronized boolean add(K value) {
		if (value == null) {
			log.log(Level.SEVERE, "value is discarded " + value);
			return false;
		}
		int h = hash(value.hashCode());
		Item<K>[] tab = getTable();
		int i = indexFor(h, tab.length);

		for (Item<K> e = tab[i]; e != null; e = e.next) {
			if (h == e.hash && eq(value, e.get())) {
				log.log(Level.SEVERE, "value is discarded " + value);
				return false;
			}
		}

		Item<K> e = tab[i];
		Item<K> newE = new Item<K>(value, queue, h, e);
		log.log(Level.SEVERE, "value is added " + value + ", item = " + newE);
		tab[i] = newE;
		if (++size >= threshold)
			resize(tab.length * 2);
		return true;
	}

	/**
	 * Removes all of the values from this set. The set will be empty after this
	 * call returns.
	 */
	public synchronized void clear() {
		// clear out reference queue. We don't need to expunge entries
		// since table is getting cleared.
		while (queue.poll() != null)
			;

		Item<K>[] tab = table;
		for (int i = 0; i < tab.length; ++i)
			tab[i] = null;
		size = 0;

		// Allocation of array may have caused GC, which may have caused
		// additional entries to go stale. Removing these entries from the
		// reference queue will make them eligible for reclamation.
		while (queue.poll() != null)
			;
	}

	/**
	 * Returns the value in set based on presented value.
	 * <p>
	 * More formally, if this set contains value which is equal and has the same
	 * hash as presented value, method returns value from set, otherwise method
	 * returns null.
	 * 
	 * @param value
	 *         a presented value
	 * @return value in this set which equals to a presented value and has the same
	 *         hash as presented value has, or null if no such value found in the
	 *         set
	 */
	public synchronized K get(K value) {
		if (value == null)
			return null;
		int h = hash(value.hashCode());
		Item<K>[] tab = getTable();
		int index = indexFor(h, tab.length);
		Item<K> e = tab[index];
		while (e != null) {
			K item = e.get();
			if (e.hash == h && eq(value, item))
				return item;
			e = e.next;
		}
		return null;
	}

	/**
	 * Returns the number of values in this set. This result is a snapshot, and may
	 * not reflect unprocessed values that will be removed before next attempted
	 * access because they are no longer referenced.
	 * 
	 * @return size of this set
	 */
	public synchronized int size() {
		if (size == 0)
			return 0;
		expungeStaleEntries();
		return size;
	}

	/**
	 * Expunges stale items from the table. Items in the weak reference queue
	 * defined as unused items.<br>
	 * Method removes unused items from hash table.
	 */
	private void expungeStaleEntries() {
		Item<K> e;
		while ((e = (Item<K>) queue.poll()) != null) {
			log.log(Level.SEVERE, "item is expunged " + e);
			int h = e.hash;
			int i = indexFor(h, table.length);

			Item<K> prev = table[i];
			Item<K> p = prev;
			while (p != null) {
				Item<K> next = p.next;
				if (p == e) {
					if (prev == e)
						table[i] = next;
					else
						prev.next = next;
					e.next = null; // Help GC
					size--;
					break;
				}
				prev = p;
				p = next;
			}
		}
	}

	/**
	 * Returns the table after first expunging stale items.
	 * 
	 * @return hash table
	 */
	private Item<K>[] getTable() {
		expungeStaleEntries();
		return table;
	}

	/**
	 * Rehashes the contents of this set into a new array with a larger capacity.
	 * This method is called automatically when the number of items in this set
	 * reaches its threshold. If current capacity is MAXIMUM_CAPACITY, this method
	 * does not resize the set, but sets threshold to Integer.MAX_VALUE. This has
	 * the effect of preventing future calls.
	 * 
	 * @param newCapacity
	 *         the new capacity, MUST be a power of two; must be greater than
	 *         current capacity unless current capacity is MAXIMUM_CAPACITY (in
	 *         which case value is irrelevant).
	 */
	private void resize(int newCapacity) {
		Item<K>[] oldTable = getTable();
		int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}

		Item<K>[] newTable = new Item[newCapacity];
		transfer(oldTable, newTable);
		table = newTable;

		/*
		 * If ignoring null elements and processing ref queue caused massive
		 * shrinkage, then restore old table. This should be rare, but avoids
		 * unbounded expansion of garbage-filled tables.
		 */
		if (size >= threshold / 2) {
			threshold = (int) (newCapacity * loadFactor);
		} else {
			expungeStaleEntries();
			transfer(newTable, oldTable);
			table = oldTable;
		}
	}

	/**
	 * Transfers all items from src to dest tables.
	 * 
	 * @param src
	 *         source table of the hash table items
	 * @param dest
	 *         destination table of the hash table items
	 */
	private void transfer(Item<K>[] src, Item<K>[] dest) {
		for (int j = 0; j < src.length; ++j) {
			Item<K> e = src[j];
			src[j] = null;
			while (e != null) {
				Item<K> next = e.next;
				Object key = e.get();
				if (key == null) {
					e.next = null; // Help GC
					size--;
				} else {
					int i = indexFor(e.hash, dest.length);
					e.next = dest[i];
					dest[i] = e;
				}
				e = next;
			}
		}
	}
}
