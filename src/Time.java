import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * The Time class represents a time in the format "yyyy-MM-dd_HH:mm:ss".<br><br>
 * Nevertheless, it can parse time strings in various formats, and check whether a given time
 * string is in the correct format.<br><br>
 * An integer, such as day or minute, can be given in format "3" instead of "03", it won't throw an error.
 */

public class Time {

    //Start time for the program, can be set only once, must be set before any other command.
    private static Time initialTime = new Time();

    //The current time of the program. It is initialized with the initial time.
    private static Time currentTime = new Time();

    //The DateTimeFormatter used to parse time strings while creating Time objects.
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    /**
     * The LocalDateTime represented by the current Time object, not static, bounded to the object.
     **/
    private LocalDateTime dateTime = null;

    /**
     * Creates a Time object from the given line after making necessary checks.
     *
     * @param line The line to create the Time object from.
     * @return The created Time object.
     * @throws IllegalArgumentException If the time format is incorrect.
     */
    public static Time createTimeObject(String line) {
        Time newTime = new Time();
        try {
            newTime.setDateTime(parseToLocalDate(getTimeString(line)));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("ERROR: Time format is not correct!");
        }
        return newTime;
    }

    /**
     * Sets the initial time for the program. Can be used once and before any other command, otherwise an error message will be displayed.
     *
     * @param line The line containing the initial time to set.
     */
    public static void setInitialTime(String line) {
        // if there is no initial time, set it
        if (initialTime.dateTime == null) {
            try {
                initialTime.dateTime = parseToLocalDate(getTimeString(line));
            } catch (IllegalArgumentException ex) {
                write("ERROR: Format of the initial date is wrong! Program is going to terminate!");
                System.exit(0);
            }

            currentTime.dateTime = initialTime.dateTime;
            write("SUCCESS: Time has been set to " + initialTime.toString() + "!");
        } else
            write("ERROR: Erroneous command!");
    }

    /**
     * Skips the specified number of minutes from the current time.
     * If the specified number of minutes is negative or zero, an error message will be displayed.
     * Otherwise, it'll call the setCurrentTime method to set the new current time.
     *
     * @param line a String containing the command to skip minutes, and the number of minutes to skip.
     */
    public static void skipMinutes(String line) {
        int minutes;
        try {
            minutes = Integer.parseInt(line.split("\t")[1]);
        } catch (NumberFormatException ex) {
            write("ERROR: Erroneous command!");
            return;
        }

        if (minutes == 0) {
            write("ERROR: There is nothing to skip!");
            return;
        }

        LocalDateTime skipTime = currentTime.dateTime.plusMinutes(minutes);
        Time newTime = new Time();
        newTime.setDateTime(skipTime);
        setCurrentTime(newTime);
    }

    /**
     * Sets the current time to the given Time object, and updates the state of the Smart Devices accordingly.<br><br>
     * If the given time is before the current time, an error message will be displayed.<br><br>
     * If the given time is the same as the current time, no action is taken.<br><br>
     * If the given time is after the current time, Smart Devices will be switched on/off accordingly.<br><br>
     *
     * @param newTime a Time object representing the new current time.
     */
    public static void setCurrentTime(Time newTime) {
        if (newTime.dateTime.isEqual(currentTime.dateTime)) {
            write("ERROR: There is nothing to change!");
            return;
        }

        // if the new time is already past, it won't be accepted
        else if (!newTime.dateTime.isBefore(currentTime.dateTime)) {
            //nop if there are switches before the new time
            while (SmartDevice.getFirstSwitchTime() != null && SmartDevice.getFirstSwitchTime().getDateTime().isBefore(newTime.getDateTime()))
                SmartDevice.nop();

            currentTime = newTime;
            SmartDevice.switchDevices();
        } else
            write("ERROR: Time cannot be reversed!");
    }

