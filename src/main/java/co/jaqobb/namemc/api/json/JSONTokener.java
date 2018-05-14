/*
 * MIT License
 *
 * Copyright (c) 2018 Jakub Zagórski
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

package co.jaqobb.namemc.api.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONTokener {
	/**
	 * current read character position on the current line.
	 */
	private long character;
	/**
	 * flag to indicate if the end of the input has been found.
	 */
	private boolean eof;
	/**
	 * current read index of the input.
	 */
	private long index;
	/**
	 * current line of the input.
	 */
	private long line;
	/**
	 * previous character read from the input.
	 */
	private char previous;
	/**
	 * Reader for the input.
	 */
	private final Reader reader;
	/**
	 * flag to indicate that a previous character was requested.
	 */
	private boolean usePrevious;
	/**
	 * the number of characters read in the previous line.
	 */
	private long characterPreviousLine;

	/**
	 * Construct a JSONTokener from a Reader. The caller must close the Reader.
	 *
	 * @param reader a reader.
	 */
	public JSONTokener(Reader reader) {
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.character = 1;
		this.characterPreviousLine = 0;
		this.line = 1;
	}

	/**
	 * Construct a JSONTokener from an InputStream. The caller must close the input stream.
	 *
	 * @param inputStream the source.
	 */
	public JSONTokener(InputStream inputStream) {
		this(new InputStreamReader(inputStream));
	}

	/**
	 * Construct a JSONTokener from a string.
	 *
	 * @param source a source string.
	 */
	public JSONTokener(String source) {
		this(new StringReader(source));
	}

	/**
	 * Back up one character. This provides a sort of lookahead capability,
	 * so that you can test for a digit or letter before attempting to parse
	 * the next number or identifier.
	 *
	 * @throws JSONException Thrown if trying to step back more than 1 step
	 *                       or if already at the start of the string
	 */
	public void back() throws JSONException {
		if (this.usePrevious || this.index <= 0) {
			throw new JSONException("Stepping back two steps is not supported");
		}
		this.decrementIndexes();
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * Decrements the indexes for the {@link #back()} method based on the previous character read.
	 */
	private void decrementIndexes() {
		this.index--;
		if (this.previous == '\r' || this.previous == '\n') {
			this.line--;
			this.character = this.characterPreviousLine;
		} else if (this.character > 0) {
			this.character--;
		}
	}

	/**
	 * Get the hex value of a character (base16).
	 *
	 * @param character a character between '0' and '9' or between 'A' and 'F' or
	 *                  between 'a' and 'f'.
	 *
	 * @return An int between 0 and 15, or -1 if c was not a hex digit.
	 */
	public static int dehexCharacter(char character) {
		if (character >= '0' && character <= '9') {
			return character - '0';
		}
		if (character >= 'A' && character <= 'F') {
			return character - ('A' - 10);
		}
		if (character >= 'a' && character <= 'f') {
			return character - ('a' - 10);
		}
		return -1;
	}

	/**
	 * Checks if the end of the input has been reached.
	 *
	 * @return true if at the end of the file and we didn't step back
	 */
	public boolean end() {
		return this.eof && !this.usePrevious;
	}

	/**
	 * Determine if the source string still contains characters that next()
	 * can consume.
	 *
	 * @return true if not yet at the end of the source.
	 *
	 * @throws JSONException thrown if there is an error stepping forward
	 *                       or backward while checking for more data.
	 */
	public boolean more() throws JSONException {
		if (this.usePrevious) {
			return true;
		}
		try {
			this.reader.mark(1);
		} catch (IOException exception) {
			throw new JSONException("Unable to preserve stream position", exception);
		}
		try {
			// -1 is EOF, but next() can not consume the null character '\0'
			if (this.reader.read() <= 0) {
				this.eof = true;
				return false;
			}
			this.reader.reset();
		} catch (IOException exception) {
			throw new JSONException("Unable to read the next character from the stream", exception);
		}
		return true;
	}

	/**
	 * Get the next character in the source string.
	 *
	 * @return the next character, or 0 if past the end of the source string.
	 *
	 * @throws JSONException thrown if there is an error reading the source string.
	 */
	public char next() throws JSONException {
		int character;
		if (this.usePrevious) {
			this.usePrevious = false;
			character = this.previous;
		} else {
			try {
				character = this.reader.read();
			} catch (IOException exception) {
				throw new JSONException(exception);
			}
		}
		if (character <= 0) { // End of stream
			this.eof = true;
			return 0;
		}
		this.incrementIndexes(character);
		this.previous = (char) character;
		return this.previous;
	}

	/**
	 * Increments the internal indexes according to the previous character
	 * read and the character passed as the current character.
	 *
	 * @param character the current character read.
	 */
	private void incrementIndexes(int character) {
		if (character > 0) {
			this.index++;
			if (character == '\r') {
				this.line++;
				this.characterPreviousLine = this.character;
				this.character = 0;
			} else if (character == '\n') {
				if (this.previous != '\r') {
					this.line++;
					this.characterPreviousLine = this.character;
				}
				this.character = 0;
			} else {
				this.character++;
			}
		}
	}

	/**
	 * Consume the next character, and check that it matches a specified
	 * character.
	 *
	 * @param character the character to match.
	 *
	 * @return the character.
	 *
	 * @throws JSONException if the character does not match.
	 */
	public char next(char character) throws JSONException {
		char matchCharacter = this.next();
		if (matchCharacter != character) {
			if (matchCharacter > 0) {
				throw this.syntaxError("Expected '" + character + "' and instead saw '" + matchCharacter + "'");
			}
			throw this.syntaxError("Expected '" + character + "' and instead saw ''");
		}
		return matchCharacter;
	}

	/**
	 * Get the next n characters.
	 *
	 * @param number The number of characters to take.
	 *
	 * @return a string of number characters.
	 *
	 * @throws JSONException substring bounds error if there are not
	 *                       n characters remaining in the source string.
	 */
	public String next(int number) throws JSONException {
		if (number == 0) {
			return "";
		}
		char[] characters = new char[number];
		int position = 0;
		while (position < number) {
			characters[position] = this.next();
			if (this.end()) {
				throw this.syntaxError("Substring bounds error");
			}
			position += 1;
		}
		return new String(characters);
	}

	/**
	 * Get the next char in the string, skipping whitespace.
	 *
	 * @return a character, or 0 if there are no more characters.
	 *
	 * @throws JSONException thrown if there is an error reading the source string.
	 */
	public char nextClean() throws JSONException {
		while (true) {
			char character = this.next();
			if (character == 0 || character > ' ') {
				return character;
			}
		}
	}

	/**
	 * Return the characters up to the next close quote character.
	 * Backslash processing is done. The formal JSON format does not
	 * allow strings in single quotes, but an implementation is allowed to
	 * accept them.
	 *
	 * @param quote the quoting character, either
	 *              <code>"</code>&nbsp;<small>(double quote)</small> or
	 *              <code>'</code>&nbsp;<small>(single quote)</small>.
	 *
	 * @return a String.
	 *
	 * @throws JSONException unterminated string.
	 */
	public String nextString(char quote) throws JSONException {
		char character;
		StringBuilder builder = new StringBuilder();
		while (true) {
			character = this.next();
			switch (character) {
				case 0:
				case '\n':
				case '\r':
					throw this.syntaxError("Unterminated string");
				case '\\':
					character = this.next();
					switch (character) {
						case 'b':
							builder.append('\b');
							break;
						case 't':
							builder.append('\t');
							break;
						case 'n':
							builder.append('\n');
							break;
						case 'f':
							builder.append('\f');
							break;
						case 'r':
							builder.append('\r');
							break;
						case 'u':
							try {
								builder.append((char) Integer.parseInt(this.next(4), 16));
							} catch (NumberFormatException exception) {
								throw this.syntaxError("Illegal escape", exception);
							}
							break;
						case '"':
						case '\'':
						case '\\':
						case '/':
							builder.append(character);
							break;
						default:
							throw this.syntaxError("Illegal escape");
					}
					break;
				default:
					if (character == quote) {
						return builder.toString();
					}
					builder.append(character);
			}
		}
	}

	/**
	 * Get the text up but not including the specified character or the
	 * end of line, whichever comes first.
	 *
	 * @param delimiter a delimiter character.
	 *
	 * @return a string.
	 *
	 * @throws JSONException thrown if there is an error while searching
	 *                       for the delimiter.
	 */
	public String nextTo(char delimiter) throws JSONException {
		StringBuilder builder = new StringBuilder();
		while (true) {
			char character = this.next();
			if (character == delimiter || character == 0 || character == '\n' || character == '\r') {
				if (character != 0) {
					this.back();
				}
				return builder.toString().trim();
			}
			builder.append(character);
		}
	}

	/**
	 * Get the text up but not including one of the specified delimiter
	 * characters or the end of line, whichever comes first.
	 *
	 * @param delimiters a set of delimiter characters.
	 *
	 * @return a string, trimmed.
	 *
	 * @throws JSONException thrown if there is an error while searching
	 *                       for the delimiter.
	 */
	public String nextTo(String delimiters) throws JSONException {
		char character;
		StringBuilder builder = new StringBuilder();
		while (true) {
			character = this.next();
			if (delimiters.indexOf(character) >= 0 || character == 0 || character == '\n' || character == '\r') {
				if (character != 0) {
					this.back();
				}
				return builder.toString().trim();
			}
			builder.append(character);
		}
	}

	/**
	 * Get the next value. The value can be a Boolean, Double, Integer,
	 * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
	 *
	 * @return an object.
	 *
	 * @throws JSONException if syntax error.
	 */
	public Object nextValue() throws JSONException {
		char character = this.nextClean();
		String string;
		switch (character) {
			case '"':
			case '\'':
				return this.nextString(character);
			case '{':
				this.back();
				return new JSONObject(this);
			case '[':
				this.back();
				return new JSONArray(this);
			default:
				break;
		}
		/*
		 * Handle unquoted text. This could be the values true, false, or
		 * null, or it can be a number. An implementation (such as this one)
		 * is allowed to also accept non-standard forms.
		 *
		 * Accumulate characters until we reach the end of the text or a
		 * formatting character.
		 */
		StringBuilder builder = new StringBuilder();
		while (character >= ' ' && ",:]}/\\\"[{;=#".indexOf(character) < 0) {
			builder.append(character);
			character = this.next();
		}
		this.back();
		string = builder.toString().trim();
		if (string.isEmpty()) {
			throw this.syntaxError("Missing value");
		}
		return JSONObject.stringToValue(string);
	}

	/**
	 * Skip characters until the next character is the requested character.
	 * If the requested character is not found, no characters are skipped.
	 *
	 * @param to a character to skip to.
	 *
	 * @return the requested character, or zero if the requested character
	 * is not found.
	 *
	 * @throws JSONException thrown if there is an error while searching
	 *                       for the to character.
	 */
	public char skipTo(char to) throws JSONException {
		char character;
		try {
			long startIndex = this.index;
			long startCharacter = this.character;
			long startLine = this.line;
			this.reader.mark(1000000);
			do {
				character = this.next();
				if (character == 0) {
					// in some readers, reset() may throw an exception if
					// the remaining portion of the input is greater than
					// the mark size (1,000,000 above).
					this.reader.reset();
					this.index = startIndex;
					this.character = startCharacter;
					this.line = startLine;
					return 0;
				}
			} while (character != to);
			this.reader.mark(1);
		} catch (IOException exception) {
			throw new JSONException(exception);
		}
		this.back();
		return character;
	}

	/**
	 * Make a JSONException to signal a syntax error.
	 *
	 * @param message the error message.
	 *
	 * @return aa JSONException object, suitable for throwing.
	 */
	public JSONException syntaxError(String message) {
		return new JSONException(message + this.toString());
	}

	/**
	 * Make a JSONException to signal a syntax error.
	 *
	 * @param message the error message.
	 * @param cause   the throwable that caused the error.
	 *
	 * @return a JSONException object, suitable for throwing.
	 */
	public JSONException syntaxError(String message, Throwable cause) {
		return new JSONException(message + this.toString(), cause);
	}

	/**
	 * Make a printable string of this JSONTokener.
	 *
	 * @return " at {index} [character {character} line {line}]".
	 */
	@Override
	public String toString() {
		return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
	}
}