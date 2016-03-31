package com.dacklabs.irc.cli;

import com.dacklabs.irc.core.*;
import com.dacklabs.irc.watchers.RandomNumberWatcher;
import com.dacklabs.irc.watchers.StandardOutMessagePrinter;

import java.io.IOException;

public class IrcMain {

    private static final String BOT_NAME = "dackbot";
    private static final String IRC_SERVER = "chat.freenode.net";
    private static final int IRC_SERVER_PORT = 6667;

    public static void main(String[] args) throws IOException {
        SocketIrcConnection irc = SocketIrcConnection.login(new IrcConnectionInfo(IRC_SERVER, IRC_SERVER_PORT, BOT_NAME));

        IrcChannel dailyProgrammerChannel = irc.join("#reddit-dailyprogrammer");
        IrcChannel bottersTestChannel = irc.join("#botters-test");

        dailyProgrammerChannel.registerWatcher(new StandardOutMessagePrinter());
        bottersTestChannel.registerWatcher(new StandardOutMessagePrinter());

        bottersTestChannel.registerWatcher(new RandomNumberWatcher(BOT_NAME, bottersTestChannel));

        irc.readInputSynchronously(); // blocks while the IRC connection is running

        irc.close();
        System.out.println("Done!");
    }
}
