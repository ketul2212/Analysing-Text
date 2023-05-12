package com.ketul.resource;

import com.ketul.dtos.Request;
import com.ketul.dtos.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@RestController
public class AnalysingTextResource {

    private List<String> words;

    public AnalysingTextResource() {
        // Load words from file on startup
        this.words = new ArrayList<>();
        try {
            File file = new File("words.txt");
            if (file.createNewFile()) {
                System.out.println("Words file created");
            } else {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String word = scanner.nextLine();
                    this.words.add(word);
                }
                scanner.close();
                System.out.println("Words file loaded");
            }
        } catch (IOException e) {
            System.err.println("Failed to load words file: " + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    public Response analyzeText(@RequestBody Request request) {
        String inputText = request.getText();
        Response response = new Response();
        response.setLexical(null);
        response.setValue(null);

        if (!words.isEmpty()) {
            // Find word with closest character value
            int minCharValueDiff = Integer.MAX_VALUE;
            String closestCharValueWord = null;
            for (String word : words) {
                int charValueDiff = Math.abs(getCharValue(inputText) - getCharValue(word));
                if (charValueDiff < minCharValueDiff) {
                    minCharValueDiff = charValueDiff;
                    closestCharValueWord = word;
                }
            }
            response.setValue(closestCharValueWord);

            // Find word with closest lexical order
            int minLexicalDiff = Integer.MAX_VALUE;
            String closestLexicalWord = null;
            for (String word : words) {
                int lexicalDiff = Math.abs(inputText.compareTo(word));
                if (lexicalDiff < minLexicalDiff) {
                    minLexicalDiff = lexicalDiff;
                    closestLexicalWord = word;
                }
            }
            response.setLexical(closestLexicalWord);
        }

        // Store new word in file
        try {
            File file = new File("words.txt");
            file.createNewFile();
            FileWriter writer = new FileWriter(file, true);
            writer.write(inputText + "\n");
            writer.close();
            words.add(inputText);
            System.out.println("New word added to file: " + inputText);
        } catch (IOException e) {
            System.err.println("Failed to store word in file: " + e.getMessage());
        }

        return response;
    }


    private int getCharValue(String word) {
        int charValue = 0;
        for (char c : word.toCharArray()) {
            charValue += (int) c - 96; // convert char to 1-based index
        }
        return charValue;
    }
}
