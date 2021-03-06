package com.gmalmquist.tpm.util;

import com.gmalmquist.tpm.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.math.BigInteger;

/**
 * Cache for external calls.
 */
public class ExternalCache {

  private static File cacheDir = new File(System.getProperty("user.home") + File.separator + ".tpm-cache");

  private ExternalCache() {
  }

  public static String getCachePath(String externalCall) {
    try {
      return cacheDir + File.separator + String.format("%x", new BigInteger(1, externalCall.getBytes("UTF-8"))) + ".extcall";
    } catch (Exception ex) {
      System.err.println(ex);
      return null;
    }
  }

  public static boolean hasCache(String externalCall) {
    if (Main.FLAGS.contains("nocache")) {
      return false;
    }
    return new File(getCachePath(externalCall)).exists();
  }

  public static String getCacheData(String externalCall) {
    if (Main.FLAGS.contains("nocache")) {
      return null;
    }

    BufferedReader in = null;
    try {
      in = new BufferedReader(
          new InputStreamReader(
              new FileInputStream(getCachePath(externalCall))));
    } catch (IOException ex) {
      return null;
    }

    StringBuffer sb = new StringBuffer(128);

    String line = null;
    do {
      try { 
        line = in.readLine(); 
      } catch (IOException ex) {
        break;
      }
      if (line != null) {
        if (sb.length() > 0) {
          sb.append("\n");
        }
        sb.append(line);
      }
    } while (line != null);

    return sb.toString();
  }

  public static boolean setCacheData(String externalCall, String data) {
    if (!cacheDir.exists() && !cacheDir.mkdirs()) {
      return false;
    }

    String path = getCachePath(externalCall);

    PrintStream out = null;
    try {
      out = new PrintStream(new File(path));
    } catch (Exception ex) {
      return false;
    }

    out.print(data);
    out.flush();

    out.close();

    return true;
  }

}