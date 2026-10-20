// The fixtures shared with the Java tests, each with both texts, the JSON stats and the Java line chunks.
import {readdirSync, readFileSync} from "node:fs";
import {fileURLToPath} from "node:url";
import {dirname, join} from "node:path";

const dir = join(dirname(fileURLToPath(import.meta.url)), "../../../test/resources/com/denkbares/knowwe/textdiff/fixtures");

export const fixtures = readdirSync(dir).filter(f => f.endsWith(".json")).sort().map(f => ({
	name: f.replace(/\.json$/, ""),
	...JSON.parse(readFileSync(join(dir, f), "utf8")),
}));

/** The fixture texts as the server sends them, with normalized line endings. */
export function normalized(text) {
	return text == null ? null : text.replace(/\r\n?/g, "\n");
}
