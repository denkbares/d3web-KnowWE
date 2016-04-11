package de.knowwe.jspwiki;

import java.util.Collection;
import java.util.List;

import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.search.LuceneSearchProvider;
import org.apache.wiki.search.SearchResult;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.plugin.Plugins;

/**
 * This is a simple SearchProvider extending the LuceneSearchProvider, but
 * allows to add additional pages from plugged providers.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.02.2013
 */
public class ExtensibleLuceneSearchProvider extends LuceneSearchProvider {

	@SuppressWarnings("unchecked")
	@Override
	public Collection<SearchResult> findPages(String query, int flags) throws ProviderException {
		Collection<SearchResult> results = super.findPages(query, flags);

		Extension[] extensions = PluginManager.getInstance().getExtensions(
				Plugins.EXTENDED_PLUGIN_ID,
				Plugins.EXTENDED_POINT_SearchProvider);

		// execute search for all plugged search-providers
		for (Extension extension : extensions) {
			Object newInstance = extension.getNewInstance();
			if (newInstance instanceof SearchProvider) {
				SearchProvider provider = ((SearchProvider) newInstance);
				// execute search for plugged provider
				List<? extends SearchResult> providerResults = provider.findResults(
						query, flags);
				results.addAll(providerResults);
			}
		}

		return results;
	}

	@Override
	public String getProviderInfo() {
		return "PluggableLuceneSearchProvider";
	}

}
