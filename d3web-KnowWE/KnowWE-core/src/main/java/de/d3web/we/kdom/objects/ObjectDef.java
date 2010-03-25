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

	@SuppressWarnings("unchecked")
	public T getObject(Section<? extends ObjectDef<T>> s) {
		return (T) KnowWEUtils.getStoredObject(s, key);
	}

	public void storeObject(Section<? extends KnowWEObjectType> s, T q) {
		KnowWEUtils.storeSectionInfo(s, key, q);
	}



}
