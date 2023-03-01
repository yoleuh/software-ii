import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Java program that counts word occurrences in a given input file and outputs
 * an HTML document with a table of the words and counts listed in alphabetical
 * order.
 *
 * @author brian tan
 *
 */
public final class WordCounter {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private WordCounter() {
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char c = text.charAt(position);
        String x = "";
        int i = position + 1;
        x = x + c;
        if (!separators.contains(c)) {
            while (i < text.length() && !separators.contains(c)) {
                c = text.charAt(i);
                if (!separators.contains(c)) {
                    String y = "" + c;
                    x = x.concat(y);
                }
                i++;
            }
        } else {
            while (i < text.length() && separators.contains(c)) {
                c = text.charAt(i);
                if (separators.contains(c)) {
                    String y = "" + c;
                    x = x.concat(y);
                }
                i++;
            }
        }
        return x;
    }

    /**
     * Assign counts to each word in the map and create a queue of distinct
     * words from the text file
     *
     * @param wordCount
     *            map of counts assigned to respective words
     * @param words
     *            queue of new words
     * @param fileIn
     *            input stream
     *
     * @ensures words contains all distinct words from the fileIn, count is
     *          assigned to words in wordCount
     *
     */
    public static void count(Map<String, Integer> wordCount,
            Queue<String> words, SimpleReader fileIn) {

        //create separator set
        Set<Character> separators = new Set1L<Character>();
        separators.add(',');
        separators.add(' ');
        separators.add('.');
        separators.add('!');
        separators.add('?');
        separators.add('/');
        separators.add(';');
        separators.add(':');
        separators.add('-');

        while (!fileIn.atEOS()) {
            String line = fileIn.nextLine();
            int i = 0;
            while (i < line.length()) {
                //check if string is separator
                String s = nextWordOrSeparator(line, i, separators);
                if (separators.contains(s.charAt(0))) {
                    i++;
                } else {
                    //word counter
                    if (wordCount.hasKey(s)) {
                        int count = wordCount.value(s) + 1;
                        wordCount.replaceValue(s, count);
                    } else {
                        //if it is a new word, add into queue
                        words.enqueue(s);
                        wordCount.add(s, 1);
                    }
                    i = i + s.length();
                }
            }
        }
    }

    /**
     * Compare two strings based on alphabetical order
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }

    }

    /**
     * Output table of words and counts into html file
     *
     * @param inputFile
     *            name of input file
     * @param fileOut
     *            output stream into html file
     * @param words
     *            queue of words sorted alphabetically
     * @param wordCount
     *            map of counts assigned to respective words
     *
     * @requires inputFile contains words
     *
     * @ensures complete html page with table
     */
    public static void output(String inputFile, SimpleWriter fileOut,
            Queue<String> words, Map<String, Integer> wordCount) {

        fileOut.println("<html>");
        fileOut.println("<head>");
        fileOut.println("<title>Words Counted in " + inputFile + "</title>");
        fileOut.println("<body>");
        fileOut.println("<h2>Words Counted in " + inputFile + "</h2>");
        fileOut.println("<hr />");
        fileOut.println("<table border=\"1\">");
        fileOut.println("<tr>");
        fileOut.println("<th>Words</th>");
        fileOut.println("<th>Counts</th>");
        fileOut.println("</tr>");

        //create table with each word
        int l = words.length();
        for (int i = 0; i < l; i++) {
            String s = words.dequeue();
            fileOut.println("<tr>");
            fileOut.println("<td>" + s + "</td>");
            fileOut.println("<td>" + wordCount.value(s) + "</td>");
            fileOut.println("</tr>");
        }

        fileOut.println("</table>");
        fileOut.println("</body>");
        fileOut.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        //open streams
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        //input prompt
        out.println("name of an input file: ");
        String inputFile = in.nextLine();
        SimpleReader fileIn = new SimpleReader1L(inputFile);

        //output prompt
        out.println("name of an output file: ");
        String outputFile = in.nextLine();
        SimpleWriter fileOut = new SimpleWriter1L(outputFile);

        //close streams
        in.close();
        out.close();

        //count the number of occurrences for each word
        Map<String, Integer> wordCount = new Map1L<>();
        Queue<String> words = new Queue1L<>();
        count(wordCount, words, fileIn);

        //sort alphabetically
        Comparator<String> comparator = new StringLT();
        words.sort(comparator);

        //output to html
        output(inputFile, fileOut, words, wordCount);

        //close streams
        fileIn.close();
        fileOut.close();
    }
}
