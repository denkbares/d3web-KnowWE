package de.knowwe.rdf2go;

import java.io.File;
import java.io.IOException;

import com.denkbares.semanticcore.SemanticCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.plugin.Instantiation;

/**
 * Instantiation to initialize the SemanticCore with a proper temp directory.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.06.16
 */
public class RepositoryInitializer implements Instantiation {
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryInitializer.class);

	@Override
	public void init(String web) {
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		String basedir = wikiConnector.getWikiProperty("var.basedir");
		if (basedir == null) basedir = "wiki";
		String appPathHash = Integer.toHexString(wikiConnector.getApplicationRootPath().hashCode());
		String context = new File(basedir).getName() + "_" + appPathHash;

		try {
			SemanticCore.shutDownRepositoryManager(); // just in case there is already one initialized (e.g. JUnit tests)
			File tempDir = SemanticCore.createRepositoryManagerDir(context);
			SemanticCore.initializeRepositoryManager(tempDir);
		}
		catch (IOException e) {
			LOGGER.error("Unable to initialize repository for SemanticCore!", e);
		}
	}
}
