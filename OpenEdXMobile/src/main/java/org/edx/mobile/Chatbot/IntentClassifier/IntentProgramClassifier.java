package org.edx.mobile.Chatbot.IntentClassifier;

import android.content.Context;

import org.edx.mobile.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static android.provider.Settings.System.getString;


public class IntentProgramClassifier {
    private List<String> intentList;

    public String selectedLanguage="en";

    private Context mContext;
    public IntentProgramClassifier(List<String> stringList, String selectedLanguage,Context context) {
        this.intentList = stringList;
        this.selectedLanguage=selectedLanguage;
        this.mContext=context;
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

    private static String matchProgram(String userInput, List<String> programs) {
        for (String program : programs) {
            if (containsAllWordsInOrder(userInput, program)) {
                return program;
            }
        }
        return ""; // No match found
    }

    private static boolean containsAllWordsInOrder(String userInput, String program) {
        String[] userWords = userInput.toLowerCase().split("\\s+");
        int currentIndex = -1;

        for (String word : userWords) {
            currentIndex = program.toLowerCase().indexOf(word, currentIndex + 1);

            if (currentIndex == -1) {
                return false;
            }
        }

        return true;
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

    private String checkMessageLanguage(int messageCode,String response) {
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
        } else if (selectedLanguage.equals("ml-IN")) {
            return getMessageMalayalam(messageCode, response);
        } else if (selectedLanguage.equals("or")) {
            return getMessageOriya(messageCode, response);
        }
        return "";
    }

    private String getMessageEnglish(int messageCode,String response){
        switch (messageCode) {
            case 0:
                return  "There's no program directly related your request "+response+".";
            case 1:
                return "User has an intent related to the program being " + response+ ".";
            default:
                String formattedString = mContext.getString(R.string.user_multiple_intent_message, String.valueOf(messageCode), response);
                return formattedString;
        }
    }


    private String getMessageHindi(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "कोई कार्यक्रम नहीं मिला।";
            case 1:
                return "उपयोगकर्ता का एक " + response + " से संबंधित हस्तक्षेप है।";
            default:
                return  "उपयोगकर्ता का इरादा कई "+messageCode+" प्रोग्राम से संबंधित है, जो "+response+" होना चाहिए। कृपया स्पष्ट करें।";
        }
    }

    private String getMessageKannada(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "ಯಾವುದೇ ಕಾರ್ಯಕ್ರಮ ಕಂಡುಬಂದಿಲ್ಲ.";
            case 1:
                return "ಬಳಕೆದಾರನಲ್ಲಿ " + response + " ಕುರಿತಾದ ಒಂದು ಇಂಟೆಂಟ್ ಇದೆ.";
            default:
                return "ಬಳಕೆದಾರನ ಉದ್ದೇಶಕ್ಕೆ ಹೆಚ್ಚುವರಿ "+messageCode+" ಪ್ರೋಗ್ರಾಮ್‌ಗಳು ಸಂಬಂಧಿತವಾಗಿರಬಹುದು, ಅದು "+response+" ಆಗಿರಬೇಕು. ದಯವಿಟ್ಟು ಸ್ಪಷ್ಟಪಡಿಸಿ.";
        }
    }
    private String getMessageTamil(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "எந்த நிராகரிக்கப்பட்ட நிராகரிக்கப்பட்டது கிடைக்கவில்லை.";
            case 1:
                return "பயனரிடம் " + response + " குறித்து ஒரு இருப்பு உள்ளது.";
            default:
                return  "பயனர் பல "+messageCode+" நிரல்களுடன் சம்பந்தப்பட்ட நோக்கம் உள்ளது, அது "+response+" ஆக வேண்டும். தயவுசெய்து தெரிவிக்கவும்.";
        }
    }

    private String getMessageBengali(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "কোনো প্রোগ্রাম পাওয়া যায়নি।";
            case 1:
                return "ব্যবহারকারীর একটি " + response + " সহিত প্রোগ্রাম সংক্ষেপ রয়েছে।";
            default:
                return "ব্যবহারকারীর উদ্দেশ্য একাধিক "+messageCode+" প্রোগ্রামের সম্পর্কে, যা "+response+" হওয়া উচিত। অনুগ্রহ করে স্পষ্ট করুন।";

        }
    }

    private String getMessageMalayalam(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "എന്തേലും പ്രോഗ്രാമം കണ്ടെത്തിയില്ല.";
            case 1:
                return "ഉപയോക്താവിന് " + response + " എന്ന ഒരു പ്രോഗ്രാമം കുറിച്ച് ആഗ്രഹിക്കുന്ന ഒരു ഉദ്ദേശം ഉണ്ട്.";
            default:
                return "ഉപയോക്താവിന്റെ ഉദ്ദേശ്യം എന്നിലും പല "+messageCode+" പ്രോഗ്രാമുകളുമായി ബന്ധപ്പെട്ടതാണ്, അത് "+response+" ആയിരിക്കണം. ദയവായി വെളിച്ചം നല്‍കുക.";


        }
    }

    private String getMessageOriya(int messageCode, String response) {
        switch (messageCode) {
            case 0:
                return "କୌଣସି ପ୍ରୋଗ୍ରାମ୍ ପାଇଁ ପାଇଁ ହେଇ ନାହିଁ";
            case 1:
                return "ବିଶେଷଜ୍ଞାନୁହୁଏ ଏହାରେ ଉପସ୍ଥିତ ଏକ " + response + " ରେଲାଟେଡ ଇନ୍ଟେଣ୍ଟ୍ ଆସୁଛି।";
            default:
                return "ବ୍ୟବହାରକର୍ତ୍ତା ଏକାଧିକ "+messageCode+" ପ୍ରୋଗ୍ରାମ୍‌ଗୁଡ଼ିକ ସଂବନ୍ଧନ୍ତ ଉଦ୍ଦେଶ ରଖୁଛନ୍ତି, ସେଗୁଡ଼ିକ "+response+" ହେଉଛି ଅନୁରୋଧିତ ହେବ। ଦୟାକରି ସ୍ପଷ୍ଟତା ପାଇଁ ପ୍ରସ୍ତୁତ ହେଲେ କରିବ।";
        }
    }





}
