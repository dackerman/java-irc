package com.dacklabs.irc.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a connection to an IRC server.
 */
public interface IrcConnection extends Closeable {

    /**
     * Writes the given command and message to the server.
     *
     * @param command The IRC command that should be sent
     * @param message The message to write
     */
    void write(String command, String message);

    /**
     * Registers a watcher on this connection. When added, the watcher will be called
     * for each line that comes in from the server.
     *
     * @param watcher The watcher to register
     * @return the ID of the watcher, so it can be removed
     */
    WatcherID registerWatcher(ChannelWatcher watcher);

    /**
     * Removes the watcher for the given ID, returned during registration.
     *
     * @param watcherID The ID of the watcher to remove
     * @return whether the watcher was removed
     */
    boolean unregisterWatcher(WatcherID watcherID);
}
