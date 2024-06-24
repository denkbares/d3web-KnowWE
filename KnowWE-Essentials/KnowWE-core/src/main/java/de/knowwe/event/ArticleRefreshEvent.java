package de.knowwe.event;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 04.09.20
 */
public class ArticleRefreshEvent extends ArticleUpdateEvent {

	private final int type;

	public ArticleRefreshEvent(String title, int type) {
		super(title, null);
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
