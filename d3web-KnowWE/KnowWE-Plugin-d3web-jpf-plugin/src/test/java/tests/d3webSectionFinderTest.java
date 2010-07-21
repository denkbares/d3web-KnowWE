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

package tests;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Annotation;
import de.d3web.we.kdom.Annotation.AnnotationContent;
import de.d3web.we.kdom.Annotation.AnnotationObject;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingComparator;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.condition.OrOperator;
import de.d3web.we.kdom.condition.old.Conjunct;
import de.d3web.we.kdom.condition.old.Disjunct;
import de.d3web.we.kdom.dashTree.questionnaires.QuestionnairesTreeANTLR;
import de.d3web.we.kdom.dashTree.solutions.SolutionsTreeANTLR;
import de.d3web.we.kdom.decisionTree.QuestionTreeANTLR;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.kdom.rules.RuleActionLine;
import de.d3web.we.kdom.rules.RuleCondLine;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.semanticAnnotation.AnnotatedString;
import de.d3web.we.kdom.semanticAnnotation.AnnotationMapSign;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationEndSymbol;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationProperty;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationPropertyDelimiter;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationPropertyName;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationStartSymbol;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;
import de.d3web.we.kdom.xcl.XCLBody;
import de.d3web.we.kdom.xcl.XCLHead;
import de.d3web.we.kdom.xcl.XCLRelation;
import de.d3web.we.kdom.xcl.XCLRelationWeight;
import de.d3web.we.kdom.xcl.XCLTail;
import de.d3web.we.kdom.xcl.XCList;
import dummies.KnowWETestWikiConnector;

/**
 * Especially for the d3web-SectionFinder which
 * cannot be testet in KnowWE2SectionFinder test.
 *
 * @author Johannes Dienst
 *
 */
public class d3webSectionFinderTest extends TestCase {

	private static final String WRONG_FIRST_START = "Wrong start at first finding";
	private static final String WRONG_FIRST_END = "Wrong end at first finding";
	private static final String WRONG_SECOND_START = "Wrong start at second finding";
	private static final String WRONG_SECOND_END = "Wrong end at second finding";
	private static final String WRONG_THIRD_START = "Wrong start at third finding";
	private static final String WRONG_THIRD_END = "Wrong end at third finding";
	private static final String WRONG_FOURTH_START = "Wrong start at fourth finding";
	private static final String WRONG_FOURTH_END = "Wrong end at fourth finding";
	private static final String WRONG_COUNT = "Children count wrong";
	private static final String WRONG_TYPE = "Wrong KnowWEObjectType";
	private static final String WRONG_TYPE_COUNT = "Wrong KnowWEObjectType  section count:";

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	public void testAllAnnotationRelatedSectionFinder() {

		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment.getInstance().getArticle("default_web", "Test_Article");

		String test = "blablub {{the currently measured mileage"
			+ "<=> asks::Real mileage  /100km}}bla blub";

		Annotation type = new de.d3web.we.kdom.Annotation.Annotation();
		KnowWEArticle article = new KnowWEArticle(test, "Test_Article2", type, "default_web");
		Section artSec = article.getSection();
		List<Section> childs = artSec.getChildren();

		// Annotation
		assertEquals(WRONG_COUNT, 3, childs.size());
		assertEquals(WRONG_TYPE, new Annotation().getName(), childs.get(1).getObjectType().getName());

		// StartSymbol, Content, EndSymbol
		childs = childs.get(1).getChildren();
		assertEquals(WRONG_COUNT, 3, childs.size());
		assertEquals(WRONG_TYPE, new SemanticAnnotationStartSymbol("{{").getName(), childs.get(0).getObjectType().getName());
		assertEquals(WRONG_TYPE, new AnnotationContent().getName(), childs.get(1).getObjectType().getName());
		assertEquals(WRONG_TYPE, new SemanticAnnotationEndSymbol("}}").getName(), childs.get(2).getObjectType().getName());

		// AnnotatedString, Annotation MapSign, AnnotationObject
		childs = childs.get(1).getChildren();
		assertEquals(WRONG_COUNT, 3, childs.size());
		assertEquals(WRONG_TYPE, new AnnotatedString().getName(), childs.get(0).getObjectType().getName());
		assertEquals(WRONG_TYPE, new AnnotationMapSign().getName(), childs.get(1).getObjectType().getName());
		assertEquals(WRONG_TYPE, new AnnotationObject().getName(), childs.get(2).getObjectType().getName());

		// PlainText, AnnotationProperty, SimpleAnnotation
		childs = childs.get(2).getChildren();
		assertEquals(WRONG_COUNT, 3, childs.size());
		assertEquals(WRONG_TYPE, new PlainText().getName(), childs.get(0).getObjectType().getName());
		assertEquals(WRONG_TYPE, new SemanticAnnotationProperty().getName(), childs.get(1).getObjectType().getName());
		assertEquals(WRONG_TYPE, new SimpleAnnotation().getName(), childs.get(2).getObjectType().getName());

		// AnnotationPropertyName, AnnotationPropertyDelimiter
		childs = childs.get(1).getChildren();
		assertEquals(WRONG_TYPE, new SemanticAnnotationPropertyName().getName(), childs.get(0).getObjectType().getName());
		assertEquals(WRONG_TYPE, new SemanticAnnotationPropertyDelimiter().getName(), childs.get(1).getObjectType().getName());
	}

