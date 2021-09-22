package org.example;

import org.elasticsearch.client.Request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TimeStampIdGenerator {
    private final long timestamp;
    private final AtomicLong id = new AtomicLong(0);
    private static String uploadId = null;
    private static final Object lock = new Object();

    public TimeStampIdGenerator(long timestamp) {
        this.timestamp = timestamp;
    }

    public String generateUploadId() {
        synchronized (lock) {
            if (uploadId == null) {
                final String prefix = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(timestamp));
                final int randomNumber = new Random(System.currentTimeMillis()).nextInt() % 100;
                uploadId = prefix + "_" + String.format("%03d", randomNumber);
            }
        }
        return uploadId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(timestamp));
    }
}
