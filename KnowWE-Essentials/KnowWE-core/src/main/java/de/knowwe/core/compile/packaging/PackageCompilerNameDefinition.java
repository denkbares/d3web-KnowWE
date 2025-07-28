/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile.packaging;

import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.objects.TermDefinition;

/**
 * Type to define and register the name of a package compiler via #getTermName
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.07.2025
 */
public interface PackageCompilerNameDefinition extends TermDefinition, RenamableTerm {
}
