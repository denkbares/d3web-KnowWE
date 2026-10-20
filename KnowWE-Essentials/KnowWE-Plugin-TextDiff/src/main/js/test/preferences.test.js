// @vitest-environment jsdom
import {afterEach, beforeAll, beforeEach, describe, expect, it, vi} from "vitest";
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

// a ResizeObserver the tests fire by hand
const resizeObservers = new Set();

class ManualResize {
	constructor(callback) {
		this.callback = callback;
	}

	observe(target) {
		this.target = target;
		resizeObservers.add(this);
	}

	disconnect() {
		resizeObservers.delete(this);
	}
}

// node ships a localStorage stub without the full interface, so the tests install their own
const stored = new Map();
const memoryStorage = {
	getItem: key => stored.has(key) ? stored.get(key) : null,
	setItem: (key, value) => stored.set(key, String(value)),
	removeItem: key => stored.delete(key),
	clear: () => stored.clear()
};

const settled = () => new Promise(resolve => setTimeout(resolve, 0));
const DATA = '<script type="application/json" class="diff-data">{"old":"a\\nb\\n","new":"a\\nB\\n","stats":{"added":1,"removed":1}}</script>';

function host(attributes) {
	return `<knowwe-text-diff data-renderer="codemirror" ${attributes}><template shadowrootmode="open">
		<div class="diff-frame diff-placeholder"></div>${DATA}</template></knowwe-text-diff>`;
}

function shown(el) {
	const frame = el.shadowRoot.querySelector(".diff-frame");
	return frame.querySelector(".cm-mergeView") ? "split" : frame.querySelector(".cm-editor") ? "unified" : null;
}

beforeAll(async () => {
	window.IntersectionObserver = VisibleAtOnce;
	window.ResizeObserver = ManualResize;
	Object.defineProperty(window, "localStorage", {value: memoryStorage, configurable: true});
	await import("../src/index.js");
});

beforeEach(() => {
	window.CodeMirror = {merge: {}};
	window.localStorage.clear();
});

afterEach(() => {
	document.body.replaceChildren();
	resizeObservers.clear();
	delete window.CodeMirror;
});

describe("layouts", () => {
	it("shows the requested layout and rebuilds the view when the attribute changes", async () => {
		document.body.innerHTML = host('data-layout="split"');
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		expect(shown(el)).toBe("split");
		expect(el.shadowRoot.querySelector(".diff-frame").getAttribute("data-layout")).toBe("split");
		expect(el.effectiveLayout).toBe("split");
		el.setAttribute("data-layout", "unified");
		expect(shown(el)).toBe("unified");
		expect(el.layout).toBe("unified");
	});

	it("announces its layout on connect and on every change", async () => {
		const events = [];
		document.body.addEventListener("knowwe-text-diff-layout", e => events.push(e.detail));
		document.body.innerHTML = host("");
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		expect(events).toEqual([{layout: "unified", effective: "unified", layouts: ["unified", "split"]}]);
		el.layout = "split";
		expect(events).toHaveLength(2);
		expect(events[1]).toEqual({layout: "split", effective: "split", layouts: ["unified", "split"]});
		expect(shown(el)).toBe("split");
	});

	it("keeps a choice per context and applies it to every element of the context", async () => {
		document.body.innerHTML = host('data-context="review"') + host('data-context="review"') + host('data-context="other"');
		const [first, second, other] = document.querySelectorAll("knowwe-text-diff");
		await settled();
		first.layout = "split";
		expect(window.localStorage.getItem("knowwe-text-diff.layout.review")).toBe("split");
		expect(shown(first)).toBe("split");
		expect(shown(second)).toBe("split");
		expect(shown(other)).toBe("unified");
		expect(first.getAttribute("data-layout")).toBeNull();
		document.body.innerHTML = host('data-context="review" data-layout="unified"');
		await settled();
		expect(shown(document.querySelector("knowwe-text-diff"))).toBe("split");
	});

	it("picks the layout by width in auto mode and follows resizes", async () => {
		let width = 600;
		vi.spyOn(HTMLElement.prototype, "getBoundingClientRect").mockImplementation(function () {
			return {top: 0, left: 0, width: this.tagName === "KNOWWE-TEXT-DIFF" ? width : 0, height: 0};
		});
		document.body.innerHTML = host('data-layout="auto"');
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		expect(shown(el)).toBe("unified");
		const observers = [...resizeObservers].filter(o => o.target === el);
		expect(observers).toHaveLength(1);
		width = 1200;
		observers[0].callback([]);
		expect(shown(el)).toBe("split");
		expect(el.layout).toBe("auto");
		expect(el.effectiveLayout).toBe("split");
		vi.restoreAllMocks();
	});

	it("rejects unknown layouts", async () => {
		document.body.innerHTML = host("");
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		expect(() => el.layout = "diagonal").toThrow();
	});

	it("has no layout with the table renderer", async () => {
		const events = [];
		document.body.addEventListener("knowwe-text-diff-layout", e => events.push(e.detail));
		document.body.innerHTML = `<knowwe-text-diff><template shadowrootmode="open"><div class="diff-frame"><table></table></div></template></knowwe-text-diff>`;
		await settled();
		expect(events).toEqual([]);
		expect(document.querySelector("knowwe-text-diff").effectiveLayout).toBeNull();
	});
});

