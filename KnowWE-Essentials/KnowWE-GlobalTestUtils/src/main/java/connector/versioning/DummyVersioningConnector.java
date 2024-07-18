package connector.versioning;

import java.util.Collections;
import java.util.List;

import connector.DummyConnector;

import de.knowwe.core.wikiConnector.WikiAttachmentInfo;
import de.knowwe.core.wikiConnector.WikiPageInfo;

public class DummyVersioningConnector extends DummyConnector {

	public DummyVersioningConnector(String path) {
		super(path, new VersioningDummyPageProvider());
	}


	@Override
	public List<WikiPageInfo> getArticleHistory(String title) {
		return dummyPageProvider.getArticleHistory(title);
	}

	@Override
	public List<WikiAttachmentInfo> getAttachmentHistory(String path) {
		if (getAttachment(path) == null) return Collections.emptyList();
		return Collections.singletonList(new WikiAttachmentInfo(path, DUMMY_USER, 1, getLastModifiedDate(path, 1)));
	}

	@Override
	public int getVersion(String title) {
		List<WikiPageInfo> articleHistory = getArticleHistory(title);
		if(articleHistory.size() == 0) return 0;
		return articleHistory.stream()
				.map(WikiPageInfo::getVersion)
				.max(Integer::compareTo)
				.get();
	}

	@Override
	public String getArticleText(String title, int version) {
		if(version == -1) {
			return dummyPageProvider.getArticle(title);
		}
		return dummyPageProvider.getArticle(title, version);
	}
}
