/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.openrdf.model.URI;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public interface URIProvider<T extends Type> extends NodeProvider<T> {

	URI getURI(Section<T> section, Rdf2GoCompiler core);
}
