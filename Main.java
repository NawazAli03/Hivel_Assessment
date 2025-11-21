import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class Main {



    public static void main(String[] args) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder jsonBuffer = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonBuffer.append(line.trim());
        }

        String jsonInput = jsonBuffer.toString();

        int requiredRootCount = parseInt(jsonInput, "\"k\"");
        Map<Integer, EncodedRoot> decodedRootMap = parseRootObjects(jsonInput);

        List<Integer> sortedIDs = new ArrayList<>(decodedRootMap.keySet());
        Collections.sort(sortedIDs);

  
        BigInteger[] chosenRoots = new BigInteger[requiredRootCount];
        for (int i = 0; i < requiredRootCount; i++) {
            EncodedRoot rootInfo = decodedRootMap.get(sortedIDs.get(i));
            chosenRoots[i] = new BigInteger(rootInfo.digits, rootInfo.base);
        }

        List<BigInteger> coefficients = constructPolynomial(chosenRoots);


        printDecodedRoots(chosenRoots);
        printDegree(requiredRootCount);
        printCoefficientList(coefficients);
        printPrettyPolynomial(coefficients);

        validateRoots(coefficients, decodedRootMap, sortedIDs);
    }

    private static List<BigInteger> constructPolynomial(BigInteger[] roots) {
        List<BigInteger> poly = new ArrayList<>();
        poly.add(BigInteger.ONE); // Start with constant 1

        for (BigInteger root : roots) {
            List<BigInteger> next = new ArrayList<>(Collections.nCopies(poly.size() + 1, BigInteger.ZERO));

            for (int i = 0; i < poly.size(); i++) {
                next.set(i, next.get(i).add(poly.get(i).multiply(root).negate())); // c[i] * (-r)
                next.set(i + 1, next.get(i + 1).add(poly.get(i)));                // c[i] * x
            }
            poly = next;
        }

        return poly;
    }

    private static BigInteger evaluatePolynomial(List<BigInteger> coeff, BigInteger x) {
        BigInteger sum = BigInteger.ZERO;

        for (int i = coeff.size() - 1; i >= 0; i--) {
            sum = sum.multiply(x).add(coeff.get(i));
        }

        return sum;
    }

    private static String polynomialToString(List<BigInteger> coeff) {
        StringBuilder result = new StringBuilder();

        for (int i = coeff.size() - 1; i >= 0; i--) {
            BigInteger c = coeff.get(i);

            if (c.equals(BigInteger.ZERO)) continue;

            if (result.length() > 0)
                result.append(c.signum() > 0 ? " + " : " - ");
            else if (c.signum() < 0)
                result.append("-");

            BigInteger abs = c.abs();

            if (i == 0) {
                result.append(abs);
            } else if (i == 1) {
                if (!abs.equals(BigInteger.ONE)) result.append(abs);
                result.append("x");
            } else {
                if (!abs.equals(BigInteger.ONE)) result.append(abs);
                result.append("x^").append(i);
            }
        }

        return result.length() == 0 ? "0" : result.toString();
    }

    // ============================================================
    //                       JSON PARSING
    // ============================================================

    static class EncodedRoot {
        int base;
        String digits;
    }

    private static int parseInt(String json, String key) {
        int p = json.indexOf(key);
        p = json.indexOf(":", p);
        int q = p + 1;

        while (!Character.isDigit(json.charAt(q))) q++;

        int start = q;
        while (Character.isDigit(json.charAt(q))) q++;

        return Integer.parseInt(json.substring(start, q));
    }

    private static String parseString(String json, String key) {
        int p = json.indexOf(key);
        p = json.indexOf("\"", p + key.length()) + 1;
        int q = json.indexOf("\"", p);
        return json.substring(p, q);
    }

    private static Map<Integer, EncodedRoot> parseRootObjects(String json) {

        Map<Integer, EncodedRoot> map = new HashMap<>();

        for (int i = 0; i < json.length(); i++) {

            if (json.charAt(i) == '"') {
                int end = json.indexOf('"', i + 1);
                String key = json.substring(i + 1, end);

                if (key.equals("keys")) { i = end; continue; }

                if (!key.chars().allMatch(Character::isDigit)) continue;

                int id = Integer.parseInt(key);

                int objStart = json.indexOf("{", end);
                int objEnd = json.indexOf("}", objStart);

                String obj = json.substring(objStart, objEnd + 1);

                EncodedRoot r = new EncodedRoot();
                r.base = parseInt(obj, "\"base\"");
                r.digits = parseString(obj, "\"value\"");

                map.put(id, r);

                i = objEnd;
            }
        }
        return map;
    }

    // ============================================================
    //                        OUTPUT HELPERS
    // ============================================================

    private static void printDecodedRoots(BigInteger[] roots) {
        System.out.print("Decoded roots: [");
        for (int i = 0; i < roots.length; i++) {
            System.out.print(roots[i]);
            if (i < roots.length - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    private static void printDegree(int rootCount) {
        System.out.println("Polynomial degree = " + (rootCount - 1));
    }

    private static void printCoefficientList(List<BigInteger> coeff) {
        System.out.print("Coefficients (lowest degree first): [");
        for (int i = 0; i < coeff.size(); i++) {
            System.out.print(coeff.get(i));
            if (i < coeff.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    private static void printPrettyPolynomial(List<BigInteger> coeff) {
        System.out.println("\nPolynomial:");
        System.out.println(polynomialToString(coeff));
    }

    private static void validateRoots(List<BigInteger> coeff,
                                      Map<Integer, EncodedRoot> rootMap,
                                      List<Integer> sortedIDs) {

        System.out.println("\nValidation (P(root) should equal 0):");

        for (int id : sortedIDs) {
            EncodedRoot enc = rootMap.get(id);
            BigInteger value = new BigInteger(enc.digits, enc.base);

            BigInteger result = evaluatePolynomial(coeff, value);

            System.out.println("Root ID " + id +
                    " (value = " + value + ") → P(r) = " + result);
        }
    }
}
