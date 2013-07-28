package io.vov.vitamio;

/**
 * DON'T TOUCH THIS FILE IF YOU DON'T KNOW THE MediaScanner PROCEDURE!!!
 */
public interface MediaScannerClient {
  public void scanFile(String path, long lastModified, long fileSize);

  public void addNoMediaFolder(String path);

  public void handleStringTag(String name, byte[] value, String valueEncoding);

  public void setMimeType(String mimeType);
}