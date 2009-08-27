package de.d3web.we.basic;


public class IdentifiableInstance {

	private String namespace;
	private String objectId;
	private Object value;
	
	public IdentifiableInstance(String namespace, String object, Object value) {
		super();
		if(namespace == null) {
			namespace = "";
		}
		if(objectId == null) {
			objectId = "";
		}
		if(value == null) {
			value = "";
		}
		this.namespace = namespace;
		this.objectId = object;
		this.value = value;
	}

	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof IdentifiableInstance)) return false;
		IdentifiableInstance i = (IdentifiableInstance) o;
		if(equalsNamespaces(i)
				&& (value.equals(i.getValue()) || i.getValue().equals(value))) return true;
		return false;
	}
	
	public boolean equalsNamespaces(IdentifiableInstance i) {
		return namespace.equals(i.getNamespace()) 
			&& objectId.equals(i.getObjectId());
	}
	
	public int hashCode() {
		int valueHash = 0;
		if(value instanceof String) {
			valueHash = value.hashCode();
		}
		return getNamespace().hashCode() 
			+ 37 * getObjectId().hashCode() 
			+ 17 * valueHash;
	}
	
	
	public boolean isValued() {
		return value != null && !value.equals("");
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getObjectId() {
		return objectId;
	}

	public Object getValue() {
		return value;
	}
	
	public String toString() {
		return namespace + " :" + objectId + " -> " + value;
	}

}
