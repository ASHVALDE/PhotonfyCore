package com.github.ASHVALDE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Photonfy extends Thread {
    private final CopyOnWriteArrayList<DataListener> listeners = new CopyOnWriteArrayList<>();

    public interface DataListener {
        void onPackageReceived(DataPackage data) throws IOException;
    }

    public void addListener(DataListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(DataPackage dataPackage) throws IOException {
        for (DataListener listener : listeners) {
            listener.onPackageReceived(dataPackage);
        }
    }

    private InputStream deviceInputConection;
    private OutputStream deviceOutputConection;
    private DataPackage multiTramaBuffer = new DataPackage();
    ExecutorService mainThreadExecutor = Executors.newSingleThreadExecutor();
    public DataSender sender;
    public Photonfy(InputStream deviceInput,OutputStream outputStream) throws IOException {
        deviceInputConection = deviceInput;
        deviceOutputConection = outputStream;
        // Cuando creamos un photonfy creamos dos hilos, Generemos uno para Recibir los paquetes de datos
        DataReceiver receiver = new DataReceiver(deviceInput);
        Thread ReceiverThread = new Thread(receiver);
        ReceiverThread.start();

        receiver.addListener(data -> {
            Stack<Byte> dataCopy = new Stack<>();
            dataCopy.addAll(data); // Clonamos los elementos del stack original
            mainThreadExecutor.execute(() -> {
                try {
                    DataPackage receivedDataPackage = new DataPackage().fromBytes(dataCopy);
                    if (receivedDataPackage.flags == 1 && receivedDataPackage.sequence == 0) {

                        /* Aqui inicia el multitrama si hay */
                        multiTramaBuffer = receivedDataPackage;
                        return;
                    }
                    if (receivedDataPackage.flags == 1) {
                        /* Aqui continua el multitrama si hay */
                        multiTramaBuffer.payload.addAll(receivedDataPackage.payload);

                        return;
                    }
                    if(receivedDataPackage.flags == 0 && receivedDataPackage.sequence != 0) {
                        /* Aqui finaliza el multitrama si hay */
                        multiTramaBuffer.payload.addAll(receivedDataPackage.payload);
                        if(multiTramaBuffer.name==PhotonfyCodes.EV_FOTO){
                            multiTramaBuffer.token = 0x05; // Update de calidad, Basicamente todos los TOKEN corresponden con sus codigos de comando
                        }
                        notifyListeners(multiTramaBuffer);

                        return;
                    }
                    if (receivedDataPackage.flags == 0) {
                        notifyListeners(receivedDataPackage);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        });
        sender = new DataSender(outputStream);

    }
}
