// @vitest-environment jsdom
import {afterEach, beforeAll, describe, expect, it, vi} from "vitest";

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
});

function response(body, ok = true) {
	return {ok, status: ok ? 200 : 500, json: async () => JSON.parse(body)};
}

describe("knowwe-text-diff", () => {
	it("loads the texts from the endpoint and builds the view", async () => {
		const fetch = vi.fn(async () => response(JSON.stringify({old: "a\nb\n", new: "a\nB\n"})));
		window.knowweTextDiffFetch = fetch;
		const stats = vi.fn();
		document.body.innerHTML = `<knowwe-file-change data-additions="7" data-deletions="9">
			<knowwe-text-diff data-action-url="action/X?file=f"></knowwe-text-diff></knowwe-file-change>`;
		document.body.addEventListener("knowwe-text-diff-stats", stats);
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(fetch).toHaveBeenCalledOnce();
		expect(fetch.mock.calls[0][0]).toContain("action/X?file=f");
		expect(el.getAttribute("data-status")).toBe("ready");
		expect(el.shadowRoot.querySelector(".diff-frame.cm-diff .cm-editor")).not.toBeNull();
		expect(el.stats).toEqual({added: 1, removed: 1});
		expect(stats).toHaveBeenCalledOnce();
		const change = document.querySelector("knowwe-file-change");
		expect(change.getAttribute("data-additions")).toBe("1");
		expect(change.getAttribute("data-deletions")).toBe("1");
	});

	it("uses the inline data of a server rendered host without a request", async () => {
		const fetch = vi.fn();
		window.knowweTextDiffFetch = fetch;
		document.body.innerHTML = `<knowwe-text-diff data-action-url="action/X"><template shadowrootmode="open">
			<div class="diff-frame diff-placeholder" style="--diff-placeholder-lines: 3"></div>
			<script type="application/json" class="diff-data">{"old":"a\\n","new":"b\\n"}</script>
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

	it("renders a host that was upgraded before its content and moved since", async () => {
		// the parser connects the element before its children exist, while the document still loads
		Object.defineProperty(document, "readyState", {value: "loading", configurable: true});
		const el = document.createElement("knowwe-text-diff");
		document.body.append(el);
		el.innerHTML = `<template shadowrootmode="open"><div class="diff-frame diff-placeholder"></div>
			<script type="application/json" class="diff-data">{"old":"a\\n","new":"b\\n"}</script></template>`;
		const panel = document.body.appendChild(document.createElement("div"));
		panel.append(el);
		Object.defineProperty(document, "readyState", {value: "complete", configurable: true});
		document.dispatchEvent(new Event("DOMContentLoaded"));
		await settled();
		await settled();
		expect(el.getAttribute("data-status")).toBe("ready");
		expect(el.shadowRoot.querySelector(".cm-editor")).not.toBeNull();
		expect(el.stats).toEqual({added: 1, removed: 1});
	});

	it("diffs the texts of its attributes", async () => {
		const fetch = vi.fn();
		window.knowweTextDiffFetch = fetch;
		document.body.innerHTML = `<knowwe-text-diff data-new-null data-old-text="x\ny"></knowwe-text-diff>`;
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(fetch).not.toHaveBeenCalled();
		expect(el.stats).toEqual({added: 0, removed: 2});
		expect(el.shadowRoot.querySelector(".cm-editor")).not.toBeNull();
	});

	it("reports a failed request", async () => {
		window.knowweTextDiffFetch = async () => response("", false);
		document.body.innerHTML = `<knowwe-text-diff data-action-url="action/X"></knowwe-text-diff>`;
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(el.getAttribute("data-status")).toBe("error");
		expect(el.getAttribute("data-status-message")).toBe("HTTP 500");
	});
});

describe("knowwe-file-change", () => {
	function change() {
		document.body.innerHTML = `<knowwe-file-change data-new-name="A">
			<a slot="actions" href="#history">history</a><button slot="actions">action</button>
			<div>body</div></knowwe-file-change>`;
		return document.querySelector("knowwe-file-change");
	}

	it("toggles the body from the chevron and from empty header space", () => {
		const el = change();
		el.shadowRoot.querySelector(".file-change-toggle .icon").dispatchEvent(new MouseEvent("click", {bubbles: true, composed: true}));
		expect(el.hasAttribute("data-collapsed")).toBe(true);
		el.shadowRoot.querySelector(".file-change-header").click();
		expect(el.hasAttribute("data-collapsed")).toBe(false);
	});

	it("leaves the body alone for clicks on header links and slotted controls", () => {
		const el = change();
		el.shadowRoot.querySelector(".file-change-name.old").click();
		el.querySelector("a[slot]").click();
		el.querySelector("button[slot]").click();
		expect(el.hasAttribute("data-collapsed")).toBe(false);
	});
});
