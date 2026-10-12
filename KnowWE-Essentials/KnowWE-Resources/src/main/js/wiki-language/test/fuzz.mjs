// Parses random documents made of wiki markup fragments. Every document must parse without error nodes and with
// properly nested nodes, and random edits must reparse incrementally to the same tree as a fresh parse. Run through
// "node test/fuzz.mjs [count] [seed] [size]" after "pnpm build", "pnpm test" runs a short round. Lezer reuses only
// larger nodes, so a size of some thousand fragments exercises the incremental parse much better.
import {defineMarkups, parser} from "../dist/wiki-language.mjs";
import {checkCoverage, checkIncremental, checkNesting} from "./checks.mjs";
import {MARKUPS} from "./markups.mjs";

const FRAGMENTS = [
    "\n", "\n", "\n", "\r\n", "\n\n", " ", "  ", "\t", "text", "a b", "__", "''", "{{", "}}", "{{{", "}}}", "~}}}", "[",
    "]", "[[", "|", "||", "[a|b]", "[a|b|c='d']", "[#1]", "[1]", "[{", "}]", "[{Foo a='1'}]", "[{$x}]",
    "[{ALLOW edit A}]",
    "[{SET a=b}]", "%%", "/%", "%", "%%foo ", "%%foo\n", "%%(a:b)", "%%Rule\n", "%%Rule x", "%%tags: a", "\n/%\n",
    "%%Version v /%", "!", "!!!", "*", "#", "** ", ";", ":", "----", "~", "~ ", "~_", "\\\\", "\\\\\\", "&amp;", "&",
    "@package: p", "\n@name n\n", "// c", "'", "_", "{", "}", "(", ")", "=", "\"",
];

function random(seed) {
    let state = seed >>> 0 || 1;
    return limit => {
        state ^= state << 13;
        state ^= state >>> 17;
        state ^= state << 5;
        return (state >>> 0) % limit;
    };
}

const count = Number(process.argv[2] ?? 2000);
const next = random(Number(process.argv[3] ?? 4711));
const size = Number(process.argv[4] ?? 60), edits = size > 100 ? 12 : 4;
const strict = parser.configure({strict: true});
let failed = 0;
for (let n = 0; n < count && failed < 10; n++) {
    defineMarkups(n % 5 === 0 ? null : MARKUPS);
    let text = "";
    for (let i = next(size); i >= 0; i--) text += FRAGMENTS[next(FRAGMENTS.length)];
    const applied = [];
    try {
        const tree = strict.parse(text);
        checkNesting(tree);
        checkCoverage(tree, text);
        let state = {text, tree};
        for (let i = 0; i < edits; i++) {
            const from = next(state.text.length + 1);
            const to = Math.min(state.text.length, from + next(4));
            const insert = FRAGMENTS[next(FRAGMENTS.length)];
            applied.push([from, to, insert]);
            state = checkIncremental(parser, state.text, state.tree, from, to, insert);
            checkNesting(state.tree);
        }
    } catch (error) {
        failed++;
        console.log(`FAIL ${JSON.stringify(text)} edits ${JSON.stringify(applied)}\n  ${error.message ?? error}`);
    }
}
console.log(failed ? `${failed} of the random documents failed` : `all ${count} random documents passed`);
process.exit(failed ? 1 : 0);
