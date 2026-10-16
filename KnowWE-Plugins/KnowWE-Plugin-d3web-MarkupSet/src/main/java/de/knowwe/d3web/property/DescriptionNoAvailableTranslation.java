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

package de.knowwe.d3web.property;

import java.util.Locale;

import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class DescriptionNoAvailableTranslation implements D3webCompileScript<DescriptionMarkup> {

	@Override
	public void compile(D3webCompiler compiler, Section<DescriptionMarkup> section) throws CompilerMessage {
		Section<DescriptionMarkup.DescriptionTextType> first = $(section).successor(DescriptionMarkup.DescriptionTextType.class).getFirst();
		if (first == null) return;

		Locale locale = DescriptionMarkup.getLocale(section);
		if (first.get().availableLocale(locale)) {
			Messages.clearMessages(compiler, section, this.getClass());
		} else {
			Messages.storeMessage(compiler, section, this.getClass(), Messages.warning("No available header translation for locale: " + locale.getLanguage()));
		}
	}
}
