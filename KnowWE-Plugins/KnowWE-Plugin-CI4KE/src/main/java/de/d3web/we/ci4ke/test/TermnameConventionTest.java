/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.d3web.we.ci4ke.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.denkbares.strings.NumberAwareComparator;
import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.TestParameter.Mode;
import de.d3web.testing.TestParameter.Type;
import de.d3web.testing.TestingUtils;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * A test that checks all term identifiers used in an article against a specified regular expression to allow
 * enforcement of simple naming conventions.
 *
 * @author Jochen Reutelshöfer
 * @created 08.08.2012
 */
public class TermnameConventionTest extends AbstractTest<Article> {

	public TermnameConventionTest() {
		this.addParameter("Naming convention pattern", Type.Regex, Mode.Mandatory,
				"A regular expression for the desired naming convention. All terms are checked against this expression.");
	}

	@Override
	public Message execute(Article testObject, String[] args, String[]... ignores) throws InterruptedException {
		String regex = args[0];
		Pattern pattern = Pattern.compile(regex);
		List<Section<TermDefinition>> terms = getTermSections(testObject);
		Set<String> violations = new HashSet<>();

		// duplicates
		for (Section<TermDefinition> section : terms) {
			String termName = section.get().getTermName(section);
			if (!pattern.matcher(termName).matches()) {
				violations.add(termName);
				setSectionError(regex, termName, section);
			}
			else {
				clearSectionError(regex, termName, section);
			}
			TestingUtils.checkInterrupt();
		}

		return violations.isEmpty() ? new Message(Message.Type.SUCCESS) : createError(regex, violations);
	}

	protected void setSectionError(String pattern, String termName, Section<TermDefinition> section) {
		Messages.storeMessage(section, getClass(), Messages.warning(String.format(
				"The name '%s' does not comply to the naming convention pattern '%s'.", termName, pattern)));
	}

	protected void clearSectionError(String pattern, String termName, Section<TermDefinition> section) {
		Messages.clearMessages(section, getClass());
	}

	protected Message createError(String pattern, Set<String> violations) {
		// create error message for the build report
		StringBuilder result = new StringBuilder();
		result.append("The following terms do not comply to the specified naming convention pattern (")
				.append(KnowWEUtils.maskJSPWikiMarkup(pattern)).append("):");
		violations.stream().sorted(NumberAwareComparator.CASE_INSENSITIVE)
				.forEach(name -> result.append("\n* ").append(KnowWEUtils.maskJSPWikiMarkup(name)));
		return new Message(Message.Type.FAILURE, result.toString());
	}

	protected List<Section<TermDefinition>> getTermSections(Article testObject) {
		Section<RootType> rootSection = testObject.getRootSection();
		return Sections.successors(rootSection, TermDefinition.class);
	}

	@Override
	public Class<Article> getTestObjectClass() {
		return Article.class;
	}

	@Override
	public String getDescription() {
		return "Tests whether the objects names used on this article comply to the specified naming convention pattern.";
	}
}
