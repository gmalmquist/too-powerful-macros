package com.gmalmquist.tpm.processor;

import com.gmalmquist.tpm.model.ProcessingContext;

import java.util.List;
import java.util.LinkedList;

public abstract class AbstractBlockProcessor implements IProcessor {

  public static final List<AbstractBlockProcessor> BLOCK_PROCESSORS = new LinkedList<>();

  private String blockName;
  private String startTag;
  private String endTag;
  private boolean needsArgs;
  private String pattern;

  protected AbstractBlockProcessor(String blockName, boolean needsArgs) {
    this.blockName = blockName;
    this.needsArgs = needsArgs;
    this.startTag = "#" + blockName;
    this.endTag = "#end";
    if (this.needsArgs) {
      // It's okay that the '.' would match whitespace, because we know
      // there has to be some not-whitespace if the first space exists,
      // because this is checked against a trimmed string.
      this.pattern = "^[#]" + blockName.toLowerCase() + " .+";
    } else {
      this.pattern = "^[#]" + blockName.toLowerCase() + ".*";
    }

    BLOCK_PROCESSORS.add(this);
  }

  public String processFile(ProcessingContext context, String content) {
    System.out.println("  debug: " + getClass().getSimpleName());

    String[] lines = content.split("\n");

    String processorName = null;
    StringBuffer processorText = new StringBuffer(128);

    StringBuffer sb = new StringBuffer(content.length());
    for (String line : lines) {
      String lower = line.toLowerCase().trim();
      if (processorName == null) {
        if (lower.matches(this.pattern)) {
          processorName = line.trim().substring(startTag.length()).trim();
        } else {
          if (sb.length() > 0)
            sb.append("\n");
          sb.append(line);
        }
      } else {
        if (lower.equals(endTag)) {
          if (sb.length() > 0)
            sb.append("\n");
          sb.append(processBlock(context, processorName, processorText.toString()));
          processorName = null;
          processorText.delete(0, processorText.length());
        } else {
          if (processorText.length() > 0)
            processorText.append("\n");
          processorText.append(line);
        }
      }
    }

    return sb.toString();
  }

  public abstract String processBlock(ProcessingContext context, String blockArgs, String blockData);

  public abstract void finish();

  public String getName() {
    return blockName.toLowerCase();
  }

}