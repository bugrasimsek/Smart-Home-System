import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A custom comparator that is used to sort the list of SmartDevices based on their switch times in ascending order and respects the relative order of them if their switch times are equal. <br><br>
 * If both SmartDevices have a null switch time, they are considered equal and their relative order is preserved. <br><br>
 * If the switch time of one SmartDevice is null while the other one is not, the one with the non-null switch time is considered greater than the one with the null switch time. <br><br>
 * If neither SmartDevice has a null switch time, they are compared based on their switch times using the built-in compareTo method of the LocalDateTime class
 */
class StableSortComparator implements Comparator<SmartDevice> {
    /**
     * Compares two SmartDevices based on their switch times in ascending order and respects the relative order of them if their switch times are equal.
     *
     * @param d1 the first object to be compared.
     * @param d2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    public int compare(SmartDevice d1, SmartDevice d2) {
        if (d1.switchTime == null && d2.switchTime == null) {
            return 0;
        } else if (d1.switchTime == null) {
            return 1;
        } else if (d2.switchTime == null) {
            return -1;
        } else
            return d1.getSwitchTime().compareTo(d2.getSwitchTime());
    }
}


public abstract class SmartDevice {
    protected static ArrayList<SmartDevice> smartDevices = new ArrayList<>();
    public String deviceName;
    public String deviceType;
    protected Time switchTime = null; // devices has no switch time by default
    protected boolean isOn = false;

    /* Methods that can be called by user */

    /**
     * Adds a new smart device to the system if it does not exist already
     *
     * @param smartDevice the device containing the device name and type
     */
    public static void addDevice(SmartDevice smartDevice) {
        if (!doesExists(smartDevice.deviceName)) {
            smartDevices.add(smartDevice);
            sortDevices();
        } else
            write("ERROR: There is already a smart device with same name!");

    }

    /**
     * Deletes the smart device with the given name from the system.
     *
     * @param line the command line containing the device name
     */
    public static void removeDevice(String line) {
        String deviceName;
        SmartDevice removedDevice;

        try {
            deviceName = line.split("\t")[1];
            /** "it has to switch the device off before removing it" */
            removedDevice = getDevice(deviceName);
            removedDevice.isOn = false;
            /** can be Used predicate filter, report **/
            smartDevices.removeIf(smartDevice -> smartDevice.deviceName.equals(deviceName));
        } catch (NullPointerException e) {
            write("ERROR: Device is null!");
            return;
        } catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException e) {
            write(e.getMessage());
            return;
        }

        if (removedDevice instanceof SmartPlug)
            ((SmartPlug) removedDevice).calculateConsumption();
        else if (removedDevice instanceof SmartCamera)
            ((SmartCamera) removedDevice).calculateUsage();

