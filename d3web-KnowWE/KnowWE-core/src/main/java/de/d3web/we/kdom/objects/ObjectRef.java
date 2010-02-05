package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;

public abstract class ObjectRef extends DefaultAbstractKnowWEObjectType{
	
	public abstract Object getObject(Section<? extends ObjectRef> s);

}
