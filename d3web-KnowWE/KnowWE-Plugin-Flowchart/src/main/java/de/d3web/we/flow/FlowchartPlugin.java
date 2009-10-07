package de.d3web.we.flow;
import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;




public class FlowchartPlugin extends AbstractDefaultKnowWEModule{

	@Override
	public List<de.d3web.we.kdom.KnowWEObjectType> getRootTypes() {
		List<de.d3web.we.kdom.KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(new FlowchartSection());
		return types;
	}
	
	private static FlowchartPlugin instance;
	
	public static FlowchartPlugin getInstance() {
		if (instance == null) {
			instance = new FlowchartPlugin();
			
		}

		return instance;
	}

}
