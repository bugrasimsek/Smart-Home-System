public class SmartCamera extends SmartDevice {
    private double megabytesPerSecond;
    private double totalMegabyteUsage;
    protected Time calculationStartTime;

    public SmartCamera(String deviceName) {
        this.deviceName = deviceName;
        this.calculationStartTime = new Time();
        this.deviceType = "Smart Camera";

        //default values will be used while creating the device, but if user gave any of the values,
        //they will be updated in the setter methods as soon as object is created
        this.megabytesPerSecond = 0;
        this.totalMegabyteUsage = 0;
    }

    /**
     * Retrieves the SmartCamera object with the given device name string.
     *
     * @param name the name of the device object to retrieve
     * @return the SmartCamera object with the given name
     * @throws DeviceNotFoundException   if no device with the given name found
     * @throws ErroneousCommandException if command is somehow incorrect
     * @throws DeviceTypeException       if the retrieved device is not a SmartCamera
     */
    public static SmartCamera getDevice(String name) throws DeviceNotFoundException, ErroneousCommandException, DeviceTypeException {
        if (!doesExists(name))
            throw new DeviceNotFoundException();

        SmartDevice device = null;

        for (SmartDevice currentDevice : smartDevices)
            if (currentDevice.deviceName.equals(name)) {
                device = currentDevice;
                break;
            }

        if (!(device instanceof SmartCamera))
            throw new DeviceTypeException("smart camera");

        return (SmartCamera) device;
    }

    /**
     * Adds a new SmartCamera device to the ArrayList of smart devices.
     *
     * @param line The command line input for adding a new SmartCamera device.
     */
    public static void addSmartCamera(String line) {
        String[] args = line.split("\t");

        String deviceName = "";
        String initialStatus = "Off";
        String megabyteStr = "NotGiven";


        switch (args.length) {
            case 4:
                deviceName = args[2];
                megabyteStr = args[3];
                break;

            case 5:
                deviceName = args[2];
                megabyteStr = args[3];
                initialStatus = args[4];
                break;

            default: // The case where number of command arguments is different from allowed
                write("ERROR: Erroneous command!");
                return;
        }

        SmartCamera camera = new SmartCamera(deviceName);

        if (doesExists(deviceName)) {
            write("ERROR: There is already a smart device with same name!");
            return;
        }

        try {
            camera.setStatus(initialStatus);
            camera.checkMegabyteConsumption(megabyteStr);
        } catch (ErroneousCommandException | IllegalArgumentException e) {
            write(e.getMessage());
            return;
        }

        //if initial status is on, then set the start time of the device
        if (camera.isOn)
            camera.calculationStartTime.setDateTime(Time.getCurrentTime().getDateTime());

        addDevice(camera);
    }

    /**
     * Checks if given megabyte usage string is valid and passes it to the setter if it's valid.
     *
     * @param megabyteStr the megabyte string that will be parsed to double
     * @throws IllegalArgumentException  if megabyte value is not a positive number
     * @throws ErroneousCommandException if megabyte value cannot be parsed to double in the first place
     */
    public void checkMegabyteConsumption(String megabyteStr) throws ErroneousCommandException, IllegalArgumentException {
        //Just returns if the megabyte value is not given at all, so that the default value of 0 is used for megabyte
        if (megabyteStr.equals("NotGiven"))
            return;

        //Otherwise, tries to parse the megabyte value to double
        double megabyte;

        try {
            megabyte = Double.parseDouble(megabyteStr);
        } catch (NumberFormatException e) {
            throw new ErroneousCommandException();
        }

        if (megabyte <= 0)
            throw new IllegalArgumentException("ERROR: Megabyte value must be a positive number!");

        else
            setMegabytesPerSecond(megabyte);
    }

    /**
     * Sets the value of megabytesPerSecond.
     *
     * @param megabytesPerSecond The double value to be set for megabytesPerSecond.
     */
    public void setMegabytesPerSecond(double megabytesPerSecond) {
        this.megabytesPerSecond = megabytesPerSecond;
    }

    /**
     * @return megabytesPerSecond - Camera's megabyte usage per second as a double.
     */
    public double getMegabytesPerSecond() {
        return megabytesPerSecond;
    }

    /**
     * Calculates the total megabyte usage of the SmartCamera object.
     * Uses formula: totalMegabyteUsage = megabytesPerSecond * timeDifference
     */
    public void calculateUsage() {
        double currentUsage = 0;
        long timeDifference = Time.getDifference(this.calculationStartTime, Time.getCurrentTime());

        currentUsage = this.megabytesPerSecond * timeDifference;

        this.totalMegabyteUsage += currentUsage;
    }

    /**
     * @return a string representation of the SmartCamera object
     */
    @Override
    public String toString() {
        String formattedUsageString = String.format("%.02f", totalMegabyteUsage);

        return deviceType + " " +
                deviceName + " is " +
                getStatus() +
                " and used " + formattedUsageString + " MB of storage so far (excluding current status)" +
                ", and its time to switch its status is " + switchTime + ".";
    }
}
