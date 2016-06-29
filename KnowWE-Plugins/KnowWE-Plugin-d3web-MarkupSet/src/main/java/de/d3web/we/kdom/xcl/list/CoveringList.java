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

package de.d3web.we.kdom.xcl.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.rules.RuleType;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.XCLRelationWeight;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.EmbracedContentFinder;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.knowwe.kdom.sectionFinder.UnquotedExpressionFinder;

/**
 * A covering-list markup parser
 * 
 * In the first line the solution is defined @see ListSolutionType The rest of
 * the content is split by ',' (commas) and the content in between is taken as
 * CoveringRelations
 * 
 * @author Jochen
 */
public class CoveringList extends AbstractType {

	private static final String RELATION_STORE_KEY = "XCLRELATION_STORE_KEY";

	public CoveringList() {
		this.setSectionFinder(AllTextFinder.getInstance());
		this.addChildType(new ListSolutionType());

		// cut the optional closing }
		AnonymousType closing = new AnonymousType("closing-bracket");
		closing.setSectionFinder(new StringSectionFinderUnquoted("}"));
		this.addChildType(closing);

		// allow for comment lines
		this.addChildType(new CommentLineType());

		// split by search for komas
		AnonymousType koma = new AnonymousType("koma");
		koma.setSectionFinder(new UnquotedExpressionFinder(","));
		this.addChildType(koma);

		// the rest is CoveringRelations
		this.addChildType(new CoveringRelation());

		this.setRenderer(new ReRenderSectionMarkerRenderer(
				new CoveringListRenderer()));

		// anything left is comment
		AnonymousType residue = new AnonymousType("derRest");
		residue.setSectionFinder(new AllTextFinderTrimmed());
		residue.setRenderer(StyleRenderer.COMMENT);
		this.addChildType(residue);
	}

