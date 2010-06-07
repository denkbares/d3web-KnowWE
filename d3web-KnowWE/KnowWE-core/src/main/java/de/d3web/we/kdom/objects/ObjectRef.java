package de.d3web.we.kdom.objects;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.terminology.KnowWETerm;
import de.d3web.we.terminology.TerminologyManager;

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
public abstract class ObjectRef<T> extends DefaultAbstractKnowWEObjectType implements TermReference<T> {

	/**
	 * has to check whether the referenced object is existing, i.e., has been
	 * created before (by a corresponding ObjectDef)
	 *
	 * @param s
	 * @return
	 */
	public abstract boolean objectExisting(Section<? extends ObjectRef<T>> s);

	public abstract T getObject(Section<? extends ObjectRef<T>> s);

	public ObjectRef() {
		// TODO make ObjectChecker singleton (somehow)
		this.addSubtreeHandler(new ObjectChecker());
		this.addSubtreeHandler(new TermUseRegistration());
	}


	class TermUseRegistration extends SubtreeHandler<ObjectRef<T>> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ObjectRef<T>> s) {

			KnowWETerm term = TerminologyManager.getInstance().getTerm(s);
			if (term != null) {
				TerminologyManager.getInstance().registerTermUse(term, s);
			}

			return new ArrayList<KDOMReportMessage>();
		}

	}

	class ObjectChecker extends SubtreeHandler<ObjectRef<T>> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ObjectRef<T>> s) {
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			if (!objectExisting(s)) {
				msgs.add(new NoSuchObjectError(s.get().getName() + ": "
								+ s.getOriginalText()));
			}
			return msgs;
		}

	}
}
