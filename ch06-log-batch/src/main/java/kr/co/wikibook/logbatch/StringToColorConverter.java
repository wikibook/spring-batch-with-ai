package kr.co.wikibook.logbatch;

import java.awt.Color;
import org.springframework.core.convert.converter.Converter;

public class StringToColorConverter implements Converter<String, Color> {
  @Override
  public Color convert(String hex) {
    return Color.decode(hex);
  }
}
