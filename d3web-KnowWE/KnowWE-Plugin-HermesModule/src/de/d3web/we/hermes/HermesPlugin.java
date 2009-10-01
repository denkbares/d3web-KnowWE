package de.d3web.we.hermes;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.hermes.kdom.TimeEventType;
import de.d3web.we.hermes.kdom.renderer.TimeLineHandler;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;
import de.d3web.we.taghandler.TagHandler;

public class HermesPlugin extends AbstractDefaultKnowWEModule {

    private static HermesPlugin instance;

    public static HermesPlugin getInstance() {
	if (instance == null) {
	    instance = new HermesPlugin();
	}
	return instance;
    }

    /**
     * @see de.d3web.we.module.AbstractDefaultKnowWEModule#getRootTypes() The
     *      Type 'DemoSectionType' is registered to the KnowWE-type system. This
     *      method is called once at initialization of KnowWE(-Modules)
     */
    @Override
    public List<KnowWEObjectType> getRootTypes() {
	List<KnowWEObjectType> rootTypes = new ArrayList<KnowWEObjectType>();
	rootTypes.add(new TimeEventType());
	return rootTypes;
    }
    
	@Override
	public List<TagHandler> getTagHandlers() {
		List<TagHandler> list = new ArrayList<TagHandler>();
		list.add(new TimeLineHandler());
		return list;
	} 
}