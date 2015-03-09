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

import de.d3web.strings.Strings;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.rendering.AnchorRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.ontology.turtle.compile.TurtleCompileHandler;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 09.03.15.
 */
public class TurtleSPOSentence extends AbstractType {


    public TurtleSPOSentence() {
        this.setSectionFinder(new AllTextFinderTrimmed());

        this.addChildType(new Subject());
        this.addChildType(PredicateObjectSentenceList.getInstance());


    }

}
