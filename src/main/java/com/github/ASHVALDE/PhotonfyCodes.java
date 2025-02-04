package com.github.ASHVALDE;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PhotonfyCodes {
    UNKNOWN(0x0),
    SET_INTEGRATION_TIME(0x01),
    GET_INTEGRATION_TIME(0x02),
    SET_GAIN(0x03),
    GET_GAIN(0x04),
    GET_RAW_SPECTRUM(0x05),
    START_STREAMING(0x06),
    STOP_STREAMING(0x07),
    SET_TRANSFER_FUNCTION(0x08),
    GET_TRANSFER_FUNCTION(0x09),
    SET_DEFAULT_TRANSFER_FUNCTION(0x0A),
    GET_DEFAULT_TRANSFER_FUNCTION(0x0B),
    RELOAD_DEFAULT_TRANSFER_FUNCTION(0x0C),
    SET_WAVELENGTH_CALIBRATION (0x0D),
    GET_WAVELENGTH_CALIBRATION(0x0E),
    SET_DEFAULT_WAVELENGTH_CALIBRATION(0x0F),
    GET_DEFAULT_WAVELENGTH_CALIBRATION(0x10),
    RELOAD_DEFAULT_WAVELENGTH_CALIBRATION(0x11),
    CALIBRATE_BACKGROUND (0x12),
    GET_BACKGROUND_CALIBRATIONS (0x13),
    CALIBRATE_DEFAULT_BACKGROUND(0x14),
    GET_DEFAULT_BACKGROUND_CALIBRATIONS(0x15),
    RELOAD_DEFAULT_BACKGROUND_CALIBRATIONS(0x16),
    NOP(0x17),
    SET_LUX_CALIBRATION(0x18),
    GET_LUX_CALIBRATION(0x19),
    SET_DEFAULT_LUX_CALIBRATION(0x1A),
    GET_DEFAULT_LUX_CALIBRATION(0x1B),
    RELOAD_DEFAULT_LUX_CALIBRATION(0x1C),
    SET_VIDEO_SAMPLE_RATE(0x1D),
    GET_VIDEO_SAMPLE_RATE(0x1E),
    SET_TIME(0x1F),
    GET_TIME(0x20),
    GET_DEVICE_STATE(0x21),
    SET_DEVICE_INFO(0x22),
    GET_DEVICE_INFO(0x23),
    SET_FIRMWARE_UPDATE(0x24),
    SET_DEFAULT_FIRMWARE_UPDATE(0x25),
    SET_FIRMWARE_UPDATE_METADATA(0x26),
    SET_SOFT_FACTORY_RESET (0x27),
    SET_HARD_FACTORY_RESET (0x28),
    SET_BACKGROUND(0x29),
    SET_DEFAULT_BACKGROUND(0x2A),
    SET_BACKGROUND_COEFFICIENTS(0x2B),
    GET_BACKGROUND_COEFFICIENTS(0x2C),
    SET_DEFAULT_BACKGROUND_COEFFICIENTS(0x2D),
    GET_DEFAULT_BACKGROUND_COEFFICIENTS(0x2E),
    RELOAD_DEFAULT_BACKGROUND_COEFFICIENTS(0x2F),
    END_INITIALIZATION(0x30),
    NEXT_SYNC_SPECTRA(0x31),
    GET_FLICKERING(0x32),
    SET_BLUETOOTH_NAME(0x33),
    RSP_ACK(0xA0),
    RSP_COMPLETED(0xA1),
    RSP_RETURN(0xA2),
    RSP_NO_MORE_DATA(0xA3),
    RSP_BUSY(0xA6),
    RSP_ERROR(0xA7),
    EV_FOTO(0xE1),
    EV_STREAMING(0xE2),
    EV_POWER_OFF (0xE4),
    EV_ENTERING_SLEEP_MODE (0xE5),
    EV_SPECTROMETER_STATE(0xE6);

    private final int hexCode;
    private static final Map<Integer, PhotonfyCodes> hexCodeToCommand = Arrays.stream(values())
            .collect(Collectors.toMap(cmd -> cmd.hexCode, cmd -> cmd));

    PhotonfyCodes(int hexCode) {
        this.hexCode = hexCode;  // Corregido: asignación correcta del parámetro
    }

    public int getHexCode() {
        return hexCode;
    }

    public static String getNameFromHex(int hexValue) {
        PhotonfyCodes command = hexCodeToCommand.get(hexValue);
        if (command == null) {
            throw new IllegalArgumentException("Código hexadecimal inválido: 0x" +
                    Integer.toHexString(hexValue).toUpperCase());
        }
        return command.name();
    }

    // Versión alternativa que devuelve Optional para mejor manejo de nulos
    public static String getNameFromHexSafe(int hexValue) {
        return hexCodeToCommand.getOrDefault(hexValue, UNKNOWN).name();
    }


    public static PhotonfyCodes fromInt(int i) {
        for (PhotonfyCodes enumValue : PhotonfyCodes.values()) {
            if (enumValue.getHexCode() == i) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}