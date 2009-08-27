package de.d3web.we.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import de.d3web.utilities.ISetMap;




public class Information implements Comparable<Information>{

	private final String namespace;
	private final String objectID;
	private final List values;
	private final TerminologyType termType;
	private final InformationType infoType;
	private Date creationDate;
	
	
	public Information(String namespaceID, String objectID, List values, TerminologyType termType, InformationType infoType) {
		super();
		creationDate = new Date();
		if(namespaceID == null) {
			namespaceID = "";
		}
		if(objectID == null) {
			objectID = "";
		}
		if(values == null) {
			values = new ArrayList();
		}
		this.namespace = namespaceID;
		this.objectID = objectID;
		this.values = values;
		this.termType = termType;
		this.infoType = infoType;
	}
	
	
	public String getNamespace() {
		return namespace;
	}

	public String getObjectID() {
		return objectID;
	}

	public List getValues() {
		return values;
	}
	
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Information)) return false;
		Information i = (Information) o;
		if(equalsNamespaces(i)
				&& equalsTypes(i)
				&& values.equals(i.getValues())) return true;
		return false;
	}
	
	public boolean equalsNamespaces(Information i) {
		return namespace.equals(i.getNamespace()) 
			&& objectID.equals(i.getObjectID());
	}
	
	public boolean equalsTypes(Information i) {
		return termType.equals(i.getTerminologyType()) 
			&& infoType.equals(i.getInformationType());
	}
	
	public int hashCode() {
		return getNamespace().hashCode() 
			+ 37 * getObjectID().hashCode() 
			+ 17 * getValues().hashCode();
	}
	
	public String toString() {
		return namespace + ":" + objectID + "->" + values + " (" + termType + ", " + infoType + ")";
	}


	public TerminologyType getTerminologyType() {
		return termType;
	}
	
	public InformationType getInformationType() {
		return infoType;
	}
	
	public IdentifiableInstance getIdentifiableObjectInstance() {
		return new IdentifiableInstance(getNamespace(), getObjectID(), null);
	}
	
	public Collection<IdentifiableInstance> getIdentifiableValueInstances() {
		Collection<IdentifiableInstance> vals = new ArrayList<IdentifiableInstance>();
		for (Object infoVal : getValues()) {
			vals.add(new IdentifiableInstance(getNamespace(),getObjectID(), infoVal));
		}	
		return vals;
	}	

	public static Collection<Information> toInformation(ISetMap<IdentifiableInstance, IdentifiableInstance> map, Information origin, InformationType infoType) {
		Collection<Information> result = new ArrayList<Information>();
		for (IdentifiableInstance iio : map.keySet()) {
			List values = getValues(iio, map.get(iio));
			Information info = new Information(iio.getNamespace(), iio.getObjectId(), values, origin.getTerminologyType(), infoType);
			info.setCreationDate(origin.getCreationDate());
			result.add(info);
		}
		return result;
	}


	private static List<Object> getValues(IdentifiableInstance iio, Set<IdentifiableInstance> iivs) {
		List<Object> result = new ArrayList<Object>();
		for (IdentifiableInstance iiv : iivs) {
			if(iiv.getNamespace().equals(iio.getNamespace()) && iiv.getObjectId().equals(iio.getObjectId())) {
				result.add(iiv.getValue());
			}
		}
		return result;
	}


	public int compareTo(Information o) {
		return o.getCreationDate().compareTo(getCreationDate());
	}


	public Date getCreationDate() {
		return creationDate;
	}


	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}


	
	
}
