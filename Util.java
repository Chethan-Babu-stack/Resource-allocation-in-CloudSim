package com.IEEEpaper.examples;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Util {

    static File outFileOut;
    private static File outFileDat;
    private static File outFileLog;


    static final String DELIM = ", ";
    static final DecimalFormat DFT = new DecimalFormat("###.####");


    static void init(String path, String expName) {

//        String simName = new SimpleDateFormat("yyyyMMdd'_'HHmmss").format(new Date());
//        simName = experimentName + "_" + simName;

        //        String OUTFILE_DAT = OUTFILE_PREFIX + "/" + simName + ".dat";
//        String OUTFILE_LOG = OUTFILE_PREFIX + "/" + simName + ".log";
        String OUTFILE_DAT = path + "/" + expName + ".dat";
        String OUTFILE_LOG = path + "/" + expName + ".log";
//       String OUTFILE_OUT = OUTFILE_PREFIX + "/" + simName + ".out";
/////// String OUTFILE_OUT = "/dev/null";
        String OUTFILE_OUT = "C:/Users/I330780/Desktop/outfile.txt";
        File outFolder = new File(path);

        if (!outFolder.mkdir()) {
            System.out.println("OutFolder could not be created.");
            System.exit(1);
        }

        outFileLog = new File(OUTFILE_LOG);
        outFileDat = new File(OUTFILE_DAT);
        outFileOut = new File(OUTFILE_OUT);

        try {
            if (!outFileDat.createNewFile()) {
                System.out.println("OutFileDat not created.");
                System.exit(1);
            }
            if (!outFileLog.createNewFile()) {
                System.out.println("OutFileLog not created.");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void writeRowToDat(String row) {
        writeRow(outFileDat, row);
    }

    static void writeRowToLog(String row) {
        writeRow(outFileLog, row);
    }


    private static void writeRow(File outfile, String row) {
        if (outfile == null) {
            System.out.println("outFile is null.");
            System.exit(1);
        }

        try {
            FileWriter fileWriter = new FileWriter(outfile, true);

            if (!row.equals(""))
                fileWriter.append(row);
            fileWriter.append("\n");

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

