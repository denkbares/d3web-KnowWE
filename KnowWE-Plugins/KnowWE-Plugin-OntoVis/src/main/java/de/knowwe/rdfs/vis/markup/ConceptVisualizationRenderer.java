package de.knowwe.rdfs.vis.markup;

import com.denkbares.semanticcore.config.RdfConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
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

	@Override
	public void renderContents(Section<?> section, UserContext user, RenderResult string) {
		PreRenderWorker.getInstance().handlePreRendering(section, user, this);
		OntoGraphDataBuilder builder = (OntoGraphDataBuilder) section.getObject(createKey());
		if (builder != null) builder.render(string);
	}

	protected String createKey() {
		return this.getClass().getName();
	}

	@Override
	public void preRender(Section<?> section, UserContext user) {

		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) return;
		Rdf2GoCore core = compiler.getRdf2GoCore();

		Config config = createConfig(section, user, core);

		if (Thread.currentThread().isInterrupted()) return;

		OntoGraphDataBuilder builder = new OntoGraphDataBuilder(section, config, new PackageCompileLinkToTermDefinitionProvider(), core);
		builder.createData();
		section.storeObject(createKey(), builder);
	}

	@Override
	public void cleanUp(Section<?> section) {
		OntoGraphDataBuilder builder = (OntoGraphDataBuilder) section.getObject(createKey());
		if (builder != null) builder.getGraphRenderer().cleanUp();
	}

	Config createConfig(Section<?> section, UserContext user, Rdf2GoCore core) {
		Config config = new Config();
		config.setCacheFileID(getCacheFileID(section));

		if (core != null && !core.getRuleSet().equals(RepositoryConfigs.get(RdfConfig.class))) {
			config.addExcludeRelations("onto:_checkChain2", "onto:_checkChain1", "onto:_checkChain3");
		}

		config.readFromSection(Sections.cast(section, DefaultMarkupType.class));

		Utils.getConceptFromRequest(user, config);

		if (!Strings.isBlank(config.getColors())) {
			config.setRelationColors(Utils.createColorCodings(config.getColors(), core, "rdf:Property"));
			config.setClassColors(Utils.createColorCodings(config.getColors(), core, "rdfs:Class"));
		}
		return config;
	}

}
