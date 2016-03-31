package com.dacklabs.irc.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LineWatcherDispatcherTest {

    @Test
    public void registeredWatchersGetMessagesFromEachLine() {
        LineWatcherDispatcher dispatcher = new LineWatcherDispatcher(Arrays.asList("line 1", "line 2").iterator());

        List<String> lines = new ArrayList<>();
        dispatcher.registerWatcher(lines::add);

        dispatcher.start();

        assertThat(lines).containsExactly("line 1", "line 2");
    }

    @Test
    public void registeredWatchesCanBeRemoved() {
        LineWatcherDispatcher dispatcher = new LineWatcherDispatcher(Arrays.asList("line 1", "line 2").iterator());

        List<String> lines = new ArrayList<>();
        WatcherID watcherID = dispatcher.registerWatcher(lines::add);

        boolean wasRemoved = dispatcher.unregisterWatcher(watcherID);

        dispatcher.start();

        assertThat(wasRemoved)
                .withFailMessage("The dispatcher didn't think it removed the watcher")
                .isTrue();

        assertThat(lines)
                .withFailMessage("The watcher got messages despite being removed")
                .isEmpty();
    }

    @Test
    public void removingANonExistentWatcherReturnsFalse() {
        LineWatcherDispatcher dispatcher = new LineWatcherDispatcher(Collections.singletonList("test").iterator());

        boolean wasRemoved = dispatcher.unregisterWatcher(WatcherID.create());

        assertThat(wasRemoved)
                .withFailMessage("The dispatcher thought it removed a watcher, but didn't")
                .isFalse();
    }

    @Test
    public void tryingToRemoveANonExistentWatcherDoesNotAffectOtherWatchers() {
        LineWatcherDispatcher dispatcher = new LineWatcherDispatcher(Arrays.asList("line 1", "line 2").iterator());

        List<String> lines = new ArrayList<>();
        dispatcher.registerWatcher(lines::add);

        boolean wasRemoved = dispatcher.unregisterWatcher(WatcherID.create());

        dispatcher.start();

        assertThat(lines).containsExactly("line 1", "line 2");
        assertThat(wasRemoved).isFalse();
    }
}