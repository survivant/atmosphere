package org.atmosphere.sockjs.transport2;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.JsonWriteContext;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

/**
 * 
 * SockJS requires a special JSON codec - it requires that many other characters, over and above what is required by the JSON spec are escaped. To satisfy this we escape any character that escapable with short escapes and any other non ASCII character we unicode escape it
 * 
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JsonCodec {

	private final static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();

		// By default, Jackson does not escape unicode characters in JSON
		// strings
		// This should be ok, since a valid JSON string can contain unescaped
		// JSON
		// characters.
		// However, SockJS requires that many unicode chars are escaped. This
		// may
		// be due to browsers barfing over certain unescaped characters
		// So... when encoding strings we make sure all unicode chars are
		// escaped

		// This code adapted from
		// http://wiki.fasterxml.com/JacksonSampleQuoteChars
		CustomSerializerFactory serializerFactory = new CustomSerializerFactory();
		serializerFactory.addSpecificMapping(String.class, new JsonSerializer<String>() {
			final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
			final int[] ESCAPE_CODES = null;// CharTypes.get7BitOutputEscapes();

			private void writeUnicodeEscape(JsonGenerator gen, char c) throws IOException {
				gen.writeRaw('\\');
				gen.writeRaw('u');
				gen.writeRaw(HEX_CHARS[(c >> 12) & 0xF]);
				gen.writeRaw(HEX_CHARS[(c >> 8) & 0xF]);
				gen.writeRaw(HEX_CHARS[(c >> 4) & 0xF]);
				gen.writeRaw(HEX_CHARS[c & 0xF]);
			}

			private void writeShortEscape(JsonGenerator gen, char c) throws IOException {
				gen.writeRaw('\\');
				gen.writeRaw(c);
			}

			@Override
			public void serialize(String str, JsonGenerator gen, SerializerProvider provider) throws IOException {
				int status = ((JsonWriteContext) gen.getOutputContext()).writeValue();
				switch (status) {
				case JsonWriteContext.STATUS_OK_AFTER_COLON:
					gen.writeRaw(':');
					break;
				case JsonWriteContext.STATUS_OK_AFTER_COMMA:
					gen.writeRaw(',');
					break;
				case JsonWriteContext.STATUS_EXPECT_NAME:
					throw new JsonGenerationException("Can not write string value here");
				}
				gen.writeRaw('"');
				for (char c : str.toCharArray()) {
					if (c >= 0x80)
						writeUnicodeEscape(gen, c); // use generic
													// escaping for all
													// non US-ASCII
													// characters
					else {
						// use escape table for first 128 characters
						int code = (c < ESCAPE_CODES.length ? ESCAPE_CODES[c] : 0);
						if (code == 0)
							gen.writeRaw(c); // no escaping
						else if (code == -1)
							writeUnicodeEscape(gen, c); // generic
														// escaping
						else
							writeShortEscape(gen, (char) code); // short
																// escaping
																// (\n
																// \t
																// ...)
					}
				}
				gen.writeRaw('"');
			}
		});
		mapper.setSerializerFactory(serializerFactory);
	}

	public static String encode(Object obj) throws Exception {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new Exception("Failed to encode as JSON");
		}
	}

	public static Object decodeValue(String str, Class<?> clazz) throws Exception {
		try {
			return mapper.readValue(str, clazz);
		} catch (Exception e) {
			throw new Exception("Failed to decode");
		}
	}
}
