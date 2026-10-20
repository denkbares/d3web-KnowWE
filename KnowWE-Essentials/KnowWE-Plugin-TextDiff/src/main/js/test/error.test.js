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
	await import("../src/index.js");
});

afterEach(() => {
	document.body.replaceChildren();
});

describe("knowwe-text-diff whose view cannot be built", () => {
	it("shows an error note and keeps the counts it computed", async () => {
		vi.spyOn(console, "warn").mockImplementation(() => {});
		document.body.innerHTML = `<knowwe-file-change data-additions="7" data-deletions="9">
			<knowwe-text-diff data-old-text="a\nb" data-new-text="a\nB"></knowwe-text-diff></knowwe-file-change>`;
		await settled();
		await settled();
		const el = document.querySelector("knowwe-text-diff");
		expect(el.getAttribute("data-status")).toBe("error");
		expect(el.getAttribute("data-status-message")).toBe("no view");
		expect(el.shadowRoot.querySelector(".diff-frame")).toBeNull();
		expect(el.stats).toEqual({added: 1, removed: 1});
	});
});
