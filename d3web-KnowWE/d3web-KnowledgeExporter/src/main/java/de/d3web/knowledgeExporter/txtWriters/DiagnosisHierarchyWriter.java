package de.d3web.knowledgeExporter.txtWriters;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.knowledgeExporter.KnowledgeManager;

public class DiagnosisHierarchyWriter extends TxtKnowledgeWriter {
	
	private boolean exportDiagnoseHierarchyID;

	public DiagnosisHierarchyWriter(KnowledgeManager manager) {
		super(manager);
	}

	public String writeText() {
		StringBuffer text = new StringBuffer();
		Diagnosis rootDiagnosis = manager.getKB().getRootDiagnosis();

		addChildDiagnoses(text, rootDiagnosis, 0, new HashSet());
		return text.toString();
	}

	private void addChildDiagnoses(StringBuffer text, Diagnosis d, int level,
			Set alreadyDone) {
		boolean alreadyWritten = alreadyDone.contains(d);
		alreadyDone.add(d);
		if (!d.equals(manager.getKB().getRootDiagnosis())
				&& (manager.getDiagnosisList().contains(d))) {
			for (int i = 0; i < level; i++) {
				text.append("-");
                if (i == level - 1) {
                    text.append(" ");
                }
			}
			String toAppend = d.getText();
			toAppend = VerbalizationManager.quoteIfNecessary(toAppend);
			text.append(toAppend);
			// Einfügen der ID
			if(exportDiagnoseHierarchyID) {
				text.append(" #" + d.getId());
			}
			text.append ("\n");
			level++;
		}
		List<Diagnosis> diagList = (List<Diagnosis>) d.getChildren();

		if (!alreadyWritten) {
			for (Iterator<Diagnosis> iter = diagList.iterator(); iter.hasNext();) {
				Diagnosis element = iter.next();

				addChildDiagnoses(text, element, level, alreadyDone);

			}
		}
	}

	public boolean isExportDiagnoseHierarchyID() {
		return exportDiagnoseHierarchyID;
	}

	public void setExportDiagnoseHierarchyID(boolean exportDiagnoseHierarchieID) {
		this.exportDiagnoseHierarchyID = exportDiagnoseHierarchieID;
	}

}
