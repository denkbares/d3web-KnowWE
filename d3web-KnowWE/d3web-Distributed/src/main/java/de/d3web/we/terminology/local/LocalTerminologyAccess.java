package de.d3web.we.terminology.local;

import java.util.Collection;

public interface LocalTerminologyAccess<E> {

	public Class getContext();
	
	public E getObject(String objectId, String valueId);
	
	public LocalTerminologyHandler<E, E> getHandler(Collection<Class> include, Collection<Class> exclude); 
	
	public LocalTerminologyHandler<E, E> getHandler(); 
	
}
