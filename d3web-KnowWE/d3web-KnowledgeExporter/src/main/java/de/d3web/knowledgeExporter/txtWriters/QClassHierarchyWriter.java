package de.d3web.knowledgeExporter.txtWriters;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.qasets.Question;
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