describe("knowwe-file-change layout button", () => {
	const button = change => change.shadowRoot.querySelector(".file-change-layout");

	it("switches the layout of its diff and shows the layout a click leads to", async () => {
		document.body.innerHTML = `<knowwe-file-change data-new-name="A">${host('data-context="review"')}</knowwe-file-change>`;
		const change = document.querySelector("knowwe-file-change");
		const diff = document.querySelector("knowwe-text-diff");
		await settled();
		expect(button(change).hidden).toBe(false);
		expect(button(change).title).toBe("Show side by side");
		button(change).click();
		expect(shown(diff)).toBe("split");
		expect(button(change).title).toBe("Show in one column");
		expect(button(change).getAttribute("data-layout")).toBe("split");
		expect(change.hasAttribute("data-collapsed")).toBe(false);
		button(change).click();
		expect(shown(diff)).toBe("unified");
	});

	it("stays hidden for a table and for an empty body", async () => {
		document.body.innerHTML = `<knowwe-file-change data-new-name="A"><knowwe-text-diff><template shadowrootmode="open">
			<div class="diff-frame"><table></table></div></template></knowwe-text-diff></knowwe-file-change>
			<knowwe-file-change data-new-name="B"></knowwe-file-change>`;
		await settled();
		for (const change of document.querySelectorAll("knowwe-file-change")) expect(button(change).hidden).toBe(true);
	});
});

describe("whitespace mode", () => {
	const f = fixtures.find(x => x.name === "whitespace-only");
	const inline = attributes => `<knowwe-text-diff data-renderer="codemirror" ${attributes}><template shadowrootmode="open">
		<div class="diff-frame"></div><script type="application/json" class="diff-data">${JSON.stringify({old: f.old, new: f.new, stats: f.stats})}</script>
		</template></knowwe-text-diff>`;

	it("recomputes the diff without a request and reports the counts of the mode", async () => {
		const stats = [];
		document.body.addEventListener("knowwe-text-diff-stats", e => stats.push(e.detail));
		document.body.innerHTML = inline("");
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		expect(el.ignoreWhitespace).toBe(false);
		expect(el.stats).toEqual({added: 2, removed: 2});
		expect(shown(el)).toBe("unified");
		el.ignoreWhitespace = true;
		expect(el.getAttribute("data-whitespace")).toBe("ignore");
		expect(el.stats).toEqual({added: 0, removed: 0});
		expect(stats.map(s => [s.added, s.removed, s.ignoreWhitespace])).toEqual([[2, 2, false], [0, 0, true]]);
		expect(el.shadowRoot.querySelector(".diff-note").textContent).toBe("Only whitespace changed.");
		expect(shown(el)).toBeNull();
		el.ignoreWhitespace = false;
		expect(shown(el)).toBe("unified");
	});

	it("keeps the choice per context and announces it", async () => {
		const events = [];
		document.body.addEventListener("knowwe-text-diff-whitespace", e => events.push(e.detail.ignore));
		document.body.innerHTML = inline('data-context="review"') + inline('data-context="review"');
		const [first, second] = document.querySelectorAll("knowwe-text-diff");
		await settled();
		expect(events).toEqual([false, false]);
		second.ignoreWhitespace = true;
		expect(window.localStorage.getItem("knowwe-text-diff.whitespace.review")).toBe("ignore");
		expect(first.ignoreWhitespace).toBe(true);
		expect(first.stats).toEqual({added: 0, removed: 0});
		expect(events).toEqual([false, false, true, true]);
	});

	it("notes an unchanged text", async () => {
		document.body.innerHTML = `<knowwe-text-diff data-renderer="codemirror"><template shadowrootmode="open">
			<div class="diff-frame"></div><script type="application/json" class="diff-data">{"old":"a\\n","new":"a\\n","stats":{"added":0,"removed":0}}</script>
			</template></knowwe-text-diff>`;
		await settled();
		expect(document.querySelector("knowwe-text-diff").shadowRoot.querySelector(".diff-note").textContent).toBe("No changes.");
	});

	it("has a header button that is pressed while whitespace is hidden", async () => {
		document.body.innerHTML = `<knowwe-file-change data-new-name="A">${inline("")}</knowwe-file-change>`;
		const change = document.querySelector("knowwe-file-change");
		const diff = document.querySelector("knowwe-text-diff");
		await settled();
		const button = change.shadowRoot.querySelector(".file-change-whitespace");
		expect(button.hidden).toBe(false);
		expect(button.getAttribute("aria-pressed")).toBe("false");
		button.click();
		expect(diff.ignoreWhitespace).toBe(true);
		expect(button.getAttribute("aria-pressed")).toBe("true");
		expect(button.title).toBe("Show whitespace changes");
		expect(change.getAttribute("data-additions")).toBe("0");
		expect(change.hasAttribute("data-collapsed")).toBe(false);
	});
});

