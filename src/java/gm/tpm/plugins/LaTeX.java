package gm.tpm.plugins;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class LaTeX {

  private int QUEUE_SIZE = 50;

  private List<String> codes = new LinkedList<String>();
  private List<String> folders = new LinkedList<String>();

  private int latexIndex = 0;
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

    codes.add(data);
    folders.add(new File(dstFile).getParentFile().toString() + File.separator + "img");
    if (codes.size() > QUEUE_SIZE) {
      finish();
    }
    return "<img src=\"img/" + ("LaTeX-image-" + (++imageIndex) + ".png") + "\"></img>";
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
      PrintStream out = new PrintStream(texfile);
      out.println("\\documentclass{minimal}");
      out.println("\\usepackage{amsmath}");
      out.println("\\usepackage{amssymb}");
      out.println("\\makeatletter");
      out.println("\\DeclareRobustCommand*\\cal{\\@fontswitch\\relax\\mathcal}");
      out.println("\\makeatletter");
      out.println("\\pagestyle{empty}");
      out.println("\\newcommand{\\lt}{<}");
      out.println("\\newcommand{\\gt}{>}");
      List<String> preamble = new LinkedList<>();
      String ptag = "%PREAMBLE ";
      String nltag = "%NL";
      for (int ci = 0; ci < codes.size(); ci++) {
        String code = codes.get(ci);
        String[] lines = code.split("\n");
        boolean any = false;
        for (int i = 0; i < lines.length; i++) {
          String line = lines[i].trim();
          if (line.startsWith(ptag)) {
            line = line.substring(ptag.length()).trim();
            if (!preamble.contains(line)) {
              preamble.add(line);
              lines[i] = null;
              any = true;
            }
          } else if (line.equals("%NL")) {
            lines[i] = "";
            any = true;
          }
        }
        if (any) {
          StringBuffer sb = new StringBuffer(code.length());
          for (String line : lines) {
            if (line == null) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append(line);
          }
          codes.set(ci, sb.toString());
        }
      }
      for (String s : preamble)
        out.println(s);
      out.println("\\begin{document}");
      boolean first = true;
      for (String code : codes) {
        if (first) {
          first = false;
        } else {
          out.println("\\newpage");
        }
        out.println(code);
      }
      out.println("\\end{document}");
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

        log("moving output images");
        int index = -1;
        for (String folder : folders) {
          index++;
          latexIndex++;
          String srcPath = String.format("%s%s.png", texname, folders.size() > 1 ? "-" + index : "");
          String dstPath = folder + File.separator + "LaTeX-image-" + latexIndex + ".png";
          File srcFile = new File(srcPath);
          File dstFile = new File(dstPath);

          if (!dstFile.getParentFile().exists()) {
            dstFile.getParentFile().mkdirs();
          }

          if (srcFile.exists()) {
            if (dstFile.exists())
              dstFile.delete();
            Files.move(srcFile.toPath(), dstFile.toPath());
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