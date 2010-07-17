package de.d3web.we.terminology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.ObjectDef;
import de.d3web.we.kdom.objects.ObjectRef;
import de.d3web.we.kdom.objects.TermReference;

/**
 * @author Jochen
 *
 *         This class manages the definition and usage of terms. A term
 *         represents some kind of object. For each term that is defined in the
 *         wiki (and registered here) it stores the location where it has been
 *         defined. Further, for any reference also the locations are stored.
 *         The service of this manager is, that for a given term the definition
 *         and the references can be asked for. Obviously, this only works if
 *         the terms are registered here.
 *
 *
 *
 */
public class TerminologyManager {

	@SuppressWarnings("unchecked")
	private final Map<String, Map<KnowWETerm, TermReferenceLog>> termsMaps =
			new HashMap<String, Map<KnowWETerm, TermReferenceLog>>();

	// @SuppressWarnings("unchecked")
	// private final Map<String, Map<KnowWETerm, TermReferenceLog>>
	// undefinedTermsMaps =
	// new HashMap<String, Map<KnowWETerm, TermReferenceLog>>();

	private static TerminologyManager instance = null;

	public static TerminologyManager getInstance() {
		if (instance == null) {
			instance = new TerminologyManager();

		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	private Map<KnowWETerm, TermReferenceLog> getTermsMap(String title) {
		Map<KnowWETerm, TermReferenceLog> tmap = termsMaps.get(title);
		if (tmap == null) {
			tmap = new HashMap<KnowWETerm, TermReferenceLog>();
			termsMaps.put(title, tmap);
		}
		return tmap;
	}

	@SuppressWarnings("unchecked")
	private <TermObject> Set<Section<? extends ObjectRef<TermObject>>> getReferringSectionsTo(
			KnowWEArticle article, KnowWETerm t) {

		if (getTermsMap(article.getTitle()).containsKey(t)) {
			return getTermsMap(article.getTitle()).get(t).getReferences();
		}
		else {
			return new HashSet<Section<? extends ObjectRef<TermObject>>>(0);
		}
	}

	@SuppressWarnings("unchecked")
	private <TermObject> TermReferenceLog<TermObject> getTermReferenceLog(KnowWEArticle article,
			Section<? extends TermReference<TermObject>> r) {
		return getTermsMap(article.getTitle()).get(
				new KnowWETerm(r));
	}

	// @SuppressWarnings("unchecked")
	// private <TermObject> TermReferenceLog<TermObject>
	// getUnregisteredTermReferenceLog(KnowWEArticle article,
	// Section<? extends TermReference<TermObject>> r) {
	// return getUndefinedTermsMap(article.getTitle()).get(
	// new KnowWETerm(r.get().getTermName(r)));
	// }

	/**
	 * Allows to register a new term
	 * 
	 * @param r
	 * 
	 * @param <TermObject>
	 */
	public <TermObject> void registerTermDef(KnowWEArticle article, Section<? extends ObjectDef<TermObject>> r) {
		KnowWETerm term = new KnowWETerm(r);
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			if (termRef.objectDefSection != null) {
				// this should not happen, because before creating a new term
				// one should check whether the name is already in use
				Logger.getLogger(this.getClass().getName())
						.log(Level.WARNING, "Tried to register new term that already exists: '" +
								term + "'!");
				// now registration will be ignored
				return;
			}
			else {
				setTermRefsToNotReused(article, r);
			}
		}
		getTermsMap(article.getTitle()).put(term, new TermReferenceLog<TermObject>(r));
	}

	/**
	 * 
	 * 
	 * @param <TermObject>
	 * @param term
	 * @param r
	 */
	public <TermObject> void registerTermRef(KnowWEArticle article, Section<? extends ObjectRef<TermObject>> r) {
		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);
		if (refLog == null) {
			refLog = new TermReferenceLog<TermObject>(null);
			getTermsMap(article.getTitle()).put(new KnowWETerm(r), refLog);
		}
		refLog.addReference(r);
	}

	/**
	 * Returns whether a term exists (i.e., has been defined and registered)
	 * 
	 * @param r
	 * @return
	 */
	public <TermObject> boolean isDefinedTerm(KnowWEArticle article, Section<? extends TermReference<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			return termRef.objectDefSection != null;
		}
		return false;
	}

	public <TermObject> boolean isUndefinedTerm(KnowWEArticle article, Section<? extends TermReference<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			return termRef.objectDefSection == null;
		}
		return false;
	}

	/**
	 * For an object reference the definition is returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public <TermObject> Section<? extends ObjectDef<TermObject>> getTermDefSection(
			KnowWEArticle article, Section<? extends ObjectRef<TermObject>> r) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);

		if (refLog != null) {
			return refLog.objectDefSection;
		}

		return null;
	}

	public <TermObject> Set<Section<? extends ObjectRef<TermObject>>> getTermRefSections(KnowWEArticle article, Section<? extends ObjectDef<TermObject>> r) {
		return getReferringSectionsTo(article, new KnowWETerm(r));
	}

	public <TermObject> void setTermRefsToNotReused(KnowWEArticle article,
			Section<? extends ObjectDef<TermObject>> r) {

		Set<Section<? extends ObjectRef<TermObject>>> refs = TerminologyManager.getInstance()
				.getTermRefSections(article, r);

		for (Section<?> ref : refs) {
			// ref.setReusedBy(article.getTitle(), false);
			Section<?> father = ref;
			while (father != null) {
				father.setReusedBy(article.getTitle(), false);
				father = father.getFather();
			}
		}
	}

	public <TermObject> void unregisterTermDef(KnowWEArticle article, Section<? extends ObjectDef<TermObject>> r) {
		setTermRefsToNotReused(article, r);
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			termRef.objectDefSection = null;
		}
	}

	public <TermObject> void unregisterTermRef(KnowWEArticle article, Section<? extends ObjectRef<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			termRef.objectRefSections.remove(r);
		}
	}

	@SuppressWarnings("unchecked")
	public void removeTermsForArticle(KnowWEArticle article) {
		termsMaps.put(article.getTitle(), new HashMap<KnowWETerm, TermReferenceLog>());
		// undefinedTermsMaps.put(article.getTitle(),
		// new HashMap<KnowWETerm, TermReferenceLog>());

	}

	/**
	 * 
	 * This is an auxiliary data-structure to store the definitions and
	 * references of terms
	 * 
	 * @author Jochen
	 *
	 * @param <TermObject>
	 */
	class TermReferenceLog<TermObject> {

		private Section<? extends ObjectDef<TermObject>> objectDefSection;

		private final Set<Section<? extends ObjectRef<TermObject>>> objectRefSections = new HashSet<Section<? extends ObjectRef<TermObject>>>();

		public TermReferenceLog(Section<? extends ObjectDef<TermObject>> d) {
			this.objectDefSection = d;
		}

		public void addReference(Section<? extends ObjectRef<TermObject>> r) {
			objectRefSections.add(r);
		}

		public Set<Section<? extends ObjectRef<TermObject>>> getReferences() {
			return objectRefSections;
		}

	}


}
