import { copyFile, mkdir, readFile, readdir, stat } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const appDir = path.resolve(__dirname, "..");

const PROFILE_CONFIG = {
  all: {
    roots: [
      { root: "../KnowWE-Essentials" },
      { root: "../KnowWE-Plugins" },
      { root: "../../KnowWE-DES/KnowWE-Plugins-DES" },
      { root: "../../Experimental/KnowWE-Plugins-Experimental" },
      { root: "../../KnowWE-SSP" },
      { root: "../../ServiceMatePlatform" },
      { root: "../../KRONE" },
      {
        root: "../../d3web-Player-Platform",
        specialSources: [
          {
            source: "d3web-Player/src/main/resources",
            targetSubdir: "KnowWEExtension/d3web-Player/client"
          }
        ]
      }
    ]
  },
  "core-plugins": {
    roots: [
      { root: "../KnowWE-Essentials" },
      { root: "../KnowWE-Plugins" }
    ]
  },
  denkbares: {
    roots: [
      { root: "../KnowWE-Essentials" },
      { root: "../KnowWE-Plugins" },
      { root: "../../KnowWE-DES/KnowWE-Plugins-DES" },
      { root: "../../Experimental/KnowWE-Plugins-Experimental" },
      { root: "../../KnowWE-SSP" },
      { root: "../../ServiceMatePlatform" }
    ]
  },
  konap: {
    roots: [
      { root: "../KnowWE-Essentials" },
      { root: "../KnowWE-Plugins" },
      { root: "../../KRONE" }
    ]
  },
  "d3web-player": {
    roots: [
      {
        root: "../../d3web-Player-Platform",
        specialSources: [
          {
            source: "d3web-Player/src/main/resources",
            targetSubdir: "KnowWEExtension/d3web-Player/client"
          }
        ]
      }
    ]
  }
};

const WATCHED_EXTENSIONS = new Set([".js", ".mjs", ".cjs", ".css"]);
const SKIP_DIRECTORIES = new Set([
  ".git",
  ".idea",
  ".settings",
  "node_modules",
  "target",
  "dist",
  "build"
]);

async function main() {
  const [mode, profileName, ...rest] = process.argv.slice(2);

  if (!mode || !profileName || !["sync", "watch"].includes(mode)) {
    printUsageAndExit(1);
  }

  const profile = PROFILE_CONFIG[profileName];
  if (!profile) {
    console.error(`Unknown profile '${profileName}'.`);
    printUsageAndExit(1);
  }

  const options = parseOptions(rest);
  const targetDir = options.targetDir ?? await inferTargetDir();
  const sources = await discoverSources(profile);

  if (sources.length === 0) {
    console.error(`No source directories found for profile '${profileName}'.`);
    process.exit(1);
  }

  console.log(`Profile: ${profileName}`);
  console.log(`Target:  ${targetDir}`);
  console.log(`Mode:    ${mode}`);
  console.log(`Sources: ${sources.length}`);
  for (const source of sources) {
    console.log(`  - ${source.sourceDir} -> ${path.join(targetDir, source.targetSubdir)}`);
  }

  if (mode === "sync") {
    const files = await scanFiles(sources);
    const result = await syncFiles(files, targetDir);
    printResult(result);
    return;
  }

  let previousSnapshot = await scanFiles(sources);
  const initialResult = await syncFiles(previousSnapshot, targetDir);
  printResult(initialResult);
  console.log(`Watching ${previousSnapshot.size} JS/CSS files. Poll interval: ${options.intervalMs} ms.`);

  setInterval(async () => {
    try {
      const currentSnapshot = await scanFiles(sources);
      const result = await processChanges(previousSnapshot, currentSnapshot, targetDir);
      if (result.copied || result.skippedMissingTarget || result.deleted) {
        printResult(result);
      }
      previousSnapshot = currentSnapshot;
    } catch (error) {
      console.error(error instanceof Error ? error.message : error);
    }
  }, options.intervalMs);
}

function parseOptions(args) {
  const options = {
    intervalMs: 1000,
    targetDir: null
  };

  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index];
    if (arg === "--interval") {
      options.intervalMs = Number(args[index + 1] ?? "1000");
      index += 1;
      continue;
    }
    if (arg === "--target") {
      options.targetDir = path.resolve(appDir, args[index + 1] ?? "");
      index += 1;
      continue;
    }
  }

  if (!Number.isFinite(options.intervalMs) || options.intervalMs < 100) {
    throw new Error("Invalid --interval value. Use a number >= 100.");
  }

  return options;
}

async function inferTargetDir() {
  const pomPath = path.join(appDir, "pom.xml");
  const pomContent = await readFile(pomPath, "utf8");
  const artifactIdMatch = pomContent.match(/<artifactId>(KnowWE-App)<\/artifactId>/);
  const versionMatch = pomContent.match(/<parent>[\s\S]*?<version>([^<]+)<\/version>[\s\S]*?<\/parent>/);

  if (!artifactIdMatch || !versionMatch) {
    throw new Error(`Could not infer target directory from ${pomPath}.`);
  }

  return path.join(appDir, "target", `${artifactIdMatch[1]}-${versionMatch[1].trim()}`);
}

