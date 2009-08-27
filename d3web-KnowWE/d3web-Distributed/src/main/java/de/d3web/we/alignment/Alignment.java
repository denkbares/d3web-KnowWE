package de.d3web.we.alignment;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.terminology.term.TermInfoType;

public abstract class Alignment implements Comparable<Alignment> {

	protected final IdentifiableInstance object;
	protected final AbstractAlignType type;
	
	private Map<String, Boolean> properties = new HashMap<String, Boolean>();
	
	
	public Alignment(IdentifiableInstance object, AbstractAlignType type) {
		super();
		this.object = object;
		this.type = type;
	}
	
	public abstract IdentifiableInstance getAligned(IdentifiableInstance ii);
	
	public Object getAlignedValue(IdentifiableInstance input, IdentifiableInstance output) {
		return getType().getAlignedValue(input, output);
	}
	
	public IdentifiableInstance getObject() {
		return object;
	}
			
	public AbstractAlignType getType() {
		return type;
	}

	public int compareTo(Alignment o) {
		//[TODO]:Peter: implement!
		return 0;
	}

	public void setProperty(String key, Boolean value) {
		properties.put(key, value);
	}
	
	public Boolean getProperty(String key) {
		return properties.get(key);
	}
	
	public Map<String, Boolean> getPropertiesMap() {
		return properties;
	}	
	
	public boolean equals(Object o) {
		if(!(o instanceof Alignment)) return false;
		if(o == this) return true;
		Alignment alignment = (Alignment) o;
		if(!getType().equals(alignment.getType())) return false;
		return alignment.getObject().equals(object);
	}

	public int hashCode() {
		return type.hashCode() 
			+ 37 * object.hashCode() ;
	}
	
	public String toString() {
		return object.toString() + " " + getType().toString();
	}

	
	
}
