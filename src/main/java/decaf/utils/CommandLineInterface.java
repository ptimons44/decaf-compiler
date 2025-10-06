package decaf.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A generic command-line interface for 6.035 compilers.  This class
 * provides command-line parsing for student projects.  It recognizes
 * the required <tt>-target</tt>, <tt>-debug</tt>, <tt>-opt</tt>, and
 * <tt>-o</tt> switches, and generates a name for input and output
 * files.
 *
 * @author 6.1100 Staff, last updated January 2024
 */
public class CommandLineInterface {

    public static void printUsage(String message) {
        if (!message.isEmpty()) {
            System.err.println(message);
        }
        System.err.println("Usage: run.sh [options] <filename>\n" +
                "Summary of options:\n" +
                "  -t <stage>              --target <stage>           compile to the given stage\n" +
                "  -o <outfile>            --output <outfile>         write output to <outfile>\n" +
                "  -O <(opt|-opt|all)...>  --opt <(opt|-opt|all)...>  perform the listed optimizations\n" +
                "  -d                      --debug                    print debugging information\n" +
                "  -h                      --help                     print help information\n" +
                "\n" +
                "Long description of options:\n" +
                "  -t <stage>          <stage> is one of \"scan\", \"parse\", \"inter\", or \"assembly\".\n" +
                "  --target <stage>    Compilation will proceed to the given stage and halt there.\n" +
                "\n" +
                "  -d                  Print debugging information.  If this option is not given,\n" +
                "  --debug             then there will be no output to the screen on successful\n" +
                "                      compilation.\n" +
                "\n" +
                "  -O <optspec>        Perform the listed optimizations.  <optspec> is a comma-\n" +
                "  --opt <optspec>     separated list of optimization names, or the special symbol\n" +
                "                      \"all\", meaning all possible optimizations.  You may\n" +
                "                      explicitly disable an optimization by prefixing its name\n" +
                "                      with '-'.\n" +
                "\n" +
                "  -o <outfile>        Write output to <outfile>.  If this option is not given,\n" +
                "  --output <outfile>  output will be written to a file with the same base name as\n" +
                "                      the input file and the extension changed according to the\n" +
                "                      final stage executed.\n");
        System.exit(1);
    }

    /**
     * DEFAULT: produce default output.
     * SCAN: scan the input and stop.
     * PARSE: scan and parse input, and stop.
     * INTER: produce a high-level intermediate representation from the input
     * ASSEMBLY: produce assembly from the input.
     */
    public enum CompilerAction {SCAN, PARSE, INTER, ASSEMBLY, DEFAULT}

    /**
     * Array indicating which optimizations should be performed.  If
     * a particular element is true, it indicates that the optimization
     * named in the optnames[] parameter to parse with the same index
     * should be performed.
     */
    public static boolean[] opts;

    /**
     * Vector of String containing the command-line arguments which could
     * not otherwise be parsed.
     */
    public static ArrayList<String> extras;

    /**
     * Name of the file to put the output in.
     */
    public static String outfile;

    /**
     * Name of the file to get input from.  This is null if the user didn't
     * provide a file name.
     */
    public static String infile;

    /**
     * The target stage.  This should be one of the integer constants
     * defined elsewhere in this package.
     */
    public static CompilerAction target;

    /**
     * The debug flag.  This is true if <tt>-debug</tt> was passed on
     * the command line, requesting debugging output.
     */
    public static boolean debug;

    /*
      Sets up default values for all of the
      result fields.  Specifically, sets the input and output files
      to null, the target to DEFAULT, and the extra array to a new
      empty ArrayList.
     */
    static {
        outfile = null;
        infile = null;
        target = CompilerAction.DEFAULT;
        extras = new ArrayList<>();
    }

    /**
     * Parse the command-line arguments.  Sets all of the result fields
     * accordingly. <BR>
     *
     * <TT>-t / --target <I>target</I></TT> sets the CLI.target field based
     * on the <I>target</I> specified. <BR>
     * <TT>scan</TT> or <TT>scanner</TT> specifies Action.SCAN
     * <TT>parse</TT> specifies Action.PARSE
     * <TT>inter</TT> specifies Action.INTER
     * <TT>assembly</TT> or <TT>codegen</TT> specifies Action.ASSEMBLY
     * <p>
     * The boolean array opts[] indicates which, if any, of the
     * optimizations in optnames[] should be performed; these arrays
     * are in the same order.
     *
     * @param args     Array of arguments passed in to the program's Main
     *                 function.
     * @param optNames Ordered array of recognized optimization names.
     */
    public static void parse(String[] args, String[] optNames) {
        String targetStr = "";

        opts = new boolean[optNames.length];

        if (args.length == 0) {
            printUsage("No arguments given.");
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--debug") || args[i].equals("-d")) {
                debug = true;
            } else if (args[i].equals("--help") || args[i].equals("-h")) {
                printUsage("");
            } else if (args[i].equals("--output") || args[i].equals("-o")) {
                if (i < (args.length - 1)) {
                    outfile = args[i + 1];
                    i++;
                } else {
                    printUsage("No output file specified with option " + args[i]);
                    throw new IllegalArgumentException("Incomplete option " + args[i]);
                }
            } else if (args[i].equals("--target") || args[i].equals("-t")) {
                if (i < (args.length - 1)) {
                    targetStr = args[i + 1];
                    i++;
                } else {
                    printUsage("No target specified with option " + args[i]);
                    throw new IllegalArgumentException("Incomplete option " + args[i]);
                }
            } else if (args[i].startsWith("--opt") || args[i].equals("-O")) {
                String[] optsList;
                if (i < (args.length - 1)) {
                    optsList = args[i + 1].split(",");
                    i++;
                } else {
                    printUsage("No optimizations specified with option " + args[i]);
                    throw new IllegalArgumentException("Incomplete option " + args[i]);
                }
                for (String options : optsList) {
                    if (options.equals("all")) {
                        Arrays.fill(opts, true);
                    } else {
                        for (int k = 0; k < optNames.length; k++) {
                            if (options.equals(optNames[k])) {
                                opts[k] = true;
                            } else if (options.charAt(0) == '-' &&
                                    options.substring(1).equals(optNames[k])) {
                                opts[k] = false;
                            }
                        }
                    }
                }
            } else {
                // if starts with '-', it's an option we don't recognize. print usage
                if (args[i].startsWith("-")) {
                    printUsage("Unrecognized option " + args[i]);
                }
                extras.add(args[i]);
            }
        }

        if (!targetStr.isEmpty()) {
            targetStr = targetStr.toLowerCase();
            switch (targetStr) {
                case "scan" -> target = CompilerAction.SCAN;
                case "parse" -> target = CompilerAction.PARSE;
                case "inter" -> target = CompilerAction.INTER;
                case "assembly" -> target = CompilerAction.ASSEMBLY;
                case "about" -> {
                    printUsage("Test run successful. Command line parameters: ");
                    System.exit(0);
                }
                default -> {
                    printUsage("Invalid target: " + targetStr);
                    throw new IllegalArgumentException(targetStr);
                }
            }
        }

        // grab infile and lose extra args
        int i = 0;
        while (infile == null && i < extras.size()) {
            String fn = extras.get(i);
            if (fn.charAt(0) != '-') {
                infile = fn;
                extras.remove(i);
            }
            i++;
        }

    }
}