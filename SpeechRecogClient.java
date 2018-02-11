package speechRecognition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import com.eclipsesource.json.Json;
import speechRecognition.SpeechAPI.*;

public class SpeechRecogClient {
	
	private static final String REQUEST_URI = "https://speech.platform.bing.com/speech/recognition/%s/cognitiveservices/v1";
	private static final String PARAMETERS = "language=%s&format=%s";
	
	private RecognitionMode mode = RecognitionMode.Interactive;
	private Language language = Language.en_US;
	private OutputFormat format = OutputFormat.Simple;
	
    private URL buildRequestURL() throws MalformedURLException {
        String url = String.format(REQUEST_URI, mode.name().toLowerCase());
        String params = String.format(PARAMETERS, language.name().replace('_', '-'), format.name().toLowerCase());
        return new URL(String.format("%s?%s", url, params));
//        return new URL("https://speech.platform.bing.com/speech/recognition/dictation/cognitiveservices/v1?language=en-US");
    }

    private HttpURLConnection connect() throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) buildRequestURL().openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true); 
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-type", "audio/wav; codec=\"audio/pcm\"; samplerate=16000");
        connection.setRequestProperty("Accept", "application/json;text/xml");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", "1d2e4ef44b5842a9897cefa570aa9cdd");
//        connection.setRequestProperty("Host", "speech.platform.bing.com");
        connection.setChunkedStreamingMode(1024); // 0 == default chunk size
        connection.connect();

        return connection;
    }
    
    private String getResponse(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
          throw new RuntimeException(String.format("Something went wrong, server returned: %d (%s)",
              connection.getResponseCode(), connection.getResponseMessage()));
        }

        try (BufferedReader reader = 
            new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
          String jsonResult = reader.lines().collect(Collectors.joining());
          return Json.parse(jsonResult).asObject().get("DisplayText").asString();
        }
    }
    
    private HttpURLConnection upload(InputStream is, HttpURLConnection connection) throws IOException {
        try (OutputStream output = connection.getOutputStream()) {
          byte[] buffer = new byte[1024];
          int length;
          while ((length = is.read(buffer)) != -1) {
            output.write(buffer, 0, length);
          }
          output.flush();
        }
        return connection;
    }

    private HttpURLConnection upload(Path filepath, HttpURLConnection connection) throws IOException {
    	try (OutputStream output = connection.getOutputStream()) {
          Files.copy(filepath, output);
        }
        return connection;
    }

    public String process(InputStream is) throws IOException {
        return getResponse(upload(is, connect()));
    }

    public String process(Path filepath) throws IOException {
        return getResponse(upload(filepath, connect()));
    }
   
}