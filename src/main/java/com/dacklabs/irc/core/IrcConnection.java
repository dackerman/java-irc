package com.dacklabs.irc.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Represents a connection to an IRC chat server. Calling {@link IrcConnection#login(IrcConnectionInfo)} will create a
 * new instance of the IRC connection, and then {@link IrcConnection#join(String)} will join a channel, which can then
 * be read from and written to.
 */
public final class IrcConnection implements Closeable {

    private final Socket socket;
    private final PrintWriter out;

    private final InputStreamBroadcaster inputStreamBroadcaster;

    public static IrcConnection login(IrcConnectionInfo connectionInfo) throws IOException {
        Socket socket = new Socket("chat.freenode.net", 6667);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        InputStreamBroadcaster inputStreamBroadcaster = new InputStreamBroadcaster(socket.getInputStream());

        IrcConnection connection = new IrcConnection(socket, out, inputStreamBroadcaster);
        connection.write("NICK", connectionInfo.nick);
        connection.write("USER", connectionInfo.username + " 0 * :" + connectionInfo.username);
        return connection;
    }

    private IrcConnection(Socket socket, PrintWriter out, InputStreamBroadcaster inputStreamBroadcaster) {
        this.socket = socket;
        this.out = out;
        this.inputStreamBroadcaster = inputStreamBroadcaster;
    }

    public void readInputAsynchronously() {
        Thread watcherThread = new Thread(inputStreamBroadcaster::start);
        watcherThread.start();
    }

    public void readInputSynchronously() {
        inputStreamBroadcaster.start();
    }

    public void write(String command, String message) {
        String fullMessage = command + " " + message;
        System.out.println(">>> " + fullMessage);  // Side effect - remove eventually
        out.print(fullMessage + "\r\n");
        out.flush();
    }

    public WatcherID registerWatcher(ChannelWatcher watcher) {
        return inputStreamBroadcaster.registerWatcher(watcher);
    }

    public boolean unregisterWatcher(WatcherID watcherID) {
        return inputStreamBroadcaster.unregisterWatcher(watcherID);
    }

    public IrcChannel join(String channelName) {
        write("JOIN", channelName);
        return new IrcChannel(this, channelName);
    }

    @Override
    public void close() throws IOException {
        inputStreamBroadcaster.stop();
        out.close();
        socket.close();
    }
}
