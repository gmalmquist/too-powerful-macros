package com.gmalmquist.tpm.plugins;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.security.MessageDigest;

public class PluginCache {

  public static int MAX_FILENAME_LENGTH = 250;

  public static String sha1(String text) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-1");
    } catch (Exception ex) {
      System.err.println("Could not locate SHA-1 algorithm!");
      return null;
    }
    byte[] data = digest.digest(text.getBytes());
    StringBuffer sb = new StringBuffer(data.length);
    for (int i = 0; i < data.length; i++) {
      String hexed = Integer.toString((data[i] & 0x0FF), 16);
      for (int j = 0; j < 2-hexed.length(); j++) {
        sb.append("0");
      }
      sb.append(hexed.substring(Math.max(0,hexed.length()-2), hexed.length()));
    }
    return sb.toString();
  }

  public static String dataToHexPath(String code) {
    StringBuffer sb = new StringBuffer(code.length()*3);
    int nameLength = 0;
    for (int i = 0; i < code.length(); i++, nameLength += 2) {
      char c = code.charAt(i);
      String hex = Integer.toHexString(c & 0x0FF);
      if (nameLength+2 > MAX_FILENAME_LENGTH) {
        sb.append(File.separator);
      }
      for (int j = 0; j < 2-hex.length(); j++) {
        sb.append("0");
      }
      for (int j = 0; j < 2 && j < hex.length(); j++) {
        sb.append(hex.charAt(j));
      }
    }
    return sb.toString();
  }

  public static boolean copy(File src, File dst) {
    try {
      Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  public static String getPath(Class<?> clazz, String name) {
    name = sha1(name);
    StringBuffer sb = new StringBuffer(128);
    sb.append(System.getProperty("user.home"));
    sb.append(File.separator);
    sb.append(".tpm-plugin-cache");
    sb.append(File.separator);
    sb.append(clazz.getName());
    sb.append(File.separator);
    sb.append(dataToHexPath(name));
    return sb.toString();
  }

  public static File[] getFiles(Class<?> clazz, String name) {
    File dir = new File(getPath(clazz, name) + File.separator + "files");
    if (!dir.exists())
      return new File[] {};
    return dir.listFiles();
  }

  public static void storeFiles(Class<?> clazz, String name, File ... files) {
    File dir = new File(getPath(clazz, name) + File.separator + "files");
    rmtree(new File(dir.getPath()));
    if (!dir.exists()) {
      dir.mkdirs();
    }
    int i = 0;
    for (File srcFile : files) {
      File dstFile = new File(dir.getPath() + File.separator + "file-" + (++i));
      copy(srcFile, dstFile);
    }
  }

  public static String getString(Class<?> clazz, String name) {
    File file = new File(getPath(clazz, name) + File.separator + "string");
    if (!file.exists())
      return null;
    if (file.isDirectory())
      return null;
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    } catch (Exception ex) {
      return null;
    }

    StringBuffer sb = new StringBuffer(128);
    while (true) {
      try {
        String line = in.readLine();
        if (line == null) break;
        if (sb.length() > 0)
          sb.append("\n");
        sb.append(line);
      } catch (IOException ex) {
        break;
      }
    }

    try { in.close(); }
    catch (IOException ex) {}

    return sb.toString();
  }

  public static boolean storeString(Class<?> clazz, String name, String value) {
    File file = new File(getPath(clazz, name) + File.separator + "string");
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      return false;
    }

    PrintStream out = null;
    try {
      out = new PrintStream(file);
    } catch (IOException ex) {
      return false;
    }

    out.print(value);
    out.flush();
    out.close();

    return true;
  }

  public static void rmtree(File file) {
    if (!file.exists())
      return;

    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        rmtree(f);
      }
    }
    file.delete();
  }

  public static void clear(Class<?> clazz, String name) {
    rmtree(new File(getPath(clazz, name)));
  }

  public static void clear(Class<?> clazz) {
    clear(clazz, "");
  }

  public static boolean hasEntry(Class<?> clazz, String name) {
    return new File(getPath(clazz, name)).exists();
  }

}