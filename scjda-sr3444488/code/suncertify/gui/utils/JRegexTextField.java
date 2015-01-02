package suncertify.gui.utils;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;

/**
 * Class <code>JRegexTextField</code> extends ValidatingTextField. It is a
 * control like {@link JFormattedTextField} but uses a regular expression
 * pattern instead of mask pattern.
 * 
 * @author Petr Shilkin
 * @version 1.0
 */
public class JRegexTextField extends ValidatingTextField {
	/**
	 * A version number for this class so that serialization can occur without
	 * worrying about the underlying class changing between serialization and
	 * deserialization.
	 */
	private static final long serialVersionUID = 5165L;
	/**
	 * Logger for debugging purposes
	 */
	private transient Logger log = Logger.getLogger(this.getClass().getPackage()
			.getName());

	/**
	 * Constructor of <code>JRegexTextField</code>. Creates new edit control with a
	 * specified regular expression.
	 * 
	 * @param regex
	 *         regular expression used in this control
	 */
	public JRegexTextField(final String regex) {
		super(new ValidationHandler() {
			/**
			 * pattern to use for validation
			 */
			private Pattern pattern = Pattern.compile(regex);

			/**
			 * {@inheritDoc}
			 */
			public boolean validate(String text) {
				Matcher matcher = pattern.matcher(text);
				boolean result = matcher.matches();
				return result;
			}
		});
	}

}
