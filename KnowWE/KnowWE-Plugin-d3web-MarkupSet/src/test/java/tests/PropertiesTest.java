package tests;

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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;
import utils.KBTestUtilNewMarkup;
import utils.TestArticleManager;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.d3web.property.PropertyDeclarationType;
import de.knowwe.d3web.property.PropertyObjectReference;
import de.knowwe.d3web.property.init.InitPropertyHandler;

/**
 * This class tests the %%Property markup. In the test article are a lot of
 * defined properties, here we check if they are created or not.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.12.2011
 */
public class PropertiesTest extends TestCase {

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	private Article getArticle() {
		return TestArticleManager
				.getArticle(KBTestUtilNewMarkup.KBCREATION_ARTICLE_FILE);
	}

	private KnowledgeBase getKB() {
		return KBTestUtilNewMarkup.getInstance().getKnowledgeBase(getArticle());
	}

	private TerminologyObject getTO(String name) {
		return getKB().getManager().search(name);
	}

	private Choice getChoice(String qName, String cName) {
		QuestionChoice to = (QuestionChoice) getTO(qName);
		for (Choice choice : to.getAllAlternatives()) {
			if (choice.getName().equals(cName)) return choice;
		}
		return null;
	}

	private InfoStore getInfoStore(String name) {
		return getTO(name).getInfoStore();
	}

	public Section<PropertyDeclarationType> getPropertyDeclarationSection(String name) {
		List<Section<PropertyObjectReference>> namendObjectRefs = Sections.successors(
				getArticle().getRootSection(), PropertyObjectReference.class);
		for (Section<PropertyObjectReference> namedObjectRef : namendObjectRefs) {
			NamedObject termObject = namedObjectRef.get().getTermObject(
					Compilers.getCompiler(namedObjectRef.getArticle(), D3webCompiler.class),
					namedObjectRef);
			if (termObject.getName().equals(name)) {
				return Sections.ancestor(namedObjectRef,
						PropertyDeclarationType.class);
			}
		}
		return null;
	}

	public void testKBProperties() {
		String author = getKB().getInfoStore().getValue(BasicProperties.AUTHOR);
		assertEquals("AUTHOR was not set correctly", "test author", author);

		String version = getKB().getInfoStore().getValue(BasicProperties.VERSION);
		assertEquals("VERSION was not set correctly", "test version", version);
	}

	public void testInitProperties() {

		String init = getInfoStore("Idle speed system o.k.?").getValue(BasicProperties.INIT);
		assertEquals("INIT for QuestionYN not set correctly", "Yes",
				init);

		init = getInfoStore("Mileage evaluation").getValue(BasicProperties.INIT);
		assertEquals("INIT UNKNOWN not set correctly", "UNKNOWN",
				init);

		init = getInfoStore("Other").getValue(BasicProperties.INIT);
		assertEquals("INIT QuestionText not set correctly", "text init",
				init);

		init = getInfoStore("Exhaust fumes").getValue(BasicProperties.INIT);
		assertEquals("INIT QuestionChoice not set correctly", "black",
				init);

		init = getInfoStore("Real mileage  /100km").getValue(BasicProperties.INIT);
		assertEquals("INIT QuestionNum not set correctly", "6",
				init);

		init = getInfoStore("Num. Mileage evaluation").getValue(BasicProperties.INIT);
		assertEquals("APRIORI for Solution not set correctly", "5.1232",
				init);

		float apriori = getInfoStore("Damaged idle speed system").getValue(BasicProperties.APRIORI);
		assertEquals("INIT QuestionChoice not set correctly",
				new Float(121.12422135151512667623523523423).floatValue(),
				apriori);
	}

