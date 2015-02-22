package gm.tpm;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import gm.tpm.antlr.*;

public class IncludeProcessor extends Processor<ProcIncludesLexer,ProcIncludesParser,FindIncludes> {


  public ProcIncludesLexer createLexer(CharStream input) {
    return new ProcIncludesLexer(input);
  }

  public ProcIncludesParser createParser(TokenStream tokens) {
    return new ProcIncludesParser(tokens);
  }

  public FindIncludes createListener(ProcIncludesParser parser) {
    return new FindIncludes(parser);
  }

  public ParserRuleContext getRoot(ProcIncludesParser parser) {
    return parser.file();
  }

}