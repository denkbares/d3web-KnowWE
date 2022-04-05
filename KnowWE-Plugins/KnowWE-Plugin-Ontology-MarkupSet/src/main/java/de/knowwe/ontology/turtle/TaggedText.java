/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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

package de.knowwe.ontology.turtle;

import java.util.Locale;

import com.denkbares.strings.Strings;
import com.denkbares.strings.Text;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.basicType.UnrecognizedSyntaxType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.LeftOfTokenFinder;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.LineSectionFinder;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Section type that represents a (optionally) language-tagged text literal.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 17.08.2018
 */
public class TaggedText extends AbstractType {

	public TaggedText() {
		super(LineSectionFinder.getInstance());
		addChildType(new StringType());
		addChildType(new LocaleType("@"));
		addChildType(new KeywordType("@"));
		addChildType(UnrecognizedSyntaxType.getInstance());
	}

	public Text getTaggedText(Section<? extends TaggedText> section) {
		Locale lang = $(section).successor(LocaleType.class).map(LocaleType::getLocale).findFirst().orElse(Locale.ROOT);
		String text = $(section).successor(StringType.class).map(StringType::getUnquoted).findFirst().orElse("");
		return new Text(text, lang);
	}

	private static class StringType extends AbstractType {
		public StringType() {
			super(new LeftOfTokenFinder("@"));
			setRenderer(StyleRenderer.PROMPT);
		}

		public String getUnquoted(Section<StringType> section) {
			String text = section.getText();
			if (text.length() >= 6 && text.startsWith("\"\"\"") && text.endsWith("\"\"\"")) {
				text = text.substring(3, text.length() - 3);
			}
			return Strings.unquote(text);
		}
	}
}
