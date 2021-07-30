package de.knowwe.d3web.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Streams;
import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.we.knowledgebase.KnowledgeBaseReference;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

public class KnowledgeBaseDownloadAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_KB_NAME = "kb";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String filename = context.getParameter(PARAM_FILENAME);
		String sectionId = context.getParameter(Attributes.SECTION_ID);

		if (sectionId == null) {
			String kbName = context.getParameter(PARAM_KB_NAME);
			if (kbName != null) {
				sectionId = KnowledgeBaseReference.getDefinition(context.getArticleManager(), kbName)
						.ancestor(KnowledgeBaseType.class)
						.mapFirst(Section::getID);
			}
		}

		if (sectionId == null) {
			// may be specified by Attributes.TOPIC
			Article article = context.getArticle();
			if (article != null) {
				Section<?> kbSection = Sections.successor(article.getRootSection(), KnowledgeBaseType.class);
				if (kbSection != null) sectionId = kbSection.getID();
			}
		}

		Section<?> section = Sections.get(sectionId);
		Section<PackageCompileType> compileSection = null;
		if (section != null) {
			compileSection = Sections.successor(section, PackageCompileType.class);
		}
		if (compileSection == null) {
			context.sendError(409, "The Sections '" + sectionId
					+ "' could not be found, possibly because somebody else"
					+ " has edited them.");
			return;
		}
		String web = compileSection.getWeb();

		if (!KnowWEUtils.canView(section.getArticle(), context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to download this knowledgebase");
			return;
		}

		Collection<String> articlesWithWrongVersion = getArticlesOfKnowledgeBaseLoadedWithWrongVersion(compileSection);

		if (!articlesWithWrongVersion.isEmpty()) {
			context.sendError(409,
					"The following articles are currently loaded with an outdated version: "
							+ articlesWithWrongVersion
							+ ". Refresh them or go back to an up to date version to be"
							+ " able to download the knowledgebase.");
			return;
		}

		// before writing, check if the user defined a desired filename
		KnowledgeBase base = D3webUtils.getKnowledgeBase(context, compileSection);
		if (base == null) failUnexpected(context, "Unable to find knowledge base on page " + section.getTitle());
		String desiredFilename = base.getInfoStore().getValue(BasicProperties.FILENAME);
		if (desiredFilename != null) {
			filename = desiredFilename;
		}
		// write the timestamp of the creation (Now!) into the knowledge
		// base
		base.getInfoStore().addValue(BasicProperties.CREATED, new Date());

		URL home;
		try {
			home = saveKnowledge(web, base);
		}
		catch (IOException e) {
			Log.warning("Error while saving knowledge base", e);
			context.sendError(410, e.getMessage());
			return;
		}

		context.setContentType(BINARY);

		context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		try (InputStream input = home.openStream()) {
			try (OutputStream outs = context.getOutputStream()) {
				Streams.stream(input, outs);
			}
		}
	}

	public URL saveKnowledge(String web, KnowledgeBase base) throws IOException {
		URL home = D3webUtils.getKnowledgeBaseURL(web, base.getId());
		// We no longer can cache knowledge bases, because attachments added as
		// resources could change without anyone knowing. As long as we do not
		// get any notification from JSPWiki that attachments have changed, we
		// need to create a new knowledge base every time.
		PersistenceManager.getInstance().save(base,
				new File(Strings.decodeURL(home.getFile())));
		return home;
	}

	private Collection<String> getArticlesOfKnowledgeBaseLoadedWithWrongVersion(Section<PackageCompileType> section) {
		PackageManager packageManager = KnowWEUtils.getPackageManager(section);
		String[] compiledPackages = section.get().getPackagesToCompile(section);
		Set<Section<?>> compiledSections = new HashSet<>();
		for (String compiledPackage : compiledPackages) {
			compiledSections.addAll(packageManager.getSectionsOfPackage(compiledPackage));
		}
		Set<Article> articlesOfKnowledgeBase = new HashSet<>();
		for (Section<?> compiledSection : compiledSections) {
			articlesOfKnowledgeBase.add(compiledSection.getArticle());
		}
		List<String> wrongVersionTitles = new LinkedList<>();
		for (Article article : articlesOfKnowledgeBase) {
			String articleText = article.getRootSection().getText();
			String connectorVersionOfArticleText = Environment.getInstance()
					.getWikiConnector()
					.getArticleText(article.getTitle());
			if (!articleText.equals(connectorVersionOfArticleText)) {
				wrongVersionTitles.add(article.getTitle());
			}
		}
		return wrongVersionTitles;
	}
}
