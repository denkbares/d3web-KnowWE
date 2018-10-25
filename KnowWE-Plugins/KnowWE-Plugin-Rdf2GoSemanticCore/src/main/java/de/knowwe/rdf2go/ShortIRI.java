package de.knowwe.rdf2go;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import com.denkbares.strings.Identifier;

public class ShortIRI extends SimpleIRI {

	private static final long serialVersionUID = -5221506155017259902L;

	ShortIRI(String uriShort) {
		super(uriShort);
	}

	public static Identifier toIdentifier(IRI uri) {
		String shortURI = uri.toString();
		int index = shortURI.indexOf(':');
		if (index == -1 || shortURI.charAt(index) == '/') return new Identifier("lns", shortURI);
		return new Identifier(shortURI.substring(0, index), shortURI.substring(index + 1));
	}
}
