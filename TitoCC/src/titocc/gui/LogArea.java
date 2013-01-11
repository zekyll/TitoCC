package titocc.gui;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Component for displaying log messages.
 */
public class LogArea extends JScrollPane implements MessageLog
{
	/**
	 * The actual text area that this log area uses.
	 */
	private JTextArea logTextArea;

	/**
	 * Constructs a new LogArea with empty contents.
	 */
	public LogArea()
	{
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		setViewportView(logTextArea);
	}

	@Override
	public void logMessage(String message)
	{
		logTextArea.append(message + "\n");
	}

	/**
	 * Clears log contents.
	 */
	public void clear()
	{
		logTextArea.setText("");
	}
}
