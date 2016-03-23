package com.dacklabs.irc.core;

/**
 * Represents a single IRC channel. It is created from {@link IrcConnection#join(String)} and allows the user to
 * post to the channel and listen for messages there.
 */
public final class IrcChannel {
    private final IrcConnection ircConnection;
    private final String channelName;

    public IrcChannel(IrcConnection ircConnection, String channelName) {
        this.ircConnection = ircConnection;
        this.channelName = channelName;
    }

    public WatcherID registerWatcher(final ChannelWatcher watcher) {
        return ircConnection.registerWatcher(message -> {
            if (message.contains(channelName)) {
                watcher.onMessage(message);
            }
        });
    }

    public void unregisterWatcher(WatcherID watcherID) {
        ircConnection.unregisterWatcher(watcherID);
    }

    public void postMessage(String message) {
        ircConnection.write("PRIVMSG", channelName + " :" + message);
    }
}
