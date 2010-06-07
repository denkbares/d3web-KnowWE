package de.d3web.we.terminology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private final Map<KnowWETerm, TermReferenceLog> termMap = new HashMap<KnowWETerm, TermReferenceLog>();

	private static TerminologyManager instance = null;

	public static TerminologyManager getInstance() {
		if (instance == null) {
			instance = new TerminologyManager();

		}

		return instance;
	}

	/**
	 * For an object reference the definition is returned.
	 *
	 * @param <T>
	 * @param s
	 * @return
	 */
	public <T> Section<? extends ObjectDef<T>> getObjectDefinition(Section<? extends ObjectRef<T>> s) {
		String termName = s.get().getTermName(s);
		TermReferenceLog<T> existingTermDefinition = termMap.get(new
				KnowWETerm(termName));
		if (existingTermDefinition != null) {

			return existingTermDefinition.definition;
		}

		return null;
	}

	/**
	 *
	 *
	 * @param <T>
	 * @param term
	 * @param r
	 */
	public <T> void registerTermUse(KnowWETerm term, Section<? extends ObjectRef<T>> r) {
		TermReferenceLog<T> existingTermDefinition = termMap.get(term);
		if (existingTermDefinition != null) {
			existingTermDefinition.addReference(r);
		}

	}

	/**
	 * Returns whether a term exists (i.e., has been defined and registered)
	 *
	 * @param r
	 * @return
	 */
	public boolean termExists(Section<? extends TermReference> r) {
		return getTerm(r) != null;
	}

	/**
	 * returns the term for a TermReference
	 *
	 * @param r
	 * @return
	 */
	public KnowWETerm getTerm(Section<? extends TermReference> r) {
		KnowWETerm t = new KnowWETerm(r.get().getTermName(r));

		if (termMap.get(t) != null) {
			return t;
		}
		return null;
	}

	/**
	 * Allows to register a new term
	 *
	 * @param <T>
	 * @param term
	 * @param s
	 */
	public <T> void registerNewTerm(String term, Section<? extends ObjectDef<T>> s) {
		TermReferenceLog existingTermDefinition = termMap.get(term);
		if (existingTermDefinition != null) {
			// this should not happen, because before creating a new term
			// one should check whether the name is already in use
			Logger.getLogger(this.getClass().getName())
					.log(Level.WARNING, "register new term which is already existing: '" +
							term + "'!");
			// now registration will be ignored
			return;
		}

		KnowWETerm t = new KnowWETerm(term);
		termMap.put(t, new TermReferenceLog<T>(t, s));

	}

	/**
	 * 
	 * This is an auxiliary data-structure to store the definitions and
	 * references of terms
	 * 
	 * @author Jochen
	 *
	 * @param <T>
	 */
	class TermReferenceLog<T> {

		private final KnowWETerm term;

		private final Section<? extends ObjectDef<T>> definition;

		private final Set<Section<? extends ObjectRef<?>>> objectRefSectionIDs = new HashSet<Section<? extends ObjectRef<?>>>();

		public TermReferenceLog(KnowWETerm t, Section<? extends ObjectDef<T>> d) {
			this.definition = d;
			this.term = t;
		}

		public void addReference(Section<? extends ObjectRef<T>> ref) {
			objectRefSectionIDs.add(ref);
		}

	}

}
