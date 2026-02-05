package de.knowwe.core;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * In a multi-wiki setup with multiple sub-wiki folders that together form the wiki-content, it is for some operations
 * necessary to keep track of the sub-wiki context, that the user is currently operating in. For instance,
 * if a page should be created from some context, the new page should be created within the same sub-wiki context
 * as the operational context that the user initiated his interaction.
 * <p>
 * In single-wiki systems the default KnowWESubWikiContext always contains the empty string.
 */
public record KnowWESubWikiContext(String subWiki) {

	private static final Logger LOGGER = LoggerFactory.getLogger(KnowWESubWikiContext.class);

	public static final KnowWESubWikiContext SIMPLE_CONTEXT = new KnowWESubWikiContext("");
	public static final KnowWESubWikiContext DEFAULT_CONTEXT = generateDefaultContext();
	public static final String KNOWWE_CONTEXT_PARAMETER = "KnowWESubWikiContext";
	public static final String MAIN_SUBWIKI_SUBFOLDER = "jspwiki.mainFolder";

	public static KnowWESubWikiContext createFrom(Section<?> compileSection) {
		return createFrom(compileSection.getTitle());
	}

	public static KnowWESubWikiContext createFrom(String globalPageName) {
		return new KnowWESubWikiContext(Environment.getInstance().getWikiConnector().getSubWikiName(globalPageName));
	}

	public String getGlobalPageName(String localPageName) {
		if (Strings.isBlank(subWiki)) return localPageName;
		return Environment.getInstance().getWikiConnector().toGlobalArticleName(localPageName, this);
	}

	public String toExistingUniqueOrGlobalName(String title) {
		if (Strings.isBlank(this.subWiki)) {
			return title;
		}
		else {
			return Environment.getInstance()
					.getWikiConnector().toExistingUniqueOrGlobalName(title);
		}
	}

	public String getLocalName(String globalName) {
		if (!globalName.startsWith(this.subWiki)) {
			throw new IllegalArgumentException("Invalid subwiki context (" + this.subWiki + ") for article: " + globalName);
		}
		return Environment.getInstance().getWikiConnector().toLocalArticleName(globalName, this);
	}

	public KnowWESubWikiContext {
		if (subWiki == null) {
			subWiki = "";
		}
	}

	/**
	 * @return default KnowWESubWikiContext
	 */
	public static KnowWESubWikiContext getDefaultContext() {
		return DEFAULT_CONTEXT;
	}

	private static @NotNull KnowWESubWikiContext generateDefaultContext() {
		if (!Environment.isInitialized()) {
			LOGGER.warn("Environment not initialized, this can happen in test environments, using simple context instead. " +
						"If this happens in production, something went wrong!");
			return KnowWESubWikiContext.SIMPLE_CONTEXT;
		}
		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();
		String wikiProperty = wikiConnector.getWikiProperty(MAIN_SUBWIKI_SUBFOLDER);
		if (wikiProperty == null) {
			return KnowWESubWikiContext.SIMPLE_CONTEXT;
		}
		else {
			return new KnowWESubWikiContext(wikiProperty);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		KnowWESubWikiContext that = (KnowWESubWikiContext) o;
		return Objects.equals(subWiki, that.subWiki);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(subWiki);
	}
}
