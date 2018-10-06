package collatzConjecture;

import gui.GUI;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

// program can test a range of numbers or an individual number within the context of the Collatz Conjecture and provides various details
// (number's path to 1 (and steps); shortest, median, mean, mode, and longest paths; a line graph of path length vs. starting number)
// [Possible addition: a graph tracing all of the paths from starting number to "big river" (all of the power of 2s down which each path flows upon entering)]

class IntComparison implements Comparator<Integer> { // used to order flowPoints in increasing sequence
	public int compare (Integer leftOfPair, Integer rightOfPair) {
		return leftOfPair - rightOfPair;
	}
}

class LinksToResourcesRunnable extends Thread implements Runnable {
	
	LinksToResourcesRunnable (GUI givenGUI) {
		gui = givenGUI;
		return;
	}
	
	boolean interrupted = false;
	
	GUI gui;
	
	JFrame studyUp = new JFrame(); // will contain buttons linking to articles about the Collatz Conjecture
	
	LinksToResourcesRunnable LTRR;
	
	public boolean isRunning() {
		return !interrupted;
	}
	
	public void interrupt() {
		interrupted = true;
		studyUp.dispose();
	}
	
	public void run() {
		Thread LTRRThread = new Thread(LTRR);
		GridLayout studyUpStructure = new GridLayout(1, 2);
		studyUp.setLayout(studyUpStructure);
		studyUp.setFocusable(true);
		studyUp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		studyUp.addWindowListener(new WindowAdapter() { // specific frame closing behavior
			public void windowClosing(WindowEvent we) {
				interrupted = true;
			}
		});
		JButton wikipediaLink = new JButton("What Wikipedia Has To Say"); // button linking to wikipedia article
		try {
			URI CCOnWikipedia = new URI("https://en.wikipedia.org/wiki/Collatz_conjecture"); // making the URI
			wikipediaLink.addActionListener(gui.linkTo(CCOnWikipedia)); // making it so that upon clicking the URI is opened
			studyUp.add(wikipediaLink); // adding the button to the JFrame
		} catch (URISyntaxException urise) {
			gui.giveThrowable(urise);
		}
		JButton wolframMathWorldLink = new JButton("What Wolfram MathWorld Has To Say"); // button linking to the Wolfram MathWorld article
		try {
			URI CCOnWolframMathWorld = new URI("http://mathworld.wolfram.com/CollatzProblem.html"); // making the URI
			wolframMathWorldLink.addActionListener(gui.linkTo(CCOnWolframMathWorld)); // making it so upon clicking the URI is opened
			studyUp.add(wolframMathWorldLink); // adding the button to the JFrame
		} catch (URISyntaxException urise) {
			gui.giveThrowable(urise);
		}
		studyUp.pack();
		int studyUpCenterXCoordinate = studyUp.getWidth() / 2;
		int studyUpCenterYCoordinate = studyUp.getHeight() / 2;
		studyUp.setLocation((gui.getScreensize().width / 2) - studyUpCenterXCoordinate, (gui.getScreensize().height / 5) - studyUpCenterYCoordinate);
		while (isRunning()) {
			studyUp.setVisible(true);
		}
	}
}

class ArrayListFirstIntComparison implements Comparator<ArrayList<Integer>> { // sort the first index of the second dimension of "notedStartingNumberAndSteps" (startingNumber)
	public int compare (ArrayList<Integer> leftOfPair, ArrayList<Integer> rightOfPair) {
		return leftOfPair.get(0) - rightOfPair.get(0);
	}
}

class ArrayListSecondIntComparison implements Comparator<ArrayList<Integer>> { // used to order the "steps" of "notedStartingNumberAndSteps"
	public int compare (ArrayList<Integer> leftOfPair, ArrayList<Integer> rightOfPair) {
		return leftOfPair.get(1) - rightOfPair.get(1);
	}
}

class TestingNumsInTheCollatzConjecture {
	
	static ArrayList<ArrayList<Integer>> notedStartingNumberAndSteps = new ArrayList<ArrayList<Integer>>(); // for finding the shortest, median, and longest paths, and for constructing the line graph of path length vs. starting number
	
	static ArrayList<ArrayList<Integer>> testedRanges = new ArrayList<ArrayList<Integer>>(); // will hold all of the ranges (beginning and end numbers) that have been run through
	
	static ArrayList<ArrayList<Integer>> startingNumberAndFlowPoints = new ArrayList<ArrayList<Integer>>(); // holds the starting number and the first power of 2 its path encounters (and, in other words, where the path joins up with the "big river" of powers of 2 (it's a big river because once a path goes there it simply flows down to 1))
	
	static ArrayList<String> notedSequence = new ArrayList<String>(); // for memoization
	
	static GUI gui = new GUI();
	
	static int notedStartingNumberAndStepsStepFiller = 0; // for filling empty spots in "notedStartingNumberAndSteps"
	
	static List<ArrayList<Integer>> rangedStartingNumberAndFlowPoints = new ArrayList<ArrayList<Integer>>(); // will hold the starting numbers and corresponding flow points from the recently-tested range of "startingNumberAndFlowPoints"
	
	static List<ArrayList<Integer>> rangedStartingNumberAndSteps; // will hold the recently-tested range from "notedStartingNumberAndSteps"
	
	static String notedSequenceFiller = "(empty)"; // for filling empty spots in "noted"
	
	static String[] floorAndCeilingValues = new String[2]; // will hold the beginning and end of the last range tested
	
