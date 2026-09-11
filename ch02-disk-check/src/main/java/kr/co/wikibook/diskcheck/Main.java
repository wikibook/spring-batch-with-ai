package kr.co.wikibook.diskcheck;

import java.io.IOException;

public class Main {
  static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.out.println("검사할 디렉터리와 남은 용량의 기대치(%)를 차례로 입력해야 합니다.");
      return;
    }
    String directory = args[0];
    int minUsablePercentage = Integer.parseInt(args[1]);
    int usablePercentage = new DiskSpaceMeter().getUsablePercentage(directory);
    System.out.println("남은 용량 " + usablePercentage + "%");
    if (usablePercentage < minUsablePercentage) {
      throw new IllegalStateException("디스크 용량이 기대치보다 작습니다 : " + usablePercentage + "% 사용 가능");
    }
  }
}
