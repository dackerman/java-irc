package com.dacklabs.irc.core;

import com.dacklabs.irc.testing.StringListWatcher;
import com.dacklabs.irc.watchers.StandardOutMessagePrinter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the Irc connection with a real socket.
 */
public class IrcChannelEndToEndTest {

    @Ignore // UnIgnore to test with a real server
    @Test
    public void ircConnectionWorksWithARealServer() throws IOException, InterruptedException {
        SocketIrcConnection irc = SocketIrcConnection.login(
                new IrcConnectionInfo("chat.freenode.net", 6667, "dackbotintegration"));

        IrcChannel bottersTestChannel = irc.join("#botters-test");

        StringListWatcher watcher = new StringListWatcher();

        bottersTestChannel.registerWatcher(watcher);
        bottersTestChannel.registerWatcher(new StandardOutMessagePrinter());

        bottersTestChannel.postMessage("Test message");

        irc.readInputAsynchronously();

        Thread.sleep(30000); // Wait for login?

        assertThat(watcher.messages).contains(":ChanServ!ChanServ@services. NOTICE dackbotintegrati :[#botters-test] Welcome");
    }
}
