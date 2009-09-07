package de.d3web.we.module;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.taghandler.TagHandler;

public abstract class AbstractDefaultKnowWEModule implements KnowWEModule{
		
	@Override
	public void addAction(
			Map<Class<? extends KnowWEAction>, KnowWEAction> map) {
		
	}

	@Override
	public void findTypeInstances(Class clazz, List<KnowWEObjectType> instances) {
		
	}
	

	@Override
	public abstract List<KnowWEObjectType> getRootTypes();

	@Override
	public void initModule(ServletContext context) {
		
	}
	
	@Override
	public List<TagHandler> getTagHandlers() {
		return null;
	} 
	@Override
	public void onSave(String topic) {
		
	}

	@Override
	public void registerKnowledgeRepresentationHandler(KnowledgeRepresentationManager mgr) {
		
	}


}
