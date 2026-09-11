package kr.co.wikibook.diskcheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DiskSpaceMeter {

  public int getUsablePercentage(String directory) throws IOException {
    var fileStore = Files.getFileStore(Path.of(directory));
    return (int) (fileStore.getUsableSpace() * 100 / fileStore.getTotalSpace());
  }
}
