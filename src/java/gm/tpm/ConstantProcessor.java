package gm.tpm;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import gm.tpm.antlr.*;

public class ConstantProcessor extends ParserFileProcessor<ConstantRefsLexer,ConstantRefsParser,ConstantSubstitution> {

  public ConstantRefsLexer createLexer(CharStream input) {
    return new ConstantRefsLexer(input);
  }

  public ConstantRefsParser createParser(TokenStream tokens) {
    return new ConstantRefsParser(tokens);
  }

  public ConstantSubstitution createListener(ConstantRefsParser parser) {
    return new ConstantSubstitution(parser, getContext());
  }

  public ParserRuleContext getRoot(ConstantRefsParser parser) {
    return parser.file();
  }

  public String getName() {
    return "constant";
  }
}