/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * Created on 22.06.2005
 */
package de.d3web.textParser.decisionTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.psMethods.shared.PSMethodShared;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.MMInfoSubject;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.KWikiToKnofficePreParser;

/**
 * Reads a file, which contains all allowed attributes as well as informations
 * about these attributes. These informations can be requested by the
 * AttributeConfigReader. While reading the file, its structure is tested.
 * Errors and inconsistency are written into a report
 * 
 * @author Andreas Klar
 */
public class AttributeConfigReader {

	private Report report;

	private Map<String, String[]> configurations;

	private List<String> allProperties;

	private Object inputFile;

	public static String APRIORI = "A Priori Bewertung";

	public static String SHARED_ABNORMALITY = "Abnormalitï¿½t";

	public static String SHARED_WEIGHT = "Gewicht";

	public static String SHARED_LOCAL_WEIGHT = "Lokales Gewicht";

	public static String SHARED_SIMILARITY = "ï¿½hnlichkeit";

	public static String LINK = "Link";

	public static String MEDIA = "Bilder";
	
	public static String NUM2CHOICE ="Num2Choice";

	public AttributeConfigReader makeAttributeConfigReader(String inputFile) {
		URL url = null;
		try {
			url = new URL(inputFile);
		} catch (Exception e) {
			report.error(new Message(
					"Fehler beim Lesen der Config-Datei: " + e, inputFile
							.toString()));
		}

		return new AttributeConfigReader(url);
	}

	public AttributeConfigReader(URL inputFile) {
			this(KWikiToKnofficePreParser.urlToReader(inputFile));
	}
	
	public AttributeConfigReader(Reader inputFile) {
		this.configurations = new Hashtable<String, String[]>();
		this.report = new Report();
		this.allProperties = new ArrayList<String>(1);
		this.inputFile = inputFile;

		try {

			

			BufferedReader reader = new BufferedReader(inputFile);

			String actLine = reader.readLine();
			for (int count = 1; actLine != null; count++, actLine = reader
					.readLine()) {
				if (!(actLine.startsWith("//") || actLine.equals("")))
					addConfiguration(actLine, count);
			}
			if (inputFile != null) {
				inputFile.close();
			}
		}

		catch (IOException e) {
			report.error(new Message(
					"Fehler beim Lesen der Config-Datei: " + e, inputFile
							.toString()));
			return;
		}

		setConstants();
	}

	/**
	 * sets the constants to identify specific attributes in an attribute table
	 */
	private void setConstants() {
		APRIORI = getAttributeText("setAPrioriProbability()");
		SHARED_ABNORMALITY = getAttributeText("PSMethodShared:SHARED_ABNORMALITY");
		SHARED_WEIGHT = getAttributeText("PSMethodShared:SHARED_WEIGHT");
		SHARED_LOCAL_WEIGHT = getAttributeText("PSMethodShared:SHARED_LOCAL_WEIGHT");
		SHARED_SIMILARITY = getAttributeText("PSMethodShared:SHARED_SIMILARITY");
		LINK = getAttributeText("MMINFO:SUBJECT:LINK");
		MEDIA = getAttributeText("MMINFO:SUBJECT:MEDIA");
		NUM2CHOICE = getAttributeText("NUM2CHOICE");
	}

	/**
	 * Reads a configuration line and saves all information provided in this
	 * line.
	 * 
	 * @param entry
	 *            the line which contains the information
	 * @param lineNo
	 *            number of the line in the attribute file
	 */
	private void addConfiguration(String entry, int lineNo) {
		String[] entryComponents = entry.split("\\|");
		for (int i = 0; i < entryComponents.length; i++)
			entryComponents[i] = entryComponents[i].trim();
		// check if line contains 4 components divided by "|"
		if (entryComponents.length != 4) {
			report.error(new Message("Ungï¿½ltiger Eintrag",
					inputFile.toString(), lineNo, entry));
			return;
		}
		entryComponents = new String[] { entryComponents[0],
				entryComponents[1], entryComponents[2], entryComponents[3],
				"0", "0", "0" };

		// check if each relevant component contains data
		checkEntryForRelevantData(entry, lineNo, entryComponents);
		// check if property-names are valid
		checkPropertyName(lineNo, entryComponents[1]);
		// check if value-definitions are correct
		checkValueDefinitions(lineNo, entryComponents);
		// check for double entry
		if (configurations.containsKey(entryComponents[0])) {
			report.error(new Message("doppelter Eintrag fï¿½r Attribut: \""
					+ entryComponents[0] + "\"", inputFile.toString(), lineNo));
		}
		// check for duplicate propertyName
		else if (allProperties.contains(entryComponents[1].trim())) {
			report.error(new Message(
					"der Property-Name wurde mehrfach angegeben: \""
							+ entryComponents[1] + "\"", inputFile.toString(),
					lineNo));
		}
		// add attribute
		else {
			configurations.put(entryComponents[0], entryComponents);
			if (!entryComponents[1].trim().equals(""))
				allProperties.add(entryComponents[1].trim());
		}
	}