	public void testImpossibleInitProperties() {
		// Solution
		String init = getInfoStore("Mechanical problem").getValue(BasicProperties.INIT);
		assertEquals("INIT for Solution not set correctly", "wrong init for solution", init);

		Section<PropertyDeclarationType> propDeclSec = getPropertyDeclarationSection("Mechanical problem");
		Collection<Message> messages = Messages.getMessages(
				D3webUtils.getCompiler(propDeclSec), propDeclSec,
				InitPropertyHandler.class);
		assertEquals("No message found for assigning an init value to a Solution", 1,
				messages.size());
		assertEquals("No error found for assigning an init value to a Solution",
				Message.Type.ERROR, messages.iterator().next().getType());

		// Quesitonnaire
		init = getInfoStore("Observations").getValue(BasicProperties.INIT);
		assertEquals("INIT for Questionnaire not set correctly", "wrong init for questionnaire",
				init);

		messages = Messages.getMessages(D3webUtils.getCompiler(propDeclSec), propDeclSec,
				InitPropertyHandler.class);
		assertEquals("No message found for assigning an init value to a Questionnaire", 1,
				messages.size());
		assertEquals("No error found for assigning an init value to a Questionnaire",
				Message.Type.ERROR, messages.iterator().next().getType());

		// Choice
		init = getChoice("Idle speed system o.k.?", "Yes").getInfoStore().getValue(
				BasicProperties.INIT);
		assertEquals("INIT QuestionChoice not set correctly", "makes no sense", init);

		propDeclSec = getPropertyDeclarationSection("Yes");
		messages = Messages.getMessages(D3webUtils.getCompiler(propDeclSec), propDeclSec,
				InitPropertyHandler.class);
		assertEquals("No message found for assigning an init value to a choice", 1,
				messages.size());
		assertEquals("No error found for assigning an init value to a choice",
				Message.Type.ERROR, messages.iterator().next().getType());

		// Wrong Choice for QuestionChoice
		init = getInfoStore("Fuel").getValue(BasicProperties.INIT);
		assertEquals("INIT QuestionChoice not set correctly", "water", init);

		propDeclSec = getPropertyDeclarationSection("Fuel");
		messages = Messages.getMessages(D3webUtils.getCompiler(propDeclSec), propDeclSec,
				InitPropertyHandler.class);
		assertEquals("No message found for assigning 'water' as the init value of Fuel", 1,
				messages.size());
		assertEquals("No message found for assigning 'water' as the init value of Fuel",
				Message.Type.ERROR, messages.iterator().next().getType());

		// Wrong value for QuestionNum
		init = getInfoStore("Average mileage /100km").getValue(BasicProperties.INIT);
		assertEquals("INIT QuestionChoice not set correctly", "five",
				init);

		propDeclSec = getPropertyDeclarationSection("Average mileage /100km");
		messages = Messages.getMessages(D3webUtils.getCompiler(propDeclSec), propDeclSec,
				InitPropertyHandler.class);
		assertEquals("No message found for assigning 'five' as the init value for a QuestionNum",
				1, messages.size());
		assertEquals("No message found for assigning 'water' as the init value for a QuestionNum",
				Message.Type.ERROR, messages.iterator().next().getType());
	}

	public void testMMInfosProperties() {
		String prompt = getInfoStore("Damaged idle speed system").getValue(MMInfo.PROMPT,
				Locale.GERMAN);
		assertEquals("PROMPT for locale de not set correctly", "test prompt de",
				prompt);

		prompt = getInfoStore("Other").getValue(MMInfo.PROMPT, Locale.ENGLISH);
		assertEquals("PROMPT for locale en not set correctly", "test prompt en",
				prompt);

		prompt = getChoice("Fuel", "unleaded gasoline").getInfoStore().getValue(MMInfo.PROMPT,
				Locale.GERMAN);
		assertEquals("PROMPT for locale de to a Choice not set correctly", "benzin",
				prompt);

		prompt = getInfoStore("Other").getValue(MMInfo.PROMPT);
		assertEquals("PROMPT for no locale not set correctly", "test prompt no locale",
				prompt);
	}

	public void testQuotedContentProperties() {
		String actual = getInfoStore("Other").getValue(MMInfo.PROMPT, Locale.FRENCH);
		String expected = "test prompt single quotest {[]}€!§$%&/()?`´'+;,:";
		assertEquals("quoted property content not set correctly", expected, actual);

		actual = getInfoStore("Other").getValue(MMInfo.PROMPT, Locale.ITALIAN);
		expected = "test tripple quotes \"{[]}€!§$%&/()?`´'+;,:#.'\"\"same line";
		assertEquals("quoted property content not set correctly", expected, actual);

		actual = getInfoStore("Other").getValue(MMInfo.PROMPT, Locale.GERMANY);
		expected = "\r\n\ttest tripple quotes start same line, end next line " +
				"\"{[]}€!§$%&/()?`´'+;,:#.'\"\" ";
		assertEquals("quoted property content not set correctly", expected, actual);

		actual = getChoice("Driving", "unsteady idle speed").getInfoStore().getValue(MMInfo.PROMPT,
				Locale.CHINESE);
		expected = "test tripple \"{[]}€!§$%&/()?`´'+;,:#.'\"\" quotes after empty " +
				"line, end line\r\nafter";
		assertEquals("quoted property content not set correctly", expected, actual);

		actual = getChoice("Driving", "insufficient power on partial load")
				.getInfoStore().getValue(MMInfo.PROMPT, Locale.US);
		expected = "\r\ntest tripple \"{[]}€!§$%&/()?`´'+;,:#.'\"\" quotes " +
				"start same line, end next \r\nline\r\n";
		assertEquals("quoted property content not set correctly", expected, actual);
	}

	public void testWildCardProperties() {
		String actual = getChoice("Idle speed system o.k.?", "Yes").getInfoStore().getValue(
				MMInfo.PROMPT,
				Locale.GERMAN);
		String expected = "Ja";
		assertEquals("Property for wild card question not set properly", expected, actual);

		actual = getChoice("SomeQuestion", "Yes").getInfoStore().getValue(MMInfo.PROMPT,
				Locale.GERMAN);
		expected = "Ja";
		assertEquals("Property for wild card question not set properly", expected, actual);

		actual = getChoice("SomeQuestion", "No").getInfoStore().getValue(MMInfo.PROMPT,
				Locale.GERMAN);
		assertNull("Unwanted property set with wild card", actual);
	}
}
