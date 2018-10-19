/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom.objectproperty;

import de.knowwe.core.kdom.Types;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

public class AbbreviatedPropertyReference extends AbbreviatedResourceReference {

	public AbbreviatedPropertyReference() {
		super(Property.class);
		Types.injectRendererToChildren(this, ResourceReference.class,
				new ToolMenuDecoratingRenderer(StyleRenderer.CHOICE));
	}

	public String getProperty(Section<? extends AbbreviatedPropertyReference> section) {
		return super.getResource(section);
	}

	public org.eclipse.rdf4j.model.URI getPropertyURI(Rdf2GoCore core, Section<? extends AbbreviatedPropertyReference> section) {
		return super.getResourceURI(core, section);
	}

}
