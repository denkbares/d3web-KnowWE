package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.scoring.DefaultScoringWeightsConfiguration;
import de.d3web.wisec.scoring.ScoringUtils;

public class SubstanceInfoWriter extends WISECWriter {

	public static final String FILE_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "SUB_";
	private static final int MAXLENGTH = 10;
	private static Map<String, String> filenameMap = new HashMap<String, String>();

	public SubstanceInfoWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (String substanceName : model.activeSubstances) {
			// for (Substance substance : model.getSubstances()) {
			String filename = getWikiFileNameFor(substanceName);
			Writer writer = ConverterUtils.createWriter(this.outputDirectory + filename + ".txt");
			String substance = findSubstanceWithName(substanceName, model);
			if (substance != null) write(substance, writer);
			writer.close();
		}
	}

	private String findSubstanceWithName(String substanceName, WISECModel model) {
		for (String substance : model.substances) {
			if (substance.equalsIgnoreCase(substanceName)) {
				return substance;
			}
		}
		return null;
	}


	private void write(String substance, Writer writer) throws IOException {
		StringBuffer b = new StringBuffer();

		// Map<String, List<SubstanceList>> differentNames =
		// computeUsesOfAttribute(substance,
		// "Chemical");
		// if (differentNames.keySet().size() == 1) {
		// substance = differentNames.keySet().toArray()[0].toString();
		// }

		b.append("!!! Substance Info: " + substance + "\n\n");
		// if (differentNames.keySet().size() > 1) {
		// for (String key : differentNames.keySet()) {
		// b.append(key + "\n");
		// }
		// b.append("\n");
		// }


		writeCASReferences(substance, b);
		writeCriteriaScoring(substance, b);
		writeOnListsRelation(substance, b);

		writer.append(b.toString());
	}

	private void writeCriteriaScoring(String substance, StringBuffer b) {
		String[] criteriaValues = new String[] {
				"1", "2", "3", "X", "u", "LAW" };
		b.append("!! Criteria Scoring\n\n");

		b.append("|| Criteria || Values ");
		for (String cValue : criteriaValues) {
			b.append(" || " + cValue);
		}
		b.append("|| Score");
		b.append("\n");

		printCriteriaForCriteriaGroup(substance, b,
				criteriaValues, new String[] {
						"P", "B", "Aqua_Tox", "Multiple_Tox", "EDC", "CMR" });

		String HOR_RULER = "| _ | | | | | | | \n";
		b.append(HOR_RULER);
		printCriteriaForCriteriaGroup(substance, b,
				criteriaValues, new String[] {
						"LRT", "Climatic_Change" });

		b.append(HOR_RULER);
		printCriteriaForCriteriaGroup(substance, b,
				criteriaValues, new String[] {
						"Risk_related", "Political", "Exposure" });

		b.append(HOR_RULER);
		printCriteriaForCriteriaGroup(substance, b,
				criteriaValues, new String[] {
						"HPV", "Regulated", "SVHC_regulated" });

		b.append("\n");
	}

	private void printCriteriaForCriteriaGroup(String substance,
			StringBuffer b, String[] criteriaValues, String[] aGroup) {
		Collection<SubstanceList> lists = model.getSubstanceListsContaining(substance);
		for (String criteria : aGroup) {
			b.append("| " + criteria + " | ");
			for (SubstanceList substanceList : lists) {
				String value = substanceList.criteria.get(criteria);
				if (value != null && value.length() > 0) {
					b.append(" [" + value + " | " +
							SubstanceListWriter.getWikiFileNameFor(substanceList.getId()) +
							"]");
				}
			}
			for (String cValue : criteriaValues) {
				List<SubstanceList> list = model.listsWithCriteriaHavingValue(substance,
						criteria, cValue);
				b.append(" | " + list.size());
			}

			double score = ScoringUtils.computeScoreFor(model,
					new DefaultScoringWeightsConfiguration(), substance, criteria);
			b.append(" | " + ScoringUtils.prettyPrint(score));
			b.append("\n");
		}
	}

	private void writeCASReferences(String substance, StringBuffer b) {
		b.append("* Chemical names: \n"
				+ WISECExcelConverter.asBulletList(model.getChemNamesFor(substance), 2) + " \n");
		b.append("* EC_No: "
				+ WISECExcelConverter.asString(model.getECNamesFor(substance)) + " \n");
		b.append("* IUPAC:  "
				+ WISECExcelConverter.asString(model.getIUPACFor(substance)) + " \n");
		b.append("\n");
	}

	private void writeOnListsRelation(String substance, StringBuffer b) {
		b.append("!! On Lists \n\n");
		for (SubstanceList list : model.substanceLists) {
			if (list.contains(substance)) {
				b.append("* " + SubstanceListWriter.asWikiMarkup(list) + " ("
						+ SubstanceListWriter.getCriteriaString(list) + ")\n");
			}
		}
	}

	public static String getWikiFileNameFor(String name) {
		String realName = name;
		if (name.length() > MAXLENGTH) {
			String choppedName = name.substring(0, MAXLENGTH);
			realName = filenameMap.get(name);
			if (realName == null) {
				realName = clean(choppedName);
				filenameMap.put(name, realName);
			}
		}
		return FILE_PRAEFIX + clean(realName);
	}

	public static String asWikiMarkup(String substanceName) {
		return "[ " + clean(substanceName) + " | "
				+ SubstanceInfoWriter.getWikiFileNameFor(substanceName) + "]";
	}

	private static String clean(String name) {
		name = name.replace("/", "-");
		name = name.replace(",", "-");
		name = name.replace(".", "-");
		name = name.replace(":", "-");
		name = ConverterUtils.clean(name);
		return name;
	}

}
