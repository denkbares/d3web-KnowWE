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

public class ConceptVisualizationRenderer extends DefaultMarkupRenderer implements PreRenderer {

	public static final String VISUALIZATION_RENDERER_KEY = "conceptVisualizationRendererKey";

	public static String getVisualizationRendererKey(UserContext user) {
		String key = VISUALIZATION_RENDERER_KEY;
		String concept = Utils.getConceptFromRequest(user);
		if (user == null || concept == null) {
			return key;
		}
		else {
			return key + concept;
		}
	}

	@Override
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult string) {
		String concept = Utils.getConceptFromRequest(user);
		OntoGraphDataBuilder builder = (OntoGraphDataBuilder) section.getObject(getVisualizationRendererKey(user));
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

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		if (user.isRenderingPreview()) {
			result.append("%%information Concept Visualization is not rendered in live preview. /%");
			return;
		}
		PreRenderWorker.getInstance().handlePreRendering(section, user, this);
		super.render(section, user, result);
	}

	@Override
	public void preRender(Section<?> section, UserContext user) {

		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) return;
		Rdf2GoCore core = compiler.getRdf2GoCore();

		Config config = createConfig(section, user, core);

		if (Thread.currentThread().isInterrupted()) return;

		OntoGraphDataBuilder builder = new OntoGraphDataBuilder(section, config, new PackageCompileLinkToTermDefinitionProvider(), core);
		builder.createData(config.getTimeout());

		section.storeObject(getVisualizationRendererKey(user), builder);
	}


	@Override
	public void cleanUp(Section<?> section) {
		OntoGraphDataBuilder builder = (OntoGraphDataBuilder) section.getObject(VISUALIZATION_RENDERER_KEY);
		if (builder != null) builder.getGraphRenderer().cleanUp();
	}

	Config createConfig(Section<?> section, UserContext user, Rdf2GoCore core) {
		Config config = new Config();
		config.setCacheFileID(getCacheFileID(section, user));

		if (core != null && !core.getRuleSet().equals(RepositoryConfigs.get(RdfConfig.class))) {
			config.addExcludeRelations("onto:_checkChain2", "onto:_checkChain1", "onto:_checkChain3");
		}

		Messages.clearMessages(section, this.getClass());
		config.init(Sections.cast(section, DefaultMarkupType.class), user);

//		Is this a ConceptVisualization template?
		if (!Strings.isBlank(DefaultMarkupType.getAnnotation(section, ConceptVisualizationType.VIS_TEMPLATE_CLASS))) {
//			Yay, it is!
			config.setConcept(Utils.getConceptFromRequest(user));
			config.setCacheFileID(getCacheFileID(section, user));
		}

		if (!Strings.isBlank(config.getColors())) {
			config.setRelationColors(Utils.createColorCodings(section, config.getColors(), core, "rdf:Property"));
			config.setClassColors(Utils.createColorCodings(section, config.getColors(), core, "rdfs:Class"));
			config.setIndividualColors(Utils.createColorCodings(section, config.getColors(), core, "owl:Thing"));
		}
		return config;
	}

}
