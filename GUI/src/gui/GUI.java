package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.net.URI;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

// class to be used whenever a GUI is required or wanted
public class GUI {
	
	class OpenURI implements ActionListener { // for traveling to links upon clicking them (possible with JButton, possible for anything that can have an action listener? I feel like the answer is yes...)
		
		URI uri;
		
		OpenURI(URI URIFromUser) {
			uri = URIFromUser;
		}
		
		public void actionPerformed(ActionEvent aella) {
			openURI();
		}
		
		void openURI() {
			GUI gui = new GUI();
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(uri);
				} catch (IOException ioe) {
					gui.give("Error!\n\n" + ioe);
				}
			} else {
				gui.give("Desktop class not supported on current platform!");
			}
		}
	}
	
	public GUI() {}
	
	// final ImageIcon icon = new ImageIcon("C:\\Users\\NickBej\\Pictures\\superSmileIcon(Rainbow-Machine).jpg");
	
	Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	
	Font GUIFont = new Font("Book Antiqua", Font.PLAIN, 20);
	
	String htmlTab = "&nbsp;&nbsp;&nbsp;";
	
	//String htmlFont = "<html><font name = 'Book Antiqua' size = '4'>";
	
	public GUI (String[] buttons) {
		JFrame fullscreenFrame = new JFrame();
		fullscreenFrame.setSize(screensize);
	}
	
	public int displayOptions (String heading, String[] options) {
		JComboBox<String> listOfOptions = new JComboBox<String>(options);
		listOfOptions.setFont(GUIFont);
		JOptionPane.showMessageDialog(null, listOfOptions, heading, 1);
		return listOfOptions.getSelectedIndex();
	}
	
	public String get (String query) {
		query = tabAndNewlineTranslator(query);
		JLabel vessel = new JLabel(query);
		vessel.setFont(GUIFont);
		JFrame inputFrame = new JFrame();
		return JOptionPane.showInputDialog(inputFrame, vessel);
	}
	
	public Font getFont() { // accesses "font" in the class "GUI"
		return GUIFont;
	}
	
	public Dimension getScreensize() { // accesses the dimension "screensize" in the class "GUI"
		return screensize;
	}
	
	public void give (String message) {
		message = tabAndNewlineTranslator(message);
		JLabel vessel = new JLabel(message);
		vessel.setFont(GUIFont);
		JFrame outputFrame = new JFrame();
		JOptionPane.showMessageDialog(outputFrame, vessel, "Message", 1, UIManager.getIcon("OptionPane.informationIcon"));
	}
	
	public JFrame giveTextArea (String message, int xCoordinate, int yCoordinate) {
		Dimension frameOutline = new Dimension(getScreensize().width, getScreensize().height / 3);
		JTextArea txtAr = new JTextArea();
		JScrollPane scrllPn = new JScrollPane(txtAr);
		txtAr.setEditable(false);
		txtAr.append(message);
		Font fnt = new Font("Helvetica", Font.BOLD, 24);
		txtAr.setFont(fnt);
		JFrame jeff = new JFrame();
		jeff.add(scrllPn, BorderLayout.CENTER);
		jeff.setLocation(xCoordinate, yCoordinate);
		JButton doneReading = new JButton("I'm Done Reading.");
		doneReading.setVerticalTextPosition(AbstractButton.BOTTOM);
		doneReading.setHorizontalTextPosition(AbstractButton.CENTER);
		doneReading.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent ae) {
				jeff.dispose();
			}
		});
		jeff.add(doneReading, BorderLayout.PAGE_END);
		jeff.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
		jeff.pack();
		jeff.setSize(frameOutline);
		jeff.setVisible(true);
		return jeff;
	}
	
	public int giveYesNo (String yesNoQuestion) {
		yesNoQuestion = tabAndNewlineTranslator(yesNoQuestion);
		JLabel vessel = new JLabel(yesNoQuestion);
		vessel.setFont(GUIFont);
		JFrame outputYNFrame = new JFrame();
		String[] responses = {"Yes", "No", "Quit"};
		return JOptionPane.showOptionDialog(outputYNFrame,
											vessel,
											null,
											JOptionPane.YES_NO_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											UIManager.getIcon("OptionPane.informationIcon"),
											responses,
											responses[responses.length - 1]);
	}
	
	public OpenURI linkTo (URI destination) { // for using the OpenURI class
		return new OpenURI(destination);
	}
	
	public String tabAndNewlineTranslator(String text) { // interprets and changes '\t' and '\n' characters so that their effects will be accurately shown
		String translatedText = "";
		if (text.contains("\t")) {
			text = text.replaceAll("\t", htmlTab);
		}
		if (text.contains("\n")) { // if there are multiple lines in the message
			int numOfLines = 1;
			for (int counter = 0; counter < text.length(); counter++) { // determining how many lines are in the message
				if (text.charAt(counter) == '\n') {
					numOfLines ++;
				}
			}
			String[] theLinesOfText = new String[numOfLines];
			String currentLineOfText = "";
			int theLinesOfTextCounter = 0;
			for (int counter = 0; counter < text.length(); counter ++) {
				if (counter < text.length() && text.charAt(counter) != '\n') {
					currentLineOfText += text.charAt(counter);
				}
				if (text.charAt(counter) == '\n') {
					theLinesOfText[theLinesOfTextCounter] = currentLineOfText;
					theLinesOfTextCounter ++;
					currentLineOfText = "";
				}
			}
			theLinesOfText[theLinesOfTextCounter] = currentLineOfText;
			text = "<html>";
			for (int counter = 0; counter < theLinesOfText.length; counter ++) {
				theLinesOfText[counter] = theLinesOfText[counter] + "<br\\>";
				text += theLinesOfText[counter];
			}
			text += "</html>";
		}		
		translatedText = text;
		return translatedText;
	}
}