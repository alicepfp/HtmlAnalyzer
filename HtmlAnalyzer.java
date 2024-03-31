import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;


/**
 * Analyzes HTML content from a given URL to find and display the text within
 * the deepest HTML tag.
 */
public class HtmlAnalyzer {
    /**
     * Main method that takes a URL as input and processes the HTML content.
     *
     * @param args Command line arguments, expects a single URL.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java HtmlAnalyzer <url>");
            return;
        }

        try {
            String deepestText = findDeepestText(args[0]);
            if (!deepestText.isEmpty()) {
                System.out.println(deepestText);
            }
        } catch (IOException e) {
            System.err.println("Failed to connect to URL: " + e.getMessage());
        }
    }

    /**
     * Connects to the specified URL and returns the text from the deepest HTML tag.
     *
     * @param urlString The URL as a string.
     * @return The deepest text found, or an empty string if none is found or in
     *         case of malformed HTML.
     * @throws IOException If an input or output exception occurs.
     */
    private static String findDeepestText(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("URL connection error with response code: " + responseCode);
        }

        String htmlString;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            htmlString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        boolean isValidHtml = isValidHtml(htmlString);
        if(!isValidHtml) {
            return "";
        }
        return extractDeepestText(htmlString);
    }

    /**
    * Extracts the deepest text from an HTML string.
    *
    * @param html the HTML string
    * @return the deepest text, or an empty string if no text is found
    */
    public static String extractDeepestText(String html) {
        String[] lines = html.split("\n");
        int currentLevel = 0, deepestLevel = 0;
        String deepestText = "";

        for (String inputLine : lines) {
            String trimmedLine = inputLine.trim();

            if (!trimmedLine.isEmpty()) {
                if (trimmedLine.startsWith("<")) {
                    if (trimmedLine.startsWith("</")) {
                        currentLevel--;
                    } else if (!trimmedLine.endsWith("/>")) {
                        currentLevel++;
                    }
                } else if (currentLevel > deepestLevel) {
                    deepestLevel = currentLevel;
                    deepestText = trimmedLine;
                }
            }
        }

        return deepestText;
    }

    /**
    * Checks if the given HTML is well-formed.
    *  
    * @param html the HTML to check
    * @return true if the HTML is well-formed, false otherwise
    */
    public static boolean isValidHtml(String html) {
        Deque<String> stack = new ArrayDeque<>();
        for (int i = 0; i < html.length(); i++) {
            if (html.charAt(i) == '<') { // Start of a tag
                int end = html.indexOf('>', i);
                if (end == -1) {
                    System.err.println("Malformed HTML.");
                    return false;
                }
                String tag = html.substring(i + 1, end).trim();
                if (!tag.isEmpty()) {
                    if (tag.charAt(0) == '/') { // Closing tag
                        String tagName = tag.substring(1).trim();
                        if (stack.isEmpty() || !stack.pop().equals(tagName)) {
                            System.err.println("Malformed HTML.");
                            return false;
                        }
                    } else if (!tag.endsWith("/")) { // Opening tag, ignoring self-closing tags
                        String tagName = tag.contains(" ") ? tag.substring(0, tag.indexOf(' ')).trim() : tag;
                        stack.push(tagName);
                    }
                }
                i = end;
            }
        }

        if (!stack.isEmpty()) {
            System.err.println("Malformed HTML.");
            return false;
        }
        return true;
    }
}
