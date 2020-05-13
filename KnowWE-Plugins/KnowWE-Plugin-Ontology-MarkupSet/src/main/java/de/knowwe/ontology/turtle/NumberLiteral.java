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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.provider.NodeProvider;

/**
 * @author Jochen Reutelshöfer
 * @created 18.06.2014
 */
public class NumberLiteral extends de.knowwe.core.kdom.basicType.Number implements NodeProvider<TurtleLiteralType> {

    public NumberLiteral() {
        super();
    }

    @Override
    public Value getNode(OntologyCompiler core, Section<? extends TurtleLiteralType> section) {
        /*
        The SectionFinder already assures that it actually is a valid number
         */
        String text = section.getText();
        boolean isInteger = false;
        try {
            Integer.parseInt(text);
            isInteger = true;

        } catch (Exception ignore) {
            // nothing to do
        }

        IRI dataType = XMLSchema.DOUBLE;
        if (isInteger) {
            dataType = XMLSchema.INTEGER;
        }
        return core.getRdf2GoCore().createLiteral(section.getText(), dataType);
    }
}
