package com.dacklabs.irc.core;

import com.dacklabs.irc.testing.StringListWatcher;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IrcChannelTest {

    @Test
    public void registeringAWatcherCausesItToBeInvokedOnlyForThatChannel() {
        FakeIrcConnection connection = new FakeIrcConnection("#blah :blah message", "#test-channel :test message");
        IrcChannel channel = new IrcChannel(connection, "#test-channel");

        StringListWatcher fakeWatcher = new StringListWatcher();
        channel.registerWatcher(fakeWatcher);

        connection.start();

        assertThat(fakeWatcher.messages).containsExactly("#test-channel :test message");
    }

    @Test
    public void unregisteringAWatcherStopsItFromReceivingMessages() {
        FakeIrcConnection connection = new FakeIrcConnection("#blah :blah message", "#test-channel :test message");
        IrcChannel channel = new IrcChannel(connection, "#test-channel");

        StringListWatcher fakeWatcher = new StringListWatcher();
        WatcherID watcherID = channel.registerWatcher(fakeWatcher);

        channel.unregisterWatcher(watcherID);

        connection.start();

        assertThat(fakeWatcher.messages).isEmpty();
    }

    @Test
    public void postMessageSendsTheAppropriateIRCMessageToTheServerForTheRightChannel() {
        FakeIrcConnection connection = new FakeIrcConnection();
        IrcMessagePoster channel = new IrcChannel(connection, "#blah");

        channel.postMessage("Test message");

        assertThat(connection.linesWritten).containsExactly(new String[] {"PRIVMSG", "#blah :Test message"});
    }

    private static final class FakeIrcConnection implements IrcConnection {
        private final LineWatcherDispatcher dispatcher;

        public boolean closed = false;
        public final List<String[]> linesWritten = new ArrayList<>();

        public FakeIrcConnection(String... lines) {
            dispatcher = new LineWatcherDispatcher(Arrays.asList(lines).iterator());
        }

        public void start() {
            dispatcher.start();
        }

        @Override
        public void write(String command, String message) {
            linesWritten.add(new String[]{command, message});
        }

        @Override
        public WatcherID registerWatcher(ChannelWatcher watcher) {
            return dispatcher.registerWatcher(watcher);
        }

        @Override
        public boolean unregisterWatcher(WatcherID watcherID) {
            return dispatcher.unregisterWatcher(watcherID);
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }
}