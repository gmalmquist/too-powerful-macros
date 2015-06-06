package gm.tpm;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import gm.tpm.antlr.*;

public class DefinitionsProcessor extends ParserFileProcessor<ProcDefinitionsLexer,ProcDefinitionsParser,ProcessDefinitions> {

  public ProcDefinitionsLexer createLexer(CharStream input) {
    return new ProcDefinitionsLexer(input);
  }

  public ProcDefinitionsParser createParser(TokenStream tokens) {
    return new ProcDefinitionsParser(tokens);
  }

  public ProcessDefinitions createListener(ProcDefinitionsParser parser) {
    return new ProcessDefinitions(parser, getContext());
  }

  public ParserRuleContext getRoot(ProcDefinitionsParser parser) {
    return parser.file();
  }

  public String getName() {
    return "definition";
  }

}