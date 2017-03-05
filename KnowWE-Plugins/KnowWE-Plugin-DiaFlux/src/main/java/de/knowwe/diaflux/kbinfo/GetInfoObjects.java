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

package de.knowwe.diaflux.kbinfo;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.diaFlux.flow.EndNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.StartNode;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.D3webTermDefinition;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.FlowchartSubTreeHandler;
import de.knowwe.diaflux.FlowchartUtils;

public class GetInfoObjects extends AbstractAction {

	private static final String ARTICLE_IDENTIFIER_PREFIX = "$$article$$";

	public GetInfoObjects() {
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String ids = context.getParameter("ids");
		String flowchartId = context.getParameter("sectionID");
		Section<?> flowchart = Sections.get(flowchartId);
		Identifier[] idArray;
		try {
			JSONArray json = new JSONArray(ids);
			idArray = new Identifier[json.length()];
			for (int i = 0; i < json.length(); i++) {
				idArray[i] = Identifier.fromExternalForm(json.getString(i));
			}
		}
		catch (JSONException e) {
			throw new IOException("Unable to read ids");
		}
		String result;
		if (ids == null || ids.isEmpty()) {
			result = "<kbinfo></kbinfo>";
		}
		else {
			StringBuilder bob = new StringBuilder();
			getInfoObjectsForIDs(flowchart, idArray, bob);
			result = bob.toString();
		}

		context.setContentType("text/xml; charset=UTF-8");
		context.getWriter().write(result);
	}

	public static void getInfoObjectsForIDs(Section<?> flowchart, Identifier[] identifiers, StringBuilder bob) {
		appendHeader(bob);

		for (Identifier identifier : identifiers) {
			appendInfoObject(flowchart, identifier, bob);
		}

		// finish result
		appendFooter(bob);
	}

	public static void appendHeader(StringBuilder bob) {
		bob.append("<kbinfo>\n");
	}

	public static void appendFooter(StringBuilder bob) {
		bob.append("</kbinfo>");
	}

	public static void appendInfoObject(Section<?> flowchart, Identifier identifier, StringBuilder bob) {
		// for objects the identifier always consists
		// of the id of compiler section and the object name;
		// for compiler it only consists of the id of the compilers compiling section
		if (isArticleIdentifier(identifier)) {
			// we want to have the article itself
			appendArticleInfoObject(flowchart, identifier, bob);
		}
		else {
			// look for an object inside a compiler
			appendKBInfoObject(flowchart, identifier, bob);
		}
	}

	private static void appendArticleInfoObject(Section<?> flowchart, Identifier articleIdentifier, StringBuilder bob) {

		String title = articleIdentifier.getLastPathElement();
		Article article = Environment.getInstance().getArticle(flowchart.getWeb(), title);
		String id = articleIdentifier.toExternalForm();

		bob.append("\t<article");
		bob.append(" id='").append(encodeXML(id)).append("'");
		bob.append(" name='").append(title).append("'");
		bob.append(">");
		Collection<D3webCompiler> compilers = Compilers.getCompilers(flowchart, D3webCompiler.class);

		List<Section<TermDefinition>> defSections = Sections.successors(article.getRootSection(), TermDefinition.class);
		Set<NamedObject> objects = new LinkedHashSet<>();
		for (Section<TermDefinition> section : defSections) {
			D3webCompiler definitionCompiler = null;
			for (D3webCompiler compiler : compilers) {
				if (compiler.isCompiling(section)) {
					definitionCompiler = compiler;
					break;
				}
			}
			if (definitionCompiler == null) continue;
			Identifier identifier = section.get().getTermIdentifier(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);
			// append if TerminologyObject
			if (section.get() instanceof D3webTermDefinition
					&& (Solution.class.isAssignableFrom(termObjectClass)
					|| QContainer.class.isAssignableFrom(termObjectClass)
					|| Question.class.isAssignableFrom(termObjectClass))) {
				Section<D3webTermDefinition> d3webDefinition = Sections.cast(section, D3webTermDefinition.class);
				@SuppressWarnings("unchecked")
				NamedObject object = d3webDefinition.get()
						.getTermObject(definitionCompiler, d3webDefinition);
				if (object != null) {
					objects.add(object);
				}
				continue;
			}

			KnowledgeBase base = definitionCompiler.getKnowledgeBase();
			String objectName = identifier.getLastPathElement();
			// append if flow
			FlowSet flowSet = DiaFluxUtils.getFlowSet(base);
			if (flowSet != null) {
				Flow flow = flowSet.get(objectName);
				if (flow != null) {
					objects.add(flow);
				}
			}
		}
		outer:
		for (NamedObject object : objects) {
			if (object instanceof TerminologyObject) {
				for (TerminologyObject parent : ((TerminologyObject) object).getParents()) {
					if (objects.contains(parent)) {
						// don't add objects that are children of other objects that will be added
						continue outer;
					}
				}
			}
			appendChild(article.getTitle(), object, bob);
		}

		bob.append("\t</article>\n");
	}

