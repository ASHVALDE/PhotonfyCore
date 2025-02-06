package com.github.ASHVALDE;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.graalvm.compiler.debug.DebugOptions.Log;

public class DataSender {
    private final OutputStream outputStream;

    public DataSender(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void sendCommand(int command) throws IOException {
        DataPackage datos = new DataPackage();
        datos.function = command;
        sendCommand(datos);
    }


    public void GET_INTEGRATION_TIME() throws IOException {
        sendCommand(PhotonfyCodes.GET_INTEGRATION_TIME.getHexCode());
    }

    public void GET_GAIN() throws IOException {
        sendCommand(PhotonfyCodes.GET_GAIN.getHexCode());
    }

    public void GET_RAW_SPECTRUM() throws IOException {
        sendCommand(PhotonfyCodes.GET_RAW_SPECTRUM.getHexCode());
    }

    public void START_STREAMING() throws IOException {
        sendCommand(PhotonfyCodes.START_STREAMING.getHexCode());
    }

    public void STOP_STREAMING() throws IOException {
        sendCommand(PhotonfyCodes.STOP_STREAMING.getHexCode());
    }

    public void GET_TRANSFER_FUNCTION() throws IOException {
        sendCommand(PhotonfyCodes.GET_TRANSFER_FUNCTION.getHexCode());
    }

    public void GET_DEFAULT_TRANSFER_FUNCTION() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEFAULT_TRANSFER_FUNCTION.getHexCode());
    }

    public void RELOAD_DEFAULT_TRANSFER_FUNCTION() throws IOException {
        sendCommand(PhotonfyCodes.RELOAD_DEFAULT_TRANSFER_FUNCTION.getHexCode());
    }

    public void GET_WAVELENGTH_CALIBRATION() throws IOException {
        sendCommand(PhotonfyCodes.GET_WAVELENGTH_CALIBRATION.getHexCode());
    }


    public void GET_DEFAULT_WAVELENGTH_CALIBRATION() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEFAULT_WAVELENGTH_CALIBRATION.getHexCode());
    }

    public void RELOAD_DEFAULT_WAVELENGTH_CALIBRATION() throws IOException {
        sendCommand(PhotonfyCodes.RELOAD_DEFAULT_WAVELENGTH_CALIBRATION.getHexCode());
    }

    public void CALIBRATE_BACKGROUND() throws IOException {
        sendCommand(PhotonfyCodes.CALIBRATE_BACKGROUND.getHexCode());
    }

    public void GET_BACKGROUND_CALIBRATIONS() throws IOException {
        sendCommand(PhotonfyCodes.GET_BACKGROUND_CALIBRATIONS.getHexCode());
    }

    public void CALIBRATE_DEFAULT_BACKGROUND() throws IOException {
        sendCommand(PhotonfyCodes.CALIBRATE_DEFAULT_BACKGROUND.getHexCode());
    }

    public void GET_DEFAULT_BACKGROUND_CALIBRATIONS() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEFAULT_BACKGROUND_CALIBRATIONS.getHexCode());
    }

    public void RELOAD_DEFAULT_BACKGROUND_CALIBRATIONS() throws IOException {
        sendCommand(PhotonfyCodes.RELOAD_DEFAULT_BACKGROUND_CALIBRATIONS.getHexCode());
    }

    public void GET_LUX_CALIBRATION() throws IOException {
        sendCommand(PhotonfyCodes.GET_LUX_CALIBRATION.getHexCode());
    }

    public void GET_DEFAULT_LUX_CALIBRATION() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEFAULT_LUX_CALIBRATION.getHexCode());
    }

    public void RELOAD_DEFAULT_LUX_CALIBRATION() throws IOException {
        sendCommand(PhotonfyCodes.RELOAD_DEFAULT_LUX_CALIBRATION.getHexCode());
    }

    public void GET_VIDEO_SAMPLE_RATE() throws IOException {
        sendCommand(PhotonfyCodes.GET_VIDEO_SAMPLE_RATE.getHexCode());
    }

    public void GET_TIME() throws IOException {
        sendCommand(PhotonfyCodes.GET_TIME.getHexCode());
    }

    public void GET_DEVICE_STATE() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEVICE_STATE.getHexCode());
    }

    public void GET_DEVICE_INFO() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEVICE_INFO.getHexCode());
    }

    public void SET_SOFT_FACTORY_RESET() throws IOException {
        sendCommand(PhotonfyCodes.SET_SOFT_FACTORY_RESET.getHexCode());
    }

    public void SET_HARD_FACTORY_RESET() throws IOException {
        sendCommand(PhotonfyCodes.SET_HARD_FACTORY_RESET.getHexCode());
    }

    public void GET_BACKGROUND_COEFFICIENTS() throws IOException {
        sendCommand(PhotonfyCodes.GET_BACKGROUND_COEFFICIENTS.getHexCode());
    }

    public void GET_DEFAULT_BACKGROUND_COEFFICIENTS() throws IOException {
        sendCommand(PhotonfyCodes.GET_DEFAULT_BACKGROUND_COEFFICIENTS.getHexCode());
    }

    public void RELOAD_DEFAULT_BACKGROUND_COEFFICIENTS() throws IOException {
        sendCommand(PhotonfyCodes.RELOAD_DEFAULT_BACKGROUND_COEFFICIENTS.getHexCode());
    }

    public void END_INITIALIZATION() throws IOException {
        sendCommand(PhotonfyCodes.END_INITIALIZATION.getHexCode());
    }

    public void GET_FLICKERING() throws IOException {
        sendCommand(PhotonfyCodes.GET_FLICKERING.getHexCode());
    }


    private byte[] convertToPrimitiveArray(List<Byte> byteList) {
        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) result[i] = byteList.get(i);
        return result;
    }

    private List<Byte> toByteList(byte[] bytes) {
        List<Byte> list = new ArrayList<>(bytes.length);
        for (byte b : bytes) list.add(b);
        return list;
    }

    private List<Byte> convertShort(short value) {
        return toByteList(ByteBuffer.allocate(2).putShort(value).array());
    }

    private List<Byte> convertFloat(float value) {
        return toByteList(ByteBuffer.allocate(4).putFloat(value).array());
    }

    private void buildAndSend(String command, List<Byte> payload) throws IOException {
        if (payload.size() < 250) {
            // If the payload is small, send it as a single package
            DataPackage frame = new DataPackage();
            frame.function = PhotonfyCodes.valueOf(command).getHexCode();
            frame.length = payload.size();
            frame.payload = payload;
            frame.flags = 0; // No more packages to send
            frame.sequence = 0; // Start sequence at 0
            sendCommand(frame);
        } else {
            // If the payload is large, split it into multiple packages
            int offset = 0;
            int sequence = 0; // Initialize sequence counter

            while (offset < payload.size()) {
                // Calculate the size of the current chunk
                int chunkSize = Math.min(250, payload.size() - offset);

                // Create a new DataPackage for the current chunk
                DataPackage frame = new DataPackage();
                frame.function = PhotonfyCodes.valueOf(command).getHexCode();
                frame.length = chunkSize;
                frame.payload = payload.subList(offset, offset + chunkSize);

                // Set flags: 1 if more packages are coming, 0 if this is the last one
                frame.flags = (offset + chunkSize < payload.size()) ? 1 : 0;

                // Set the sequence number and increment it for the next package
                frame.sequence = (short) sequence;
                sequence++;

                // Send the current chunk
                sendCommand(frame);

                // Move the offset to the next chunk
                offset += chunkSize;
            }
        }

    }

    public void sendCommand(DataPackage frame) throws IOException {
        frame.token = (short) frame.function;
        outputStream.write(convertToPrimitiveArray(frame.toBytes()));
    }

    // Métodos SET con validaciones
    public void SET_INTEGRATION_TIME(short integrationTime) throws IOException {
        if (integrationTime < 5 || integrationTime > 7000) {
            throw new IllegalArgumentException("Integration time must be between 5 and 7000 ms");
        }
        buildAndSend("SET_INTEGRATION_TIME", convertShort(integrationTime));
    }

    public void SET_GAIN(byte activated) throws IOException {
        if (activated != 0 && activated != 1) {
            throw new IllegalArgumentException("Gain activation must be 0 (off) or 1 (on)");
        }
        buildAndSend("SET_GAIN", Collections.singletonList(activated));
    }
    public void SET_DEFAULT_WAVELENGTH_CALIBRATION(List<Float> coeficientes) throws IOException {
        if (coeficientes == null || coeficientes.size() != 6) {
            throw new IllegalArgumentException("Exactly 6 calibration coefficients required");
        }
        for (Float f : coeficientes) {
            if (f == null || f < -380.0f || f > 380.0f) {
                throw new IllegalArgumentException("Coefficients must be between -380 and 380");
            }
        }

        List<Byte> payload = new ArrayList<>();
        coeficientes.forEach(f -> payload.addAll(convertFloat(f)));
        buildAndSend("SET_DEFAULT_WAVELENGTH_CALIBRATION", payload);
    }
    public void SET_WAVELENGTH_CALIBRATION(List<Float> coeficientes) throws IOException {
        if (coeficientes == null || coeficientes.size() != 6) {
            throw new IllegalArgumentException("Exactly 6 calibration coefficients required");
        }
        for (Float f : coeficientes) {
            if (f == null || f < -380.0f || f > 380.0f) {
                throw new IllegalArgumentException("Coefficients must be between -380 and 380");
            }
        }

        List<Byte> payload = new ArrayList<>();
        coeficientes.forEach(f -> payload.addAll(convertFloat(f)));
        buildAndSend("SET_WAVELENGTH_CALIBRATION", payload);
    }

    public void SET_TRANSFER_FUNCTION(List<Float> coeficientes) throws IOException {
        if (coeficientes == null || coeficientes.size() != 81) {
            throw new IllegalArgumentException("Transfer function requires exactly 81 coefficients");
        }

        List<Byte> payload = new ArrayList<>();
        coeficientes.forEach(f -> payload.addAll(convertFloat(f)));
        buildAndSend("SET_TRANSFER_FUNCTION", payload);
    }

    public void SET_BACKGROUND_COEFFICIENTS(List<Float> coeficientes) throws IOException {
        if (coeficientes == null || coeficientes.size() != 42) { // 14 integ * 3 coeff
            throw new IllegalArgumentException("Background coefficients require exactly 42 values");
        }

        List<Byte> payload = new ArrayList<>();
        coeficientes.forEach(f -> payload.addAll(convertFloat(f)));
        buildAndSend("SET_BACKGROUND_COEFFICIENTS", payload);
    }

    public void SET_LUX_CALIBRATION(float coeficiente) throws IOException {
        if (Float.isNaN(coeficiente) || coeficiente <= 0) {
            throw new IllegalArgumentException("Invalid lux calibration value");
        }
        buildAndSend("SET_LUX_CALIBRATION", convertFloat(coeficiente));
    }

    public void SET_VIDEO_SAMPLE_RATE(float rate) throws IOException {
        if (rate <= 0 || Float.isNaN(rate)) {
            throw new IllegalArgumentException("Sample rate must be a positive number");
        }
        buildAndSend("SET_VIDEO_SAMPLE_RATE", convertFloat(rate));
    }

    public void SET_TIME(String fecha, String hora) throws IOException {
        // Validación de fecha
        String[] fechaParts = fecha.split("/");
        if (fechaParts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Use dd/MM/yyyy");
        }

        int day = Integer.parseInt(fechaParts[0]);
        int month = Integer.parseInt(fechaParts[1]);
        int year = Integer.parseInt(fechaParts[2]);
        if (year < 2000 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 2000-9999");
        }

        // Validación de hora
        String[] timeParts = hora.split(":");
        if (timeParts.length != 3) {
            throw new IllegalArgumentException("Invalid time format. Use HH:mm:ss");
        }

        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);
        int seconds = Integer.parseInt(timeParts[2]);

        // Validación de rangos
        if (day < 1 || day > 31) throw new IllegalArgumentException("Invalid day");
        if (month < 1 || month > 12) throw new IllegalArgumentException("Invalid month");
        if (hours < 0 || hours > 23) throw new IllegalArgumentException("Invalid hour");
        if (minutes < 0 || minutes > 59) throw new IllegalArgumentException("Invalid minutes");
        if (seconds < 0 || seconds > 59) throw new IllegalArgumentException("Invalid seconds");

        List<Byte> payload = Arrays.asList(
                (byte) hours,
                (byte) minutes,
                (byte) seconds,
                (byte) day,
                (byte) month,
                (byte) (year - 2000)
        );
        buildAndSend("SET_TIME", payload);
    }

    public void SET_BLUETOOTH_NAME(String name) throws IOException {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length > 24) {
            throw new IllegalArgumentException("Bluetooth name exceeds 24 bytes");
        }

        List<Byte> payload = new ArrayList<>();
        for (byte b : nameBytes) {
            payload.add(b);
        }
        payload.add((byte) 0); // Null terminator
        buildAndSend("SET_BLUETOOTH_NAME", payload);
    }


    public void SET_DEFAULT_TRANSFER_FUNCTION(List<Float> coeficientes) throws IOException {
        // Misma validación que SET_TRANSFER_FUNCTION
        if (coeficientes == null || coeficientes.size() != 81) {
            throw new IllegalArgumentException("Default transfer function requires exactly 81 coefficients");
        }

        // Validación adicional para valores por defecto (si aplica)
        for (Float f : coeficientes) {
            if (f == null || Float.isNaN(f)) {
                throw new IllegalArgumentException("Invalid coefficient value in transfer function");
            }
        }

        List<Byte> payload = new ArrayList<>();
        coeficientes.forEach(f -> payload.addAll(convertFloat(f)));
        buildAndSend("SET_DEFAULT_TRANSFER_FUNCTION", payload);
    }

    public void SET_DEFAULT_BACKGROUND_COEFFICIENTS(List<Float> coeficientes) throws IOException {
        // 14 integraciones × 3 coeficientes (a, b, c)
        if (coeficientes == null || coeficientes.size() != 42) {
            throw new IllegalArgumentException("Default background coefficients require exactly 42 values (14×3)");
        }

        // Validar rango de coeficientes
        for (Float f : coeficientes) {
            if (f == null || Float.isNaN(f) || Float.isInfinite(f)) {
                throw new IllegalArgumentException("Invalid coefficient value");
            }
        }

        List<Byte> payload = new ArrayList<>();
        coeficientes.forEach(f -> payload.addAll(convertFloat(f)));
        buildAndSend("SET_DEFAULT_BACKGROUND_COEFFICIENTS", payload);
    }

    public void SET_DEFAULT_LUX_CALIBRATION(float coeficiente) throws IOException {
        // Misma validación que SET_LUX_CALIBRATION
        if (Float.isNaN(coeficiente) || coeficiente <= 0) {
            throw new IllegalArgumentException("Invalid default lux calibration value");
        }
        buildAndSend("SET_DEFAULT_LUX_CALIBRATION", convertFloat(coeficiente));
    }

    public void SET_DEFAULT_BACKGROUND(short count, short integration, float temp) throws IOException {
        // Reutilizar validación de SET_BACKGROUND
        validateBackgroundParameters(count, integration, temp);

        List<Byte> payload = new ArrayList<>();
        payload.addAll(convertShort(count));
        payload.addAll(convertShort(integration));
        payload.addAll(convertFloat(temp));
        buildAndSend("SET_DEFAULT_BACKGROUND", payload);
    }

    private void validateBackgroundParameters(short count, short integration, float temp) {
        if (count < 0 || count > 4095) {
            throw new IllegalArgumentException("Count must be between 0-4095");
        }
        if (integration < 5 || integration > 7000) {
            throw new IllegalArgumentException("Invalid integration time");
        }
        if (temp < -50 || temp > 150) {
            throw new IllegalArgumentException("Temperature out of range (-50°C to 150°C)");
        }
    }

    // Método original SET_BACKGROUND actualizado para usar la validación común
    public void SET_BACKGROUND(short count, short integration, float temp) throws IOException {
        validateBackgroundParameters(count, integration, temp);

        List<Byte> payload = new ArrayList<>();
        payload.addAll(convertShort(count));
        payload.addAll(convertShort(integration));
        payload.addAll(convertFloat(temp));
        buildAndSend("SET_BACKGROUND", payload);
    }


}