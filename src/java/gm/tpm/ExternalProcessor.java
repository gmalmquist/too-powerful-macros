package gm.tpm;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ExternalProcessor extends AbstractBlockProcessor {

  public ExternalProcessor() {
    super("external", true);
  }

  @Override
  public String processBlock(ProcessingContext context, String blockArgs, String blockData) {
    String call = blockArgs.trim().replaceAll("\\s", " ");
    String input = blockData;
    
    // Amazing regex inspired by 
    // http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
    String delimiter = " (?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    String name = call;
    String[] parts = { call };

    int first = call.indexOf(' ');
    if (first > 0) {
      name = call.substring(0, first);
      parts = call.substring(first+1).trim().split(delimiter);
      List<String> actual = new LinkedList<>();
      actual.add(name);
      for (String p : parts) {
        if (p.length() > 0) {
          actual.add(p);
        }
      }
      if (parts.length != actual.size()) {
        parts = actual.toArray(new String[actual.size()]);
      }
    }

    if (Main.SKIP.contains("external-" + name)) {
      System.out.println("debug: skipping " + name);
      return "";
    }

    String cacheCode = call + "\n" + input;
    if (ExternalCache.hasCache(cacheCode)) {
      System.out.println("debug: loading " + name + " from cache.");
      String data = ExternalCache.getCacheData(cacheCode);
      if (data != null) {
        return data;
      }
    }

    System.out.println("debug: executing " + name + (parts == null ? "" : " with " + Arrays.toString(parts)));
    try {
      File cwd = new File(context.outpath).getParentFile();
      System.out.println("       cwd is " + cwd);
      Process p = Runtime.getRuntime().exec(parts, null, cwd);
      InputStream sin = p.getInputStream();
      final InputStream serr = p.getErrorStream();
      OutputStream sout = p.getOutputStream();
      PrintStream out = new PrintStream(sout);
      out.print(input);
      out.flush();
      out.close();

      new Thread(new Runnable() {
        public void run() {
          try {
            BufferedReader in = new BufferedReader(new InputStreamReader(serr));
            while (true) {
              try {
                String line = in.readLine();
                if (line == null) break;
                System.err.println("[ERROR] " + line);
              } catch (Exception ex) {
                break;
              }
            }
          } catch (Exception ex) {}
        }
      }).start();

      BufferedReader in = new BufferedReader(new InputStreamReader(sin));
      try {
        int exit = p.waitFor();
      } catch (InterruptedException x) {}

      StringBuffer sb = new StringBuffer(input.length());
      while (in.ready()) {
        try {
          String line = in.readLine();
          if (line == null) break;
          if (sb.length() > 0)
            sb.append("\n");
          sb.append(line);
        } catch (Exception x) {
          break;
        }
      }
      try { in.close(); } catch (Exception x) {}

      String result = sb.toString();
      System.out.println("  debug: updating external call cache");
      ExternalCache.setCacheData(cacheCode, result);
      return result;
    } catch (Exception x) {
      System.err.println("Error running command: " + x);
      return "Compiler error: " + String.valueOf(x);
    }
  }

  @Override
  public void finish() {

  }

}