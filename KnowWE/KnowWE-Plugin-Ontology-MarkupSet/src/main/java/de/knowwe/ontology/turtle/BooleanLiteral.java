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
import de.knowwe.kdom.sectionFinder.OneOfStringFinder;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.vocabulary.XSD;

/**
 * @author Jochen Reutelshöfer
 * @created 18.06.2014
 */
public class BooleanLiteral extends AbstractType implements NodeProvider<TurtleLiteralType> {


    public BooleanLiteral() {
        this.setSectionFinder(new OneOfStringFinder("true", "false"));
        this.setRenderer(StyleRenderer.CHOICE);
    }

    @Override
    public Node getNode(Section<TurtleLiteralType> section, Rdf2GoCompiler core) {
        return core.getRdf2GoCore().createLiteral(section.getText(), XSD._boolean);
    }
}
