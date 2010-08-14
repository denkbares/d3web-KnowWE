/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.tirex.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A singleton class providing the settings, which are necessary to run the
 * TiRexInterpreter. Currently the allowed levenstein-distance for pairs of
 * Strings, the synonym-sets and the regex-knoffice-pairs can be retrieved.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class TiRexSettings {

	/**
	 * The settings for TiRex are meant to be found in this HashMap.
	 */
	public Map<String, String> TIREX_SETTINGS;

	private Map<String, String> RegExps;

	private Map<String, Collection<String>> synonymsMap;

	/**
	 * Instance for the singleton.
	 */
	private static TiRexSettings instance;

	/**
	 * The mimimumMatchPercentage, relevant for several extraction strategies.
	 */
	private double minimumMatchPercentage = -1;

	private TiRexSettings() {
		ResourceBundle settingsBundle = ResourceBundle
				.getBundle("TiRexSettings");
		Enumeration<String> keyset = settingsBundle.getKeys();

		TIREX_SETTINGS = new HashMap<String, String>();

		while (keyset.hasMoreElements()) {
			String key = keyset.nextElement();
			TIREX_SETTINGS.put(key, settingsBundle.getString(key));
		}
		// try {
		// fetchTiRexSettings();
		// } catch (IOException e) {
		// System.out.println("Loading TiRexSettgins failed.");
		// }
	}

	private TiRexSettings(String settings, String synonyms, String regExp) {

		if (settings != null) {
			TIREX_SETTINGS = new HashMap<String, String>();

			BufferedReader lnr = new BufferedReader(new StringReader(settings));
			// LineNumberReader lnr = new LineNumberReader(new
			// FileReader(datei));

			String zeile = null;
			try {
				zeile = lnr.readLine();
			}
			catch (IOException e) { /* StringReader should be no problem */
			}
			while (zeile != null) {
				if (zeile.contains("=")) {
					int split = zeile.indexOf("=");
					TIREX_SETTINGS.put(zeile.substring(0, split - 1), zeile
							.substring(split + 1, zeile.length()));
				}
				try {
					zeile = lnr.readLine();
				}
				catch (IOException e) { /* StringReader should be no problem */
				}
			}
		}
		else { // get default ressourcebundle if TiRexSettingsPage somehow
			// fails to load
			ResourceBundle settingsBundle = ResourceBundle
					.getBundle("TiRexSettings");
			Enumeration<String> keyset = settingsBundle.getKeys();

			TIREX_SETTINGS = new HashMap<String, String>();

			while (keyset.hasMoreElements()) {
				String key = keyset.nextElement();
				TIREX_SETTINGS.put(key, settingsBundle.getString(key));
			}
		}

		this.initRegexKnofficePairs(regExp);

		this.initSynonymsMap(synonyms);

	}

	// /**
	// * This method fills the TIREX_SETTINGS HashMap with values
	// *
	// * @throws IOException
	// * If problems with reading the settings-file occur.
	// */
	// private void fetchTiRexSettings() throws IOException {
	// BufferedReader settingsReader = new BufferedReader(new StringReader(
	// TiRexFileReader.getInstance().getTiRexSettingsFile()));
	//
	// String line;
	// while ((line = settingsReader.readLine()) != null) {
	// String[] keyAndValue = line.split(" = ", 2);
	// TIREX_SETTINGS.put(keyAndValue[0], keyAndValue[1]);
	// }
	//
	// }

	/**
	 * @return The unique instance of TiRexSettings.
	 */
	public static TiRexSettings getInstance() {
		if (instance == null) {
			instance = new TiRexSettings();
		}

		return instance;
	}

	public static TiRexSettings createNewInstanceFromStrings(String settings,
			String synonyms, String regExp) {
		if (settings == null) {
			System.out.println("!!! Settings are NULL !!!");
		}
		if (synonyms == null) {
			System.out.println("!!! Synonyms are NULL !!!");
		}
		if (regExp == null) {
			System.out.println("!!! regExp are NULL !!!");
		}

		instance = new TiRexSettings(settings, synonyms, regExp);
		return instance;
	}

	/**
	 * @return The sets of synonyms which are saved in an extra text-file.
	 * @throws IOException
	 */
	public Map<String, Collection<String>> getSynonymsMap() throws IOException {
		if (this.synonymsMap == null) {

			// FIXME: TirRexFileReader.getInstance may not have been
			// initialized,
			// lazy instantiation does not set the SynonymSetsFile
			// ==> leading to a nullPointerException at this point
			// Bugfix: Catch NullPointer and initialise synonymsMap empty
			try {
				String synonymes = TiRexUtilities.getInstance().getReaderAsString(
						new FileReader(TiRexFileReader.getInstance()
								.getSynonymSetsFile()));
				this.synonymsMap = createSynMap(synonymes);
			}
			catch (NullPointerException e1) {
				this.synonymsMap = new HashMap<String, Collection<String>>();
			}

		}
		return synonymsMap;
	}

	private void initSynonymsMap(String synonymes) {
		if (synonymes == null) {
			try {
				this.synonymsMap = getSynonymsMap();
			}
			catch (IOException e) {
				System.err.println("sysnomysmap missing");
				e.printStackTrace();
				synonymsMap = new HashMap<String, Collection<String>>();
			}
			// FIXME: This is just a hotfix (look FIXME above)
			// remove this catch block asap
			catch (NullPointerException npe) {
				System.err
						.println("sysnomysmap missing, caused by catched Exception (hotfix):");
				npe.printStackTrace();
				synonymsMap = new HashMap<String, Collection<String>>();
			}

		}
		else {
			this.synonymsMap = createSynMap(synonymes);
		}
	}

	private HashMap<String, Collection<String>> createSynMap(String synonymes) {
		HashMap<String, Collection<String>> syns = new HashMap<String, Collection<String>>();
		String[] synonymLines = synonymes.split("\n");

		for (String synonymLine : synonymLines) {
			String[] temp = synonymLine.split(" = ");

			syns.put(temp[0], TiRexUtilities.getInstance()
					.convertArrayToCollection(temp[1].split(", ")));
		}
		return syns;
	}

	/**
	 * @return The allowed levenstein distances as they are saved in the
	 *         ResourceBundle. The distances are coded as follows:
	 *         allowed_distance in lower_limit-upper_limit
	 */
	public Collection<String> getAllowedEditDistancesForLevenstein() {
		Collection<String> distances = new ArrayList<String>();

		String codedDistances = TIREX_SETTINGS
				.get("property.allowedEditDistance");
		String[] differentDistances = codedDistances.split(", ");

		for (String s : differentDistances) {
			distances.add(s);
		}

		return distances;
	}

	// public void initRegExp()

	public Map<String, String> getRegexKnofficePairs() {
		return this.RegExps;
	}

	/**
	 * @return A Map is returned, the regexes are keys and the according
	 *         KnOffice shape is saved as an object with its respective key
	 * @throws IOException If opening or reading the regex-file file fails
	 *         somehow.
	 */
	public Map<String, String> initRegexKnofficePairs() throws IOException {

		InputStream is = getClass().getResourceAsStream(
				"/TiRexNumericalRegularExpressions.txt");
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));

		return readRegExps(bufReader);
	}

	public void initRegexKnofficePairs(String regExp) {
		if (regExp == null) {
			try {
				initRegexKnofficePairs();
			}
			catch (IOException e) {
				System.err
						.println("TiRexNumericalRegularExpressions.txt missing");
				e.printStackTrace();
			}
		}
		else {
			this.RegExps = readRegExps(new StringReader(regExp));
		}
	}

	private Map<String, String> readRegExps(Reader bufReader) {

		Map<String, String> map = new HashMap<String, String>();
		String numericalExpressions = null;

		try {
			numericalExpressions = TiRexUtilities.getInstance()
					.getReaderAsString(bufReader);
		}
		catch (IOException e) {
			System.out.println("ERROR: init KnOffice RegExps from String");
			e.printStackTrace();
		}

		String extractionRegex = "def .*\n(.+\n)+[}]";
		Pattern extractionPattern = Pattern.compile(extractionRegex);

		Matcher extractionMatcher = extractionPattern
				.matcher(numericalExpressions);

		while (extractionMatcher.find()) {
			String extracted = extractionMatcher.group();

			Pattern replacementP = Pattern.compile("def \"(.*)\" [{]");
			Matcher replacementM = replacementP.matcher(extracted);

			Pattern pToReplace = Pattern.compile("\n(.+)(?=\n)");
			Matcher mToReplace = pToReplace.matcher(extracted);

			if (replacementM.find()) {
				String replacement = replacementM.group(1);
				while (mToReplace.find()) {
					String toReplace = mToReplace.group(1);

					map.put(toReplace, replacement);
				}
			}
		}

		return map;
	}

	/**
	 * @return The minimumMatchPercentage is set to a default value (as it is
	 *         read from the TiRexSettings-ResourceBundle), if it has not been
	 *         set yet. Otherwise the minimumMatchPercentage is returned as it
	 *         is currently set.
	 */
	public double getMinimumMatchPercentage() {
		if (minimumMatchPercentage == -1) {
			minimumMatchPercentage = Double.parseDouble(TIREX_SETTINGS
					.get("property.minimumMatchPercentage"));
		}

		return minimumMatchPercentage;
	}

	public void setMinimumMatchPercentage(double value) {
		minimumMatchPercentage = value;
	}
}
