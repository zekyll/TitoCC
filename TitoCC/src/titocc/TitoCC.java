package titocc;

import javax.swing.SwingUtilities;
import titocc.gui.UserInterface;

/**
 * Main class for the program.
 */
public class TitoCC
{
	/**
	 * Program entry point.
	 *
	 * @param args command line arguments, not used
	 */
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new UserInterface());
	}
}
