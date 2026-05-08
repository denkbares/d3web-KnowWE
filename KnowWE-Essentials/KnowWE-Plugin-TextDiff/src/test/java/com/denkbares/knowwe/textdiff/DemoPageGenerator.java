package com.denkbares.knowwe.textdiff;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a static HTML demo page that exercises the {@code <knowwe-text-diff>} web component
 * with a few representative diffs. Open the resulting file in a browser to eyeball rendering,
 * theming, and the "expand elided region" interaction. Not part of the production artefact.
 */
class DemoPageGenerator {

	public static void main(String[] args) throws Exception {
		Path target = Path.of(args.length > 0 ? args[0] : "target/demo.html").toAbsolutePath().normalize();
		if (target.getParent() != null) {
			Files.createDirectories(target.getParent());
		}
		Path resourceBase = findWebappResourceBase();
		Files.writeString(target, buildPage(resourceBase.toUri().toString()));
		System.out.println("Wrote demo to " + target.toAbsolutePath());
		System.out.println("Using resources from " + resourceBase);
	}

	private static Path findWebappResourceBase() throws Exception {
		Path cwd = Path.of("").toAbsolutePath().normalize();
		Path candidate = cwd.resolve("src/main/resources/webapp").normalize();
		if (isWebappResourceBase(candidate)) return candidate;

		for (Path path = cwd; path != null; path = path.getParent()) {
			candidate = path.resolve("KnowWE-Plugin-TextDiff/src/main/resources/webapp").normalize();
			if (isWebappResourceBase(candidate)) return candidate;

			candidate = path.resolve("KnowWE-DES/KnowWE-Plugins-DES/KnowWE-Plugin-TextDiff/src/main/resources/webapp").normalize();
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

	private static String buildPage(String resourceBaseUri) {

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
				<style>
				  body { font: 14px system-ui, sans-serif; max-width: 960px; margin: 2em auto; padding: 0 1em; transition: background 0.15s, color 0.15s; }
				  body.light-mode { background: #ffffff; color: #1f2328; }
				  body.dark-mode  { background: #0d1117; color: #e6edf3; }
				  body.dark-mode h2 { border-bottom-color: #30363d; }
				  section { margin: 2em 0; }
				  h2 { border-bottom: 1px solid #d0d7de; padding-bottom: 0.25em; }
				  .file-change-demo { display: flex; flex-direction: column; gap: 1rem; }
				</style>
				</head>
				<body class="light-mode">
				<h1>KnowWE TextDiff — demo</h1>
				<p>Each section renders <code>&lt;knowwe-text-diff&gt;</code> or wraps it in
				  <code>&lt;knowwe-file-change&gt;</code> with declarative shadow DOM.</p>
				<p>
				  <button id="toggle-dark" type="button">Toggle dark mode</button>
				  <em style="margin-left: 1em; color: #6e7781;">sets the page class and component <code>data-theme</code></em>
				</p>
				<script>
				  window.knowweTextDiffResourceBase = %s;
				  window.syncDemoTheme = () => {
				    const theme = document.body.classList.contains('dark-mode') ? 'dark' : 'light';
				    document.querySelectorAll('knowwe-text-diff, knowwe-file-change')
				      .forEach(el => el.setAttribute('data-theme', theme));
				  };
				  document.getElementById('toggle-dark').addEventListener('click', () => {
				    const b = document.body;
				    b.classList.toggle('dark-mode');
				    b.classList.toggle('light-mode');
				    window.syncDemoTheme();
				  });
				</script>
				""".formatted(resourceBaseUri, jsString(resourceBaseUri)));

		page.append("<section><h2>1. Short edit</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(oldShort, newShort)))
				.append("</section>");

		page.append("<section><h2>2. Long file with elided context (3 lines)</h2>")
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

		page.append("<section><h2>6. No elision (contextLines = -1)</h2>")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(longOld.toString(), longNew.toString()),
						DiffRenderOptions.defaults().withContextLines(-1)))
				.append("</section>");

		page.append("""
				<section>
				  <h2>7. Custom theming via ::part()</h2>
				  <p>The host page tints this instance via <code>::part(line added)</code> and friends.</p>
				  <style>
				    .themed knowwe-text-diff::part(line added)   { background: #fff3bf; color: #5c3c00; }
				    .themed knowwe-text-diff::part(line removed) { background: #f3d9fa; color: #5f3dc4; }
				    .themed knowwe-text-diff::part(num)          { font-style: italic; }
				  </style>
				""");
		page.append("<div class=\"themed\">")
				.append(DiffHtmlRenderer.renderTextDiff(new TextDiff(oldShort, newShort)))
				.append("</div></section>");

		String createdArticle = "%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n}\n%%\n";
		String deletedArticle = "%%COOM:DSL\nproduct LegacyBike {\n\tbool deprecated\n}\n%%\n";
		String modifiedOld = "%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n\tbool available\n}\n%%\n";
		String modifiedNew = "%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n\tnum price\n\tbool available\n}\n%%\n";
		String renamedModifiedOld = "@package: demo_old\n\n%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n}\n%%\n";
		String renamedModifiedNew = "@package: demo_new\n\n%%COOM:DSL\nproduct DemoBike {\n\tnum weight\n\tnum price\n}\n%%\n";

		page.append("""
				<section>
				  <h2>8. File/article change frames</h2>
				  <p>The reusable <code>&lt;knowwe-file-change&gt;</code> component renders a GitHub-inspired
				    file header and leaves content diffs to <code>&lt;knowwe-text-diff&gt;</code>.</p>
				  <div class="file-change-demo">
				""");
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.ADDED,
				null,
				"CreatedArticle",
				"https://example.com/wiki/CreatedArticle",
				null,
				null,
				new TextDiff(null, createdArticle),
				false)));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.DELETED,
				"DeletedArticle",
				null,
				"https://example.com/wiki/DeletedArticle",
				null,
				null,
				new TextDiff(deletedArticle, null),
				false)));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.MODIFIED,
				"ModifiedArticle",
				"ModifiedArticle",
				"https://example.com/wiki/ModifiedArticle",
				null,
				null,
				new TextDiff(modifiedOld, modifiedNew),
				false)));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.RENAMED,
				"OldArticleName",
				"NewArticleName",
				null,
				"https://example.com/wiki/OldArticleName",
				"https://example.com/wiki/NewArticleName",
				null,
				false)));
		page.append(DiffHtmlRenderer.renderFileChange(new FileChange(
				FileChange.ChangeType.RENAMED_MODIFIED,
				"OldArticleWithContent",
				"NewArticleWithContent",
				null,
				"https://example.com/wiki/OldArticleWithContent",
				"https://example.com/wiki/NewArticleWithContent",
				new TextDiff(renamedModifiedOld, renamedModifiedNew),
				true)));
		page.append("""
				  </div>
				</section>
				""");

		String lazyOld = "config:\n  retries: 3\n  timeout: 30s\n  endpoint: api.example.com\n";
		String lazyNew = "config:\n  retries: 5\n  timeout: 60s\n  endpoint: api.example.com\n  features:\n    - x\n    - y\n";
		String lazyResponse = withDemoResourceUrls(DiffHtmlRenderer.renderTextDiffShadowContent(new TextDiff(lazyOld, lazyNew),
				DiffRenderOptions.defaults().withContextLines(3)), resourceBaseUri);

		page.append("""
				<section>
				  <h2>9. Lazy-loaded via slotted text + mocked backend</h2>
				  <p>The component sees no pre-rendered shadow content, gathers raw text from
				    <code>&lt;script type="text/plain" slot="…"&gt;</code> children, and POSTs to
				    <code>action/TextDiffAction.action</code>. Below the fetch is intercepted and answered
				    with the server-pre-baked HTML so this works in a static demo.</p>
				  <script>
				    window.__demoLazyResponses = {
				      'demo-config': %s
				    };
				    window.knowweTextDiffFetch = async (url, init) => {
				      const payload = JSON.parse(init && init.body || '{}');
				      // demo: route by a marker in oldText (just look up the config example)
				      const key = (payload.oldText || '').includes('retries: 3') ? 'demo-config' : null;
				      const html = window.__demoLazyResponses[key];
				      if (!html) return new Response('not mocked', { status: 404 });
				      await new Promise(r => setTimeout(r, 400));   // simulate latency
				      return new Response(html, { status: 200, headers: { 'Content-Type': 'text/html' } });
				    };
				  </script>
				  <knowwe-text-diff data-context-lines="3">
				    <script type="text/plain" slot="old">%s</script>
				    <script type="text/plain" slot="new">%s</script>
				  </knowwe-text-diff>
				</section>
				""".formatted(jsString(lazyResponse), lazyOld, lazyNew));

		page.append("""
				<section>
				  <h2>10. Programmatic load via JS properties</h2>
				  <p>Same lazy path, but the consumer sets <code>el.oldText</code> / <code>el.newText</code> instead
				    of slotting text. Click the button to trigger.</p>
				  <button id="programmatic-trigger" type="button">Load diff</button>
				  <knowwe-text-diff id="programmatic-diff" data-context-lines="3"></knowwe-text-diff>
				  <script>
				    document.getElementById('programmatic-trigger').addEventListener('click', () => {
				      const el = document.getElementById('programmatic-diff');
				      el.oldText = "config:\\n  retries: 3\\n  timeout: 30s\\n  endpoint: api.example.com\\n";
				      el.newText = "config:\\n  retries: 5\\n  timeout: 60s\\n  endpoint: api.example.com\\n  features:\\n    - x\\n    - y\\n";
				    });
				  </script>
				</section>
				""");

		page.append("<script src=\"KnowWEExtension/scripts/KnowWE-Plugin-TextDiff.js\"></script>");
		page.append("<script>window.syncDemoTheme();</script>");
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
