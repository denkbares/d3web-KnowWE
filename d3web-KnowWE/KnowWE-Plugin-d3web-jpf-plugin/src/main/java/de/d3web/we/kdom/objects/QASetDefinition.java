package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.we.kdom.IncrementalConstraints;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;


public abstract class QASetDefinition<TermObject extends QASet>
		extends D3webTermDefinition<TermObject>
		implements IncrementalConstraints {

	public QASetDefinition(String key) {
		super(key);
	}

	@Override
	public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
		return false;
	}

}
