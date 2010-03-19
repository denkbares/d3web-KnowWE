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

import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.knowledgeExporter.KnowledgeManager;

public class QClassHierarchyWriter extends TxtKnowledgeWriter {
	
	private List initQASets;
	private Set validQASets;
	private boolean exportQuestionClassHierarchieID;
	
	public QClassHierarchyWriter(KnowledgeManager manager) {
		super(manager);
		validQASets = manager.getQClasses();
	}

	@Override
	public String writeText() {
		StringBuffer text = new StringBuffer();
		QASet set = manager.getKB().getRootQASet();
		initQASets = manager.getKB().getInitQuestions();
		addChildQASets(text, set, 0, new HashSet());
		
		return text.toString();
	}
	

	private void addChildQASets(StringBuffer text, QASet qaSet, int level,
			Set alreadyDone) {
		boolean alreadyWritten = alreadyDone.contains(qaSet);
		alreadyDone.add(qaSet);
		if (!(qaSet instanceof Question) && (validQASets == null || validQASets.contains(qaSet))) {
			if(!qaSet.equals(manager.getKB().getRootQASet())) {
			
			for (int i = 0; i < level; i++) {
				text.append("-");
                if (i == level - 1) {
                    text.append(" ");
                }
			}

			String toAppend = qaSet.getText();
			VerbalizationManager.quoteIfNecessary(toAppend);
			text.append(toAppend);
			
			int index = -1;
			if (initQASets.contains(qaSet)) {
				for (int i = 0; i < initQASets.size(); i++) {
					QASet element = (QASet) initQASets.get(i);
					if (qaSet.equals(element)) {
						index = i;
						break;
					}
				}

			}
			if (index != -1 && !alreadyWritten) {
				text.append(" [" + (index+1) + "]");

			}
			// Einfügen der ID
			if(exportQuestionClassHierarchieID) {
				text.append(" #" + qaSet.getId());
			}
			text.append("\n");
			level++;
			}
			List<QASet> diagList = (List<QASet>) qaSet.getChildren();
			
			
			if (!alreadyWritten) {
				for (Iterator<QASet> iter = diagList.iterator(); iter.hasNext();) {
					QASet element = iter.next();
					addChildQASets(text, element, level, alreadyDone);
				}
			}
			
		}
		
	}


	public boolean isExportQuestionClassHierarchieID() {
		return exportQuestionClassHierarchieID;
	}

	public void setExportQuestionClassHierarchieID(
			boolean exportQuestionClassHierarchieID) {
		this.exportQuestionClassHierarchieID = exportQuestionClassHierarchieID;
	}

	

}
