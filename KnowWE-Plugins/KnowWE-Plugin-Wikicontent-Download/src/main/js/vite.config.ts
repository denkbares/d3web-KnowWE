import {defineConfig} from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({mode}) => {
    let outputDir: string;
    if (mode === "production") {
        outputDir = "../resources/webapp/KnowWEExtension";
    } else {
        outputDir = "../../../../../../KnowWE/KnowWE-App/target/KnowWE-App-2025.4-SNAPSHOT/KnowWEExtension";
    }

    return {
        plugins: [react()],
        build: {
            minify: mode === "production",
            sourcemap: mode !== "production",
            rollupOptions: {
                input: {
                    "KnowWE-Plugin-WikiZIPDownload": "./src/index.ts",
                },
                external: ["react", "react-dom"],
                output: {
                    dir: outputDir,
                    entryFileNames: "scripts/[name].js",
                    // We can't use ES modules (default), but need IIFE (Immediately Invoked Function Expression), since
                    // React is statically available within KnowWE, but not as ESM, only IIFE.
                    format: "iife",
                    globals: {
                        react: "React",
                        "react-dom": "ReactDOM",
                    },
                },
            },
        },
        esbuild: {legalComments: "none"},
    };
});
