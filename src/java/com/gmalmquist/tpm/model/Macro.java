package com.gmalmquist.tpm.model;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class Macro {
  private String name;
  private String[] params;
  private MacroToken[] tokens;

  public Macro(String name, String[] params, String[] body) {
    this.name = name;
    this.tokens = new MacroToken[body.length];
    this.params = params;
    HashMap<String, Integer> paramToInt = new HashMap<>();
    for (int i = 0; i < params.length; i++) {
      paramToInt.put(params[i], i);
    }
    for (int i = 0; i < tokens.length; i++) {
      if (paramToInt.containsKey(body[i]))
        this.tokens[i] = new MacroTokenArg(paramToInt.get(body[i]));
      else
        this.tokens[i] = new MacroTokenString(body[i]);
    }
  }

  public String[] getParams() {
    return params;
  }

  public String getName() {
    return this.name;
  }

  public String[] runMacro(List<String[]> args) {
    List<String> replaced = new LinkedList<>();
    for (int i = 0; i < tokens.length; i++) {
      for (String s : tokens[i].toString(args)) {
        if (replaced.size() > 0 && s.startsWith("\n") && replaced.get(replaced.size()-1).equals("\\")) {
          replaced.remove(replaced.size()-1);
        }
        replaced.add(s);
      }
    }
    return replaced.toArray(new String[replaced.size()]);
  }

  abstract class MacroToken {
    abstract String[] toString(List<String[]> args);
  }

  class MacroTokenString extends MacroToken {
    private String str;
    MacroTokenString(String str) {
      this.str = str;
    }
    String[] toString(List<String[]> args) { return new String[] { str }; }
  }

  class MacroTokenArg extends MacroToken {
    private int index;
    MacroTokenArg(int index) {
      this.index = index;
    }
    String[] toString(List<String[]> args) {
      if (index >= args.size()) {
        System.err.println("Malformed macro arguments!");
        return new String[] { "(MALFORMED MACRO)" };
      }
      return args.get(index);
    }
  }

}