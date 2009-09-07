package de.d3web.we.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public abstract class DefaultKnowWEModule extends AbstractXMLObjectType implements KnowWEModule {
	
	public DefaultKnowWEModule(String type) {
		super(type);
		
	}
	
	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
		rootTypes.add(this);
		return rootTypes;
	}

	
	@Override
	public Collection<Section> getAllSectionsOfType() {
		//TODO override in all existing types...
			return null;
	}
	
	@Override
	public String getName() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

		
	@Override
	public void initModule(ServletContext context) {
		//does nothing as default
	}
	

	
	@Override
	public void reviseSubtree(Section section, KnowledgeRepresentationManager kbm, String web, KnowWEDomParseReport rep) {
		//does nothing as default
	}

//	@Override
//	public KnowWEParseResult modifyAndInsert(String topic, String web,
//			String text, KnowledgeBaseManagement kbm) {
//		return new KnowWEParseResult(new Report(),topic,text);
//	}
	
	public void onSave(String topic) {
		//do nothing
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

//	@Override
//	public String preCacheModifications(String text, KnowledgeBase kb,
//			String topicname) {
//		return text;
//	}



	
	

}
