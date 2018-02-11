package speechRecognition;

import java.nio.file.Paths;
import speechRecognition.SpeechRecogClient;
import java.nio.file.Path;

public class Example {
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		Path filePath = Paths.get("C:\\Users\\Chengyu\\Downloads\\11.wav");
//		Path filePath = Paths.get("C:\\Users\\Chengyu\\Documents\\Sound recordings\\Recording1.wav");
//		Path filePath = Paths.get(args[0]);
		SpeechRecogClient client = new SpeechRecogClient();
//		InputStream input = new FileInputStream(Paths.get(filePath).toFile());
		String recogResult = client.process(filePath);
		System.out.println(recogResult);
		long stop = System.currentTimeMillis();
		System.out.println(stop - start);
	}
}