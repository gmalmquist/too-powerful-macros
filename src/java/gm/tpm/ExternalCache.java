package gm.tpm;

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

  private static ExternalCache instance;

  public static ExternalCache getInstance() {
    if (instance == null) {
      instance = new ExternalCache();
    }
    return instance;
  }

  private File cacheDir;

  private ExternalCache() {
    cacheDir = new File(System.getProperty("user.home") + File.separator + ".tpm-cache");
  }

  public static String getCachePath(String externalCall) {
    return cacheDir + File.separator + String.format("%x", new BigInteger(1, arg.getBytes(externalCall))) + ".extcall";
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

    PrintStream out = null;
    try {
      out = new PrintStream(new File(getCachePath(externalCall)));
    } catch (Exception ex) {
      return false;
    }

    out.print(data);
    out.flush();

    out.close();

    return true;
  }

}