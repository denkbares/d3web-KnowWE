package cc.wiki.todo;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.module.AbstractDefaultKnowWEModule;

public class TodoPlugin extends AbstractDefaultKnowWEModule {

	static {
		//KnowWEEnvironment.getInstance().registerTagHandler(new TodoTagHandler());
	}
	
	@Override
	public List<KnowWEObjectType> getRootTypes() {
		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(new TodoSection());
		return types;
	}

}
