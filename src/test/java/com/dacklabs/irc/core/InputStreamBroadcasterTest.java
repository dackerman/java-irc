package com.dacklabs.irc.core;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class InputStreamBroadcasterTest {

    @Test
    public void registeredWatchersGetMessagesFromTheInputStream() {
        ByteArrayInputStream testStream = new ByteArrayInputStream("line 1\nline 2".getBytes());

        InputStreamBroadcaster broadcaster = new InputStreamBroadcaster(testStream);

        List<String> lines = new ArrayList<>();
        broadcaster.registerWatcher(lines::add);

        broadcaster.start();

        assertThat(lines).containsExactly("line 1", "line 2");
    }

    @Test
    public void registeredWatchesCanBeRemoved() {
        ByteArrayInputStream testStream = new ByteArrayInputStream("line 1\nline 2".getBytes());

        InputStreamBroadcaster broadcaster = new InputStreamBroadcaster(testStream);

        List<String> lines = new ArrayList<>();
        WatcherID watcherID = broadcaster.registerWatcher(lines::add);

        boolean wasRemoved = broadcaster.unregisterWatcher(watcherID);

        broadcaster.start();

        assertThat(wasRemoved)
                .withFailMessage("The broadcaster didn't think it removed the watcher")
                .isTrue();

        assertThat(lines)
                .withFailMessage("The watcher got messages despite being removed")
                .isEmpty();
    }

    @Test
    public void removingANonExistentWatcherReturnsFalse() {
        ByteArrayInputStream testStream = new ByteArrayInputStream("test".getBytes());

        InputStreamBroadcaster broadcaster = new InputStreamBroadcaster(testStream);

        boolean wasRemoved = broadcaster.unregisterWatcher(WatcherID.create());

        assertThat(wasRemoved)
                .withFailMessage("The broadcaster thought it removed a watcher, but didn't")
                .isFalse();
    }

    @Test
    public void tryingToRemoveANonExistentWatcherDoesNotAffectOtherWatchers() {
        ByteArrayInputStream testStream = new ByteArrayInputStream("line 1\nline 2".getBytes());

        InputStreamBroadcaster broadcaster = new InputStreamBroadcaster(testStream);

        List<String> lines = new ArrayList<>();
        broadcaster.registerWatcher(lines::add);

        boolean wasRemoved = broadcaster.unregisterWatcher(WatcherID.create());

        broadcaster.start();

        assertThat(lines).containsExactly("line 1", "line 2");
        assertThat(wasRemoved).isFalse();
    }

    @Test
    public void broadcasterCanBeStopped() throws InterruptedException {
        CharacterQueueInputStream stream = new CharacterQueueInputStream();
        LinkedBlockingQueue<Character> queue = stream.queue;

        InputStreamBroadcaster broadcaster = new InputStreamBroadcaster(stream);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        AtomicBoolean finished = new AtomicBoolean(false);

        executor.submit(() -> {
            broadcaster.start();
            finished.set(true);
        });

        executor.submit(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            broadcaster.stop();
            queue.add('a');
            queue.add('\n');
        });

        executor.shutdown();
        boolean terminated = executor.awaitTermination(1, TimeUnit.SECONDS);

        assertThat(terminated).withFailMessage("Threads didn't terminate").isTrue();
        assertThat(finished.get()).withFailMessage("Broadcaster didn't exit gracefully").isTrue();
    }

    private static final class CharacterQueueInputStream extends InputStream {

        public final LinkedBlockingQueue<Character> queue = new LinkedBlockingQueue<>();

        @Override
        public int read() throws IOException {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                fail("Interrupted thread while trying to read from queue", e);
            }
            return 'a'; // Don't want to return -1 as it might be a false positive for the test
        }
    }
}