/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.ontology.turtle;

import java.util.List;
import java.util.stream.Collectors;

import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;

public class PredicateSentence extends AbstractType {

	public PredicateSentence() {
		this.setSectionFinder(new PredicateSentenceSectionFinder());
		this.addCompileScript(Priority.LOW, new ObjectExistingChecker());
		this.addChildType(new Predicate());
		this.addChildType(new ObjectList());

	}

	static class PredicateSentenceSectionFinder implements SectionFinder {


		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<StringFragment> sentences = Strings.splitUnquoted(text, ";", true,
					TurtleMarkup.TURTLE_QUOTES);
			final List<SectionFinderResult> sectionFinderResults = SectionFinderResult.resultList(sentences);
			// filter out matches of length 0
			return sectionFinderResults.stream().filter(finding -> finding.getStart() != finding.getEnd()).collect(Collectors.toList());
		}

	}

	private static class ObjectExistingChecker extends OntologyCompileScript<PredicateObjectSentenceList> {
		@Override
		public void compile(OntologyCompiler compiler, Section<PredicateObjectSentenceList> section) throws CompilerMessage {
			if(Sections.successors(section, Object.class).isEmpty()) {
				throw new CompilerMessage(new Message(Message.Type.ERROR, "No objects found in this turtle predicate sentence."));
			}
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<PredicateObjectSentenceList> section) {
			// nothing to do
		}
	}
}
