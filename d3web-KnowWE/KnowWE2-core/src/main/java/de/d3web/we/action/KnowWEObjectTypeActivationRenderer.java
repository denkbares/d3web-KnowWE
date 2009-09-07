package de.d3web.we.action;

import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEObjectType;

public class KnowWEObjectTypeActivationRenderer implements KnowWEAction{

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		// get the one needed and change is Activation state.
		List<KnowWEObjectType> types = KnowWEEnvironment.getInstance()
				.getAllKnowWEObjectTypes();
		int index = this.findIndexOfType(parameterMap.get("KnowWeObjectType"),
				types);

		// if type found
		if (index != -1) {
			List<KnowWEObjectType> toChange = KnowWEEnvironment.getInstance()
					.searchTypeInstances(types.get(index).getClass());

			for (KnowWEObjectType type : toChange) {
				if (!type.getActivationStatus()) {
					type.activateType();
				} else {
					type.deactivateType();
				}
			}
		}
		
		return "Was immer du willst";
	}
	
	private int findIndexOfType(String typeName, List<KnowWEObjectType> types) {
		String shortTypeName = typeName.substring(typeName.lastIndexOf(".")+1);
		for(KnowWEObjectType typ : types) {			
			if(typ.getName().equals(shortTypeName)) {
				return types.indexOf(typ);
			}
		}
		return -1;
	}

}
