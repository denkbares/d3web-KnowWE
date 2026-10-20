package com.denkbares.knowwe.textdiff;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a static HTML demo page that exercises both web components with a few representative diffs. Open the
 * resulting file in a browser to eyeball rendering, theming, the layout and whitespace switches and the folded
 * context. Not part of the production artefact.
 */
class DemoPageGenerator {

	public static void main(String[] args) throws Exception {
		Path target = Path.of(args.length > 0 ? args[0] : "target/demo.html").toAbsolutePath().normalize();
		if (target.getParent() != null) {
			Files.createDirectories(target.getParent());
		}
		Path resourceBase = findWebappResourceBase();
		Path themeVariables = findThemeVariables(resourceBase);
		Files.writeString(target, buildPage(resourceBase.toUri().toString(),
				themeVariables == null ? null : themeVariables.toUri().toString()));
		System.out.println("Wrote demo to " + target.toAbsolutePath());
		System.out.println("Using resources from " + resourceBase);
		System.out.println(themeVariables == null
				? "KnowWE theme variables not found, the components show their fallback colors"
				: "Using KnowWE theme variables from " + themeVariables);
	}

	/**
	 * The theme variables of the wiki, which the components follow where a page defines them. They live in the
	 * resources module next to this plugin, a demo without them shows the fallback colors only.
	 */
	private static Path findThemeVariables(Path resourceBase) {
		for (Path path = resourceBase; path != null; path = path.getParent()) {
			Path candidate = path.resolve("KnowWE-Resources/src/main/webapp/KnowWEExtension/css/variables.css");
			if (Files.isRegularFile(candidate)) return candidate.normalize();
		}
		return null;
	}

	private static final String CODEMIRROR_BUNDLE = "../../../../../KnowWE-Resources/src/main/webapp/KnowWEExtension/scripts/codemirror.min.js";

	private static Path findWebappResourceBase() throws Exception {
		Path cwd = Path.of("").toAbsolutePath().normalize();
		Path candidate = cwd.resolve("src/main/resources/webapp").normalize();
		if (isWebappResourceBase(candidate)) return candidate;

		for (Path path = cwd; path != null; path = path.getParent()) {
			candidate = path.resolve("KnowWE-Plugin-TextDiff/src/main/resources/webapp").normalize();
			if (isWebappResourceBase(candidate)) return candidate;

			candidate = path.resolve("KnowWE/KnowWE-Essentials/KnowWE-Plugin-TextDiff/src/main/resources/webapp").normalize();
			if (isWebappResourceBase(candidate)) return candidate;
		}

		URL resource = DemoPageGenerator.class.getClassLoader()
				.getResource("webapp/KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js");
		if (resource != null && "file".equals(resource.getProtocol())) {
			Path script = Path.of(resource.toURI()).normalize();
			candidate = script.getParent().getParent().getParent();
			if (isWebappResourceBase(candidate)) return candidate;
		}

		throw new IllegalStateException("Could not locate TextDiff webapp resources. Expected to find "
				+ "KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js and "
				+ "KnowWEExtension/css/KnowWE-Plugin-TextDiff.css below a webapp directory.");
	}

	private static boolean isWebappResourceBase(Path path) {
		return Files.isRegularFile(path.resolve("KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js"))
			   && Files.isRegularFile(path.resolve("KnowWEExtension/css/KnowWE-Plugin-TextDiff.css"));
	}

