package com.dacklabs.irc.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.Channel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a connection to an IRC chat server. Calling {@link IrcConnection#login(IrcConnectionInfo)} will create a
 * new instance of the IRC connection, and then {@link IrcConnection#join(String)} will join a channel, which can then
 * be read from and written to.
 */
public final class IrcConnection implements Closeable {

    private final Socket socket;
    private final PrintWriter out;

    private final InputWatcher inputWatcher;

    private static final class InputWatcher implements Runnable {

        private final Socket socket;
        private final ConcurrentHashMap<WatcherID, ChannelWatcher> watchers = new ConcurrentHashMap<>();

        private final AtomicBoolean shouldStopThread = new AtomicBoolean(false);

        public InputWatcher(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Scanner in = new Scanner(socket.getInputStream());
                while (!shouldStopThread.get() && in.hasNext()) {
                    String message = in.nextLine();
                    for (ChannelWatcher watcher : watchers.values()) {
                        watcher.onMessage(message);
                    }
                }
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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

    public static IrcConnection login(IrcConnectionInfo connectionInfo) throws IOException {
        Socket socket = new Socket("chat.freenode.net", 6667);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        InputWatcher inputWatcher = new InputWatcher(socket);

        IrcConnection connection = new IrcConnection(socket, out, inputWatcher);
        connection.write("NICK", connectionInfo.nick);
        connection.write("USER", connectionInfo.username + " 0 * :" + connectionInfo.username);
        return connection;
    }

    private IrcConnection(Socket socket, PrintWriter out, InputWatcher inputWatcher) {
        this.socket = socket;
        this.out = out;
        this.inputWatcher = inputWatcher;
    }

    public void readInputAsynchronously() {
        Thread watcherThread = new Thread(inputWatcher);
        watcherThread.start();
    }

    public void readInputSynchronously() {
        inputWatcher.run();
    }

    public void write(String command, String message) {
        String fullMessage = command + " " + message;
        System.out.println(">>> " + fullMessage);  // Side effect - remove eventually
        out.print(fullMessage + "\r\n");
        out.flush();
    }

    public WatcherID registerWatcher(ChannelWatcher watcher) {
        return inputWatcher.registerWatcher(watcher);
    }

    public boolean unregisterWatcher(WatcherID watcherID) {
        return inputWatcher.unregisterWatcher(watcherID);
    }

    public IrcChannel join(String channelName) {
        write("JOIN", channelName);
        return new IrcChannel(this, channelName);
    }

    @Override
    public void close() throws IOException {
        inputWatcher.stop();
        out.close();
        socket.close();
    }
}
