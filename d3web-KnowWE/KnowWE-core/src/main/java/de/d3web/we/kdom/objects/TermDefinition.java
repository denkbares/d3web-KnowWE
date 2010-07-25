package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

/**
 * A type representing a text slice which _defines_ an object (class, instance,
 * question, whatever) i.e., there should be some compilation script
 * (SubtreeHandler) to actually _create_ and store the object.
 * 
 * This should NOT be used for object references @see {@link TermReference}
 * 
 * @author Jochen, Albrecht
 * 
 * @param <TermObject>
 */
public abstract class TermDefinition<TermObject>
		extends DefaultAbstractKnowWEObjectType
		implements KnowWETerm<TermObject> {

	protected String key;

	public TermDefinition(String key) {
		this.key=key;
	}

	/**
	 * Allows quick and simple access to the object defined by this section, if
	 * it was stored using storeObject()
	 */
	@SuppressWarnings("unchecked")
	public TermObject getTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		// in case the of duplicate definitions, get the one that has actually
		// created the TermObject
		Section<?> s2 = KnowWEUtils.getTerminologyHandler(article.getWeb()).getTermDefinitionSection(
				article, s);
		return (TermObject) KnowWEUtils.getStoredObject(article, s2 != null ? s2 : s, key);
	}

	/**
	 * If a Section is not reused in the current KDOM, its stored object will
	 * not be found in the current SectionStore (unlike the stored object of
	 * reused Sections). It will however still reside in the last SectionStore,
	 * so you can use this method to sill get it from there, e.g. to destroy it
	 * in the method destroy in the SubtreeHandler.
	 */
	@SuppressWarnings("unchecked")
	public TermObject getTermObjectFromLastVersion(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		return (TermObject) KnowWEUtils.getObjectFromLastVersion(article, s, key);
	}

	/**
	 * When the actual object is created, it should be stored via this method
	 * This allows quick and simple access to the object via getObject() when
	 * needed for the further compilation process
	 */
	public void storeTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s, TermObject q) {
		KnowWEUtils.storeSectionInfo(article, s, key, q);
	}


}
