package com.gmalmquist.tpm.processor.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

import com.gmalmquist.tpm.antlr.ProcDefinitionsBaseListener;
import com.gmalmquist.tpm.antlr.ProcDefinitionsParser;
import com.gmalmquist.tpm.model.MacroDef;
import com.gmalmquist.tpm.model.ProcessingContext;
import com.gmalmquist.tpm.util.PathTools;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ProcessDefinitions extends ProcDefinitionsBaseListener {

  private ProcessingContext context;
  private StringBuffer fullText;
  private Set<ParseTree> ignoreSet;
  private ProcDefinitionsParser parser;

  public ProcessDefinitions(ProcDefinitionsParser parser, ProcessingContext context) { 
    this.parser = parser; 
    this.fullText = new StringBuffer(512);
    this.context = context;
    this.ignoreSet = new HashSet<>();
  }

  @Override
  public String toString() {
    return fullText.toString();
  }

  private String fixLineSkips(String text) {
    if (text.indexOf('\n') < 0) {
      return text;
    }
    String[] parts = text.split("\n");
    for (int i = 0; i < parts.length; i++) {
      parts[i] = parts[i].trim();
      if (parts[i].charAt(parts[i].length()-1) == '\\') {
        parts[i] = parts[i].substring(0, parts[i].length()-1);
      }
    }
    StringBuffer sb = new StringBuffer(text.length() - parts.length + 1);
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) sb.append("\n");
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  private boolean isIgnored(ParseTree tree) {
    while (tree != null) {
      if (ignoreSet.contains(tree)) return true;
      tree = tree.getParent();
    }
    return false;
  }

  @Override
  public void enterDefinition(ProcDefinitionsParser.DefinitionContext ctx) {
    if (isIgnored(ctx))
      return;
    ignoreSet.add(ctx);
    String name = ctx.NAME().getText().trim();
    String value = ctx.longValue().getText().trim();
    value = fixLineSkips(value);
    context.constants.put(name, value);
  }

  @Override
  public void enterMacro(ProcDefinitionsParser.MacroContext ctx) {
    if (isIgnored(ctx))
      return;
    ignoreSet.add(ctx);
    String name = ctx.MACRO_NAME().getText();
    name = name.substring(0, name.length()-1); // trim off the open paren
    String body = ctx.longValue().getText();
    body = fixLineSkips(body);

    List<String> args = new LinkedList<>();
    for (int i = 0; i < ctx.args().getChildCount(); i++) {
      ParseTree k = ctx.args().getChild(i);
      if (k instanceof ProcDefinitionsParser.ArgumentContext) {
        args.add(k.getText().trim());
      }
    }

    MacroDef macro = new MacroDef(name, args.toArray(new String[args.size()]), body);
    context.macros.put(macro.getName(), macro);
  }

  @Override
  public void enterIfStatement(ProcDefinitionsParser.IfStatementContext ctx) {
    //System.out.println("IF: { " + ctx.getText().replaceAll("[\n.]+", " ").trim() + " }");
  }

  private void handleIf(ParseTree ctx, boolean condition) {
    if (!condition) {
      ignoreSet.add(ctx);
      return;
    }
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree k = ctx.getChild(i);
      if (!(k instanceof ProcDefinitionsParser.IfBodyContext)) {
        ignoreSet.add(k);
      }
    }
  }

  private void handleIfn(ParseTree ctx, boolean condition) {
    handleIf(ctx, !condition);
  }

  @Override
  public void enterIfDef(ProcDefinitionsParser.IfDefContext ctx) {
    String value = ctx.NAME().getText();
    handleIf(ctx, context.constants.containsKey(value));
  }

  @Override
  public void enterIfNdef(ProcDefinitionsParser.IfNdefContext ctx) {
    String value = ctx.NAME().getText();
    handleIfn(ctx, context.constants.containsKey(value));
  }

  @Override
  public void enterIfExt(ProcDefinitionsParser.IfExtContext ctx) {
    String value = ctx.value().getText().trim();
    handleIf(ctx, context.filepath.endsWith("."+value));
  }

  @Override
  public void enterIfNext(ProcDefinitionsParser.IfNextContext ctx) {
    String value = ctx.value().getText().trim();
    handleIfn(ctx, context.filepath.endsWith("."+value));
  }

  @Override
  public void enterIfDir(ProcDefinitionsParser.IfDirContext ctx) {
    String value = ctx.value().getText().trim();
    handleIf(ctx, PathTools.inDirectory(value, context.filepath));
  }

  @Override
  public void enterIfNdir(ProcDefinitionsParser.IfNdirContext ctx) {
    String value = ctx.value().getText().trim();
    handleIfn(ctx, PathTools.inDirectory(value, context.filepath));
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {

  }

  @Override
  public void visitTerminal(TerminalNode ctx) {
    if (!isIgnored(ctx))
      fullText.append(ctx);
  }

}