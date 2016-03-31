package com.dacklabs.irc.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Represents a connection to an IRC chat server. Calling {@link SocketIrcConnection#login(IrcConnectionInfo)} will create a
 * new instance of the IRC connection, and then {@link SocketIrcConnection#join(String)} will join a channel, which can then
 * be read from and written to.
 */
public final class SocketIrcConnection implements IrcConnection {

    private final Socket socket;
    private final PrintWriter out;

    private final LineWatcherDispatcher lineWatcherDispatcher;

    public static SocketIrcConnection login(IrcConnectionInfo connectionInfo) throws IOException {
        Socket socket = new Socket(connectionInfo.ircServer, connectionInfo.port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        InputStream socketInput = socket.getInputStream();
        Scanner scanner = new Scanner(socketInput);

        Iterator<String> lineIterator = new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return scanner.hasNextLine();
            }

            @Override
            public String next() {
                return scanner.nextLine();
            }
        };

        LineWatcherDispatcher lineWatcherDispatcher = new LineWatcherDispatcher(lineIterator);

        SocketIrcConnection connection = new SocketIrcConnection(socket, out, lineWatcherDispatcher);
        connection.write("NICK", connectionInfo.nick);
        connection.write("USER", connectionInfo.username + " 0 * :" + connectionInfo.username);
        return connection;
    }

    private SocketIrcConnection(Socket socket, PrintWriter out, LineWatcherDispatcher lineWatcherDispatcher) {
        this.socket = socket;
        this.out = out;
        this.lineWatcherDispatcher = lineWatcherDispatcher;
    }

    public void readInputAsynchronously() {
        Thread watcherThread = new Thread(lineWatcherDispatcher::start);
        watcherThread.start();
    }

    public void readInputSynchronously() {
        lineWatcherDispatcher.start();
    }

    @Override
    public void write(String command, String message) {
        String fullMessage = command + " " + message;
        System.out.println(">>> " + fullMessage);  // Side effect - remove eventually
        out.print(fullMessage + "\r\n");
        out.flush();
    }

    @Override
    public WatcherID registerWatcher(ChannelWatcher watcher) {
        return lineWatcherDispatcher.registerWatcher(watcher);
    }

    @Override
    public boolean unregisterWatcher(WatcherID watcherID) {
        return lineWatcherDispatcher.unregisterWatcher(watcherID);
    }

    public IrcChannel join(String channelName) {
        write("JOIN", channelName);
        return new IrcChannel(this, channelName);
    }

    @Override
    public void close() throws IOException {
        out.close();
        socket.close();
    }
}
