package json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import xml.model.Pair;

/**
 * Created by ekaterina on 20.08.2015.
 */
public class NearestNeighbourOnJSONResolver {
	private static final int VALUES_INDEX_IN_JSON = 0;
	public static final String DATA_PROPERTY = "data";
	public static final String VALUES_PROPERTY = "values";
	public static final int DEFAULT_K = 10;

	public static void main(String[] args) {
		try {
			List<File> similarJSONFilesForAudio = getSimilarJSONFilesForAudio(new File(
					"C:/RWTH/AndroidLab/xml/json1/01. Avril Lavigne Losing Grip.json"), "C:/RWTH/AndroidLab/xml/json1");
			for (File file : similarJSONFilesForAudio) {
				System.out.println(file.getAbsolutePath());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * A method which builds a list of json files forming the first K closest
	 * neighbours to the centerJson file
	 * 
	 * @param centerJson
	 *            a json file which the nearest neighbours will be search to
	 * @param folderWithJsonsPath
	 *            a folder containing all the json files
	 * @return a list of json files which are the first K closest neighbours to
	 *         the centerJson file
	 * @throws IOException
	 */
	public static List<File> getSimilarJSONFilesForAudio(File centerJson, String folderWithJsonsPath)
			throws IOException {
		List<Pair<File, Pair<File, Double>>> closestFilesWithDistances = getSimilarObjectsForAudio(centerJson,
				folderWithJsonsPath);
		List<File> firstKNearestNeighbours = new ArrayList<>();
		for (Pair<File, Pair<File, Double>> closestNeighbour : closestFilesWithDistances) {
			firstKNearestNeighbours.add(closestNeighbour.secondArg.firstArg);
		}
		return firstKNearestNeighbours;
	}

	/**
	 * A method which searches for the first K nearest neighbours to the center
	 * file. The center file will also be included into result as the closest
	 * one with the distance 0.0
	 * 
	 * @param centerJson
	 *            a json file which the nearest neighbours will be search to
	 * @param folderWithJsonsPath
	 *            a folder containing all the json files
	 * @return a list of pairs where he first argument is the center file and
	 *         the second argument is a file-distance pair to the closest files
	 * @throws IOException
	 */
	public static List<Pair<File, Pair<File, Double>>> getSimilarObjectsForAudio(File centerJson,
			String folderWithJsonsPath) throws IOException {
		List<Pair<File, Pair<File, Double>>> similarAudioFiles = new ArrayList<>();
		double[] featureVector = getFeatures(centerJson);
		if (featureVector != null) {
			List<Pair<File, Double>> distances = new LinkedList<>();
			for (File anotherJsonFile : new File(folderWithJsonsPath).listFiles()) {
				double[] anotherFeatureVector = getFeatures(anotherJsonFile);
				if (anotherFeatureVector != null) {
					double L2Distance = calculateL2DistanceBetweenVectors(featureVector, anotherFeatureVector);

					// remember the distances from audio to all
					// other audio files
					distances.add(new Pair<>(anotherJsonFile, L2Distance));
				}
			}

			// define sorting for distances
			Collections.sort(distances, new Comparator<Pair<File, Double>>() {
				public int compare(Pair<File, Double> o1, Pair<File, Double> o2) {
					return o1.secondArg.compareTo(o2.secondArg);
				}
			});

			int kNumber = DEFAULT_K;

			// here, if the number of elements in the search field is less than
			// a number of requested K nearest neighbours, we put only those
			// which are available
			if (distances.size() < kNumber) {
				Logger.getGlobal().log(Level.SEVERE, "Number of neighbours is less than K.");
				kNumber = distances.size();
			}

			// choose only k nearest neighbors
			for (int i = 0; i < kNumber; i++) {
				similarAudioFiles.add(new Pair<>(centerJson, new Pair<>(distances.get(i).firstArg,
						distances.get(i).secondArg)));
			}
		}
		return similarAudioFiles;
	}

	private static double calculateL2DistanceBetweenVectors(double[] f1, double[] f2) {
		double squaredDistance = 0;
		for (int i = 0; i < f1.length; i++) {
			squaredDistance += Math.pow(f1[i] - f2[i], 2);
		}

		return Math.sqrt(squaredDistance);
	}

	public static double[] getFeatures(File jsonFile) throws IOException {
		if (!jsonFile.exists()) {
			Logger.getGlobal().log(Level.SEVERE, "JSON file does not exist.");
			return null;
		}

		FileInputStream fisTargetFile = new FileInputStream(jsonFile);
		String content = IOUtils.toString(fisTargetFile);
		JSONObject fullJson = new JSONObject(content);
		JSONArray data = fullJson.getJSONArray(DATA_PROPERTY);

		if (data == null || data.length() == 0) {
			Logger.getGlobal().log(Level.SEVERE, "Data in JSON is absent or empty.");
			return null;
		}
		JSONArray values = data.getJSONObject(VALUES_INDEX_IN_JSON).getJSONArray(VALUES_PROPERTY);
		if (values == null || values.length() < 2) {
			Logger.getGlobal().log(Level.SEVERE, "Values in JSON are absent or empty.");
			return null;
		}
		int numberOfFeatures = values.length();
		double[] features = new double[numberOfFeatures - 1];
		for (int i = 1; i < numberOfFeatures; i++) {
			features[i - 1] = values.getDouble(i);
		}
		return features;
	}

}
