// @vitest-environment jsdom
import {afterEach, beforeAll, describe, expect, it, vi} from "vitest";

vi.mock("../src/renderer/codemirror.js", () => ({
	createDiffView: () => {
		throw new Error("no view");
	}
}));

const settled = () => new Promise(resolve => setTimeout(resolve, 0));

beforeAll(async () => {
	window.IntersectionObserver = class {
		constructor(callback) {
			this.callback = callback;
		}

		observe(target) {
			this.callback([{target, isIntersecting: true}]);
		}

		disconnect() {
		}
	};
	window.CodeMirror = {merge: {}};
	await import("../src/index.js");
});

afterEach(() => {
	document.body.replaceChildren();
});

describe("knowwe-text-diff without a working CodeMirror view", () => {
	it("shows the table built in the browser from the same texts", async () => {
		vi.spyOn(console, "warn").mockImplementation(() => {});
		document.body.innerHTML = `<knowwe-file-change data-additions="7" data-deletions="9">
			<knowwe-text-diff data-renderer="codemirror" data-context-lines="1">
			<script type="text/plain" slot="old">a\nb\nc\nd\ne\nf\ng</script>
			<script type="text/plain" slot="new">a\nB\nc\nd\ne\nf\ng</script></knowwe-text-diff></knowwe-file-change>`;
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(el.getAttribute("data-status")).toBe("ready");
		expect(el.shadowRoot.querySelector(".diff-frame").classList.contains("cm-diff")).toBe(false);
		expect(el.shadowRoot.querySelectorAll("tr.line.added")).toHaveLength(1);
		expect(el.shadowRoot.querySelectorAll("tr.line.removed")).toHaveLength(1);
		expect(el.shadowRoot.querySelector("tr.elided")).not.toBeNull();
		el.shadowRoot.querySelector("button.expand").click();
		expect(el.shadowRoot.querySelectorAll("tr.hidden")).toHaveLength(0);
		const change = document.querySelector("knowwe-file-change");
		expect(change.getAttribute("data-additions")).toBe("1");
		expect(change.getAttribute("data-deletions")).toBe("1");
	});
});
