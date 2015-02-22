package gm.tpm;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import gm.tpm.antlr.*;

public class MacroCallsProcessor extends Processor<ProcMacroCallsLexer,ProcMacroCallsParser,ProcessMacroCalls> {

  private ProcessingContext context;

  public MacroCallsProcessor(ProcessingContext context) {
    this.context = context;
  }

  public ProcMacroCallsLexer createLexer(CharStream input) {
    return new ProcMacroCallsLexer(input);
  }

  public ProcMacroCallsParser createParser(TokenStream tokens) {
    return new ProcMacroCallsParser(tokens);
  }

  public ProcessMacroCalls createListener(ProcMacroCallsParser parser) {
    return new ProcessMacroCalls(parser, context);
  }

  public ParserRuleContext getRoot(ProcMacroCallsParser parser) {
    return parser.file();
  }

}