package de.knowwe.jspwiki.changeannotations;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.event.WikiEvent;
import org.apache.wiki.event.WikiEventListener;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.event.WikiPageEvent;
import org.apache.wiki.event.WikiPageRenameEvent;
import org.apache.wiki.pages.PageManager;

import com.denkbares.knowwe.changeannotations.InMemoryPageAnnotationCache;
import com.denkbares.knowwe.changeannotations.PageAnnotation;
import com.denkbares.knowwe.changeannotations.PageAnnotationCache;

/**
 * JSPWiki-flavoured wrapper around a {@link PageAnnotationCache}: hooks the storage to
 * {@link WikiEventManager} so save, delete, and rename events invalidate the affected
 * entries, and binds {@link #getOrCompute(Engine, String)} to {@link JspWikiPageAnnotator}.
 *
 * <p>The pure storage logic lives in the domain plugin
 * ({@link InMemoryPageAnnotationCache}); this class adds the JSPWiki integration on top.
 */
public final class JspWikiPageAnnotationCache implements WikiEventListener {

	private static final JspWikiPageAnnotationCache INSTANCE = new JspWikiPageAnnotationCache();

	public static JspWikiPageAnnotationCache getInstance() {
		return INSTANCE;
	}

	private final PageAnnotationCache delegate;
	private final AtomicBoolean listenerRegistered = new AtomicBoolean(false);

	JspWikiPageAnnotationCache() {
		this(new InMemoryPageAnnotationCache());
	}

	JspWikiPageAnnotationCache(PageAnnotationCache delegate) {
		this.delegate = delegate;
	}

	/**
	 * Returns the cached annotation for {@code pageName}, computing it via
	 * {@link JspWikiPageAnnotator#annotate(Engine, String)} on first access. Idempotent —
	 * also registers the WikiEvent listener on first call.
	 */
	public PageAnnotation getOrCompute(Engine engine, String pageName) {
		if (engine == null) throw new NullPointerException("engine");
		if (pageName == null) throw new NullPointerException("pageName");
		ensureListenerRegistered(engine);
		return delegate.getOrCompute(pageName, name -> JspWikiPageAnnotator.annotate(engine, name));
	}

	/** Test seam: bypasses {@link JspWikiPageAnnotator} so unit tests stay JSPWiki-free. */
	PageAnnotation getOrCompute(String pageName, Function<String, PageAnnotation> loader) {
		return delegate.getOrCompute(pageName, loader);
	}

	public void invalidate(String pageName) {
		delegate.invalidate(pageName);
	}

	public void invalidateAll() {
		delegate.invalidateAll();
	}

	@Override
	public void actionPerformed(WikiEvent event) {
		if (event instanceof WikiPageRenameEvent rename) {
			delegate.invalidate(rename.getOldPageName());
			delegate.invalidate(rename.getNewPageName());
			return;
		}
		if (event instanceof WikiPageEvent pageEvent) {
			switch (pageEvent.getType()) {
				case WikiPageEvent.PAGE_REINDEX, // fired after a save (DefaultPageManager#putPageText)
						WikiPageEvent.PAGE_DELETED,
						WikiPageEvent.PAGE_DELETE_REQUEST -> delegate.invalidate(pageEvent.getPageName());
				default -> {
					// ignore — PAGE_REQUESTED / PAGE_DELIVERED / PAGE_LOCK / PAGE_UNLOCK do not change content
				}
			}
		}
	}

	private void ensureListenerRegistered(Engine engine) {
		if (listenerRegistered.compareAndSet(false, true)) {
			PageManager pageManager = engine.getManager(PageManager.class);
			WikiEventManager.addWikiEventListener(pageManager, this);
		}
	}
}
