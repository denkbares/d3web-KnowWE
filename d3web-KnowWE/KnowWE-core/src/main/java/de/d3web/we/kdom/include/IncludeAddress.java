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

package de.d3web.we.kdom.include;

import de.d3web.we.kdom.SectionID;


/**
 * @author astriffler
 *
 */
public class IncludeAddress {
	
	private String targetArticle;
	
	private String targetSection;
	
	private String originalAddress;
	
	private boolean isContent;
	
	private boolean isWildcard;
	
	public IncludeAddress(String src) {
		this.originalAddress = src;
		if (src != null) {
			if (src.contains(SectionID.SEPARATOR)) {
				this.targetArticle = src.substring(0, src.indexOf(SectionID.SEPARATOR));
				this.isWildcard = src.endsWith("*");
				if (this.isWildcard) {
					this.targetSection = src.substring(src.lastIndexOf(SectionID.SEPARATOR) + 1, 
							src.length() - 1).trim();
					this.isContent = src.matches(".*?" + SectionID.CONTENT_SUFFIX + " *\\*");
				} else {
					this.targetSection = src.substring(src.lastIndexOf(SectionID.SEPARATOR) + 1);
					this.isContent = src.endsWith(SectionID.CONTENT_SUFFIX);
				}
				
			} else {
				this.targetArticle = src;
			}
		}
	}
	
	public String getOriginalAddress() {
		return originalAddress;
	}

	public String getTargetArticle() {
		return targetArticle;
	}

	public String getTargetSection() {
		return targetSection;
	}

	public boolean isContentSectionTarget() {
		return isContent;
	}
	
	public boolean isWildcardSectionTarget() {
		return isWildcard;
	}

}