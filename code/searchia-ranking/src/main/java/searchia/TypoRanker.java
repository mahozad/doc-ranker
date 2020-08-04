package searchia;

import searchia.Query.QueryType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static searchia.Query.QueryType.*;

public class TypoRanker {

    public static List<Doc> rankByTypo(List<Query> queries, List<Doc> docs) {
        boolean queriesContainCorrectedOrSuggested = queriesContainCorrectedOrSuggested(queries);

        queries.stream()
                .filter(query -> query.getType() == ORIGINAL || query.getType() == WILDCARD)
                .forEach(query -> {
                    for (Doc doc : docs) {
                        boolean isDocMatching = isDocMatchingWithQuery(doc, query);
                        if (isDocMatching) {
                            doc.setPhaseScore(1);
                        } else {
                            doc.setPhaseScore(Math.max(0, doc.getPhaseScore()));
                        }
                    }
                });

        if (queriesContainCorrectedOrSuggested) {
            return docs.stream().sorted(reverseOrder()).collect(toList());
        } else {
            // Reset the scores (all docs should be considered equal in next phase)
            return docs.stream().sorted(reverseOrder()).peek(doc -> doc.setPhaseScore(0)).collect(toList());
        }
    }

    public static boolean isDocMatchingWithQuery(Doc doc, Query query) {
        List<String> tokens = DocumentProcessor.tokenizeText(query.getText());
        for (String token : tokens) {
            if (query.getType() == WILDCARD && token.endsWith("*")) {
                String tokenStem = token.replace("*", "");
                boolean noMatches = doc.getTokens().keySet().stream().noneMatch(s -> s.startsWith(tokenStem));
                if (noMatches) {
                    return false;
                }
            } else if (!doc.getTokens().containsKey(token)) {
                return false;
            }
        }
        return true;
    }

    public static boolean queriesContainCorrectedOrSuggested(List<Query> queries) {
        Set<QueryType> queryTypes = queries.stream().map(Query::getType).collect(toSet());
        return queryTypes.contains(CORRECTED) || queryTypes.contains(SUGGESTED);
    }

    /**
     * Compute distance of words using Levenshtein algorithm.
     * <p>
     * Adopted from https://github.com/apache/commons-text/blob/master/src/main/java/org/apache
     * /commons/text/similarity/LevenshteinDistance.java
     * <p>
     * Consider using Damerau-Levenshtein algorithm which allows transposition of adjacent symbols.
     *
     * @param word1
     * @param word2
     * @return
     */
    public static int measureWordsDistance(CharSequence word1, CharSequence word2, int threshold) {
        if (word1 == null || word2 == null) {
            throw new IllegalArgumentException("CharSequences must not be null");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must not be negative");
        }

        /*
         * This implementation only computes the distance if it's less than or
         * equal to the threshold value, returning -1 if it's greater. The
         * advantage is performance: unbounded distance is O(nm), but a bound of
         * k allows us to reduce it to O(km) time by only computing a diagonal
         * stripe of width 2k + 1 of the cost table. It is also possible to use
         * this to compute the unbounded Levenshtein distance by starting the
         * threshold at 1 and doubling each time until the distance is found;
         * this is O(dm), where d is the distance.
         *
         * One subtlety comes from needing to ignore entries on the border of
         * our stripe eg. p[] = |#|#|#|* d[] = *|#|#|#| We must ignore the entry
         * to the left of the leftmost member We must ignore the entry above the
         * rightmost member
         *
         * Another subtlety comes from our stripe running off the matrix if the
         * strings aren't of the same size. Since string s is always swapped to
         * be the shorter of the two, the stripe will always run off to the
         * upper right instead of the lower left of the matrix.
         *
         * As a concrete example, suppose s is of length 5, t is of length 7,
         * and our threshold is 1. In this case we're going to walk a stripe of
         * length 3. The matrix would look like so:
         *
         * <pre>
         *    1 2 3 4 5
         * 1 |#|#| | | |
         * 2 |#|#|#| | |
         * 3 | |#|#|#| |
         * 4 | | |#|#|#|
         * 5 | | | |#|#|
         * 6 | | | | |#|
         * 7 | | | | | |
         * </pre>
         *
         * Note how the stripe leads off the table as there is no possible way
         * to turn a string of length 5 into one of length 7 in edit distance of
         * 1.
         *
         * Additionally, this implementation decreases memory usage by using two
         * single-dimensional arrays and swapping them back and forth instead of
         * allocating an entire n by m matrix. This requires a few minor
         * changes, such as immediately returning when it's detected that the
         * stripe has run off the matrix and initially filling the arrays with
         * large values so that entries we don't compute are ignored.
         *
         * See Algorithms on Strings, Trees and Sequences by Dan Gusfield for
         * some discussion.
         */

        int n = word1.length(); // length of word1
        int m = word2.length(); // length of word2

        // if one string is empty, the edit distance is necessarily the length
        // of the other
        if (n == 0) {
            return m <= threshold ? m : -1;
        } else if (m == 0) {
            return n <= threshold ? n : -1;
        }

        if (n > m) {
            // swap the two strings to consume less memory
            final CharSequence tmp = word1;
            word1 = word2;
            word2 = tmp;
            n = m;
            m = word2.length();
        }

        // the edit distance cannot be less than the length difference
        if (m - n > threshold) {
            return -1;
        }

        int[] p = new int[n + 1]; // 'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally
        int[] tempD; // placeholder to assist in swapping p and d

        // fill in starting table values
        final int boundary = Math.min(n, threshold) + 1;
        for (int i = 0; i < boundary; i++) {
            p[i] = i;
        }
        // these fills ensure that the value above the rightmost entry of our
        // stripe will be ignored in following loop iterations
        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
        Arrays.fill(d, Integer.MAX_VALUE);

        // iterates through t
        for (int j = 1; j <= m; j++) {
            final char word2J = word2.charAt(j - 1); // jth character of word2
            d[0] = j;

            // compute stripe indices, constrain to array size
            final int min = Math.max(1, j - threshold);
            final int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(
                    n, j + threshold);

            // ignore entry left of leftmost
            if (min > 1) {
                d[min - 1] = Integer.MAX_VALUE;
            }

            // iterates through [min, max] in s
            for (int i = min; i <= max; i++) {
                if (word1.charAt(i - 1) == word2J) {
                    // diagonally left and up
                    d[i] = p[i - 1];
                } else {
                    // 1 + minimum of cell to the left, to the top, diagonally
                    // left and up
                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
                }
            }

            // copy current distance counts to 'previous row' distance counts
            tempD = p;
            p = d;
            d = tempD;
        }

        // if p[n] is greater than the threshold, there's no guarantee on it
        // being the correct
        // distance
        if (p[n] <= threshold) {
            return p[n];
        }
        return -1;
    }
}
