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

package de.d3web.we.kdom;


public class RenameFinding implements Comparable{
	
	private Section sec;
	private String context;
	private int start;
	
	public static final int CONTEXT_SIZE_SMALL = 15;

	/**
	 * <p>Constructor.</p>
	 * @param begin
	 * @param con
	 * @param sec
	 */
	public RenameFinding(int begin, String con, Section sec) {
		this.sec = sec;
		this.context = con;
		this.start = begin;
	}

	/**
	 * <p>Returns the context in witch the query string has been found.</p>
	 * @param start
	 * @param text
	 * @return
	 */
	public static String getContext(int start, Section sec, String articletext, int findingLength) {
		int startIndex = start+ sec.getAbsolutePositionStartInArticle() - CONTEXT_SIZE_SMALL;
		if (startIndex < 0)
			startIndex = 0;

		int endIndex = findingLength + start +  sec.getAbsolutePositionStartInArticle() +  CONTEXT_SIZE_SMALL;
		if (endIndex >= articletext.length())
			endIndex = articletext.length();
		
		String result = articletext.substring(startIndex, endIndex );
		
		return result;
	}
	
	/**
	 * Returns an additional text that is wrapped around the finding so the user
	 * can easily arrange the finding.
	 *  
	 * @param pos          start position of the finding
	 * @param direction    text [p]revious or [a]fter the finding
	 * @param curChars     current length of the additional text
	 * @param queryLength  length of the search text
	 * @param text         text of the section/article
	 * @return
	 */
	public static String getAdditionalContext(int pos, String direction,
			int curChars, int queryLength, String text){
		
		if(curChars == 0)
			return "";
		
		final int MAX_CHARS = 100;
		
		//set length of the new text
		if(curChars + CONTEXT_SIZE_SMALL <= MAX_CHARS){
			curChars += CONTEXT_SIZE_SMALL;
		}else{
			curChars = MAX_CHARS;
		}
		
		int index = 0;
		
		//handle end and beginning of the text
		if(direction.equals("p")){
			index = pos - CONTEXT_SIZE_SMALL - curChars;
			if(index < 0){
				index = 0;
			}
			if ((pos - CONTEXT_SIZE_SMALL) < 0)
				return "";
				
			return text.substring(index, pos - CONTEXT_SIZE_SMALL);
		} else if(direction.equals("a")){
			index = pos + queryLength + CONTEXT_SIZE_SMALL + curChars;
			if(index >= text.length()){
				index = text.length();
			}
			int start = pos + queryLength + CONTEXT_SIZE_SMALL;
			if( start > text.length() )
				start = index;
			return text.substring(start, index);
		}
		return "";
	}

	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof RenameFinding) {
			RenameFinding other = ((RenameFinding)arg0);
			if(other.sec.equals(this.sec)) {
				if(this.start > other.start) {
					return 1;
				}else {
					return -1;
				}
			}
			
		}
		return 0;
	}
	
	public String contextText() {
		return context;
	}

	public Section getSec() {
		return sec;
	}

	public int getStart() {
		return start;
	}
}
