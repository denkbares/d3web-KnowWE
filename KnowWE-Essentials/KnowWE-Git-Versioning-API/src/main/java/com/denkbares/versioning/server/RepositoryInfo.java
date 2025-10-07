/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

public record RepositoryInfo(int id, String name, String relativePath, String repoUrl, String webUrl) {
}