    /**
     * Creates a Time object from given command string if command is correct<br><br>
     * Sets the current time to the new Time object, and updates the state of the Smart Devices accordingly.<br><br>
     * If the given time is before the current time, an error message will be displayed.<br><br>
     * If the given time is the same as the current time, an error message will be displayed.<br><br>
     * If the given time is after the current time, current time is updated Smart Devices will be switched on/off if necessary.<br><br>
     *
     * @param line the command string that includes the new time.
     */
    public static void setCurrentTime(String line) {
        Time newTime;
        try {
            newTime = createTimeObject(line);

            // if the new time is the same as the current time, illegal argument exception is thrown
            if (newTime.dateTime.isEqual(currentTime.dateTime))
                throw new IllegalArgumentException("ERROR: There is nothing to change!");

            // if the new time is already past, it won't be accepted
            if (newTime.dateTime.isBefore(currentTime.dateTime))
                throw new IllegalArgumentException("ERROR: Time cannot be reversed!");
        } catch (IllegalArgumentException ex) {
            write(ex.getMessage());
            return;
        }

        //nop if there are switches before the new time
        while (SmartDevice.getFirstSwitchTime() != null && SmartDevice.getFirstSwitchTime().getDateTime().isBefore(newTime.getDateTime()))
            SmartDevice.nop();

        currentTime = newTime;
        SmartDevice.switchDevices();
    }

    /**
     * Calculates the difference in minutes between two Time objects.
     *
     * @param time1 the first Time object.
     * @param time2 the second Time object.
     * @return the difference in minutes between the two Time objects (never returns a negative difference). <br><br>
     * If either of the Time objects is null, -1 is returned.
     */
    public static long getDifference(Time time1, Time time2) {
        Long duration = null;
        try {
            LocalDateTime localTime1 = time1.dateTime;
            LocalDateTime localTime2 = time2.dateTime;
            duration = MINUTES.between(localTime1, localTime2);
        } catch (NullPointerException e) {
            return -1;
        }

        return Math.abs(duration);
    }

    /* Simple Getters and Setters (That does not have any checks, not used with command lines */

    /**
     * Returns the current time.
     *
     * @return a Time object representing the current time.
     */
    public static Time getCurrentTime() {
        return currentTime;
    }

    /**
     * Returns the initial time when the simulation was started.
     *
     * @return a Time object representing the initial time.
     */
    public static Time getInitialTime() {
        return initialTime;
    }

    /**
     * Returns the LocalDateTime object of this Time object.
     * This method is replaced by direct comparison within the class.
     *
     * @return a LocalDateTime object representing the time of this Time object.
     */
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    /**
     * Sets the LocalDateTime bounded to this Time object.
     *
     * @param dateTime The LocalDateTime object representing the time to set.
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /* Private Methods That Are For In-Class Use Only  */

    /**
     * Writes the given content to the output file. (appends to file, adds a new line)
     *
     * @param content the content to be written to the output file
     */
    private static void write(String content) {
        FileOutput.write(content);
    }

    /**
     * Checks whether the given time string is in the correct format.
     *
     * @param timeString The time string to check.
     * @return The LocalDateTime object representing the time string.
     * @throws IllegalArgumentException If the time string is not in the correct format.
     */
    private static LocalDateTime parseToLocalDate(String timeString) throws IllegalArgumentException {
        try {
            return LocalDateTime.parse(timeString, formatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Splits the given command line regarding time and extracts the first parseable time string.
     *
     * @param line The command line to extract the time string from.
     * @return The first time string encountered, "No time string found!" if there's no valid time string.
     */
    private static String getTimeString(String line) {
        String[] splitLine = line.split("\t");
        for (String keyword : splitLine) {
            try {
                LocalDateTime.parse(keyword, formatter);
                return keyword;
            } catch (DateTimeParseException e) {
                //ignore the exception and try next keyword
                continue;
            }
        }
        return "No time string found!";
    }

    /**
     * Returns a string representation of this Time object in the format "yyyy-MM-dd_HH:mm:ss".
     *
     * @return a string representation of this Time object
     */
    @Override
    public String toString() {
        return this.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"));
    }
}//end of Time class