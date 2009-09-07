package de.d3web.we.kdom.contexts;

public class AnnotationContext extends Context{

	public static final String CID="ANNOTATIONCONTEXT";
	
	public AnnotationContext(){
		super();
	}

	public AnnotationContext(String s){
		super();
		setAnnotationproperty(s);
	}
	
	@Override
	public String getCID() {		
		return CID;				
	}
	
	public void setAnnotationproperty(String prop){
		attributes.put("property", prop);
	}
	
	public String getAnnotationproperty(){
		return attributes.get("property");
	}
	
	
	

}
