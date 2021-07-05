/*
 * Copyright (c) 2015-2021 Divested Computing Group
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 3/3/16
 * Time: 2:36 PM
 */
public class Main {

  private static final Map<String, String> databaseOut = new TreeMap<>();
  private static String wpsdbDir = "";
  private static int cTT = 0;
  private static int cVT = 0;

  private static int topLeftLat = 0;
  private static int topLeftLon = 0;
  private static int bottomRightLat = 0;
  private static int bottomRightLon = 0;

  public static void main(String[] args) {
    System.out.println("Wi-Fi Database Merger");
    System.out.println("Copyright 2015-2021 Divested Computing Group");
    System.out.println("License: GPLv3\n");
    if (args.length != 1) {
      System.out.println("Please specify a config file");
      System.exit(1);
    }
    File config = new File(args[0]);
    if (config.exists()) {
      System.out.println(config.getAbsoluteFile().getParent());
      wpsdbDir = config.getAbsoluteFile().getParent();
      executeConfig(config);
    }
  }

  private static void executeConfig(File config) {
    try {
      Scanner scanner = new Scanner(config);
      String line = "";
      boolean hasBound = false;
      while (scanner.hasNext()) {
        line = scanner.nextLine();
        if (line.startsWith("BOUND: ")) {
          String[] lineArr = line.split(": ")[1].split(", ");
          topLeftLat = Integer.valueOf(lineArr[0]);
          topLeftLon = Integer.valueOf(lineArr[1]);
          bottomRightLat = Integer.valueOf(lineArr[2]);
          bottomRightLon = Integer.valueOf(lineArr[3]);
          if (topLeftLat == 0 || topLeftLon == 0 || bottomRightLat == 0 || bottomRightLon == 0) {
            System.out.println("Invalid bound");
            System.exit(1);
          } else {
            hasBound = true;
            System.out.println("Parsed bound");
          }
        }
        if (line.startsWith("DATABASE: ")) {
          if (hasBound) {
            String[] lineArr = line.split(": ")[1].split("; ");
            if (lineArr.length == 6) {
              File db = new File(wpsdbDir + "/" + lineArr[0]);
              if (db.exists()) {
                String sep = lineArr[1];
                int bssidPos = Integer.valueOf(lineArr[2]);
                int latPos = Integer.valueOf(lineArr[3]);
                int lonPos = Integer.valueOf(lineArr[4]);
                boolean quotes = Boolean.valueOf(lineArr[5]);
                System.out.println("Parsing " + db.getName());
                processCSVDump(db, sep, bssidPos, latPos, lonPos, quotes);
              } else {
                System.out.println("Skipping unavailable database: " + lineArr[0]);
              }
            } else {
              System.out.println("A database requires 6 arguments");
              System.exit(1);
            }
          } else {
            System.out.println("A bound must be specified before any databases");
            System.exit(1);
          }
        }
      }
      File outDB = new File(config.toString().replaceAll(".cfg", "") + ".csv");
      if (outDB.exists()) {
        outDB.renameTo(new File(outDB.getName() + ".bak"));
      }
      writeDatabase(outDB);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void writeDatabase(File outDB) {
    try {
      PrintWriter out = new PrintWriter(outDB, "UTF-8");
      for (Map.Entry<String, String> entry : databaseOut.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        out.println(key + ":" + value);
      }
      out.close();
      System.out.println("--- Finished ---");
      System.out.println("Read " + cTT + " total entries");
      System.out.println("Identified " + cVT + " total entries within range");
      System.out.println("Wrote out " + databaseOut.size() + " entries to new CSV database");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void processCSVDump(File file, String sep, int bssidPos, int latPos, int lonPos,
      boolean quotes) {
    try {
      int cT = 0;
      int cV = 0;
      int eS = databaseOut.size();
      BufferedReader csv = new BufferedReader(new FileReader(file));
      String line;
      csv.readLine();
      while ((line = csv.readLine()) != null) {
        try {
          line = line.replaceAll(sep, ":");
          if (quotes) {
            line = line.replaceAll("\"", "");
          }
          String[] wifi = line.split(":");
          if (wifi.length >= 3) {
            String bssid = wifi[bssidPos];
            String lat = wifi[latPos];
            String lon = wifi[lonPos];
            if (bssid.length() == 12 && isHexadecimal(bssid)
                && Double.valueOf(lat) >= bottomRightLat && Double.valueOf(lat) <= topLeftLat
                && Double.valueOf(lon) >= topLeftLon && Double.valueOf(lon) <= bottomRightLon) {
              databaseOut.put(bssid, lat + ":" + lon);
              cV++;
            }
          }
          cT++;
        } catch (Exception e1) {
          // e1.printStackTrace();
        }
      }
      csv.close();
      System.gc();
      cTT += cT;
      cVT += cV;
      System.out.println("\tRead " + cT + " entries");
      System.out.println("\tIdentified " + cV + " entries within range");
      System.out.println("\tAdded " + (databaseOut.size() - eS) + " entries");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Credit (CC BY-SA 4.0): https://stackoverflow.com/a/13667522
  private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");
  private static boolean isHexadecimal(String input) {
    final Matcher matcher = HEXADECIMAL_PATTERN.matcher(input);
    return matcher.matches();
  }
}
