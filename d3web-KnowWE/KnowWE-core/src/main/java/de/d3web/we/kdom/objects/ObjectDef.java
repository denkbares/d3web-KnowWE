package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

/**
 * A type representing a text slice which _defines_ an object (class, instance,
 * question, whatever) i.e., there should be some compilation script
 * (ReviseSubtreeHandler) to actually _create_ and store the object.
 *
 * This should NOT be used for object references @see {@link ObjectRef}
 *
 * @author Jochen
 *
 * @param <T>
 */
public abstract class ObjectDef<T> extends DefaultAbstractKnowWEObjectType implements ObjectIDContainer<T> {

	protected String key;

	public ObjectDef(String key) {
		this.key=key;
	}

	/**
	 * Allows quick and simple access to the object defined by the section of
	 * this type IFF was stored when create using storeObject()
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getObject(Section<? extends ObjectDef<T>> s) {
		return (T) KnowWEUtils.getStoredObject(s, key);
	}

	/**
	 * When the actual object is created, it should be stored via this method
	 * This allows quick and simple access to the object via getObject() when
	 * needed for the further compilation process
	 * 
	 * @param s
	 * @param q
	 */
	public void storeObject(Section<? extends KnowWEObjectType> s, T q) {
		KnowWEUtils.storeSectionInfo(s, key, q);
	}



}
