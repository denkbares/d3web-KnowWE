package de.knowwe.rdfs.vis.markup;

import com.denkbares.semanticcore.config.RdfConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.PackageCompileLinkToTermDefinitionProvider;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.rdfs.vis.PreRenderWorker;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.Config;

public class ConceptVisualizationRenderer extends DefaultMarkupRenderer implements PreRenderer<OntoGraphDataBuilder> {

	@Override
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult string) {
		OntoGraphDataBuilder builder = getGraphDataBuilder(section, user);
		if (builder != null) {
			builder.render(string);
			if (builder.isTimeOut()) {
				string.appendHtml("<div class='warning'>");
				//appendMessage(section, e, user, result);
				Config config = builder.getConfig();
				string.appendHtml("Creation of visualization timed out after " + Stopwatch.getDisplay(config.getTimeout()));
				string.appendHtml("<br/><a onclick='KNOWWE.plugin.ontovis.retry(\"" + section.getID()
						+ "\")' title='Try executing the query again, if you think it was only a temporary problem.'"
						+ " class='tooltipster'>Try again...</a>");
			}
		}
	}

	public OntoGraphDataBuilder getGraphDataBuilder(Section<?> section, UserContext user) {
		return PreRenderWorker.getInstance().getPreRenderedArtefact(section, user, this);
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		if (user.isRenderingPreview()) {
			result.append("%%information Concept Visualization is not rendered in live preview. /%");
			return;
		}
		super.render(section, user, result);
	}

	@Override
	public OntoGraphDataBuilder preRender(Section<?> section, UserContext user) {

		Rdf2GoCompiler compiler = Compilers.getCompiler(user, section, Rdf2GoCompiler.class);
		if (compiler == null) return null;
		Rdf2GoCore core = compiler.getRdf2GoCore();

		Config config = createConfig(section, user, core);

		if (Thread.currentThread().isInterrupted()) return null;

		OntoGraphDataBuilder builder = new OntoGraphDataBuilder(section, config, new PackageCompileLinkToTermDefinitionProvider(), core);
		builder.createData(config.getTimeout());
		return builder;
	}

	Config createConfig(Section<?> section, UserContext user, Rdf2GoCore core) {
		Config config = new Config();
		config.setCacheFileID(Utils.getFileID(section, user));

		if (core != null && !core.getRuleSet().equals(RepositoryConfigs.get(RdfConfig.class))) {
			config.addExcludeRelations("onto:_checkChain2", "onto:_checkChain1", "onto:_checkChain3");
		}

		Messages.clearMessages(section, this.getClass());
		config.init(Sections.cast(section, DefaultMarkupType.class), user);

//		Is this a ConceptVisualization template?
		if (!Strings.isBlank(DefaultMarkupType.getAnnotation(section, ConceptVisualizationType.VIS_TEMPLATE_CLASS))) {
//			Yay, it is!
			config.setConcept(Utils.getConceptFromRequest(user));
			config.setCacheFileID(Utils.getFileID(section, user));
		}

		if (!Strings.isBlank(config.getColorsProperty())) {
			config.setColors(Utils.createColorCodings(section, config.getColorsProperty(), core));
		}
		return config;
	}
}
