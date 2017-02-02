package de.knowwe.diaflux.utils;

/**
 * @author Adrian Müller
 * @created 27.01.17
 */
public abstract class AbstractDiaFluxConverter {

	protected StringBuilder res;

	StringBuilder convert(String start, String end) {
		res = new StringBuilder();
		res.append(start);
		res.append("\n");
		createHeader();
		res.append("\n");
		createNodeList();
		res.append("\n");
		createEdgeList();
		res.append(end);
		res.append("\n");
		return res;
	}

	protected abstract void createHeader();

	protected abstract void createNodeList();

	protected abstract void createEdgeList();
}
