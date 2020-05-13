/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.compile.OntologyCompiler;

public interface NodeProvider<T extends Type> extends Type {

	Value getNode(OntologyCompiler core, Section<? extends T> section);

}
