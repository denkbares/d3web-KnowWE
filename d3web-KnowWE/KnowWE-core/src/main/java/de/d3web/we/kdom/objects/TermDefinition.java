package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

/**
 * A type representing a text slice which _defines_ an object (class, instance,
 * question, whatever) i.e., there should be some compilation script
 * (ReviseSubtreeHandler) to actually _create_ and store the object.
 *
 * This should NOT be used for object references @see {@link TermReference}
 *
 * @author Jochen
 *
 * @param <TermObject>
 */
public abstract class TermDefinition<TermObject>
		extends DefaultAbstractKnowWEObjectType
		implements KnowWETerm<TermObject> {

	protected String key;

	public TermDefinition(String key) {
		this.key=key;
		// this.addSubtreeHandler(Priority.HIGHEST, new NewTermRegistration());
	}

	/**
	 * Allows quick and simple access to the object defined by the section of
	 * this type IFF was stored when create using storeObject()
	 * @param article TODO
	 * @param s
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TermObject getTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		return (TermObject) KnowWEUtils.getStoredObject(article, s, key);
	}

	/**
	 * Allows quick and simple access to the object defined by the section of
	 * this type IFF was stored when create using storeObject()
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TermObject getTermObjectFromLastVersion(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s) {
		return (TermObject) KnowWEUtils.getObjectFromLastVersion(article, s, key);
	}

	/**
	 * When the actual object is created, it should be stored via this method
	 * This allows quick and simple access to the object via getObject() when
	 * needed for the further compilation process
	 * @param article TODO
	 * @param s
	 * @param q
	 */
	public void storeTermObject(KnowWEArticle article, Section<? extends TermDefinition<TermObject>> s, TermObject q) {
		KnowWEUtils.storeSectionInfo(article, s, key, q);
	}

	// class NewTermRegistration extends SubtreeHandler<ObjectDef<TermObject>> {
	//	
	// @Override
	// public Collection<KDOMReportMessage> create(KnowWEArticle article,
	// Section<ObjectDef<TermObject>> s) {
	//	
	// TerminologyManager.getInstance().registerTermDef(article, s);
	//	
	// return null;
	// }
	//	
	// @Override
	// public void destroy(KnowWEArticle article, Section<ObjectDef<TermObject>>
	// s) {
	//	
	// TerminologyManager.getInstance().unregisterTermDef(article, s);
	// }
	//	
	// }

}
