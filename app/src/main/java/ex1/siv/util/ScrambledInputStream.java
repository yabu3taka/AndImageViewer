package ex1.siv.util;

import androidx.annotation.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ScrambledInputStream extends FilterInputStream {
    private final int mScrambled;

    public ScrambledInputStream(InputStream in, int scrambled) {
        super(in);
        mScrambled = scrambled;
    }

    public int read() throws IOException {
        int b = in.read();
        if (b == -1) {
            return -1;
        }
        b = b ^ mScrambled;
        return b;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int count) throws IOException {
        int len = in.read(buffer, offset, count);
        for (int i = 0; i < len; ++i) {
            int p = i + offset;
            buffer[p] = (byte) (buffer[p] ^ mScrambled);
        }
        return len;
    }
}
