import {defineConfig} from "vite";

// Builds a single IIFE file next to the other vendored scripts. The output is committed, see README.md.
export default defineConfig({
    build: {
        outDir: "../../webapp/KnowWEExtension/scripts",
        emptyOutDir: false,
        minify: true,
        sourcemap: false,
        lib: {
            entry: "src/index.js",
            name: "CodeMirror",
            formats: ["iife"],
            fileName: () => "codemirror.min.js",
        },
    },
    esbuild: {legalComments: "none"},
});