	private static String buildPage(String resourceBaseUri, String themeVariablesUri) {
		DiffRenderOptions auto = DiffRenderOptions.defaults().withView(DiffView.AUTO).withContext("demo");

		String oldShort = "= Title =\nFirst line.\nSecond line.\nThird line.\n";
		String newShort = "= Title =\nFirst line.\nSecond line, edited.\nThird line.\nFourth line added.\n";

		StringBuilder longOld = new StringBuilder();
		StringBuilder longNew = new StringBuilder();
		for (int i = 1; i <= 30; i++) {
			longOld.append("line ").append(i).append('\n');
			longNew.append("line ").append(i).append('\n');
		}
		longOld.insert(0, "header (old)\n");
		longNew.insert(0, "header (new)\n");
		longOld.append("trailer (old)\n");
		longNew.append("trailer (new)\n");

		String htmlChars = "function greet(name) {\n  return \"<b>\" + name + \"</b>\";\n}\n";
		String htmlChars2 = "function greet(name) {\n  return `<b>${name}</b>`;\n}\n";

		StringBuilder page = new StringBuilder();
		page.append("""
				<!DOCTYPE html>
				<html lang="en">
				<head>
				<meta charset="utf-8">
				<base href="%s">
				<title>KnowWE TextDiff demo</title>
				%s
				<style>
				  body { font: 14px system-ui, sans-serif; max-width: 960px; margin: 2em auto; padding: 0 1em; transition: background 0.15s, color 0.15s; }
				  body.light-mode { background: var(--bg-color-primary, #ffffff); color: var(--text-color-primary, #1f2328); }
				  body.dark-mode  { background: var(--bg-color-primary, #0d1117); color: var(--text-color-primary, #e6edf3); }
				  body.dark-mode h2 { border-bottom-color: #30363d; }
				  section { margin: 2em 0; }
				  h2 { border-bottom: 1px solid #d0d7de; padding-bottom: 0.25em; }
				  .file-change-demo { display: flex; flex-direction: column; gap: 1rem; }
				</style>
				</head>
				<body class="light-mode">
				<h1>KnowWE TextDiff demo</h1>
				<p>Each section renders <code>&lt;knowwe-text-diff&gt;</code> or wraps it in
				  <code>&lt;knowwe-file-change&gt;</code> with declarative shadow DOM.</p>
				<p>
				  <button id="toggle-dark" type="button">Toggle dark mode</button>
				  <button id="toggle-variables" type="button">Toggle KnowWE theme variables</button>
				  <em style="margin-left: 1em; color: #6e7781;">switches the body class the way the wiki does; in the wiki that only happens on page load, so the demo re-themes the components itself</em>
				</p>
				<script>
				  window.knowweTextDiffResourceBase = %s;
				  // stands in for KnowWE-helper.js, which is not available outside the wiki
				  window.KNOWWE = { helper: { getDisplayMode: () => document.body.classList.contains('dark-mode') ? 'dark' : 'light' } };
				  document.getElementById('toggle-dark').addEventListener('click', () => {
				    const b = document.body;
				    b.classList.toggle('dark-mode');
				    b.classList.toggle('light-mode');
				    const theme = KNOWWE.helper.getDisplayMode();
				    document.querySelectorAll('knowwe-text-diff, knowwe-file-change')
				      .forEach(el => el.setAttribute('data-theme', theme));
				  });
				  // compares the wiki look with the fallback colors the components have on any other page
				  document.getElementById('toggle-variables').addEventListener('click', () => {
				    const link = document.getElementById('knowwe-variables');
				    if (link) link.disabled = !link.disabled;
				  });
				</script>
				""".formatted(resourceBaseUri,
				themeVariablesUri == null ? "" : "<link id=\"knowwe-variables\" rel=\"stylesheet\" href=\"" + themeVariablesUri + "\">",
				jsString(resourceBaseUri)));

		page.append("<section><h2>1. Short edit</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(oldShort, newShort)))
				.append("</section>");

		page.append("<section><h2>2. Long file with folded context (3 lines)</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(longOld.toString(), longNew.toString())))
				.append("</section>");

		page.append("<section><h2>3. HTML-special characters in content</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(htmlChars, htmlChars2)))
				.append("</section>");

		page.append("<section><h2>4. New file (null old)</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(null, "alpha\nbeta\ngamma\n")))
				.append("</section>");

		page.append("<section><h2>5. Deleted file (null new)</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff("alpha\nbeta\ngamma\n", null)))
				.append("</section>");

		page.append("<section><h2>6. No folding (contextLines = -1), side by side where wide enough</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(longOld.toString(), longNew.toString()),
						auto.withContextLines(-1)))
				.append("</section>");

		String createdArticle = "%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n}\n%%\n";
		String deletedArticle = "%%COOM:DSL\nproduct LegacyBike {\n\tbool deprecated\n}\n%%\n";
		String modifiedOld = "%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n\tbool available\n}\n%%\n";
		String modifiedNew = "%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n\tnum price\n\tbool available\n}\n%%\n";
		String renamedModifiedOld = "@package: demo_old\n\n%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n}\n%%\n";
		String renamedModifiedNew = "@package: demo_new\n\n%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n\tnum price\n}\n%%\n";

		page.append("""
				<section>
				  <h2>7. File/article change frames</h2>
				  <p>The reusable <code>&lt;knowwe-file-change&gt;</code> component renders a GitHub-inspired
				    file header with the layout and whitespace switches and leaves content diffs to
				    <code>&lt;knowwe-text-diff&gt;</code>. The choices are shared by all diffs of the demo context.</p>
				  <div class="file-change-demo">
				""");
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.ADDED, null, "CreatedArticle", "https://example.com/wiki/CreatedArticle", null, null,
				new TextDiff(null, createdArticle)), auto));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.DELETED, "DeletedArticle", null, "https://example.com/wiki/DeletedArticle", null, null,
				new TextDiff(deletedArticle, null)), auto));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.MODIFIED, "ModifiedArticle", "ModifiedArticle", "https://example.com/wiki/ModifiedArticle",
				null, null, new TextDiff(modifiedOld, modifiedNew)), auto));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.RENAMED, "OldArticleName", "NewArticleName", null,
				"https://example.com/wiki/OldArticleName", "https://example.com/wiki/NewArticleName", null), auto));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.RENAMED_MODIFIED, "OldArticleWithContent", "NewArticleWithContent", null,
				"https://example.com/wiki/OldArticleWithContent", "https://example.com/wiki/NewArticleWithContent",
				new TextDiff(renamedModifiedOld, renamedModifiedNew)), auto));
		page.append("""
				  </div>
				</section>
				""");

		page.append("""
				<section>
				  <h2>8. Texts given as attributes</h2>
				  <p>A host without server rendered content diffs the texts of its attributes once it scrolls near the viewport.</p>
				  <knowwe-text-diff data-context="demo" data-old-text="config:&#10;  retries: 3&#10;  timeout: 30s&#10;"
				                    data-new-text="config:&#10;  retries: 5&#10;  timeout: 60s&#10;  features:&#10;    - x&#10;"></knowwe-text-diff>
				</section>
				""");

		// the component script is built against the vendored CodeMirror bundle of KnowWE-Resources, which has to load
		// first
		page.append("<script src=\"" + CODEMIRROR_BUNDLE + "\"></script>");
		page.append("<script src=\"KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js\"></script>");
		page.append("</body></html>");
		return withDemoResourceUrls(page.toString(), resourceBaseUri);
	}

	private static String htmlAttribute(String value) {
		return value
				.replace("&", "&amp;")
				.replace("\"", "&quot;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	private static String withDemoResourceUrls(String html, String resourceBaseUri) {
		String knowweExtension = htmlAttribute(resourceBaseUri + "KnowWEExtension/");
		return html
				.replace("href=\"KnowWEExtension/", "href=\"" + knowweExtension)
				.replace("src=\"KnowWEExtension/", "src=\"" + knowweExtension);
	}

	/** Encode an arbitrary string as a JavaScript string literal (with surrounding quotes). */
	private static String jsString(String s) {
		StringBuilder out = new StringBuilder(s.length() + 2);
		out.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\\' -> out.append("\\\\");
				case '"' -> out.append("\\\"");
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\t' -> out.append("\\t");
				case '<' -> out.append("\\u003c"); // avoid premature </script>
				default -> {
					if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
					else out.append(c);
				}
			}
		}
		out.append('"');
		return out.toString();
	}
}
