/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.ontology.kdom.table;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.*;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableType;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtle.*;

import java.util.List;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 05.05.15.
 */
public class TableSubjectURIWithDefinition  extends TurtleURI {


    public TableSubjectURIWithDefinition() {
        SimpleReference reference = Types.successor(this, ResourceReference.class);
        reference.addCompileScript(Priority.HIGH, new SubjectPredicateKeywordDefinitionHandler(new String[]{"^" + PredicateAType.a + "$", "[\\w]*?:?type", "[\\w]*?:?subClassOf",  "[\\w]*?:?isA", "[\\w]*?:?subPropertyOf"}));
        reference.addCompileScript(Priority.HIGH, new SubjectColumnHeaderDefinitionHandler());

    }

    class SubjectPredicateKeywordDefinitionHandler extends PredicateKeywordDefinitionHandler {

        public SubjectPredicateKeywordDefinitionHandler(String[] matchExpressions) {
            super(matchExpressions);
        }

        @Override
        protected List<Section<Predicate>> getPredicates(Section<SimpleReference> s) {
            final Section<OntologyTableMarkup> markupSection = Sections.ancestor(s, OntologyTableMarkup.class);
            return Sections.successors(markupSection, Predicate.class);
        }
    }

	/**
	 * In the first row (subject row) the subjects can be defined to be instance of a class specified in the header cell of the first column.
	 *
	 */
	private class SubjectColumnHeaderDefinitionHandler  extends OntologyCompileScript<SimpleReference> {

		@Override
		public void compile(OntologyCompiler compiler, Section<SimpleReference> section) throws CompilerMessage {

			Section<TableCellContent> cell = Sections.ancestor(section, TableCellContent.class);
			Section<TableCellContent> rowHeaderCell = TableUtils.getColumnHeader(cell);
			Section<OntologyTableMarkup.BasicURIType> colHeaderConceptReference = Sections.successor(rowHeaderCell, OntologyTableMarkup.BasicURIType.class);
			if(colHeaderConceptReference == null) {
				// no definition intended
				return;
			}
			Class<?> termClass = Resource.class;
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			terminologyManager.registerTermDefinition(compiler, section, termClass, section.get().getTermIdentifier(section));
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<SimpleReference> s) {
			compiler.getTerminologyManager().unregisterTermDefinition(compiler, s,
					s.get().getTermObjectClass(s), s.get().getTermIdentifier(s));
		}
	}
}
