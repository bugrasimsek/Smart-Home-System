public class DeviceTypeException extends Exception{

    public DeviceTypeException(String deviceType) {
        super("ERROR: This device is not a " + deviceType + "!");
    }
}
