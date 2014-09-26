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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.XSD;

/**
 * @author Jochen Reutelshöfer
 * @created 18.06.2014
 */
public class NumberLiteral extends de.knowwe.core.kdom.basicType.Number implements NodeProvider<TurtleLiteralType> {

    public NumberLiteral() {
        super();
    }

    @Override
    public Node getNode(Section<TurtleLiteralType> section, Rdf2GoCompiler core) {
        /*
        The SectionFinder already assures that it actually is a valid number
         */
        String text = section.getText();
        boolean isInteger = false;
        try {
            Integer.parseInt(text);
            isInteger = true;

        } catch (Exception e) {
            // nothing to do
        }

        URI dataType = XSD._double;
        if (isInteger) {
            dataType = XSD._integer;
        }
        return core.getRdf2GoCore().createLiteral(section.getText(), dataType);
    }
}
