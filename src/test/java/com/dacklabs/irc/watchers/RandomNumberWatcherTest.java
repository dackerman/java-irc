package com.dacklabs.irc.watchers;

import com.dacklabs.irc.core.IrcMessagePoster;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomNumberWatcherTest {

    @Test
    public void botIgnoresIrrelevantMessages() {
        FakeMessagePoster poster = new FakeMessagePoster();
        RandomNumberWatcher watcher = new RandomNumberWatcher("sally", poster);

        watcher.onMessage("#test blah");
        watcher.onMessage("#test hello tom");

        assertThat(poster.postedMessages).isEmpty();
    }

    @Test
    public void botRespondsToARandomNumberRequest() {
        double multiplier = 0.5;
        int randomValueBound = 5000;

        FakeMessagePoster poster = new FakeMessagePoster();
        RandomNumberWatcher watcher = new RandomNumberWatcher("tombot", poster, new StaticNumberSource(multiplier));

        watcher.onMessage("#test tombot random " + randomValueBound);

        assertThat(poster.postedMessages).containsExactly("Your random number is " + (int)(randomValueBound * multiplier));
    }

    @Test
    public void botRespondsWithDifferentRandomValuesEachTime() {
        FakeMessagePoster poster = new FakeMessagePoster();
        RandomNumberWatcher watcher = new RandomNumberWatcher("tombot", poster, new StaticNumberSource(0.1, 0.2, 0.5));

        watcher.onMessage("#test tombot random 100"); // * 0.1
        watcher.onMessage("#test tombot random 100"); // * 0.2
        watcher.onMessage("#test tombot random 100"); // * 0.5

        assertThat(poster.postedMessages).containsExactly(
                "Your random number is 10",
                "Your random number is 20",
                "Your random number is 50");
    }

    @Test
    public void botRespondsToExtremelyLargeNumbers() {
        String hugeNumber = "9287349826592385623943984732847230572380574805724857047504758047357438057";

        FakeMessagePoster poster = new FakeMessagePoster();
        RandomNumberWatcher watcher = new RandomNumberWatcher("rainman", poster, new StaticNumberSource(1.0));

        watcher.onMessage("#test rainman random " + hugeNumber);

        assertThat(poster.postedMessages).containsExactly("Your random number is " + hugeNumber);
    }

    private static final class FakeMessagePoster implements IrcMessagePoster {

        public final List<String> postedMessages = new ArrayList<>();

        @Override
        public void postMessage(String message) {
            postedMessages.add(message);
        }
    }

    private static final class StaticNumberSource implements RandomNumberWatcher.RNGSource {

        private final double[] vals;
        private int index = 0;

        public StaticNumberSource(double... vals) {
            this.vals = vals;
        }

        @Override
        public double nextDouble() {
            if (index >= vals.length)
                throw new IllegalStateException("nextDouble called more times than values available");

            double val = vals[index];
            index++;
            return val;
        }
    }
}