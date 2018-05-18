/*
 * Copyright (c) 2002 JSON.org
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
 * The Software shall be used for Good, not Evil.
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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2016-08/15
 */
public class JSONArray implements Iterable<Object>
{
	/**
	 * The arrayList where the JSONArray's properties are kept.
	 */
	private final ArrayList<Object> properties;

	/**
	 * Construct an empty JSONArray.
	 */
	public JSONArray()
	{
		this.properties = new ArrayList<>(10);
	}

	/**
	 * Construct a JSONArray from a JSONTokener.
	 *
	 * @param tokener a JSONTokener.
	 *
	 * @throws JSONException if there is a syntax error.
	 */
	public JSONArray(JSONTokener tokener) throws JSONException
	{
		this();
		if (tokener.nextClean() != '[')
		{
			throw tokener.syntaxError("A JSONArray text must start with '['");
		}
		char nextCharacter = tokener.nextClean();
		if (nextCharacter == 0)
		{
			// array is unclosed. No ']' found, instead EOF
			throw tokener.syntaxError("Expected a ',' or ']'");
		}
		if (nextCharacter != ']')
		{
			tokener.back();
			while (true)
			{
				if (tokener.nextClean() == ',')
				{
					tokener.back();
					this.properties.add(JSONObject.NULL);
				}
				else
				{
					tokener.back();
					this.properties.add(tokener.nextValue());
				}
				switch (tokener.nextClean())
				{
					case 0:
						// array is unclosed. No ']' found, instead EOF
						throw tokener.syntaxError("Expected a ',' or ']'");
					case ',':
						nextCharacter = tokener.nextClean();
						if (nextCharacter == 0)
						{
							// array is unclosed. No ']' found, instead EOF
							throw tokener.syntaxError("Expected a ',' or ']'");
						}
						if (nextCharacter == ']')
						{
							return;
						}
						tokener.back();
						break;
					case ']':
						return;
					default:
						throw tokener.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	/**
	 * Construct a JSONArray from a source JSON text.
	 *
	 * @param source a string that begins with <code>[</code>&nbsp;<small>(left
	 *               bracket)</small> and ends with <code>]</code>
	 *               &nbsp;<small>(right bracket)</small>.
	 *
	 * @throws JSONException if there is a syntax error.
	 */
	public JSONArray(String source) throws JSONException
	{
		this(new JSONTokener(source));
	}

	/**
	 * Construct a JSONArray from a Collection.
	 *
	 * @param collection a Collection.
	 */
	public JSONArray(Collection<?> collection)
	{
		if (collection == null)
		{
			this.properties = new ArrayList<>(10);
		}
		else
		{
			this.properties = new ArrayList<>(collection.size());
			for (Object object : collection)
			{
				this.properties.add(JSONObject.wrap(object));
			}
		}
	}

	/**
	 * Construct a JSONArray from an array
	 *
	 * @throws JSONException if not an array or if an array value is non-finite number.
	 */
	public JSONArray(Object array) throws JSONException
	{
		this();
		if (array.getClass().isArray())
		{
			int length = Array.getLength(array);
			this.properties.ensureCapacity(length);
			for (int index = 0; index < length; index++)
			{
				this.put(JSONObject.wrap(Array.get(array, index)));
			}
		}
		else
		{
			throw new JSONException("JSONArray initial value should be a string or collection or array");
		}
	}

	@Override
	public Iterator<Object> iterator()
	{
		return this.properties.iterator();
	}

	/**
	 * Get the object value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return an object value.
	 *
	 * @throws JSONException if there is no value for the index.
	 */
	public Object get(int index) throws JSONException
	{
		Object object = this.opt(index);
		if (object == null)
		{
			throw new JSONException("JSONArray[" + index + "] not found");
		}
		return object;
	}

	/**
	 * Get the boolean value associated with an index. The string values "true"
	 * and "false" are converted to boolean.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @throws JSONException if there is no value for the index or if the value is not
	 *                       convertible to boolean.
	 */
	public boolean getBoolean(int index) throws JSONException
	{
		Object object = this.get(index);
		if (object.equals(Boolean.FALSE) || (object instanceof String && ((String) object).equalsIgnoreCase("false")))
		{
			return false;
		}
		if (object.equals(Boolean.TRUE) || (object instanceof String && ((String) object).equalsIgnoreCase("true")))
		{
			return true;
		}
		throw new JSONException("JSONArray[" + index + "] is not a boolean");
	}

	/**
	 * Get the double value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 *
	 * @throws JSONException if the key is not found or if the value cannot be converted
	 *                       to a number.
	 */
	public double getDouble(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			return object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble((String) object);
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] is not a number", exception);
		}
	}

	/**
	 * Get the float value associated with a key.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the numeric value.
	 *
	 * @throws JSONException if the key is not found or if the value is not a Number
	 *                       object and cannot be converted to a number.
	 */
	public float getFloat(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			return object instanceof Number ? ((Number) object).floatValue() : Float.parseFloat(object.toString());
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] is not a number", exception);
		}
	}

