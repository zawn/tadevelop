package com.tadevelop.sdk;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public final class ParseBenchmark extends SimpleBenchmark {

	@Param
	Document document;
	@Param
	Api api;

	private enum Document {

		TWEETS, READER_SHORT, READER_LONG;
	}

	private enum Api {

		// JACKSON_STREAM {
		// @Override
		// Parser newParser() {
		// return new JacksonStreamParser();
		// }
		// },
		// GSON_STREAM {
		// @Override
		// Parser newParser() {
		// return new GsonStreamParser();
		// }
		// },
		GSON_SKIP {
			@Override
			Parser newParser() {
				return new GsonSkipParser();
			}
		};

		abstract Parser newParser();
	}

	private char[] text;
	private Parser parser;

	@Override
	protected void setUp() throws Exception {
		text = resourceToString(document.name() + ".json").toCharArray();
		parser = api.newParser();
	}

	public void timeParse(int reps) throws Exception {
		for (int i = 0; i < reps; i++) {
			parser.parse(text, document);
		}
	}

	private static String resourceToString(String path) throws Exception {
		InputStream in = ParseBenchmark.class.getResourceAsStream(path);
		if (in == null) {
			throw new IllegalArgumentException("No such file: " + path);
		}

		Reader reader = new InputStreamReader(in, "UTF-8");
		char[] buffer = new char[8192];
		StringWriter writer = new StringWriter();
		int count;
		while ((count = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, count);
		}
		reader.close();
		return writer.toString();
	}

	public static void main(String[] args) throws Exception {
		Runner.main(ParseBenchmark.class, args);
	}

	interface Parser {

		void parse(char[] data, Document document) throws Exception;
	}

	private static class GsonStreamParser implements Parser {

		@Override
		public void parse(char[] data, Document document) throws Exception {
			android.gson.stream.JsonReader jsonReader = new android.gson.stream.JsonReader(new CharArrayReader(data));
			readToken(jsonReader);
			jsonReader.close();
		}

		private void readToken(android.gson.stream.JsonReader reader) throws IOException {
			while (true) {
				switch (reader.peek()) {
				case BEGIN_ARRAY:
					reader.beginArray();
					break;
				case END_ARRAY:
					reader.endArray();
					break;
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case NAME:
					reader.nextName();
					break;
				case BOOLEAN:
					reader.nextBoolean();
					break;
				case NULL:
					reader.nextNull();
					break;
				case NUMBER:
					reader.nextLong();
					break;
				case STRING:
					reader.nextString();
					break;
				case END_DOCUMENT:
					return;
				default:
					throw new IllegalArgumentException("Unexpected token" + reader.peek());
				}
			}
		}
	}

	private static class GsonSkipParser implements Parser {

		@Override
		public void parse(char[] data, Document document) throws Exception {
			android.gson.stream.JsonReader jsonReader = new android.gson.stream.JsonReader(new CharArrayReader(data));
			jsonReader.skipValue();
			jsonReader.close();
		}
	}

	private static class JacksonStreamParser implements Parser {

		@Override
		public void parse(char[] data, Document document) throws Exception {
			JsonFactory jsonFactory = new JsonFactory();
			com.fasterxml.jackson.core.JsonParser jp = jsonFactory.createJsonParser(new CharArrayReader(data));
			int depth = 0;
			do {
				switch (jp.nextToken()) {
				case START_OBJECT:
				case START_ARRAY:
					depth++;
					break;
				case END_OBJECT:
				case END_ARRAY:
					depth--;
					break;
				case FIELD_NAME:
					jp.getCurrentName();
					break;
				case VALUE_STRING:
					jp.getText();
					break;
				case VALUE_NUMBER_INT:
				case VALUE_NUMBER_FLOAT:
					jp.getLongValue();
					break;
				}
			} while (depth > 0);
			jp.close();
		}
	}
}
