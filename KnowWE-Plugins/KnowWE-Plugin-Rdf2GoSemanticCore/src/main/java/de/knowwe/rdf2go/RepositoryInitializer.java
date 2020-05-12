package de.knowwe.rdf2go;

import java.io.IOException;

import com.denkbares.semanticcore.SemanticCore;
import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.plugin.Instantiation;

/**
 * Instantiation to initialize the SemanticCore with a proper temp directory.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.06.16
 */
public class RepositoryInitializer implements Instantiation {

	@Override
	public void init(String web) {
		String realPath = Environment.getInstance().getWikiConnector().getRealPath().replaceAll("\\W", "");
		String pathHash = Integer.toHexString(Environment.getInstance()
				.getWikiConnector()
				.getApplicationRootPath()
				.hashCode());
		String context = realPath + "_" + pathHash;

		try {
			SemanticCore.initializeRepositoryManager(SemanticCore.createRepositoryPath(context));
		}
		catch (IOException e) {
			Log.severe("Unable to initialize repository for SemanticCore!", e);
		}
	}
}
