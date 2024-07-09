package de.uniwue.d3web.gitConnector.impl;

import java.util.List;

public record HistoryTuple(long timestamp, List<String> commitHashes) {
}
