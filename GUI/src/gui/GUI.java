package gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

import java.net.URI;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

// class to be used whenever a GUI is required or wanted
public class GUI {
	
	class OpenURI implements ActionListener { // for traveling to links upon clicking them
		
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
	
	public GUI() {
		setLookAndFeel();
	}
	
	Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	
	Font GUIFont = new Font("Book Antiqua", Font.PLAIN, 20);
	
	String htmlTab = "&nbsp;&nbsp;&nbsp;";
	
	public GUI (String[] buttons) {
		JFrame fullscreenFrame = new JFrame();
		fullscreenFrame.setSize(screensize);
	}
	
	public int displayOptions (String heading, String[] options) {
		JComboBox<String> listOfOptions = new JComboBox<String>(options);
		listOfOptions.setFont(GUIFont);
		JFrame displayOptionsFrame = new JFrame();
		JOptionPane.showMessageDialog(displayOptionsFrame, listOfOptions, heading, JOptionPane.INFORMATION_MESSAGE);
		return listOfOptions.getSelectedIndex();
	}
	
	public String get (String query) {
		query = tabAndNewlineTranslator(query);
		JLabel vessel = new JLabel(query, SwingConstants.CENTER);
		vessel.setFont(GUIFont);
		JFrame inputFrame = new JFrame();
		return JOptionPane.showInputDialog(inputFrame, vessel, "Input", JOptionPane.QUESTION_MESSAGE);
	}
	
	public Font getFont() {
		return GUIFont;
	}
	
	public Dimension getScreensize() {
		return screensize;
	}
	
	public void give (String message) {
		message = tabAndNewlineTranslator(message);
		JLabel vessel = new JLabel(message, SwingConstants.CENTER);
		vessel.setFont(GUIFont);
		JFrame outputFrame = new JFrame();
		JOptionPane.showMessageDialog(outputFrame, vessel, "Message", JOptionPane.INFORMATION_MESSAGE, UIManager.getIcon("OptionPane.informationIcon"));
	}
	
	public void giveThrowable (Throwable gah) {
		String gahDescriptionAndMessage = "Short Description - " + gah.toString() + "\n\n"
										+ "Detail Message String - " + ((gah.getMessage() != null) ? (gah.getMessage()) : ("(No detail message string)")); // if getMessage() doesn't return null, show its return; else, show custom text
		gahDescriptionAndMessage = tabAndNewlineTranslator(gahDescriptionAndMessage);
		JLabel vessel = new JLabel(gahDescriptionAndMessage, SwingConstants.CENTER);
		vessel.setFont(GUIFont);
		JFrame outputFrame = new JFrame();
		String[] option = {"Continue"};
		JOptionPane.showOptionDialog(outputFrame, vessel, "Warning!", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE, UIManager.getIcon("OptionPane.warningIcon"), option, 0);
	}
	
	public JFrame giveTextArea (String message, int xCoordinate, int yCoordinate, int fractionOfWidth, int fractionOfHeight) {
		Dimension frameOutline = new Dimension(getScreensize().width / fractionOfWidth, getScreensize().height / fractionOfHeight); // what fraction of the display should this text area take up
		JTextArea txtAr = new JTextArea();
		JScrollPane scrllPn = new JScrollPane(txtAr);
		txtAr.setEditable(false);
		txtAr.append(message);
		txtAr.setFont(getFont());
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
		jeff.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); // won't be blocked by application-modal dialogs
		jeff.pack();
		jeff.setSize(frameOutline);
		jeff.setVisible(true);
		return jeff;
	}
	
	public int giveYesNo (String yesNoQuestion) {
		yesNoQuestion = tabAndNewlineTranslator(yesNoQuestion);
		JLabel vessel = new JLabel(yesNoQuestion, SwingConstants.CENTER);
		vessel.setFont(GUIFont);
		JFrame outputYNFrame = new JFrame();
		String[] responses = {"Yes", "No", "Quit"};
		return JOptionPane.showOptionDialog(outputYNFrame,
											vessel,
											"Y/N/Q",
											JOptionPane.YES_NO_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE,
											UIManager.getIcon("OptionPane.informationIcon"),
											responses,
											responses[responses.length - 1]);
	}
	
	public OpenURI linkTo (URI destination) { // for using the OpenURI class
		return new OpenURI(destination);
	}
	
	public void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException cnfe) {
			giveThrowable(cnfe);
		} catch (IllegalAccessException iae) {
			giveThrowable(iae);
		} catch (InstantiationException ie) {
			giveThrowable(ie);
		} catch (UnsupportedLookAndFeelException ulafe) {
			giveThrowable(ulafe);
		}
	}
	
	public String tabAndNewlineTranslator(String text) { // interprets and changes '\t' and '\n' characters so that their effects will be accurately shown
		String translatedText = "";
		if (text.contains("\t")) { // using html for tabs
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
			for (int counter = 0; counter < text.length(); counter ++) { // filling "theLinesOfText"
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
			for (int counter = 0; counter < theLinesOfText.length; counter ++) { // using html for newlines
				theLinesOfText[counter] = theLinesOfText[counter] + "<br\\>";
				text += theLinesOfText[counter];
			}
			text += "</html>";
		}		
		translatedText = text;
		return translatedText;
	}
}