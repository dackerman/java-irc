package com.dacklabs.irc.core;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Iterates over the lines of the input, and dispatches them to the {@link ChannelWatcher} instances.
 */
public final class LineWatcherDispatcher {

    private final Iterator<String> lines;
    private final ConcurrentHashMap<WatcherID, ChannelWatcher> watchers = new ConcurrentHashMap<>();

    public LineWatcherDispatcher(Iterator<String> lines) {
        this.lines = lines;
    }

    /**
     * Iterates over the input until there are no more lines.
     */
    public void start() {
        while (lines.hasNext()) {
            String message = lines.next();
            for (ChannelWatcher watcher : watchers.values()) {
                watcher.onMessage(message);
            }
        }
    }

    public WatcherID registerWatcher(ChannelWatcher watcher) {
        WatcherID watcherID = WatcherID.create();
        watchers.put(watcherID, watcher);
        return watcherID;
    }

    public boolean unregisterWatcher(WatcherID watcherID) {
        return watchers.remove(watcherID) != null;
    }
}
