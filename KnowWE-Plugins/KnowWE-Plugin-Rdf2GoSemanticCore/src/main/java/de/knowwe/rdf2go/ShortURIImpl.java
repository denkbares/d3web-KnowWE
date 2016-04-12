package de.knowwe.rdf2go;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import de.d3web.strings.Identifier;

public class ShortURIImpl extends URIImpl {

	private static final long serialVersionUID = -5221506155017259902L;
	private URI uriLong;

	ShortURIImpl(String uriShort, URI uriLong) {
		super(uriShort);
		this.uriLong = uriLong;
	}

	@Override
	public String toSPARQL() {
		return uriLong.toSPARQL();
	}

	public static Identifier toIdentifier(URI uri) {
		String shortURI = uri.toString();
		int index = shortURI.indexOf(':');
		if (index == -1 || shortURI.charAt(index) == '/') return new Identifier("lns", shortURI);
		return new Identifier(shortURI.substring(0, index), shortURI.substring(index + 1));
	}
}
