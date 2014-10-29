package prj.womtools;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

class TreeEvaluator {

    // Set-up a simple wiki configuration
    WikiConfig config = DefaultConfigEnWp.generate();
    private String filePath;
    // Instantiate a compiler for wiki pages
    WtEngineImpl engine = new WtEngineImpl(config);
    final int wrapCol = 80;


    TreeEvaluator(String filePath) {
        this.filePath = filePath;
    }

    void processFile() throws IOException, EngineException, LinkTargetException {
        try (BufferedReader br = new BufferedReader(new FileReader( filePath )))  {
            //Read wikitext file line by line
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while (line != null) {
                sb.append( line );
                sb.append('\n');
                line = br.readLine();
            }
            String wikitext = sb.toString();

            // Retrieve a page
            PageTitle pageTitle = PageTitle.make(config, filePath);

            PageId pageId = new PageId(pageTitle, -1);

            // Compile the retrieved page
            EngProcessedPage cp = engine.postprocess(pageId, wikitext, null);

            //Check chembox in the begining

            TreePrinter p = new TreePrinter(config, wrapCol);
            String text = (String) p.go(cp.getPage());

            System.out.println( text );

        }
    }
}
