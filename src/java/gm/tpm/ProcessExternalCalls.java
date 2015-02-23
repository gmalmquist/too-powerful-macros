 package gm.tpm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import gm.tpm.antlr.*;

public class ProcessExternalCalls extends ExternalCallsBaseListener {

  private ProcessingContext context;
  private StringBuffer fullText;
  private Set<ParseTree> ignoreSet;
  private ExternalCallsParser parser;

  private Map<ParseTree, String> replacements;

  public ProcessExternalCalls(ExternalCallsParser parser, ProcessingContext context) { 
    this.parser = parser; 
    this.fullText = new StringBuffer(512);
    this.context = context;
    this.ignoreSet = new HashSet<>();
    this.replacements = new HashMap<>();
  }

  @Override
  public String toString() {
    return fullText.toString();
  }

  private boolean isIgnored(ParseTree tree) {
    while (tree != null) {
      if (ignoreSet.contains(tree)) return true;
      tree = tree.getParent();
    }
    return false;
  }

  private String getText(ParseTree tree) {
    if (replacements.containsKey(tree)) {
      return replacements.get(tree);
    }
    if (tree.getChildCount() == 0) {
      return tree.getText();
    }

    StringBuffer sb = new StringBuffer(64);
    for (int i = 0; i < tree.getChildCount(); i++) {
      sb.append(getText(tree.getChild(i)));
    }
    return sb.toString();
  }


  private String makeCall(String call, String input) {
    call = call.trim().replaceAll("\\s", " ");
    // Amazing regex inspired by 
    // http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
    String delimiter = " (?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    String name = call;
    String[] parts = { call };

    int first = call.indexOf(' ');
    if (first > 0) {
      name = call.substring(0, first);
      parts = call.substring(first+1).trim().split(delimiter);
      List<String> actual = new LinkedList<>();
      actual.add(name);
      for (String p : parts) {
        if (p.length() > 0) {
          actual.add(p);
        }
      }
      if (parts.length != actual.size()) {
        parts = actual.toArray(new String[actual.size()]);
      }
    }

    System.out.println("executing " + name + (parts == null ? "" : " with " + Arrays.toString(parts)));
    try {
      Process p = Runtime.getRuntime().exec(parts, null, new java.io.File(context.outpath).getParentFile());
      InputStream sin = p.getInputStream();
      OutputStream sout = p.getOutputStream();
      PrintStream out = new PrintStream(sout);
      out.print(input);
      out.flush();
      out.close();
      BufferedReader in = new BufferedReader(new InputStreamReader(sin));
      try {
        int exit = p.waitFor();
      } catch (InterruptedException x) {}

      StringBuffer sb = new StringBuffer(input.length());
      while (in.ready()) {
        try {
          String line = in.readLine();
          if (line == null) break;
          if (sb.length() > 0)
            sb.append("\n");
          sb.append(line);
        } catch (Exception x) {
          break;
        }
      }
      try { in.close(); } catch (Exception x) {}
      return sb.toString();
    } catch (Exception x) {
      System.err.println("Error running command: " + x);
      return "Compiler error: " + String.valueOf(x);
    }
  }


  @Override
  public void exitExternalCall(ExternalCallsParser.ExternalCallContext ctx) {
    String command = ctx.shellCall().getText().trim();
    String block = ctx.externalBlock().getText();

    replacements.put(ctx, makeCall(command, block));
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {

  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    // Collapse the stack to reduce recursion needed.
    replacements.put(ctx, getText(ctx));
  }

  @Override
  public void exitFile(ExternalCallsParser.FileContext ctx) {  
    // Actually create output text.
    fullText.append(getText(ctx));
  }

  @Override
  public void visitTerminal(TerminalNode ctx) {

  }

}