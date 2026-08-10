/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.search.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.util.lucene.LuceneIndexCompatibility;
import com.denkbares.util.lucene.LuceneUtils;
import de.knowwe.search.analysis.WikiAnalyzers;

/**
 * Owns the Lucene index of the section search: one writer, one near-real-time searcher, and the rules for when the
 * index on disk may still be used.
 * <p>
 * Deliberately knows nothing about JSPWiki or KnowWE, so it can be tested against a temporary directory. The wiki
 * facing part lives in the service that drives it.
 * <p>
 * Near real time means new documents become visible through {@link SearcherManager#maybeRefresh()} without a commit, so
 * an edited page can be searchable within seconds while commits stay rare.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearchIndex implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiSearchIndex.class);

	private static final String COMMIT_SCHEMA_VERSION = "schemaVersion";
	private static final int RAM_BUFFER_MB = 64;

	private final Path path;
	private final Directory directory;
	private final IndexWriter writer;
	private final SearcherManager searcherManager;

	/**
	 * Opens the index, rebuilding from scratch if what is on disk cannot be trusted: a different schema version, or a
	 * Lucene format this version cannot read.
	 */
	public WikiSearchIndex(@NotNull Path path) throws IOException {
		this.path = path;
		if (Files.isDirectory(path) && !isCompatible(path)) {
			LOGGER.info("Discarding incompatible search index at {}", path);
			deleteRecursively(path);
		}
		Files.createDirectories(path);
		this.directory = FSDirectory.open(path);
		this.writer = new IndexWriter(directory, new IndexWriterConfig(WikiAnalyzers.forIndexing())
				.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
				.setRAMBufferSizeMB(RAM_BUFFER_MB));
		// applyAllDeletes so a replaced page never shows up twice, writeAllDeletes false to keep it cheap
		this.searcherManager = new SearcherManager(writer, true, false, null);
	}

	/**
	 * Replaces every document of the page in one step. Never delete and add separately: that opens a window in which
	 * the page is missing from the results.
	 */
	public void replacePage(@NotNull String title, @NotNull List<Document> documents) throws IOException {
		Term key = new Term(SearchFields.REPLACE_KEY, SectionDocumentBuilder.pageKey(title));
		if (documents.isEmpty()) {
			writer.deleteDocuments(key);
		}
		else {
			writer.updateDocuments(key, stamped(documents, key));
		}
	}

	/**
	 * Marks what this write replaces.
	 * <p>
	 * Set here rather than by whoever builds the document: it says what the write does, not what the document is, and a
	 * builder that forgot it would leave documents that can never be replaced -- they would quietly pile up on every
	 * re-index.
	 */
	private static List<Document> stamped(List<Document> documents, Term key) {
		for (Document document : documents) {
			document.removeFields(key.field());
			document.add(new StringField(key.field(), key.text(), Field.Store.NO));
		}
		return documents;
	}

	public void removePage(@NotNull String title) throws IOException {
		// by page key, not by replace key: this takes the attachments of the page with it, which is what a deleted
		// page means
		writer.deleteDocuments(new Term(SearchFields.PAGE_KEY, SectionDocumentBuilder.pageKey(title)));
	}

	public void replaceAttachment(@NotNull String path, @NotNull Document document) throws IOException {
		Term key = new Term(SearchFields.REPLACE_KEY, AttachmentDocumentBuilder.replaceKey(path));
		writer.updateDocument(key, stamped(List.of(document), key).get(0));
	}

	public void removeAttachment(@NotNull String path) throws IOException {
		writer.deleteDocuments(new Term(SearchFields.REPLACE_KEY, AttachmentDocumentBuilder.replaceKey(path)));
	}

	/** Makes everything written so far visible to searchers, without the cost of a commit. */
	public void refresh() throws IOException {
		searcherManager.maybeRefresh();
	}

	/** Persists to disk and stamps the schema version, so the next start knows whether it may reuse this index. */
	public void commit() throws IOException {
		LuceneUtils.addCommitData(writer, COMMIT_SCHEMA_VERSION, SearchFields.SCHEMA_VERSION);
		writer.commit();
		searcherManager.maybeRefresh();
	}

	/** Runs a read against a searcher that is released again afterwards even if the task throws. */
	public <T> T search(@NotNull SearchTask<T> task) throws IOException {
		IndexSearcher searcher = searcherManager.acquire();
		try {
			return task.run(searcher);
		}
		finally {
			searcherManager.release(searcher);
		}
	}

	public int documentCount() {
		return writer.getDocStats().numDocs;
	}

	public @NotNull Path getPath() {
		return path;
	}

	/**
	 * Order matters. An index writer left open keeps the index files undeletable on Windows for the lifetime of the
	 * JVM, which is a known trap in this codebase.
	 */
	@Override
	public void close() throws IOException {
		try {
			commit();
		}
		catch (IOException e) {
			LOGGER.warn("Could not commit search index at {} while closing", path, e);
		}
		searcherManager.close();
		writer.close();
		directory.close();
	}

	/**
	 * An index may be reused only if it was written by this schema version and in a readable Lucene format. Both are
	 * checked, because the directory name alone would not notice an analyzer change.
	 */
	private static boolean isCompatible(Path path) {
		LuceneIndexCompatibility.IndexFormat format = LuceneIndexCompatibility.probeFormat(path);
		if (format != LuceneIndexCompatibility.IndexFormat.CURRENT) {
			LOGGER.info("Search index at {} has format {}: {}", path, format,
					LuceneIndexCompatibility.checkReadable(path));
			return false;
		}
		try (Directory directory = FSDirectory.open(path); DirectoryReader reader = DirectoryReader.open(directory)) {
			Map<String, String> commitData = reader.getIndexCommit().getUserData();
			String version = commitData.get(COMMIT_SCHEMA_VERSION);
			if (!SearchFields.SCHEMA_VERSION.equals(version)) {
				LOGGER.info("Search index at {} was written by schema {}, expected {}",
						path, version, SearchFields.SCHEMA_VERSION);
				return false;
			}
			return true;
		}
		catch (IOException e) {
			LOGGER.info("Cannot read search index at {}, rebuilding: {}", path, e.getMessage());
			return false;
		}
	}

	private static void deleteRecursively(Path path) throws IOException {
		try (var paths = Files.walk(path)) {
			for (Path each : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(each);
			}
		}
	}

	@FunctionalInterface
	public interface SearchTask<T> {
		T run(IndexSearcher searcher) throws IOException;
	}
}