async function discoverSources(profile) {
  const sources = [];

  for (const rootConfig of profile.roots) {
    const rootDir = path.resolve(appDir, rootConfig.root);
    if (!(await exists(rootDir))) {
      continue;
    }

    if (rootConfig.specialSources) {
      for (const specialSource of rootConfig.specialSources) {
        const sourceDir = path.join(rootDir, specialSource.source);
        if (await exists(sourceDir)) {
          sources.push({
            sourceDir,
            targetSubdir: specialSource.targetSubdir
          });
        }
      }
      continue;
    }

    const discoveredDirs = await findWebappDirs(rootDir);
    for (const sourceDir of discoveredDirs) {
      sources.push({
        sourceDir,
        targetSubdir: "."
      });
    }
  }

  sources.sort((left, right) => left.sourceDir.localeCompare(right.sourceDir));
  return sources;
}

async function findWebappDirs(rootDir) {
  const discovered = [];
  const queue = [rootDir];

  while (queue.length > 0) {
    const currentDir = queue.pop();
    let entries;

    try {
      entries = await readdir(currentDir, { withFileTypes: true });
    } catch {
      continue;
    }

    for (const entry of entries) {
      if (!entry.isDirectory()) {
        continue;
      }

      if (SKIP_DIRECTORIES.has(entry.name)) {
        continue;
      }

      const entryPath = path.join(currentDir, entry.name);
      const normalizedPath = entryPath.split(path.sep).join("/");
      if (
        normalizedPath.endsWith("/src/main/resources/webapp") ||
        normalizedPath.endsWith("/src/main/webapp")
      ) {
        discovered.push(entryPath);
        continue;
      }

      queue.push(entryPath);
    }
  }

  return discovered;
}

async function scanFiles(sources) {
  const snapshot = new Map();

  for (const source of sources) {
    await walkFiles(source.sourceDir, async (filePath, fileStat) => {
      if (!WATCHED_EXTENSIONS.has(path.extname(filePath))) {
        return;
      }

      const relativePath = path.relative(source.sourceDir, filePath);
      snapshot.set(filePath, {
        filePath,
        relativePath,
        mtimeMs: fileStat.mtimeMs,
        size: fileStat.size,
        source
      });
    });
  }

  return snapshot;
}

async function walkFiles(rootDir, onFile) {
  const queue = [rootDir];

  while (queue.length > 0) {
    const currentDir = queue.pop();
    let entries;

    try {
      entries = await readdir(currentDir, { withFileTypes: true });
    } catch {
      continue;
    }

    for (const entry of entries) {
      const entryPath = path.join(currentDir, entry.name);

      if (entry.isDirectory()) {
        if (!SKIP_DIRECTORIES.has(entry.name)) {
          queue.push(entryPath);
        }
        continue;
      }

      if (!entry.isFile()) {
        continue;
      }

      let fileStat;
      try {
        fileStat = await stat(entryPath);
      } catch {
        continue;
      }

      await onFile(entryPath, fileStat);
    }
  }
}

async function processChanges(previousSnapshot, currentSnapshot, targetDir) {
  const result = {
    copied: 0,
    skippedMissingTarget: 0,
    deleted: 0
  };

  for (const [filePath, fileInfo] of currentSnapshot) {
    const previous = previousSnapshot.get(filePath);
    if (previous && previous.mtimeMs === fileInfo.mtimeMs && previous.size === fileInfo.size) {
      continue;
    }

    const targetPath = path.join(targetDir, fileInfo.source.targetSubdir, fileInfo.relativePath);
    if (!(await exists(targetPath))) {
      result.skippedMissingTarget += 1;
      continue;
    }

    await mkdir(path.dirname(targetPath), { recursive: true });
    await copyFile(filePath, targetPath);
    console.log(`[copy] ${filePath} -> ${targetPath}`);
    result.copied += 1;
  }

  for (const filePath of previousSnapshot.keys()) {
    if (!currentSnapshot.has(filePath)) {
      console.log(`[gone] ${filePath}`);
      result.deleted += 1;
    }
  }

  return result;
}

async function syncFiles(snapshot, targetDir) {
  const emptySnapshot = new Map();
  return processChanges(emptySnapshot, snapshot, targetDir);
}

function printResult(result) {
  console.log(
    `Summary: copied=${result.copied}, skipped-missing-target=${result.skippedMissingTarget}, deleted=${result.deleted}`
  );
}

async function exists(targetPath) {
  try {
    await stat(targetPath);
    return true;
  } catch {
    return false;
  }
}

function printUsageAndExit(exitCode) {
  console.error("Usage: node watch-resources.mjs <sync|watch> <profile> [--interval <ms>] [--target <dir>]");
  console.error(`Profiles: ${Object.keys(PROFILE_CONFIG).join(", ")}`);
  process.exit(exitCode);
}

await main();
