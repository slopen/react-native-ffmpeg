package com.ffmpegcommandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.ArrayList;

import android.content.Context;

import com.ffmpegcommandline.ShellCallback;
import com.ffmpegcommandline.BinaryInstaller;



class StreamGobbler extends Thread {

    Process process;
    ShellCallback sc;
    InputStream is;
    String type;

    StreamGobbler (
        Process process,
        ShellCallback sc,
        InputStream is,
        String type
    ) {
        this.process = process;
        this.sc = sc;
        this.is = is;
        this.type = type;
    }

    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);

            int len;
            char[] chars = new char [4*1024];

            while ((len = isr.read (chars)) >= 0) {
                sc.shellOut (chars);
            }

            if (type == "OUTPUT") {
                sc.shellOut ("FFMPEG_DONE".toCharArray ());
                process.destroy ();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace ();
            sc.shellOut ("FFMPEG_ERROR".toCharArray ());
            process.destroy ();
        }
    }
}

public class FFMPEGWrapper {

    String[] libraryAssets = {"ffmpeg"};
    File fileBinDir;
    Context context;

    public FFMPEGWrapper (
        Context ctx
    ) throws FileNotFoundException, IOException {
        context = ctx;
        fileBinDir = context.getDir ("bin", 0);

        if (!new File (fileBinDir, libraryAssets [0]).exists ()) {
            BinaryInstaller bi = new BinaryInstaller (context, fileBinDir);
            bi.installFromRaw ();
        }
    }

    public String[] processVideo (
        String[] command,
        ShellCallback sc
    ) throws Exception {

        String ffmpegBin = new File (fileBinDir, "ffmpeg").getAbsolutePath ();
        String[] ffmpegCommand = {ffmpegBin};

        ArrayList<String> temp = new ArrayList<String>();

        temp.addAll (Arrays.asList (ffmpegCommand));
        temp.addAll (Arrays.asList (command));

        String[] concatedArgs = temp.toArray (
            new String[ffmpegCommand.length + command.length]
        );

        execProcess (concatedArgs, sc);

        return concatedArgs;
    }

    private void execProcess (
        String[] cmds,
        ShellCallback sc
    ) throws Exception {

        ProcessBuilder pb = new ProcessBuilder (cmds);
        pb.redirectErrorStream (true);

        Process process = pb.start ();

        StreamGobbler errorGobbler =
            new StreamGobbler (process, sc, process.getErrorStream (), "ERROR");

        StreamGobbler outputGobbler =
            new StreamGobbler (process, sc, process.getInputStream (), "OUTPUT");

        errorGobbler.start ();
        outputGobbler.start ();
    }
}