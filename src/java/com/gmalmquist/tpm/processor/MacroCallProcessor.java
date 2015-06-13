package com.gmalmquist.tpm.processor;


import com.gmalmquist.tpm.antlr.ProcMacroCallsLexer;
import com.gmalmquist.tpm.model.MacroDef;
import com.gmalmquist.tpm.model.ProcessingContext;
import org.antlr.v4.runtime.*;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class MacroCallProcessor implements IProcessor {

  private CharStream getStream(String content) {
    try {
      return new ANTLRInputStream(new StringReader(content));
    } catch (Exception ex) {
      return null;
    }
  }

  public String processFile(ProcessingContext context, String content) {
    StringBuffer results = new StringBuffer(content.length());

    ProcMacroCallsLexer lexer = new ProcMacroCallsLexer(getStream(content));

    Pattern macroCallPattern = Pattern.compile("^[a-zA-Z0-9_$]+[(]$");

    LinkedList<Closable> macroStack = new LinkedList<>();
    for (Token token : lexer.getAllTokens()) {
      String text = token.getText();
      if (macroCallPattern.matcher(text).matches()) {
        String name = text.substring(0, text.length()-1);
        if (context.macros.containsKey(name)) {
          macroStack.push(new MacroClosable(context.macros.get(name)));
          continue;
        }
      }
      if (!macroStack.isEmpty()) {
        if (text.equals("(")) {
          macroStack.push(new ParentheticalClosable());
          continue;
        }

        if (macroStack.peek().consume(text)) {
          Closable closer = macroStack.pop();
          if (!macroStack.isEmpty()) {
            macroStack.peek().consume(closer.getText());
          } else {
            results.append(closer.getText());
          }
        }
        continue;
      }

      results.append(text);
    }

    return results.toString();
  }

  public String getName() {
    return "macro";
  }

  interface Closable {
    /** Consumes the token, returning true if we're ready to close. */
    public boolean consume(String token);

    public String getText();
  }

  class MacroClosable implements Closable {
    private MacroDef def;
    private List<String> args;
    private StringBuffer current;

    MacroClosable(MacroDef def) {
      this.def = def;
      this.args = new LinkedList<>();
      this.current = new StringBuffer(64);
    }

    public boolean consume(String token) {
      if (token.equals(")")) {
        if (current.length() > 0) {
          args.add(current.toString());
          current.delete(0, current.length());
        }
        return true;
      }
      if (token.equals(",")) {
        args.add(current.toString());
        current.delete(0, current.length());
        return false;
      }
      current.append(token);
      return false;
    }

    public String getText() {
      return def.apply(args.toArray(new String[args.size()]));
    }
  }

  class ParentheticalClosable implements Closable {
    private StringBuffer contents;

    ParentheticalClosable() {
      this.contents = new StringBuffer(64);
      contents.append("(");
    }

    public boolean consume(String token) {
      contents.append(token);

      if (token.equals(")")) {
        return true;
      }
      return false;
    }

    public String getText() {
      return contents.toString();
    }
  }
}