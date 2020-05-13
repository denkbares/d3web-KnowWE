/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.eclipse.rdf4j.model.IRI;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.compile.OntologyCompiler;

public interface URIProvider<T extends Type> extends NodeProvider<T> {

	IRI getIRI(OntologyCompiler core, Section<T> section);
}