	/**
	 * Tests if the name of the property is valid
	 * 
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param propertyName
	 */
	private void checkPropertyName(int lineNo, String propertyName) {
		propertyName = propertyName.trim();
		if (propertyName.startsWith("MMINFO:SUBJECT")
				&& (propertyName.indexOf(" ") != -1 || propertyName.split(":").length != 3))
			this.report.error(new Message(
					"Ungï¿½ltiger Property-Name fï¿½r MMINFO:SUBJECT", inputFile
							.toString(), lineNo));

		else if (propertyName.startsWith("MMINFO")
				&& (propertyName.trim().indexOf(" ") != -1 || !((propertyName
						.split(":").length == 2) || (propertyName.split(":").length == 3))))
			this.report.error(new Message(
					"Ungï¿½ltiger Property-Name fï¿½r MMINFO: " + propertyName,
					inputFile.toString(), lineNo));

		else if (propertyName.startsWith("PSMethodShared")
				&& (propertyName.indexOf(" ") != -1 || propertyName.split(":").length != 2))
			this.report.error(new Message(
					"Ungï¿½ltiger Property-Name fï¿½r PSMethodShared: "
							+ propertyName, inputFile.toString(), lineNo));
	}

	/**
	 * Checks if the configuration contains an attribute name and range of
	 * values
	 * 
	 * @param entry
	 *            the whole entry
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param entryComponents
	 *            an array containing all components of the configuration
	 */
	private void checkEntryForRelevantData(String entry, int lineNo,
			String[] entryComponents) {
		if (entryComponents[0].equals(""))
			report.error(new Message(
					"Ungï¿½ltiger Eintrag (fehlender Attribut-Name)", inputFile
							.toString(), lineNo, entry));
		if (entryComponents[2].equals(""))
			report.error(new Message(
					"Ungï¿½ltiger Eintrag (fehlender Wertebereich)", inputFile
							.toString(), lineNo, entry));
	}

	/**
	 * Checks if the range of values in the configuration is valid
	 * 
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param entryComponents
	 *            an array containing all components of the configuration
	 */
	private void checkValueDefinitions(int lineNo, String[] entryComponents) {
		// multiplicity construct
		if (!checkForMultiplicityConstruct(lineNo, entryComponents))
			return;
		// list construct
		if (!checkForListConstruct(lineNo, entryComponents))
			return;
		// set of allowed values
		if (checkForSetOfAllowedValues(lineNo, entryComponents))
			return;
		// standard types
		checkForStandardTypes(lineNo, entryComponents);
	}

	/**
	 * Checks if the range of values is a valid standard type, which means
	 * String, Number, or Boolean
	 * 
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param entryComponents
	 *            the range of values defined in the configuration
	 */
	private boolean checkForStandardTypes(int lineNo, String[] entryComponents) {
		if (!(entryComponents[2].equalsIgnoreCase("string")
				|| entryComponents[2].equalsIgnoreCase("number") || entryComponents[2]
				.equalsIgnoreCase("boolean"))) {
			report.error(new Message("Ungï¿½ltige Wertebereichsangabe ",
					inputFile.toString(), lineNo, entryComponents[2]));
			return false;
		}
		return true;
	}

