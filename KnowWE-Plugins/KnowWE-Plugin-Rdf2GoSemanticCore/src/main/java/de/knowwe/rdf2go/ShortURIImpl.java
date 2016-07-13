package de.knowwe.rdf2go;

import org.openrdf.model.URI;

import com.denkbares.strings.Identifier;

public class ShortURIImpl extends org.openrdf.model.impl.URIImpl {

	private static final long serialVersionUID = -5221506155017259902L;
	private final URI uriLong;

	ShortURIImpl(String uriShort, URI uriLong) {
		super(uriShort);
		this.uriLong = uriLong;
	}

	public static Identifier toIdentifier(URI uri) {
		String shortURI = uri.toString();
		int index = shortURI.indexOf(':');
		if (index == -1 || shortURI.charAt(index) == '/') return new Identifier("lns", shortURI);
		return new Identifier(shortURI.substring(0, index), shortURI.substring(index + 1));
	}
}