	public static Identifier createArticleIdentifier(String title) {
		return new Identifier(ARTICLE_IDENTIFIER_PREFIX, title);
	}

	public static boolean isArticleIdentifier(Identifier identifier) {
		return identifier.getPathElementAt(0).equals(ARTICLE_IDENTIFIER_PREFIX);
	}

	private static void appendKBInfoObject(Section<?> flowchart, Identifier identifier, StringBuilder bob) {
		String objectName = identifier.getLastPathElement();
		String title = identifier.getPathElementAt(0);

		Collection<D3webCompiler> compilers = Compilers.getCompilers(flowchart, D3webCompiler.class);
		D3webCompiler definitionCompiler = null;
		KnowledgeBase base = null;
		NamedObject object = null;
		outer:
		for (D3webCompiler compiler : compilers) {
			Collection<Section<?>> termDefiningSections = compiler.getTerminologyManager()
					.getTermDefiningSections(new Identifier(objectName));
			for (Section<? extends Type> termDefiningSection : termDefiningSections) {
				if (termDefiningSection.getTitle().equals(title)) {
					definitionCompiler = compiler;
					base = compiler.getKnowledgeBase();
					object = base.getManager().search(objectName);
					break outer;
				}
			}
		}
		if (definitionCompiler == null) return;

		if (object instanceof Solution) {
			appendSolutionInfoObject(definitionCompiler, title, (Solution) object, bob);
		}
		else if (object instanceof Question) {
			appendQuestionInfoObject(definitionCompiler, title, (Question) object, bob);
		}
		else if (object instanceof QContainer) {
			appendQContainerInfoObject(definitionCompiler, title, (QContainer) object, bob);
		}
		else {
			// no object found in TermManager of KB
			FlowSet set = base.getKnowledgeStore().getKnowledge(FluxSolver.FLOW_SET);
			// next, try flowcharts
			if (set != null && set.contains(objectName)) {
				appendFlowInfoObject(title, set.get(objectName), bob);
			}
			else {
				bob.append("<unknown id='").append(objectName).append("'></unknown>");
			}
		}
	}

