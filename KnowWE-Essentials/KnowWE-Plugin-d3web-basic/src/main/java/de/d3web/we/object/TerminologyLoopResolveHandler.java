package de.d3web.we.object;

import java.util.Collection;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class TerminologyLoopResolveHandler<TermObject extends TerminologyObject> implements D3webHandler<D3webTermDefinition<TermObject>> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<D3webTermDefinition<TermObject>> section) {

		Object object = section.getObject(compiler,
				TerminologyLoopDetectionHandler.LOOP_DETECTED);
		if (TerminologyLoopDetectionHandler.REMOVE_PARENTS.equals(object)) {
			TermObject termObject = section.get().getTermObject(compiler, section);
			if (termObject == null) return Messages.noMessage();
			TerminologyObject[] parents = termObject.getParents();
			for (TerminologyObject parent : parents) {
				if (parent instanceof QASet) {
					((QASet) parent).removeChild((QASet) termObject);
					termObject.getKnowledgeBase().getRootQASet().addChild((QASet) termObject);
				}
				else if (parent instanceof Solution) {
					((Solution) parent).removeChild((Solution) termObject);
					termObject.getKnowledgeBase().getRootSolution().addChild((Solution) termObject);
				}
			}
		}
		return Messages.noMessage();
	}

}
