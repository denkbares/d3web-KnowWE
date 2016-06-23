package de.knowwe.core.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.d3web.strings.Strings;
import de.d3web.utils.Streams;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Returns the text of a specified section.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.06.16
 */
public class GetSectionTextAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionText = null;
		String title = context.getParameter("title");
		String name = context.getParameter("name");

		if (context.getParameter(Attributes.SECTION_ID) != null) {
			Section<?> section = getSection(context);
			sectionText = section.getText();
			name = section.getID();
		}
		else if (title != null && name != null) {
			final String finalName = name;
			Section<DefaultMarkupType> namedSection = $(context.getArticleManager()
					.getArticle(title)).successor(DefaultMarkupType.class)
					.filter(markupSection -> finalName.equals(DefaultMarkupType.getAnnotation(markupSection, "name")))
					.getFirst();
			if (namedSection != null && KnowWEUtils.canView(namedSection, context)) {
				sectionText = namedSection.getText();
			}
		}

		if (sectionText != null) {
			File tempTextFile = File.createTempFile(name, "_SectionText.txt");
			try {
				Strings.writeFile(tempTextFile.getPath(), sectionText);

				context.setContentType("application/x-bin");
				context.setHeader("Content-Disposition", "attachment;filename=\"" + name + ".txt\"");

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
}
