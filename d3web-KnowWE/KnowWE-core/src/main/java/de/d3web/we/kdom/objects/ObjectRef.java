package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.NoSuchObjectError;

/**
 * A type representing a text slice, which _references_ an (existing) Object. It
 * comes along with a ReviseHandler that checks whether the referenced object is
 * existing and throws an error if not.
 *
 * This should not be used for types _creating_ objects @link {@link ObjectDef}
 * 
 *
 * @author Jochen
 * 
 * @param <T>
 */
public abstract class ObjectRef<T> extends DefaultAbstractKnowWEObjectType implements ObjectIDContainer<T> {

	public abstract boolean objectExisting(Section<? extends ObjectRef<T>> s);

	public ObjectRef() {
		this.addReviseSubtreeHandler(new ObjectChecker());
	}


	class ObjectChecker implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			if (!objectExisting(s)) {
				KDOMReportMessage.storeError(s,
						new NoSuchObjectError(s.get().getName() + ": "
						+ s.getOriginalText()));
			}
			return null;
		}

	}
}
