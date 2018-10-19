/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile.provider;

import org.eclipse.rdf4j.model.Value;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.rdf2go.Rdf2GoCompiler;

public interface NodeProvider<T extends Type> extends Type {

	Value getNode(Section<? extends T> section, Rdf2GoCompiler core);

}
