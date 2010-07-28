package de.d3web.we.terminology;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.NotUniqueKnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;

/**
 * @author Jochen, Albrecht
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
public class TerminologyHandler implements KnowledgeRepresentationHandler {

	public final static String HANDLER_KEY = TerminologyHandler.class.getSimpleName();

	private final Map<String, Boolean> modifiedTermDefinitions = new HashMap<String, Boolean>();

	@SuppressWarnings("unused")
	private String web;

	@SuppressWarnings("unchecked")
	private final Map<String, Map<TermIdentifier, TermReferenceLog>> termReferenceLogsMaps =
			new HashMap<String, Map<TermIdentifier, TermReferenceLog>>();

	// private static TerminologyHandler instance = null;
	//
	// public static TerminologyHandler getInstance() {
	// if (instance == null) {
	// instance = new TerminologyHandler();
	//
	// }
	//
	// return instance;
	// }

	/**
	 * <b>This constructor SHOULD NOT BE USED!</b>
	 * <p/>
	 * Use KnowWEUtils.getTerminologyHandler(String web) instead!
	 */
	public TerminologyHandler(String web) {
		this.web = web;
	}

	public TerminologyHandler() {
		this.web = KnowWEEnvironment.DEFAULT_WEB;
	}

	@SuppressWarnings("unchecked")
	private Map<TermIdentifier, TermReferenceLog> getTermReferenceLogsMap(String title) {
		Map<TermIdentifier, TermReferenceLog> tmap = termReferenceLogsMaps.get(title);
		if (tmap == null) {
			tmap = new HashMap<TermIdentifier, TermReferenceLog>();
			termReferenceLogsMaps.put(title, tmap);
		}
		return tmap;
	}

	@SuppressWarnings("unchecked")
	private <TermObject> TermReferenceLog<TermObject> getTermReferenceLog(KnowWEArticle article,
			Section<? extends KnowWETerm<TermObject>> r) {
		return getTermReferenceLogsMap(article.getTitle()).get(
				new TermIdentifier(article, r));
	}

	@SuppressWarnings("unchecked")
	private void removeTermReferenceLogsForArticle(KnowWEArticle article) {
		termReferenceLogsMaps.put(article.getTitle(), new HashMap<TermIdentifier, TermReferenceLog>());
	}

	public void initArticle(KnowWEArticle article) {
		if (article.isFullParse()) {
			removeTermReferenceLogsForArticle(article);
		}
	}

	public void finishArticle(KnowWEArticle article) {
		modifiedTermDefinitions.put(article.getTitle(), false);
	}

	/**
	 * Allows to register a new term
	 * 
	 * @param r
	 * 
	 * @param <TermObject>
	 */
	public <TermObject> void registerTermDefinition(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> r) {
		TermIdentifier termName = new TermIdentifier(article, r);
		TermReferenceLog<TermObject> termRefLog = getTermReferenceLog(article, r);
		if (termRefLog != null) {
			if (termRefLog.termDefiningSection != null) {
				// this should not happen, because before creating a new term
				// one should check whether the name is already in use
				Logger.getLogger(this.getClass().getName())
						.log(Level.WARNING, "Tried to register new term that already exists: '" +
								termName + "'!");
				// now registration will be ignored
				return;
			}
			else {
				setTermReferencesToNotReused(article, r);
			}
		}
		getTermReferenceLogsMap(article.getTitle()).put(termName, new TermReferenceLog<TermObject>(r));
		modifiedTermDefinitions.put(article.getTitle(), true);
	}

	public <TermObject> void registerTermReference(KnowWEArticle article, Section<? extends TermReference<TermObject>> r) {
		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);
		if (refLog == null) {
			refLog = new TermReferenceLog<TermObject>(null);
			getTermReferenceLogsMap(article.getTitle()).put(new TermIdentifier(article, r), refLog);
		}
		refLog.addTermReference(r);
	}

	/**
	 * Returns whether a term is defined through an TermDefinition
	 */
	public <TermObject> boolean isDefinedTerm(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			return termRef.termDefiningSection != null;
		}
		return false;
	}

	/**
	 * Returns whether there are TermReferences for this Term, but no
	 * TermDefinition
	 */
	public <TermObject> boolean isUndefinedTerm(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			return termRef.termDefiningSection == null;
		}
		return false;
	}

	/**
	 * For a TermReference the TermDefinition is returned.
	 * 
	 * @param <TermObject>
	 * @param s
	 * @return
	 */
	public <TermObject> Section<? extends TermDefinition<TermObject>> getTermDefinitionSection(
			KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {

		TermReferenceLog<TermObject> refLog = getTermReferenceLog(article, r);

		if (refLog != null) {
			return refLog.termDefiningSection;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public <TermObject> Set<Section<? extends TermReference<TermObject>>> getTermReferenceSections(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> r) {
		TermIdentifier t = new TermIdentifier(article, r);
		if (getTermReferenceLogsMap(article.getTitle()).containsKey(t)) {
			return getTermReferenceLogsMap(article.getTitle()).get(t).getReferences();
		}
		else {
			return new HashSet<Section<? extends TermReference<TermObject>>>(0);
		}
	}

	public <TermObject> void setTermReferencesToNotReused(KnowWEArticle article,
			Section<? extends TermDefinition<TermObject>> r) {

		Set<Section<? extends TermReference<TermObject>>> refs = getTermReferenceSections(
				article, r);

		for (Section<?> ref : refs) {
			ref.setReusedBy(article.getTitle(), false);
			// Section<?> father = ref;
			// while (father != null) {
			// father.setReusedBy(article.getTitle(), false);
			// father = father.getFather();
			// }
		}
	}

	public <TermObject> void unregisterTermDefinition(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> r) {
		setTermReferencesToNotReused(article, r);
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			termRef.termDefiningSection = null;
		}
		modifiedTermDefinitions.put(article.getTitle(), true);
	}

	public <TermObject> void unregisterTermReference(KnowWEArticle article, Section<? extends TermReference<TermObject>> r) {
		TermReferenceLog<TermObject> termRef = getTermReferenceLog(article, r);
		if (termRef != null) {
			termRef.termReferingSections.remove(r);
		}
	}

	public boolean areTermDefinitionsModifiedFor(KnowWEArticle article) {
		if (!modifiedTermDefinitions.containsKey(article.getTitle())) {
			return false;
		}
		return modifiedTermDefinitions.get(article.getTitle());
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return TerminologyHandler.HANDLER_KEY;
	}

	@Override
	public URL saveKnowledge(String title) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeb(String web) {
		this.web = web;
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

		private Section<? extends TermDefinition<TermObject>> termDefiningSection;

		private final Set<Section<? extends TermReference<TermObject>>> termReferingSections = new HashSet<Section<? extends TermReference<TermObject>>>();

		public TermReferenceLog(Section<? extends TermDefinition<TermObject>> d) {
			this.termDefiningSection = d;
		}

		public void addTermReference(Section<? extends TermReference<TermObject>> r) {
			termReferingSections.add(r);
		}

		public Set<Section<? extends TermReference<TermObject>>> getReferences() {
			return termReferingSections;
		}

	}

	private class TermIdentifier {

		private final String termIdentifier;

		@SuppressWarnings("unchecked")
		public <TermObject> TermIdentifier(KnowWEArticle article, Section<? extends KnowWETerm<TermObject>> s) {

			String termObjectClass = "UnidentifiedTerm";
			Class<?> tempClass = s.get().getClass();
			while (tempClass.getSuperclass() != null
				&& !tempClass.getGenericSuperclass().toString().endsWith(">")) {
				tempClass = tempClass.getSuperclass();
			}
			if (tempClass.getGenericSuperclass() != null) {
				String fullGenericClassName = tempClass.getGenericSuperclass().toString();
				int start = fullGenericClassName.indexOf("<");
				int stop = fullGenericClassName.lastIndexOf(">");
				if (start > 0 && stop > 0) termObjectClass = fullGenericClassName.substring(start + 1, stop);
			}

			String tempTermpIdentifier;
			if (s.get() instanceof NotUniqueKnowWETerm) {
				Section<? extends NotUniqueKnowWETerm> nus = (Section<? extends NotUniqueKnowWETerm>) s;
				tempTermpIdentifier = nus.get().getUniqueTermIdentifier(article, nus);
			}
			else {
				tempTermpIdentifier = s.get().getTermName(s);
			}
			termIdentifier = termObjectClass + " " + tempTermpIdentifier;

			// System.out.println(termIdentifier);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((termIdentifier == null) ? 0 : termIdentifier.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TermIdentifier other = (TermIdentifier) obj;
			if (termIdentifier == null) {
				if (other.termIdentifier != null) return false;
			}
			else if (!termIdentifier.equals(other.termIdentifier)) return false;
			return true;
		}

		@Override
		public String toString() {
			return termIdentifier;
		}

	}


}
