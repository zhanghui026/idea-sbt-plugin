// Copyright © 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.sbt.runner;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MulticastPipe extends Writer {

    // TODO: PipedReader lags with the default 1024 size buffer, when seeing sbt's "actions" or "help"
    // Would some other data structure have better latency?
    private static final int PIPE_BUFFER_SIZE = 8 * 1024;

    private final List<PipedWriter> subscribers = new CopyOnWriteArrayList<PipedWriter>();

    public Reader subscribe() throws IOException {
        PipedReader r = new PipedReader(PIPE_BUFFER_SIZE);
        subscribers.add(new PipedWriter(r));
        return r;
    }

    private void unsubscribe(PipedWriter w) {
        subscribers.remove(w);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        for (PipedWriter w : subscribers) {
            try {
                w.write(cbuf, off, len);
            } catch (IOException e) {
                unsubscribe(w);
            }
        }
    }

    public void flush() throws IOException {
        for (PipedWriter w : subscribers) {
            try {
                w.flush();
            } catch (IOException e) {
                unsubscribe(w);
            }
        }
    }

    public void close() throws IOException {
        for (PipedWriter w : subscribers) {
            try {
                w.close();
            } catch (IOException e) {
                unsubscribe(w);
            }
        }
    }
}
