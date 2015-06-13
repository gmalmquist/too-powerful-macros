package com.gmalmquist.tpm.processor.listeners;

import java.util.HashSet;
import java.util.Set;

import com.gmalmquist.tpm.antlr.ConstantRefsBaseListener;
import com.gmalmquist.tpm.antlr.ConstantRefsParser;
import com.gmalmquist.tpm.model.ProcessingContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ConstantSubstitution extends ConstantRefsBaseListener {

  private ProcessingContext context;
  private StringBuffer fullText;
  private Set<ParseTree> ignoreSet;
  private ConstantRefsParser parser;

  public ConstantSubstitution(ConstantRefsParser parser, ProcessingContext context) { 
    this.parser = parser; 
    this.fullText = new StringBuffer(512);
    this.context = context;
    this.ignoreSet = new HashSet<>();
    
    this.context.constants.put("FILE", context.filepath);
  }

  @Override
  public String toString() {
    return fullText.toString();
  }

  @Override
  public void enterReference(ConstantRefsParser.ReferenceContext ctx) {
    String name = ctx.NAME().getText();
    if (context.constants.containsKey(name)) {
      context.thingsChanged = true;
      fullText.append(context.constants.get(name));
      for (int i = 0; i < ctx.getChildCount(); i++) {
        ignoreSet.add(ctx.getChild(i));
      }
    }
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {

  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {

  }

  @Override
  public void visitTerminal(TerminalNode ctx) {
    if (!ignoreSet.contains(ctx)) {
      fullText.append(ctx.getText());
    }
  }

}