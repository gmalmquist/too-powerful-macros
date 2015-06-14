package com.gmalmquist.tpm.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

public class LaTeX {

  private int QUEUE_SIZE = 50;

  private List<String> codes = new LinkedList<String>();
  private List<String> folders = new LinkedList<String>();
  private Map<String, String> filenameMap = new HashMap<>();

  private int imageIndex = 0;

  private String density = "120";

  private void log(String msg) {
    System.out.println("[" + getClass().getSimpleName() + "] " + msg);
  }

  public String process(String srcFile, String dstFile, String args, String data) {
    String[] lines = data.split("\n");
    StringBuffer sb = new StringBuffer(data.length());
    for (String line : lines) {
      if (line.trim().length() == 0) continue;
      if (sb.length() > 0)
        sb.append("\n");
      sb.append(line);
    }
    data = sb.toString();

    args = args.trim();
    if (args.length() > 0 && !args.equals(density)) {
      if (codes.size() > 0) {
        finish();
      }
      density = args;
    }

    String folder = new File(dstFile).getParentFile().toString() + File.separator + "img";
    String filename = "LaTeX-image-" + (++imageIndex) + ".png";
    filenameMap.put(data, filename);

    File cachedFile = getCachedFile(data);
    if (cachedFile == null) {
      codes.add(data);
      folders.add(new File(dstFile).getParentFile().toString() + File.separator + "img");
      if (codes.size() > QUEUE_SIZE) {
        finish();
      }
    } else {
      if (!new File(folder).exists()) {
        new File(folder).mkdirs();
      }
      PluginCache.copy(cachedFile, new File(folder + File.separator + filename));
    }
    return "<img src=\"img/" + filename + "\"></img>";
  }

  private File getCachedFile(String code) {
    File[] files = PluginCache.getFiles(LaTeX.class, density+":" + code);
    if (files == null || files.length == 0) {
      return null;
    }
    return files[0];
  }

  private void storeCachedFile(String code, File src) {
    PluginCache.storeFiles(LaTeX.class, density+":" + code, src);
  }

  private boolean readStream(final Process process) {
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    } catch (Exception ex) {
    }
    System.out.print("  ");
    boolean success = true;
    while (true) {
      try {
        String line = in.readLine();
        if (line == null) break;
        if (success && (line.contains("Control-D to exit") || line.startsWith("! "))) {
          System.err.println("pdflatex failed!");
          new Thread(new Runnable() {
            public void run() {
              try { Thread.sleep(250); }
              catch (InterruptedException ex) {}
              process.destroy();
            }
          }).start();
          success = false;
        }
        if (!success) {
          System.err.println(line);
        } else {
          System.out.print(".");
        }
      } catch (Exception ex) {
        break;
      }
    }
    System.out.println("done");
    return success;
  }

  public void finish() {
    try {
      log("generating latex file.");
      String texname = "temp-latex-file";
      File texfile = new File(texname + ".tex");
      StringBuffer sb = new StringBuffer(512);
      sb.append("\\documentclass{minimal}"+"\n");
      sb.append("\\usepackage{amsmath}"+"\n");
      sb.append("\\usepackage{amssymb}"+"\n");
      sb.append("\\makeatletter"+"\n");
      sb.append("\\DeclareRobustCommand*\\cal{\\@fontswitch\\relax\\mathcal}"+"\n");
      sb.append("\\makeatletter"+"\n");
      sb.append("\\pagestyle{empty}"+"\n");
      sb.append("\\newcommand{\\lt}{<}"+"\n");
      sb.append("\\newcommand{\\gt}{>}"+"\n");
      List<String> preamble = new LinkedList<>();
      String ptag = "%PREAMBLE ";
      String nltag = "%NL";
      String[] originalCodes = new String[codes.size()];
      for (int ci = 0; ci < codes.size(); ci++) {
        String code = codes.get(ci);
        originalCodes[ci] = code; // for caching.
        String[] lines = code.split("\n");
        boolean any = false;
        for (int i = 0; i < lines.length; i++) {
          String line = lines[i].trim();
          if (line.length() == 0) {
            lines[i] = null;
            any = true;
            continue;
          }
          if (line.startsWith(ptag)) {
            line = line.substring(ptag.length()).trim();
            if (!preamble.contains(line)) {
              preamble.add(line);
              lines[i] = null;
              any = true;
            }
          } else if (line.equals(nltag)) {
            lines[i] = "";
            any = true;
          }
        }
        if (any) {
          StringBuffer ss = new StringBuffer(code.length());
          for (String line : lines) {
            if (line == null) continue;
            if (ss.length() > 0) ss.append("\n");
            ss.append(line);
          }
          codes.set(ci, ss.toString());
        }
      }
      for (String s : preamble)
        sb.append(s+"\n");
      sb.append("\\begin{document}"+"\n");
      boolean first = true;
      for (String code : codes) {
        if (first) {
          first = false;
        } else {
          sb.append("\\newpage"+"\n");
        }
        sb.append(code+"\n");
      }

      sb.append("\\end{document}"+"\n");

      PrintStream out = new PrintStream(texfile);
      out.print(sb.toString());
      out.close();

      log("pdflatex (" + codes.size() + " images)");
      Process pdflatex = Runtime.getRuntime().exec(new String[] {
        "pdflatex", texfile.getName(),
      });
      if (readStream(pdflatex)) {
        log("pdfcrop");
        Runtime.getRuntime().exec(new String[] {
          "pdfcrop", texname + ".pdf",
        }).waitFor();

        log("convert (density=" + density + ")");
        Runtime.getRuntime().exec(new String[] {
          "convert", "-density", density, texname + "-crop.pdf", "-quality", "90", texname + ".png"
        }).waitFor();

        File[] results = new File[folders.size()];
        log("moving output images");
        int index = -1;
        for (String folder : folders) {
          index++;
          String srcPath = String.format("%s%s.png", texname, folders.size() > 1 ? "-" + index : "");
          String dstPath = folder + File.separator + filenameMap.get(originalCodes[index]);
          File srcFile = new File(srcPath);
          File dstFile = new File(dstPath);
          results[index] = dstFile;

          if (!dstFile.getParentFile().exists()) {
            dstFile.getParentFile().mkdirs();
          }

          if (srcFile.exists()) {
            if (dstFile.exists())
              dstFile.delete();
            Files.move(srcFile.toPath(), dstFile.toPath());
            storeCachedFile(originalCodes[index], dstFile);
          } else {
            System.err.println("Src file " + srcFile + " does not exist!");
          }
        }
      }

      log("cleaning up");
      for (File file : new File(".").listFiles()) {
        if (file.getName().startsWith(texname)) {
          file.delete();
        } else if (file.getName().equals("texput.log")) {
          file.delete();
        }
      }
    } catch (Exception ex) {
      System.err.println("Exception running LaTeX: " + ex);
    }

    log("done.");

    codes.clear();
    folders.clear();
  }

}