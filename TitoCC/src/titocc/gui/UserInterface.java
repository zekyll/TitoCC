package titocc.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Graphical frontend for the compiler. Allows opening, editing, saving and
 * compiling source files.
 */
public class UserInterface implements Runnable, ActionListener, DocumentListener
{
	/**
	 * GUI components.
	 */
	private JFrame frame;
	private JMenuItem openItem, exitItem, saveItem, saveAsItem, compileItem;
	private JCheckBoxMenuItem saveOnCompileItem, createOutputFileItem;
	private JTextArea sourceTextArea, outputTextArea;
	private LogArea logArea;
	private JFileChooser fileChooser;
	/**
	 * Source file object.
	 */
	private SourceFile sourceFile;

	/**
	 * Initializes the user interface.
	 */
	@Override
	public void run()
	{
		sourceFile = new SourceFile();

		createFrame();

		sourceTextArea.setDocument(sourceFile.getDocument());
		sourceTextArea.getDocument().addDocumentListener(this);

		sourceFile.createNewFile();
		updateTitle(false);
	}

	/**
	 * Creates the main window frame and all the components.
	 */
	private void createFrame()
	{
		frame = new JFrame("TitoCC");

		frame.setPreferredSize(new Dimension(650, 500));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		createComponents(frame.getContentPane());

		frame.setMinimumSize(new Dimension(200, 200));
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Creates all the conpoments of the main window.
	 *
	 * @param container container in which the components are created
	 */
	private void createComponents(Container container)
	{
		container.setLayout(new BorderLayout());

		createMenu();
		createFileChooser();

		sourceTextArea = new JTextArea();
		sourceTextArea.setFont(new Font("Monospaced", 0, 12));

		outputTextArea = new JTextArea("Compile the file from File -> Compile.");
		outputTextArea.setEditable(false);
		outputTextArea.setFont(new Font("Monospaced", 0, 12));
		JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
		outputScrollPane.setPreferredSize(new Dimension(300, 0));

		JSplitPane midSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(sourceTextArea), outputScrollPane);
		midSplitPane.setDividerSize(4);
		midSplitPane.setResizeWeight(1);
		midSplitPane.setContinuousLayout(true);
		disableF6Shortcut(midSplitPane);

		logArea = new LogArea();
		logArea.setPreferredSize(new Dimension(0, 80));

		JSplitPane clientSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				midSplitPane, logArea);
		clientSplitPane.setDividerSize(4);
		clientSplitPane.setResizeWeight(1);
		clientSplitPane.setContinuousLayout(true);
		disableF6Shortcut(clientSplitPane);

