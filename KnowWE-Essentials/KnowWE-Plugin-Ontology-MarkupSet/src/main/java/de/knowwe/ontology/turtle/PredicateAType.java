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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.OneOfStringFinderExact;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.vocabulary.RDF;

/**
 * @author Jochen Reutelshöfer
 * @created 18.06.2014
 */
public class PredicateAType extends AbstractType implements NodeProvider<TurtleLiteralType> {

    public static final String a = "a";

    public PredicateAType() {
        this.setSectionFinder(new OneOfStringFinderExact(a));
        this.setRenderer(StyleRenderer.KEYWORDS);
    }

    @Override
    public Node getNode(Section<TurtleLiteralType> section, Rdf2GoCompiler core) {
        return RDF.type;
    }
}
