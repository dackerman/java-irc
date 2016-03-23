package com.dacklabs.irc.core;

/**
 * A channel watcher is notified when an IRC channel recieves a message.
 *
 * Note that this may be invoked from another thread, so take care not to work with non-synchronized or non-concurrent
 * data types from other threads.
 */
public interface ChannelWatcher {

    /**
     * Invoked when an IRC channel receives a message.
     * @param message the message recieved.
     */
    void onMessage(String message);
}
