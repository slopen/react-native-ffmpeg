package com.ffmpegcommandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

public class BinaryInstaller  {

    File installFolder;
    Context context;

    private static String CHMOD_EXEC = "700";

    private final static int FILE_WRITE_BUFFER_SIZE = 32256;

    public BinaryInstaller (
        Context context,
        File installFolder
    ) {
        this.installFolder = installFolder;

        this.context = context;
    }

    /*
     * Extract binary from the APK file
     */
    public boolean installFromRaw (
    ) throws IOException, FileNotFoundException {

        InputStream is = context.getResources ().openRawResource (
            context.getResources ().getIdentifier ("ffmpeg", "raw", context.getPackageName ())
        );
        File outFile = new File (installFolder, "ffmpeg");

        streamToFile (is, outFile, CHMOD_EXEC);

        return true;
    }

    /*
     * Write the inputstream contents to the file
     */
    private static boolean streamToFile (
        InputStream streamIn,
        File outFile,
        String mode
    ) throws IOException {

        int bytecount;
        byte[] buffer = new byte [FILE_WRITE_BUFFER_SIZE];

        OutputStream streamOut = new FileOutputStream (outFile, false);

        while ((bytecount = streamIn.read (buffer)) > 0) {
            streamOut.write (buffer, 0, bytecount);
        }

        streamOut.close ();
        streamIn.close ();

        Runtime.getRuntime ().exec (
            "chmod " + mode + " " + outFile.getAbsolutePath ()
        );

        return true;
    }


}