		container.add(clientSplitPane);
	}

	/**
	 * Disables the default F6 shortcut used by a split pane.
	 *
	 * @param splitPane split pane object whose shortcuts will be disabled
	 */
	private void disableF6Shortcut(JSplitPane splitPane)
	{
		InputMap inputMap = splitPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "none");
	}

	/**
	 * Creates the main menu and all the items.
	 */
	private void createMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		openItem = createMenuItem(fileMenu, "Open...");
		saveItem = createMenuItem(fileMenu, "Save");
		saveAsItem = createMenuItem(fileMenu, "Save as...");
		compileItem = createMenuItem(fileMenu, "Compile");
		exitItem = createMenuItem(fileMenu, "Exit");

		saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		compileItem.setAccelerator(KeyStroke.getKeyStroke("F6"));

		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);

		saveOnCompileItem = createCheckBoxMenuItem(optionsMenu, "Save on compile");
		createOutputFileItem = createCheckBoxMenuItem(optionsMenu, "Create .k91 output file");

		frame.add(menuBar, BorderLayout.NORTH);
	}

	/**
	 * Creates a single menu item from given caption and adds it to a menu.
	 *
	 * @param menu menu in which the item is added
	 * @param caption caption of the new item
	 * @return created item
	 */
	private JMenuItem createMenuItem(JMenu menu, String caption)
	{
		JMenuItem item = new JMenuItem(caption);
		item.addActionListener(this);
		menu.add(item);
		return item;
	}

	/**
	 * Creates a checkbox menu item with given caption and adds it to a menu.
	 *
	 * @param menu menu in which the item is added
	 * @param caption caption of the new item
	 * @return created item
	 */
	private JCheckBoxMenuItem createCheckBoxMenuItem(JMenu menu, String caption)
	{
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(caption);
		item.addActionListener(this);
		menu.add(item);
		return item;
	}

	/**
	 * Creates the file chooser and sets the filters.
	 */
	private void createFileChooser()
	{
		fileChooser = new JFileChooser();

		FileNameExtensionFilter filter = new FileNameExtensionFilter("C source files (*.c)", "c");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileFilter(filter);
	}

	/**
	 * Shows error dialog on the screen.
	 *
	 * @param title title of the dialog
	 * @param message error message
	 */
	private void showErrorMessage(String title, String message)
	{
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Handles document update events.
	 *
	 * @param de
	 */
	@Override
	public void insertUpdate(DocumentEvent de)
	{
		updateTitle(true);
	}

	/**
	 * Handles document remove events.
	 *
	 * @param de
	 */
	@Override
	public void removeUpdate(DocumentEvent de)
	{
		updateTitle(true);
	}

	/**
	 * Handles document changed events.
	 *
	 * @param de
	 */
	@Override
	public void changedUpdate(DocumentEvent de)
	{
	}

	/**
	 * Updates the window title with the name of the opened source file.
	 *
	 * @param modified if true prefixes the title with "*"
	 */
	private void updateTitle(boolean modified)
	{
		frame.setTitle((modified ? "* " : "") + sourceFile.getName() + " - TitoCC");
	}

	/**
	 * Handles menu actions.
	 *
	 * @param ae
	 */
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == openItem)
			open();
		else if (ae.getSource() == compileItem)
			compile();
		else if (ae.getSource() == saveItem)
			save();
		else if (ae.getSource() == saveAsItem)
			saveAs();
		else if (ae.getSource() == exitItem)
			System.exit(0);
	}

	/**
	 * Displays file selection dialog and opens the selected file in editor.
	 */
	private void open()
	{
		fileChooser.setSelectedFile(new File(""));
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			try {
				sourceFile.readFromFile(fileChooser.getSelectedFile());
				outputTextArea.setText("");
				logArea.clear();
				updateTitle(false);
			} catch (FileNotFoundException e) {
				showErrorMessage("File open error", "File could not be opened: " + e.getMessage());
			}
		}
	}

	/**
	 * Saves the currently opened file. If no physical file is associated with
	 * the source file then displays the file chooser.
	 */
	private void save()
	{
		if (sourceFile.getFile() != null)
			save(sourceFile.getFile());
		else
			saveAs();
	}

	/**
	 * Saves the source file to a file selected with the file chooser.
	 */
	private void saveAs()
	{
		File file = sourceFile.getFile();
		if (file == null)
			file = new File(sourceFile.getName());
		fileChooser.setSelectedFile(file);
		if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
			save(fileChooser.getSelectedFile());
	}

	/**
	 * Saves the currently opened source file to specific file.
	 *
	 * @param file file in which the contents are written
	 */
	private void save(File file)
	{
		try {
			sourceFile.writeToFile(file);
			updateTitle(false);
		} catch (IOException e) {
			showErrorMessage("Write error", "An error occured when writing the file: "
					+ e.getMessage());
		}
	}

	/**
	 * Compiles the file (writing output file according to options) and displays
	 * the output.
	 */
	private void compile()
	{
		if (saveOnCompileItem.getState())
			save();

		if (createOutputFileItem.getState() && sourceFile.getFile() == null) {
			JOptionPane.showMessageDialog(frame, "Source file must be saved before generating "
					+ "the output file.", "Save", JOptionPane.PLAIN_MESSAGE);
			return;
		}

		logArea.clear();

		Writer writer = new StringWriter();
		try {
			sourceFile.compile(logArea, writer, createOutputFileItem.getState());
		} catch (IOException e) {
			showErrorMessage("Write error", "Error occured when writing file to disk: "
					+ e.getMessage());
		}
		outputTextArea.setText(writer.toString());
	}
}
