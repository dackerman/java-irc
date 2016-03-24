package com.dacklabs.irc.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class WatcherIDTest {
    @Test
    public void twoWatcherIDsDoNotHashToTheSameThing() {
        WatcherID a = WatcherID.create();
        WatcherID b = WatcherID.create();

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void aWatcherIDHashesToItself() {
        WatcherID a = WatcherID.create();

        assertEquals(a.hashCode(), a.hashCode());
    }
}