	public void testAnnotationSectioner() {
		String test = "blablub {{the currently measured mileage <=> asks:: Real mileage  /100km}}bla blub";
		List<SectionFinderResult> results =
			new Annotation().new AnnotationSectionFinder().lookForSections(test, null, null);

		assertEquals(WRONG_FIRST_START, 8, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 74, results.get(0).getEnd());
	}

	/**
	 * Seems not right. finding tests only if text.contains("=")
	 */
	public void testComplexFindingSectionFinders() {
		String test = "Are you hungry? = Yes OR Are you hungry? = Very very hungry";
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		KnowWEEnvironment.getInstance().getArticle("default_web", "Test_Article");

		KnowWEArticle article = new KnowWEArticle(test, "Test_Article2", new ComplexFinding(), "default_web");
		Section artSec = article.getSection();
		List<Section> childs = artSec.getChildren();

		// ComplexFinding
		assertEquals(WRONG_COUNT, 1, childs.size());
		assertEquals(WRONG_TYPE, new ComplexFinding().getName(), childs.get(0).getObjectType().getName());

		// disjunctive form
		Section disjunction = childs.get(0);
		childs = disjunction.getChildren();
		assertEquals(WRONG_COUNT, 5, childs.size());

		// TODO Test order more deeply: disj, or, disj
		assertEquals(WRONG_TYPE_COUNT+" "+OrOperator.class.toString(),1,  disjunction.findChildrenOfType(OrOperator.class).size());
		assertEquals(WRONG_TYPE_COUNT+" "+Disjunct.class.getName(),2, disjunction.findChildrenOfType(Disjunct.class).size());

		//conjunctive form (trivial in this case)
		childs = childs.get(0).getChildren();
		assertEquals(WRONG_COUNT, 1, childs.size());
		assertEquals(WRONG_TYPE, new Conjunct().getName(),
				childs.get(0).getObjectType().getName());

		//conjunctive form (trivial in this case)
		childs = childs.get(0).getChildren();
		assertEquals(WRONG_COUNT, 1, childs.size());
		assertEquals(WRONG_TYPE, new Finding().getName(), childs.get(0).getObjectType().getName());

		// FindingQuestion, findingComparator, FindingAnswer
		Section finding = childs.get(0);
		childs = finding.getChildren();
		assertEquals(WRONG_COUNT, 5, childs.size());
		assertEquals(WRONG_TYPE_COUNT+" "+FindingQuestion.class.toString(),1,  finding.findChildrenOfType(FindingQuestion.class).size());
		assertEquals(WRONG_TYPE_COUNT+" "+FindingComparator.class.toString(),1,  finding.findChildrenOfType(FindingComparator.class).size());
		assertEquals(WRONG_TYPE_COUNT+" "+FindingAnswer.class.toString(),1,  finding.findChildrenOfType(FindingAnswer.class).size());

	}

