package de.knowwe.jspwiki;

import java.util.Collection;

import com.ecyrd.jspwiki.SearchResult;
import com.ecyrd.jspwiki.providers.ProviderException;
import com.ecyrd.jspwiki.search.LuceneSearchProvider;

/**
 * This is a simple SearchProvider extending the LuceneSearchProvider, but
 * allows to add additional pages from plugged providers.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.02.2013
 */
public class PluggableLuceneSearchProvider extends LuceneSearchProvider {

	@SuppressWarnings("unchecked")
	@Override
	public Collection<SearchResult> findPages(String query, int flags) throws ProviderException {
		Collection<SearchResult> luceneResults = super.findPages(query, flags);

		return luceneResults;
	}

	@Override
	public String getProviderInfo()
	{
		return "PluggableLuceneSearchProvider";
	}

}
