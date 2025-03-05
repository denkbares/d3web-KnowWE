package de.knowwe.upload;

import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * ToolProvider that for each ZIP Attachment provides a button to deploy the zip content as the new wiki content.
 * CAUTION: CURRENT CONTENT WILL BE OVERRIDDEN !
 */
public class DeployWikiZIPProvider implements ToolProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeployWikiZIPProvider.class);

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		String articleName = section.getTitle();
		List<WikiAttachment> attachmentsZips = getWikiAttachments(articleName);

		Tool[] result = new Tool[attachmentsZips.size()];
		for (int i = 0; i < attachmentsZips.size(); i++) {
			result[i] = createTool(attachmentsZips.get(i), section);
		}

		return result;
	}

	private static @NotNull List<WikiAttachment> getWikiAttachments(String articleName) {
		List<WikiAttachment> attachmentsZips;
		try {
			List<WikiAttachment> attachments = Environment.getInstance()
					.getWikiConnector()
					.getRootAttachments(articleName);
			attachmentsZips = attachments
					.stream()
					.filter(att -> att.getFileName().endsWith("zip"))
					.toList();
		}
		catch (IOException e) {
			LOGGER.error("Could not retrieve attachments list for page: " + articleName);
			throw new RuntimeException(e);
		}
		return attachmentsZips;
	}

	private Tool createTool(WikiAttachment wikiAttachment, Section<?> section) {
		String message = "ACHTUNG: Sie sind dabei den aktuellen Wiki-Inhalt mit dem Inhalt der Datei "+wikiAttachment.getFileName()+" zu überschreiben. Der aktuelle Wikiinhalt geht dabei vollständig verloren. Nutzen Sie ggf. erst den Download-Knopf um den aktuellen Stand zu sichern. \n" +
				"Möchten Sie fortfahren und den Inhalt überschreiben?";
		String jsAction = "const userConfirmed = confirm('"+message+"');" +
				"if (userConfirmed) {" +
				"	window.location = 'action/"+ DeployWikiContentZIPAction.class.getSimpleName()+"?" + Attributes.ATTACHMENT_NAME + "=" + wikiAttachment.getFileName() + "&amp;" + Attributes.TOPIC + "=" + section.getTitle() + "'; " +
				"}";

		return new DefaultTool(
				Icon.BOLT,
				"Deploy " + wikiAttachment.getFileName(),
				"Deploys the entire wiki content to " + wikiAttachment.getFileName() + ". WARNING: Your current wiki content will be overridden!!!",
				jsAction, Tool.CATEGORY_EXECUTE);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return KnowWEUtils.isAdmin(userContext) && !getWikiAttachments(section.getTitle()).isEmpty();
	}
}
