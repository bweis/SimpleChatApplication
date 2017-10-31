package chatapplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ChatFilter {
    private final String badWordsFileName;
    private List<String> bannedWords;

    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;
        if (badWordsFileName != null) {
            try {
                bannedWords = Files.readAllLines(Paths.get(badWordsFileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Banned Words: " + bannedWords);
        }
    }

    public String filter(String msg) {
        if (badWordsFileName == null) {
            return msg;
        } else {
            for (String badWord : bannedWords) {
                if (msg.toLowerCase().contains(badWord.toLowerCase())) {
                    msg = msg.replaceAll("(?i)" + badWord, asteriskGen(badWord.length()));
                }
            }
            return msg;
        }
    }

    private String asteriskGen(int length) {
        if (length > 0) {
            char[] array = new char[length];
            Arrays.fill(array, '*');
            return new String(array);
        }
        return "";
    }
}
