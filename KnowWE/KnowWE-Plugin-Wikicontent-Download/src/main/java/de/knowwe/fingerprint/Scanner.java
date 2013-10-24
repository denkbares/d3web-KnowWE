package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;

import de.knowwe.core.kdom.Article;

/**
 * Class for creating particular perspectives of a compiled article.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 01.10.2013
 */
public interface Scanner {

	/**
	 * Creates a single record of a article describing a particular perspective
	 * of the articles compiled content. The method shall create the target file
	 * as a single file. If this Scanner is not suitable to create a record for
	 * that article, no target file shall be created.
	 * 
	 * 
	 * @created 01.10.2013
	 * @param article the article to be scanned
	 * @param target the resulting description file
	 */
	void scan(Article article, File target) throws IOException;

	/**
	 * Returns the file extension that should be used for the target files of
	 * this scanner.
	 * 
	 * @created 01.10.2013
	 * @return the file extension, including the separating character, e.g.
	 *         ".txt"
	 */
	String getExtension();

	/**
	 * Returns a short name of the items to be scanned.
	 * 
	 * @created 01.10.2013
	 * @return a short item name
	 */
	String getItemName();

	/**
	 * Returns the comparison of two files produced by this scanner describes
	 * the same finger-print.
	 * 
	 * @created 01.10.2013
	 * @param file1 a created file of this scanner
	 * @param file2 a created file of this scanner
	 * @return compare object describing the equality of the scanned contents
	 * @throws IOException if any of the specified files cannot be read as
	 *         expected
	 */
	Diff compare(File file1, File file2) throws IOException;
}
