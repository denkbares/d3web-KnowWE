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

package de.knowwe.ontology.turtle;

import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.kdom.resource.ResourceReference;

import java.util.List;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 05.05.15.
 */
public class SubjectURIWithDefinition extends TurtleURI {


    public SubjectURIWithDefinition() {
        SimpleReference reference = Types.successor(this, ResourceReference.class);
        reference.addCompileScript(Priority.HIGH, new SubjectPredicateKeywordDefinitionHandler(new String[]{"^" + PredicateAType.a + "$", "[\\w]*?:?type", "[\\w]*?:?subClassOf",  "[\\w]*?:?isA", "[\\w]*?:?subPropertyOf"}));

    }

    class SubjectPredicateKeywordDefinitionHandler extends PredicateKeywordDefinitionHandler {

        public SubjectPredicateKeywordDefinitionHandler(String[] matchExpressions) {
            super(matchExpressions);
        }

        @Override
        protected List<Section<Predicate>> getPredicates(Section<SimpleReference> s) {
            // finds all predicates of the turtle sentence
            Section<TurtleSentence> sentence = Sections.ancestor(s, TurtleSentence.class);
            return Sections.successors(sentence, Predicate.class);
        }
    }
}
