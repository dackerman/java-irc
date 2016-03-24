package com.dacklabs.irc.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reads from an inputstream and dispatches each line to registered {@link ChannelWatcher} instances.
 *
 * After calling {@link InputStreamBroadcaster#start()}, this class will run indefinitely until the stream is closed
 * or {@link InputStreamBroadcaster#stop()} is called from another thread.
 */
public final class InputStreamBroadcaster {

    private final Scanner in;
    private final ConcurrentHashMap<WatcherID, ChannelWatcher> watchers = new ConcurrentHashMap<>();

    private final AtomicBoolean shouldStopThread = new AtomicBoolean(false);

    public InputStreamBroadcaster(InputStream stream) {
        this.in = new Scanner(stream);
    }

    /**
     * Starts listening on the given input stream indefinitely until it is closed or
     * {@link InputStreamBroadcaster#stop()} is invoked.
     */
    public void start() {
        while (!shouldStopThread.get() && in.hasNext()) {
            String message = in.nextLine();
            for (ChannelWatcher watcher : watchers.values()) {
                watcher.onMessage(message);
            }
        }
        in.close();
    }

    /**
     * Stops the broadcaster from listening on the stream, even if there is more to read.
     */
    public void stop() {
        shouldStopThread.set(true);
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