	public static void main (String[] args) {
		gui.give("Welcome to Collatz Conjecture Analytics!\n"														 // Introductions...
			   + "Here you can test individual/multiple numbers and observe trends within the Collatz Conjecture."); // .
		LinksToResourcesRunnable information = new LinksToResourcesRunnable(gui);
		int userAnswer = gui.giveYesNo("If you're unfamiliar with the Collatz Conjecture, that's perfectly fine!\n\n"
									 + "Click \"Yes\" to read up on it (I'll wait; I'm very patient);\n"
									 + "Click \"No\" to skip it;\n"
									 + "Click \"Quit\" to end the whole program."); // educational resources about the Collatz Conjecture can be made available
		if (userAnswer == 0) { // answer was "Yes"
			information.start();
			try {
				information.join();
			} catch (InterruptedException ie) {
				gui.giveThrowable(ie);
			}
		} else if (userAnswer == 2) { // answer was "Quit"
			goodbye();
		}
		information.interrupt(); // tidying up just in case
		gui.give("With this program, you'll:\n"																		     // Introductions cont'd.
			   + "\t\t> numerically see the path of travels from starting number to 1 through the Collatz Conjecture;\n" // .
			   + "\t\t> find the shortest, median, mode, mean, and longest paths (if you test a range);\n"			     // .
			   + "\t\t> know the \"flow gates\" (this and \"flow point\" are personal terminology)\n"					 // .
			   + "\t\t\t\t(a flow point is the first power of 2 that a path encounters, while\n"	     				 // .
			   + "\t\t\t\ta flow gate is a flow point that has numerous paths encountering it, the proportion\n" 		 // .
			   + "\t\t\t\tof which to the total number of paths meets or surpasses a threshold percentage)\n"			 // .
			   + "\t\t\t\t[range testing will show the flow gates; individual testing will show the flow point]\n"	     // .
			   + "\t\t> and display a path length vs. starting number line graph (if you test a range).\n");			 // .
		// f(x) = 2,015,911,748.5e^(0.0005570598x)
		// So, some explaining of the above. I had timed various trials of the program, then plotted the number of tested numbers (ranging from 1000 - 7000 tested numbers) on the x-axis with runtimes on the y-axis [in OpenOffice Calc].
		// The exponential graph of f(x) = 0.7753506725e^(0.0005570598x) had an R-Squared of 0.95627. That equation, however, is with a processor speed of 2.6 GHz. I wanted to figure out how the processor speed affected the function.
		// The first function is a general function which will be adjusted to the processing speed of the computer running the program, by dividing the coefficient by the processing speed.
		String processorSpeedString = "";
		double processorSpeed = -2;
		while (true) {
			processorSpeedString = gui.get("One last thing: here, you can enter your processor speed in HERTZ,\n"
										 + "which will be used to tune an experimental calculations-runtime model to your specific computer.\n"
										 + "The model will be used to figure out the estimated time remaining for the calculations to a (hopefully!) near-accurate degree.\n\n"
										 + "This feature is optional, so if you want to skip it, just leave the field blank and hit \"OK\".\n"
										 + "(There will still be an estimated time remaining feature for you, albeit one using a different method)");
			if (processorSpeedString == null) { // "Cancel" was hit
				goodbye();
			}
			else if (processorSpeedString.contentEquals("")) { // field was left blank
				break;
			}
			try {
				processorSpeed = Double.parseDouble(processorSpeedString);
				if (Double.compare(processorSpeed, Math.round(processorSpeed)) != 0) { // the processor speed entered has a decimal
					gui.give("Your computer has a speed of " + (int) Math.floor(processorSpeed) + " and a fraction of a hertz?\n"
						   + "I'm suspicious...\n\n"
						   + "(Even if that is the case, please just give me an estimation of your choosing)");
					continue;
				}
			} catch (NumberFormatException nfe){
				gui.giveThrowable(nfe);
				gui.give("Please enter a number!");
				continue;
			}
			break;
		}
		double functionCoefficient = 0.0; // will hold the coefficient of the function after it is adjusted with the user's computer's processing speed
		if (processorSpeed != -2) { // user had entered their computer's processing speed
			functionCoefficient = 2015911748.5 / processorSpeed;
		}
		double xCoefficient = 0.0005570598; // for cleanliness
		int[] numberPair = acquireNumberPair(); // getting the range of numbers or single number the user wants to test and storing them/it in "numberPair"
		JFrame progressionThroughRange = new JFrame("Determining or Fetching Path(s)"); // will show the status of the progression through the range of numbers
		progressionThroughRange.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridLayout PTRStructure = new GridLayout(3, 1);
		progressionThroughRange.setLayout(PTRStructure);
		JProgressBar PTRProgressBar = new JProgressBar(); // the actual progress bar
		PTRProgressBar.setStringPainted(true);
		progressionThroughRange.getContentPane().add(PTRProgressBar);
		JLabel recentRuntimesEstimatedTimeRemaining = new JLabel();
		progressionThroughRange.getContentPane().add(recentRuntimesEstimatedTimeRemaining);
		JLabel modeledEstimatedTimeRemaining = new JLabel();
		progressionThroughRange.getContentPane().add(modeledEstimatedTimeRemaining);
		FontMetrics PTRFontMetrics = progressionThroughRange.getFontMetrics(gui.getFont()); // used in determining the size of the JFrame
		progressionThroughRange.setPreferredSize(new Dimension(PTRFontMetrics.stringWidth(progressionThroughRange.getTitle()) + 248, 100)); // 248 is the constant that needs to be added (is the total width of the buttons in the upper right corner?)
		progressionThroughRange.pack();
		gui.center(progressionThroughRange);
		String allInfo = ""; // will hold information gathered through the running of the program
		notedSequence.add("0\n"
						+ "Steps Taken: 0"); // the first value of "notedSequence"
		int ultramarineCounter = 0; // how many tests are done
			Ultramarine: // the testing loop
			while (true) {
				ultramarineCounter ++;
				if (numberPair[0] != numberPair[1]) { // testing a range
					allInfo += "---------- FROM " + numberPair[0] + " TO " + numberPair[1] + " ----------\n";
				} else { // testing a single number
					allInfo += "---------- FOR THE NUMBER " + numberPair[1] + " ----------\n";
				}
				progressionThroughRange.setVisible(true); // time to show the JFrame and JProgressBar (calculations ahead)
				if (numberPair[0] < numberPair[1]) { // a range is being tested
					allInfo += testRange(numberPair, progressionThroughRange, functionCoefficient, xCoefficient, processorSpeed);
				} else { // a single number is being tested
					if (numberPair[0] < notedSequence.size() - 1 && !notedSequence.get(numberPair[0]).equals(notedSequenceFiller)) { // the number has been calculated before
						allInfo += "↓Starting Number" + "\n" + notedSequence.get(numberPair[0]) + "\n\n";
					} else { // the number has not been calculated before
						allInfo += "↓Starting Number" + "\n" + hotpo(numberPair[0]) + "\n\n";
					}
				}
				progressionThroughRange.setVisible(false); // done with the calculations, so the JFrame and JProgressBar can be hidden
				int startOfRangeIndex = 0; // for holding the beginning of the range (just tested) in "notedStartingNumberAndSteps"
				ArrayListFirstIntComparison arrangingStartingNumInIncreasingOrder = new ArrayListFirstIntComparison();
				notedStartingNumberAndSteps.sort(arrangingStartingNumInIncreasingOrder); // make sure the starting numbers are in order
				while (notedStartingNumberAndSteps.get(startOfRangeIndex).get(0) != numberPair[0]) { // getting to the start of the recently tested range in "notedStartingNumberAndSteps"
					startOfRangeIndex ++;
				}
				int endOfRangeIndex = startOfRangeIndex + (numberPair[1] - numberPair[0] + 1); // "+1" because ".sublist" "arg2" is exclusive
				rangedStartingNumberAndSteps = notedStartingNumberAndSteps.subList(startOfRangeIndex, endOfRangeIndex); // done to avoid issues with "notedStartingNumberAndStepsStepFiller"
				ArrayListSecondIntComparison arrangingFlowPointsInIncreasingOrder = new ArrayListSecondIntComparison();
				startingNumberAndFlowPoints.sort(arrangingFlowPointsInIncreasingOrder);
				rangedStartingNumberAndFlowPoints.clear();
				int counter = 0;
				while (counter < startingNumberAndFlowPoints.size() && startingNumberAndFlowPoints.get(counter).get(0) >= numberPair[0] && startingNumberAndFlowPoints.get(counter).get(0) <= numberPair[1]) { // while the current starting number is within the range that was tested
					rangedStartingNumberAndFlowPoints.add(startingNumberAndFlowPoints.get(counter)); // add the starting number and flow point pair to "rangedFlowPoints"
					counter ++;
				}
				ArrayListSecondIntComparison arrangingStepsInIncreasingOrder = new ArrayListSecondIntComparison(); // for ordering the steps within "notedStartingNumberAndSteps" in increasing sequence
				rangedStartingNumberAndSteps.sort(arrangingStepsInIncreasingOrder);
				if (numberPair[0] < numberPair[1]) { // a range was tested, gathering various information about that range, and recording the start and end of the range
					String modalInformation = ""; // will hold the modal value of the tested range along with the modal path length(s) and corresponding starting numbers
					modalInformation = gatherModalInformation();
					String meanInformation = ""; // will state the average path length
					meanInformation = gatherMeanInformation();
					String[] leastMiddleAndMostInformation = gatherLeastMiddleAndMostInformation();
					allInfo += leastMiddleAndMostInformation[0] + leastMiddleAndMostInformation[1] + modalInformation + meanInformation + leastMiddleAndMostInformation[2]; // record them in "allSequencesAndSuch"
					allInfo += determineFlowGates(rangedStartingNumberAndFlowPoints);
					testedRanges.add(new ArrayList<Integer>(Arrays.asList(numberPair[0], numberPair[1]))); // recording the range
				} else { // a single number was tested
					allInfo += "The flow point is " + startingNumberAndFlowPoints.get(startingNumberAndFlowPoints.size() - 1).get(1) + ".";
				}
				int answer = gui.giveYesNo("Would you like to input different data?\n"
										 + "(Results are shown after all inputting is complete)"); // the user's response is stored
				if (answer == 0) { // response is "Yes"
					numberPair = acquireNumberPair(); // get a new range
					continue; // move to the beginning of Ultramarine
				} else if (answer == 1) { // response is "No"
					JFrame displayedAllInfo = gui.giveTextArea(allInfo, 0, 0, 1, 3); // displaying information gathered
					floorAndCeilingValues[0] = Integer.toString(numberPair[0]);
					floorAndCeilingValues[1] = Integer.toString(numberPair[1]);
					LineGraph.main(floorAndCeilingValues);
					break Ultramarine;
				} else if (answer == 2) { // response is "Quit"
					break Ultramarine;
				}
			}
		goodbye();
	}
	
