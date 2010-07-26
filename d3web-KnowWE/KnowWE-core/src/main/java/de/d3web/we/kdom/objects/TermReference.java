package de.d3web.we.kdom.objects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * A type representing a text slice, which _references_ an (existing) Object. It
 * comes along with a ReviseHandler that checks whether the referenced object is
 * existing and throws an error if not.
 * 
 * This should not be used for types _creating_ objects @link
 * {@link TermDefinition}
 * 
 * 
 * @author Jochen, Albrecht
 * 
 * @param <TermObject>
 */
public abstract class TermReference<TermObject>
		extends DefaultAbstractKnowWEObjectType
		implements KnowWETerm<TermObject> {

	/**
	 * Allows quick and simple access to the object this sections is refering
	 * to.
	 */
	public final TermObject getTermObject(KnowWEArticle article, Section<? extends TermReference<TermObject>> s) {
		Section<? extends TermDefinition<TermObject>> objectDefinition = KnowWEUtils.getTerminologyHandler(
				article.getWeb()).getTermDefinitionSection(article, s);
		if (objectDefinition != null) {
			TermObject c = objectDefinition.get().getTermObject(article, objectDefinition);
			if (c != null && c.toString().equals(objectDefinition.get().getTermName(s))) {
				return c;
			}
		}
		return getTermObjectFallback(article, s);
	}

	/**
	 * Fallback method in case the object isn't defined via an ObjectDef
	 * respectively in the TerminologyManager.
	 */
	public TermObject getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<TermObject>> s) {
		return null;
	}

	public TermReference() {
		this.addSubtreeHandler(new TermRegistration());
	}

	class TermRegistration extends SubtreeHandler<TermReference<TermObject>> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TermReference<TermObject>> s) {

			KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermReference(article, s);

			if (!KnowWEUtils.getTerminologyHandler(article.getWeb()).isDefinedTerm(article, s)
					&& s.get().getTermObjectFallback(article, s) == null) {

				return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(s.get().getName()
						+ ": " + s.get().getTermName(s)));
			}

			return new ArrayList<KDOMReportMessage>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<TermReference<TermObject>> s) {
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermReference(article, s);
		}

	}

}
