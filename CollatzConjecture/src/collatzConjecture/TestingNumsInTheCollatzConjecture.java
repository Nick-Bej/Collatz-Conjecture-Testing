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

class SecondDimensionFirstIntComparison implements Comparator<ArrayList<Integer>> { // sort the first index of the second dimension of "notedStartingNumberAndSteps" (startingNumber)
	public int compare (ArrayList<Integer> leftOfPair, ArrayList<Integer> rightOfPair) {
		return leftOfPair.get(0) - rightOfPair.get(0);
	}
}

class IntComparison implements Comparator<Integer> { // used to order flowPoints in increasing sequence
	public int compare (Integer leftOfPair, Integer rightOfPair) {
		return leftOfPair - rightOfPair;
	}
}

class SecondDimensionSecondIntComparison implements Comparator<ArrayList<Integer>> { // used to order the "steps" of "notedStartingNumberAndSteps"
	public int compare (ArrayList<Integer> leftOfPair, ArrayList<Integer> rightOfPair) {
		return leftOfPair.get(1) - rightOfPair.get(1);
	}
}

class TestingNumsInTheCollatzConjecture {
	
	static ArrayList<ArrayList<Integer>> notedStartingNumberAndSteps = new ArrayList<ArrayList<Integer>>(); // for finding the shortest, median, and longest paths, and for constructing the line graph of path length vs. starting number
	
	static ArrayList<ArrayList<Integer>> testedRanges = new ArrayList<ArrayList<Integer>>(); // will hold all of the ranges (beginning and end numbers) that have been run through
	
	static ArrayList<Integer> flowPoints = new ArrayList<Integer>(); // holds the powers of 2 that each calculated path connects to (and, in other words, where the path joins up with the "big river" of powers of 2 (it's a big river because once a path goes there it simply flows down to 1))
	
	static ArrayList<String> noted = new ArrayList<String>(); // for memoization

	static boolean again = false; // for determining whether the StepsLineGraph is being run more than once
	
	static GUI gui = new GUI();
	
	static int notedStartingNumberAndStepsStepFiller = 0;
	
	static List<ArrayList<Integer>> rangedStartingNumberAndSteps; // will hold the recently-tested range from "notedStartingNumberAndSteps"
	
	static String notedFiller = "(empty)";
	
	static String[] floorAndCeilingValues = new String[2]; // will hold the beginning and end of the range being tested
	
