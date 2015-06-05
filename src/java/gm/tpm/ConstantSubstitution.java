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