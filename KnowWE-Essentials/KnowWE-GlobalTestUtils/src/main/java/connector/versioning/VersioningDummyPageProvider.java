package connector.versioning;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import connector.SimpleDummyPageProvider;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.wikiConnector.WikiPageInfo;
import de.knowwe.core.wikiConnector.WikiPageInfoFull;

import static connector.DummyConnector.DUMMY_USER;

/**
 * A DummyPageProvider that can handle versioning of pages.
 */

public class VersioningDummyPageProvider extends SimpleDummyPageProvider {

	private final TreeMap<String, List<WikiPageInfoFull>> articles = new TreeMap<>();

	@Override
	public List<WikiPageInfo> getArticleHistory(String title) {
		List<WikiPageInfoFull> wikiPageInfos = articles.get(title);
		if (wikiPageInfos == null) wikiPageInfos = Collections.emptyList();
		List<WikiPageInfo> wikiPageInfosReverse = new ArrayList<>(wikiPageInfos);
		Collections.reverse(wikiPageInfosReverse);
		return wikiPageInfosReverse;
	}

	@Nullable
	@Override
	public String getArticle(String title, int version) {
		return get(title, version, WikiPageInfoFull::getText);
	}

	private String get(String title, int version, Function<WikiPageInfoFull, String> function) {
		List<WikiPageInfoFull> wikiPageInfos = articles.get(title);
		if (wikiPageInfos == null) return null;
		// this could be made more efficient, but as this class is used in some tests only, it should be okay
		WikiPageInfoFull pageInfo = wikiPageInfos.stream()
				.filter(wikiPageInfo -> wikiPageInfo.getVersion() == version)
				.findFirst()
				.orElse(null);
		if (pageInfo != null) {
			return function.apply(pageInfo);
		}
		return null;
	}

	@Override
	public String getChangeNote(String title, int version) {
		return get(title, version, WikiPageInfoFull::getChangeNote);
	}


	@Override
	public void setArticleContent(String title, String content) {
		setArticleContent(title, content, "");
	}




	@Override
	public void setArticleContent(String title, String content, String changeNote) {
		List<WikiPageInfoFull> wikiPageInfos = articles.computeIfAbsent(title, x -> new ArrayList<>());
		int currentVersion = 0; // if article does not yet exist at all
		if (wikiPageInfos.size() > 0) {
			currentVersion = wikiPageInfos.get(wikiPageInfos.size() - 1).getVersion();
		}
		int createdVersion = currentVersion + 1;
		wikiPageInfos.add(new WikiPageInfoFull(title, DUMMY_USER, createdVersion, Date.from(Instant.now()), changeNote, content));
	}

	@Override
	public Map<String, String> getAllArticles() {
		return Map.copyOf(articles.keySet().stream().collect(Collectors.toMap(title -> title, this::getArticle)));
	}

	@Override
	public String getArticle(String title) {
		List<WikiPageInfoFull> wikiPageInfos = articles.get(title);
		return wikiPageInfos.get(wikiPageInfos.size() - 1).getText();
	}

	@Override
	public void deletePage(String title) {
		articles.remove(title);
	}

	@Override
	public void renameArticle(String fromPage, String toPage) {
		List<WikiPageInfoFull> wikiPageInfos = articles.get(fromPage);
		List<WikiPageInfoFull> wikiPageInfosRenamed = new ArrayList<>();
		wikiPageInfos.forEach(oldPageInfo -> wikiPageInfosRenamed.add(
				new WikiPageInfoFull(toPage, oldPageInfo.getAuthor(), oldPageInfo.getVersion(), oldPageInfo.getSaveDate(), oldPageInfo.getChangeNote(), oldPageInfo.getText()))
		);
		articles.remove(fromPage);
		articles.put(toPage, wikiPageInfosRenamed);
	}
}
