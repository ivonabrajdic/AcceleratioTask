package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class provides the application with the Graphic User Interface and all of its
 * functionalities.
 * 
 * On the first start, the file favorites.txt is empty and the user can enter
 * the absolute path to the desired directory. The application then prints the
 * File Tree containing all of the subfiles an subfolders of the entered
 * directory. The user is expected to select a desired file and add it to
 * favorite files by pressing the "Add to Favorites" button. Favorite files are
 * added to the favorites.txt file.
 * 
 * On the second start of the application, the File Tree of the directory
 * entered in the last run is printed along with marks ***FAVORITE*** next to
 * the files added to favorites in the last run.
 * 
 * On closing the application after its second run, favorite.txt files contents
 * are deleted.
 * 
 * @author Ivona Brajdic
 *
 */
public class FileBrowser extends JFrame {

	private static final long serialVersionUID = 1L;
	JButton inputButton = new JButton("View");
	JButton favorites = new JButton("Add to Favorites");
	JTextField enterText = new JTextField();
	JLabel label = new JLabel();
	String path = "";
	JTree tree = new JTree();
	Container contentPane;
	static boolean firstStart = true;
	String text = "";
	String pathToNode = null;
	static PrintWriter out = null;
	boolean firstClick = true;

	public FileBrowser() {
		try {
			// Set the required look and feel
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		setTitle("Acceleratio Directory Tree");
		setSize(600, 400);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initGUI();
		setLocationRelativeTo(null);
	}

	private void initGUI() {
		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		enterText.setBorder(BorderFactory.createTitledBorder("Enter path to directory"));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(enterText);
		panel.add(inputButton);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.LINE_AXIS));
		panel2.add(Box.createHorizontalGlue());
		panel2.add(label);
		panel2.add(Box.createHorizontalGlue());
		panel2.add(favorites);

		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get("favorites.txt"));
		} catch (IOException e1) {

		}
		try {
			text = new String(encoded, "UTF-8");
		} catch (UnsupportedEncodingException e1) {

		}
		if (!text.equals("")) {
			firstStart = false;
			String[] lines = text.split("\n");
			path = lines[0].replace("\n", "").replace("\r", "");
			enterText.setText(path);
			label.setText(path);
			TreePrint(path);
		}

		inputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				path = enterText.getText();
				if (path.equals("")) {
					label.setText("Please enter path to directory.");
				} else {
					label.setText(path);
					TreePrint(path);
				}
			}
		});

		contentPane.add(panel, BorderLayout.PAGE_START);
		contentPane.add(panel2, BorderLayout.PAGE_END);

		favorites.setEnabled(false);
	}

	public void TreePrint(String pathToFile) {
		File main = new File(pathToFile);
		if (!main.exists()) {
			label.setText("File doesn't exist!");
		} else {

			favorites.setEnabled(true);

			String[] parts = pathToFile.split("\\\\");
			DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[parts.length];

			for (int i = 0; i < parts.length; i++) {
				nodes[i] = new DefaultMutableTreeNode(parts[i]);
			}

			if (main.isDirectory()) {
				File[] listOfFiles = main.listFiles();

				if (listOfFiles.length > 0) {

					List<DefaultMutableTreeNode> list = new ArrayList<>();

					if (firstStart) {
						for (int i = 0; i < listOfFiles.length; i++) {
							list.add(i, new DefaultMutableTreeNode(listOfFiles[i].toString()));
						}
					} else {

						enterText.setEnabled(false);
						favorites.setEnabled(false);
						inputButton.setEnabled(false);

						String[] lines = text.split("\n");

						for (int i = 0; i < listOfFiles.length; i++) {
							String fav = "";
							for (int j = 1; j < lines.length; j++) {
								String p = lines[j].replace("\n", "").replace("\r", "");
								if (listOfFiles[i].toString().contains(p)) {
									fav = "  ***FAVORITE***";
									break;
								}
							}
							list.add(i, new DefaultMutableTreeNode(listOfFiles[i].toString() + fav));

						}
					}

					for (int i = 0; i < listOfFiles.length; i++) {
						nodes[(parts.length) - 1].add(list.get(i));
					}

				}
			}
			for (int i = ((parts.length) - 1); i >= 1; i--) {
				nodes[i - 1].add(nodes[i]);
			}

			try {
				out = new PrintWriter("favorites.txt", "UTF-8");
			} catch (FileNotFoundException | UnsupportedEncodingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			favorites.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					if (!main.isDirectory()) {
						return;
					}

					if (firstClick) {
						out.println(path);
						firstClick = false;
					}

					if (pathToNode != null) {
						out.println(pathToNode);
						System.out.println(pathToNode);
						label.setText("File " + pathToNode + " successfully added to favorites.");
					} else {
						label.setText("Please select file to add to favorites.");
					}
				}
			});

			tree = new JTree(nodes[0]);

			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

					/* if nothing is selected */
					if (node == null)
						return;

					/* retrieve the node that was selected */
					pathToNode = node.toString();

				}
			});

			JScrollPane scroll = new JScrollPane(tree);
			contentPane.add(scroll, BorderLayout.CENTER);
			repaint();
		}

	}

	public static void main(String[] args) {
		FileBrowser fb = new FileBrowser();
		SwingUtilities.invokeLater(() -> {
			fb.setVisible(true);
		});

		fb.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// close the printwriter used to write favorites
				// to file favorites.txt
				if (out != null) {
					out.close();
				}
				e.getWindow().dispose();
				// if it's the second start of the program and we're closing it
				// delete all favorites from file so the next start will be
				// 'the first start'
				if (!firstStart) {
					PrintWriter writer;
					try {
						writer = new PrintWriter("favorites.txt");
						writer.print("");
						writer.close();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}

}