	private static int[] acquireNumberPair() { // getting the range to be tested
		int[] numberPair = {0, 0}; // to hold the beginning and end of the range, respectively
		while (true) {
			String str1 = gui.get("Please enter the beginning of the range you'd like to test. (Positive and natural number).\n"
								+ "For testing a single number, enter that number now and then once again in the next prompt."); // ask for and store the user's response
			if (str1 == "") { // user didn't enter anything
				gui.give("Please enter something! (The field was blank.)"); // notify them
				continue; // move to the beginning of the loop
			}
			else if (str1 == null) { // user hit cancel
				goodbye();
			}
			try {
				numberPair[0] = Integer.parseInt(str1); // try to convert the user's response into an int
			} catch (NumberFormatException nfe) { // if the user's response was not a number
				gui.giveThrowable(nfe);
				gui.give("Please enter a valid number! (What you entered is not recognized as an integer...)"); // notify them
				continue; // move to the beginning of the loop
			}
			if (numberPair[0] <= 0) { // the number entered was negative or zero
				gui.give("Please enter a valid number! (What you entered is nonpositive.)");
				continue; // move to the beginning of the loop
			}
			break;
		}
		while (true) {
			String str2 = gui.get("Please enter the end of the range you'd like to test. (Positive and natural number).\n"
								+ "For testing a single number, enter the same number from the first prompt."); // ask for and store the user's response
			if (str2 == "") { // user didn't enter anything
				gui.give("Please enter something! (The field was blank.)"); // notify them
				continue; // move to the beginning of the loop
			}
			else if (str2 == null) { // user hit cancel
				goodbye();
			}
			try {
				numberPair[1] = Integer.parseInt(str2); // try to convert the user's response into an int
			} catch (NumberFormatException nfe) { // if the user's response was not a number
				gui.giveThrowable(nfe);
				gui.give("Please enter a valid number! (What you entered is not recognized as an integer...)"); // notify them
				continue; // move to the beginning of the loop
			}
			if (numberPair[1] <= 0) { // the number entered was negative or zero
				gui.give("Please enter a valid number! (What you entered was nonpositive.)");
				continue; // move to the beginning of the loop
			}
			if (numberPair[1] < numberPair[0]) { // if the end of the range is before the beginning of the range
				gui.give("Please have the end of the range be either equal to or greater than the beginning of the range! (Second number entered was less than the first number entered.)"); // notify the user
				continue; // move to the beginning of the loop
			}
			break;
		}
		return numberPair;
	}
	
