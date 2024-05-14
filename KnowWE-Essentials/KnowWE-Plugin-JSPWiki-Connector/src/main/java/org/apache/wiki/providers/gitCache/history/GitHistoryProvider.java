package org.apache.wiki.providers.gitCache.history;

import java.util.List;

import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.structs.PageIdentifier;

public interface GitHistoryProvider {

	/**
	 * Access the history of the page provided by @pageName
	 *
	 * @param pageName
	 * @return
	 * @throws ProviderException
	 */
	List<Page> getPageHistory(PageIdentifier pageIdentifier) throws ProviderException;

	String basePath();



//	/**
//	 * Access the history of the attachment provided by @att
//	 *
//	 * @param att
//	 * @return
//	 * @throws ProviderException
//	 */
//	List<Attachment> getVersionHistory(Attachment att);
}
