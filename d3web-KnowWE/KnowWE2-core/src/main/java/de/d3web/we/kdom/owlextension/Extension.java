package de.d3web.we.kdom.owlextension;

import java.util.HashMap;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;

public class Extension extends DefaultAbstractKnowWEObjectType{
	private HashMap<Section, String> extensiontexts;
	private HashMap<Section, ExtensionObject> extensionmap;

	public Extension(){
		super();
		extensiontexts=new HashMap<Section, String>();
		extensionmap=new HashMap<Section, ExtensionObject>();
	}
	
	public void addExtensionSource(Section s,String src) {
		extensiontexts.put(s, src);		
	}
	
	public void setExtensionObject (Section s, ExtensionObject e){
		extensionmap.put(s, e);
	}
	
	public String getExtensionSource(Section s){
		return extensiontexts.get(s);
	}
	
	public ExtensionObject getExtensionObject(Section sec) {		
		return extensionmap.get(sec);
	}
		

	/* (non-Javadoc)
	 * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
	 */
	@Override
	protected void init() {
	   this.setCustomRenderer(ExtensionRenderer.getInstance());
	   this.sectionFinder=(new ExtensionSectionFinder(this));	    
	}
}