	private static void appendSolutionInfoObject(D3webCompiler definitionCompiler, String title, Solution object, StringBuilder bob) {
		String id = new Identifier(title, object.getName()).toExternalForm();
		bob.append("\t<solution");
		bob.append(" id='").append(encodeXML(id)).append("'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		bob.append(">\n");
		appendChildren(definitionCompiler, title, object, bob);
		bob.append("\t</solution>\n");
	}

	private static void appendQuestionInfoObject(D3webCompiler definitionCompiler, String title, Question object, StringBuilder bob) {
		String id = new Identifier(title, object.getName()).toExternalForm();
		bob.append("\t<question");
		bob.append(" id='").append(encodeXML(id)).append("'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		if (BasicProperties.isAbstract(object)) {
			bob.append(" abstract='true'");
		}
		bob.append(" type='");
		bob.append(
				(object instanceof QuestionYN) ? "bool" :
						(object instanceof QuestionOC) ? "oc" :
								(object instanceof QuestionMC) ? "mc" :
										(object instanceof QuestionDate) ? "date" :
												(object instanceof QuestionNum) ? "num" :
														(object instanceof QuestionText) ? "text" :
																"???"
		);
		bob.append("'");
		bob.append(">\n");
		appendChildren(definitionCompiler, title, object, bob);

		if (object instanceof QuestionChoice) {
			for (Choice answer : ((QuestionChoice) object).getAllAlternatives()) {
				bob.append("\t\t<choice>").append(encodeXML(answer.getName())).append(
						"</choice>\n");
			}
		}
		else if (object instanceof QuestionNum) {
			InfoStore infoStore = object.getInfoStore();
			if (infoStore.contains(BasicProperties.QUESTION_NUM_RANGE)) {
				NumericalInterval interval = infoStore.getValue(BasicProperties.QUESTION_NUM_RANGE);
				// TODO: check for open/closed
				bob.append("<range min='").append(interval.getLeft()).append("' ");
				bob.append("max='").append(interval.getRight()).append("'></range>");
			}
			if (infoStore.contains(MMInfo.UNIT)) {
				String value = infoStore.getValue(MMInfo.UNIT);
				bob.append("<unit>").append(value).append("</unit>");
			}
		}
		bob.append("\t</question>\n");
	}

	private static void appendQContainerInfoObject(D3webCompiler definitionCompiler, String title, QContainer object, StringBuilder bob) {
		String id = new Identifier(title, object.getName()).toExternalForm();
		bob.append("\t<qset");
		bob.append(" id='").append(encodeXML(id)).append("'");
		bob.append(" name='").append(encodeXML(object.getName())).append("'");
		bob.append(">\n");
		appendChildren(definitionCompiler, title, object, bob);
		bob.append("\t</qset>\n");
	}

	private static void appendFlowInfoObject(String title, Flow flow, StringBuilder bob) {
		String name = flow.getName();
		String id = new Identifier(title, name).toExternalForm();
		List<StartNode> startNodes = flow.getStartNodes();
		List<EndNode> exitNodes = flow.getExitNodes();

		bob.append("\t<flowchart");
		bob.append(" id='").append(encodeXML(id)).append("'");
		bob.append(" name='").append(encodeXML(name)).append("'");

		// String iconName = flow.getInfoStore().getValue(
		// Property.getProperty(FlowchartSubTreeHandler.ICON, String.class));
		String iconName = (String) FlowchartUtils.getFlowProperty(flow,
				FlowchartSubTreeHandler.ICON_KEY);

		if (iconName != null && !iconName.isEmpty()) {
			bob.append(" icon='").append(encodeXML(iconName)).append("'");
		}

		bob.append(">\n");
		for (StartNode node : startNodes) {
			bob.append("\t\t<start>").append(encodeXML(node.getName())).append("</start>\n");
		}
		for (EndNode node : exitNodes) {
			bob.append("\t\t<exit>").append(encodeXML(node.getName())).append("</exit>\n");
		}
		bob.append("\t</flowchart>\n");
	}

	private static void appendChildren(D3webCompiler definitionCompiler, String title, TerminologyObject object, StringBuilder bob) {
		outer:
		for (TerminologyObject child : object.getChildren()) {
			Collection<Section<?>> termDefiningSections = definitionCompiler.getTerminologyManager()
					.getTermDefiningSections(new Identifier(child.getName()));
			for (Section<?> termDefiningSection : termDefiningSections) {
				if (termDefiningSection.getTitle().equals(title)) {
					appendChild(title, child, bob);
					continue outer;
				}
			}
		}
	}

	private static void appendChild(String title, NamedObject child, StringBuilder bob) {
		bob.append("\t\t<child>");
		bob.append(encodeXML(new Identifier(title, child.getName()).toExternalForm()));
		bob.append("</child>\n");
	}

	static String encodeXML(String text) {
		// we must additionally some jspwiki control characters
		return StringEscapeUtils.escapeXml(text)
				.replace("\\\\", "&#92;&#92;")
				.replace("~", "&#126;")
				.replace("%%", "&#37;&#37;");
	}
}
