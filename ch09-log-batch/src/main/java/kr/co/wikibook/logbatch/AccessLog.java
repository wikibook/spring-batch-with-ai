package kr.co.wikibook.logbatch;

import java.time.Instant;

public record AccessLog(
    Instant accessDateTime,
    String ip,
    String username
) {
}
