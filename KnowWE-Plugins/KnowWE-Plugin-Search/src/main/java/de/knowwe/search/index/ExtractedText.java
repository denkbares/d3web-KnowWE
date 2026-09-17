/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.search.index;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * What {@link KdomTextExtractor} gets out of a chunk: the readable prose that goes into the {@code body} field, and the
 * markup vocabulary that goes into the low weighted {@code markup} field.
 *
 * @param body         readable text without any wiki or markup syntax
 * @param markupTokens markup and annotation names in their written form, for example {@code %%Package} and
 *                     {@code @file}. The analyzer additionally emits them without the sigil, so both {@code %%Package}
 *                     and {@code Package} find the block.
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record ExtractedText(@NotNull String body, @NotNull Set<String> markupTokens) {

	public static final ExtractedText EMPTY = new ExtractedText("", Set.of());

	public boolean isEmpty() {
		return body.isBlank() && markupTokens.isEmpty();
	}
}
