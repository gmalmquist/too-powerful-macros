package gm.tpm;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import gm.tpm.antlr.*;

public class FindIncludes extends ProcIncludesBaseListener {

  private StringBuffer fullText;

  ProcIncludesParser parser;
  public FindIncludes(ProcIncludesParser parser) { 
    this.parser = parser; 
    this.fullText = new StringBuffer(512);
  }

  @Override
  public String toString() {
    return fullText.toString();
  }

  private boolean isInclude(ParseTree tree) {
    return tree instanceof ProcIncludesParser.IncludeContext;
  }

  private boolean isIncludeChild(ParseTree tree) {
    while (tree != null) {
      if (isInclude(tree)) return true;
      tree = tree.getParent();
    }
    return false;
  }

  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    if (isInclude(ctx)) {
      ProcIncludesParser.IncludeContext include = (ProcIncludesParser.IncludeContext) ctx;
      String path = String.valueOf(include.path().getText());
      fullText.append(FileLoader.getFileContent(path));
    }
  }

  @Override
  public void visitTerminal(TerminalNode ctx) {
    if (!isIncludeChild(ctx))
      fullText.append(ctx);
  }

}