public class SmartPlug extends SmartDevice {
    private double ampere;
    private int voltage;
    private double totalEnergyConsumption;
    protected Time calculationStartTime;

    public SmartPlug(String deviceName) {
        this.deviceName = deviceName;
        this.calculationStartTime = new Time();
        this.deviceType = "Smart Plug";

        //default values will be used while creating the device, but if user gave any of the values,
        //they will be updated in the setter methods as soon as object is created
        this.ampere = 0;
        this.voltage = 220;
        this.totalEnergyConsumption = 0;
    }

    /**
     * Retrieves the SmartPlug object with the given device name string.
     *
     * @param name the name of the device object to retrieve
     * @return the SmartPlug object with the given name
     * @throws DeviceNotFoundException   if no device with the given name found
     * @throws ErroneousCommandException if command is somehow incorrect
     * @throws DeviceTypeException       if the retrieved device is not a SmartPlug
     */
    public static SmartPlug getDevice(String name) throws DeviceNotFoundException, ErroneousCommandException, DeviceTypeException {
        if (!doesExists(name))
            throw new DeviceNotFoundException();

        SmartDevice device = null;

        for (SmartDevice currentDevice : smartDevices)
            if (currentDevice.deviceName.equals(name)) {
                device = currentDevice;
                break;
            }


        if (!(device instanceof SmartPlug))
            throw new DeviceTypeException("smart plug");

        return (SmartPlug) device;
    }

    /**
     * Checks if given ampere string is valid and passes it to the setter if it's valid.
     *
     * @param ampereStr the megabyte string that will be parsed to double
     * @throws IllegalArgumentException  if ampere value is not a positive number
     * @throws ErroneousCommandException if ampere value cannot be parsed to double in the first place
     */
    public void checkAmpere(String ampereStr) throws ErroneousCommandException {
        //Just returns if the ampere value is not given at all, so that the default value of 0 is used for ampere
        if (ampereStr.equals("NotGiven"))
            return;

        //Otherwise, tries to parse the ampere value to double
        double ampere;

        try {
            ampere = Double.parseDouble(ampereStr);
        } catch (NumberFormatException e) {
            throw new ErroneousCommandException();
        }

        if (ampere <= 0)
            throw new IllegalArgumentException("ERROR: Ampere value must be a positive number!");

        else
            setAmpere(ampere);
    }

    /**
     * Adds a new SmartPlug device to the ArrayList of smart devices.
     *
     * @param line The command line input for adding a new SmartPlug device.
     */
    public static void addSmartPlug(String line) {
        String[] args = line.split("\t");

        String deviceName = "";
        String initialStatus = "Off";
        String ampereStr = "NotGiven";


        switch (args.length) {
            case 3:
                deviceName = args[2];
                break;

            case 4:
                deviceName = args[2];
                initialStatus = args[3];
                break;

            case 5: // The case where ampere value is given
                deviceName = args[2];
                initialStatus = args[3];
                ampereStr = args[4];
                break;

            default: // The case where number of command arguments is different from allowed
                write("ERROR: Erroneous command!");
                return;
        }

        SmartPlug plug = new SmartPlug(deviceName);

        if (doesExists(deviceName)) {
            write("ERROR: There is already a smart device with same name!");
            return;
        }

        try {
            plug.setStatus(initialStatus);
            plug.checkAmpere(ampereStr);
        } catch (ErroneousCommandException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        //if initial status is on, then set the start time of the device
        if (plug.isOn)
            plug.calculationStartTime.setDateTime(Time.getCurrentTime().getDateTime());

        addDevice(plug);
    }


    /**
     * Plugs in a device to the SmartPlug.<br>
     * If the given ampere value is not valid, gives an error message. <br>
     * If the given device name is not valid, gives an error message.<br>
     * If the given device is not a SmartPlug, gives an error message.<br>
     * If the device has already something plugged in, gives an error message.<br>
     *
     * @param line the input line containing the name of the device and its ampere value separated by a tab
     */
    public static void plugIn(String line) {
        String name = null;
        String ampereStr;
        SmartPlug plug = null;

        try {
            name = line.split("\t")[1];
            ampereStr = line.split("\t")[2];
            plug = getDevice(name);

            // check if the device is already plugged in
            if (plug.isSomethingPlugged())
                throw new IllegalArgumentException("ERROR: There is already an item plugged in to that plug!");

            //check if the given ampere value is valid, if it is, then set the ampere value of the plug
            plug.checkAmpere(ampereStr);
        } catch (ArrayIndexOutOfBoundsException e) {
            write("ERROR: Erroneous command!");
            return;
        } catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException |
                 IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }


        //if something is plugged in while plug is switched on, start timer
        if (plug.isOn)
            plug.calculationStartTime.setDateTime(Time.getCurrentTime().getDateTime());


    }

    /**
     * Unplugs the device from the SmartPlug.<br>
     * If the given device name is not valid, gives an error message.<br>
     * If the given device is not a SmartPlug, gives an error message.<br>
     * If the device has nothing plugged in, gives an error message.<br>
     *
     * @param line the input line containing the name of the device separated by a tab
     */
    public static void plugOut(String line) {
        String name;
        SmartPlug plug = null;

        try {
            name = line.split("\t")[1];
            plug = getDevice(name);

            // check if plug is already empty
            if (!plug.isSomethingPlugged())
                throw new IllegalArgumentException("ERROR: This plug has no item to plug out from that plug!");
        } catch (DeviceNotFoundException | ErroneousCommandException | DeviceTypeException |
                 IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }


        //Unplugging something is actually just setting the ampere value to 0
        plug.ampere = 0;

        //calculate the consumption since the device is now removed from plug
        plug.calculateConsumption();

        //reset the calculation start time for next calculation
        plug.calculationStartTime.setDateTime(null);

    }

    /**
     * Calculates the energy consumption of the SmartPlug device.
     * Uses the formula: energy consumption = (ampere * voltage * time difference) / 60
     * where time difference is the difference between the current time and the start time of the device in minutes.
     */
    public void calculateConsumption() {
        double currentConsumption = 0;
        long timeDifference = Time.getDifference(this.calculationStartTime, Time.getCurrentTime());

        currentConsumption = (ampere * voltage * timeDifference) / (60);

        this.totalEnergyConsumption += currentConsumption;
    }

    /**
     * @return true if ampere is not equal to 0 (something plugged),
     * false if it is equal to 0 (nothing plugged).
     */
    public boolean isSomethingPlugged() {
        return ampere != 0;
    }

    /**
     * @return a string representation of the SmartPlug object
     */
    @Override
    public String toString() {
        String formattedConsumptionString = String.format("%.02f", totalEnergyConsumption);

        return deviceType + " " +
                deviceName + " is " +
                getStatus() +
                " and consumed " + formattedConsumptionString + "W so far (excluding current device)" +
                ", and its time to switch its status is " + switchTime + ".";
    }

    /**
     * Sets the value of megabytesPerSecond.
     *
     * @param ampere The double value to be set for megabytesPerSecond.
     */
    public void setAmpere(double ampere) {
        this.ampere = ampere;
    }
}// end of SmartPlug class
