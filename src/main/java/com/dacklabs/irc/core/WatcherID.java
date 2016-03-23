package com.dacklabs.irc.core;

/**
 * WatcherID represents a unique identifier for a watcher on an IRC channel.
 *
 * This is an opaque object, so the only thing "equal" to it is itself.
 */
public final class WatcherID {

    public static WatcherID create() {
        return new WatcherID();
    }

    private WatcherID() {}
}
