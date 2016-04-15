package de.knowwe.ontology.turtle.compile;

import org.openrdf.model.Value;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public interface NodeProvider<T extends Type> extends Type {

	Value getNode(Section<T> section, Rdf2GoCompiler core);

}
