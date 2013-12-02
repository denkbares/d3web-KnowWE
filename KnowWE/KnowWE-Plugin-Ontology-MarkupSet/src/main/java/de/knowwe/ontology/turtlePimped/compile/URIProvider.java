package de.knowwe.ontology.turtlePimped.compile;


import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCore;

public interface URIProvider<T extends Type> extends NodeProvider<T> {

	URI getURI(Section<T> section, Rdf2GoCore core);
}
