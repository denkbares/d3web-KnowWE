/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.d3web.correction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.wcohen.ss.Levenstein;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.tools.CustomTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * This ToolProvider provides quick fixes for correcting small mistakes (typos)
 * in term references.
 *  
 * @author Alex Legler
 * @created 19.12.2010 
 */
public class CorrectionToolProvider implements ToolProvider {

	/**
	 * The CorrectionTool is a {@link CustomTool} that renders a Tool displaying
	 * suggestions for correcting a typo.
	 * 
	 * @author Alex Legler
	 * @created 23.12.2010
	 */
	public class CorrectionTool extends CustomTool {

		private Section<?> section;
		private KnowWEArticle article;
		private List<String> suggestions = null;
		
		/**
		 * The max Levensthein distance for finding matches.
		 */
		private static final int THRESH = 5;
		
		public CorrectionTool(KnowWEArticle article, Section<?> section) {
			this.article = article;
			this.section = section;
		}
		
		@Override
		public String render() {
			StringBuilder buffy = new StringBuilder();
			
			if (suggestions == null) {
				suggestions = getSuggestions(article, section);
			}
			
			if (suggestions == null || suggestions.size() == 0) {
				return null;
			}
			
			buffy.append("<img src=\"KnowWEExtension/images/quickfix.gif\" alt=\"Quick Fix\" />&nbsp;");
			buffy.append(KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.Correction.do"));
			buffy.append("<div class=\"correction-suggestions\">");
			
			for (String suggestion : getSuggestions(article, section)) {
				buffy.append("<a href=\"javascript://\" class=\"markupMenuItem\" ");
				buffy.append("onclick=\"KNOWWE.plugin.correction.doCorrection('").append(section.getID()).append("', '").append(suggestion).append("');\">");
				buffy.append("<img src=\"KnowWEExtension/images/correction_change.gif\" alt=\"Correct to\" /> ").append(suggestion).append("</a>");
			}
			
			buffy.append("</div>");
			return buffy.toString();
		}
		
		/**
		 * Creates a Javascript array containing all suggestions applicable to a Section.
		 * 
		 * @param s The section to fecth corrections for
		 * @return ['suggestion1', 'suggestion2', ...]
		 */
		@SuppressWarnings("unchecked")
		private List<String> getSuggestions(KnowWEArticle article, Section<?> s) {
			if (!(s.get() instanceof TermReference)) {
				return null;
			}
			
			if (!s.hasErrorInSubtree(article)) {
				return null;
			}
			
			TerminologyHandler terminologyHandler = KnowWEUtils.getTerminologyHandler(KnowWEEnvironment.DEFAULT_WEB);
			TermReference<?> termReference = ((TermReference<?>) s.get());		
			
			Collection<String> localTermMatches = terminologyHandler.getAllLocalTermsOfType(
					s.getArticle().getTitle(), 
					termReference.getTermObjectClass()
			);

			String originalText = s.getOriginalText();
			List<String> suggestions = new LinkedList<String>();
			Levenstein l = new Levenstein();
			
			for (String match : localTermMatches) {
				String[] parts = match.split(" ");
				
				String name = parts.length == 1 ? parts[0] : parts[1];
				String type = parts.length == 2 ? parts[0] : null;
				
				if (l.score(originalText, name) >= -THRESH) {
					// Special case: AnswerReference: Also check that the defining Question matches
					if (termReference instanceof AnswerReference) {
						AnswerReference answerReference = (AnswerReference) termReference;
						String question = answerReference.getQuestionSection((Section<? extends AnswerReference>) s).getOriginalText();
						
						if (!question.equals(type)) {
							continue;
						}
					}
					
					String item = name;
					
					// not yet needed
					//if (type != null) {
					//	item += ";type:" + type;
					//}
					
					suggestions.add(item);
				}
			}
			
			return suggestions;
		}

		@Override
		public boolean hasContent() {
			if (suggestions == null) {
				suggestions = getSuggestions(article, section);
			}
			
			return !(suggestions == null || suggestions.size() == 0);
		}
	}

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		Tool correction = getCorrectionTool(article, section, userContext);
		return new Tool[] { correction };
	}
	
	protected Tool getCorrectionTool(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		return new CorrectionTool(article, section);
	}
}
