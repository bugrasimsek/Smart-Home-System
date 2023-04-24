import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileOutput {
    /**
     * This function writes given content to file at given path.
     *
     * @param path    Path for the file content is going to be written.
     * @param content Content that is going to be written to file.
     * @param append  Append status, true if wanted to append to file if it exists,
     *                false if wanted to create file from zero.
     * @param newLine True if wanted to append a new line after content, false if
     *                vice versa.
     * @param sout    True if wanted to print content to console as well, false if
     *                want to print to file only.
     */

    public static boolean shouldSout = false;
    public static String path = Main.outFile;

    /**
     Writes the given content to a file specified in path variable in FileOutput class.<br><br>
     It appends to the file and adds a new line after the content.
     @param content The content to be written to the file.
     */
    public static void write(String content) {
        boolean append = true;
        boolean newLine = true;
        writeToFile(path, content, append, newLine, shouldSout);
    }

    /**
     An overloaded version of the write method that allows for specifying whether the file
     should be appended or overwritten.
     @param content The content to be written to the file.
     @param append True to append to the file, false to overwrite it.
     */
    public static void write(String content, boolean append) {
        boolean newLine = true;
        writeToFile(path, content, append, newLine, shouldSout);
    }

    /**
     An overloaded version of the write method that allows for specifying whether the file
     should be appended or overwritten and whether to add a new line after the content.
     @param content The content to be written to the file.
     @param append True to append to the file, false to overwrite it.
     @param newLine True to add a new line after the given content.
     */
    public static void write(String content, boolean append, boolean newLine) {
        writeToFile(path, content, append, newLine, shouldSout);
    }

    public static void writeToFile(String path, String content, boolean append, boolean newLine, boolean sout) {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(path, append));
            ps.print(content + (newLine ? "\n" : ""));

            // For debugging purposes. Prints content also to console if sout is true.
            if (sout)
                System.out.print(content + (newLine ? "\n" : ""));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) { // Flushes all the content and closes the stream if it has been successfully
                              // created.
                ps.flush();
                ps.close();
            }
        }
    }
}