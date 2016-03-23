package com.dacklabs.irc.watchers;

import com.dacklabs.irc.core.ChannelWatcher;

/**
 * Prints all channel messages to standard out.
 */
public final class StandardOutMessagePrinter implements ChannelWatcher {
    @Override
    public void onMessage(String message) {
        System.out.println("<<< " + message);
    }
}
