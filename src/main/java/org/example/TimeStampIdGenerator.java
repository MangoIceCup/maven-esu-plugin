package org.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class TimeStampIdGenerator {
    private final long timestamp;
    private final AtomicLong id = new AtomicLong(0);

    public TimeStampIdGenerator(long timestamp) {
        this.timestamp = timestamp;
    }

    public String generateUploadId() {
        final String prefix = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(timestamp));
        return prefix + "_" + String.format("%03d", id.incrementAndGet());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(timestamp));
    }
}
