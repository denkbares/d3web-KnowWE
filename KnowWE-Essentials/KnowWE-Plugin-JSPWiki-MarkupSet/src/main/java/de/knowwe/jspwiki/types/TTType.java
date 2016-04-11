/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * 
 * @author Stefan Plehn
 * @created 12.05.2011
 */
public class TTType extends AbstractType {

	public TTType() {
		this.setSectionFinder(new RegexSectionFinder("\\{\\{(.*?)\\}\\}"));

		this.addChildType(new KeywordType(Pattern.compile("^\\{\\{"), 0, true));
		this.addChildType(new KeywordType(Pattern.compile("\\}\\}$"), 0, true));

		this.addChildType(new BoldType());
		this.addChildType(new ItalicType());
		this.addChildType(new StrikeThroughType());
		this.addChildType(new LinkType());
		this.addChildType(new WikiTextType());
	}
}
