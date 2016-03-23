package com.dacklabs.irc.watchers;

import com.dacklabs.irc.core.ChannelWatcher;
import com.dacklabs.irc.core.IrcChannel;

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

    private final IrcChannel channel;
    private final Pattern randomNumberRegex;

    public RandomNumberWatcher(String botName, IrcChannel channel) {
        this.channel = channel;
        this.randomNumberRegex = Pattern.compile(".*" + botName + ".*random (\\d+).*");
    }

    @Override
    public void onMessage(String message) {
        Matcher matcher = randomNumberRegex.matcher(message);
        if (matcher.matches()) {
            try {
                BigDecimal bound = new BigDecimal(matcher.group(1));
                double someRandomness = new Random().nextDouble();
                BigInteger random = bound.multiply(new BigDecimal(someRandomness)).toBigInteger();
                channel.postMessage("Your random number is " + random);
            } catch (NumberFormatException e) {
                channel.postMessage("Hmm, that didn't look like a number to me.");
            }
        }
    }
}
