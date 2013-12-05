package de.knowwe.ontology.turtlePimped.compile;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCore;

public interface StatementProvider<T extends Type> extends Type {

	StatementProviderResult getStatements(Section<T> section, Rdf2GoCore core, Article article);
}
