import {copyFileSync} from "node:fs";

// The IIFE build is committed next to the other KnowWE scripts.
copyFileSync("dist/KnowWE-WikiLanguage.js", "../../webapp/KnowWEExtension/scripts/KnowWE-WikiLanguage.js");
console.log("copied KnowWE-WikiLanguage.js");
