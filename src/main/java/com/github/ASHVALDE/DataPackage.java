package com.github.ASHVALDE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.github.ASHVALDE.PhotonfyCodes.GET_RAW_SPECTRUM;

public class DataPackage {
    private static final int HEADER_SIZE = 8;
    int[] syncWord = {85, 2};
    public int flags, token, function, length;
    public PhotonfyCodes name;
    public String response;
    public short sequence;
    public List<Byte> payload;


    public DataPackage fromBytes(List<Byte> data) throws IOException {
        if (data == null || data.size() < HEADER_SIZE) {
            throw new IOException("Datos inválidos o incompletos");
        }
        syncWord = new int[]{byteToUnsigned(data.get(0)), byteToUnsigned(data.get(1))};
        flags = byteToUnsigned(data.get(2));
        token = byteToUnsigned(data.get(3));
        function = byteToUnsigned(data.get(4));
        sequence = (short) ((data.get(5) << 8) | byteToUnsigned(data.get(6)));
        length = byteToUnsigned(data.get(7));
        name = PhotonfyCodes.fromInt(function);
        if (data.size() < HEADER_SIZE + length) {
            throw new IOException("Longitud del payload inconsistente");
        }
        payload = data.subList(HEADER_SIZE, data.size());
        return this;
    }

    public List<Byte> toBytes() {
        List<Byte> bytes = new ArrayList<>(HEADER_SIZE + length);
        bytes.add((byte) syncWord[0]);
        bytes.add((byte) syncWord[1]);
        bytes.add((byte) flags);
        bytes.add((byte) token);
        bytes.add((byte) function);
        bytes.add((byte) (sequence >> 8));
        bytes.add((byte) sequence);
        bytes.add((byte) length);
        if (length > 0 && payload != null) {
            bytes.addAll(payload.subList(0, Math.min(length, payload.size())));
        }
        return bytes;
    }

