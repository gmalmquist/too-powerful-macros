package gm.tpm;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.io.File;
import java.io.PrintStream;

import gm.tpm.antlr.*;

public class Main {

  public static Set<String> SKIP = new HashSet<>();
  public static Set<String> FLAGS = new HashSet<>();

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: \n"
        + "  tpm sources* -Darg1=4 -Darg2=potato -o outputDirectory"
        + "\n");
      return;
    }

    List<String> sources = new LinkedList<>();
    List<CmdArg> cmdArgs = new LinkedList<>();
    Map<String, String> options = new HashMap<>();

    String prevArg = null;
    for (String s : args) {
      if (s.toLowerCase().matches("^[-][-]?d.+")) {
        int pos = s.toLowerCase().indexOf('d');
        s = s.substring(pos+1);
        int div = s.indexOf('=');
        if (div > 0) {
          options.put(s.substring(0, div), s.substring(div+1));
        } else {
          options.put(s, "1");
        }
      } else {
        boolean isFlag = s.startsWith("-");
        if (prevArg != null) {
          if (isFlag) {
            cmdArgs.add(new CmdArg(prevArg, "1"));
            prevArg = s;
          } else {
            cmdArgs.add(new CmdArg(prevArg, s));
            prevArg = null;
          }
        } else if (isFlag) {
          prevArg = s;
        } else {
          sources.add(s);
        }
      }
    }

    if (sources.isEmpty()) {
      System.out.println("No sources specified.\n");
      return;
    }

    for (CmdArg arg : cmdArgs) {
      if (arg.getValue().equals("1")) {
        FLAGS.add(arg.getName().toLowerCase());
      }
    }

    String outdir = "bin";
    for (CmdArg arg : cmdArgs) {
      if (arg.is("o")) {
        outdir = arg.getValue();
      }
    }

    boolean nowrite = false;
    for (CmdArg arg : cmdArgs) {
      if (arg.is("nowrite"))
        nowrite = true;
    }

    for (CmdArg arg : cmdArgs) {
      if (arg.is("skip")) {
        SKIP.add(arg.getValue());
      }
    }

    File outdirFile = new File(outdir);
    if (!outdirFile.exists()) {
      outdirFile.mkdirs();
    }
    if (!outdirFile.isDirectory()) {
      System.err.println("Output directory cannot be an already existing non-directory!");
      return;
    }
    outdir = PathTools.realPath(outdir);
    System.out.println("OUTPUT DIRECTORY: " + outdir);

    for (String source : sources) {
      System.out.println("info: processing " + source);
      String content = "";
      String dest = PathTools.moved(".", outdir, source);
      if (!nowrite)
        content = FileProcessor.getFileContent(source, dest, options);
        //content = FileProcessor.getFileContent(source, options);
      System.out.println("info: writing to " + dest);
      File dp = new File(dest).getParentFile();
      if (!dp.exists()) {
        dp.mkdirs();
      }
      if (nowrite) continue;
      try {
        PrintStream out = new PrintStream(dest);
        out.print(content);
        out.flush();
        out.close();
      } catch (Exception ex) {
        System.err.println(" error writing output: " + ex);
      }
    }
  }

}
