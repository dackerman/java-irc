package com.dacklabs.irc.testing;

import com.dacklabs.irc.core.ChannelWatcher;

import java.util.ArrayList;
import java.util.List;

public final class StringListWatcher implements ChannelWatcher {

    public final List<String> messages = new ArrayList<>();

    @Override
    public void onMessage(String message) {
        messages.add(message);
    }
}