	private static String determineFlowGates(List<ArrayList<Integer>> rangedStartingNumberAndFlowPoints) { // finds if any of the flow points qualify to be flow gates (must have a frequency of 10% or higher) and returns that information
		JFrame flowGatesProgress = new JFrame("Flow Gates Calculations"); // for displaying the progress of calculating the flow gates within the range tested [in practice, the progress is quick, so the displaying is imperceptible (at least with the numbers tested)
		flowGatesProgress.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JProgressBar FGPStatus = new JProgressBar(0, rangedStartingNumberAndFlowPoints.size()); // the actual progress bar with bounds zero and last element of "flowPoints"
		int FGPStatusPoint = 0;
		FGPStatus.setValue(FGPStatusPoint);
		FGPStatus.setStringPainted(true);
		flowGatesProgress.getContentPane().add(FGPStatus);
		FontMetrics FGPFontMetrics = flowGatesProgress.getFontMetrics(gui.getFont()); // for sizing the JFrame
		flowGatesProgress.setPreferredSize(new Dimension(FGPFontMetrics.stringWidth(flowGatesProgress.getTitle()) + 248, 100)); // 248 is the required additional constant (for the total width of the buttons in the upper right corner?)
		flowGatesProgress.pack();
		gui.center(flowGatesProgress);
		flowGatesProgress.setVisible(true);
		int thresholdPercentage = 10; // the threshold after which a flow point is considered a flow gate
		String flowGates = "Here are the " + thresholdPercentage + "% threshold flow gates:\n";
		double thresholdDecimal = thresholdPercentage / 100.0;
		int frequencyCounter = 1; // for seeing if a flow point qualifies to be a flow gate
		int totalNumberOfFlowPoints = rangedStartingNumberAndFlowPoints.size(); // for seeing if a flow point qualifies to be a flow gate
		for (int counter = 1; counter <= rangedStartingNumberAndFlowPoints.size(); counter++) { // calculating flow gates
			FGPStatusPoint = counter; // update the progress
			FGPStatus.setValue(FGPStatusPoint);
			if (counter != totalNumberOfFlowPoints) { // not at the end of "flowPoints"
				if (rangedStartingNumberAndFlowPoints.get(counter).get(1) == rangedStartingNumberAndFlowPoints.get(counter - 1).get(1)) { // the current flow point and the previous flow point are the same
					frequencyCounter++; // increase the known frequency of the flow point
				} else { // the current flow point and the previous flow point are different
					double frequency = (double) frequencyCounter / (double) totalNumberOfFlowPoints; // calculate and store the frequency of the previous flow point
					frequencyCounter = 1; // "zero" the counter
					if (frequency >= thresholdDecimal) { // the flow point is considered to be a flow gate
						flowGates += rangedStartingNumberAndFlowPoints.get(counter - 1).get(1) + " (" + (frequency * 100) + "%) "; // retrieve the previous flow point (now considered a flow gate) and record it along with its frequency in "flowGates"
					}
				}
			} else { // at the end of "flowPoints"
				double frequency = (double) frequencyCounter / (double) totalNumberOfFlowPoints; // calculate and store the frequency of the last flow point in "flowPoints"
				if (frequency >= thresholdDecimal) { // the flow point is considered to be a flow gate
					flowGates += rangedStartingNumberAndFlowPoints.get(counter - 1).get(1) + " (" + (frequency * 100) + "%) "; // retrieve the last flow point (now considered a flow gate) and record it along with its frequency in "flowGates"
				}
			}
		}
		flowGates += "\n\n"; // add two empty lines for cleanliness
		flowGatesProgress.dispose();
		return flowGates;
	}
	
