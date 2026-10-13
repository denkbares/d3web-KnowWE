// Tests the markup block context and the completion dispatch with dummy markups. Run through "pnpm test".
import {EditorState} from "@codemirror/state";
import {ensureSyntaxTree} from "@codemirror/language";
import {CompletionContext} from "@codemirror/autocomplete";
import {buildParser} from "@lezer/generator";
import {
    articleTitle, completeMarkup, completionSources, defineMarkups, markupBlock, registerCompletion, registerMarkup,
    unregisterCompletion, unregisterMarkup, wiki,
} from "../dist/wiki-language.mjs";

let failures = 0, passed = 0;

async function test(name, body) {
    try {
        await body();
        passed++;
    } catch (error) {
        failures++;
        console.log("FAIL " + name + "\n  " + String(error.stack ?? error).replace(/\n/g, "\n  "));
    }
}

function equal(actual, expected, what = "value") {
    const a = JSON.stringify(actual), e = JSON.stringify(expected);
    if (a !== e) throw new Error(`${what}: expected ${e} but got ${a}`);
}

// a stand-in for an embedded language that parses words
const words = buildParser("@top Words { Word* } @skip { space } "
    + "@tokens { Word { ![ \\t\\n]+ } space { $[ \\t\\n]+ } }");

defineMarkups(["Dummy", "Plain", {name: "Version", inline: true}]);

// the caret is marked by "|" in the text
function editor(marked) {
    const pos = marked.indexOf("|");
    const doc = marked.slice(0, pos) + marked.slice(pos + 1);
    const state = EditorState.create({doc, extensions: [wiki(), articleTitle.of("Main")]});
    ensureSyntaxTree(state, doc.length, 5000);
    return {state, pos};
}

// aborts the query like the autocompletion plugin does
function abort(ctx) {
    for (const listener of ctx.abortListeners) listener();
    ctx.abortListeners = null;
}

function context(marked, explicit = false) {
    const {state, pos} = editor(marked);
    return new CompletionContext(state, pos, explicit);
}

const PAGE = "! Heading\ntext\n%%Dummy\nalpha beta\n@package: p1\n@name n\n/%\nmore text\n%%Plain: x y\n"
    + "%%Plain\nfirst\n  @package  : p2\n  next line\n%\nVersion %%Version 1.2 /% done";

registerMarkup("Dummy", {parser: words});

await test("block of a markup with parser", () => {
    const at = PAGE.indexOf("beta");
    const {state} = editor(PAGE.slice(0, at) + "|" + PAGE.slice(at));
    const block = markupBlock(state, at);
    equal(block.name, "Dummy", "name");
    equal(block.article, "Main", "article");
    equal(state.sliceDoc(block.from, block.to), block.text, "text range");
    equal(block.text.startsWith("\nalpha beta\n@package: p1\n"), true, "text start");
    equal(block.tree.type.name, "Words", "tree");
    equal(block.tree.length, block.text.length, "tree length");
    const word = block.tree.topNode.firstChild;
    equal(block.text.slice(word.from, word.to), "alpha", "first word");
    equal(block.annotations.map(a => [a.name, a.value, block.text.slice(a.valueFrom, a.valueTo)]),
        [["package", "p1", "p1"], ["name", "n", "n"]], "annotations");
    const first = block.annotations[0];
    equal(block.text.slice(first.from, first.to), "@package: p1", "annotation range");
});

await test("block of a markup without parser", () => {
    const at = PAGE.indexOf("first");
    const {state} = editor(PAGE.slice(0, at) + "|" + PAGE.slice(at));
    const block = markupBlock(state, at);
    equal(block.name, "Plain", "name");
    equal(block.tree, null, "tree");
    equal(block.annotations.map(a => [a.name, a.value]), [["package", "p2\n  next line"]], "annotations");
});

await test("block of a single line and an inline markup", () => {
    const {state} = editor(PAGE + "|");
    const line = markupBlock(state, PAGE.indexOf("x y") + 1);
    equal([line.name, line.text.trim()], ["Plain", "x y"], "single line");
    const inline = markupBlock(state, PAGE.indexOf("1.2"));
    equal([inline.name, inline.text.trim()], ["Version", "1.2"], "inline");
});

await test("no block outside of bodies", () => {
    const {state} = editor(PAGE + "|");
    for (const text of ["Heading", "more text", "%%Dumm", "/%\nmore", " done"]) {
        equal(markupBlock(state, PAGE.indexOf(text) + 2), null, JSON.stringify(text));
    }
});

await test("block at the end of an unterminated markup", () => {
    const {state, pos} = editor("text\n%%Dummy\nalpha|");
    const block = markupBlock(state, pos);
    equal([block.name, block.text, pos - block.from], ["Dummy", "\nalpha", 6], "block");
});

