// @vitest-environment jsdom
import {afterEach, beforeAll, beforeEach, describe, expect, it, vi} from "vitest";

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
		expect(events).toEqual([{layout: "unified", effective: "unified"}]);
		el.layout = "split";
		expect(events).toHaveLength(2);
		expect(events[1]).toEqual({layout: "split", effective: "split"});
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
