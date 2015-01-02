package suncertify.db;

/**
 * Interface <code>Finalizable</code> must implemented by classes that have
 * clean up functionality. <br>
 * In some cases object that have running threads inside could not be finalized.
 * This interface tells that object must be forcedly finalized before all
 * references to the one are lost.
 */
public interface Finalizable {
	
	/**
	 * This method should be called to perform clean up.
	 */
	void finalize();
}
