package org.astropeci.omwlauncher;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

@RequiredArgsConstructor
class ProgressInputStream extends InputStream {

    private final InputStream underlying;
    private final Consumer<Long> listener;

    private long count = 0;
    private long lastUpdate = Long.MIN_VALUE;

    @Override
    public int read() throws IOException {
        int value = underlying.read();
        if (value != -1) {
            addCount(1);
        }
        return value;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int amount = underlying.read(b, off, len);
        if (amount != -1) {
            addCount(amount);
        }
        return amount;
    }

    private void addCount(long amount) {
        count += amount;

        long now = System.currentTimeMillis();
        if (now > lastUpdate + 250) {
            lastUpdate = now;
            listener.accept(count);
        }
    }
}
