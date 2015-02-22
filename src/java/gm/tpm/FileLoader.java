package gm.tpm;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import gm.tpm.antlr.*;

public class FileLoader {
  
  private static HashMap<String, String> contentMap = new HashMap<>();
  
  public static String getFileContent(String path) {
    if (contentMap.containsKey(path)) {
      return contentMap.get(path);
    }
    // We put an empty message in the map before loading it,
    // to prevent infinite include recursion.
    contentMap.put(path, "");
    contentMap.put(path, loadFile(path));
    return contentMap.get(path);
  }

  private static String loadFile(String path) {
    return new IncludeProcessor().processFile(path);
  }


}