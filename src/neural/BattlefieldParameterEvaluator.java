package neural;

import java.util.Arrays;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import javax.imageio.ImageIO;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import robocode.BattleResults;
import robocode.control.*;
import robocode.control.events.*;

public class BattlefieldParameterEvaluator {
	// Minimum allowable battlefield size is 400
	final static int MAXBATTLEFIELDSIZE = 4000;
	// Minimum allowable gun cooling rate is 0.1
	final static double MAXGUNCOOLINGRATE = 10;
	final static int NUMBATTLEFIELDSIZES = 601;
	final static int NUMCOOLINGRATES = 501;
	final static int NUMSAMPLES = 1000;
	// Number of inputs for the multilayer perceptron (size of the input
	// vectors)
	final static int NUM_NN_INPUTS = 2;
	// Number of hidden neurons of the neural network
	final static int NUM_NN_HIDDEN_UNITS = 50;
	// Number of epochs for training
	final static int NUM_TRAINING_EPOCHS = 100000;
	static int NdxBattle;
	static double[] FinalScore1;
	static double[] FinalScore2;

	public static void main(String[] args) throws FileNotFoundException {
		double[] BattlefieldSize = new double[NUMSAMPLES];
		double[] GunCoolingRate = new double[NUMSAMPLES];

		FinalScore1 = new double[NUMSAMPLES];
		FinalScore2 = new double[NUMSAMPLES];
		Random rng = new Random(15L);
		// Disable log messages from Robocode
		RobocodeEngine.setLogMessagesEnabled(false);
		// Create the RobocodeEngine
		// Run from C:/Robocode
		RobocodeEngine engine = new RobocodeEngine(new java.io.File("/Users/EduardoP/robocode"));
		// Add our own battle listener to the RobocodeEngine
		engine.addBattleListener(new BattleObserver());
		// Show the Robocode battle view
		engine.setVisible(false);
		// Setup the battle specification
		// Setup battle parameters
		int numberOfRounds = 1;
		long inactivityTime = 100;
		int sentryBorderSize = 50;
		boolean hideEnemyNames = false;
		// Get the robots and set up their initial states
		RobotSpecification[] competingRobots = engine.getLocalRepository("sample.RamFire,sample.TrackFire");
		RobotSetup[] robotSetups = new RobotSetup[2];
		for (NdxBattle = 0; NdxBattle < NUMSAMPLES; NdxBattle++) {

			// Choose the battlefield size and gun cooling rate
			BattlefieldSize[NdxBattle] = MAXBATTLEFIELDSIZE * (0.1 + 0.9 * rng.nextDouble());
			GunCoolingRate[NdxBattle] = MAXGUNCOOLINGRATE * (0.1 + 0.9 * rng.nextDouble());

			// Create the battlefield
			BattlefieldSpecification battlefield = new BattlefieldSpecification((int) BattlefieldSize[NdxBattle],
					(int) BattlefieldSize[NdxBattle]);

			// Set the robot positions
			robotSetups[0] = new RobotSetup(BattlefieldSize[NdxBattle] / 2.0, BattlefieldSize[NdxBattle] / 3.0, 0.0);
			robotSetups[1] = new RobotSetup(BattlefieldSize[NdxBattle] / 2.0, 2.0 * BattlefieldSize[NdxBattle] / 3.0,
					0.0);

			// Prepare the battle specification
			BattleSpecification battleSpec = new BattleSpecification(battlefield, numberOfRounds, inactivityTime,
					GunCoolingRate[NdxBattle], sentryBorderSize, hideEnemyNames, competingRobots, robotSetups);
			// Run our specified battle and let it run till it is over
			engine.runBattle(battleSpec, true); // waits till the battle
												// finishes
		}
		// Cleanup our RobocodeEngine
		engine.close();
		System.out.println(Arrays.toString(BattlefieldSize));
		System.out.println(Arrays.toString(GunCoolingRate));
		System.out.println(Arrays.toString(FinalScore1));
		System.out.println(Arrays.toString(FinalScore2));
		// Create the training dataset for the neural network
		double[][] RawInputs = new double[NUMSAMPLES][NUM_NN_INPUTS];
		double[][] RawOutputs = new double[NUMSAMPLES][1];
		for (int NdxSample = 0; NdxSample < NUMSAMPLES; NdxSample++) {
			// IMPORTANT: normalize the inputs and the outputs to
			// the interval [0,1]
			RawInputs[NdxSample][0] = BattlefieldSize[NdxSample] / MAXBATTLEFIELDSIZE;
			RawInputs[NdxSample][1] = GunCoolingRate[NdxSample] / MAXGUNCOOLINGRATE;
			RawOutputs[NdxSample][0] = FinalScore1[NdxSample] / 250;
		}
		BasicNeuralDataSet MyDataSet = new BasicNeuralDataSet(RawInputs, RawOutputs);
		// Create and train the neural network
		// ... TODO ...
		// Creating the neural network:
		BasicNetwork pergamperneural = new BasicNetwork();
		pergamperneural.addLayer(new BasicLayer(new ActivationSigmoid(), true, NUM_NN_INPUTS)); // Input
																								// Layer
		pergamperneural.addLayer(new BasicLayer(new ActivationSigmoid(), true, NUM_NN_HIDDEN_UNITS)); // Hidden
																										// Layer
																										// with
																										// 50
																										// neurons
		pergamperneural.addLayer(new BasicLayer(new ActivationSigmoid(), true, 1)); // Output
																					// layer
		pergamperneural.getStructure().finalizeStructure();
		pergamperneural.reset();
		// It's properly done we think.

		System.out.println("Training network...");
		// ... TODO ...
		final Train pergampertrain = new ResilientPropagation(pergamperneural, MyDataSet);
		double[] perro = new double[NUM_TRAINING_EPOCHS];
		PrintWriter pw = new PrintWriter(new File("error.txt"));
		for (int Training_Times = 0; Training_Times < NUM_TRAINING_EPOCHS; Training_Times++) {
			pergampertrain.iteration(); // Training pergampertrain
			perro[Training_Times] = pergampertrain.getError(); //I store the error into an array.
			
			pw.println(perro[Training_Times]);
			
			System.out.println("Error of Epoch  " + Training_Times + " is :" + perro[Training_Times] );
		}
		pw.close();
		System.out.println("Training completed.");
		System.out.println("Testing network...");
		// Generate test samples to build an output image
		int[] OutputRGBint = new int[NUMBATTLEFIELDSIZES * NUMCOOLINGRATES];
		Color MyColor;
		double MyValue = 0;
		double[][] MyTestData = new double[NUMBATTLEFIELDSIZES * NUMCOOLINGRATES][NUM_NN_INPUTS];
		for (int NdxBattleSize = 0; NdxBattleSize < NUMBATTLEFIELDSIZES; NdxBattleSize++) {
			for (int NdxCooling = 0; NdxCooling < NUMCOOLINGRATES; NdxCooling++) {
				MyTestData[NdxCooling + NdxBattleSize * NUMCOOLINGRATES][0] = 0.1
						+ 0.9 * ((double) NdxBattleSize) / NUMBATTLEFIELDSIZES;
				MyTestData[NdxCooling + NdxBattleSize * NUMCOOLINGRATES][1] = 0.1
						+ 0.9 * ((double) NdxCooling) / NUMCOOLINGRATES;
			}
		}

		// Simulate the neural network with the test samples and fill a matrix
		for (int NdxBattleSize = 0; NdxBattleSize < NUMBATTLEFIELDSIZES; NdxBattleSize++) {
			for (int NdxCooling = 0; NdxCooling < NUMCOOLINGRATES; NdxCooling++) {
				BasicMLData input = new BasicMLData(MyTestData[NdxCooling + NdxBattleSize * NUMCOOLINGRATES]);
				final MLData output = pergamperneural.compute(input.clone());
				double MyResult = output.getData()[0]; // output.getData(0); also working. 
				MyValue = ClipColor(MyResult);
				MyColor = new Color((float) 0.0, (float) 0.0, (float) MyValue);
				OutputRGBint[NdxCooling + NdxBattleSize * NUMCOOLINGRATES] = MyColor.getRGB();
						
//						MyTestData[NdxCooling+NdxBattleSize*NUMCOOLINGRATES][0] +
//						MyTestData[NdxCooling+NdxBattleSize*NUMCOOLINGRATES][1]; 
//						
																								// get
																								// output
																								// for
																								// a
																								// given
																								// input
																								// in
																								// the
																								// NN.
			}
		}
		System.out.println("Testing completed.");
		// Plot the training samples
		for (int NdxSample = 0; NdxSample < NUMSAMPLES; NdxSample++) {
			MyValue = ClipColor(FinalScore1[NdxSample] / 250);
			MyColor = new Color((float) 0.0, (float) MyValue, (float) 0.0);
			int MyPixelIndex = (int) (Math
					.round(NUMCOOLINGRATES * ((GunCoolingRate[NdxSample] / MAXGUNCOOLINGRATE) - 0.1) / 0.9)
					+ Math.round(NUMBATTLEFIELDSIZES * ((BattlefieldSize[NdxSample] / MAXBATTLEFIELDSIZE) - 0.1) / 0.9)
							* NUMCOOLINGRATES);
			if ((MyPixelIndex >= 0) && (MyPixelIndex < NUMCOOLINGRATES * NUMBATTLEFIELDSIZES)) {
				OutputRGBint[MyPixelIndex] = MyColor.getRGB();
			}
		}
		BufferedImage img = new BufferedImage(NUMCOOLINGRATES, NUMBATTLEFIELDSIZES, BufferedImage.TYPE_INT_RGB);
		img.setRGB(0, 0, NUMCOOLINGRATES, NUMBATTLEFIELDSIZES, OutputRGBint, 0, NUMCOOLINGRATES);
		File f = new File("pergamperneuraloutput.png");
		try {
			ImageIO.write(img, "png", f);
		} catch (IOException e) {
			// TODO Auto‐generated catch block
			e.printStackTrace();
		}

		System.out.println("Image generated.");
		// Make sure that the Java VM is shut down properly
		System.exit(0);
	}

