// Parses all wiki pages below the given files or directories with the markups in knowwe-markups.json. Every page
// must parse without error nodes and with properly nested nodes, and random edits must reparse incrementally to the
// same tree as a fresh parse. Run through "node test/corpus.mjs <path>..." after "pnpm build". A path starting with
// "@" names a file that lists one path per line.
import {readdirSync, readFileSync, statSync} from "node:fs";
import {extname, join} from "node:path";
import {defineMarkups, parser} from "../dist/wiki-language.mjs";
import {checkCoverage, checkIncremental, checkNesting} from "./checks.mjs";

const EDITS = 5;
const INSERTS = ["\n", " ", "_", "__", "'", "''", "[", "]", "{", "}", "{{{", "}}}", "|", "%%", "/%", "%%Rule\n",
    "\n/%\n", "*", "!", ";", ":", "~", "\\\\", "@", "[{", "}]", "x"];

function collect(path, files) {
    if (path.startsWith("@")) {
        for (const line of readFileSync(path.slice(1), "utf8").split("\n")) if (line) collect(line, files);
    } else if (statSync(path).isDirectory()) {
        for (const entry of readdirSync(path).sort()) {
            if (extname(entry) === ".txt") files.push(join(path, entry));
        }
    } else {
        files.push(path);
    }
    return files;
}

// a small deterministic generator, so failures can be reproduced
function random(seed) {
    let state = seed >>> 0 || 1;
    return limit => {
        state ^= state << 13;
        state ^= state >>> 17;
        state ^= state << 5;
        return (state >>> 0) % limit;
    };
}

const paths = process.argv.slice(2);
if (paths.length === 0) {
    console.log("usage: node test/corpus.mjs <file, directory or @list>...");
    process.exit(2);
}

defineMarkups(JSON.parse(readFileSync(new URL("knowwe-markups.json", import.meta.url), "utf8")));
const strict = parser.configure({strict: true});
let failed = 0, total = 0, bytes = 0, millis = 0, slowest = {millis: 0};
for (const file of paths.flatMap(path => collect(path, []))) {
    const text = readFileSync(file, "utf8");
    total++;
    bytes += text.length;
    try {
        const start = performance.now();
        const tree = strict.parse(text);
        const elapsed = performance.now() - start;
        millis += elapsed;
        if (elapsed > slowest.millis) slowest = {millis: elapsed, file};
        checkNesting(tree);
        checkCoverage(tree, text);
        const next = random(text.length);
        let state = {text, tree};
        for (let i = 0; i < EDITS && text.length > 0; i++) {
            const from = next(state.text.length + 1);
            const to = Math.min(state.text.length, from + (next(2) ? 0 : next(8)));
            state = checkIncremental(parser, state.text, state.tree, from, to, INSERTS[next(INSERTS.length)]);
        }
    } catch (error) {
        failed++;
        console.log("FAIL " + file + ": " + (error.message ?? error));
    }
}
console.log(`${total - failed} of ${total} pages passed, ${(bytes / 1e6).toFixed(1)} MB in ${millis.toFixed(0)} ms, `
    + `slowest ${slowest.millis.toFixed(0)} ms ${slowest.file ?? ""}`);
process.exit(failed ? 1 : 0);
