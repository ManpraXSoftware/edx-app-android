package org.edx.mobile.Chatbot.IntentClassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class IntentTopicClassifier {
    private static List<String> intentList;

    public String selectedLanguage="en";
    public IntentTopicClassifier(List<String> stringList, String selectedLanguage) {
        this.intentList = stringList;
        this.selectedLanguage=selectedLanguage;
    }
    public static String convertAlphabeticToNumeric(String input) {
        // Create a map to store the word representations of numbers and their numeric equivalents
        Map<String, String> numberMap = createNumberMap();

        // Split the input string into words
        String[] words = input.split("\\s+");

        // Iterate through each word and replace it if it's a known word representation of a number
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase(); // Convert to lowercase for case-insensitivity
            if (numberMap.containsKey(word)) {
                words[i] = numberMap.get(word);
            }
        }

        // Join the words back into a string
        return String.join(" ", words);
    }

    private static Map<String, String> createNumberMap() {
        // Create a map with word representations of numbers and their numeric equivalents
        Map<String, String> numberMap = new HashMap<>();
        numberMap.put("zero", "0");
        numberMap.put("one", "1");
        numberMap.put("two", "2");
        numberMap.put("three", "3");
        numberMap.put("four", "4");
        numberMap.put("five", "5");
        numberMap.put("six", "6");
        numberMap.put("seven", "7");
        numberMap.put("eight", "8");
        numberMap.put("nine", "9");
        numberMap.put("ten", "10");
        numberMap.put("eleven", "11");
        numberMap.put("twelve", "12");
        numberMap.put("thirteen", "13");
        numberMap.put("fourteen", "14");
        numberMap.put("fifteen", "15");
        numberMap.put("sixteen", "16");
        numberMap.put("seventeen", "17");
        numberMap.put("eighteen", "18");
        numberMap.put("nineteen", "19");
        numberMap.put("twenty", "20");
        numberMap.put("thirty", "30");
        numberMap.put("forty", "40");
        numberMap.put("fifty", "50");
        numberMap.put("sixty", "60");
        numberMap.put("seventy", "70");
        numberMap.put("eighty", "80");
        numberMap.put("ninety", "90");

        // Add mappings for numbers up to 100
        for (int i = 21; i <= 100; i++) {
            String wordRepresentation = getWordRepresentation(i);
            numberMap.put(wordRepresentation, String.valueOf(i));
        }

        return numberMap;
    }
    private static String getWordRepresentation(int number) {
        if (number < 20) {
            return ""; // Not needed for numbers less than 20
        } else if (number < 100) {
            int tens = (number / 10) * 10;
            int ones = number % 10;
            return tens + (ones > 0 ? "-" + ones : "");
        } else {
            return "hundred";
        }
    }



    private static String tokenizeString(String s) {
        String[] tokens = s.toLowerCase().replaceAll("[^a-zA-Z0-9]", " ").split("\\s+"); // Replace non-alphanumeric characters
        return String.join(" ", tokens);
    }

    private static boolean containsCommonSubstring(String str1, String str2) {
        // Split the strings into words
        String[] words1 = str1.split(" ");
        String[] words2 = str2.split(" ");

        // Check if there is any common word
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equalsIgnoreCase(word2)) {
                    return true;
                }
            }
        }

        return false;
    }


    public Map<String, String> classifyIntent(String userInput) {
        Map<String, String> resultMap = new HashMap<>();
        List<String> responseList = new ArrayList<>();

        IntentClassifier intentClassifier=new IntentClassifier(intentList,userInput,selectedLanguage);
        responseList=intentClassifier.findMatchingIntent();



        int programsFound = responseList.size();

        StringJoiner  responses= new StringJoiner(", ");
        for(String program:responseList){
            responses.add(program);
        }
        String response=responses.toString();
        switch (programsFound) {
            case 0:
                resultMap.put("Intent", "Not-Found");
                resultMap.put("message", checkMessageLanguage(0,response));
                resultMap.put("action", "false");
                break;
            case 1:
                resultMap.put("Intent", response.toString());
                resultMap.put("message",checkMessageLanguage(1,response));
                resultMap.put("action", "true");
                break;
            default:
                resultMap.put("Intent", response.toString());
                resultMap.put("message", checkMessageLanguage(programsFound,response));
                resultMap.put("action", "false");
                break;
        }
        return resultMap;
    }


    private boolean containsIgnoreCase(String input, String subject) {
        return input.toLowerCase().contains(subject.toLowerCase());
    }

    private String checkMessageLanguage(int messageCode, String response) {
        if (selectedLanguage.equals("en")) {
            return getMessageEnglish(messageCode, response);
        } else if (selectedLanguage.equals("hi")) {
            return getMessageHindi(messageCode, response);
        } else if (selectedLanguage.equals("kn")) {
            return getMessageKannada(messageCode, response);
        } else if (selectedLanguage.equals("ta")) {
            return getMessageTamil(messageCode, response);
        } else if (selectedLanguage.equals("bn")) {
            return getMessageBengali(messageCode, response);
        } else if (selectedLanguage.equals("ml")) {
            return getMessageMalayalam(messageCode, response);
        } else if (selectedLanguage.equals("or")) {
            return getMessageOriya(messageCode, response);
        }
        return "";
    }

    private String getMessageEnglish(int messageCode,String response){
        switch (messageCode) {
            case 0:
                return  "There's no topic directly related your request "+response+".";
            case 1:
                return "User has an intent related to the topic being " + response+ ".";
            default:
                return "User has an intent related to the multiple topic being " + response + ". Please clarify";
        }
    }

    private String getMessageHindi(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "कोई विषय नहीं मिला।";
            case 1:
                return "उपयोगकर्ता के पास उस विषय से संबंधित एक इंटेंट है " + response + "।";
            default:
                return "उपयोगकर्ता का इरादा है कि "+messageCode+" विषयों से संबंधित किया जाए " + response + "। कृपया स्पष्टीकरण करें।";
        }
    }


    private String getMessageKannada(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "ಯಾವ ವಿಷಯವೂ ಸಿಗಲಿಲ್ಲ.";
            case 1:
                return "ಬಳಸಿಕೊಳ್ಳುವವನ ವಿಷಯಕ್ಕೆ ಸಂಬಂಧಿಸಿದ ಇಂಟೆಂಟ್ " + response + " ಇದೆ.";
            default:
                return  "ಬಳಕೆದಾರನ ಉದ್ದೇಶವು " + response + " ಆಗಿರಬೇಕಾದ ವಿಷಯಗಳನ್ನು "+messageCode+" ಗಳೊಂದಿಗೆ ಸಂಬಂಧಿಸಿದೆ. ದಯವಿಟ್ಟು ಸ್ಪಷ್ಟಪಡಿಸಿ।";
        }
    }

    private String getMessageTamil(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "எந்த பாடமும் கிடைக்கவில்லை.";
            case 1:
                return "பயனருக்கு " + response + " குறித்து உள்ள ஒரு இருக்கையில் இருக்கும்.";
            default:
                return  "பயனரின் இருக்கையில் "+messageCode+" தலைப்புகளுக்கு " + response + " இருக்க வேண்டும். தயவுசெய்து வெளியிடுக.";
        }
    }

    private String getMessageBengali(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "কৌণসো বিষয় পাওয়া যায়নি।";
            case 1:
                return "ব্যবহারকারীর কাছে এই বিষয়ে সংক্ষেপের একটি ইনটেন্ট রয়েছে " + response + "।";
            default:
                return  "ব্যবহারকারীর উদ্দেশ্য হলো " + response + " হতে "+messageCode+" বিষয় সম্পর্কে। অনুগ্রহ করে স্পষ্ট করুন।";
        }
    }

    private String getMessageMalayalam(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "എന്തെങ്കിലും വിഷയം കണ്ടെത്തിയില്ല.";
            case 1:
                return "ഉപയോക്താവിന് " + response + " എന്ന വിഷയത്തോടുള്ള ഒരു ഇന്‍റന്റ്‌ട്ട് ഉണ്ടായിരിക്കുന്നു.";
            default:
                return  "ഉപയോക്താവിന് "+messageCode+" വിഷയങ്ങളും ബന്ധപ്പെട്ടിരിക്കാം " + response + " ആകുന്നത്. ദയവില്ലാതെ സ്പഷ്ടമാക്കുക.";
        }
    }

    private String getMessageOriya(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "କୌଣସି ବିଷୟ ପାଇଁ ନାଁ ହେବା ନାହିଁ।";
            case 1:
                return "ବ୍ୟବହାରକାରୀଙ୍କୁ " + response + " ଏହି ବିଷୟରେ ଏକ ଇଣ୍ଟେଣ୍ଟ୍ ଆସିଛି।";
            default:
                return  "ବ୍ୟବହାରକର୍ତ୍ତାର ଇଚ୍ଛା ହେଉଛି "+messageCode+" ବିଷୟଗୁଡିକୁ " + response + " ହେବ। ଅନୁଗ୍ରହ କରିପାରି ସ୍ପଷ୍ଟତା ପ୍ରଦାନ କରନ୍ତୁ।";
        }
    }




}
