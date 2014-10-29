package prj.womtools;


import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.IOException;

public class App
{
    private final static String SHOW_TREE = "--showtree";

    private static void printUsage() {
        System.err.println("Usage: java -jar womtools.jar --showtree <Path to file>");
    }

    public static void main( String[] args ) throws IOException, LinkTargetException, EngineException {
        if (args.length != 2) {
            printUsage();
            return;
        }
        String command = args[0];
        String wikiTextFilePath = args[1];

        if (command.equals(SHOW_TREE))
        {
            TreeEvaluator evaluator = new TreeEvaluator(wikiTextFilePath);
            evaluator.processFile();
        }
        else {
            printUsage();
        }
    }
}