	/**
	 * Seems not right. finding tests only if text.contains("=")
	 */
	public void testComplexFindingANTLRSectionCreator() {
		String test = "Are you hungry? = Yes OR Are you hungry? = Very very hungry [2]";
		ComplexFinding.ComplexFindingANTLRSectionFinder creator =
					new ComplexFinding().new ComplexFindingANTLRSectionFinder();
		List<SectionFinderResult> results = creator.lookForSections(test, null, null);
		assertEquals(WRONG_FIRST_START, -1, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 58, results.get(0).getEnd());
	}

	/**
	 * Seems not right. finding tests only if text.contains(=)
	 */
	public void testFindingSectionFinder() {
		String text = "Are you hungry? = Yes";
		Finding.FindingSectionFinder f = new Finding().new FindingSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 0, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 21, results.get(0).getEnd());
	}

	// TODO: What text...
	public void testQuestionnairesKDOMANTLRSectionFinder() {
		String text = "";
		QuestionnairesTreeANTLR.QuestionnairesKDOMANTLRSectionFinder f =
			new QuestionnairesTreeANTLR().new QuestionnairesKDOMANTLRSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);
	}

	// TODO: What text...
	public void testQuestionTreeKDOMANTLRSectionFinder() {
		String text = "bla blub <Questions-section>"
					+ "Interview for what to do"
					+ "- Are you tired from work? [oc]"
					+ "-- Can hardly keep the eyes open"
					+ "-- A little bit"
					+ "-- No"
					+ "- Is your fridge empty? [oc]"
					+ "-- Yes"
					+ "-- No"
					+ "</Questions-section> bla blub";

		QuestionTreeANTLR.QuestionTreeKDOMANTLRSectionFinder f =
			new QuestionTreeANTLR().new QuestionTreeKDOMANTLRSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);
		System.out.println();
	}

	public void testRuleActionLineSectionFinder() {
		String text = " yoyo THEN Exhaust pipe color evaluation += abnormal";
		RuleActionLine.RuleActionLineSectionFinder f =
			new RuleActionLine().new RuleActionLineSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 6, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 52, results.get(0).getEnd());
	}

	public void testRuleCondLineSectionFinder() {
		String text = " yoyo IF (Fuel = unleaded gasoline AND Exhaust pipe color = sooty black)";
		ISectionFinder f =
			new RuleCondLine().getSectioner();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 6, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 72, results.get(0).getEnd());
	}

	public void testRuleSectionFinder() {
		String text = " yoyo IF (Fuel = diesel AND Exhaust pipe color = sooty black)\r\n"
					+ "THEN Exhaust pipe color evaluation += normal";
		ISectionFinder f = new Rule().getSectioner();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 6, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 107, results.get(0).getEnd());
	}

	// TODO: What text...
	public void testSolutionsKDOMANTLRSectionFinder() {
		String text = "";
		SolutionsTreeANTLR.SolutionsKDOMANTLRSectionFinder f =
			new SolutionsTreeANTLR().new SolutionsKDOMANTLRSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);
	}

	// TODO How to test that
	public void testTableSectionFinder() {
		String text = "<Table default=\"+,-,0\" width=\"100\" row=\"1\" column=\"1\">"
			+ "|                        | Apple \r \n"
			+ "| sweetness              |   +   "
			+ "</Table>";

	}

	public void testXCListSectionFinder() {
		String text = "Buy some food { \r\n"
		    		+ "Is your fridge empty? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
		    	    + "Are you hungry? = Very very hungry,\r\n"
		    	    + "What do you like? = Shopping,\r\n"
		    	    + "Are the stores still open? = Yes [!],\r\n"
		    		+ "}\r\n \r\n"
		    		+ "Meet someone at a restaurant {\r\n"
		    	    + "Are you hungry? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
		    	    + "Do you have a date? = Yes OR Is there a friend to hang out with? = Yes [2],\r\n"
		    	    + "Is your fridge empty? = Yes [1],\r\n"
		    	    + "What do you like? = Meeting people,\r\n"
		    		+ "}\r\n";

		ISectionFinder f = new XCList().getSectioner();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 0, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 198, results.get(0).getEnd());

		assertEquals(WRONG_SECOND_START, 203, results.get(1).getStart());
		assertEquals(WRONG_SECOND_END, 450, results.get(1).getEnd());
	}

	public void testXCLBodySectionFinder() {
		String text = "Buy some food { \r\n"
				+ "Is your fridge empty? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
				+ "Are you hungry? = Very very hungry,\r\n"
				+ "What do you like? = Shopping,\r\n"
				+ "Are the stores still open? = Yes [!],\r\n"
				+ "}\r\n \r\n \r \n";
//				+ "Meet someone at a restaurant {\r\n"
//				+ "Are you hungry? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
//				+ "Do you have a date? = Yes OR Is there a friend to hang out with? = Yes [2],\r\n"
//				+ "Is your fridge empty? = Yes [1],\r\n"
//				+ "What do you like? = Meeting people,\r\n"
//				+ "}\r\n";

		XCLBody.XCLBodySectionFinder f = new XCLBody().new XCLBodySectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 14, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 198, results.get(0).getEnd());
	}

	public void testXCLHeadSectionFinder() {
		String text = "Buy some food { \r\n"
					+ "Is your fridge empty? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
					+ "Are you hungry? = Very very hungry,\r\n"
					+ "What do you like? = Shopping,\r\n"
					+ "Are the stores still open? = Yes [!],\r\n"
					+ "}\r\n \r\n \r \n";

		XCLHead.XCLHeadSectionFinder f = new XCLHead().new XCLHeadSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 0, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 13, results.get(0).getEnd());
	}

	public void testXCLTailSectionFinder() {
		String text = "Buy some food { \r\n"
			+ "Is your fridge empty? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
			+ "Are you hungry? = Very very hungry,\r\n"
			+ "What do you like? = Shopping,\r\n"
			+ "Are the stores still open? = Yes [!],\r\n"
			+ "} \r\n \r\n \r \n"
			+ "Meet someone at a restaurant {\r\n"
			+ "Are you hungry? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
			+ "Do you have a date? = Yes OR Is there a friend to hang out with? = Yes [2],\r\n"
			+ "Is your fridge empty? = Yes [1],\r\n"
			+ "What do you like? = Meeting people,\r\n"
			+ "} [lol] \r\n";

		XCLTail.XCLTailSectionFinder f = new XCLTail().new XCLTailSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 456, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 461, results.get(0).getEnd());
	}

	public void testXCLRelationSectionFinder() {
		String text = "Is your fridge empty? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
			+ "Are you hungry? = Very very hungry,\r\n"
			+ "What do you like? = Shopping,\r\n"
			+ "} \r\n \r\n \r \n";

		ISectionFinder f = new XCLRelation().getSectioner();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 0, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 69, results.get(0).getEnd());

		assertEquals(WRONG_SECOND_START, 72, results.get(1).getStart());
		assertEquals(WRONG_SECOND_END, 106, results.get(1).getEnd());

		assertEquals(WRONG_THIRD_START, 109, results.get(2).getStart());
		assertEquals(WRONG_THIRD_END, 137, results.get(2).getEnd());
	}

	public void testXCLRelationWeightSectionFinder() {
		String text = "Buy some food { \r\n"
			+ "Is your fridge empty? = Yes OR Are you hungry? = Very very hungry [2],\r\n"
			+ "Are you hungry? = Very very hungry[--],\r\n"
			+ "What do you like? = Shopping[!],\r\n"
			+ "Are the stores still open? = Yes [++],\r\n"
			+ "Is there any body out there? = NO [;:], \r\n"
			+ "} \r\n \r\n \r \n";

		XCLRelationWeight.XCLRelationWeightSectionFinder f =
			new XCLRelationWeight().new XCLRelationWeightSectionFinder();
		List<SectionFinderResult> results = f.lookForSections(text, null, null);

		assertEquals(WRONG_FIRST_START, 84, results.get(0).getStart());
		assertEquals(WRONG_FIRST_END, 87, results.get(0).getEnd());

		assertEquals(WRONG_SECOND_START, 124, results.get(1).getStart());
		assertEquals(WRONG_SECOND_END, 128, results.get(1).getEnd());

		assertEquals(WRONG_THIRD_START, 159, results.get(2).getStart());
		assertEquals(WRONG_THIRD_END, 162, results.get(2).getEnd());

		assertEquals(WRONG_FOURTH_START, 198, results.get(3).getStart());
		assertEquals(WRONG_FOURTH_END, 202, results.get(3).getEnd());
	}


}
