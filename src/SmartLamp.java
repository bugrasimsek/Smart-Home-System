public class SmartLamp extends SmartDevice{
    private int kelvinValue;
    private int brightnessPercentage;

    public SmartLamp(String deviceName) {
        this.deviceName = deviceName;
        this.deviceType = "Smart Lamp";

        //default values will be used while creating the device, but if user gave any of the values,
        //they will be updated in the setter methods as soon as object is created
        this.kelvinValue = 4000;
        this.brightnessPercentage = 100;
    }

    /**
     Retrieves the SmartPlug object with the given device name string.
     @param name the name of the device object to retrieve
     @return the SmartLamp object with the given name
     @throws DeviceNotFoundException if no device with the given name found
     @throws ErroneousCommandException if command is somehow incorrect
     @throws DeviceTypeException if the retrieved device is not a SmartLamp
     */
    public static SmartLamp getDevice(String name) throws DeviceNotFoundException, ErroneousCommandException, DeviceTypeException {
        if (!doesExists(name))
            throw new DeviceNotFoundException();

        SmartDevice device = null;

        for (SmartDevice currentDevice : smartDevices)
            if (currentDevice.deviceName.equals(name)){
                device = currentDevice;
                break;
            }

        if (!(device instanceof SmartLamp))
            throw new DeviceTypeException("smart lamp");

        return (SmartLamp) device;
    }

    /**
     Checks if given kelvin or brightness string is a valid int
     @param valueStr the kelvin or brightness string that will be parsed to int
     @throws ErroneousCommandException if value cannot be parsed to int
     @return value - the parsed int value of brightness or kelvin (or Integer.MAX_VALUE if the value is not given)
     */
    public static int checkParseable(String valueStr) throws ErroneousCommandException{
        //the default value will be used only if both of values are not given
        if(valueStr.equals("NotGiven"))
            return Integer.MAX_VALUE;

        //Otherwise, tries to parse the kelvin value and brightness value to int
        int value;

        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            throw new ErroneousCommandException();
        }

        return value;
    }

    /**
     Checks if given kelvin and brightness integers are in the correct range
     @param mode - the kelvin or brightness values that will be checked
     @param value - the value that will be checked
     @throws IllegalArgumentException if value is not in expected range
     */
    public static void checkRange(String mode,int value) throws IllegalArgumentException {
        switch (mode) {
            case "Kelvin":
                if(value < 2000 || value > 6500)
                    throw new IllegalArgumentException("ERROR: Kelvin value must be in range of 2000K-6500K!");
                break;
            case "Brightness":
                if(value < 0 || value > 100)
                    throw new IllegalArgumentException("ERROR: Brightness must be in range of 0%-100%!");
                break;
            default:
                break;
        }
    }


    /**
     * Adds a new SmartLamp device to the ArrayList of smart devices.
     *
     * @param line The command line input for adding a new SmartLamp device.
     */
    public static void addSmartLamp(String line){
        String[] args = line.split("\t");

        String deviceName = "";
        String initialStatus = "Off";
        String kelvinStr = "NotGiven";
        String brightnessStr = "NotGiven";

        switch(args.length){
            case 3:
                deviceName = args[2];
                break;

            case 4:
                deviceName = args[2];
                initialStatus = args[3];
                break;

            case 6: //The case where kelvin value and brightness are given
                deviceName = args[2];
                initialStatus = args[3];
                kelvinStr = args[4];
                brightnessStr = args[5];
                break;

            default:
                write("ERROR: Erroneous command!");
                return;
        }

        SmartLamp lamp = new SmartLamp(deviceName);

        if(doesExists(deviceName)){
            write("ERROR: There is already a smart device with same name!");
            return;
        }

        try {
            lamp.setStatus(initialStatus);

            //if one of the values is not given while other is given, that's an error, there's no such case
            if(kelvinStr.equals("NotGiven") && !brightnessStr.equals("NotGiven"))
                throw new ErroneousCommandException();

            else if(!kelvinStr.equals("NotGiven") && brightnessStr.equals("NotGiven"))
                throw new ErroneousCommandException();

            //if both values given, check range and parseability
            //if both values are not given or any one of them is wrong, then the default values will be used for both
            if(!kelvinStr.equals("NotGiven") && !brightnessStr.equals("NotGiven")){
                int kelvin = checkParseable(kelvinStr);
                int brightness = checkParseable(brightnessStr);
                checkRange("Kelvin", kelvin);
                checkRange("Brightness",brightness);

                //if both values are valid, then set them
                lamp.setKelvinValue(kelvin);
                lamp.setBrightnessPercentage(brightness);
            }

        } catch (ErroneousCommandException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        addDevice(lamp);

    }


    /**
     * Sets the brightness percentage of the SmartLamp.
     * @param brightnessPercentage The brightness percentage of the SmartLamp.
     */
    public void setBrightnessPercentage(int brightnessPercentage) {
        this.brightnessPercentage = brightnessPercentage;
    }

    /**
     * Sets the kelvin value of the SmartLamp.
     * @param kelvinValue The kelvin value of the SmartLamp.
     */
    public void setKelvinValue(int kelvinValue) {
        this.kelvinValue = kelvinValue;
    }

    /**
     * Sets a new kelvin value for the SmartLamp from string
     * @param line The command line input for setting a new kelvin value.
     */
    public static void changeKelvin(String line){
        String deviceName;
        String kelvinStr;

        try {
            try {
                deviceName = line.split("\t")[1];
                kelvinStr = line.split("\t")[2];
            } catch (ArrayIndexOutOfBoundsException e){
                throw new ErroneousCommandException();
            }
            int kelvinValue = checkParseable(kelvinStr);
            checkRange("Kelvin", kelvinValue);
            SmartLamp device = getDevice(deviceName);
            device.setKelvinValue(kelvinValue);
        }catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }
    }

    /**
     * Sets a new brightness value for the SmartLamp from string
     * @param line The command line input for setting a new brightness value.
     */
    public static void changeBrightness(String line){
        String deviceName;
        String brightnessStr;

        try {
            try {
                deviceName = line.split("\t")[1];
                brightnessStr = line.split("\t")[2];
            } catch (ArrayIndexOutOfBoundsException e){
                throw new ErroneousCommandException();
            }
            SmartLamp device = getDevice(deviceName);
            int brightnessValue = checkParseable(brightnessStr);
            checkRange("Brightness", brightnessValue);
            device.setBrightnessPercentage(brightnessValue);
        }catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }
    }


    /**
     * Changes both kelvin value and brightness of the SmartLamp from string
     * When processing the command, first checks if there's data type inconvenience (e.g. brightness is given as a string)
     * Then checks if the ranges of kelvin and brightness are correct
     * If both ranges are incorrect, gives an error message for the first one
     * @param line The command line input for setting a new kelvin value and brightness value.
     */
    public static void changeWhite(String line) {
        String deviceName;
        String kelvinStr;
        String brightnessStr;

        try {
            try {
                deviceName = line.split("\t")[1];
                kelvinStr = line.split("\t")[2];
                brightnessStr = line.split("\t")[3];
            } catch (ArrayIndexOutOfBoundsException e){
                throw new ErroneousCommandException();
            }
            int kelvinValue = checkParseable(kelvinStr);
            int brightnessValue = checkParseable(brightnessStr);
            checkRange("Kelvin", kelvinValue);
            checkRange("Brightness", brightnessValue);
            SmartLamp device = getDevice(deviceName);
            device.setKelvinValue(kelvinValue);
            device.setBrightnessPercentage(brightnessValue);
        }catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }
    }

    /**
     @return a string representation of the SmartLamp object
     */
    @Override
    public String toString() {
        return deviceType + " " +
                deviceName + " is " +
                getStatus() +
                " and its kelvin value is " + kelvinValue + "K with " + brightnessPercentage +"% brightness" +
                ", and its time to switch its status is " + switchTime + ".";
    }

    public int getKelvinValue() {
        return kelvinValue;
    }

    public int getBrightnessPercentage() {
        return brightnessPercentage;
    }
}//end of SmartLamp class
