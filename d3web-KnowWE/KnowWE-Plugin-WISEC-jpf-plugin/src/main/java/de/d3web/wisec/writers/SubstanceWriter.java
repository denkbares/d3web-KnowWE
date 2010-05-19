package de.d3web.wisec.writers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.wisec.model.Substance;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.WISECModel;
import de.d3web.wisec.scoring.DefaultScoringWeightsConfiguration;
import de.d3web.wisec.scoring.ScoringUtils;

public class SubstanceWriter extends WISECWriter {

	public static final String FILE_PRAEFIX = "WI_SUB_";
	private static final int MAXLENGTH = 10;
	private static Map<String, String> filenameMap = new HashMap<String, String>();

	public SubstanceWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (Substance substance : model.getSubstances()) {
			if (model.usesInLists(substance) >= model.SUBSTANCE_OCCURRENCE_THRESHOLD) {
				String filename = getWikiFileNameFor(substance.getName());
				FileWriter writer = new FileWriter(new File(
						this.outputDirectory + filename + ".txt"));
				write(substance, writer);
				writer.close();
			}
		}
	}

	private void write(Substance substance, Writer writer) throws IOException {
		StringBuffer b = new StringBuffer();
		b.append("!!! " + substance.getName() + "\n\n");

		writeUsesOfCAS(substance,b);
		writeCriteriaScoring(substance, b);
		writeOnListsRelation(substance, b);

		writer.append(b.toString());
	}

	private void writeCriteriaScoring(Substance substance, StringBuffer b) {
		String[] criteriaValues = new String[] { "1", "2", "3", "X", "u", "LAW"};
		b.append("!! Criteria Scoring\n\n");

		b.append("|| Criteria || Values ");
		for (String cValue : criteriaValues) {
			b.append(" || " + cValue);
		}
		b.append("|| Score");
		b.append("\n");
		
		printCriteriaForCriteriaGroup(substance, b, 
				criteriaValues, new String[] {"P", "B", "Aqua_Tox", "Multiple_Tox", "EDC", "CMR"});
		
		String HOR_RULER = "| _ | | | | | | | \n";
		b.append(HOR_RULER);
		printCriteriaForCriteriaGroup(substance, b, 
				criteriaValues, new String[] {"LRT", "Climatic_Change"});
		
		b.append(HOR_RULER);
		printCriteriaForCriteriaGroup(substance, b, 
				criteriaValues, new String[] {"Risk_related", "Political", "Exposure"});

		b.append(HOR_RULER);
		printCriteriaForCriteriaGroup(substance, b, 
				criteriaValues, new String[] {"HPV", "Regulated", "SVHC_regulated"});

		b.append("\n");
	}

	private void printCriteriaForCriteriaGroup(Substance substance,
			StringBuffer b, String[] criteriaValues, String[] aGroup) {
		List<SubstanceList> lists = getSubstanceListsWith(substance);
		for (String criteria : aGroup) {
			b.append("| " + criteria + " | ");
			for (SubstanceList substanceList : lists) {
				String value = substanceList.criteria.get(criteria);
				if (value != null && value.length() > 0) {
					b.append(" ["+value+" | " + 
							SubstanceListWriter.getWikiFileNameFor(substanceList.name) +
							"]");
				}
			}
			for (String cValue : criteriaValues) {
				List<SubstanceList> list = model.listsWithCriteriaHavingValue(substance.getName(), criteria, cValue);
				b.append(" | " + list.size());
			}
			b.append(" | " + ScoringUtils.computeScoreFor(model, new DefaultScoringWeightsConfiguration(), substance, criteria));
			b.append("\n");
		}
	}

	private List<SubstanceList> getSubstanceListsWith(Substance substance) {
		List<SubstanceList> resultList = new ArrayList<SubstanceList>();
		for (SubstanceList list : model.getSubstanceLists()) {
			if (list.hasSubstanceWithName(substance.getName())) {
				resultList.add(list);
			}
		}
		return resultList;
	}

	private void writeUsesOfCAS(Substance substance, StringBuffer b) {
		// b.append("!! CAS Uses \n");
		
		// Compute: Which CAS names are used in which lists
		Map<String, List<SubstanceList>> casUses = new HashMap<String, List<SubstanceList>>();
		for (SubstanceList list : model.getSubstanceLists()) {
			if (list.hasSubstanceWithName(substance.getName())) {
				String casName = substance.getCAS();
				List<SubstanceList> listNames = casUses.get(casName);
				if (listNames == null) {
					List<SubstanceList> l = new ArrayList<SubstanceList>();
					l.add(list);
					casUses.put(casName, l);
				}
				else {
					listNames.add(list);
					casUses.put(casName, listNames);
				}
			}
		}
		
		if (casUses.keySet().size() == 1) {
			b.append("* Unique CAS used: " + casUses.keySet().iterator().next() + "\n");
		} else {
			for (String cas : casUses.keySet()) {
				b.append("! " + cas + "\n");
				for (SubstanceList list : casUses.get(cas)) {
					b.append("# " + SubstanceListWriter.asWikiMarkup(list)
							+ "\n");
				}
			}
		}
		b.append("\n");
	}

	private void writeOnListsRelation(Substance substance, StringBuffer b) {
		b.append("!! On Lists \n\n");
		for (SubstanceList list : substance.usesInLists) {
			b.append("* " + SubstanceListWriter.asWikiMarkup(list) + " ("
					+ SubstanceListWriter.getCriteriaString(list) + ")\n");
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

	public static String asWikiMarkup(Substance substance) {
		return "[ " + substance.getName() + " | "
				+ SubstanceWriter.getWikiFileNameFor(substance.getName()) + "]";
	}

	private static String clean(String name) {
		name = name.replace("/", "-");
		name = name.replace(",", "-");
		name = name.replace(".", "-");
		name = name.replace(":", "-");
		return name;
	}

}
