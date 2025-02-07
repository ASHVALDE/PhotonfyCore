package com.github.ASHVALDE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

class DataReceiver implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private final InputStream input;
    // Removed the unused class member 'buffer' to avoid confusion.
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
        // The header is 8 bytes:
        // 2 bytes SYNC (0x55, 0x02), 1 byte FLAGS, 1 byte TOKEN,
        // 1 byte FUNCTION, 2 bytes SEQ, 1 byte LENGTH.
        final int HEADER_SIZE = 8;
        final int MAX_BUFFER_SIZE = 4096; // adjust as needed
        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);

        // A temporary buffer for reading data from the stream
        byte[] chunk = new byte[BUFFER_SIZE];

        try {
            while (!Thread.currentThread().isInterrupted()) {
                int bytesRead = input.read(chunk);
                if (bytesRead == -1) {
                    break; // End of stream
                }
                // Append the new data to our ByteBuffer
                byteBuffer.put(chunk, 0, bytesRead);
                byteBuffer.flip(); // Prepare to read from the buffer

                // Process as many complete packages as possible
                while (byteBuffer.remaining() >= HEADER_SIZE) {
                    byteBuffer.mark(); // Mark current position in case we don't have a full package yet

                    // Look for the sync bytes (0x55, 0x02)
                    byte sync1 = byteBuffer.get();
                    byte sync2 = byteBuffer.get();
                    if ((sync1 & 0xFF) != 0x55 || (sync2 & 0xFF) != 0x02) {
                        // Not a valid sync sequence, shift one byte and try again
                        byteBuffer.reset();
                        byteBuffer.get(); // Skip one byte
                        continue;
                    }

                    // Make sure we have the rest of the header
                    if (byteBuffer.remaining() < (HEADER_SIZE - 2)) {
                        // Not enough header bytes; wait for more data
                        byteBuffer.reset();
                        break;
                    }

                    // Skip the next header fields (flags, token, function, seq)
                    byteBuffer.get();  // FLAGS
                    byteBuffer.get();  // TOKEN
                    byteBuffer.get();  // FUNCTION
                    byteBuffer.getShort(); // SEQ

                    // Read the LENGTH field that tells us the payload size
                    byte lengthByte = byteBuffer.get();
                    int payloadLength = lengthByte & 0xFF;
                    int totalPackageSize = HEADER_SIZE + payloadLength;

                    // Check if the full package is available in the buffer
                    if (byteBuffer.remaining() < payloadLength) {
                        // Not enough data yet, wait for more
                        byteBuffer.reset();
                        break;
                    }

                    // Reset to the beginning of the package and extract the full package
                    byteBuffer.reset();
                    byte[] frame = new byte[totalPackageSize];
                    byteBuffer.get(frame, 0, totalPackageSize);

                    // Notify listeners with the complete package (header + payload)
                    notifyListeners(frame);
                }

                // Compact the buffer: discard processed bytes and prepare for more incoming data.
                byteBuffer.compact();
            }
        } catch (IOException e) {
            throw new RuntimeException("Stream error", e);
        }
    }

    /**
     * Converts the complete package (frame) to a Stack<Byte> and notifies all listeners.
     */
    private void notifyListeners(byte[] frame) throws IOException {
        // Convert byte[] to Stack<Byte>
        Stack<Byte> dataStack = new Stack<>();
        for (byte b : frame) {
            dataStack.push(b);
        }

        for (DataListener listener : listeners) {
            listener.onData(dataStack);
        }
    }
}