	private static String[] gatherLeastMiddleAndMostInformation() {
		String[] leastMiddleAndMostInfo = new String[3];
		ArrayList<Integer> beginningOfSortedByStepsRange = rangedStartingNumberAndSteps.get(0);
		String startingNumbersWithShortestPath = ""; // will hold the starting number(s) which have the shortest path
		ArrayList<Integer> middleOfSortedByStepsRange = new ArrayList<Integer>();
		String startingNumbersWithMedianPath = ""; // will hold the starting number(s) which have the median path
		boolean andAHalf = false, // whether the median is a number.5 or just a number
				correspondingStartNumber = true; // whether there is a corresponding start number with the median steps
		if (rangedStartingNumberAndSteps.size() % 2 == 0) { // even number of elements
			int firstMiddleNumber = rangedStartingNumberAndSteps.get((rangedStartingNumberAndSteps.size() / 2) - 1).get(1);
			int secondMiddleNumber = rangedStartingNumberAndSteps.get(rangedStartingNumberAndSteps.size() / 2).get(1);
			double median = (firstMiddleNumber + secondMiddleNumber) / 2.0;
			int medianInt = (int) Math.floor(median);
			if (median != medianInt) { // there's a decimal, specifically .5
				andAHalf = true;
			}
			middleOfSortedByStepsRange.add(-1); // no corresponding number
			middleOfSortedByStepsRange.add(medianInt);
			correspondingStartNumber = false;
		} else { // odd number of elements
			middleOfSortedByStepsRange = rangedStartingNumberAndSteps.get(rangedStartingNumberAndSteps.size() / 2);
		}
		if (correspondingStartNumber == false) {
			startingNumbersWithMedianPath += "...no corresponding starting number, "; // last two characters will get shaved off later
		}
		ArrayList<Integer> endOfSortedByStepsRange = rangedStartingNumberAndSteps.get(rangedStartingNumberAndSteps.size() - 1);
		String startingNumbersWithLongestPath = ""; // will hold all of the starting numbers that have the longest path
		for (ArrayList<Integer> numAndStepPair : rangedStartingNumberAndSteps) { // finding all of the starting numbers with the shortest path or median path or longest path
			if (numAndStepPair.get(1) == beginningOfSortedByStepsRange.get(1)) { // at a pair with shortest path
				startingNumbersWithShortestPath += numAndStepPair.get(0) + ", "; // get and record the starting number, comma and space for clarity and looks
			} else if (numAndStepPair.get(1) == middleOfSortedByStepsRange.get(1) && correspondingStartNumber == true) { // at a pair with median path and there is a corresponding start number
				startingNumbersWithMedianPath += numAndStepPair.get(0) + ", "; // get and record the starting number, comma and space for clarity and looks
			} else if (numAndStepPair.get(1) == endOfSortedByStepsRange.get(1)) { // at a pair with longest path
				startingNumbersWithLongestPath += numAndStepPair.get(0) + ", "; // get and record the starting number, comma and space for clarity and looks
			}
		}
		if (startingNumbersWithShortestPath.length() > 2) {
			startingNumbersWithShortestPath = startingNumbersWithShortestPath.substring(0, startingNumbersWithShortestPath.length() - 2); // getting rid of the ", " at the end
			startingNumbersWithShortestPath += "."; // putting on a period instead
		}
		if (startingNumbersWithMedianPath.length() > 2) {
			startingNumbersWithMedianPath = startingNumbersWithMedianPath.substring(0, startingNumbersWithMedianPath.length() - 2); // shaving off the ", " at the end
			startingNumbersWithMedianPath += "."; // period at the end now
		}
		if (startingNumbersWithLongestPath.length() > 2) {
			startingNumbersWithLongestPath = startingNumbersWithLongestPath.substring(0, startingNumbersWithLongestPath.length() - 2); // shaving off the ", " at the end
			startingNumbersWithLongestPath += "."; // a period is at the end now
		}
		String leastNumberOfSteps = "The shortest path is " + beginningOfSortedByStepsRange.get(1) + " step(s), with starting number(s) " + startingNumbersWithShortestPath + "\n"; // retrieve shortest path details from "notedStartingNumberAndSteps" and store in a string
		String middleNumberOfSteps = "";
		if (andAHalf) { // the median value is a number with .5
			middleNumberOfSteps = "The median path is " + middleOfSortedByStepsRange.get(1) + ".5 step(s), with starting number(s) " + startingNumbersWithMedianPath + "\n";
		} else { // the median value is an integer
			middleNumberOfSteps = "The median path is " + middleOfSortedByStepsRange.get(1) + " step(s), with starting number(s) " + startingNumbersWithMedianPath + "\n"; // retrieve median path details and store in a string
		}
		String mostNumberOfSteps = "The longest path is " + endOfSortedByStepsRange.get(1) + " step(s), with starting number(s) " + startingNumbersWithLongestPath + "\n\n"; // retrieve longest path details and store in a string
		leastMiddleAndMostInfo[0] = leastNumberOfSteps;
		leastMiddleAndMostInfo[1] = middleNumberOfSteps;
		leastMiddleAndMostInfo[2] = mostNumberOfSteps;
		return leastMiddleAndMostInfo;
	}
	
