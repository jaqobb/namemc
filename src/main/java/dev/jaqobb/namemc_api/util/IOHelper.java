/*
 * This file is a part of namemc-api, licensed under the MIT License.
 *
 * Copyright (c) Jakub Zagórski (jaqobb)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.jaqobb.namemc_api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class IOHelper {
  private IOHelper() {
  }

  public static String getReaderContent(final BufferedReader reader) throws IOException {
    if(reader == null) {
      throw new NullPointerException("reader cannot be null");
    }
    String content = "";
    String line;
    while((line = reader.readLine()) != null) {
      content += line;
      content += System.lineSeparator();
    }
    return content;
  }

  public static String getWebsiteContent(final String website) throws IOException {
    if(website == null) {
      throw new NullPointerException("website cannot be null");
    }
    if(website.isBlank()) {
      throw new IllegalArgumentException("website cannot be blank");
    }
    try(final InputStream inputStream = new URL(website).openStream(); final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return IOHelper.getReaderContent(reader);
    }
  }
}