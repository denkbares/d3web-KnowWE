package de.d3web.we.taghandler;

/**
 * @author Jochen
 * 
 * An abstract implementation of the TagHandler Interface handling the tagName in lowercase.
 *
 */
public abstract class AbstractTagHandler implements TagHandler{
	
	private String name = null;
	
	public AbstractTagHandler(String name) {
		this.name = name.toLowerCase();
	}

	@Override
	public String getTagName() {
		return name.toLowerCase();
	}
	
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName() + "}]";
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	
	public String getDescription() {
		return "TODO add description for this Handler";
	}
	
}
