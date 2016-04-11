package de.knowwe.include;

/*
 * Copyright (C) 2012 denkbares GmbH
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

import java.util.regex.Pattern;

import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Markup to include several referenced articles or header sections (and their
 * contents) into the wiki page. Inside the markup each non-empty line specifies
 * an included section or article. There are two types of includes, a
 * "simple include" and a "normalized header include". To include an article or
 * header, use the normal wiki-like syntax, such as e.g. "[article]",
 * "[article#header]" or "[displayed name | article]".
 * <p>
 * <b>Simple Include</b>
 * <p>
 * To include a article, use the article name as <code>article name</code>. The
 * whole content of the article will be displayed. To include a header section
 * use <code>article name#header title</code>. There will be the header section
 * included, as well as all the sections naturally belongs to the header (all
 * sections until a header with the same level or a higher level will follow).
 * <p>
 * Using simple includes (and only there), you can also skip the brackets
 * "[...]" (for backward compatibility), because we know that each line contains
 * a link.
 * 
 * <pre>
 * %%Include
 *   [my full article]               // Includes the content of the full article (with no additional header)
 *   my other article                // Includes the content of the full article (with no additional header)
 *   [my partial article#my header]  // Includes header "my header" plus the sections belonging to the header 
 * %
 * </pre>
 * <p>
 * <b>Normalized Header Include</b>
 * <p>
 * You can also include a section using header normalization. Here you precede
 * every include by one or more enumeration characters ("#", "*", or "-").
 * Include declaration will be used as a synthetic header. The level of the
 * header will be the highest with one enumeration character (like page
 * headings); for two enumeration characters there will be a heading of similar
 * to "!!!", for tree characters "!!" and so on.
 * <p>
 * The enumeration icons have different meanings. "#" is the usual one. It
 * creates headings and allows the headings style to be enumerated (if defined
 * in the corresponding style). Using "*" suppresses the enumeration of the
 * headings, for the specified heading and all headings included. Using the "-"
 * will suppress the heading that is included (but not the headings with deeper
 * level that are also included by the statement.
 * <p>
 * If you are defining an include line with an empty link, only the heading is
 * created without including anything.
 * 
 * <pre>
 * %%Include
 *   * [Status]                     // includes article Status suppressing header numbering
 *   # [Preface | my full article]  // includes article with header "Preface"
 *   # [About |]                    // creates top-level header without including anything
 *   # About                        // identical to the line above
 *   ## [About the Project]         // includes article as sub-heading section 
 *   ## [About this Document]       // includes an other article as sub-heading section
 * %
 * </pre>
 * 
 * @author Benedikt Kaemmerer, Volker Belli
 * @created 06.07.2012
 */
public class IncludeMarkup extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	public static final String MARKUP_NAME = "Include";

	public static final String ANNOTATION_ZOOM = "zoom";
	public static final String ANNOTATION_FRAME = "frame";
	public static final String ANNOTATION_DEFINITION = "definition";

	public static final String ANNOTATION_TEMPLATE = "template";
	public static final String ANNOTATION_AUTHOR = "author";
	public static final String ANNOTATION_PROJECT = "project";
	public static final String ANNOTATION_TITLE = "title";
	public static final String ANNOTATION_VERSION = "version";

	static {
		m = new DefaultMarkup(MARKUP_NAME);
		m.addContentType(new CommentLineType());
		m.addContentType(new InnerWikiReference());
		m.addAnnotation(ANNOTATION_DEFINITION, false, "hide", "show");
		m.addAnnotation(ANNOTATION_FRAME, false, "hide", "show");
		m.addAnnotation(ANNOTATION_ZOOM, false);
		m.addAnnotation(ANNOTATION_TITLE, false);
		m.addAnnotation(ANNOTATION_PROJECT, false);
		m.addAnnotation(ANNOTATION_AUTHOR, false);
		m.addAnnotation(ANNOTATION_TEMPLATE, false);
		m.addAnnotation(ANNOTATION_VERSION, false, Pattern.compile("\\d+(\\.\\d+)*"));
		m.addAnnotationContentType(ANNOTATION_TEMPLATE, new AttachmentType());
	}

	public IncludeMarkup() {
		super(m);
		this.setRenderer(new IncludeRenderer());
	}
}