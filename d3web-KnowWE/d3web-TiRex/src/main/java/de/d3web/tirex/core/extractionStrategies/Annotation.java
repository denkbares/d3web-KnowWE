package de.d3web.tirex.core.extractionStrategies;

/**
 * An annotation, which consists of a "prefix" and a "suffix". It can be used to
 * mark parts of text extracted by an "Extraction Strategy":
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class Annotation {
	private String prefix;

	private String suffix;

	public Annotation(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}
}
