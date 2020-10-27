package dev.m00nl1ght.clockwork.config;

import dev.m00nl1ght.clockwork.config.ImmutableConfig.Builder;

import java.util.*;

public class SimpleDataParser {

    public static final Segment<String> DEFAULT_STRING = new StringSegment('"', '"');
    public static final Segment<Config> DEFAULT_CONFIG = new ConfigSegment(DEFAULT_STRING, '{', '}', '=', ',');
    public static final Segment<List<String>> DEFAULT_STRING_LIST = new StringListSegment(DEFAULT_STRING, '[', ']', ',');
    public static final Segment<List<Config>> DEFAULT_CONFIG_LIST = new ConfigListSegment(DEFAULT_CONFIG, '[', ']', ',');
    public static final Format DEFAULT_FORMAT = new Format(DEFAULT_STRING, DEFAULT_CONFIG, DEFAULT_STRING_LIST, DEFAULT_CONFIG_LIST);

    public static <T> T parse(Segment<T> segment, String input) {
        final var parser = new SimpleDataParser(input.strip(), DEFAULT_FORMAT);
        final var result = segment.consume(parser);
        if (result == null) return null;
        if (parser.tryAdvance()) throw parser.errorUnexpected();
        return result;
    }

    public final String input;
    public final Format format;

    private int pos = 0;

    public SimpleDataParser(String input, Format format) {
        this.input = Objects.requireNonNull(input);
        this.format = Objects.requireNonNull(format);
    }

    public int position() {
        return pos;
    }

    public char get() {
        return input.charAt(pos);
    }

    public char next() {
        if (tryAdvance()) return get(); else throw errorEOI();
    }

    public boolean tryAdvance() {
        while (pos < input.length() - 1) {
            pos++;
            if (!Character.isWhitespace(input.charAt(pos))) {
                return true;
            }
        }
        return false;
    }

    public RuntimeException errorEOI() {
        return new RuntimeException("Unexpectedly reached end of input: " + input);
    }

    public RuntimeException errorUnexpected() {
        return new RuntimeException("Unexpected '" + input.charAt(pos) + "' at position " + pos + " in: " + input);
    }

    public RuntimeException errorInvalid() {
        return new RuntimeException("Failed to parse segment starting at position " + pos + " in: " + input);
    }

    public static class Format {

        private final Set<Segment<?>> segments;
        private final Character[] specialChars;

        public Format(Segment<?>... segments) {
            this(Set.of(segments));
        }

        public Format(Set<Segment<?>> segments) {
            this.segments = Set.copyOf(segments);
            this.specialChars = segments.stream()
                    .map(Segment::getSpecialChars)
                    .flatMap(Collection::stream)
                    .distinct().toArray(Character[]::new);
        }

        @SuppressWarnings("Convert2streamapi")
        public boolean isSpecialChar(char c) {
            for (int i = 0; i < specialChars.length; i++)
                if (specialChars[i] == c) return true;
            return false;
        }

        public Set<Segment<?>> getSegments() {
            return segments;
        }

    }

    public interface Segment<T> {

        T consume(SimpleDataParser parser);

        void applyToBuilder(T parsed, String key, Builder builder);

        Collection<Character> getSpecialChars();

    }

    public static class StringSegment implements Segment<String> {

        public final char QUOTE_START;
        public final char QUOTE_END;

        public StringSegment(char quoteStart, char quoteEnd) {
            this.QUOTE_START = quoteStart;
            this.QUOTE_END = quoteEnd;
        }

        @Override
        public String consume(SimpleDataParser parser) {
            if (parser.get() == QUOTE_START) {
                for (int i = parser.pos + 1; i < parser.input.length(); i++) {
                    if (parser.input.charAt(i) == QUOTE_END) {
                        final var str = parser.input.substring(parser.pos + 1, i).strip();
                        parser.pos = i; return str;
                    }
                }
                throw parser.errorEOI();
            } else {
                if (parser.format.isSpecialChar(parser.get())) return null;
                for (int i = parser.pos + 1; i < parser.input.length(); i++) {
                    if (parser.format.isSpecialChar(parser.input.charAt(i))) {
                        final var str = parser.input.substring(parser.pos, i).strip();
                        parser.pos = i - 1; return str;
                    }
                }
                parser.pos = parser.input.length() - 1;
                return parser.input.substring(parser.pos).strip();
            }
        }