	/**
	 * Checks if more than one value is allowed for an attribute
	 * 
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param entryComponents
	 *            the range of values defined in the configuration
	 */
	private boolean checkForSetOfAllowedValues(int lineNo,
			String[] entryComponents) {
		if (entryComponents[2].startsWith("{")
				|| entryComponents[2].endsWith("}")) {
			if (entryComponents[2].matches("[{][^{}]+[^}]")
					|| entryComponents[2].matches("[^{][^{}]+[}]")) {
				report.error(new Message("Ungï¿½ltiges \"Mengen-Konstrukt\" in "
						+ "Wertebereichsangabe (fehlende Klammer)", inputFile
						.toString(), lineNo, entryComponents[2]));
				return true;
			} else if (entryComponents[2].matches("[{][^{}]+[}]")) {
				entryComponents[2] = entryComponents[2].substring(1,
						entryComponents[2].length() - 1);
				entryComponents[6] = "1";
				return true;
			} else {
				report
						.error(new Message(
								"Ungï¿½ltiges \"Mengen-Konstrukt\" in Wertebereichsangabe",
								inputFile.toString(), lineNo,
								entryComponents[2]));
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the attribute value may be a list of values
	 * 
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param entryComponents
	 *            the range of values defined in the configuration
	 */
	private boolean checkForListConstruct(int lineNo, String[] entryComponents) {
		if (entryComponents[2].toLowerCase().startsWith("list")) {
			if (entryComponents[2].toLowerCase().matches("list[<][^<>]+[>]")) {
				entryComponents[2] = entryComponents[2].substring(5,
						entryComponents[2].length() - 1);
				entryComponents[5] = "1";
				return true;
			} else {
				report.error(new Message(
						"Ungï¿½ltiges \"List-Konstrukt\" in Wertebereichsangabe",
						inputFile.toString(), lineNo, entryComponents[2]));
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the attribute may have more than one value in the attribute
	 * table
	 * 
	 * @param lineNo
	 *            line number of the configuration in the input file
	 * @param entryComponents
	 *            the range of values defined in the configuration
	 */
	private boolean checkForMultiplicityConstruct(int lineNo,
			String[] entryComponents) {
		if (entryComponents[2].startsWith("[")
				|| entryComponents[2].endsWith("]*")) {
			if (entryComponents[2].matches("[\\[][^\\[\\]]+[\\]][*]")) {
				entryComponents[2] = entryComponents[2].substring(1,
						entryComponents[2].length() - 2);
				entryComponents[4] = "1";
				return true;
			} else {
				report
						.error(new Message(
								"Ungï¿½ltiges \"Multiplizitï¿½ts-Konstrukt\" in Wertebereichsangabe",
								inputFile.toString(), lineNo,
								entryComponents[2]));
				return false;
			}
		}
		return true;
	}

	public Report getReport() {
		return this.report;
	}

	public String getAttributeText(String propertyName) {
		for (Iterator it = this.configurations.values().iterator(); it
				.hasNext();) {
			String[] nextEntry = (String[]) it.next();
			if (nextEntry[1].equalsIgnoreCase(propertyName))
				return nextEntry[0];
		}
		return null;
	}

	public Property getPropertyField(String attributeText) {
		if (!configurations.containsKey(attributeText))
			return null;
		String propertyName = (configurations.get(attributeText))[1];
		String fieldName = propertyName.split(":")[0];
		Iterator iter = Arrays.asList(Property.class.getFields()).iterator();
		while (iter.hasNext()) {
			Field field = (Field) iter.next();
			if (field.getName().equals(fieldName)) {
				try {
					return (Property) field.get(null);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}

	public PSMethodShared getPSMethodSharedField(String attributeText) {
		if (!configurations.containsKey(attributeText))
			return null;
		String propertyName = (configurations.get(attributeText))[1];
		String fieldName = propertyName.split(":")[1];
		Iterator iter = Arrays.asList(PSMethodShared.class.getFields())
				.iterator();
		while (iter.hasNext()) {
			Field field = (Field) iter.next();
			if (field.getName().equals(fieldName)) {
				try {
					return (PSMethodShared) field.get(null);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}

	public DCElement getDCElement(String attributeText) {
		if (!configurations.containsKey(attributeText))
			return null;
		String propertyName = (configurations.get(attributeText))[1];
		String fieldName = propertyName.split(":")[1];
		Iterator iter = Arrays.asList(DCElement.class.getFields()).iterator();
		while (iter.hasNext()) {
			Field field = (Field) iter.next();
			if (field.getName().equals(fieldName)) {
				try {
					return (DCElement) field.get(null);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}

	public MMInfoSubject getMMInfoSubject(String attributeText) {
		if (!configurations.containsKey(attributeText))
			return null;
		String propertyName = (configurations.get(attributeText))[1];
		String fieldName = propertyName.split(":")[2];
		Iterator iter = Arrays.asList(MMInfoSubject.class.getFields())
				.iterator();
		while (iter.hasNext()) {
			Field field = (Field) iter.next();
			if (field.getName().equals(fieldName)) {
				try {
					return (MMInfoSubject) field.get(null);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}

	public List<String> getAllowedValues(String attributeText) {
		if (configurations.containsKey(attributeText)) {
			String allowedValuesString = (configurations
					.get(attributeText))[2];
			List<String> allowedValues = new ArrayList<String>(1);
			if (hasSetOfAllowedValues(attributeText)) {
				String[] temp = allowedValuesString.split(",");
				for (int i = 0; i < temp.length; i++)
					allowedValues.add(temp[i].trim());
				return allowedValues;
			} else {
				allowedValues.add(allowedValuesString);
				return allowedValues;
			}
		}
		return null;
	}

	public boolean hasSetOfAllowedValues(String attributeText) {
		if (configurations.containsKey(attributeText)
				&& (configurations.get(attributeText))[6]
						.equals("1"))
			return true;
		return false;
	}

	private boolean isAllowed(List<String> allowedValues, List<String> allValues) {
		if (allowedValues.size() > 1) {
			// check all values
			for (Iterator it = allValues.iterator(); it.hasNext();) {
				if (!allowedValues.contains(it.next()))
					return false;
			}
		} else if (allowedValues.get(0).equalsIgnoreCase("number")) {
			for (Iterator it = allValues.iterator(); it.hasNext();) {
				try {
					Double.parseDouble((String) it.next());
				} catch (NumberFormatException e) {
					return false;
				}
			}
		}
		// allowed values for Boolean are: true, yes, ja, false, no, nein
		else if (allowedValues.get(0).equalsIgnoreCase("boolean")) {
			for (Iterator it = allValues.iterator(); it.hasNext();) {
				String val = ((String) it.next()).toLowerCase();
				if (!(val.equals("true") || val.equals("yes")
						|| val.equals("ja") || val.equals("false")
						|| val.equals("no") || val.equals("nein")))
					return false;
			}
		}
		// type "string" accepts every value
		return true;
	}

	// validation of values is case-sensitive !!!
	public boolean isAllowedAttributeValue(String attributeText, String value) {
		if (!configurations.containsKey(attributeText))
			return false;
		// split "value" if it's a list
		List<String> allValues = new ArrayList<String>(1);
		if (value.startsWith("{") && value.endsWith("}")) {
			String[] temp = value.substring(1, value.length() - 1).split(" ");
			for (int i = 0; i < temp.length; i++) {
				if (!temp[i].trim().equals(""))
					allValues.add(temp[i].trim());
			}
		} else
			allValues.add(value);

		// check if multiple values are allowed
		if (allValues.size() > 1 && !isList(attributeText))
			return false;

		List<String> allowedValues = getAllowedValues(attributeText);
		return isAllowed(allowedValues, allValues);
	}

	// public boolean isAllowedObject(String attributeText, String className) {
	// if (!configurations.containsKey(attributeText))
	// return false;
	// String[] allowed =
	// (this.configurations.get(attributeText))[3].split(",");
	// for (int i=0; i<allowed.length; i++) {
	// if (className.endsWith(allowed[i].trim()))
	// return true;
	// }
	// return false;
	// }

	public boolean isAllowedObject(String attributeText, NamedObject object) {
		if (!configurations.containsKey(attributeText))
			return false;
		String[] allowed = (this.configurations.get(attributeText))[3]
				.split(",");
		for (int i = 0; i < allowed.length; i++) {
			Class tmp = null;
			try {
				tmp = Class.forName("de.d3web.kernel.domainModel."
						+ allowed[i].trim());
			} catch (ClassNotFoundException e) {
				try {
					tmp = Class.forName("de.d3web.kernel.domainModel.qasets."
							+ allowed[i].trim());
				} catch (ClassNotFoundException f) {
				}
			}
			// Prï¿½ft, ob tmp von der gleichen Klasse oder einer Oberklasse von
			// object ist
			if (tmp != null && tmp.isAssignableFrom(object.getClass()))
				return true;
		}
		return false;
	}

	public boolean isProperty(String attributeText) {
		if (configurations.containsKey(attributeText))
			return true;
		return false;
	}

	public boolean isMultiple(String attributeText) {
		if (configurations.containsKey(attributeText)
				&& (configurations.get(attributeText))[4]
						.equals("1"))
			return true;
		return false;
	}

	public boolean isList(String attributeText) {
		if (configurations.containsKey(attributeText)
				&& ((String[]) configurations.get(attributeText))[5]
						.equals("1"))
			return true;
		return false;
	}
}
