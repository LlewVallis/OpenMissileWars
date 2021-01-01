package org.astropeci.omwlauncher;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * An input stream which occasionally notifies a callback of how many bytes have been read.
 *
 * The stream aims to update the listener every 250ms, but this is not guaranteed. Furthermore, no updates will be
 * provided if no bytes are read or a bulk read operation is exceedingly slow.
 */
@RequiredArgsConstructor
public class ProgressInputStream extends InputStream {

    private final InputStream underlying;
    private final Consumer<Long> listener;

    private long count = 0;
    private long lastUpdate = Long.MIN_VALUE;

    @Override
    public int read() throws IOException {
        // Attempt to read a byte, if successful count one byte
        int value = underlying.read();
        if (value != -1) {
            addCount(1);
        }
        return value;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Attempt to read up to len bytes, if the end of the stream hasn't been reached update the count accordingly
        int amount = underlying.read(b, off, len);
        if (amount != -1) {
            addCount(amount);
        }
        return amount;
    }

    /**
     * Updates the internal byte count and notifies the listener if it has not received an update recently.
     */
    private void addCount(long amount) {
        count += amount;

        // De-dupe if an update was provided in the last 250ms
        long now = System.currentTimeMillis();
        if (now > lastUpdate + 250) {
            lastUpdate = now;
            listener.accept(count);
        }
    }
}