        @Override
        public void applyToBuilder(String parsed, String key, Builder builder) {
            builder.putString(key, parsed);
        }

        @Override
        public Collection<Character> getSpecialChars() {
            return List.of(QUOTE_START, QUOTE_END);
        }

    }

    public static class ConfigSegment implements Segment<Config> {

        public final Segment<String> keySegment;

        public final char TAG_START;
        public final char TAG_END;
        public final char MAPPING;
        public final char DELIMETER;

        public ConfigSegment(Segment<String> keySegment, char tagStart, char tagEnd, char mapping, char delimeter) {
            this.keySegment = keySegment;
            this.TAG_START = tagStart;
            this.TAG_END = tagEnd;
            this.MAPPING = mapping;
            this.DELIMETER = delimeter;
        }

        @Override
        public Config consume(SimpleDataParser parser) {
            if (parser.get() != TAG_START) return null;
            final var builder = ImmutableConfig.builder();
            ploop: while (parser.tryAdvance()) {
                final var key = keySegment.consume(parser);
                if (key == null || parser.next() != MAPPING) throw parser.errorUnexpected();
                if (!parser.tryAdvance()) throw parser.errorEOI();
                for (final var segment : parser.format.segments) {
                    if (tryConsumeValue(parser, builder, segment, key)) {
                        final var next = parser.next();
                        if (next == DELIMETER) continue ploop;
                        if (next != TAG_END) throw parser.errorUnexpected();
                        return builder.build();
                    }
                }
                throw parser.errorInvalid();
            }
            throw parser.errorEOI();
        }

        protected <T> boolean tryConsumeValue(SimpleDataParser parser, Builder builder, Segment<T> segment, String key) {
            final var prev = parser.pos;
            final var value = segment.consume(parser);
            if (value == null) {
                parser.pos = prev;
                return false;
            } else {
                segment.applyToBuilder(value, key, builder);
                return true;
            }
        }

        @Override
        public void applyToBuilder(Config parsed, String key, Builder builder) {
            builder.putSubconfig(key, parsed);
        }

        @Override
        public Collection<Character> getSpecialChars() {
            return List.of(TAG_START, TAG_END, MAPPING, DELIMETER);
        }

    }

    public static abstract class ListSegment<T> implements Segment<List<T>> {

        public final Segment<T> elementSegment;

        public final char LIST_START;
        public final char LIST_END;
        public final char DELIMETER;

        protected ListSegment(Segment<T> elementSegment, char listStart, char listEnd, char delimeter) {
            this.elementSegment = elementSegment;
            this.LIST_START = listStart;
            this.LIST_END = listEnd;
            this.DELIMETER = delimeter;
        }

        @Override
        public List<T> consume(SimpleDataParser parser) {
            if (parser.get() != LIST_START) return null;
            final var list = new ArrayList<T>();
            while (parser.tryAdvance()) {
                final var element = elementSegment.consume(parser);
                if (element == null) return null;
                list.add(element);
                final var next = parser.next();
                if (next == DELIMETER) continue;
                if (next != LIST_END) throw parser.errorUnexpected();
                return list;
            }
            throw parser.errorEOI();
        }

        @Override
        public Collection<Character> getSpecialChars() {
            return List.of(LIST_START, LIST_END, DELIMETER);
        }

    }

    public static class StringListSegment extends ListSegment<String> {

        public StringListSegment(Segment<String> elementSegment, char listStart, char listEnd, char delimeter) {
            super(elementSegment, listStart, listEnd, delimeter);
        }

        @Override
        public void applyToBuilder(List<String> parsed, String key, Builder builder) {
            builder.putStrings(key, parsed);
        }

    }

    public static class ConfigListSegment extends ListSegment<Config> {

        public ConfigListSegment(Segment<Config> elementSegment, char listStart, char listEnd, char delimeter) {
            super(elementSegment, listStart, listEnd, delimeter);
        }

        @Override
        public void applyToBuilder(List<Config> parsed, String key, Builder builder) {
            builder.putSubconfigs(key, parsed);
        }

    }

}
