package com.gmalmquist.tpm;

import com.gmalmquist.tpm.processor.IncludeProcessor;

import java.util.HashMap;

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