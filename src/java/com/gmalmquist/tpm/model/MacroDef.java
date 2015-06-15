package com.gmalmquist.tpm.model;

import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroDef {

  private String name;
  private String[] args;
  private String body;
  private List<Token> tokens;

  public MacroDef(String name, String[] args, String body) {
    this.name = name;
    this.args = args;
    this.body = body;

    tokens = tokenize(body);
  }

  public String getName() { return name; }

  public String[] getArgs() { return args; }

  public String getBody() { return body; }

  public String apply(String[] args) {
    String[] reArgs = new String[getArgs().length];
    for (int i = 0; i < reArgs.length && i < args.length; i++) reArgs[i] = args[i];
    for (int i = args.length; i < reArgs.length; i++) reArgs[i] = "";
    if (reArgs.length < args.length) {
      StringBuffer fillLast = new StringBuffer(reArgs[reArgs.length - 1]);
      for (int i = reArgs.length; i < args.length; i++) {
        fillLast.append(",");
        fillLast.append(args[i]);
      }
      reArgs[reArgs.length-1] = fillLast.toString();
    }

    StringBuffer sb = new StringBuffer(tokens.size()*4);
    for (Token t : tokens) {
      sb.append(t.toString(reArgs));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(128);
    sb.append(name);
    sb.append(" ");
    sb.append(java.util.Arrays.toString(args));
    sb.append(" {");
    String[] vals = new String[args.length];
    for (int i = 0; i < vals.length; i++) {
      vals[i] = String.valueOf("$" + i);
    }
    for (Token t : tokens) {
      sb.append(" [");
      sb.append(t.toString(vals));
      sb.append("]");
    }
    sb.append(" }");
    return sb.toString();
  }

  private String escape(String s) {
    String special = ".$()[]^+-*?{}\\";
    StringBuffer sb = new StringBuffer(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (special.indexOf(c) >= 0) {
        sb.append("\\");
      }
      sb.append(c);
    }
    return sb.toString();
  }

  private List<Token> tokenize(String text) {
    List<Token> ls = new LinkedList<>();

    Pattern[] patterns = new Pattern[args.length];
    for (int i = 0; i < args.length; i++) {
      patterns[i] = Pattern.compile(escape(args[i]));
    }

    while (text.length() > 0) {
      int arg = -1;
      int start = 0;
      int end = 0;
      for (int i = 0; i < args.length; i++) {
        Matcher m = patterns[i].matcher(text);
        if (m.find()) {
          if (m.group() != null && (arg < 0 || m.start() < start)) {
            arg = i;
            start = m.start();
            end = m.end();
          }
        }
      }

      if (arg < 0) {
        ls.add(new TokenLiteral(text));
        text = "";
        continue;
      }

      if (start > 0) {
        ls.add(new TokenLiteral(text.substring(0, start)));
      }

      ls.add(new TokenArg(arg));

      text = text.substring(end);
    }

    return ls;
  }

  abstract class Token {
    public abstract String toString(String[] args);
  }

  class TokenLiteral extends Token {
    String literal;
    public TokenLiteral(String s) {
      this.literal = s;
    }
    public String toString(String[] args) {
      return literal;
    }
  }

  class TokenArg extends Token {
    int arg;
    public TokenArg(int i) {
      this.arg = i;
    }
    public String toString(String[] args) {
      return args[arg];
    }
  }

}