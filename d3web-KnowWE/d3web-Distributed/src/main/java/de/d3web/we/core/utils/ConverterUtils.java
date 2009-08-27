package de.d3web.we.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.answers.AnswerNum;

public class ConverterUtils {

	public static List<String> toIdStringList(Collection<? extends IDObject> idObjects) {
		List<String> result = new ArrayList<String>();
		for (IDObject idObject : idObjects) {
			result.add(idObject.getId());
		}
		return result;
	}
	
	public static List<String> toIdStringList(Object[] idObjects) {
		List<String> result = new ArrayList<String>();
		for (Object idObject : idObjects) {
			if(idObject instanceof IDObject) {
				result.add(((IDObject) idObject).getId());
			}
		}
		return result;
	}

	public static List<Object> toValueList(List values, XPSCase theCase) {
		List<Object> result = new ArrayList<Object>();
		for (Object idObject : values) {
			if(idObject instanceof AnswerNum) {
				Double value = (Double) ((AnswerNum)idObject).getValue(theCase);
				if(value != null) {
					result.add(value);
				}
			} else if(idObject instanceof IDObject) {
				result.add(((IDObject) idObject).getId());
			}
		}
		return result;
	}
	
}
