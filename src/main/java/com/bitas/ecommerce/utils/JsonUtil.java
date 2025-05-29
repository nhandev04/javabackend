package com.bitas.ecommerce.utils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for simple JSON serialization and deserialization.
 * Provides basic methods to convert between Java objects and JSON strings.
 * This is a simplified implementation without external dependencies.
 */
public class JsonUtil {

    /**
     * Convert a Java object to a JSON string
     * 
     * @param object Object to convert
     * @return JSON string representation
     */
    public String toJson(Object object) {
        if (object == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        if (object instanceof Map) {
            mapToJson((Map<?, ?>) object, sb);
        } else if (object instanceof List) {
            listToJson((List<?>) object, sb);
        } else {
            // For custom objects like User, Product, etc.
            beanToJson(object, sb);
        }

        return sb.toString();
    }

    /**
     * Convert a JSON string to a Java object
     * 
     * @param <T> Type of the target object
     * @param json JSON string
     * @param clazz Class of the target object
     * @return Java object of type T
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            if (clazz == Map.class) {
                @SuppressWarnings("unchecked")
                T result = (T) parseJsonObject(json);
                return result;
            } else if (clazz == List.class) {
                @SuppressWarnings("unchecked")
                T result = (T) parseJsonArray(json);
                return result;
            } else {
                // For custom objects like User, Product, etc.
                return jsonToBean(json, clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert a Map to a JSON string
     * 
     * @param map Map to convert
     * @param sb StringBuilder to append to
     */
    private void mapToJson(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');

        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;

            String key = entry.getKey().toString();
            Object value = entry.getValue();

            sb.append('"').append(escapeJson(key)).append('"').append(':');

            appendValue(value, sb);
        }

        sb.append('}');
    }

    /**
     * Convert a List to a JSON string
     * 
     * @param list List to convert
     * @param sb StringBuilder to append to
     */
    private void listToJson(List<?> list, StringBuilder sb) {
        sb.append('[');

        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(',');
            }
            first = false;

