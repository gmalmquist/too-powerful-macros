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
      sb.append(Integer.toString((data[i] * 0x0FF) + 0x100, 16).substring(1));
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
    StringBuffer sb = new StringBuffer(128);
    sb.append(".tpm-plugin-cache");
    sb.append(File.separator);
    sb.append(clazz.getName());
    sb.append(File.separator);
    sb.append(name);
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