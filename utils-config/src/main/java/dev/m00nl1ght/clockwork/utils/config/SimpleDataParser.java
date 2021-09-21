package dev.m00nl1ght.clockwork.utils.config;

import dev.m00nl1ght.clockwork.utils.config.impl.ModifiableConfigImpl;

import java.util.*;

public class SimpleDataParser {

    public static final Segment<String> DEFAULT_STRING = new StringSegment('"', '"');
    public static final Segment<ModifiableConfig> DEFAULT_CONFIG = new ConfigSegment(DEFAULT_STRING, '{', '}', '=', ',');
    public static final Segment<List<String>> DEFAULT_STRING_LIST = new StringListSegment(DEFAULT_STRING, '[', ']', ',');
    public static final Segment<List<ModifiableConfig>> DEFAULT_CONFIG_LIST = new ConfigListSegment(DEFAULT_CONFIG, '[', ']', ',');
    public static final Format DEFAULT_FORMAT = new Format(DEFAULT_STRING, DEFAULT_CONFIG, DEFAULT_STRING_LIST, DEFAULT_CONFIG_LIST);

    public static <T> T parse(Segment<T> segment, String input) {
        return parse(DEFAULT_FORMAT, segment, input);
    }

    public static <T> T parse(Format format, Segment<T> segment, String input) {
        final var parser = new SimpleDataParser(input, format);
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

        @SuppressWarnings("unchecked")
        public <T> Optional<T> getSegment(Class<T> type) {
            return segments.stream().filter(type::isInstance).map(s -> (T) s).findFirst();
        }

    }

    public interface Segment<T> {

        T consume(SimpleDataParser parser);

        void applyToConfig(T parsed, String key, ModifiableConfig config);

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
                final var str = parser.input.substring(parser.pos).strip();
                parser.pos = parser.input.length() - 1;
                return str;
            }
        }

        @Override
        public void applyToConfig(String parsed, String key, ModifiableConfig config) {
            config.putString(key, parsed);
        }

        @Override
        public Collection<Character> getSpecialChars() {
            return List.of(QUOTE_START, QUOTE_END);
        }

    }

    public static class ConfigSegment implements Segment<ModifiableConfig> {

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
        public ModifiableConfig consume(SimpleDataParser parser) {
            if (parser.get() != TAG_START) return null;
            final var config = new ModifiableConfigImpl();
            ploop: while (parser.tryAdvance()) {
                final var key = keySegment.consume(parser);
                if (key == null || parser.next() != MAPPING) throw parser.errorUnexpected();
                if (!parser.tryAdvance()) throw parser.errorEOI();
                for (final var segment : parser.format.segments) {
                    if (tryConsumeValue(parser, config, segment, key)) {
                        final var next = parser.next();
                        if (next == DELIMETER) continue ploop;
                        if (next != TAG_END) throw parser.errorUnexpected();
                        return config;
                    }
                }
                throw parser.errorInvalid();
            }
            throw parser.errorEOI();
        }

        protected <T> boolean tryConsumeValue(SimpleDataParser parser, ModifiableConfig config, Segment<T> segment, String key) {
            final var prev = parser.pos;
            final var value = segment.consume(parser);
            if (value == null) {
                parser.pos = prev;
                return false;
            } else {
                segment.applyToConfig(value, key, config);
                return true;
            }
        }

        @Override
        public void applyToConfig(ModifiableConfig parsed, String key, ModifiableConfig config) {
            config.putSubconfig(key, parsed);
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
        public void applyToConfig(List<String> parsed, String key, ModifiableConfig config) {
            config.putStrings(key, parsed);
        }

    }

    public static class ConfigListSegment extends ListSegment<ModifiableConfig> {

        public ConfigListSegment(Segment<ModifiableConfig> elementSegment, char listStart, char listEnd, char delimeter) {
            super(elementSegment, listStart, listEnd, delimeter);
        }

        @Override
        public void applyToConfig(List<ModifiableConfig> parsed, String key, ModifiableConfig config) {
            config.putSubconfigs(key, parsed);
        }

    }

}
