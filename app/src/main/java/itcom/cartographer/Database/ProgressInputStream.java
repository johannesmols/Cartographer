package itcom.cartographer.Database;

import android.support.annotation.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A special InputStream that estimates the progress of how much of the file has been read
 * Source: https://stackoverflow.com/questions/27890287/how-to-publish-progress-for-large-json-file-parsing-with-gson
 */
public class ProgressInputStream extends FilterInputStream {

    private final int size;
    private long bytesRead;
    private int percent;
    private List<Listener> listeners = new ArrayList<>();

    public ProgressInputStream(InputStream in) {
        super(in);
        try {
            size = available();
            if (size == 0) throw new IOException();
        } catch (IOException e) {
            throw new RuntimeException("This reader can only be used on InputStreams with a known size", e);
        }
        bytesRead = 0;
        percent = 0;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Read the file and update the progress with each byte
     */
    @Override
    public int read() throws IOException {
        int b = super.read();
        updateProgress(1);
        return b;
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        return updateProgress(super.read(b));
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        return updateProgress(super.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return updateProgress(super.skip(n));
    }

    @Override
    public void mark(int readLimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Estimate the progress
     * @return the estimated progress in percentage
     */
    private <T extends Number> T updateProgress(T numBytesRead) {
        if (numBytesRead.longValue() > 0) {
            bytesRead += numBytesRead.longValue();
            if (bytesRead * 100 / size > percent) {
                percent = (int) (bytesRead * 100 / size);
                for (Listener listener : listeners) {
                    listener.onProgressChanged(percent, bytesRead, size);
                }
            }
        }
        return numBytesRead;
    }

    public interface Listener {
        void onProgressChanged(int percentage, long bytesRead, int size);
    }
}