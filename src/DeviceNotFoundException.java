public class DeviceNotFoundException extends Exception {
    public DeviceNotFoundException() {
        super("ERROR: There is not such a device!");
    }
}