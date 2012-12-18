package titocc;

import javax.swing.SwingUtilities;
import titocc.gui.UserInterface;

public class TitoCC
{
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new UserInterface());
	}
}
