public class SmartColorLamp extends SmartLamp{
    /**only holds a hexadecimal value as string, kelvin value is kept in kelvinValue variable of super class SmartLamp*/
    private String colorCode;
    private boolean inColorMode;

    public SmartColorLamp(String deviceName) {
        super(deviceName);
        this.deviceType = "Smart Color Lamp";

        //default values are same as the SmartLamp's default values, so colorMode is off as default
        colorCode = "Not Specified";
        inColorMode = false;
    }

    /**
     Retrieves the SmartPlug object with the given device name string.
     @param name the name of the device object to retrieve
     @return the SmartColorLamp object with the given name
     @throws DeviceNotFoundException if no device with the given name found
     @throws ErroneousCommandException if command is somehow incorrect
     @throws DeviceTypeException if the retrieved device is not a SmartColorLamp
     */
    public static SmartColorLamp getDevice(String name) throws DeviceNotFoundException, ErroneousCommandException, DeviceTypeException {
        if (!doesExists(name))
            throw new DeviceNotFoundException();

        SmartDevice device = null;

        for (SmartDevice currentDevice : smartDevices)
            if (currentDevice.deviceName.equals(name)){
                device = currentDevice;
                break;
            }

        if (!(device instanceof SmartColorLamp))
            throw new DeviceTypeException("smart color lamp");

        return (SmartColorLamp) device;
    }

    /**
     * Adds a new SmartColorLamp device to the ArrayList of smart devices.
     *
     * @param line The command line input for adding a new SmartColorLamp device.
     */
    public static void addSmartColorLamp(String line){
        String[] args = line.split("\t");

        String deviceName = "";
        String initialStatus = "Off";
        String brightnessStr = "NotGiven";
        String colorStr = "NotGiven";

        switch(args.length){
            case 3:
                deviceName = args[2];
                break;

            case 4:
                deviceName = args[2];
                initialStatus = args[3];
                break;

            case 6:
                deviceName = args[2];
                initialStatus = args[3];
                colorStr = args[4];
                brightnessStr = args[5];
                break;

            default:
                write("ERROR: Erroneous command!");
                return;
        }

        SmartColorLamp colorLamp = new SmartColorLamp(deviceName);

        if(doesExists(deviceName)){
            write("ERROR: There is already a smart device with same name!");
            return;
        }


        //decide if the color is in color mode or kelvin mode
        colorLamp.setColorMode(isInColorMode(colorStr));

        int colorCode = 0;
        int kelvin = 4000;

        try {
            colorLamp.setStatus(initialStatus);

            if(!brightnessStr.equals("NotGiven") && !colorStr.equals("NotGiven")){
                if (colorLamp.inColorMode)
                    colorCode = checkParseable(colorStr, true);
                else
                    kelvin = checkParseable(colorStr, false);
                int brightness = checkParseable(brightnessStr, false);

                if(colorLamp.inColorMode)
                    checkRange("Color Code", colorCode);
                else
                    checkRange("Kelvin", kelvin);
                checkRange("Brightness", brightness);

                if(colorLamp.inColorMode)
                    colorLamp.setColorCode(colorStr);
                else
                    colorLamp.setKelvinValue(kelvin);
                colorLamp.setBrightnessPercentage(brightness);
            }

        } catch (ErroneousCommandException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        addDevice(colorLamp);

    }

    /**
     * Sets the color code of the device.
     * @param colorCode the new color code of the device.
     */
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
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
            case "Color Code":
                if(value < 0 || value > Integer.parseInt("FFFFFF", 16))
                    throw new IllegalArgumentException("ERROR: Color code value must be in range of 0x0-0xFFFFFF!");
                break;
            default:
                break;
        }
    }

    /**
     Checks if given kelvin or brightness string is a valid int
     @param valueStr the kelvin or brightness string that will be parsed to int
     @throws ErroneousCommandException if value cannot be parsed to int
     @return value - the parsed int value of brightness or kelvin (or Integer.MAX_VALUE if the value is not given)
     */
    public static int checkParseable(String valueStr, boolean isColorMode) throws ErroneousCommandException{
        //the default value will be used only if both of values are not given
        if(valueStr.equals("NotGiven"))
            return Integer.MAX_VALUE;

        //Otherwise, tries to parse the kelvin value and brightness value to int
        int value;

        try {
            if(isColorMode)
                value = Integer.parseInt(valueStr.substring(2), 16);
            else
                value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            throw new ErroneousCommandException();
        }
        return value;
    }

    /**
     * Changes the color code of the device
     * @param line The command line input for changing the color code of the device.
     */
    public static void changeColorCode(String line){
        String deviceName;
        String colorCodeStr;
        SmartColorLamp device;

        try {
            try {
                deviceName = line.split("\t")[1];
                colorCodeStr = line.split("\t")[2];
            } catch (ArrayIndexOutOfBoundsException e){
                throw new ErroneousCommandException();
            }
            device = getDevice(deviceName);
            int colorCodeValue = checkParseable(colorCodeStr);
            checkRange("Color Code", colorCodeValue);
            device.setColorCode(colorCodeStr);
        }catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        device.inColorMode = true;
    }

    /**
     * Changes both color code and brightness<br>
     * When processing the command, first checks if there's data type inconvenience (e.g. brightness is given as a string) <br>
     * Then checks if the ranges of color code and brightness are correct <br>
     * If both ranges are incorrect, gives an error message for the first one <br>
     * @param line  the command line that will be processed
     */
    public static void changeColor(String line) {
        String deviceName;
        String colorCodeStr;
        String brightnessStr;
        SmartColorLamp device;

        try {
            try {
                deviceName = line.split("\t")[1];
                colorCodeStr = line.split("\t")[2];
                brightnessStr = line.split("\t")[3];
            } catch (ArrayIndexOutOfBoundsException e){
                throw new ErroneousCommandException();
            }
            device = getDevice(deviceName);
            int colorValue = checkParseable(colorCodeStr, true);
            int brightnessValue = checkParseable(brightnessStr, false);
            checkRange("Color Mode", colorValue);
            checkRange("Brightness", brightnessValue);
            device.setColorCode(colorCodeStr);
            device.setBrightnessPercentage(brightnessValue);
        }catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        device.inColorMode = true;
    }

    /**
     @return a string representation of the SmartColorLamp object
     */
    @Override
    public String toString() {

        //if in color mode, only color code is given, otherwise kelvin value is concatenated to "K" unit
        String colorValue = inColorMode ?  getColorCode() : getKelvinValue() + "K";

        return deviceType + " " +
                deviceName + " is " +
                getStatus() +
                " and its color value is " + colorValue + " with " + getBrightnessPercentage() +"% brightness" +
                ", and its time to switch its status is " + switchTime + ".";
    }

    /**
     * Checks if the given color value is in color mode
     * @param colorStr the color value that will be checked
     * @return true if the color value is in color mode (starts with "0x"), false otherwise
     */
    public static boolean isInColorMode(String colorStr){
        return colorStr.startsWith("0x");
    }


    /**
     * Changes the color mode of the lamp
     * @param colorMode the color mode that will be set
     */
    public void setColorMode(boolean colorMode) {
        this.inColorMode = colorMode;
    }

    /**
     * @return the color mode of the lamp
     */
    public String getColorCode() {
            return colorCode;
    }


}//end of SmartColorLamp class
