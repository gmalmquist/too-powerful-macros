package gm.tpm;

public interface AbstractFileProcessor {
  /**
   * Process the given input file, and return the processed text.
   * @param context The processing context for this file.
   * @param content the full text content of the file.
   * @return the processed text of the file.
   */
  public String processFile(ProcessingContext context, String content);

  /**
   * The name of this processor, used for debugs and skipping.
   */
  public String getName();
}