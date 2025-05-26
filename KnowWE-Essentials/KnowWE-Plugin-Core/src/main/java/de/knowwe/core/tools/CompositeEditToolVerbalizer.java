/*
 * Copyright (C) 2025 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core.tools;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Type;

/**
 * Simple interface a Type can implement to specify the "Show Info" tool text of an identifier it registers/defines in
 * the TerminologyManager
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 26.05.2025
 */
public interface CompositeEditToolVerbalizer extends Type {

	default boolean showToolForIdentifier(TermCompiler compiler, Identifier identifier) {
		return true;
	}

	default String getCompositeEditToolText(TermCompiler compiler, Identifier identifier) {
		return CompositeEditToolProvider.createToolText(identifier.toPrettyPrint());
	}
}