	private static String gatherMeanInformation() {
		String info = "";
		double pathLengthSummation = 0.000;
		double mean = 0.000;
		for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter ++) { // adding up all of the path lengths
			pathLengthSummation += rangedStartingNumberAndSteps.get(counter).get(1);
		}
		mean = pathLengthSummation / (rangedStartingNumberAndSteps.size());
		NumberFormat toTheThousandths = new DecimalFormat("#0.000"); // the mean format will go out to the thousandths place
		mean = Double.parseDouble(toTheThousandths.format(mean)); // apply the format, convert the answer back to a double
		info += "The average path is " + mean + ".\n";
		return info;
	}
	
	private static String gatherModalInformation() {
		String info = "";
		int previousMaxFrequency = 1, 	  // .
			frequency = 1,		      // .
			maxFrequency = 1;	   	  // ... for determining the modal value
		for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter ++) { // finding the modal value
			if (counter == rangedStartingNumberAndSteps.size() - 1) { // last step, do some final frequency checking
				if (frequency >= previousMaxFrequency) { // the frequency encountered is greater than the last maximum frequency encountered
					maxFrequency = frequency;
					previousMaxFrequency = frequency; // "remembering" this most recent frequency
				}
				break;
			}
			if (rangedStartingNumberAndSteps.get(counter).get(1) == rangedStartingNumberAndSteps.get(counter + 1).get(1)) { // the path lengths at the current step in the for-loop and the next step are the same
				frequency ++;
			} else { // the path length at the next step is different from the path length at the current step
				if (frequency >= previousMaxFrequency) { // the frequency encountered is greater than the last maximum frequency encountered
					maxFrequency = frequency;
					previousMaxFrequency = frequency; // "remembering" this most recent frequency
				}
				frequency = 1; // "zeroing" (re-initializing)
			}
		}
		if (maxFrequency > 1) { // if there is a mode
			info += "The maximum path re-occurence rate is " + maxFrequency + ", for the following path length(s) and starting numbers: ";
			frequency = 1; // "zeroing" (re-initializing)
			for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter ++) { // fetching the modal info
				if (counter == rangedStartingNumberAndSteps.size() - 1) { // last step, do some final frequency checking
					if (frequency >= previousMaxFrequency) { // the frequency encountered is greater than the last maximum frequency encountered
						maxFrequency = frequency;
						previousMaxFrequency = frequency; // "remembering" this most recent frequency
					}
					break;
				}
				if (counter < rangedStartingNumberAndSteps.size() && rangedStartingNumberAndSteps.get(counter).get(1) == rangedStartingNumberAndSteps.get(counter + 1).get(1)) { // if the path length at the current step in the for-loop is the same as the path length of the next step
					frequency ++;
				} else {
					frequency = 1; // "zeroing" (re-initializing)
				}
				if (frequency == maxFrequency) { // the frequency is a modal value
					info += "\n\t" + rangedStartingNumberAndSteps.get(counter).get(1) + " ("; // record the path length (should be the same for all of the entries making up the modal value) and open parenthesis for a space for the starting numbers
					int startIndexOfModalNumbers = counter - (maxFrequency - 2); // adjustment (when in this if-statement, "counter" is at the second-to-last member of the 'frequent group' (see previous if-statement logic) so -2)
					int innerCounter = 0;
					for (innerCounter = startIndexOfModalNumbers; innerCounter < startIndexOfModalNumbers + maxFrequency; innerCounter ++) { // go back to the beginning of the modal stretch (the beginning of the entries making up the modal value) and get the starting numbers for each one
						info += rangedStartingNumberAndSteps.get(innerCounter).get(0); // append the starting number
						if (innerCounter != (startIndexOfModalNumbers + maxFrequency) - 1) { // if not at the end of the entries making up the modal value...
							info += ", "; //...append a comma
						}
					}
					info += ")."; // close parenthesis and period to contain the starting numbers and indicate the end. New line for clarity and looks.
					counter = innerCounter - 1; // the outer loop will pick up where the inner loop stepped down (the subtraction cancels out the incrementing of "counter" from the for-loop update statement)
				}
			}
			info += "\n";
		}
		else { // there is no mode
			info += "No mode.\n";
		}
		return info;
	}
	
	private static void goodbye() {
		gui.give("Thank you for using this program!\n\n"
			   + "Kind wishes!"); // appreciation and good wishes
		System.exit(0); // ensure the program ends
	}
	
	private static String hotpo(int num) { // Half Or Triple Plus One
		int startingNum = num; // keep track of the beginning number
		String sequence = num + " » "; // the path from num to 1
		int steps = 0; // steps in the path
		boolean bigRiverReached = false; // the river of powers of 2 has not been met
		while (num != 1) {
			if (num % 2 == 0) { // num is even
				if ((Math.log(num) / Math.log(2)) == (int) (Math.log(num) / Math.log(2)) && !bigRiverReached) { // num is a power of 2 (using change-of-base) and the river of powers of 2 has not been met
					bigRiverReached = true; // now that river has been met
					ArrayList<Integer> startingNumAndFlowPoint = new ArrayList<Integer>();
					startingNumAndFlowPoint.add(startingNum);
					startingNumAndFlowPoint.add(num);
					startingNumberAndFlowPoints.add(startingNumAndFlowPoint); // record num as a flow point in "flowPoints"
				}
				num /= 2; // half and store
			}
			else { // num is odd
				num *= 3; // triple and store
				num++; // plus one and store
			}
			steps ++; // a step has been taken
			sequence += num + " » "; // record num and step representation in "sequence"
		}
		sequence = sequence.substring(0, sequence.length() - 3); // getting rid of the " » " at the end
		sequence += "\n" + "Steps Taken: " + steps; // record the total number of steps in "sequence"
		if (startingNum > notedSequence.size()) { // the beginning number doesn't have a spot in "notedSequence" (can occur when the user tests a single number after testing a range, with the single number outside of that range
			notedSequence.ensureCapacity(startingNum); // make room in "notedSequence"
			for (int counter = notedSequence.size(); counter < startingNum - 1; counter++) { // fill that room
				notedSequence.add(notedSequenceFiller);
			}
			notedStartingNumberAndSteps.ensureCapacity(startingNum); // make room in "notedStartingNumberAndSteps"
			for (int counter = notedStartingNumberAndSteps.size(); counter < startingNum - 1; counter++) { // fill that room
				notedStartingNumberAndSteps.add(new ArrayList<Integer>(Arrays.asList(counter, notedStartingNumberAndStepsStepFiller)));
			}
		}
		if (startingNum < notedSequence.size() && notedSequence.get(startingNum).equals(notedSequenceFiller)) { // the beginning number has a spot in "notedSequence", and that spot is filled with "notedSequenceFiller"
			notedSequence.set(startingNum, sequence); // set that spot as containing the sequence instead
		} else { // the starting number is outside of "notedSequence"
			notedSequence.add(sequence); // records the sequence of the beginning number in "notedSequence"
		}
		if (startingNum < notedStartingNumberAndSteps.size() && notedStartingNumberAndSteps.get(startingNum).get(1).equals(notedStartingNumberAndStepsStepFiller)) { // the beginning number has a spot in "notedStartingNumberAndSteps" and that spot's "steps" dimension is filled with "notedStartingNumberAndStepsStepFiller"
			notedStartingNumberAndSteps.get(startingNum).set(1, steps); // set the "steps" dimension to store the steps recorded before
		} else { // the starting number is outside of "notedStartingNumberAndSteps"
			notedStartingNumberAndSteps.add(new ArrayList<Integer>(Arrays.asList(startingNum, steps)));
		}
		return sequence;
	}
	
	public static String testRange(int[] range, JFrame display, double functionCoefficient, double xCoefficient, double processorSpeed) {
		String sequences = "";
		long startTime = 0, // ...
			 endTime = 0, // ...will be used to see how long the calculations or retrieval for the last number took
			 duration = 0; // will hold how long the calculations or retrieval for the last number took
		int tenPercentChunkOfRange = (int) (((range[1] - range[0]) + 1) * 0.1); // ten percent of the range tested
		if (tenPercentChunkOfRange == 0) { // ten percent of the range gives a number less than one, which rounds down to 0
			tenPercentChunkOfRange = 1; // just go to 1 (whatever percent of the range that is)
		}
		Long[] tenPercentMostRecentTimes = new Long[tenPercentChunkOfRange]; // will hold the times of the calculations or retrievals in 10% of the total range 'chunks'. The estimated time remaining will be determined based off of these chunks.
		long totalTime = 0; // the sum of the most recent 10% of calculation or retrieval times
		long recentRuntimesETRNumber = 0; // will hold the estimated time remaining (ETR) based off of the recent runtimes multiplied by the remaining amount of numbers to be tested
		String recentRuntimesETRString = ""; // will show the user how much time is estimated to be left (recent runtimes and remaining tasks method)
		String modeledETRString = ""; // will show the user how much time is estimated to be left (model method)
		int numberOfIntegersToBeTested = range[1] - range[0] + 1; // calculate for the range being tested (the "+1" is to get the actual number of integers to be tested (e.g., testing 1 to 10 and just subtracting the starting number from the ending number gives 9. But you're testing 10 integers, not 9, so +1))
		double modeledRuntimeInSeconds = (functionCoefficient) * (Math.pow(Math.E, xCoefficient * numberOfIntegersToBeTested)); // calculate for range being tested next
		long currentTimeInSeconds = System.currentTimeMillis() / 1000; // works with the modeled estimated time remaining
		long modeledEndTimeInSeconds = currentTimeInSeconds + (long) modeledRuntimeInSeconds; // works with the modeled estimated time remaining
		int modeledETRNumber = 0; // will hold the estimated time based off of the function
		int tenPercentChunkCounter = 1; // how much of the ten percent chunk is filled
		JProgressBar PTRProgressBarForRange = (JProgressBar) display.getContentPane().getComponent(0);
		PTRProgressBarForRange.setMinimum(range[0]);
		PTRProgressBarForRange.setMaximum(range[1]);
		int progress = 0;
		JLabel recentRuntimesEstimatedTimeRemaining = (JLabel) display.getContentPane().getComponent(1);
		JLabel modeledEstimatedTimeRemaining = (JLabel) display.getContentPane().getComponent(2);
		for (int counter = range[0]; counter <= range[1]; counter ++) { // the determining or fetching path(s) loop
			progress = counter; // progress is updated
			PTRProgressBarForRange.setValue(progress);
			startTime = System.nanoTime();
			if (counter < notedSequence.size() && !notedSequence.get(counter).equals(notedSequenceFiller)) { // the number has been calculated before
				sequences += "↓Starting Number" + "\n" + notedSequence.get(counter) + "\n\n";
			} else { // the number has not be calculated before
				sequences += "↓Starting Number" + "\n" + hotpo(counter) + "\n\n";
			}
			endTime = System.nanoTime();
			duration = endTime - startTime;
			tenPercentMostRecentTimes[counter % tenPercentChunkOfRange] = duration; // modulo to prevent out-of-bounds (stays within the ten percent chunk)
			if (tenPercentChunkCounter % tenPercentChunkOfRange == 0) { // the ten percent chunk is full
				for (int mostRecentTimesCounter = 0; mostRecentTimesCounter < tenPercentMostRecentTimes.length; mostRecentTimesCounter ++) { // adding up all of the most recent times
					totalTime += tenPercentMostRecentTimes[mostRecentTimesCounter];
				}
				recentRuntimesETRNumber = (totalTime / (long) tenPercentChunkOfRange) * (long) (range[1] - counter);
				modeledETRNumber = (int) (modeledEndTimeInSeconds - (System.currentTimeMillis() / 1000));
				recentRuntimesETRString = " Estimated time remaining (using recent runtimes and remaining tasks): " + String.format("%d min(s) %d sec(s)", TimeUnit.NANOSECONDS.toMinutes(recentRuntimesETRNumber), TimeUnit.NANOSECONDS.toSeconds(recentRuntimesETRNumber) % 60);
				modeledETRString = " Estimated time remaining (using modeled growth): " + String.format("%d min(s), %d sec(s)", TimeUnit.SECONDS.toMinutes(modeledETRNumber), modeledETRNumber % 60);
				recentRuntimesEstimatedTimeRemaining.setText(recentRuntimesETRString);
				if (processorSpeed == -2) { // the user did not enter their computer's processor speed some distance up
					modeledEstimatedTimeRemaining.setText(" (Experimental Estimated Time Remaining Not In Use)");
				} else { // the user did enter their computer's processor speed some distance up
					modeledEstimatedTimeRemaining.setText(modeledETRString);
				}
			}
			tenPercentChunkCounter ++;
		}
		return sequences;
	}
	
	public static class LineGraph extends Application { // for constructing and displaying the line graph of path length vs. starting number
		
		static int ceiling; // stores the end of the range tested
		
		static int floor; // stores the beginning of the range tested
		
		static LineChart <Number, Number> stepsVsStartingNumber; // the line graph
		
		static NumberAxis xAxis, yAxis;  // horizontal axis (starting number) and vertical axis (path length)
		
		static Scene scene; // the scene will take up half of the vertical part of the screen and all of the horizontal part
		
		static Stage stage;
		
		static XYChart.Series<Number, Number> coordinatePoints; // to store the x-y points (starting number - path length points)
		
		public static void initialize(ArrayList<Integer> range) { // setting up the graph and using the first range
			floor = range.get(0);
			ceiling = range.get(1);
			int numberOfIntegersTested = ceiling - floor + 1; // the "+1" ensures "numberOfIntegersTested" will be accurate (has to do with including either the beginning or the end of the range)
			int tickSpacing = (int) Math.round(0.05 * (numberOfIntegersTested)); // at 5%-of-the-range increments
			if (tickSpacing == 0) { // due to rounding
				tickSpacing = 1;
			}
			xAxis = new NumberAxis("Starting Number", (double) ((floor == 1) ? (floor - 1) : floor), (double) ceiling, tickSpacing);
			yAxis = new NumberAxis();
			yAxis.setLabel("Path Length");
			stepsVsStartingNumber = new LineChart<Number, Number>(xAxis, yAxis);
			stepsVsStartingNumber.setTitle("Steps to Reach ONE for Numbers " + floor + " to " + ceiling);
			coordinatePoints = new XYChart.Series<Number, Number>();
			coordinatePoints.setName("Data");
			ArrayListFirstIntComparison arrangingStartingNumbersInIncreasingOrder = new ArrayListFirstIntComparison();
			notedStartingNumberAndSteps.sort(arrangingStartingNumbersInIncreasingOrder); // make sure the starting numbers are in order
			int startOfRangeIndex = 0;
			while (notedStartingNumberAndSteps.get(startOfRangeIndex).get(0) != floor) { // getting to the start of the recently tested range in "notedStartingNumberAndSteps"
				startOfRangeIndex ++;
			}
			int endOfRangeIndex = startOfRangeIndex + (ceiling - floor + 1); // "+1" because ".sublist" "arg2" is exclusive
			rangedStartingNumberAndSteps = notedStartingNumberAndSteps.subList(startOfRangeIndex, endOfRangeIndex); // done to avoid issues with "notedStartingNumberAndStepsStepFiller"
			Gathering:
				for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter++) { // recording the x-y points
					coordinatePoints.getData().add(new XYChart.Data<Number, Number>(rangedStartingNumberAndSteps.get(counter).get(0), rangedStartingNumberAndSteps.get(counter).get(1)));
				}
			stepsVsStartingNumber.getData().add(coordinatePoints); // putting the points on the line graph
			scene = new Scene(new Group(), gui.getScreensize().getWidth(), gui.getScreensize().getHeight() / 2);
			stage.setScene(scene); // put the scene on the stage
			stage.setX(0); // place the stage's upper left corner all the way to the left
			stage.setY(gui.getScreensize().height / 3); // place the stage's upper left corner a third of the way down the screen (under "teaRex")
		}
		
		public void start (Stage paramSteph) {
			stage = new Stage();
			Platform.setImplicitExit(true);
			stage.setTitle("Graph(s)");
			int comboBoxLength = testedRanges.size();
			ComboBox<String> rangeOptions = new ComboBox<String>(); // for the user to select which range to view
			rangeOptions.setPromptText("Range(s)");
			for (int counter = 0; counter < comboBoxLength; counter ++) { // naming the options
				rangeOptions.getItems().add(Integer.toString(testedRanges.get(counter).get(0)) + " to " + Integer.toString(testedRanges.get(counter).get(1)));
			}
			int selectedIndex = 0; // initial selection
			ChangeListener<Number> newSelectionAndUpdate = new ChangeListener<Number>() { // activates when the selection in "rangeOptions" changes and updates the graph with the new range
				public void changed (ObservableValue <? extends Number> selection, Number oldIndex, Number newIndex) {
					update(testedRanges.get((int) newIndex));
				}
			};
			rangeOptions.getSelectionModel().selectedIndexProperty().addListener(newSelectionAndUpdate);
			initialize(testedRanges.get(selectedIndex));
			GridPane sceneStructure = new GridPane();
			sceneStructure.add(rangeOptions, 0, 0);
			sceneStructure.add(stepsVsStartingNumber, 0, 1);
			sceneStructure.setPrefSize(scene.getWidth(), scene.getHeight());
			sceneStructure.setVgap(10);
			GridPane.setHgrow(stepsVsStartingNumber, Priority.ALWAYS);
			GridPane.setVgrow(stepsVsStartingNumber, Priority.ALWAYS);
			Group sceneRoot = (Group) scene.getRoot();
			sceneRoot.getChildren().add(sceneStructure);
			stage.show();
		}

		public static void main(String[] args) {
			launch();
		}
		
		public static void update (ArrayList<Integer> range) { // to change the displayed graph to the graph of the selected range
			stage.setOpacity(0.75); // disappear a bit while the update is happening
			floor = range.get(0);
			ceiling = range.get(1);
			stepsVsStartingNumber.setTitle("Steps to Reach ONE for Numbers " + floor + " to " + ceiling);
			stepsVsStartingNumber.setData(FXCollections.observableArrayList());
			xAxis.setLowerBound((double) ((floor == 1) ? (floor - 1) : floor));
			xAxis.setUpperBound((double) ceiling);
			int numberOfIntegersTested = ceiling - floor + 1; // the "+1" ensures "numberOfIntegersTested" will be accurate (has to do with including either the beginning or the end of the range)
			int tickSpacing = (int) Math.round(0.05 * (numberOfIntegersTested)); // at 5%-of-the-range increments
			if (tickSpacing == 0) { // due to rounding
				tickSpacing = 1;
			}
			xAxis.setTickUnit(tickSpacing);
			coordinatePoints = new XYChart.Series<Number, Number>();
			coordinatePoints.setName("Data");
			ArrayListFirstIntComparison arrangingStartingNumbersInIncreasingOrder = new ArrayListFirstIntComparison();
			notedStartingNumberAndSteps.sort(arrangingStartingNumbersInIncreasingOrder); // make sure the starting numbers are in order
			int startOfRangeIndex = 0;
			while (notedStartingNumberAndSteps.get(startOfRangeIndex).get(0) != floor) { // getting to the start of the recently tested range in "notedStartingNumberAndSteps"
				startOfRangeIndex ++;
			}
			int endOfRangeIndex = startOfRangeIndex + (ceiling - floor + 1); // "+1" because ".sublist" "arg2" is exclusive
			rangedStartingNumberAndSteps = notedStartingNumberAndSteps.subList(startOfRangeIndex, endOfRangeIndex); // done to avoid issues with "notedStartingNumberAndStepsStepFiller"
			Gathering:
				for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter++) {
					coordinatePoints.getData().add(new XYChart.Data<Number, Number>(rangedStartingNumberAndSteps.get(counter).get(0), rangedStartingNumberAndSteps.get(counter).get(1)));
				}
			stepsVsStartingNumber.getData().add(coordinatePoints);
			stage.setOpacity(1); // return to full visibility
		}
	}
}