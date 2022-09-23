package de.knowwe.d3web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.utils.Streams;
import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.knowledgebase.KnowledgeBaseReference;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.RecompileAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

public class KnowledgeBaseDownloadAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_FULL_COMPILE = "requireFullCompile";
	public static final String PARAM_KB_NAME = "kb";

	@Override
	public void execute(UserActionContext context) throws IOException {

		D3webCompiler compiler = getCompiler(context);

		if (compiler == null) failUnexpected(context, "No compiler found on page");

		if (!KnowWEUtils.canView(compiler.getCompileSection().getArticle(), context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to download this knowledge base");
			return;
		}

		Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
		if (Boolean.parseBoolean(context.getParameter(PARAM_FULL_COMPILE, "false"))) {
			if (compiler.isIncrementalBuild()) {
				RecompileAction.recompile(compiler.getCompileSection().getArticle(), true, "Knowledge base download by " + context.getUserName());
				Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
				compiler = getCompiler(context);
				if (compiler == null) failUnexpected(context, "Compile no longer available after recompile");
			}
		}

		KnowledgeBase base = compiler.getKnowledgeBase();

		String filename = context.getParameter(PARAM_FILENAME);
		if (filename == null) {
			filename = base.getInfoStore().getValue(BasicProperties.FILENAME);
		}
		if (filename == null) {
			filename = compiler.getName() + ".d3web";
		}

		context.setContentType(BINARY);
		context.getResponse().addHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		context.getResponse().addHeader("Last-Modified", org.apache.http.client.utils.DateUtils.formatDate(compiler.getLastModified()));

		// write the timestamp of the creation (Now!) into the knowledge base
		base.getInfoStore().addValue(BasicProperties.CREATED, new Date());

		File file = Files.createTempFile(compiler.getName(), filename).toFile();
		try {
			PersistenceManager.getInstance().save(base, file);
			try (InputStream input = new FileInputStream(file)) {
				try (OutputStream outs = context.getOutputStream()) {
					Streams.stream(input, outs);
				}
			}
		}
		finally {
			file.delete();
		}
	}

	private D3webCompiler getCompiler(UserActionContext context) {
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
			return null;
		}

		return D3webUtils.getCompiler(context, compileSection);
	}
}
