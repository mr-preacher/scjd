package suncertify;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * Class <code>ApplicationWindow</code> is abstract class of window that
 * application uses as the main window.<br>
 * On startup window located in the central position of the screen and when user
 * closes that window confirmation dialog appears to allow user to confirm
 * application exit.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public abstract class ApplicationWindow extends JFrame {

	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;
	/**
	 * application reference
	 */
	protected Application application;;
	/**
	 * windows closing listener
	 */
	private WindowListener windowClosingAdapter;
	/**
	 * confirmation message into close confirmation dialog
	 */
	private final static String closeMsg = "Close Application?";

	/**
	 * title of close confirmation dialog
	 */
	private final static String closeTitle = "Close confirmation";

	/**
	 * Creates application window.
	 * 
	 * @param application
	 *         application object reference
	 */
	public ApplicationWindow(Application application) {
		if (application == null)
			throw new IllegalArgumentException("application must be not null");
		this.application = application;
		this.windowClosingAdapter = new WindowClosingAdapter();
		this.addWindowListener(this.windowClosingAdapter);
		this.setDefaultCloseOperation(0);
	}

	/**
	 * Method closes application.<br>
	 * If user confirms closing, application cleanup invoked.
	 */
	public void close() {
		windowClosingAdapter.windowClosing(null);
	}

	/**
	 * Overrides method of class <code>JFrame</code> to disable closing of window
	 * without confirmation while application runs.
	 * 
	 * @param operation
	 *         well-known closing operation constant
	 */
	public void setDefaultCloseOperation(int operation) {
		operation = WindowConstants.DO_NOTHING_ON_CLOSE;
		super.setDefaultCloseOperation(operation);
	}

	/**
	 * Overrides method of {@link JFrame} to bring frame to the central position.
	 * 
	 * @param flag
	 *         visibility flag
	 */
	public void setVisible(boolean flag) {
		if (flag) {
			this.pack();
			this.centerWindow();
		}
		super.setVisible(flag);
	}

	/**
	 * Method that is called to update window content when it is needed.<br>
	 * Changes of a state of the application needed to be reflected on the
	 * application window. That method is used for those purposes.
	 */
	public abstract void updateWindowContent();

	/**
	 * Brings window in the central position of screen.
	 */
	private void centerWindow() {
		Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension wdim = this.getSize();
		this.setLocation((sdim.width - wdim.width) / 2,
				(sdim.height - wdim.height) / 2);
	}
	
	/**
	 * Windows listener for window.<br>
	 * When user clicks on the right upper cross button to close window
	 * confirmation dialog will be displayed to allow user to confirm closing.
	 */
	private class WindowClosingAdapter extends WindowAdapter {
		/**
		 * Method handles windowClosing event
		 * 
		 * @param e
		 *         window closing event
		 */
		public void windowClosing(WindowEvent e) {
			if (JOptionPane.showConfirmDialog(ApplicationWindow.this, closeMsg,
					closeTitle, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				application.cleanUp();
				ApplicationWindow.this.dispose();
				System.exit(0);
			}
		}
	}
}
