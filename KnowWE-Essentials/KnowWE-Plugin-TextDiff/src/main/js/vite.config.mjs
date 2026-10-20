import {defineConfig} from "vite";

// CodeMirror and the wiki language are not bundled. They resolve to the vendored global "CodeMirror" bundle and
// the global "KnowWEWikiLanguage" in the IIFE build, while the tests import the packages directly.
const externals = {
    "@codemirror/state": "CodeMirror.state",
    "@codemirror/view": "CodeMirror.view",
    "@codemirror/language": "CodeMirror.language",
    "@codemirror/search": "CodeMirror.search",
    "@codemirror/merge": "CodeMirror.merge",
    "@lezer/highlight": "CodeMirror.lezer.highlight",
};

export default defineConfig({
    build: {
        outDir: "../resources/webapp/KnowWEExtension/scripts",
        emptyOutDir: false,
        minify: false,
        sourcemap: false,
        lib: {
            entry: "src/index.js",
            name: "KnowWETextDiff",
            formats: ["iife"],
            fileName: () => "KnowWE-Plugin-TextDiff.js",
        },
        rollupOptions: {
            external: Object.keys(externals),
            output: {globals: externals},
        },
    },
    esbuild: {legalComments: "none"},
    test: {
        include: ["test/**/*.test.js"],
    },
});
