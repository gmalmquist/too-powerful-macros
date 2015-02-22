package gm.tpm;

import java.util.Map;
import java.util.HashMap;

public class ProcessingContext {
  
  public String filepath;
  public Map<String, String> constants;
  public Map<String, MacroDef> macros;

  public boolean thingsChanged = false;

  public ProcessingContext(String filepath, Map<String, String> constants, Map<String, MacroDef> macros) {
    this.filepath = filepath;
    this.constants = new HashMap<>();
    this.macros = new HashMap<>();

    if (constants != null) {
      for (String key : constants.keySet()) {
        this.constants.put(key, constants.get(key));
      }
    }

    if (macros != null) {
      for (String key : macros.keySet()) {
        this.macros.put(key, macros.get(key));
      }
    }
  }

  public ProcessingContext() {
    this(null, null, null);
  }

}