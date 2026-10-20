// @vitest-environment jsdom
import {afterEach, beforeAll, describe, expect, it, vi} from "vitest";
import {renderTable} from "../src/renderer/table.js";
import {computeDiff} from "../src/diff/index.js";
import {fixtures} from "./fixtures.js";

// jsdom has no IntersectionObserver, every element counts as visible at once
class VisibleAtOnce {
	constructor(callback) {
		this.callback = callback;
	}

	observe(target) {
		this.callback([{target, isIntersecting: true}]);
	}

	disconnect() {
	}
}

const settled = () => new Promise(resolve => setTimeout(resolve, 0));

beforeAll(async () => {
	window.IntersectionObserver = VisibleAtOnce;
	await import("../src/index.js");
});

afterEach(() => {
	document.body.replaceChildren();
	delete window.knowweTextDiffFetch;
	delete window.CodeMirror;
});

function response(body, ok = true) {
	return {ok, status: ok ? 200 : 500, text: async () => body, json: async () => JSON.parse(body)};
}

describe("table renderer", () => {
	it("renders the same rows as the server for a fixture", () => {
		const f = fixtures.find(x => x.name === "wiki-table");
		const html = renderTable(computeDiff(f.old, f.new));
		expect(html.match(/class="line removed"/g)).toHaveLength(2);
		expect(html.match(/class="line added"/g)).toHaveLength(3);
		expect(html).not.toContain("elided");
		expect(renderTable(computeDiff("a", "<b>&"))).toContain("&lt;b&gt;&amp;");
	});

	it("folds unchanged regions beyond the context lines", () => {
		const f = fixtures.find(x => x.name === "repeated-lines");
		const html = renderTable(computeDiff(f.old, f.new));
		expect(html).toContain('data-hidden-count="11"');
		const hidden = [...html.matchAll(/data-hidden-count="(\d+)"/g)].reduce((sum, m) => sum + Number(m[1]), 0);
		expect(html.match(/class="line common hidden"/g)).toHaveLength(hidden);
	});

	it("notes an empty diff", () => {
		expect(renderTable(computeDiff("a\n", "a\n"))).toContain("No changes.");
	});

	it("uses a single number column for a created file", () => {
		expect(renderTable(computeDiff(null, "a\nb\n"))).toContain('class="diff single-line-number new-only"');
	});
});

describe("knowwe-text-diff", () => {
	it("hydrates a server rendered table and wires the expanders", async () => {
		document.body.innerHTML = `<knowwe-text-diff><template shadowrootmode="open"><div class="diff-frame">
			<table><tbody><tr class="elided"><td><button class="expand"></button></td></tr>
			<tr class="line common hidden"></tr><tr class="line common hidden"></tr><tr class="line added"></tr></tbody></table>
			</div></template></knowwe-text-diff>`;
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		el.shadowRoot.querySelector("button.expand").click();
		expect(el.shadowRoot.querySelectorAll("tr.hidden")).toHaveLength(0);
		expect(el.shadowRoot.querySelector("tr.elided")).toBeNull();
	});

	it("posts slotted texts to the backend with the table renderer", async () => {
		const fetch = vi.fn(async () => response('<div class="diff-frame"><table></table></div>'));
		window.knowweTextDiffFetch = fetch;
		document.body.innerHTML = `<knowwe-text-diff data-context-lines="5">
			<script type="text/plain" slot="old">a</script><script type="text/plain" slot="new">b</script></knowwe-text-diff>`;
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		expect(fetch).toHaveBeenCalledOnce();
		expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual({oldText: "a", newText: "b", contextLines: 5});
		expect(el.getAttribute("data-status")).toBe("ready");
		expect(el.shadowRoot.querySelector("table")).not.toBeNull();
	});

	it("reports a failed request", async () => {
		window.knowweTextDiffFetch = async () => response("", false);
		document.body.innerHTML = `<knowwe-text-diff data-action-url="action/X"></knowwe-text-diff>`;
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(el.getAttribute("data-status")).toBe("error");
		expect(el.getAttribute("data-status-message")).toBe("HTTP 500");
	});

	it("loads JSON and builds the view with the CodeMirror renderer", async () => {
		window.CodeMirror = {merge: {}};
		const fetch = vi.fn(async () => response(JSON.stringify({old: "a\nb\n", new: "a\nB\n", stats: {added: 1, removed: 1}})));
		window.knowweTextDiffFetch = fetch;
		const stats = vi.fn();
		document.body.innerHTML = `<knowwe-file-change data-additions="7" data-deletions="9">
			<knowwe-text-diff data-renderer="codemirror" data-action-url="action/X?file=f"></knowwe-text-diff></knowwe-file-change>`;
		document.body.addEventListener("knowwe-text-diff-stats", stats);
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(fetch.mock.calls[0][0]).toContain("format=json");
		expect(el.getAttribute("data-status")).toBe("ready");
		expect(el.shadowRoot.querySelector(".diff-frame.cm-diff .cm-editor")).not.toBeNull();
		expect(el.stats).toEqual({added: 1, removed: 1});
		expect(stats).toHaveBeenCalledOnce();
		const change = document.querySelector("knowwe-file-change");
		expect(change.getAttribute("data-additions")).toBe("1");
		expect(change.getAttribute("data-deletions")).toBe("1");
	});

	it("uses inline data of a server rendered CodeMirror host without a request", async () => {
		window.CodeMirror = {merge: {}};
		const fetch = vi.fn();
		window.knowweTextDiffFetch = fetch;
		document.body.innerHTML = `<knowwe-text-diff data-renderer="codemirror" data-action-url="action/X"><template shadowrootmode="open">
			<div class="diff-frame diff-placeholder" style="--diff-placeholder-lines: 3"></div>
			<script type="application/json" class="diff-data">{"old":"a\\n","new":"b\\n","stats":{"added":1,"removed":1}}</script>
			</template></knowwe-text-diff>`;
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(fetch).not.toHaveBeenCalled();
		expect(el.shadowRoot.querySelector("script.diff-data")).toBeNull();
		expect(el.shadowRoot.querySelector(".diff-placeholder")).toBeNull();
		expect(el.shadowRoot.querySelector(".cm-editor")).not.toBeNull();
		expect(el.stats).toEqual({added: 1, removed: 1});
	});

	it("diffs slotted texts in the browser with the CodeMirror renderer", async () => {
		window.CodeMirror = {merge: {}};
		const fetch = vi.fn();
		window.knowweTextDiffFetch = fetch;
		document.body.innerHTML = `<knowwe-text-diff data-renderer="codemirror" data-new-null>
			<script type="text/plain" slot="old">x\ny</script></knowwe-text-diff>`;
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(fetch).not.toHaveBeenCalled();
		expect(el.stats).toEqual({added: 0, removed: 2});
		expect(el.shadowRoot.querySelector(".cm-editor")).not.toBeNull();
	});

	it("stays a table without the CodeMirror bundle", async () => {
		window.knowweTextDiffFetch = vi.fn(async () => response('<div class="diff-frame"></div>'));
		document.body.innerHTML = `<knowwe-text-diff data-renderer="codemirror" data-action-url="action/X"></knowwe-text-diff>`;
		await settled();
		expect(window.knowweTextDiffFetch.mock.calls[0][0]).not.toContain("format=json");
	});
});
