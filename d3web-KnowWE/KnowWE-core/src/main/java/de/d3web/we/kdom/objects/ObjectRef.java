package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public abstract class ObjectRef<T> extends DefaultAbstractKnowWEObjectType{
	
	protected String key;
	
	public ObjectRef(String key) {
		this.key=key;
	}
	
	@SuppressWarnings("unchecked")
	public T getObject(Section<? extends ObjectRef<T>> s) {
		return (T) KnowWEUtils.getStoredObject(s, key);
	}
	
	public void storeObject(Section<? extends KnowWEObjectType> s, T q) {
		KnowWEUtils.storeSectionInfo(s, key, q);
	}
	
	public abstract String getID(Section<? extends ObjectRef<T>> s);

}