    private static int byteToUnsigned(Byte b) {
        return b & 0xFF;
    }
    public String getResponse(){
        return getResponse(false);
    }
    public String getResponse(boolean SimpleResponse) {
        PhotonfyCodes function = PhotonfyCodes.fromInt(this.token);

        if(this.token==5 ){
            String ResponsePhoto = "";
            int startCapture = payload.get(0)& 0xFF;
            int hour = payload.get(1)& 0xFF;
            int minute = payload.get(2)& 0xFF;
            int seconds = payload.get(3)& 0xFF;
            int day = payload.get(4)& 0xFF;
            int month = payload.get(5)& 0xFF;
            int year = payload.get(6)& 0xFF;
            short integration_time = (short) ((payload.get(7) << 8) | (payload.get(8) & 0xFF));
            byte[] x = {payload.get(9),payload.get(10),payload.get(11),payload.get(12)};
            float temperature =  ByteBuffer.wrap(x).getFloat();
            ResponsePhoto = String.format("{\"ResponsePhoto\":%s,\"startCapture\":%s,\"hour\":%s,\"minute\":%s,\"seconds\":%s,\"day\":%s,\"month\":%s,\"year\":%s,\"integration_time\":%s,\"temperature\":%s}\n",
                    ResponsePhoto,startCapture,hour,minute,seconds,day,month,year,integration_time,temperature);
            List<Byte> datosEspectrografia = payload.subList(13,payload.size());

            List<Integer> datosCompilados = new ArrayList<>();

            if(datosEspectrografia.size()==512) {
                for (int j = 0; j < datosEspectrografia.size() / 2; j++) {
                    int num1 = j * 2;
                    int num2 = j * 2 + 1;
                    // Combine the two bytes into an unsigned short
                    int unsignedShort = ((datosEspectrografia.get(num1) & 0xFF) << 8) | (datosEspectrografia.get(num2) & 0xFF);
                    datosCompilados.add(unsignedShort);
                    ResponsePhoto = ResponsePhoto+unsignedShort+",";
                }
            }

            return ResponsePhoto;
        }else{
            switch (function) {

                case EV_POWER_OFF:
                    return "Dispositivo apagandose";
                case GET_TRANSFER_FUNCTION:
                case GET_DEFAULT_TRANSFER_FUNCTION:
                    String TransferCalibration ="";
                    for (int i = 0; i < payload.size(); i=i+4) {
                        byte[] WaveLenghtCoef = {payload.get(i),payload.get(i+1),payload.get(i+2),payload.get(i+3)};
                        TransferCalibration = TransferCalibration+ByteBuffer.wrap(WaveLenghtCoef).getFloat()+", ";
                    }
                    TransferCalibration = TransferCalibration.substring(0, TransferCalibration.length() - 2);
                    return TransferCalibration;
                case GET_DEVICE_STATE:
                    int Bateria = payload.get(0)& 0xFF;
                    byte[] temperatureBytes = {payload.get(1),payload.get(2),payload.get(3),payload.get(4)};
                    float temperature =  ByteBuffer.wrap(temperatureBytes).getFloat();
                    return "Bateria: "+Bateria+"\nTemperatura:"+ temperature+" °C";
                case GET_GAIN:
                    int GAIN = payload.get(0)& 0xFF;
                    if(SimpleResponse){
                        return String.valueOf(GAIN);
                    }
                    if(GAIN==1){
                        return "Gain Enabled";
                    }else{
                        return "Gain Disabled";
                    }

                case GET_INTEGRATION_TIME:
                    byte[] integrationTime = {payload.get(0),payload.get(1)};
                    float Integration =  ByteBuffer.wrap(integrationTime).getShort();
                    if(SimpleResponse){
                        return String.valueOf(Integration);

                    }else{
                        return "Integration time: "+Integration+" ms";
                    }
                case GET_DEFAULT_LUX_CALIBRATION:
                case GET_LUX_CALIBRATION:
                    byte[] x = {payload.get(0),payload.get(1),payload.get(2),payload.get(3)};
                    float Lux =  ByteBuffer.wrap(x).getFloat();
                    if(SimpleResponse){
                        return String.valueOf(Lux);
                    }
                    return "Lux: "+Lux;
                case GET_VIDEO_SAMPLE_RATE:
                    float SampleRate= ByteBuffer.wrap(new byte[]{payload.get(0), payload.get(1), payload.get(2), payload.get(3)}).getFloat();
                    if(SimpleResponse){
                        return String.valueOf(SampleRate);
                    }
                    return "SampleRate: "+SampleRate;
                case GET_DEFAULT_WAVELENGTH_CALIBRATION:
                case GET_WAVELENGTH_CALIBRATION:
                    String WavelenghtCalibration ="";
                    for (int i = 0; i < payload.size(); i=i+4) {
                        byte[] WaveLenghtCoef = {payload.get(i),payload.get(i+1),payload.get(i+2),payload.get(i+3)};
                        WavelenghtCalibration = WavelenghtCalibration + ByteBuffer.wrap(WaveLenghtCoef).getFloat()+",";
                    }
                    return WavelenghtCalibration;
                case GET_DEFAULT_BACKGROUND_CALIBRATIONS:
                case GET_BACKGROUND_CALIBRATIONS:

                    String response = "";
                    short data =  ByteBuffer.wrap(new byte[]{payload.get(0), payload.get(1)}).getShort();
                    response = response+"data: "+data+"\n";
                    short integrationTimeBG =  ByteBuffer.wrap(new byte[]{payload.get(2), payload.get(3)}).getShort();
                    response = response+"integrationTime: "+integrationTimeBG+"\n";
                    float Temperature =  ByteBuffer.wrap(new byte[]{payload.get(4), payload.get(5), payload.get(6), payload.get(7)}).getFloat();
                    response = response+"Temperature: "+Temperature+"\n";
                    if(SimpleResponse){
                        return data+","+integrationTimeBG+","+Temperature;
                    }
                    return response;

                case GET_TIME:
                    int Hora = payload.get(0)& 0xFF;
                    int Minuto = payload.get(1)& 0xFF;
                    int Segundo = payload.get(2)& 0xFF;
                    int Dia = payload.get(3)& 0xFF;
                    int Mes = payload.get(4)& 0xFF;
                    int Anyo = payload.get(5)& 0xFF;
                    if(SimpleResponse){
                        return Hora+","+Minuto+","+Segundo+","+Dia+","+Mes+","+Anyo;
                    }
                    return Hora+":"+Minuto+":"+Segundo+"|| Fecha -> Dia:"+Dia+" Mes:"+Mes+" Año:"+Anyo;
                case GET_DEFAULT_BACKGROUND_COEFFICIENTS:
                case GET_BACKGROUND_COEFFICIENTS:
                    String responseBGCoef = "";
                    for (int i = 0; i < payload.size(); i=i+4) {
                        byte[] BackGroundCoef = {payload.get(i),payload.get(i+1),payload.get(i+2),payload.get(i+3)};
                        responseBGCoef =  responseBGCoef + ByteBuffer.wrap(BackGroundCoef).getFloat()+" ,";
                    }
                    return responseBGCoef;
                case GET_FLICKERING:
                    String Flickering = "";
                    for (int i = 0; i < payload.size(); i=i+4) {
                        byte[] FlickeringData = {payload.get(i),payload.get(i+1),payload.get(i+2),payload.get(i+3)};
                        Flickering = Flickering + (ByteBuffer.wrap(FlickeringData).getInt())+",\n";
                    }
                    return Flickering;
                case GET_DEVICE_INFO:
                    System.out.println(payload.get(0));
                    return "This Function has not been Implemented yet";
                default:
                    return this.name.toString();
            }
        }

    }
}