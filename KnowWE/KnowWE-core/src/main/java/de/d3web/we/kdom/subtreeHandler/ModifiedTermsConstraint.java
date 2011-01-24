package de.d3web.we.kdom.subtreeHandler;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;


public class ModifiedTermsConstraint<T extends KnowWEObjectType> extends ConstraintModule<T> {

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<T> s) {
		return KnowWEUtils.getTerminologyHandler(article.getWeb()).areTermDefinitionsModifiedFor(
				article);
	}

}
