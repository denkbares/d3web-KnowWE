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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.d3web.core.inference.condition.Condition;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.kbrenderer.verbalizer.VerbalizationManager.RenderingFormat;

/**
 * This class generates a verablisation of a XCLModel object.
 * 
 * @author kazamatzuri
 * 
 */
public class XclVerbalizer implements Verbalizer {
	private static final Logger LOGGER = LoggerFactory.getLogger(XclVerbalizer.class);

	@Override
	public Class<?>[] getSupportedClassesForVerbalization() {
		return new Class[] { XCLModel.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.kernel.verbalizer.Verbalizer#getSupportedRenderingTargets()
	 */
	@Override
	public RenderingFormat[] getSupportedRenderingTargets() {
		return new RenderingFormat[] {
				RenderingFormat.HTML, RenderingFormat.PLAIN_TEXT };
	}

	@Override
	/*
	 * @return produces an textual representation of this model
	 */
	public String verbalize(Object o, RenderingFormat targetFormat,
			Map<String, Object> parameter) {
		// no if-s to catch false inputs, as verbalisation manager should be
		// delegating those here
		String verbalisation = "";
		XCLModel model = (XCLModel) o;
		if (targetFormat == RenderingFormat.HTML) {
			verbalisation = verbalizeHTML(model);
		}
		else if (targetFormat == RenderingFormat.PLAIN_TEXT) {
			verbalisation = verbalizePlainText(model, parameter);
		}
		else {
			LOGGER.warn("RenderingTarget" + targetFormat + " is not supported by XclVerbalizer!");
		}
		return verbalisation;
	}

	public String verbalizePlainText(XCLModel model, Map<String, Object> parameter) {

		ArrayList<XCLRelationType> types = new ArrayList<>();
		types.add(XCLRelationType.requires);
		types.add(XCLRelationType.sufficiently);
		types.add(XCLRelationType.explains);
		types.add(XCLRelationType.contradicted);

		StringBuilder text = new StringBuilder();

		ConditionVerbalizer v = new ConditionVerbalizer();

		text.append(model.getSolution().getName()).append(" {\n");

		Map<XCLRelationType, Collection<XCLRelation>> relationMap = model.getTypedRelations();
		for (XCLRelationType type : types) {
			Collection<XCLRelation> relationsCol = relationMap.get(type);
			ArrayList<XCLRelation> relations = new ArrayList<>();
			relations.addAll(relationsCol);
			if (type == XCLRelationType.explains) {
				Collections.sort(relations,
						new XCLRelationComparator());
			}
			for (XCLRelation rel : relations) {
				Condition cond = rel.getConditionedFinding();
				String weight = "";
				if (type == XCLRelationType.explains) {
					if (rel.getWeight() != XCLRelation.DEFAULT_WEIGHT) {
						weight = "[" + doubleToString(rel.getWeight()) + "]";
					}
				}
				else if (type == XCLRelationType.contradicted) {
					weight = "[--]";
				}
				else if (type == XCLRelationType.requires) {
					weight = "[!]";
				}
				else if (type == XCLRelationType.sufficiently) {
					weight = "[++]";
				}

				text.append("  ")
						.append(v.verbalize(cond, RenderingFormat.PLAIN_TEXT, parameter))
						.append(weight.equals("") ? "" : " " + weight);

				text.append(",\n");
			}
		}
		text.append("}");
		if (!(model.getMinSupport() == null
				&& model.getEstablishedThreshold() == null && model.getSuggestedThreshold() == null)) {
			String suggested = model.getSuggestedThreshold().toString();
			if (suggested.equals("null")) {
				suggested = "-";
			}
			String established = model.getEstablishedThreshold().toString();
			if (established.equals("null")) {
				established = "-";
			}
			String minSupport = model.getMinSupport().toString();
			if (minSupport.equals("null")) {
				minSupport = "-";
			}
			text.append("[ establishedThreshold = ").append(established).append(",\n");
			text.append("   suggestedThreshold = ").append(suggested).append(",\n");
			text.append("   minSupport = ").append(minSupport).append("\n ]");
		}

		return text.toString();
	}

	/**
	 * @return produces an textual representation of this model
	 */
	public String verbalizeHTML(XCLModel model) {
		String nl = "<br />";
		String cont = model.getSolution() + nl;
		if (!model.getNecessaryRelations().isEmpty()) {
			cont += "necessary relations:" + nl;
			for (XCLRelation current : model.getNecessaryRelations()) {
				cont += VerbalizationManager.getInstance().verbalize(
						current.getConditionedFinding(), RenderingFormat.HTML)
						+ nl;
			}
		}
		if (!model.getSufficientRelations().isEmpty()) {
			cont += nl + "sufficient relations:" + nl;
			for (XCLRelation current : model.getSufficientRelations()) {
				cont += VerbalizationManager.getInstance().verbalize(
						current.getConditionedFinding(), RenderingFormat.HTML)
						+ nl;
			}
		}
		if (!model.getContradictingRelations().isEmpty()) {
			cont += nl + "contradicting relations:" + nl;
			for (XCLRelation current : model.getContradictingRelations()) {
				cont += VerbalizationManager.getInstance().verbalize(
						current.getConditionedFinding(), RenderingFormat.HTML)
						+ nl;
			}
		}

		cont += nl + "explains:" + nl;
		for (XCLRelation current : model.getRelations()) {
			cont += VerbalizationManager.getInstance().verbalize(
					current.getConditionedFinding(), RenderingFormat.HTML)
					+ (current.getWeight() == XCLRelation.DEFAULT_WEIGHT ? nl
							: " weighted: " + current.getWeight() + nl);
		}
		cont += nl;
		if (model.getSuggestedThreshold() != null) cont += " non standard suggested threshold: "
					+ model.getSuggestedThreshold() + nl;
		if (model.getMinSupport() != null) cont += " non standard minimum support: "
				+ model.getMinSupport()
					+ nl;
		if (model.getEstablishedThreshold() != null) cont += " non standard establihshed threshold: "
					+ model.getEstablishedThreshold() + nl;
		return cont;
	}

	private class XCLRelationComparator implements Comparator<XCLRelation> {

		@Override
		public int compare(XCLRelation r1, XCLRelation r2) {
			return -Double.compare(r1.getWeight(), r2.getWeight());
		}

	}

	private String doubleToString(double d) {
		String s = Double.toString(d);
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}
}
