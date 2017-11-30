package de.knowwe.rdfs.vis.markup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.denkbares.strings.Identifier;
import com.denkbares.utils.Log;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.tools.GetToolMenuAction;
import de.knowwe.tools.ToolMenuDecoratingRenderer;
import de.knowwe.tools.ToolSet;
import de.knowwe.tools.ToolUtils;

/**
 * @author Dmitrij Kozlov
 * @created 05.12.2016
 */
public class GetVisMenuAction extends GetToolMenuAction {

	public static final String CONCEPT_NAME = "term";

	public static Collection<TerminologyManager> getTerminologyManager(Section<? extends Type> section) {
		List<TerminologyManager> managers = new ArrayList<>();
		if (section != null) {
			List<de.knowwe.core.compile.Compiler> allCompilers = section.getArticleManager()
					.getCompilerManager()
					.getCompilers();
			managers.addAll(allCompilers.stream()
					.filter(compiler -> compiler instanceof TermCompiler)
					.map(compiler -> ((TermCompiler) compiler).getTerminologyManager())
					.collect(Collectors.toList()));
		}
		return managers;

	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		RenderResult string = new RenderResult(context);
		String identifier = context.getParameter("sectionID");
		Section<? extends Type> visMarkupSection = getSection(context, identifier);

		String conceptName = context.getParameter(CONCEPT_NAME);
		if (conceptName == null) {
			String message = "term name paraeter not found";
			Log.severe(message);
			string.append(message);
			writeResponse(context, null, string);
			return;
		}

		String[] identifierParts = conceptName.split(":");
		if(identifierParts[0].isEmpty()) {
			identifierParts = new String[] { identifierParts[1]};
		}

		Identifier termIdentifier = new Identifier(identifierParts);
		Section<Term> termDefiningSection = null;
		final Collection<TerminologyManager> terminologyManager = getTerminologyManager(visMarkupSection);
		for (TerminologyManager manager : terminologyManager) {
			Section<? extends Type> definition = manager.getTermDefiningSection(termIdentifier);
			if (definition != null && definition.get() instanceof Term) {
				termDefiningSection = Sections.cast(definition, Term.class);
			}
		}
		if (termDefiningSection == null) {
			String message = "No defining Section found";
			Log.severe(message);
			string.append(message);
			writeResponse(context, null, string);
			return;
		}

		ToolSet tools = getTools(context, termDefiningSection);
		if (!tools.hasTools()) return;

		ToolMenuDecoratingRenderer.renderToolMenuDecorator(conceptName, termDefiningSection.getID(), ToolUtils
				.hasToolInstances(termDefiningSection, context), string);

		writeResponse(context, termDefiningSection, string);

	}

	@Override
	public void writeResponse(UserActionContext context, Section<? extends Type> section, RenderResult string) throws IOException {
		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(string.toString());
		}
	}

}