        write("SUCCESS: Information about removed smart device is as follows:");
        write(removedDevice.toString());

    }

    /**
     * Switches the device on or off at a given time by setting switchTime, its command from user is "setSwitchTame"
     *
     * @param line
     */
    public static void switchLater(String line) {
        //Time object from custom Time class with given time string
        Time switchTime;
        String deviceName;
        String switchTimeStr;
        SmartDevice smartDevice;

        try {
            deviceName = line.split("\t")[1];
            switchTimeStr = line.split("\t")[2];

            switchTime = Time.createTimeObject(switchTimeStr);
            smartDevice = getDevice(deviceName);

            if (smartDevice == null)
                throw new DeviceNotFoundException();
        } catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException |
                 IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        // check if time is before current time
        if (switchTime.getDateTime().isBefore(Time.getCurrentTime().getDateTime()))
            write("ERROR: Switch time cannot be in the past!");

        else
            smartDevice.switchTime = switchTime;

        sortDevices();
        switchDevices();
    }

    /**
     * Switches the device on or off immediately, its command from user is "switch"
     *
     * @param line the command line containing the device name and status
     */
    public static void switchNow(String line) {
        String deviceName;
        String status;
        SmartDevice smartDevice;

        try {
            deviceName = line.split("\t")[1];
            status = line.split("\t")[2];
            smartDevice = getDevice(deviceName);

            if (smartDevice == null)
                throw new DeviceNotFoundException();

            // check if device is already switched on or off
            if (status.equalsIgnoreCase(smartDevice.getStatus()))
                throw new IllegalArgumentException("ERROR: This device is already switched " + smartDevice.getStatus() + "!");

            smartDevice.setStatus(status);
        } catch (ArrayIndexOutOfBoundsException | ErroneousCommandException e) {
            write("ERROR: Erroneous command!");
            return;
        } catch (IllegalArgumentException | DeviceTypeException | DeviceNotFoundException e) {
            write(e.getMessage());
            return;
        }

        if (smartDevice instanceof SmartPlug) {
            SmartPlug plug = (SmartPlug) smartDevice;

            //if plug is switched on while something is plugged in, start timer
            if (plug.isSomethingPlugged() && plug.isOn)
                plug.calculationStartTime.setDateTime(Time.getCurrentTime().getDateTime());

                //if plug is switched off while something is plugged in calculate consumption
            else if (plug.isSomethingPlugged() && !plug.isOn) {
                plug.calculateConsumption();
                plug.calculationStartTime.setDateTime(null);
            }
        } else if (smartDevice instanceof SmartCamera) {
            SmartCamera camera = (SmartCamera) smartDevice;

            //if camera is switched on, start timer
            if (camera.isOn)
                camera.calculationStartTime.setDateTime(Time.getCurrentTime().getDateTime());

                //if camera is switched off, calculate storage usage
            else if (!camera.isOn) {
                camera.calculateUsage();
                camera.calculationStartTime.setDateTime(null);
            }
        }

        smartDevice.switchTime = null;
        sortDevices();
    }

    /**
     * Switches the time to the next switch time of a smart device.
     */
    public static void nop() {
        // If arraylist is empty, there is nothing to switch
        if (smartDevices.isEmpty()) {
            write("ERROR: There is nothing to switch!");
            return;
        }

        /*If there is at least one device that has a switch time, it has to be in the first index of the arraylist
        So, if not even the first device has a switch time, it also means there is  nothing to switch */
        if (smartDevices.get(0).switchTime == null) {
            write("ERROR: There is nothing to switch!");
            return;
        }

        /*Otherwise, we can switch the time to first device's switch time since
        arraylist is sorted with respect to switch time in ascending order*/
        else
            Time.setCurrentTime(smartDevices.get(0).switchTime);

    }

    /**
     * Displays the Z-Report, including the current time and various information about all smart devices.
     */
    public static void displayZReport() {
        write("Time is:\t" + Time.getCurrentTime().toString());
        for (SmartDevice smartDevice : smartDevices)
            write(smartDevice.toString());
    }

    /**
     * Changes the name of a smart device if it exists and new name is not taken and both of the names are not the same
     *
     * @param line the command line containing the device name
     */
    public static void changeName(String line) {
        String deviceName;
        String newDeviceName;
        SmartDevice smartDevice;

        try {
            deviceName = line.split("\t")[1];
            newDeviceName = line.split("\t")[2];

            // check if both of the names are the same
            if (deviceName.equals(newDeviceName))
                throw new IllegalArgumentException("ERROR: Both of the names are the same, nothing changed!");

            smartDevice = getDevice(deviceName);

            // check if new device name is already taken
            if (doesExists(newDeviceName))
                throw new IllegalArgumentException("ERROR: There is already a smart device with same name!");
        } catch (ArrayIndexOutOfBoundsException | DeviceNotFoundException | ErroneousCommandException |
                 DeviceTypeException e) {
            write("ERROR: Erroneous command!");
            return;
        } catch (NullPointerException e) {
            write("ERROR: Device is null!");
            return;
        } catch (IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        //update the device name
        smartDevice.deviceName = newDeviceName;
    }

    /* Non-Public Methods that are used among SmartDevice class family */

    /**
     * @param deviceName name of the device
     * @return true if there is a device with the given name, false otherwise
     */
    protected static boolean doesExists(String deviceName) {
        return smartDevices.stream().anyMatch(device -> device.deviceName.equals(deviceName));
    }

    /**
     * Iterates through the list of devices and switches their status if their switch time is reached.
     */
    protected static void switchDevices() {
        for (SmartDevice smartDevice : smartDevices) {
            //if a null switch time is encountered, the rest of the devices are also null
            if (smartDevice.switchTime == null)
                break;

            if (smartDevice.switchTime.getDateTime().isBefore(Time.getCurrentTime().getDateTime()) || smartDevice.switchTime.getDateTime().isEqual(Time.getCurrentTime().getDateTime())) {
                smartDevice.isOn = !smartDevice.isOn; //reverse the status of the device, i.e., switch it
                smartDevice.switchTime = null;

                if (smartDevice instanceof SmartPlug) {
                    SmartPlug plug = (SmartPlug) smartDevice;

                    //if plug is switched on while something is plugged in, start timer
                    if (plug.isSomethingPlugged() && smartDevice.isOn)
                        plug.calculationStartTime.setDateTime(Time.getCurrentTime().getDateTime());

                        //if plug is switched off while something is plugged in calculate consumption
                    else if (plug.isSomethingPlugged() && !smartDevice.isOn) {
                        plug.calculateConsumption();
                        plug.calculationStartTime.setDateTime(null);
                    }
                }
            }
        }
        sortDevices();
    }

    /**
     * @return The switch time of the first device in the list.
     */
    protected static Time getFirstSwitchTime() {
        if (smartDevices.size() == 0)
            return null;

        return smartDevices.get(0).switchTime;
    }

    /**
     * Sorts the arraylist of smart devices with respect to their switch times in ascending order
     */
    protected static void sortDevices() {
        smartDevices.sort(new StableSortComparator());
    }

    /**
     * Returns the SmartDevice object with the given name if it exists in the smartDevices list.
     *
     * @param deviceName the name of the device to search for
     * @return the SmartDevice object with the given name
     * @throws DeviceNotFoundException   if the device with the given name does not exist
     * @throws ErroneousCommandException if the device name is null or empty
     * @throws DeviceTypeException       if the device with the given name is not in expected type
     */
    protected static SmartDevice getDevice(String deviceName) throws DeviceNotFoundException, ErroneousCommandException, DeviceTypeException {
        if (!doesExists(deviceName))
            throw new DeviceNotFoundException();

        for (SmartDevice smartDevice : smartDevices) {
            if (smartDevice.deviceName.equals(deviceName))
                return smartDevice;
        }

        return null;
    }

    /**
     * @return the switch time of the device as a LocalDateTime object
     */
    protected LocalDateTime getSwitchTime() {
        return switchTime.getDateTime();
    }

    /**
     * Sets the status of the device based on the given status string.<br><br>
     * If the string is "On" or "on", the status of the device is set to true. <br><br>
     * If the string is "Off" or "off", the status of the device is set to false.
     *
     * @param status a string representing the desired status of the device.
     * @throws ErroneousCommandException if the given status string is neither "On" / "on" nor "Off" / "off".
     */
    protected void setStatus(String status) throws ErroneousCommandException {
        if (status.equalsIgnoreCase("on"))
            isOn = true;

        else if (status.equalsIgnoreCase("off"))
            isOn = false;

        else
            throw new ErroneousCommandException();
    }

    /**
     * @return the status of the device as a string. If the device is on, it returns "on". If the device is off, it returns "off".
     */
    protected String getStatus() {
        if (isOn)
            return "on";

        else
            return "off";
    }

    /**
     * Writes the given content to the output file. (appends to file, adds a new line)
     *
     * @param content the content to be written to the output file
     */
    protected static void write(String content) {
        FileOutput.write(content);
    }

}// end of SmartDevice class
