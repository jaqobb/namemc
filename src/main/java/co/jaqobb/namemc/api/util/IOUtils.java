package co.jaqobb.namemc.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Collection of useful methods related to the I/O.
 */
public final class IOUtils {
	/**
	 * Private constructor to make sure no one will initialize this class.
	 */
	private IOUtils() {
	}

	/**
	 * Returns a content of the given url.
	 *
	 * @param url A url.
	 *
	 * @return A content of the given url.
	 *
	 * @throws IOException If the error occured while trying to read url content.
	 */
	public static String getWebsiteContent(String url) throws IOException {
		try (InputStream inputStream = new URL(url).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			return IOUtils.getContent(reader);
		}
	}

	/**
	 * Returns a content of the given reader.
	 *
	 * @param reader A reader.
	 *
	 * @return A content of the given reader.
	 *
	 * @throws IOException If the error occured while trying to read reader content.
	 */
	public static String getContent(Reader reader) throws IOException {
		StringBuilder content = new StringBuilder();
		int character;
		while ((character = reader.read()) != -1) {
			content.append((char) character);
		}
		return content.toString();
	}
}