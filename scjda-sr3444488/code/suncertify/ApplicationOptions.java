package suncertify;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import suncertify.Application.ApplicationType;

/**
 * Class <code>ApplicationOptions</code> provides functionality for the
 * application properties manipulation.<br>
 * Options of application is stored in properties file. It can be loaded and
 * saved at any moment.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class ApplicationOptions {
	/**
	 * name of exported option name for port value
	 */
	private static final String PORT = "port";
	/**
	 * name of exported option name for host value
	 */
	private static final String HOST = "host";
	/**
	 * name of exported option name for path value
	 */
	private static final String PATH = "path";
	/**
	 * name of exported property name for a lock timeout value
	 */
	private static final String LOCKTIMEOUT = "locktimeout";
	/**
	 * properties in which options is stored
	 */
	private Properties options = new Properties();
	/**
	 * filename of properties file
	 */
	private String fileName;
	/**
	 * reference to application object
	 */
	private Application application;
	/**
	 * names of server options
	 */
	private final String[] serverOptionsNames = new String[] { PATH, PORT,
			LOCKTIMEOUT };
	/**
	 * names of client options
	 */
	private final String[] clientOptionsNames = new String[] { HOST, PORT };

	/**
	 * names of standalone client options
	 */
	private final String[] standaloneOptionsNames = new String[] { PATH,
			LOCKTIMEOUT };
	/**
	 * map that defines dependencies of option's names and items in properties file
	 */
	private Map<String, String> mapping = new HashMap<String, String>();

	/**
	 * Constructor creates <code>ApplicationOptions<code> object.
	 * 
	 * @param app
	 *         reference to the application object
	 * @param fileName
	 *         file name of properties file
	 */
	public ApplicationOptions(Application app, String fileName) {
		this.application = app;
		this.fileName = fileName;
		initMapping();
	}
	
	/**
	 * Initialize options manually.<br>
	 * The method is useful when property file is not available for reading.
	 */
	public void initOptionsManually() {
		options.setProperty("HOST", "127.0.0.1");
		options.setProperty("PATH", "");
		options.setProperty("PORT", Integer.toString(8080));
		options.setProperty("LOCK_TIMEOUT", Integer.toString(5000));
	}

	/**
	 * Saves options to the properties file.
	 * 
	 * @throws IOException
	 *          on write file error
	 */
	public void saveOptions() throws IOException {
		options.store(new FileOutputStream(fileName), getComment());
	}

	/**
	 * Loads options from the properties file.
	 * 
	 * @throws IOException
	 *          on file read error
	 */
	public void loadOptions() throws IOException {
		options.load(new FileInputStream(fileName));
	}
	
	/**
	 * Returns application type specific options names.
	 * 
	 * @return array of option's names for application according application type
	 */
	public String[] getOptionsNames() {
		ApplicationType type = application.getApplicationType();
		switch (type) {
		case SERVER:
			return getServerOptionsNames();
		case CLIENT:
			return getClientOptionsNames();
		case STANDALONE:
			return getStandaloneOptionsNames();
		}
		return null;
	}
	
	/**
	 * Sets application type specific options values.
	 * 
	 * @param values
	 *         array of option's values for application
	 */
	public void setOptionsValues(String[] values) {
		ApplicationType type = application.getApplicationType();
		switch (type) {
		case SERVER:
			setServerOptionsValues(values);
			break;
		case CLIENT:
			setClientOptionsValues(values);
			break;
		case STANDALONE:
			setStandaloneOptionsValues(values);
			break;
		}
	}

	/**
	 * Returns names of server options.
	 * 
	 * @return array of server option's names
	 */
	public String[] getServerOptionsNames() {
		return serverOptionsNames;
	}

	/**
	 * Returns names of client options.
	 * 
	 * @return array of client option's names
	 */
	public String[] getClientOptionsNames() {
		return clientOptionsNames;
	}

	/**
	 * Returns array of standalone option's names.
	 * 
	 * @return array of standalone option's names
	 */
	public String[] getStandaloneOptionsNames() {
		return standaloneOptionsNames;
	}
	
	/**
	 * Returns application type specific options values.
	 * 
	 * @return array of option's values for this application according application
	 *         type
	 */
	public String[] getOptionsValues() {
		ApplicationType type = application.getApplicationType();
		switch (type) {
		case SERVER:
			return getServerOptionsValues();
		case CLIENT:
			return getClientOptionsValues();
		case STANDALONE:
			return getStandaloneOptionsValues();
		}
		return null;
	}
	
	/**
	 * Returns local path to database file.
	 * 
	 * @return local path to database file
	 */
	public String getDBPath() {
		return getOptionByName(PATH);
	}

	/**
	 * Returns server port number for server and network client.
	 * 
	 * @return server port number
	 */
	public int getPortNumber() {
		String port = getOptionByName(PORT);
		return Integer.parseInt(port);
	}

	/**
	 * Returns host name of server using in client.
	 * 
	 * @return host name of server
	 */
	public String getHostName() {
		return getOptionByName(HOST);
	}

	/**
	 * Returns timeout for locking of database record.
	 * 
	 * @return timeout for locking in milliseconds
	 */
	public int getLockTimeout() {
		String timeout = getOptionByName(LOCKTIMEOUT);
		return Integer.parseInt(timeout);
	}
	
	/**
	 * Overrides method of {@link Object} class for debugging issues.
	 */
	public String toString()
	{
		return String.valueOf(options);
	}

	
	/**
	 * Initializes mapping for option names and properties file items.
	 */
	private void initMapping() {
		mapping.put(HOST, "HOST");
		mapping.put(PATH, "PATH");
		mapping.put(PORT, "PORT");
		mapping.put(LOCKTIMEOUT, "LOCK_TIMEOUT");
	}

	/**
	 * Returns values extracted from properties by option's names.
	 * 
	 * @param names
	 *         array of options names
	 * @return array of option's values
	 */
	private String[] getValuesByNames(String[] names) {
		String[] result = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			String propName = mapping.get(name);
			String value = this.options.getProperty(propName);
			result[i] = value;
		}
		return result;
	}

	/**
	 * Sets items of properties by option's names and values.
	 * 
	 * @param names
	 *         array of options names
	 * @param values
	 *         array of values according <code>names</code>
	 */
	private void setValuesByNames(String[] names, String values[]) {
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			String propName = mapping.get(name);
			String value = values[i];
			this.options.setProperty(propName, value);
		}
	}

	/**
	 * Returns comment of properties file.
	 */
	private String getComment() {
		return "***** ATTENTION!!! DO NOT EDIT MANUALLY *****";
	}

	/**
	 * Returns array of server option's values.
	 * 
	 * @return array of server option's values
	 */
	private String[] getServerOptionsValues() {
		return getValuesByNames(serverOptionsNames);
	}

	/**
	 * Returns array of client option's values.
	 * 
	 * @return array of client option's values
	 */
	private String[] getClientOptionsValues() {
		return getValuesByNames(clientOptionsNames);
	}

	/**
	 * Returns array of client option's values.
	 * 
	 * @return array of client option's values
	 */
	private String[] getStandaloneOptionsValues() {
		return getValuesByNames(standaloneOptionsNames);
	}

	/**
	 * Sets values of options for client.
	 * 
	 * @param optionsValues
	 *         array of option's values
	 */
	private void setClientOptionsValues(String[] optionsValues) {
		setValuesByNames(clientOptionsNames, optionsValues);
	}

	/**
	 * Sets values of options for server.
	 * 
	 * @param optionsValues
	 *         array of option's values
	 */
	private void setServerOptionsValues(String[] optionsValues) {
		setValuesByNames(serverOptionsNames, optionsValues);
	}

	/**
	 * Sets values for standalone client.
	 * 
	 * @param optionsValues
	 *         array of option's values
	 */
	private void setStandaloneOptionsValues(String[] optionsValues) {
		setValuesByNames(standaloneOptionsNames, optionsValues);
	}

	/**
	 * Returns options value by option name.
	 * 
	 * @param optionName
	 *         option name
	 * @return option value
	 */
	private String getOptionByName(String optionName) {
		String[] names = new String[] { optionName };
		return getValuesByNames(names)[0];
	}


}
