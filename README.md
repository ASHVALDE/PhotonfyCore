# Photonfy Java Reimplementation

## Descripción
Debido a la caída de los servidores de Photonfy, se ha realizado una reimplementación de su funcionalidad en una librería de Java. Esta nueva versión permite analizar y gestionar espectros de luz sin depender de la infraestructura original de Photonfy, asegurando una mayor disponibilidad y flexibilidad.


## Uso
Ejemplo de uso en código:
```java
StreamConnection conn = (StreamConnection) Connector.open("btspp://"+DeviceMAC+":5");
Photonfy photonfy =  new Photonfy(conn.openInputStream(),conn.openOutputStream());
photonfy.addListener(new Photonfy.DataListener() {
  @Override
  public void onPackageReceived(DataPackage dataPackage) throws IOException {
    //Eventos Cuando Se Recibe Un Paquete
  }
});
// Envia comandos
photonfy.sender.SET_BLUETOOTH_NAME("HolaMundo");
```

