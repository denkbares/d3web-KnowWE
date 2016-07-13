package de.knowwe.core.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Streams;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Returns the text of a specified section as a text file.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.06.16
 */
public class GetSectionTextAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionText = null;
		String title = context.getParameter("title");
		if (title == null) title = context.getParameter("article");
		final String name = context.getParameter("name");
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		String fileName = null;

		Section<?> referencedSection = null;
		if (sectionId != null) {
			referencedSection = Sections.get(sectionId);
			fileName = referencedSection.getID();
		}
		else if (title != null && name != null) {
			referencedSection = $(context.getArticleManager()
					.getArticle(title)).successor(DefaultMarkupType.class)
					.filter(markupSection -> name.equals(DefaultMarkupType.getAnnotation(markupSection, "name")))
					.getFirst();
			fileName = name;
		}

		if (referencedSection != null) {
			if (KnowWEUtils.canView(referencedSection, context)) {
				sectionText = referencedSection.getText();
			}
			else {
				context.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to view/download section");
				return;
			}
		}
		else {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No valid section id or title and name given");
			return;
		}

		File tempTextFile = File.createTempFile(name, "_SectionText.txt");
		try {
			Strings.writeFile(tempTextFile.getPath(), sectionText);

			context.setContentType("application/x-bin");
			context.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + ".txt\"");

			FileInputStream in = new FileInputStream(tempTextFile);
			OutputStream out = context.getOutputStream();
			Streams.streamAndClose(in, out);
		}
		finally {
			tempTextFile.delete();
			tempTextFile.deleteOnExit();
		}
	}
}
