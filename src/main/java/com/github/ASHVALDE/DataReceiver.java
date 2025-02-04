package com.github.ASHVALDE;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

class DataReceiver implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private final InputStream input;
    private final List<Byte> buffer = new Stack<Byte>();
    private final CopyOnWriteArrayList<DataListener> listeners = new CopyOnWriteArrayList<>();

    public DataReceiver(InputStream input) {
        this.input = input;
    }

    public interface DataListener {
        void onData(Stack<Byte> data) throws IOException;
    }

    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void run() {
        try {
            byte[] chunk = new byte[BUFFER_SIZE];
            while (!Thread.interrupted()) {
                if (input.available() > 0) {
                    int read = input.read(chunk);
                    for (int i = 0; i < read; i++) buffer.add(chunk[i]);
                } else if (!buffer.isEmpty()) {
                    notifyListeners();
                    buffer.clear();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Stream error", e);
        }
    }

    private void notifyListeners() throws IOException {
        for (DataListener listener : listeners) {
            listener.onData((Stack<Byte>) buffer);
        }
    }
}