            appendValue(item, sb);
        }

        sb.append(']');
    }

    /**
     * Append a value to the JSON string
     * 
     * @param value Value to append
     * @param sb StringBuilder to append to
     */
    private void appendValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append('"').append(escapeJson((String) value)).append('"');
        } else if (value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof LocalDateTime) {
            sb.append('"').append(((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append('"');
        } else if (value instanceof Map) {
            mapToJson((Map<?, ?>) value, sb);
        } else if (value instanceof List) {
            listToJson((List<?>) value, sb);
        } else {
            // For custom objects
            beanToJson(value, sb);
        }
    }

    /**
     * Convert a Java bean to a JSON string using reflection
     * 
     * @param bean Java bean to convert
     * @param sb StringBuilder to append to
     */
    private void beanToJson(Object bean, StringBuilder sb) {
        Map<String, Object> map = new HashMap<>();

        // Use reflection to get bean properties
        try {
            for (Method method : bean.getClass().getMethods()) {
                if (method.getName().startsWith("get") && !method.getName().equals("getClass")) {
                    String propertyName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                    Object value = method.invoke(bean);
                    map.put(propertyName, value);
                } else if (method.getName().startsWith("is") && method.getReturnType() == boolean.class) {
                    String propertyName = method.getName().substring(2, 3).toLowerCase() + method.getName().substring(3);
                    Object value = method.invoke(bean);
                    map.put(propertyName, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting bean to JSON: " + e.getMessage(), e);
        }

        mapToJson(map, sb);
    }

    /**
     * Escape special characters in a JSON string
     * 
     * @param input String to escape
     * @return Escaped string
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Parse a JSON object string into a Map
     * 
     * @param json JSON string
     * @return Map representation
     */
    private Map<String, Object> parseJsonObject(String json) {
        // This is a simplified parser for demonstration purposes
        // In a real application, you would use a proper JSON parser
        Map<String, Object> map = new HashMap<>();

        // Remove the outer braces and whitespace
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
        }

        // Split by commas, but not commas inside quotes or nested objects/arrays
        List<String> pairs = splitJsonPairs(json);

        for (String pair : pairs) {
            int colonPos = findUnquotedChar(pair, ':');
            if (colonPos > 0) {
                String key = pair.substring(0, colonPos).trim();
                String value = pair.substring(colonPos + 1).trim();

                // Remove quotes from key
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }

                map.put(key, parseJsonValue(value));
            }
        }

        return map;
    }

    /**
     * Parse a JSON array string into a List
     * 
     * @param json JSON string
     * @return List representation
     */
    private List<Object> parseJsonArray(String json) {
        // This is a simplified parser for demonstration purposes
        // In a real application, you would use a proper JSON parser
        List<Object> list = new ArrayList<>();

        // Remove the outer brackets and whitespace
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1).trim();
        }

        // Split by commas, but not commas inside quotes or nested objects/arrays
        List<String> elements = splitJsonElements(json);

        for (String element : elements) {
            list.add(parseJsonValue(element.trim()));
        }

        return list;
    }

    /**
     * Parse a JSON value string into a Java object
     * 
     * @param json JSON string
     * @return Java object
     */
    private Object parseJsonValue(String json) {
        json = json.trim();

        if (json.equals("null")) {
            return null;
        } else if (json.equals("true")) {
            return Boolean.TRUE;
        } else if (json.equals("false")) {
            return Boolean.FALSE;
        } else if (json.startsWith("\"") && json.endsWith("\"")) {
            // String value
            return json.substring(1, json.length() - 1);
        } else if (json.startsWith("{") && json.endsWith("}")) {
            // Object value
            return parseJsonObject(json);
        } else if (json.startsWith("[") && json.endsWith("]")) {
            // Array value
            return parseJsonArray(json);
        } else {
            // Number value
            try {
                if (json.contains(".")) {
                    return Double.parseDouble(json);
                } else {
                    return Long.parseLong(json);
                }
            } catch (NumberFormatException e) {
                return json;
            }
        }
    }

    /**
     * Split a JSON string into key-value pairs
     * 
     * @param json JSON string
     * @return List of key-value pair strings
     */
    private List<String> splitJsonPairs(String json) {
        List<String> pairs = new ArrayList<>();

        int start = 0;
        int braceCount = 0;
        int bracketCount = 0;
        boolean inQuotes = false;

        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);

            if (ch == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (ch == '{') {
                    braceCount++;
                } else if (ch == '}') {
                    braceCount--;
                } else if (ch == '[') {
                    bracketCount++;
                } else if (ch == ']') {
                    bracketCount--;
                } else if (ch == ',' && braceCount == 0 && bracketCount == 0) {
                    pairs.add(json.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }

        if (start < json.length()) {
            pairs.add(json.substring(start).trim());
        }

        return pairs;
    }

    /**
     * Split a JSON array string into elements
     * 
     * @param json JSON array string
     * @return List of element strings
     */
    private List<String> splitJsonElements(String json) {
        return splitJsonPairs(json);
    }

    /**
     * Find the position of a character in a string, ignoring characters inside quotes
     * 
     * @param str String to search
     * @param ch Character to find
     * @return Position of the character, or -1 if not found
     */
    private int findUnquotedChar(String str, char ch) {
        boolean inQuotes = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '"' && (i == 0 || str.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == ch && !inQuotes) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Convert a JSON string to a Java bean
     * 
     * @param <T> Type of the target bean
     * @param json JSON string
     * @param clazz Class of the target bean
     * @return Java bean of type T
     */
    private <T> T jsonToBean(String json, Class<T> clazz) {
        Map<String, Object> map = parseJsonObject(json);

        try {
            T bean = clazz.newInstance();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                String setterName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

                // Find the setter method
                for (Method method : clazz.getMethods()) {
                    if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                        Class<?> paramType = method.getParameterTypes()[0];

                        if (value == null) {
                            method.invoke(bean, (Object) null);
                        } else if (paramType == String.class && value instanceof String) {
                            method.invoke(bean, value);
                        } else if ((paramType == int.class || paramType == Integer.class) && value instanceof Number) {
                            method.invoke(bean, ((Number) value).intValue());
                        } else if ((paramType == long.class || paramType == Long.class) && value instanceof Number) {
                            method.invoke(bean, ((Number) value).longValue());
                        } else if ((paramType == double.class || paramType == Double.class) && value instanceof Number) {
                            method.invoke(bean, ((Number) value).doubleValue());
                        } else if ((paramType == boolean.class || paramType == Boolean.class) && value instanceof Boolean) {
                            method.invoke(bean, value);
                        } else if (paramType == BigDecimal.class && value instanceof Number) {
                            method.invoke(bean, new BigDecimal(value.toString()));
                        } else if (paramType == LocalDateTime.class && value instanceof String) {
                            method.invoke(bean, LocalDateTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        }
                        // Add more type conversions as needed

                        break;
                    }
                }
            }

            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to bean: " + e.getMessage(), e);
        }
    }
}
