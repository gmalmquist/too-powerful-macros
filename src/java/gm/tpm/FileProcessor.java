package gm.tpm;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import gm.tpm.antlr.*;

public class FileProcessor {

  public static String getFileContent(String path, String dest) {
    return getFileContent(path, dest, new HashMap<String, String>());
  }

  public static String getFileContent(String path, String dest, Map<String, String> options) {
    ProcessingContext context = new ProcessingContext(path, options, null);
    context.outpath = dest;

    if (path == null) {
      System.err.println("Input path is null!");
    }

    if (dest == null) {
      System.err.println("Output path is null!");
    }

    File srcFile = new File(path).getAbsoluteFile();
    File dstFile = new File(dest).getAbsoluteFile();
    try {
      context.constants.put("TPM_BUILD_ROOT", new File(".").getAbsolutePath());
      context.constants.put("TPM_SRC_FILE", srcFile.getAbsolutePath());
      context.constants.put("TPM_DST_FILE", dstFile.getAbsolutePath());
      context.constants.put("TPM_FILE_NAME", srcFile.getName());
      context.constants.put("TPM_SRC_DIR", srcFile.getParentFile().getAbsolutePath());
      context.constants.put("TPM_DST_DIR", dstFile.getParentFile().getAbsolutePath());
    } catch (Exception ex) {
      System.err.println("Error setting path constants: " + ex);
      System.err.println("\tsrcFile: " + srcFile + " (exists? " + srcFile.exists() + ")");
      System.err.println("\tdstFile: " + dstFile + " (exists? " + dstFile.exists() + ")");
    }

    List<AbstractFileProcessor> preprocessors = new LinkedList<>();
    List<AbstractFileProcessor> midprocessors = new LinkedList<>();
    List<AbstractFileProcessor> endprocessors = new LinkedList<>();

    preprocessors.add(new DefinitionsProcessor());

    midprocessors.add(new FastMacroCaller());
    midprocessors.add(new ConstantProcessor());

    endprocessors.add(new ExternalProcessor());
    endprocessors.add(PluginProcessor.getInstance());

    String content = FileLoader.getFileContent(path);
    for (AbstractFileProcessor afp : preprocessors) {
      content = runMaybe(afp, context, content);
    }

    context.thingsChanged = true;
    for (int i = 0; context.thingsChanged && i < 10; i++) {
      context.thingsChanged = false;
      for (AbstractFileProcessor afp : midprocessors) {
        content = runMaybe(afp, context, content);
      }
    }

    for (AbstractFileProcessor afp : endprocessors) {
      content = runMaybe(afp, context, content);
    }

    return content;
  }

  private static String runMaybe(AbstractFileProcessor afp, ProcessingContext context, String content) {
    if (Main.SKIP.contains(afp.getName()) || Main.SKIP.contains(afp.getName()+"s"))
      return content;
    return afp.processFile(context, content);
  }

}