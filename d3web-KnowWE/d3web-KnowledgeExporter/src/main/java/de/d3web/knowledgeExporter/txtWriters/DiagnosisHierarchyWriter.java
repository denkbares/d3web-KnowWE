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

package de.d3web.knowledgeExporter.txtWriters;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.knowledgeExporter.KnowledgeManager;

public class DiagnosisHierarchyWriter extends TxtKnowledgeWriter {
	
	private boolean exportDiagnoseHierarchyID;

	public DiagnosisHierarchyWriter(KnowledgeManager manager) {
		super(manager);
	}

	@Override
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
			String toAppend = d.getName();
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