await test("completion positions are relative to the body", async () => {
    const seen = [];
    registerMarkup("Dummy", {
        parser: words, complete(block, ctx) {
            const word = ctx.matchBefore(/\w*/);
            seen.push([ctx.pos, block.text.slice(0, ctx.pos), word]);
            return {from: word.from, options: [{label: "gamma"}], validFor: (text, from, to) => from === word.from};
        },
    });
    const ctx = context("x\n%%Dummy\nalpha ga|\n/%");
    const result = await completeMarkup(ctx);
    equal(seen, [[9, "\nalpha ga", {from: 7, to: 9, text: "ga"}]], "context");
    const gaFrom = ctx.state.doc.toString().indexOf("ga\n");
    equal([result.from, result.options[0].label], [gaFrom, "gamma"], "result");
    equal([result.validFor("gam", gaFrom, gaFrom + 3), result.validFor("am", gaFrom + 1, gaFrom + 3)],
        [true, false], "validFor");
});

await test("matchBefore stops at the body start", () => {
    let match, text;
    registerMarkup("Dummy", {
        parser: words, complete(block, ctx) {
            match = ctx.matchBefore(/.*/);
            text = block.text;
            return null;
        },
    });
    completeMarkup(context("%%Dummy: ab|"));
    equal(match, {from: 0, to: text.length, text}, "match");
    equal(text.trim(), "ab", "body");
});

await test("completion of a markup without parser", async () => {
    registerMarkup("Plain", {complete: (block, ctx) => ({from: ctx.pos, options: [{label: block.name}]})});
    const result = await completeMarkup(context("%%Plain\nx|\n/%"));
    equal([result.from, result.options[0].label], [9, "Plain"], "result");
    unregisterMarkup("Plain");
    equal(completeMarkup(context("%%Plain\nx|\n/%")), null, "unregistered");
});

await test("no completion outside of bodies and for markups without complete", () => {
    let calls = 0;
    registerMarkup("Dummy", {parser: words, complete: () => (calls++, null)});
    equal(completeMarkup(context("te|xt\n%%Dummy\nx\n/%")), null, "text");
    equal(completeMarkup(context("%%Dum|my\nx\n/%")), null, "declaration");
    registerMarkup("Plain", {});
    equal(completeMarkup(context("%%Plain\nx|\n/%")), null, "no complete");
    unregisterMarkup("Plain");
    equal(calls, 0, "calls");
});

await test("async completion and abort", async () => {
    registerMarkup("Dummy", {
        parser: words,
        complete: async (block, ctx) => ({from: ctx.pos - 1, options: [{label: "later"}]}),
    });
    const ctx = context("%%Dummy\nab|\n/%", true);
    const pending = completeMarkup(ctx);
    equal(typeof pending.then, "function", "promise");
    equal((await pending).from, 9, "resolved");

    const aborted = context("%%Dummy\nab|\n/%", true);
    const result = completeMarkup(aborted);
    abort(aborted);
    equal(await result, null, "aborted");
});

await test("context passes explicit and aborted through", () => {
    let seen;
    registerMarkup("Dummy", {parser: words, complete: (block, ctx) => (seen = ctx, null)});
    const ctx = context("%%Dummy\nab|\n/%", true);
    completeMarkup(ctx);
    let fired = false;
    seen.addEventListener("abort", () => fired = true);
    equal([seen.explicit, seen.aborted], [true, false], "before");
    abort(ctx);
    equal([seen.aborted, fired], [true, true], "after");
});

await test("page completions apply where no markup completes", () => {
    const calls = [];
    const page = ctx => (calls.push(ctx.pos), {from: ctx.pos, options: [{label: ctx.state.facet(articleTitle)}]});
    registerCompletion(page);
    registerCompletion(page);
    registerMarkup("Dummy", {parser: words, complete: () => null});
    registerMarkup("Plain", {});
    const sources = completionSources();
    equal(sources.length, 2, "sources");
    const run = marked => sources[1](context(marked));
    equal(run("te|xt").options[0].label, "Main", "wiki text");
    equal(run("%%Dum|my\nx\n/%") !== null, true, "declaration");
    equal(run("%%Plain\nx|\n/%") !== null, true, "markup without complete");
    equal(run("%%Dummy\nx|\n/%"), null, "markup with complete");
    equal(calls, [2, 5, 9], "calls");
    unregisterCompletion(page);
    unregisterMarkup("Plain");
    equal(completionSources().length, 1, "unregistered");
});

unregisterMarkup("Dummy");
console.log(failures ? `${failures} of ${failures + passed} completion tests failed`
    : `all ${passed} completion tests passed`);
process.exit(failures ? 1 : 0);
