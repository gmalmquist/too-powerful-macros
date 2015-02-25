package gm.tpm;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

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

    String content = FileLoader.getFileContent(path);
    content = new DefinitionsProcessor(context).processText(content);

    context.thingsChanged = true;
    for (int i = 0; context.thingsChanged && i < 10; i++) {
      context.thingsChanged = false;
      if (!Main.SKIP.contains("macros"))
        content = new MacroCallsProcessor(context).processText(content);
      if (!Main.SKIP.contains("constants"))
        content = new ConstantProcessor(context).processText(content);
    }

    if (!Main.SKIP.contains("external") && !Main.SKIP.contains("externals"))
      content = new ExternalProcessor(context).processText(content);

    return content;
  }

}