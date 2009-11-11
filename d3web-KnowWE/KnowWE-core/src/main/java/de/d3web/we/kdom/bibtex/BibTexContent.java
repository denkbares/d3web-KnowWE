/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.bibtex;

import java.io.IOException;
import java.util.List;

import org.bibsonomy.bibtex.parser.SimpleBibTeXParser;

import bibtex.parser.ParseException;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.utils.KnowWEUtils;

public class BibTexContent extends XMLContent {

	public static final String PARSEEXCEPTION = "PARSEEXCEPTION";
	public static final String IOEXCEPTION = "IOEXCEPTION";
	public static final String BIBTEXs = "BIBTEXs";

	@Override
	protected void init() {
		this.setCustomRenderer(new BibTexContentRenderer());

	}

	@Override
	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		String text = s.getOriginalText();
		List<org.bibsonomy.model.BibTex> bts=null;
		SimpleBibTeXParser simpleparser = new SimpleBibTeXParser();
		try {
			bts = simpleparser
					.parseBibTeXs(text);
		} catch (ParseException e) {
			KnowWEUtils.storeSectionInfo(s, PARSEEXCEPTION, e.toString());
		} catch (IOException e) {
			KnowWEUtils.storeSectionInfo(s, IOEXCEPTION, e.toString());
		}
		
		if (bts!=null){
			KnowWEUtils.storeSectionInfo(s, BIBTEXs, bts);
		}
		//TODO: generate owl statements from bts
		return io;
	}

}
