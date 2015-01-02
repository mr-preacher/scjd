package suncertify.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 * Class <code>RecordTableModel</code> is the custom table model used by the
 * {@link ClientWindow} instance. This table model maps record numbers in
 * database to row numbers in JTable.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class RecordTableModel extends AbstractTableModel {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization. Not that we ever serialize this class of course, but
	 * AbstractTableModel implements Serializable, so therefore by default we do as
	 * well.
	 */
	private static final long serialVersionUID = 5165L;

	/**
	 * Logger for debugging issues
	 */
	private transient Logger log = Logger.getLogger(this.getClass().getPackage()
			.getName());

	/**
	 * An array of <code>String</code> objects representing the table headers.
	 */
	private String[] headerNames = { "name", "location", "specialties", "size",
			"rate", "owner" };

	/**
	 * Holds all records instances displayed in the main table.
	 */
	private List<String[]> records = new ArrayList<String[]>(5);

	/**
	 * Record number mapping defines which record number corresponds to a row
	 * number. Index of element in the list is record number and value of elements
	 * is record number.
	 */
	private List<Long> recordNumbers = new ArrayList<Long>();

	/**
	 * Returns the number of columns in the model. A <code>JTable</code> uses this
	 * method to determine how many columns it should create and display by
	 * default.
	 * 
	 * @return the number of columns in the model
	 */
	public int getColumnCount() {
		return this.headerNames.length;
	}

	/**
	 * Returns the number of rows in the model. A <code>JTable</code> uses this
	 * method to determine how many rows it should display. This method should be
	 * quick, as it is called frequently during rendering.
	 * 
	 * @return the number of rows in the model
	 */
	public int getRowCount() {
		return this.records.size();
	}

	/**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>.
	 * 
	 * @param rowIndex
	 *         the row whose value is to be queried
	 * @param columnIndex
	 *         the column whose value is to be queried
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		String[] rowValues = this.records.get(rowIndex);
		String value = rowValues[columnIndex];
		return value == null ? "" : value.trim();
	}

	/**
	 * Returns the name of the column at <code>columnIndex</code>. This is used to
	 * initialize the table's column header name. Note: this name does not need to
	 * be unique; two columns in a table can have the same name.
	 * 
	 * @param columnIndex
	 *         the index of the column
	 * @return the name of the column
	 */
	public String getColumnName(int columnIndex) {
		return headerNames[columnIndex];
	}

	/**
	 * Returns false. This is the default implementation for all cells.
	 * 
	 * @param rowIndex
	 *         the row being queried
	 * @param columnIndex
	 *         the column being queried
	 * @return always <code>false</code>
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * Adds new record to the table model.
	 * 
	 * @param recNo
	 *         record number of adding record
	 * @param data
	 *         array of fields of adding record
	 */
	public void addRecord(long recNo, String[] data) {
		int row = records.size();
		recordNumbers.add(recNo);
		records.add(data);
		fireTableRowsInserted(row, row);
		log.log(Level.SEVERE, "row added: " + row + " " + Arrays.toString(data)
				+ " record number: " + recNo);
	}

	/**
	 * Deletes record from table model using specified row number.
	 * 
	 * @param row
	 *         row number of deleting record
	 */
	public void deleteRow(int row) {
		records.remove(row);
		recordNumbers.remove(row);
		fireTableRowsDeleted(row, row);
		log.log(Level.SEVERE, "row removed: " + row);
	}

	/**
	 * Returns record number by a row number.
	 * 
	 * @param row
	 *         row number of record in the table
	 * @return record number, that corresponds to the <code>row</cod>
	 */
	public long getRecNoByRow(int row) {
		Long recNo = recordNumbers.get(row);
		log.log(Level.SEVERE, "record number is " + recNo + " for row number " + row);
		return recNo;
	}

	/**
	 * Deletes all rows from table model.
	 */
	public void clear() {
		recordNumbers.clear();
		int size = records.size();
		records.clear();
		fireTableRowsDeleted(0, size - 1);
		log.log(Level.SEVERE, "all rows removed");
	}

	/**
	 * Updates row with new data of record.
	 * 
	 * @param row
	 *         row number
	 * @param data
	 *         new data, array of fields of the record
	 */
	public void update(int row, String[] data) {
		records.remove(row);
		records.add(row, data);
		fireTableRowsUpdated(row, row);
		log.log(Level.SEVERE, "row updated: " + row + " " + Arrays.toString(data));
	}

}