	/**
	 * Get the Number value associated with a key.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the numeric value.
	 *
	 * @throws JSONException if the key is not found or if the value is not a Number
	 *                       object and cannot be converted to a number.
	 */
	public Number getNumber(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			if (object instanceof Number)
			{
				return (Number) object;
			}
			return JSONObject.stringToNumber(object.toString());
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] is not a number", exception);
		}
	}

	/**
	 * Get the enum value associated with an index.
	 *
	 * @param clazz the type of enum to retrieve.
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the enum value at the index location
	 *
	 * @throws JSONException if the key is not found or if the value cannot be converted
	 *                       to an enum.
	 */
	public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException
	{
		E value = this.optEnum(clazz, index);
		if (value == null)
		{
			// JSONException should really take a throwable argument.
			// If it did, I would re-implement this with the Enum.valueOf
			// method and place any thrown exception in the JSONException
			throw new JSONException("JSONArray[" + index + "] is not an enum of type " + JSONObject.quote(clazz.getSimpleName()));
		}
		return value;
	}

	/**
	 * Get the BigDecimal value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 *
	 * @throws JSONException if the key is not found or if the value cannot be converted
	 *                       to a BigDecimal.
	 */
	public BigDecimal getBigDecimal(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			return new BigDecimal(object.toString());
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] could not convert to BigDecimal", exception);
		}
	}

	/**
	 * Get the BigInteger value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 *
	 * @throws JSONException if the key is not found or if the value cannot be converted
	 *                       to a BigInteger.
	 */
	public BigInteger getBigInteger(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			return new BigInteger(object.toString());
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] could not convert to BigInteger", exception);
		}
	}

	/**
	 * Get the int value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 *
	 * @throws JSONException if the key is not found or if the value is not a number.
	 */
	public int getInt(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			return object instanceof Number ? ((Number) object).intValue() : Integer.parseInt((String) object);
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] is not a number", exception);
		}
	}

	/**
	 * Get the JSONArray associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return a JSONArray value.
	 *
	 * @throws JSONException if there is no value for the index. or if the value is not a
	 *                       JSONArray.
	 */
	public JSONArray getJSONArray(int index) throws JSONException
	{
		Object object = this.get(index);
		if (object instanceof JSONArray)
		{
			return (JSONArray) object;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONArray");
	}

	/**
	 * Get the JSONObject associated with an index.
	 *
	 * @param index subscript.
	 *
	 * @return a JSONObject value.
	 *
	 * @throws JSONException if there is no value for the index or if the value is not a
	 *                       JSONObject.
	 */
	public JSONObject getJSONObject(int index) throws JSONException
	{
		Object object = this.get(index);
		if (object instanceof JSONObject)
		{
			return (JSONObject) object;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONObject");
	}

	/**
	 * Get the long value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 *
	 * @throws JSONException if the key is not found or if the value cannot be converted
	 *                       to a number.
	 */
	public long getLong(int index) throws JSONException
	{
		Object object = this.get(index);
		try
		{
			return object instanceof Number ? ((Number) object).longValue() : Long.parseLong((String) object);
		}
		catch (Exception exception)
		{
			throw new JSONException("JSONArray[" + index + "] is not a number", exception);
		}
	}

	/**
	 * Get the string associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return a string value.
	 *
	 * @throws JSONException if there is no string value for the index.
	 */
	public String getString(int index) throws JSONException
	{
		Object object = this.get(index);
		if (object instanceof String)
		{
			return (String) object;
		}
		throw new JSONException("JSONArray[" + index + "] not a string");
	}

	/**
	 * Determine if the value is <code>null</code>.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return true if the value at the index is <code>null</code>, or if there is no value.
	 */
	public boolean isNull(int index)
	{
		return JSONObject.NULL.equals(this.opt(index));
	}

	/**
	 * Make a string from the contents of this JSONArray. The
	 * <code>separator</code> string is inserted between each element. Warning:
	 * This method assumes that the data structure is acyclical.
	 *
	 * @param separator a string that will be inserted between the elements.
	 *
	 * @return a string.
	 *
	 * @throws JSONException of the array contains an invalid number.
	 */
	public String join(String separator) throws JSONException
	{
		int length = this.length();
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < length; index++)
		{
			if (index > 0)
			{
				builder.append(separator);
			}
			builder.append(JSONObject.valueToString(this.properties.get(index)));
		}
		return builder.toString();
	}

	/**
	 * Get the number of elements in the JSONArray, included nulls.
	 *
	 * @return the length (or size).
	 */
	public int length()
	{
		return this.properties.size();
	}

	/**
	 * Get the optional object value associated with an index.
	 *
	 * @param index the index must be between 0 and length() - 1. If not, null is returned.
	 *
	 * @return an object value, or null if there is no object at that index.
	 */
	public Object opt(int index)
	{
		return (index < 0 || index >= this.length()) ? null : this.properties.get(index);
	}

	/**
	 * Get the optional boolean value associated with an index. It returns false
	 * if there is no value at that index, or if the value is not Boolean.TRUE
	 * or the String "true".
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the truth.
	 */
	public boolean optBoolean(int index)
	{
		return this.optBoolean(index, false);
	}

	/**
	 * Get the optional boolean value associated with an index. It returns the
	 * defaultValue if there is no value at that index or if it is not a Boolean
	 * or the String "true" or "false" (case insensitive).
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue a boolean default.
	 *
	 * @return the truth.
	 */
	public boolean optBoolean(int index, boolean defaultValue)
	{
		try
		{
			return this.getBoolean(index);
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}

	/**
	 * Get the optional double value associated with an index. NaN is returned
	 * if there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 */
	public double optDouble(int index)
	{
		return this.optDouble(index, Double.NaN);
	}

	/**
	 * Get the optional double value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index        subscript
	 * @param defaultValue the default value.
	 *
	 * @return tje value.
	 */
	public double optDouble(int index, double defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof Number)
		{
			return ((Number) value).doubleValue();
		}
		if (value instanceof String)
		{
			try
			{
				return Double.parseDouble((String) value);
			}
			catch (Exception exception)
			{
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get the optional float value associated with an index. NaN is returned
	 * if there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 */
	public float optFloat(int index)
	{
		return this.optFloat(index, Float.NaN);
	}

	/**
	 * Get the optional float value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index        subscript.
	 * @param defaultValue The default value.
	 *
	 * @return the value.
	 */
	public float optFloat(int index, float defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof Number)
		{
			return ((Number) value).floatValue();
		}
		if (value instanceof String)
		{
			try
			{
				return Float.parseFloat((String) value);
			}
			catch (Exception exception)
			{
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get the optional int value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 */
	public int optInt(int index)
	{
		return this.optInt(index, 0);
	}

	/**
	 * Get the optional int value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default value.
	 *
	 * @return the value.
	 */
	public int optInt(int index, int defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof Number)
		{
			return ((Number) value).intValue();
		}
		if (value instanceof String)
		{
			try
			{
				return new BigDecimal(value.toString()).intValue();
			}
			catch (Exception exception)
			{
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get the enum value associated with a key.
	 *
	 * @param clazz the type of enum to retrieve.
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return tThe enum value at the index location or null if not found.
	 */
	public <E extends Enum<E>> E optEnum(Class<E> clazz, int index)
	{
		return this.optEnum(clazz, index, null);
	}

	/**
	 * Get the enum value associated with a key.
	 *
	 * @param clazz        the type of enum to retrieve.
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default in case the value is not found.
	 *
	 * @return the enum value at the index location or defaultValue if
	 * the value is not found or cannot be assigned to clazz.
	 */
	public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue)
	{
		try
		{
			Object value = this.opt(index);
			if (JSONObject.NULL.equals(value))
			{
				return defaultValue;
			}
			if (clazz.isAssignableFrom(value.getClass()))
			{
				// we just checked it!
				@SuppressWarnings("unchecked")
				E e = (E) value;
				return e;
			}
			return Enum.valueOf(clazz, value.toString());
		}
		catch (IllegalArgumentException | NullPointerException exception)
		{
			return defaultValue;
		}
	}

	/**
	 * Get the optional BigInteger value associated with an index. The
	 * defaultValue is returned if there is no value for the index, or if the
	 * value is not a number and cannot be converted to a number.
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default value.
	 *
	 * @return the value.
	 */
	public BigInteger optBigInteger(int index, BigInteger defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof BigInteger)
		{
			return (BigInteger) value;
		}
		if (value instanceof BigDecimal)
		{
			return ((BigDecimal) value).toBigInteger();
		}
		if (value instanceof Double || value instanceof Float)
		{
			return new BigDecimal(((Number) value).doubleValue()).toBigInteger();
		}
		if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte)
		{
			return BigInteger.valueOf(((Number) value).longValue());
		}
		try
		{
			final String valueString = value.toString();
			if (JSONObject.isDecimalNotation(valueString))
			{
				return new BigDecimal(valueString).toBigInteger();
			}
			return new BigInteger(valueString);
		}
		catch (Exception exception)
		{
			return defaultValue;
		}
	}

	/**
	 * Get the optional BigDecimal value associated with an index. The
	 * defaultValue is returned if there is no value for the index, or if the
	 * value is not a number and cannot be converted to a number.
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default value.
	 *
	 * @return the value.
	 */
	public BigDecimal optBigDecimal(int index, BigDecimal defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof BigDecimal)
		{
			return (BigDecimal) value;
		}
		if (value instanceof BigInteger)
		{
			return new BigDecimal((BigInteger) value);
		}
		if (value instanceof Double || value instanceof Float)
		{
			return new BigDecimal(((Number) value).doubleValue());
		}
		if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte)
		{
			return new BigDecimal(((Number) value).longValue());
		}
		try
		{
			return new BigDecimal(value.toString());
		}
		catch (Exception exception)
		{
			return defaultValue;
		}
	}

	/**
	 * Get the optional JSONArray associated with an index.
	 *
	 * @param index subscript.
	 *
	 * @return a JSONArray value, or null if the index has no value, or if the
	 * value is not a JSONArray.
	 */
	public JSONArray optJSONArray(int index)
	{
		Object object = this.opt(index);
		return object instanceof JSONArray ? (JSONArray) object : null;
	}

	/**
	 * Get the optional JSONObject associated with an index. Null is returned if
	 * the key is not found, or null if the index has no value, or if the value
	 * is not a JSONObject.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return a JSONObject value.
	 */
	public JSONObject optJSONObject(int index)
	{
		Object object = this.opt(index);
		return object instanceof JSONObject ? (JSONObject) object : null;
	}

	/**
	 * Get the optional long value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return the value.
	 */
	public long optLong(int index)
	{
		return this.optLong(index, 0);
	}

	/**
	 * Get the optional long value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default value.
	 *
	 * @return the value.
	 */
	public long optLong(int index, long defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof Number)
		{
			return ((Number) value).longValue();
		}

		if (value instanceof String)
		{
			try
			{
				return new BigDecimal(value.toString()).longValue();
			}
			catch (Exception exception)
			{
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get an optional {@link Number} value associated with a key, or <code>null</code>
	 * if there is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number ({@link BigDecimal}). This method
	 * would be used in cases where type coercion of the number value is unwanted.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return an object which is the value.
	 */
	public Number optNumber(int index)
	{
		return this.optNumber(index, null);
	}

	/**
	 * Get an optional {@link Number} value associated with a key, or the default if there
	 * is no such key or if the value is not a number. If the value is a string,
	 * an attempt will be made to evaluate it as a number ({@link BigDecimal}). This method
	 * would be used in cases where type coercion of the number value is unwanted.
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default value.
	 *
	 * @return an object which is the value.
	 */
	public Number optNumber(int index, Number defaultValue)
	{
		Object value = this.opt(index);
		if (JSONObject.NULL.equals(value))
		{
			return defaultValue;
		}
		if (value instanceof Number)
		{
			return (Number) value;
		}
		if (value instanceof String)
		{
			try
			{
				return JSONObject.stringToNumber((String) value);
			}
			catch (Exception exception)
			{
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
	 * Get the optional string value associated with an index. It returns an
	 * empty string if there is no value at that index. If the value is not a
	 * string and is not null, then it is converted to a string.
	 *
	 * @param index the index must be between 0 and length() - 1.
	 *
	 * @return a String value.
	 */
	public String optString(int index)
	{
		return this.optString(index, "");
	}

	/**
	 * Get the optional string associated with an index. The defaultValue is
	 * returned if the key is not found.
	 *
	 * @param index        the index must be between 0 and length() - 1.
	 * @param defaultValue the default value.
	 *
	 * @return a String value.
	 */
	public String optString(int index, String defaultValue)
	{
		Object object = this.opt(index);
		return JSONObject.NULL.equals(object) ? defaultValue : object.toString();
	}

	/**
	 * Append a boolean value. This increases the array's length by one.
	 *
	 * @param value a boolean value.
	 *
	 * @return this.
	 */
	public JSONArray put(boolean value)
	{
		return this.put(value ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 *
	 * @param value a Collection value.
	 *
	 * @return this.
	 *
	 * @throws JSONException If the value is non-finite number.
	 */
	public JSONArray put(Collection<?> value)
	{
		return this.put(new JSONArray(value));
	}

	/**
	 * Append a double value. This increases the array's length by one.
	 *
	 * @param value a double value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the value is not finite.
	 */
	public JSONArray put(double value) throws JSONException
	{
		return this.put(Double.valueOf(value));
	}

	/**
	 * Append a float value. This increases the array's length by one.
	 *
	 * @param value a float value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the value is not finite.
	 */
	public JSONArray put(float value) throws JSONException
	{
		return this.put(Float.valueOf(value));
	}

	/**
	 * Append an int value. This increases the array's length by one.
	 *
	 * @param value an int value.
	 *
	 * @return this.
	 */
	public JSONArray put(int value)
	{
		return this.put(Integer.valueOf(value));
	}

	/**
	 * Append an long value. This increases the array's length by one.
	 *
	 * @param value a long value.
	 *
	 * @return this.
	 */
	public JSONArray put(long value)
	{
		return this.put(Long.valueOf(value));
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 *
	 * @param value a Map value.
	 *
	 * @return this.
	 *
	 * @throws JSONException        if a value in the map is a non-finite number.
	 * @throws NullPointerException if a key in the map is <code>null</code>.
	 */
	public JSONArray put(Map<?, ?> value)
	{
		return this.put(new JSONObject(value));
	}

	/**
	 * Append an object value. This increases the array's length by one.
	 *
	 * @param value an object value. The value should be a Boolean, Double,
	 *              Integer, JSONArray, JSONObject, Long, or String, or the
	 *              JSONObject.NULL object.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the value is a non-finite number.
	 */
	public JSONArray put(Object value)
	{
		JSONObject.testValidity(value);
		this.properties.add(value);
		return this;
	}

	/**
	 * Put or replace a boolean value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 *
	 * @param index the subscript.
	 * @param value a boolean value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the index is negative.
	 */
	public JSONArray put(int index, boolean value) throws JSONException
	{
		return this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 *
	 * @param index the subscript.
	 * @param value a Collection value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the index is negative or if the value is non-finite.
	 */
	public JSONArray put(int index, Collection<?> value) throws JSONException
	{
		return this.put(index, new JSONArray(value));
	}

	/**
	 * Put or replace a double value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 *
	 * @param index The subscript.
	 * @param value A double value.
	 *
	 * @return this.
	 *
	 * @throws JSONException If the index is negative or if the value is non-finite.
	 */
	public JSONArray put(int index, double value) throws JSONException
	{
		return this.put(index, Double.valueOf(value));
	}

	/**
	 * Put or replace a float value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 *
	 * @param index the subscript.
	 * @param value a float value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the index is negative or if the value is non-finite.
	 */
	public JSONArray put(int index, float value) throws JSONException
	{
		return this.put(index, Float.valueOf(value));
	}

	/**
	 * Put or replace an int value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 *
	 * @param index the subscript.
	 * @param value an int value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the index is negative.
	 */
	public JSONArray put(int index, int value) throws JSONException
	{
		return this.put(index, Integer.valueOf(value));
	}

	/**
	 * Put or replace a long value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 *
	 * @param index the subscript.
	 * @param value a long value.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the index is negative.
	 */
	public JSONArray put(int index, long value) throws JSONException
	{
		return this.put(index, Long.valueOf(value));
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject that
	 * is produced from a Map.
	 *
	 * @param index the subscript.
	 * @param value the Map value.
	 *
	 * @return this.
	 *
	 * @throws JSONException        if the index is negative or if the the value is an invalid
	 *                              number.
	 * @throws NullPointerException if a key in the map is <code>null</code>
	 */
	public JSONArray put(int index, Map<?, ?> value) throws JSONException
	{
		this.put(index, new JSONObject(value));
		return this;
	}

	/**
	 * Put or replace an object value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 *
	 * @param index the subscript.
	 * @param value the value to put into the array. The value should be a
	 *              Boolean, Double, Integer, JSONArray, JSONObject, Long, or
	 *              String, or the JSONObject.NULL object.
	 *
	 * @return this.
	 *
	 * @throws JSONException if the index is negative or if the the value is an invalid
	 *                       number.
	 */
	public JSONArray put(int index, Object value) throws JSONException
	{
		if (index < 0)
		{
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		if (index < this.length())
		{
			JSONObject.testValidity(value);
			this.properties.set(index, value);
			return this;
		}
		if (index == this.length())
		{
			// simple append
			return this.put(value);
		}
		// if we are inserting past the length, we want to grow the array all at once
		// instead of incrementally.
		this.properties.ensureCapacity(index + 1);
		while (index != this.length())
		{
			// we don't need to test validity of NULL objects
			this.properties.add(JSONObject.NULL);
		}
		return this.put(value);
	}

	/**
	 * Creates a JSONPointer using an initialization string and tries to
	 * match it to an item within this JSONArray. For example, given a
	 * JSONArray initialized with this document:
	 * <pre>
	 * [
	 *     {"b":"c"}
	 * ]
	 * </pre>
	 * and this JSONPointer string:
	 * <pre>
	 * "/0/b"
	 * </pre>
	 * Then this method will return the String "c"
	 * A JSONPointerException may be thrown from code called by this method.
	 *
	 * @param pointer string that can be used to create a JSONPointer.
	 *
	 * @return the item matched by the JSONPointer, otherwise null.
	 */
	public Object query(String pointer)
	{
		return this.query(new JSONPointer(pointer));
	}

	/**
	 * Uses a uaer initialized JSONPointer  and tries to
	 * match it to an item whithin this JSONArray. For example, given a
	 * JSONArray initialized with this document:
	 * <pre>
	 * [
	 *     {"b":"c"}
	 * ]
	 * </pre>
	 * and this JSONPointer:
	 * <pre>
	 * "/0/b"
	 * </pre>
	 * Then this method will return the String "c"
	 * A JSONPointerException may be thrown from code called by this method.
	 *
	 * @param pointer string that can be used to create a JSONPointer.
	 *
	 * @return the item matched by the JSONPointer, otherwise null.
	 */
	public Object query(JSONPointer pointer)
	{
		return pointer.queryFrom(this);
	}

	/**
	 * Queries and returns a value from this object using {@code jsonPointer}, or
	 * returns null if the query fails due to a missing key.
	 *
	 * @param pointer the string representation of the JSON pointer.
	 *
	 * @return the queried value or {@code null}.
	 *
	 * @throws IllegalArgumentException if {@code jsonPointer} has invalid syntax.
	 */
	public Object optQuery(String pointer)
	{
		return this.optQuery(new JSONPointer(pointer));
	}

	/**
	 * Queries and returns a value from this object using {@code jsonPointer}, or
	 * returns null if the query fails due to a missing key.
	 *
	 * @param pointer the JSON pointer.
	 *
	 * @return the queried value or {@code null}.
	 *
	 * @throws IllegalArgumentException if {@code pointer} has invalid syntax.
	 */
	public Object optQuery(JSONPointer pointer)
	{
		try
		{
			return pointer.queryFrom(this);
		}
		catch (JSONPointerException exception)
		{
			return null;
		}
	}

	/**
	 * Remove an index and close the hole.
	 *
	 * @param index the index of the element to be removed.
	 *
	 * @return the value that was associated with the index, or null if there
	 * was no value.
	 */
	public Object remove(int index)
	{
		return index >= 0 && index < this.length() ? this.properties.remove(index) : null;
	}

	/**
	 * Determine if two JSONArrays are similar.
	 * They must contain similar sequences.
	 *
	 * @param other the other JSONArray.
	 *
	 * @return true if they are equal, false otherwise.
	 */
	public boolean similar(Object other)
	{
		if (!(other instanceof JSONArray))
		{
			return false;
		}
		int length = this.length();
		if (length != ((JSONArray) other).length())
		{
			return false;
		}
		for (int index = 0; index < length; index += 1)
		{
			Object valueThis = this.properties.get(index);
			Object valueOther = ((JSONArray) other).properties.get(index);
			if (valueThis.equals(valueOther))
			{
				continue;
			}
			if (valueThis instanceof JSONObject)
			{
				if (!((JSONObject) valueThis).similar(valueOther))
				{
					return false;
				}
			}
			else if (valueThis instanceof JSONArray)
			{
				if (!((JSONArray) valueThis).similar(valueOther))
				{
					return false;
				}
			}
			else if (!valueThis.equals(valueOther))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Produce a JSONObject by combining a JSONArray of names with the values of
	 * this JSONArray.
	 *
	 * @param names a JSONArray containing a list of key strings. These will be
	 *              paired with the values.
	 *
	 * @return a JSONObject, or null if there are no names or if this JSONArray
	 * has no values.
	 *
	 * @throws JSONException if any of the names are null.
	 */
	public JSONObject toJSONObject(JSONArray names) throws JSONException
	{
		if (names == null || names.length() == 0 || this.length() == 0)
		{
			return null;
		}
		JSONObject object = new JSONObject(names.length());
		for (int index = 0; index < names.length(); index++)
		{
			object.put(names.getString(index), this.opt(index));
		}
		return object;
	}

	/**
	 * Make a JSON text of this JSONArray. For compactness, no unnecessary
	 * whitespace is added. If it is not possible to produce a syntactically
	 * correct JSON text then null will be returned instead. This could occur if
	 * the array contains an invalid number.
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @return a printable, displayable, transmittable representation of the
	 * array.
	 */
	@Override
	public String toString()
	{
		try
		{
			return this.toString(0);
		}
		catch (Exception exception)
		{
			return null;
		}
	}

	/**
	 * Make a pretty-printed JSON text of this JSONArray.
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONArray} has only
	 * one element, then the array will be output on a single line:
	 * <pre>{@code [1]}</pre>
	 * <p>If an array has 2 or more elements, then it will be output across
	 * multiple lines: <pre>{@code
	 * [
	 * 1,
	 * "value 2",
	 * 3
	 * ]
	 * }</pre>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @param indentFactor the number of spaces to add to each level of indentation.
	 *
	 * @return a printable, displayable, transmittable representation of the
	 * object, beginning with <code>[</code>&nbsp;<small>(left
	 * bracket)</small> and ending with <code>]</code>
	 * &nbsp;<small>(right bracket)</small>.
	 */
	public String toString(int indentFactor) throws JSONException
	{
		try (StringWriter writer = new StringWriter())
		{
			synchronized (writer.getBuffer())
			{
				return this.write(writer, indentFactor, 0).toString();
			}
		}
		catch (IOException exception)
		{
			return "";
		}
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @return the writer.
	 */
	public Writer write(Writer writer) throws JSONException
	{
		return this.write(writer, 0, 0);
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer.
	 * <p>If <code>indentFactor > 0</code> and the {@link JSONArray} has only
	 * one element, then the array will be output on a single line:
	 * <pre>{@code [1]}</pre>
	 * <p>If an array has 2 or more elements, then it will be output across
	 * multiple lines: <pre>{@code
	 * [
	 * 1,
	 * "value 2",
	 * 3
	 * ]
	 * }</pre>
	 * <p><b>
	 * Warning: This method assumes that the data structure is acyclical.
	 * </b>
	 *
	 * @param writer       writes the serialized JSON.
	 * @param indentFactor the number of spaces to add to each level of indentation.
	 * @param indent       the indentation of the top level.
	 *
	 * @return the writer.
	 */
	public Writer write(Writer writer, int indentFactor, int indent) throws JSONException
	{
		try
		{
			boolean commanate = false;
			int length = this.length();
			writer.write('[');
			if (length == 1)
			{
				try
				{
					JSONObject.writeValue(writer, this.properties.get(0), indentFactor, indent);
				}
				catch (Exception exception)
				{
					throw new JSONException("Unable to write JSONArray value at index: 0", exception);
				}
			}
			else if (length != 0)
			{
				final int newIndent = indent + indentFactor;
				for (int index = 0; index < length; index += 1)
				{
					if (commanate)
					{
						writer.write(',');
					}
					if (indentFactor > 0)
					{
						writer.write('\n');
					}
					JSONObject.indent(writer, newIndent);
					try
					{
						JSONObject.writeValue(writer, this.properties.get(index), indentFactor, newIndent);
					}
					catch (Exception exception)
					{
						throw new JSONException("Unable to write JSONArray value at index: " + index, exception);
					}
					commanate = true;
				}
				if (indentFactor > 0)
				{
					writer.write('\n');
				}
				JSONObject.indent(writer, indent);
			}
			writer.write(']');
			return writer;
		}
		catch (IOException exception)
		{
			throw new JSONException(exception);
		}
	}

	/**
	 * Returns a java.util.List containing all of the elements in this array.
	 * If an element in the array is a JSONArray or JSONObject it will also
	 * be converted.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a java.util.List containing the elements of this array.
	 */
	public List<Object> toList()
	{
		List<Object> results = new ArrayList<>(this.properties.size());
		for (Object element : this.properties)
		{
			if (element == null || JSONObject.NULL.equals(element))
			{
				results.add(null);
			}
			else if (element instanceof JSONArray)
			{
				results.add(((JSONArray) element).toList());
			}
			else if (element instanceof JSONObject)
			{
				results.add(((JSONObject) element).toMap());
			}
			else
			{
				results.add(element);
			}
		}
		return results;
	}
}