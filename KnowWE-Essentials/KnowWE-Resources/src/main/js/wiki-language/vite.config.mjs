import {defineConfig} from "vite";
import {lezer} from "@lezer/generator/rollup";

// CodeMirror and Lezer are not bundled. They resolve to the namespaces of the vendored global "CodeMirror" bundle in
// the IIFE build, while the ES build keeps the imports for the node based tests.
const externals = {
    "@codemirror/state": "CodeMirror.state",
    "@codemirror/language": "CodeMirror.language",
    "@lezer/common": "CodeMirror.lezer.common",
    "@lezer/highlight": "CodeMirror.lezer.highlight",
    "@lezer/lr": "CodeMirror.lezer.lr",
};

export default defineConfig({
    plugins: [lezer()],
    build: {
        outDir: "dist",
        emptyOutDir: true,
        minify: false,
        sourcemap: false,
        lib: {
            entry: "src/index.js",
            name: "KnowWEWikiLanguage",
            formats: ["iife", "es"],
            fileName: format => format === "iife" ? "KnowWE-WikiLanguage.js" : "wiki-language.mjs",
        },
        rollupOptions: {
            external: Object.keys(externals),
            output: {globals: externals},
        },
    },
    esbuild: {legalComments: "none"},
});
