/*
 * Created on 23.06.2005
 *
 */
package de.d3web.textParser.decisionTable;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;
/**
 * @author Andreas Klar
 */
public abstract class KnowledgeGenerator {

	protected KnowledgeBaseManagement kbm;
	protected Report report;
	protected boolean syntaxCheckOnly = false;
	
	public void setSyntaxCheckOnly(boolean syntaxCheckOnly) {
		this.syntaxCheckOnly = syntaxCheckOnly;
	}

	public KnowledgeGenerator(KnowledgeBaseManagement kbm) {
		this.kbm = kbm;
		this.report = new Report();
	}
	
    public abstract Report generateKnowledge(DecisionTable table);
    
}
