package de.knowwe.ontology.turtle.compile;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public interface StatementProvider<T extends Type> extends Type {

	StatementProviderResult getStatements(Section<? extends T> section, Rdf2GoCompiler core);
}
