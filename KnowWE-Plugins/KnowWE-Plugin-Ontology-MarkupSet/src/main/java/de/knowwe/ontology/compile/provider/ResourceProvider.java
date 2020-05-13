/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.eclipse.rdf4j.model.Resource;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.compile.OntologyCompiler;

public interface ResourceProvider<T extends Type> extends NodeProvider<T> {

	Resource getResource(OntologyCompiler compiler, Section<? extends T> section);
}