	/*
	 * Clip a color value (double precision) to lie in the valid range [0,1]
	 */
	public static double ClipColor(double Value) {
		if (Value < 0.0) {
			Value = 0.0;
		}
		if (Value > 1.0) {
			Value = 1.0;
		}
		return Value;

	}

	//
	// Our private battle listener for handling the battle event we are
	// interested in.
	//
	static class BattleObserver extends BattleAdaptor {
		// Called when the battle is completed successfully with battle results
		public void onBattleCompleted(BattleCompletedEvent e) {
			System.out.println("‐‐ Battle has completed ‐‐");

			// Get the indexed battle results
			BattleResults[] results = e.getIndexedResults();

			// Print out the indexed results with the robot names
			System.out.println("Battle results:");
			for (BattleResults result : results) {
				System.out.println("  " + result.getTeamLeaderName() + ": " + result.getScore());
			}
			// Store the scores of the robots
			BattlefieldParameterEvaluator.FinalScore1[NdxBattle] = results[0].getScore();
			BattlefieldParameterEvaluator.FinalScore2[NdxBattle] = results[1].getScore();
		}

		// Called when the game sends out an information message during the
		// battle
		public void onBattleMessage(BattleMessageEvent e) {
			// System.out.println("Msg> " + e.getMessage());
		}

		// Called when the game sends out an error message during the battle
		public void onBattleError(BattleErrorEvent e) {
			System.out.println("Err> " + e.getError());
		}
	}
}