package org.edx.mobile.Chatbot.IntentClassifier;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

public class IntentClassifier {
    private static List<String> intentList = Arrays.asList();
    public static String userInput="";
    public static String selectLanguage="en";
    public IntentClassifier(List<String> intents,String userInput,String selectLanguage) {
        this.intentList = intents;
        this.userInput=userInput;
        this.selectLanguage=selectLanguage;
    }


    public static List<String> findMatchingIntent() {
        if(selectLanguage.equals("en")){
            userInput = convertAlphabeticToNumeric(userInput).toLowerCase();
        }
        try {
            userInput = new String(userInput.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<String> userTokens = List.of(userInput.split("\\s+"));
        List<String> matchingIntents = new ArrayList<>();
        List<String> commonKeywordsList = new ArrayList<>(); // Change to List of Lists

        // Loop through each predefined intent
        int highestMatch=0;
        for (String intent : intentList) {
            if(selectLanguage.equals("en")){
                intent=tokenizeString(intent);
            }
            try {
                intent = new String(intent.getBytes("UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            // Tokenize the intent
            //String[] intentTokens = intent.split("\\s+");
            List<String> intentTokens = List.of(intent.split("\\s+"));
            List<String> commonKeywords = new ArrayList<>(userTokens);
            commonKeywords.retainAll(intentTokens.stream().map(String::toLowerCase).collect(Collectors.toList()));


            // If common keywords found, add the intent to matching intents
            if (!commonKeywords.isEmpty()) {
                for (String keyword : commonKeywords) {
                    commonKeywordsList.add(keyword);
                }
                if(highestMatch<commonKeywords.size())
                {
                    highestMatch=commonKeywords.size();
                }
                //matchingintents.add(intent);
            }
        }

        commonKeywordsList = removeDuplicates(commonKeywordsList);


        List<List<String>> subsets = generateSubsets(commonKeywordsList);
        sortSubsets(subsets);

        if(highestMatch!=0) {
            for (String intent : intentList) {
                if (matchKeyWord(intent, subsets, highestMatch)) {
                    matchingIntents.add(intent);
                }
            }
        }

        return removeDuplicates(matchingIntents);
    }

    private static boolean matchKeyWord(String intent,List<List<String>> commonKeywordsList, int highestMatch){
        try {
            intent = new String(intent.getBytes("UTF-8"), "UTF-8").toLowerCase();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for(List<String> commonKeywords: commonKeywordsList){
            if(commonKeywords.size()>=highestMatch)
                if(matchListWithString(commonKeywords,intent)){
                    return true;
                }
        }
        return false;
    }

    private static boolean matchListWithString(List<String> inputList, String intent) {
        for (String element : inputList) {
            if (isNumeric(element)) {

                if(!(intent+" ").contains(element+" ")){
                    return false;
                }
            }
            else if (!intent.contains(element)) {
                return false; // If any element is not found in the intent, return false
            }
        }
        return true; // All elements found in the intent
    }

    private static boolean isNumeric(String str) {
        return str.matches("\\d+(\\.\\d+)?"); // Matches integers and decimals
    }

    private static List<List<String>> generateSubsets(List<String> inputList) {
        List<List<String>> subsets = new ArrayList<>();
        generateSubsetsHelper(inputList, 0, new ArrayList<>(), subsets);
        return subsets;
    }

    private static void generateSubsetsHelper(List<String> inputList, int index, List<String> currentSubset, List<List<String>> subsets) {
        // Add the currentSubset to the list of subsets
        subsets.add(new ArrayList<>(currentSubset));

        // Generate subsets by including the current element
        for (int i = index; i < inputList.size(); i++) {
            currentSubset.add(inputList.get(i));
            generateSubsetsHelper(inputList, i + 1, currentSubset, subsets);
            currentSubset.remove(currentSubset.size() - 1);  // Backtrack to generate other subsets
        }
    }

    private static <T> List<T> removeDuplicates(List<T> list) {
        // Create a HashSet to store unique elements
        HashSet<T> set = new HashSet<>(list);

        // Create a new list from the unique elements in the set
        List<T> uniqueList = new ArrayList<>(set);

        return uniqueList;
    }

    private static void sortSubsets(List<List<String>> subsets) {
        // Custom comparator to compare lists based on their size and lexicographical order in reverse
        Comparator<List<String>> comparator = Comparator
                .<List<String>, Integer>comparing(List::size)
                .reversed()
                .thenComparing(list -> list.toString(), Comparator.reverseOrder()); // Lexicographical order based on toString() in reverse

        // Sort the subsets using the reversed comparator
        Collections.sort(subsets, comparator);
    }
    private static String tokenizeString(String s) {
        String[] tokens = s.toLowerCase().replaceAll("[^a-zA-Z0-9]", " ").split("\\s+"); // Replace non-alphanumeric characters
        return String.join(" ", tokens).toLowerCase();
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

}
