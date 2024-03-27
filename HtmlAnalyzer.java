import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            } else {
                System.err.println("Malformed HTML or no text content available.");
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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return extractDeepestText(reader);
        }
    }

    /**
     * Extracts and returns the text from the deepest HTML tag in the content read
     * from the BufferedReader.
     *
     * @param reader BufferedReader for reading the HTML content.
     * @return The deepest text found, or an empty string if none is found.
     * @throws IOException If an input or output exception occurs.
     */
    private static String extractDeepestText(BufferedReader reader) throws IOException {
        String inputLine;
        int currentLevel = 0, deepestLevel = 0;
        String deepestText = "";

        while ((inputLine = reader.readLine()) != null) {
            String trimmedLine = inputLine.trim();

            if (!trimmedLine.isEmpty()) {
                if (trimmedLine.startsWith("<")) {
                    if (trimmedLine.startsWith("</")) {
                        currentLevel--;
                    } else if (!trimmedLine.endsWith("/>")) { // Ignore self-closing tags
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
}
