package titocc.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import titocc.compiler.Parser;
import titocc.compiler.Compiler;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.Tokenizer;

public class UserInterface implements Runnable, ActionListener
{
	private JFrame frame;
	private JMenuItem openItem, exitItem, saveItem, compileItem;
	private JTextArea sourceTextArea, errorLogTextArea, outputTextArea;

	@Override
	public void run()
	{
		frame = new JFrame("TitoCC");
		frame.setPreferredSize(new Dimension(600, 500));

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		createComponents(frame.getContentPane());

		frame.setMinimumSize(new Dimension(200, 200));
		frame.pack();
		frame.setVisible(true);
	}

	private void createComponents(Container container)
	{
		container.setLayout(new BorderLayout(5, 5));

		createMenu();

		sourceTextArea = new JTextArea();
		sourceTextArea.setText("int main()\n{\n\treturn 0;\n}");
		sourceTextArea.setFont(new Font("Monospaced", 0, 12));
		container.add(sourceTextArea, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout(5, 5));
		frame.add(rightPanel, BorderLayout.EAST);

		outputTextArea = new JTextArea();
		outputTextArea.setEditable(false);
		outputTextArea.setPreferredSize(new Dimension(300, 999));
		outputTextArea.setLineWrap(true);
		outputTextArea.setFont(new Font("Monospaced", 0, 12));
		rightPanel.add(outputTextArea, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout(5, 5));
		frame.add(bottomPanel, BorderLayout.SOUTH);

		errorLogTextArea = new JTextArea();
		errorLogTextArea.setEditable(false);
		errorLogTextArea.setPreferredSize(new Dimension(999, 80));
		bottomPanel.add(errorLogTextArea, BorderLayout.CENTER);
	}

	private void createMenu()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		openItem = createMenuItem(fileMenu, "Open...");
		saveItem = createMenuItem(fileMenu, "Save");
		compileItem = createMenuItem(fileMenu, "Compile");
		exitItem = createMenuItem(fileMenu, "Exit");

		compileItem.setAccelerator(KeyStroke.getKeyStroke("F6"));

		frame.add(menuBar, BorderLayout.NORTH);
	}

	private JMenuItem createMenuItem(JMenu menu, String caption)
	{
		JMenuItem item = new JMenuItem(caption);
		item.addActionListener(this);
		menu.add(item);
		return item;
	}

	private void compile()
	{
		clearLog();
		String sourceCode = sourceTextArea.getText();
		Tokenizer tokenizer = new Tokenizer(new StringReader(sourceCode));
		try {
			List<Token> tokens = tokenizer.tokenize();
			addLogItem("Tokenizing completed successfully.");

			outputTextArea.setText("");
			//outputTextArea.setText("TOKENS:\n");
			//for (Token t : tokens)
			//	outputTextArea.append(t.toString() + "\n");

			Parser parser = new Parser(tokens);
			TranslationUnit trUnit = parser.parse();
			addLogItem("Parsing completed successfully.");
			//outputTextArea.append("\nCODE ELEMENTS:\n");
			//outputTextArea.append(trUnit.toString());

			Compiler compiler = new Compiler(trUnit);
			StringWriter writer = new StringWriter();
			compiler.compile(writer);
			addLogItem("Compilation completed successfully.");
			//outputTextArea.append("\n\nTTK-91 ASSEMBLY:\n");
			outputTextArea.append(writer.toString());

		} catch (SyntaxException e) {
			int line = e.getLine() + 1;
			int character = e.getColumn() + 1;
			addLogItem("Compiler error (line " + line + ", ch " + character + "): " + e.getMessage());
		} catch (Exception e) {
			addLogItem("Compilation failed: " + e.getMessage());
		}
	}

	private void clearLog()
	{
		errorLogTextArea.setText("");
	}

	private void addLogItem(String message)
	{
		errorLogTextArea.append(message + "\n");
	}

	public JFrame getFrame()
	{
		return frame;
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == exitItem)
			System.exit(0);
		else if (ae.getSource() == compileItem)
			compile();
	}
}
