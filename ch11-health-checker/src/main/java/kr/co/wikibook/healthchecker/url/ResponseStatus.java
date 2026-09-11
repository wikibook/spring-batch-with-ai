package kr.co.wikibook.healthchecker.url;

import java.net.URI;

public record ResponseStatus(
	URI url,
	int statusCode,
	long responseTimeMillis
) {
}