describe("emphasis", () => {
	const inline = (oldText, newText, attributes = "") => `<knowwe-text-diff data-renderer="codemirror" data-context-lines="-1" ${attributes}><template shadowrootmode="open">
		<div class="diff-frame"></div><script type="application/json" class="diff-data">${JSON.stringify({old: oldText, new: newText, stats: {added: 0, removed: 0}})}</script>
		</template></knowwe-text-diff>`;

	it("marks the changed words of a line, in the new line and in the deleted row", async () => {
		document.body.innerHTML = inline("keep\nalpha beta gamma\n", "keep\nalpha BETA gamma\n");
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		await new Promise(r => setTimeout(r, 20));
		const root = el.shadowRoot;
		expect([...root.querySelectorAll(".cm-line .cm-changedText")].map(e => e.textContent)).toEqual(["BETA"]);
		expect([...root.querySelectorAll(".cm-deletedLine .cm-deletedText")].map(e => e.textContent)).toEqual(["beta"]);
	});

	it("leaves whole line insertions and rewrites without emphasis", async () => {
		document.body.innerHTML = inline("keep\none two three\n", "keep\nnew line\nfour five six\n");
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		await new Promise(r => setTimeout(r, 20));
		expect(el.shadowRoot.querySelectorAll(".cm-changedText, .cm-deletedText")).toHaveLength(0);
	});

	it("indents the continuation rows of wrapped lines like their first row, also in deleted rows", async () => {
		document.body.innerHTML = inline("\t\tgone\n", "    kept\n        deeper\nflat\n");
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		await new Promise(r => setTimeout(r, 20));
		const root = el.shadowRoot;
		const styles = [...root.querySelectorAll(".cm-line")].map(l => l.getAttribute("style") || "");
		expect(styles[0]).toContain("text-indent: -4ch");
		expect(styles[1]).toMatch(/padding-left: calc\((6px \+ 8ch|8ch \+ 6px)\)/);
		expect(styles[2]).toBe("");
		expect(root.querySelector(".cm-deletedLine").getAttribute("style")).toContain("text-indent: -8ch");
	});

	it("marks both sides of the split view", async () => {
		document.body.innerHTML = inline("alpha beta gamma\n", "alpha BETA gamma\n", 'data-layout="split"');
		const el = document.querySelector("knowwe-text-diff");
		await settled();
		const root = el.shadowRoot;
		expect([...root.querySelectorAll(".cm-merge-a .cm-changedText")].map(e => e.textContent)).toEqual(["beta"]);
		expect([...root.querySelectorAll(".cm-merge-b .cm-changedText")].map(e => e.textContent)).toEqual(["BETA"]);
	});

});
