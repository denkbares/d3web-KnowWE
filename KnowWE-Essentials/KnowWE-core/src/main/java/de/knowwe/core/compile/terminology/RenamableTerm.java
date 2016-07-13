/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.terminology;

import com.denkbares.strings.Identifier;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.TermUtils;
import de.knowwe.core.kdom.parsing.Section;

/**
 * 
 * This interface provides functionality for renaming terms. Therefore you have
 * to implement it, if you want renaming to work. It's is possible to specify
 * how a renaming action affects the corresponding section. If it is the
 * simplest case and replacement text and section text are the same, you can
 * just return the replacement text.
 * 
 * 
 * @author Stefan Plehn
 * @created 22.05.2013
 */
public interface RenamableTerm extends Type {

	default String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		return TermUtils.quoteIfRequired(newIdentifier.getLastPathElement());
	}

}
