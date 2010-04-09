/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.session.Value;
import de.d3web.core.session.XPSCase;
import de.d3web.core.session.values.AnswerDate;
import de.d3web.core.session.values.AnswerNum;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.EvaluatableAnswerDateValue;
import de.d3web.core.session.values.NumValue;

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

	/**
	 * Use: public static List<Object> toValueList(Value givenValue, XPSCase
	 * theCase)
	 */
	@Deprecated
	public static List<Object> toValueList(List values, XPSCase theCase) {
		List<Object> result = new ArrayList<Object>();
		for (Object idObject : values) {
			if(idObject instanceof AnswerNum) {
				Double value = (Double) ((AnswerNum)idObject).getValue(theCase);
				if(value != null) {
					result.add(value);
				}
			}
			else if (idObject instanceof AnswerDate) {
				EvaluatableAnswerDateValue dateEval = (EvaluatableAnswerDateValue) ((AnswerDate)idObject).getValue(theCase);
				Date value = dateEval.eval(theCase);
				if(value != null) {
					result.add(value);
				}
			}
			else if(idObject instanceof IDObject) {
				result.add(((IDObject) idObject).getId());
			}
		}
		return result;
	}
	
	/**
	 * Use: public static List<Object> toValueList(Value givenValue, XPSCase
	 * theCase)
	 */
	@Deprecated
	public static List<Object> toValueList(Answer givenValue, XPSCase theCase) {
		List<Object> result = new ArrayList<Object>();
		if (givenValue instanceof AnswerNum) {
			Double value = (Double) ((AnswerNum) givenValue).getValue(theCase);
			if (value != null) {
				result.add(value);
			}
		} else if (givenValue instanceof AnswerDate) {
			EvaluatableAnswerDateValue dateEval = (EvaluatableAnswerDateValue) ((AnswerDate) givenValue)
					.getValue(theCase);
			Date value = dateEval.eval(theCase);
			if (value != null) {
				result.add(value);
			}
		} else if (givenValue instanceof IDObject) {
			result.add(((IDObject) givenValue).getId());
		}
		return result;
	}

	public static List<Object> toValueList(Value givenValue, XPSCase theCase) {
		List<Object> result = new ArrayList<Object>();
		if (givenValue instanceof NumValue) {
			Double value = (Double) ((NumValue) givenValue).getValue();
			if (value != null) {
				result.add(value);
			}
		}
		else if (givenValue instanceof DateValue) {
			EvaluatableAnswerDateValue dateEval = (EvaluatableAnswerDateValue) ((DateValue) givenValue)
					.getValue();
			Date value = dateEval.eval(theCase);
			if (value != null) {
				result.add(value);
			}
		}
		else if (givenValue instanceof IDObject) {
			result.add(((IDObject) givenValue).getId());
		}
		return result;
	}
}
