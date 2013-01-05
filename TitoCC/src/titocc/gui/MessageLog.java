package titocc.gui;

/**
 * Interface for logging compilation messages etc.
 */
public interface MessageLog
{
	/**
	 * Adds a new message to the log.
	 *
	 * @param message message to be logged
	 */
	void logMessage(String message);
}
