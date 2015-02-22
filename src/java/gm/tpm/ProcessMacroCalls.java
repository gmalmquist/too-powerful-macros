 package gm.tpm;

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

public class ProcessMacroCalls extends ProcMacroCallsBaseListener {

  private ProcessingContext context;
  private StringBuffer fullText;
  private Set<ParseTree> ignoreSet;
  private ProcMacroCallsParser parser;

  private Map<ParseTree, String> replacements;

  public ProcessMacroCalls(ProcMacroCallsParser parser, ProcessingContext context) { 
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

  @Override
  public void exitFile(ProcMacroCallsParser.FileContext ctx) {  
    fullText.append(getText(ctx));
  }

  @Override
  public void exitMacroCall(ProcMacroCallsParser.MacroCallContext ctx) {
    String name = ctx.MACRO_NAME().getText();
    name = name.substring(0, name.length()-1);

    if (!context.macros.containsKey(name)) {
      return;
    }
    MacroDef macro = context.macros.get(name);

    List<String> argsList = new LinkedList<>();
    for (int i = 0; i < ctx.parameters().getChildCount(); i++) {
      ParseTree k = ctx.parameters().getChild(i);
      if (k instanceof ProcMacroCallsParser.ParameterContext) {
        argsList.add(getText(k));
      }
    }

    String[] args = argsList.toArray(new String[argsList.size()]);

    if (args.length != macro.getArgs().length) {
      System.out.println("Argument number for " + name + " doesn't match; skipping.");
      return;
    }

    replacements.put(ctx, macro.apply(args));

    context.thingsChanged = true;
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
  public void visitTerminal(TerminalNode ctx) {
    //if (!isIgnored(ctx))
    //  fullText.append(ctx);
  }

}