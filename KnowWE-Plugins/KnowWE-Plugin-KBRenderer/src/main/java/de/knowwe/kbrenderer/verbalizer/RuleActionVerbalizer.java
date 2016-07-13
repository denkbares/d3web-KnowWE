/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.kbrenderer.verbalizer;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.formula.FormulaElement;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.indication.ActionContraIndication;
import de.d3web.indication.ActionInstantIndication;
import de.d3web.indication.ActionNextQASet;
import de.d3web.indication.ActionSuppressAnswer;
import de.d3web.scoring.ActionHeuristicPS;
import com.denkbares.utils.Log;
import de.knowwe.kbrenderer.verbalizer.VerbalizationManager.RenderingFormat;

/**
 * This class verbalizes (renders to String representation) a RuleAction. It
 * integrates the old VerbalizationFactory/RuleToHTML classes into the
 * verbalizer framework.
 * 
 * TODO: Internationalize!
 * 
 * @author lemmerich
 * @date june 2008
 */
public class RuleActionVerbalizer implements Verbalizer {

	private static final ResourceBundle propertyRB = ResourceBundle.getBundle("properties.messages");

	@Override
	public Class<?>[] getSupportedClassesForVerbalization() {
		return new Class[] { PSAction.class };
	}

	@Override
	public RenderingFormat[] getSupportedRenderingTargets() {
		return new RenderingFormat[] { RenderingFormat.HTML };
	}

	/**
	 * Returns a verbalization (String representation) of the given RuleAction
	 * in the target format using additional parameters.
	 * 
	 * 
	 * @param o the RuleAction to be verbalized. returns null and logs a warning
	 *        for non-rule.
	 * @param targetFormat The output format of the verbalization
	 *        (HTML/PlainText...)
	 * @param parameter additional parameters used to adapt the verbalization
	 *        (e.g., singleLine, etc...)
	 * @return A String representation of given object o in the target format
	 */
	@Override
	public String verbalize(Object o, RenderingFormat targetFormat, Map<String, Object> parameter) {
		// test, if targetformat is legal for this verbalizer

		if (targetFormat != RenderingFormat.HTML) {
			Log.warning("RenderingTarget" + targetFormat + " is not supported by RuleActionVerbalizer!");
			return null;
		}
		// Test if object is legal for this verbalizer
		if (!(o instanceof PSAction)) {
			Log.warning("Object " + o + " couldnt be rendered by RuleActionVerbalizer!");
			return null;
		}
		// cast the given object to RuleAction
		PSAction ra = (PSAction) o;

		// read parameter from parameter map, default = null
		Object context = null;
		if (parameter != null) {
			if (parameter.containsKey(Verbalizer.CONTEXT)) {
				context = parameter.get(Verbalizer.CONTEXT);
			}

		}
		return createHTMLfromAction(ra, context);
	}

	/**
	 * Creates a text-visualisation of the given action in HTML
	 * 
	 * @param PSAction the action that will be displayed
	 * @return String the text view of the action
	 */
	private static String createHTMLfromAction(PSAction ra, Object context) {
		String s = "";

		if (ra instanceof ActionHeuristicPS) {
			ActionHeuristicPS ah = (ActionHeuristicPS) ra;
			if (ah.getSolution() != null && ah.getSolution() != context) {
				s += VerbalizationManager.getInstance().verbalize(ah.getSolution(),
						RenderingFormat.HTML)
						+ ": ";
			}
			if (ah.getScore() != null) s += ah.getScore().getSymbol();
			if (ah.getSolution() != null && ah.getSolution() != context) {
				s += " (" + propertyRB.getString("rule.HeuristicScore") + ") ";
			}
			return s;

		}
		else if (ra instanceof ActionContraIndication) {
			ActionContraIndication aci = (ActionContraIndication) ra;

			s += propertyRB.getString("rule.do.ContraIndication") + " ";

			if (aci.getQASets() != null) s += createActionList(aci.getQASets());
			return s;

		}
		else if (ra instanceof ActionSuppressAnswer) {
			ActionSuppressAnswer asa = (ActionSuppressAnswer) ra;
			s += propertyRB.getString("rule.do.SuppressAnswer") + " ";
			if (asa.getQuestion() != null) s += VerbalizationManager.getInstance().verbalize(
					asa.getQuestion(), RenderingFormat.HTML);

			s += ": ";
			if (asa.getSuppress() != null) s += createActionList(asa.getSuppress());
			return s;

		}
		else if (ra instanceof ActionInstantIndication) {
			ActionInstantIndication aii = (ActionInstantIndication) ra;

			s += propertyRB.getString("rule.InstantIndication") + " ";

			if (aii.getQASets() != null) s += createActionList(aii.getQASets());
			return s;

		}
		else if (ra instanceof ActionNextQASet) {
			ActionNextQASet anqas = (ActionNextQASet) ra;
			s += propertyRB.getString("rule.NextQASet") + " ";
			if (anqas.getQASets() != null) s += createActionList(anqas.getQASets());
			return s;

		}
		// else if (ra instanceof ActionAddValue) {
		// ActionAddValue aav = (ActionAddValue) ra;
		// s += propertyRB.getString("rule.do.AddValue") + " ";
		// if (aav.getQuestion() != null) s +=
		// VerbalizationManager.getInstance().verbalize(
		// aav.getQuestion(), RenderingFormat.HTML);
		// s += ": ";
		// if (aav.getValue() != null) s +=
		// createActionList(Arrays.asList(aav.getValue()));
		// return s;
		//
		// }
		else if (ra instanceof ActionSetQuestion) {
			ActionSetQuestion asv = (ActionSetQuestion) ra;
			s += propertyRB.getString("rule.do.SetValue") + " ";
			if (asv.getQuestion() != null) s += VerbalizationManager.getInstance().verbalize(
					asv.getQuestion(), RenderingFormat.HTML);
			s += ": ";
			if (asv.getValue() != null) s += createActionList(Collections.singletonList(asv.getValue()));
			return s;

		}
		else {
			// no appropriate type found:
			return String.valueOf(ra);
		}
	}

	public static String createActionList(List<?> tempList) {
		String s = "";

		if (tempList.size() > 1) s += "(";

		// for each list member do
		Iterator<?> iter = tempList.iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			if (item instanceof Choice) {
				s += VerbalizationManager.getInstance().verbalize(item, RenderingFormat.HTML);
			}
			else if (item instanceof FormulaElement) {
				s += item.toString();
			}
			else if (item instanceof FormulaNumber) {
				s += item.toString();
			}
			else if (item instanceof NamedObject) {
				s += getIDObjectVerbalistion((NamedObject) item);
			}
			else {
				s += DefaultVerbalizer.verbalizeUnexpectedObject(item);
			}

			// do, if its not the last ListElement
			if (iter.hasNext()) s += "; ";
		}

		if (tempList.size() > 1) s += ")";

		return s;
	}

	// import from the old VerbalizationFactory
	private static String getIDObjectVerbalistion(NamedObject ido) {
		if (ido == null) return "";
		StringBuilder sb = new StringBuilder();
		sb.append(ido);

		return sb.toString();
	}
}