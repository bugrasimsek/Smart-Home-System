public class Main {
    public static String inpFile;
    public static String outFile;

    /**
     * reads the input file and returns the lines of the file as an array of strings
     *
     * @param name the name of the input file
     * @return the lines of the input file as an array of strings
     */
    public static String[] read(String name) {
        return FileInput.readFile(name, true, true);
    }

    /**
     * Checks if the first line of the command is "SetInitialTime" and sets the initial time.<br><br>
     * If the first line is not "SetInitialTime" or is not in right command format, throws an error and terminates the program.<br><br>
     * Also, writes the first line to the output file, which does not append to the file but overwrites it, so that, whenever the program is run, the output file gets overwritten.<br><br>
     *
     * @param firstLine the first line of the command file
     */
    public static void checkStartingCommand(String firstLine) {
        FileOutput.write(("COMMAND: " + firstLine), false);

        String[] args = firstLine.split("\t");
        String firstCommand = args[0];

        // if the first line is not "SetInitialTime", or number of arguments is incorrect, throw an error and terminate the program
        if (!firstCommand.equals("SetInitialTime") || args.length != 2) {
            write("ERROR: First command must be set initial time! Program is going to terminate!");
            System.exit(0);
        }

        // set the initial time
        else
            Time.setInitialTime(firstLine);

    }

    /**
     * Splits the command line into arguments and calls the appropriate methods for the commands.
     */
    public static void manageCommands() {
        String[] lines = read(inpFile);

        checkStartingCommand(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            //arguments of the current line
            String[] args = line.split("\t");

            // command keyword of the current line
            String command = args[0];

            write(("COMMAND: " + line));

            /* General SmartDevice Commands */
            // Add commands for every device type
            if (command.equals("Add")) {
                String deviceType = line.split("\t")[1];
                switch (deviceType) {
                    case "SmartCamera":
                        SmartCamera.addSmartCamera(line);
                        break;
                    case "SmartLamp":
                        SmartLamp.addSmartLamp(line);
                        break;
                    case "SmartColorLamp":
                        SmartColorLamp.addSmartColorLamp(line);
                        break;
                    case "SmartPlug":
                        SmartPlug.addSmartPlug(line);
                        break;
                    default:
                        write("ERROR: Erroneous command!");
                        break;
                }
            } else if (command.equals("Remove")) {
                if (args.length != 2) {
                    write("ERROR: Erroneous command!");
                    continue;
                }

                SmartDevice.removeDevice(line);
            } else if (command.equals("ChangeName")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartDevice.changeName(line);
            } else if (command.equals("Switch")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartDevice.switchNow(line);
            } else if (command.equals("ZReport")) {
                if (args.length != 1) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartDevice.displayZReport();
            }

            /* Time Manipulation Commands */
            else if (command.equals("Nop")) {
                if (args.length != 1) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartDevice.nop();
            } else if (command.equals("SetTime")) {
                if (args.length != 2) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                Time.setCurrentTime(line);
            } else if (command.equals("SkipMinutes")) {
                if (args.length != 2) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                Time.skipMinutes(line);
            } else if (command.equals("SetSwitchTime")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartDevice.switchLater(line);
            }

            /* SmartPlug Commands */
            else if (command.equals("PlugIn")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartPlug.plugIn(line);
            } else if (command.equals("PlugOut")) {
                if (args.length != 2) {
                    write("ERROR: Erroneous command!");
                    continue;
                }

                SmartPlug.plugOut(line);

            }

            /* SmartLamp - SmartColorLamp Commands */
            else if (command.equals("SetKelvin")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }

                SmartLamp.changeKelvin(line);
            } else if (command.equals("SetBrightness")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }

                SmartLamp.changeBrightness(line);
            } else if (command.equals("SetWhite")) {
                if (args.length != 4) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartLamp.changeWhite(line);
            } else if (command.equals("SetColorCode")) {
                if (args.length != 3) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartColorLamp.changeColorCode(line);
            } else if (command.equals("SetColor")) {
                if (args.length != 4) {
                    write("ERROR: Erroneous command!");
                    continue;
                }
                SmartColorLamp.changeColor(line);
            }

            //Not recognized command
            else {
                write("ERROR: Erroneous command!");
            }
        }

        checkFinishingCommand(lines[lines.length - 1]);
    }

    /**
     * Checks if the last command is "ZReport" by reading the last line of the input file. <br><br>
     * If the last command is "ZReport", the method returns without taking any action. <br><br>
     * If the last command is not "ZReport", it calls the ZReport() method to generate a report. <br><br>
     *
     * @param lastLine the last line of the input file
     */
    public static void checkFinishingCommand(String lastLine) {
        String lastCommand = lastLine.split("\t")[0];

        if (lastCommand.equals("ZReport"))
            return;

        else {
            write("ZReport:");
            SmartDevice.displayZReport();
        }
    }

    public static void write(String content) {
        FileOutput.write(content);
    }

    public static void main(String[] args) {
        inpFile = args[0];
        outFile = args[1];
        manageCommands();

    }// end of main method

}// end of Main class