package de.d3web.we.jspwiki;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.providers.AbstractFileProvider;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.javaEnv.KnowWETopicLoader;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class JSPWikiKnowWEConnector implements KnowWEWikiConnector {

	private ServletContext context = null;
	private WikiEngine engine = null;

	public JSPWikiKnowWEConnector(WikiEngine eng) {
		this.context = eng.getServletContext();
		this.engine = eng;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	public String getPagePath() {
		try {
			return WikiEngine.getRequiredProperty(engine.getWikiProperties(),
					AbstractFileProvider.PROP_PAGEDIR);
		} catch (NoRequiredPropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error reading Property: AbstractFileProvider.PROP_PAGEDIR";
		}

	}

	@Override
	public KnowWEActionDispatcher getActionDispatcher() {
		// TODO Auto-generated method stub
		return new JSPActionDispatcher();
	}

	@Override
	public KnowWETopicLoader getLoader() {
		// TODO Auto-generated method stub
		return new JSPWikiLoader(getPagePath());
	}

	@Override
	public boolean saveArticle(String name, String text, KnowWEParameterMap map) {
		try {
			HttpServletRequest req = map.getRequest();
			WikiContext context = engine.createContext(req, WikiContext.EDIT);
			context.setPage(engine.getPage(name));
			engine.saveText(context, text);
			// engine.saveText(map.getContext(), text);
			return true;
		} catch (WikiException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public LinkedList<String> getAttachments() {

		try {
			LinkedList<String> sortedAttList = new LinkedList<String>();
			Collection<Attachment> attList = (Collection<Attachment>) this.engine
					.getAttachmentManager().getAllAttachments();

			for (Attachment p : attList) {

				if (p.getFileName().endsWith("jar")) {
					sortedAttList.add(p.getFileName());
				}

			}
			return sortedAttList;
		} catch (ProviderException e) {
			return null;
		}
	}

	@Override
	public String getAttachmentPath(String jarName) {

		if (this.getMeAttachment(jarName) != null) {

			String jarPath = "";

			// Get Path where all Attachments are stored
			String storageDir;
			try {
				storageDir = WikiEngine.getRequiredProperty(this.engine
						.getWikiProperties(),
						"jspwiki.basicAttachmentProvider.storageDir");
				jarPath += storageDir;
			} catch (NoRequiredPropertyException e) {
				// TODO Auto-generated catch block
				return null;
			}

			// Get Attachments ParentPage
			String parentPage = this.getMeAttachment(jarName).getParentName();
			jarPath += "/" + parentPage + "-att/";

			// Get the Attachments directory
			// TEST: WHAT VERSION IS THE NEWEST
			jarPath += jarName + "-dir/";

			// Fixes a bug in which the getVersion returns -1 instead of 1;
			jarPath += String.valueOf(Math.abs(this.getMeAttachment(jarName)
					.getVersion()));

			jarPath += ".jar";

			return jarPath;
		}
		return null;
	}

	private Attachment getMeAttachment(String name) {

		Collection<Attachment> attList;

		try {
			attList = (Collection<Attachment>) this.engine
					.getAttachmentManager().getAllAttachments();
		} catch (ProviderException e) {
			// TODO Auto-generated catch block
			return null;
		}

		for (Attachment p : attList) {
			if (p.getFileName().equals(name)) {
				return p;
			}
		}

		return null;
	}

	public java.util.Map<String, String> getAllArticles(String web) {
		Map<String, String> result = new HashMap<String, String>();
		Collection<WikiPage> pages = null;
		try {
			pages = this.engine.getPageManager().getProvider().getAllPages();
		} catch (ProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pages == null)
			return null;

		for (WikiPage wikiPage : pages) {
			String pageContent = null;
			try {
				pageContent = this.engine.getPageManager().getPageText(
						wikiPage.getName(), wikiPage.getVersion());
			} catch (ProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (pageContent != null) {
				result.put(wikiPage.getName(), pageContent);
			}

		}

		return result;
	}

	public String createWikiPage(String topic, String content, String author) {

		WikiPage wp = new WikiPage(this.engine, topic);

		try {
			// References Updaten.
			// this.engine.getReferenceManager().updateReferences(
			// att.getName(),
			// new java.util.Vector() );
			wp.setAuthor(author);
			this.engine.getPageManager().putPageText(wp, content);
			this.engine.getSearchManager().reindexPage(wp);

		} catch (ProviderException e) {
			return null;
		}

		return this.engine.getPureText(wp);
	}

	// private String getHashMapContent(HashMap<String, String> pageContent,
	// String topic ) {
	//		
	// String content = "<Kopic id=\"" + topic + "\"" + ">";
	//		
	// // append the Sections to content if they are not "".
	// // 1. Questionnaires-section
	// if (pageContent.get("qClassHierarchy") != "") {
	// content += "\n<Questionnaires-section>\n";
	// content += pageContent.get("qClassHierarchy");
	// content += "</Questionnaires-section>\n";
	// }
	//		
	// // 2.Questions-section
	// if (pageContent.get("decisionTree") != "") {
	// content += "<Questions-section>\n";
	// content += pageContent.get("decisionTree");
	// content += "</Questions-section>\n";
	// }
	//		
	// // 3. SetCoveringList-Section
	// if (pageContent.get("xcl") != "") {
	// content += "<SetCoveringList-section>\n";
	// content += pageContent.get("xcl");
	// content += "</SetCoveringList-section>\n";
	// }
	//				
	// // 4. Rules-section
	// if (pageContent.get("rules") != "") {
	// content.concat("<Rules-section>\n");
	// content.concat(pageContent.get("rules"));
	// content.concat("</Rules-section>\n");
	// }
	//		
	// // 5. Solutions-Section
	// if (pageContent.get("diagnosisHierarchy") != "") {
	// content += "<Solutions-section>\n";
	// content += pageContent.get("diagnosisHierarchy");
	// content += "</Solutions-section>\n";
	// }
	//				
	// content += "</Kopic>";
	// return content;
	// }

	@Override
	public boolean doesPageExist(String topic) {

		// Check if a Page with the chosen Topic already exists.
		if (this.engine.pageExists(topic)) {
			return true;
		}
		return false;
	}

	@Override
	public String appendContentToPage(String topic, String content) {

		try {
			content = this.engine.getPureText(this.engine.getPage(topic))
					+ content;
			WikiContext context = new WikiContext(this.engine, this.engine
					.getPage(topic));
			this.engine.saveText(context, content);
			this.engine.updateReferences(this.engine.getPage(topic));
			this.engine.getSearchManager().reindexPage(
					this.engine.getPage(topic));

		} catch (ProviderException e) {
			return null;
		} catch (WikiException e1) {
			return null;
		}

		return this.engine.getPureText(this.engine.getPage(topic));
	}

	@Override
	public String getArticleSource(String name) {
		if(this.engine.getPage(name)==null)
		    return null;
	    WikiContext context = new WikiContext(this.engine, this.engine.getPage(name));
		
		String pagedata = context.getEngine().getPureText(
				context.getPage().getName(), context.getPage().getVersion());
		return pagedata;
	}

	@Override
	public String getBaseUrl() {		
		return engine.getBaseURL();
	}

}