	public static void main (String[] args) {
		gui.give("Welcome to Collatz Conjecture Analytics!\n"														 // Introductions...
			   + "Here you can test individual/multiple numbers and observe trends within the Collatz Conjecture."); // .
		class LinksToResourcesRunnable extends Thread implements Runnable {
			
			LinksToResourcesRunnable () {
				return;
			}
			
			LinksToResourcesRunnable ltrr;
			
			boolean interrupted = false;
			
			JFrame studyUp = new JFrame(); // will contain buttons linking to articles about the Collatz Conjecture
			
			public boolean isRunning() {
				return !interrupted;
			}
			
			public void interrupt() {
				interrupted = true;
				studyUp.dispose();
			}
			
			public void run() {
				Thread ltrrThread = new Thread(ltrr);
				GridLayout grin = new GridLayout(1, 2);
				studyUp.setLayout(grin);
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
					gui.give("Error!\n\n" + urise);
				}
				JButton wolframMathWorldLink = new JButton("What Wolfram MathWorld Has To Say"); // button linking to the Wolfram MathWorld article
				try {
					URI CCOnWolframMathWorld = new URI("http://mathworld.wolfram.com/CollatzProblem.html"); // making the URI
					wolframMathWorldLink.addActionListener(gui.linkTo(CCOnWolframMathWorld)); // making it so upon clicking the URI is opened
					studyUp.add(wolframMathWorldLink); // adding the button to the JFrame
				} catch (URISyntaxException urise) {
					gui.give("Error!\n\n" + urise);
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
		LinksToResourcesRunnable information = new LinksToResourcesRunnable();
		int userAnswer = gui.giveYesNo("If you're unfamiliar with the Collatz Conjecture, that's perfectly fine!\n\n"
									 + "Click \"Yes\" to read up on it (I'll wait; I'm very patient);\n"
									 + "Click \"No\" to skip it;\n"
									 + "Click \"Quit\" to end the whole program."); // educational resources about the Collatz Conjecture can be made available
		if (userAnswer == 0) { // answer was "Yes"
			information.start();
			try {
				information.join();
			} catch (InterruptedException ie) {
				gui.give("Error!\n"
					   + ie);
			}
		} else if (userAnswer == 2) { // answer was "Quit"
			System.exit(0);
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
				System.exit(0);
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
		int numberOfIntegersToBeTested = numberPair[1] - numberPair[0] + 1; // the "+1" is to get the actual number of integers to be tested (e.g., testing 1 to 10 and just subtracting the starting number from the ending number gives 9. But you're testing 10 integers, not 9, so +1)
		double modeledRuntimeInSeconds = (functionCoefficient) * (Math.pow(Math.E, xCoefficient * numberOfIntegersToBeTested));
		JFrame jeff = new JFrame("Determining or Fetching Path(s)"); // will show the status of the progression through the range of numbers
		jeff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridLayout greg = new GridLayout(3, 1);
		jeff.setLayout(greg);
		JProgressBar philip = new JProgressBar(); // the actual progress bar
		int numberProgress = numberPair[0];
		philip.setValue(numberProgress);
		philip.setStringPainted(true);
		jeff.getContentPane().add(philip);
		JLabel JLabelRecentRuntimesEstimatedTimeRemaining = new JLabel();
		jeff.getContentPane().add(JLabelRecentRuntimesEstimatedTimeRemaining);
		JLabel JLabelModeledEstimatedTimeRemaining = new JLabel();
		jeff.getContentPane().add(JLabelModeledEstimatedTimeRemaining);
		FontMetrics fahima = jeff.getFontMetrics(gui.getFont()); // used in determining the size of the JFrame
		jeff.setPreferredSize(new Dimension(fahima.stringWidth(jeff.getTitle()) + 248, 100)); // 248 is the constant that needs to be added (is the total width of the buttons in the upper right corner?)
		jeff.pack();
		int jeffsCenterXValue = jeff.getWidth() / 2; // calculating and storing the center point of the JFrame (x-coordinate)
		int jeffsCenterYValue = jeff.getHeight() / 2; // calculating and storing the center point of the JFrame (y-coordinate)
		jeff.setLocation((gui.getScreensize().width / 2) - (jeffsCenterXValue), (gui.getScreensize().height / 2) - (jeffsCenterYValue)); // in the middle of the screen
		String allSequencesAndSuch = ""; // will hold information gathered through the running of the program
		int ultramarineCounter = 0;
		Ultramarine:
			while (true) {
				ultramarineCounter ++;
				if (numberPair[0] != numberPair[1]) { // testing a range
					allSequencesAndSuch += "---------- FROM " + numberPair[0] + " TO " + numberPair[1] + " ----------\n";
				} else { // testing a single number
					allSequencesAndSuch += "---------- FOR THE NUMBER " + numberPair[1] + " ----------\n";
				}
				jeff.setVisible(true); // time to show the JFrame and JProgressBar (calculations ahead)
				if (numberPair[0] < numberPair[1]) { // a range is being tested
					long startTime = 0, // ...
						 endTime = 0, // ...will be used to see how long the calculations or retrieval for the last number took
						 duration = 0; // how long the calculations or retrieval for the last number took
					int tenPercentChunk = (int) (((numberPair[1] - numberPair[0]) + 1) * 0.1); // ten percent of the range tested
					if (tenPercentChunk == 0) { // ten percent of the range gives a number less than one, which rounds down to 0
						tenPercentChunk = 1; // just go to 1 (whatever percent of the range that is)
					}
					Long[] tenPercentMostRecentTimes = new Long[tenPercentChunk]; // will hold the times of the calculations or retrievals in 10% of the total range 'chunks'. The estimated time remaining will be determined based off of these chunks.
					long totalTime = 0; // the sum of the most recent 10% of calculation or retrieval times
					long recentRuntimesEstimatedTimeRemaining = 0; // will hold the estimated time remaining based off of the recent runtimes multiplied by the remaining amount of numbers to be tested
					String displayedRecentRuntimesEstimatedTimeRemaining = ""; // will show the user how much time is estimated to be left (recent runtimes and remaining tasks method)
					String displayedModeledEstimatedTimeRemaining = ""; // will show the user how much time is estimated to be left (model method)
					numberOfIntegersToBeTested = numberPair[1] - numberPair[0] + 1; // calculate for the range being tested next
					modeledRuntimeInSeconds = (functionCoefficient) * (Math.pow(Math.E, xCoefficient * numberOfIntegersToBeTested)); // calculate for range being tested next
					long currentTimeInSeconds = System.currentTimeMillis() / 1000; // works with the modeled estimated time remaining
					long estimatedEndTimeInSeconds = currentTimeInSeconds + (long) modeledRuntimeInSeconds; // works with the modeled estimated time remaining
					int modeledEstimatedTimeRemaining = 0; // will hold the estimated time based off of the function
					int tenPercentChunkCounter = 1; // how much of the ten percent chunk is filled
					JProgressBar boundedPhilip = (JProgressBar) jeff.getContentPane().getComponent(0);
					boundedPhilip.setMinimum(numberPair[0]);
					boundedPhilip.setMaximum(numberPair[1]);
					for (int counter = numberPair[0]; counter <= numberPair[1]; counter ++) { // the determining or fetching path(s) loop
						numberProgress = counter; // progress is updated
						philip.setValue(numberProgress);
						startTime = System.nanoTime();
						if (counter < noted.size() - 1 && !noted.get(counter).equals(notedFiller)) { // the number has been calculated before
							allSequencesAndSuch += "↓Starting Number" + "\n" + noted.get(counter) + "\n\n";
						} else { // the number has not be calculated before
							allSequencesAndSuch += "↓Starting Number" + "\n" + hotpo(counter) + "\n\n";
						}
						endTime = System.nanoTime();
						duration = endTime - startTime;
						tenPercentMostRecentTimes[counter % tenPercentChunk] = duration; // modulo to prevent out-of-bounds (stays within the ten percent chunk)
						if (tenPercentChunkCounter % tenPercentChunk == 0) { // the ten percent chunk is full
							for (int mostRecentTimesCounter = 0; mostRecentTimesCounter < tenPercentMostRecentTimes.length; mostRecentTimesCounter ++) { // adding up all of the most recent times
								totalTime += tenPercentMostRecentTimes[mostRecentTimesCounter];
							}
							recentRuntimesEstimatedTimeRemaining = (totalTime / (long) tenPercentChunk) * (long) (numberPair[1] - counter);
							modeledEstimatedTimeRemaining = (int) (estimatedEndTimeInSeconds - (System.currentTimeMillis() / 1000));
							displayedRecentRuntimesEstimatedTimeRemaining = " Estimated time remaining (using recent runtimes and remaining tasks): " + String.format("%d min(s) %d sec(s)", TimeUnit.NANOSECONDS.toMinutes(recentRuntimesEstimatedTimeRemaining), TimeUnit.NANOSECONDS.toSeconds(recentRuntimesEstimatedTimeRemaining) % 60);
							displayedModeledEstimatedTimeRemaining = " Estimated time remaining (using modeled growth): " + String.format("%d min(s), %d sec(s)", TimeUnit.SECONDS.toMinutes(modeledEstimatedTimeRemaining), modeledEstimatedTimeRemaining % 60);
							JLabelRecentRuntimesEstimatedTimeRemaining.setText(displayedRecentRuntimesEstimatedTimeRemaining);
							if (processorSpeed == -2) { // the user did not enter their computer's processor speed some distance up
								JLabelModeledEstimatedTimeRemaining.setText(" (Experimental Estimated Time Remaining Not In Use)");
							} else { // the user did enter their computer's processor speed some distance up
								JLabelModeledEstimatedTimeRemaining.setText(displayedModeledEstimatedTimeRemaining);
							}
						}
						tenPercentChunkCounter ++;
					}
				} else { // a single number is being tested
					if (numberPair[0] < noted.size() - 1 && !noted.get(numberPair[0]).equals(notedFiller)) { // the number has been calculated before
						allSequencesAndSuch += "↓Starting Number" + "\n" + noted.get(numberPair[0]) + "\n\n";
					} else { // the number has not been calculated before
						allSequencesAndSuch += "↓Starting Number" + "\n" + hotpo(numberPair[0]) + "\n\n";
					}
				}
				jeff.setVisible(false); // done with the calculations, so the JFrame and JProgressBar can be hidden
				if (numberPair[0] < numberPair[1]) { // a range is being tested
					allSequencesAndSuch += determineFlowGates(flowPoints); // calculate the flow gates and record them within "allSequencesAndSuch"
				} else { // a single number is being tested
					allSequencesAndSuch += "The flow point is " + flowPoints.get(flowPoints.size() - 1) + ".";
					IntComparison inola = new IntComparison();
					flowPoints.sort(inola);
				}
				int startOfRangeIndex = 0;
				SecondDimensionFirstIntComparison faris = new SecondDimensionFirstIntComparison();
				notedStartingNumberAndSteps.sort(faris); // make sure the starting numbers are in order
				while (notedStartingNumberAndSteps.get(startOfRangeIndex).get(0) != numberPair[0]) { // getting to the start of the recently tested range in "notedStartingNumberAndSteps"
					startOfRangeIndex ++;
				}
				int endOfRangeIndex = startOfRangeIndex + (numberPair[1] - numberPair[0] + 1); // "+1" because ".sublist" "arg2" is exclusive
				rangedStartingNumberAndSteps = notedStartingNumberAndSteps.subList(startOfRangeIndex, endOfRangeIndex); // done to avoid issues with "notedStartingNumberAndStepsStepFiller"
				SecondDimensionSecondIntComparison salma = new SecondDimensionSecondIntComparison(); // for ordering the steps within "notedStartingNumberAndSteps" in increasing sequence
				rangedStartingNumberAndSteps.sort(salma);
				if (numberPair[0] < numberPair[1]) { // a range is being tested
					String modalInformation = ""; // will hold the modal value of the tested range along with the modal path length(s) and corresponding starting numbers
					int previousMaxFrequency = 1, // .
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
						modalInformation += "The maximum path re-occurence rate is " + maxFrequency + ", for the following path length(s) and starting numbers: ";
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
								modalInformation += "\n\t" + rangedStartingNumberAndSteps.get(counter).get(1) + " ("; // record the path length (should be the same for all of the entries making up the modal value) and open parenthesis for a space for the starting numbers
								int startIndex = counter - (maxFrequency - 2); // adjustment (when in this if-statement, "counter" is at the second-to-last member of the 'frequent group' (see previous if-statement logic) so -2)
								int innerCounter = 0;
								for (innerCounter = startIndex; innerCounter < startIndex + maxFrequency; innerCounter ++) { // go back to the beginning of the modal stretch (the beginning of the entries making up the modal value) and get the starting numbers for each one
									modalInformation += rangedStartingNumberAndSteps.get(innerCounter).get(0); // append the starting number
									if (innerCounter != (startIndex + maxFrequency) - 1) { // if not at the end of the entries making up the modal value...
										modalInformation += ", "; //...append a comma
									}
								}
								modalInformation += ")."; // close parenthesis and period to contain the starting numbers and indicate the end. New line for clarity and looks.
								counter = innerCounter - 1; // the outer loop will pick up where the inner loop stepped down (the subtraction cancels out the incrementing of "counter" from the for-loop update statement)
							}
						}
						modalInformation += "\n";
					}
					else { // there is no mode
						modalInformation += "No mode.\n";
					}
					String meanInformation = ""; // will state the average path length
					double pathLengthSummation = 0.000;
					double mean = 0.000;
					for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter ++) { // adding up all of the path lengths
						pathLengthSummation += rangedStartingNumberAndSteps.get(counter).get(1);
					}
					mean = pathLengthSummation / (rangedStartingNumberAndSteps.size());
					NumberFormat nuFor = new DecimalFormat("#0.000"); // the mean format will go out to the thousandths place
					mean = Double.parseDouble(nuFor.format(mean)); // apply the format, convert the answer back to a double
					meanInformation += "The average path is " + mean + ".\n";
					ArrayList<Integer> beginningOfSortedByStepsRange = rangedStartingNumberAndSteps.get(0);
					String startingNumbersWithShortestPath = ""; // will hold the starting number(s) which have the shortest path
					ArrayList<Integer> middleOfSortedByStepsRange = new ArrayList<Integer>();
					String startingNumbersWithMedianPath = ""; // will hold the starting number(s) which have the median path
					boolean point5 = false, // whether the median is a number.5 or just a number
							correspondingStartNumber = true; // whether there is a corresponding start number with the median steps
					if (rangedStartingNumberAndSteps.size() % 2 == 0) { // even number of elements
						int firstMiddleNumber = rangedStartingNumberAndSteps.get((rangedStartingNumberAndSteps.size() / 2) - 1).get(1);
						int secondMiddleNumber = rangedStartingNumberAndSteps.get(rangedStartingNumberAndSteps.size() / 2).get(1);
						double median = (firstMiddleNumber + secondMiddleNumber) / 2.0;
						int medianInt = (int) Math.floor(median);
						if (median != medianInt) { // there's a decimal, specifically .5
							point5 = true;
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
					if (point5) { // the median value is a number with .5
						middleNumberOfSteps = "The median path is " + middleOfSortedByStepsRange.get(1) + ".5 step(s), with starting number(s) " + startingNumbersWithMedianPath + "\n";
					} else { // the median value is an integer
						middleNumberOfSteps = "The median path is " + middleOfSortedByStepsRange.get(1) + " step(s), with starting number(s) " + startingNumbersWithMedianPath + "\n"; // retrieve median path details and store in a string
					}
					String mostNumberOfSteps = "The longest path is " + endOfSortedByStepsRange.get(1) + " step(s), with starting number(s) " + startingNumbersWithLongestPath + "\n\n"; // retrieve longest path details and store in a string
					allSequencesAndSuch += leastNumberOfSteps + middleNumberOfSteps + modalInformation + meanInformation + mostNumberOfSteps; // record them in "allSequencesAndSuch"
				}
				floorAndCeilingValues[0] = String.valueOf(numberPair[0]); // ...
				floorAndCeilingValues[1] = String.valueOf(numberPair[1]); // ...converting the beginning and end of the range into strings, then storing them in a string array
				if (numberPair[0] != numberPair[1]) { // a range was tested
					testedRanges.add(new ArrayList<Integer>(Arrays.asList(numberPair[0], numberPair[1]))); // recording the range
				}
				int answer = gui.giveYesNo("Would you like to input different data?\n"
										 + "(Results are shown after all inputting is complete)"); // the user's response is stored
				if (answer == 0) { // response is "Yes"
					numberPair = acquireNumberPair(); // get a new range
					continue; // move to the beginning of Ultramarine
				} else if (answer == 1) { // response is "No"
					JFrame teaRex = gui.giveTextArea(allSequencesAndSuch, 0, 0); // displaying information gathered
					StepsLineGraph.main(floorAndCeilingValues);
					break Ultramarine;
				} else if (answer == 2) { // response is "Quit"
					break Ultramarine;
				}
			}
		gui.give("Thank you for using this program!\n\n"
			   + "Kind wishes!"); // appreciation and good wishes
		System.exit(0); // ensure the program ends
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
				System.exit(0);
			}
			try {
				numberPair[0] = Integer.parseInt(str1); // try to convert the user's response into an int
			} catch (NumberFormatException nfe) { // if the user's response was not a number
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
				System.exit(0);
			}
			try {
				numberPair[1] = Integer.parseInt(str2); // try to convert the user's response into an int
			} catch (NumberFormatException nfe) { // if the user's response was not a number
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
	
	private static String determineFlowGates(ArrayList<Integer> flowPoints) { // finds if any of the flow points qualify to be flow gates (must have a frequency of 10% or higher) and returns that information
		JFrame jeff = new JFrame("Flow Gates Calculations"); // for displaying the progress of calculating the flow gates within the range tested [in practice, the progress is quick, so the displaying is imperceptible (at least with the numbers tested)
		jeff.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JProgressBar philip = new JProgressBar(0, flowPoints.size()); // the actual progress bar with bounds zero and last element of "flowPoints"
		int flowGatesProgress = 0;
		philip.setValue(flowGatesProgress);
		philip.setStringPainted(true);
		jeff.getContentPane().add(philip);
		FontMetrics fahima = jeff.getFontMetrics(gui.getFont()); // for sizing the JFrame
		jeff.setPreferredSize(new Dimension(fahima.stringWidth(jeff.getTitle()) + 248, 100)); // 248 is the required additional constant (for the total width of the buttons in the upper right corner?)
		jeff.pack();
		int jeffsCenterXValue = jeff.getWidth() / 2; // center point of the JFrame (x-coordinate)
		int jeffsCenterYValue = jeff.getHeight() / 2; // center point of the JFrame (y-coordinate)
		jeff.setLocation((gui.getScreensize().width / 2) - (jeffsCenterXValue), (gui.getScreensize().height / 2) - (jeffsCenterYValue)); // in the middle of the screen
		jeff.setVisible(true);
		int thresholdPercentage = 10; // the threshold after which a flow point is considered a flow gate
		String flowGates = "Here are the " + thresholdPercentage + "% threshold flow gates:\n";
		double thresholdDecimal = thresholdPercentage / 100.0;
		IntComparison inola = new IntComparison(); // for ordering the flow points in increasing sequence
		flowPoints.sort(inola);
		int frequencyCounter = 1; // for seeing if a flow point qualifies to be a flow gate
		int total = flowPoints.size(); // for seeing if a flow point qualifies to be a flow gate
		for (int counter = 1; counter <= flowPoints.size(); counter++) { // calculating flow gates
			flowGatesProgress = counter; // update the progress
			philip.setValue(flowGatesProgress);
			if (counter != total) { // not at the end of "flowPoints"
				if (flowPoints.get(counter) == flowPoints.get(counter - 1)) { // the current flow point and the previous flow point are the same
					frequencyCounter++; // increase the known frequency of the flow point
				} else { // the current flow point and the previous flow point are different
					double frequency = (double) frequencyCounter / (double) total; // calculate and store the frequency of the previous flow point
					frequencyCounter = 1; // "zero" the counter
					if (frequency >= thresholdDecimal) { // the flow point is considered to be a flow gate
						flowGates += flowPoints.get(counter - 1) + " (" + (frequency * 100) + "%) "; // retrieve the previous flow point (now considered a flow gate) and record it along with its frequency in "flowGates"
					}
				}
			} else { // at the end of "flowPoints"
				double frequency = (double) frequencyCounter / (double) total; // calculate and store the frequency of the last flow point in "flowPoints"
				if (frequency >= thresholdDecimal) { // the flow point is considered to be a flow gate
					flowGates += flowPoints.get(counter - 1) + " (" + (frequency * 100) + "%) "; // retrieve the last flow point (now considered a flow gate) and record it along with its frequency in "flowGates"
				}
			}
		}
		flowGates += "\n\n"; // add two empty lines for cleanliness
		jeff.dispose();
		return flowGates;
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
					flowPoints.add(num); // record num as a flow point in "flowPoints"
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
		if (startingNum > noted.size()) { // the beginning number doesn't have a spot in "noted" (can occur when the user tests a single number after testing a range, with the single number outside of that range
			noted.ensureCapacity(startingNum); // make room in "noted"
			for (int counter = noted.size(); counter < startingNum - 1; counter++) { // fill that room
				noted.add(notedFiller);
			}
			notedStartingNumberAndSteps.ensureCapacity(startingNum); // make room in "notedStartingNumberAndSteps"
			for (int counter = notedStartingNumberAndSteps.size(); counter < startingNum - 1; counter++) { // fill that room
				notedStartingNumberAndSteps.add(new ArrayList<Integer>(Arrays.asList(counter, notedStartingNumberAndStepsStepFiller)));
			}
		}
		if (startingNum < noted.size() && noted.get(startingNum).equals(notedFiller)) { // the beginning number has a spot in "noted," and that spot is filled with "notedFiller"
			noted.set(startingNum, sequence); // set that spot as containing the sequence instead
		} else { // the beginning number is right outside of "noted" (the next number after the last number in "noted") (?)
			noted.add(sequence); // records the sequence of the beginning number in "noted"
		}
		if (startingNum < notedStartingNumberAndSteps.size() && notedStartingNumberAndSteps.get(startingNum).get(1).equals(notedStartingNumberAndStepsStepFiller)) { // the beginning number has a spot in "notedStartingNumberAndSteps" and that spot's "steps" dimension is filled with "notedStartingNumberAndStepsStepFiller"
			notedStartingNumberAndSteps.get(startingNum).set(1, steps); // set the "steps" dimension to store the steps recorded before
		} else { // the beginning number is right outside of "notedStartingNumberAndSteps" (it's the next number after the last number in "notedStartingNumberAndSteps") (?)
			notedStartingNumberAndSteps.add(new ArrayList<Integer>(Arrays.asList(startingNum, steps)));
		}
		return sequence;
	}
	
	public static class StepsLineGraph extends Application { // for constructing and displaying the line graph of path length vs. starting number
		
		static int ceiling; // stores the end of the range tested
		
		static int floor; // stores the beginning of the range tested
		
		static LineChart <Number, Number> stepsVsStartingNumber; // the line graph
		
		static NumberAxis horace, vesuvio;  // horizontal axis (starting number) and vertical axis (path length)
		
		static Scene billyShakes; // the scene will take up a third of the vertical part of the screen and all of the horizontal part
		
		static Stage steph;
		
		static XYChart.Series<Number, Number> points; // to store the x-y points (starting number - path length points)
		
		public static void initialize(ArrayList<Integer> range) { // setting up the graph and using the first range
			floor = range.get(0);
			ceiling = range.get(1);
			int numberOfIntegersTested = ceiling - floor + 1; // the "+1" ensures "numberOfIntegersTested" will be accurate (has to do with including either the beginning or the end of the range)
			int tickSpacing = (int) Math.round(0.05 * (numberOfIntegersTested)); // at 5%-of-the-range increments
			if (tickSpacing == 0) { // due to rounding
				tickSpacing = 1;
			}
			horace = new NumberAxis("Starting Number", (double) ((floor == 1) ? (floor - 1) : floor), (double) ceiling, tickSpacing);
			vesuvio = new NumberAxis();
			vesuvio.setLabel("Path Length");
			stepsVsStartingNumber = new LineChart<Number, Number>(horace, vesuvio);
			stepsVsStartingNumber.setTitle("Steps to Reach ONE for Numbers " + floor + " to " + ceiling);
			points = new XYChart.Series<Number, Number>();
			points.setName("Data");
			SecondDimensionFirstIntComparison faris = new SecondDimensionFirstIntComparison();
			notedStartingNumberAndSteps.sort(faris); // make sure the starting numbers are in order
			int startOfRangeIndex = 0;
			while (notedStartingNumberAndSteps.get(startOfRangeIndex).get(0) != floor) { // getting to the start of the recently tested range in "notedStartingNumberAndSteps"
				startOfRangeIndex ++;
			}
			int endOfRangeIndex = startOfRangeIndex + (ceiling - floor + 1); // "+1" because ".sublist" "arg2" is exclusive
			rangedStartingNumberAndSteps = notedStartingNumberAndSteps.subList(startOfRangeIndex, endOfRangeIndex); // done to avoid issues with "notedStartingNumberAndStepsStepFiller"
			rangedStartingNumberAndSteps.sort(faris); // so the "Gathering" is correct ("notedStartingNumberAndSteps" had been previously sorted based on the "Steps" dimension...)
			Gathering:
				for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter++) { // recording the x-y points
					points.getData().add(new XYChart.Data<Number, Number>(rangedStartingNumberAndSteps.get(counter).get(0), rangedStartingNumberAndSteps.get(counter).get(1)));
				}
			stepsVsStartingNumber.getData().add(points); // putting the points on the line graph
			billyShakes = new Scene(new Group(), gui.getScreensize().getWidth(), gui.getScreensize().getHeight() / 3);
			steph.setScene(billyShakes); // put the scene on the stage
			steph.setX(0); // place the stage's upper left corner all the way to the left
			steph.setY(gui.getScreensize().height / 3); // place the stage's upper left corner a third of the way down the screen (under "teaRex")
		}
		
		public void start (Stage paramSteph) {
			steph = new Stage();
			Platform.setImplicitExit(true);
			steph.setTitle("Graph(s)");
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
			GridPane georgePierre = new GridPane();
			georgePierre.add(rangeOptions, 0, 0);
			georgePierre.add(stepsVsStartingNumber, 0, 1);
			georgePierre.setPrefSize(billyShakes.getWidth(), billyShakes.getHeight());
			georgePierre.setVgap(10);
			GridPane.setHgrow(stepsVsStartingNumber, Priority.ALWAYS);
			GridPane.setVgrow(stepsVsStartingNumber, Priority.ALWAYS);
			Group billyShakesRoot = (Group) billyShakes.getRoot();
			billyShakesRoot.getChildren().add(georgePierre);
			steph.show();
		}

		public static void main(String[] args) {
			launch();
		}
		
		public static void update (ArrayList<Integer> range) { // to change the displayed graph to the graph of the selected range
			steph.setOpacity(0.5); // disappear a bit while stuff is shuffling
			floor = range.get(0);
			ceiling = range.get(1);
			stepsVsStartingNumber.setTitle("Steps to Reach ONE for Numbers " + floor + " to " + ceiling);
			stepsVsStartingNumber.setData(FXCollections.observableArrayList());
			horace.setLowerBound((double) ((floor == 1) ? (floor - 1) : floor));
			horace.setUpperBound((double) ceiling);
			int numberOfIntegersTested = ceiling - floor + 1; // the "+1" ensures "numberOfIntegersTested" will be accurate (has to do with including either the beginning or the end of the range)
			int tickSpacing = (int) Math.round(0.05 * (numberOfIntegersTested)); // at 5%-of-the-range increments
			if (tickSpacing == 0) { // due to rounding
				tickSpacing = 1;
			}
			horace.setTickUnit(tickSpacing);
			points = new XYChart.Series<Number, Number>();
			points.setName("Data");
			SecondDimensionFirstIntComparison faris = new SecondDimensionFirstIntComparison();
			notedStartingNumberAndSteps.sort(faris); // make sure the starting numbers are in order
			int startOfRangeIndex = 0;
			while (notedStartingNumberAndSteps.get(startOfRangeIndex).get(0) != floor) { // getting to the start of the recently tested range in "notedStartingNumberAndSteps"
				startOfRangeIndex ++;
			}
			int endOfRangeIndex = startOfRangeIndex + (ceiling - floor + 1); // "+1" because ".sublist" "arg2" is exclusive
			rangedStartingNumberAndSteps = notedStartingNumberAndSteps.subList(startOfRangeIndex, endOfRangeIndex); // done to avoid issues with "notedStartingNumberAndStepsStepFiller"
			rangedStartingNumberAndSteps.sort(faris); // so the "Gathering" is correct ("notedStartingNumberAndSteps" had been previously sorted based on the "Steps" dimension...)
			Gathering:
				for (int counter = 0; counter < rangedStartingNumberAndSteps.size(); counter++) {
					points.getData().add(new XYChart.Data<Number, Number>(rangedStartingNumberAndSteps.get(counter).get(0), rangedStartingNumberAndSteps.get(counter).get(1)));
				}
			stepsVsStartingNumber.getData().add(points);
			steph.setOpacity(1); // return to clear visibility
		}
	}
}