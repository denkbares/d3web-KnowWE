package connector;

import java.util.Date;
import java.util.List;
import java.util.Map;

import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiPageInfo;

public interface DummyPageProvider {

	void setArticleContent(String title, String content);

	void setArticleContent(String title, String content, String changeNote);

	void storeAttachment(WikiAttachment attachment);

	void deleteAttachment(String path);

	Map<String, String> getAllArticles();

	String getArticle(String title);

	String getArticle(String title, int version);

	Map<String, WikiAttachment> getAllAttachments();

	Map<String, WikiAttachment> getRootAttachments();

	WikiAttachment getAttachment(String path);


	Date getStartUpdate();

	void deletePage(String title);

	void renameArticle(String fromPage, String toPage);

	List<WikiPageInfo> getArticleHistory(String title);

	String getChangeNote(String title, int version);
}