	/**
	 * 
	 * @author volker_belli
	 * @created 08.12.2010
	 */
	private static final class CoveringListRenderer implements Renderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			KnowWEUtils.renderAnchor(section, result);
			result.appendHtml("<span id='" + section.getID() + "'>");
			DelegateRenderer.getInstance().render(section, user, result);
			result.appendHtml("</span>");
		}
	}

	class CoveringRelation extends AbstractType {

		public CoveringRelation() {

			this.setSectionFinder(new ConditionalSectionFinder(new AllTextFinderTrimmed()) {

				// hack to allow for comment after last relation
				// TODO: find better way
				@Override
				protected boolean condition(String text, Section<?> father) {
					// if starts as a comment and there is no next line, there
					// is CoveringRelation in it
					if (text.trim().startsWith("//") && !(text.contains("\n"))) {
						return false;
					}
					return true;
				}
			});

			this.addCompileScript(Priority.LOW, new CreateXCLRelationHandler());
			this.setRenderer(new CoveringRelationRenderer());

			// here also a comment might occur:
			AnonymousType relationComment = new AnonymousType("comment");
			relationComment.setSectionFinder(new RegexSectionFinder("[\\t ]*"
					+ "//[^\r\n]*+" + "\\r?\\n"));
			relationComment.setRenderer(StyleRenderer.COMMENT);
			this.addChildType(relationComment);

			// take weights
			this.addChildType(new XCLWeight());

			// add condition
			CompositeCondition cond = new CompositeCondition();

			// these are the allowed/recognized terminal-conditions
			// List<Type> termConds = new ArrayList<Type>();
			// termConds.add(new Finding());
			// termConds.add(new NumericalFinding());
			// termConds.add(new NumericalIntervallFinding());
			List<Type> termConds = RuleType.getTerminalConditions();
			cond.setAllowedTerminalConditions(termConds);

			this.addChildType(cond);
		}

		/**
		 * this handler translates the parsed covering-relation-KDOM to the
		 * d3web knowledge base
		 * 
		 * @author Jochen
		 */
		class CreateXCLRelationHandler implements D3webHandler<CoveringRelation> {

			private Section<SolutionDefinition> getCorrespondingSolutionDef(Section<CoveringRelation> s) {
				return Sections.successor(s.getParent().getParent(), SolutionDefinition.class);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * de.d3web.we.kdom.subtreeHandler.SubtreeHandler#create(de.d3web
			 * .we.kdom.Article, de.d3web.we.kdom.Section)
			 */
			@Override
			public Collection<Message> create(D3webCompiler compiler, Section<CoveringRelation> s) {

				List<Message> result = new ArrayList<>();

				Section<CompositeCondition> cond = Sections.successor(s,
						CompositeCondition.class);
				if (cond == null) {
					// no valid relation, do not revise
					return result;
				}

				if (s.hasErrorInSubtree(compiler)) {
					return Messages.asList(Messages.creationFailedWarning(
							D3webUtils.getD3webBundle()
									.getString("KnowWE.xcllist.relationfail")));
				}

				Section<SolutionDefinition> solutionDef = getCorrespondingSolutionDef(s);
				if (solutionDef != null) {
					Solution solution = solutionDef.get().getTermObject(
							compiler, solutionDef);

					if (solution != null) {
						XCLModel xclModel = solution.getKnowledgeStore().getKnowledge(
								XCLModel.KNOWLEDGE_KIND);

						if (xclModel != null) {

							if (cond != null) {

								Condition condition = KDOMConditionFactory.createCondition(
										compiler,
										cond);

								if (condition == null) {
									return Messages.asList(Messages.creationFailedWarning(
											D3webUtils.getD3webBundle()
													.getString("KnowWE.xcllist.conditionerror")));
								}

								// check the weight/relation type in square
								// brackets
								Section<XCLWeight> weight = Sections.successor(
										s, XCLWeight.class);
								XCLRelationType type = XCLRelationType.explains;
								Double w = 1.0;
								if (weight != null) {
									String weightString = weight.getText();
									type = getXCLRealtionTypeForString(weightString);
									if (type == XCLRelationType.explains) {
										weightString = weightString.replaceAll("\\[", "");
										weightString = weightString.replaceAll("\\]", "");
										try {
											w = Double.valueOf(weightString.trim());
											if (w <= 0) {
												result.add(Messages.invalidNumberWarning(
														weightString));
											}
										}
										catch (NumberFormatException e) {
											// not a valid weight
											result.add(Messages.invalidNumberWarning(weightString));
										}
									}
								}

								// Insert the Relation into the currentModel
								XCLRelation relation = XCLModel.insertAndReturnXCLRelation(
										getKnowledgeBase(compiler),
										condition,
										solution, type, w);

								KnowWEUtils.storeObject(compiler, s, RELATION_STORE_KEY, relation);

								String wString = "";
								if (w > 0 && w != 1) {
									wString = Double.toString(w);
								}
								return result;
							}
						}
					}
				}
				return Messages.asList(Messages.creationFailedWarning(
						D3webUtils.getD3webBundle()
								.getString("KnowWE.xcllist.relationfail")));
			}

			@Override
			public void destroy(D3webCompiler article, Section<CoveringRelation> s) {
				Section<SolutionDefinition> soltuionDef = getCorrespondingSolutionDef(s);

				if (soltuionDef == null) return;
				Solution solution = soltuionDef.get().getTermObject(article,
						soltuionDef);

				if (solution == null) return;
				XCLModel xclModel = solution.getKnowledgeStore().getKnowledge(
						XCLModel.KNOWLEDGE_KIND);

				if (xclModel == null) return;
				XCLRelation rel = (XCLRelation) s.getObject(article,
						RELATION_STORE_KEY);

				if (rel == null) return;
				xclModel.removeRelation(rel);

			}

		}

	}

	class XCLWeight extends AbstractType {

		public static final char BOUNDS_OPEN = '[';
		public static final char BOUNDS_CLOSE = ']';

		public XCLWeight() {
			this.setSectionFinder(new EmbracedContentFinder(BOUNDS_OPEN, BOUNDS_CLOSE, 1));

		}
	}

	/**
	 * @author Johannes Dienst
	 * 
	 *         Highlights XCLRelations. Answer Right: Green Answer wrong: Red
	 *         Answer unknown: No Highlighting
	 * 
	 */
	class CoveringRelationRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user, RenderResult string) {

			// wrapper for highlighting
			string.appendHtml("<span id='" + sec.getID()
					+ "' class = 'XCLRelationInList'>");

			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);
			XCLRelation relation = (XCLRelation) KnowWEUtils.getStoredObject(compiler, sec,
					RELATION_STORE_KEY);

			if (relation == null) {
				DelegateRenderer.getInstance().render(sec, user, string);
				return;
			}

			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			Session session = SessionProvider.getSession(user, kb);

			if (session != null) {
				// eval the Relation to find the right Rendering
				try {
					boolean fulfilled = relation.eval(session);
					// Highlight Relation
					this.renderRelation(sec, user, fulfilled, string, true);
					// close the wrapper
					string.appendHtml("</span>");
					return;
				}
				catch (Exception e) {
					// Call the XCLRelationMarkerHighlightingRenderer
					// without any additional info
					// string.append(this.renderRelationChildren(sec, user,
					// false, false);
				}
			}
			// Something went wrong: Delegate to children
			this.renderRelation(sec, user, false, string, false);
			// close the wrapper
			string.appendHtml("</span>");
		}

		/***
		 * Replaces the SpecialDelegateRenderer functionality to enable
		 * highlighting of Relations without their RelationWeights.
		 * 
		 * @param relationSection
		 * @param user
		 * @param fulfilled
		 * @param string
		 * @return
		 */
		private void renderRelation(Section<?> relationSection,
				UserContext user, boolean fulfilled, RenderResult string, boolean highlight) {

			StringBuilder buffi = new StringBuilder();

			// need a span below XCLRelationInList
			if (!highlight) {
				List<Section<?>> children = relationSection.getChildren();
				for (Section<?> s : children) {
					buffi.append(this.renderRelationChild(s,
							fulfilled, user, ""));
				}
				string.appendHtml(buffi.toString());
				return;
			}

			// b true: Color green
			if (fulfilled) {
				// Iterate over children of the relation.
				List<Section<?>> children = relationSection.getChildren();
				for (Section<?> s : children) {
					buffi.append(this.renderRelationChild(s,
							true, user, StyleRenderer.CONDITION_FULLFILLED));
				}

			}
			else {
				// b false: Color red
				List<Section<?>> children = relationSection.getChildren();
				for (Section<?> s : children) {
					buffi.append(this.renderRelationChild(s,
							true, user, StyleRenderer.CONDITION_FALSE));
				}

			}
			string.appendHtml(buffi.toString());
		}

		/**
		 * Renders the children of a CoveringRelation.
		 */
		private String renderRelationChild(
				Section<?> sec, boolean fulfilled, UserContext user,
				String color) {
			RenderResult buffi = new RenderResult(user);
			Type type = sec.get();

			if (type instanceof XCLRelationWeight) { // renders contradiction in
				// red if fulfilled

				if (fulfilled && sec.getText().trim().equals("[--]")) {
					StyleRenderer.OPERATOR.render(sec,
							user, buffi);
				}
				else {
					type.getRenderer().render(sec, user, buffi);
				}

			}
			else if (type instanceof CompositeCondition) {
				RenderResult temp = new RenderResult(buffi);
				// we do not want masked JPSWiki markup
				StyleRenderer.getRenderer(null, color).render(
						sec, user, temp);
				buffi.append(KnowWEUtils.unmaskJSPWikiMarkup(temp.toStringRaw()));
			}
			else {
				type.getRenderer().render(sec, user, buffi);
			}

			return buffi.toString();
		}
	}

	public static XCLRelationType getXCLRealtionTypeForString(String weightString) {
		if (weightString.contains("--")) {
			return XCLRelationType.contradicted;
		}
		else if (weightString.contains("!")) {
			return XCLRelationType.requires;
		}
		else if (weightString.contains("++")) {
			return XCLRelationType.sufficiently;
		}
		else {
			return XCLRelationType.explains;
		}
	}
}
