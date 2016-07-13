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

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

import java.util.Collections;
import java.util.List;

/**
 * Usually a turtle sentence consists of at least a subject, a predicate, and an object.
 * However, according to the official specification also top-level blank nodes are allowed, for example: [ rdf:type rdf:Resource].
 * This type implements this case, allowing 'bare blank nodes' on top-level.
 *
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 09.03.15.
 */
public class BareBlankNodeSentence extends AbstractType {

    public BareBlankNodeSentence() {
        this.setSectionFinder(new BareBlankNodeFinder());

        this.addChildType(new BlankNode());

    }

    private class BareBlankNodeFinder implements SectionFinder {

        @Override
        public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
            final String trimmed = text.trim();
            if(trimmed.indexOf("[") == 0 && Strings.indexOfClosingBracket(trimmed, 0, '[' , ']')==trimmed.length()-1){
                return new AllTextFinderTrimmed().lookForSections(text, father, type);
            }
            return Collections.emptyList();
        }
    }
}
