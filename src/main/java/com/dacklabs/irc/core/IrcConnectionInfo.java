package com.dacklabs.irc.core;

/**
 * Data containing information about the IRC server to connect to, and the user to login with.
 */
public final class IrcConnectionInfo {
    public final String ircServer;
    public final int port;
    public final String nick;
    public final String username;

    public IrcConnectionInfo(String ircServer, int port, String nick, String username) {
        this.ircServer = ircServer;
        this.port = port;
        this.nick = nick;
        this.username = username;
    }

    public IrcConnectionInfo(String ircServer, int port, String username) {
        this(ircServer, port, username, username);
    }
}
