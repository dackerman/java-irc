package com.dacklabs.irc.watchers;

import com.dacklabs.irc.core.ChannelWatcher;
import com.dacklabs.irc.core.IrcMessagePoster;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A channel watcher that looks for people requesting random numbers, and returns a random number between
 * zero and the requestor's specified number.
 */
public final class RandomNumberWatcher implements ChannelWatcher {

    private final IrcMessagePoster channel;
    private final Pattern randomNumberRegex;
    private final RNGSource rngSource;

    /**
     * Creates a new watcher that listens for messages to the given name, on the given channel.
     *
     * @param botName The name of the bot for which to respond to messages
     * @param poster The channel in which to post messages to
     */
    public RandomNumberWatcher(String botName, IrcMessagePoster poster) {
        this(botName, poster, new RandomRNGSource());
    }

    public RandomNumberWatcher(String botName, IrcMessagePoster poster, RNGSource rngSource) {
        this.channel = poster;
        this.randomNumberRegex = Pattern.compile(".*" + botName + ".*random ([^\\s]+).*");
        this.rngSource = rngSource;
    }

    @Override
    public void onMessage(String message) {
        Matcher matcher = randomNumberRegex.matcher(message);
        if (matcher.matches()) {
            try {
                BigDecimal bound = new BigDecimal(matcher.group(1));
                double someRandomness = rngSource.nextDouble();
                BigInteger random = bound.multiply(new BigDecimal(someRandomness)).toBigInteger();
                channel.postMessage("Your random number is " + random);
            } catch (NumberFormatException e) {
                channel.postMessage("Hmm, that didn't look like a number to me.");
            }
        }
    }

    private static final class RandomRNGSource implements RNGSource {

        private final Random random = new Random();

        @Override
        public double nextDouble() {
            return random.nextDouble();
        }
    }

    public interface RNGSource {
        double nextDouble();
    }
}
