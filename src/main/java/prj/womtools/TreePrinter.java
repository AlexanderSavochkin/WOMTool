package prj.womtools;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.parser.nodes.*;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class TreePrinter
		extends
			AstVisitor<WtNode>
{
	private static final Pattern ws = Pattern.compile("\\s+");
	private final WikiConfig config;
	private final int wrapCol;
	private StringBuilder line;
    private StringBuilder resultStringBuilder;
	private int extLinkNum;
	private boolean pastBod;
	private int needNewlines;
	private boolean needSpace;
	private boolean noWrap;
    private int indent = 0;

    private PrintStream output = System.out;
	
	private LinkedList<Integer> sections;
	
	// =========================================================================
	
	public TreePrinter(WikiConfig config, int wrapCol)
	{
		this.config = config;
		this.wrapCol = wrapCol;
	}
	
	@Override
	protected boolean before(WtNode node)
	{
		// This method is called by go() before visitation starts
		line = new StringBuilder();
        resultStringBuilder = new StringBuilder();
		extLinkNum = 1;
		pastBod = false;
		needNewlines = 0;
		needSpace = false;
		noWrap = false;
		sections = new LinkedList<Integer>();
		return super.before(node);
	}
	
	@Override
	protected Object after(WtNode node, Object result)
	{
        output.println();
		// This method is called by go() after visitation has finished
		// The return value will be passed to go() which passes it to the caller
		return null;
	}
	
	// =========================================================================
	
	public void visit(WtNode n)
	{
		// Fallback for all nodes that are not explicitly handled below
        output.print(StringUtils.strrep(' ', indent));
		output.print("<");
        output.print(n.getNodeName());
        output.println(" />");
	}
	
	public void visit(WtNodeList n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtNodeList]");
        output.println();
        indent += 4;
        iterate(n);
        indent -= 4;
	}
	
	public void visit(WtUnorderedList e)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtUnorderedList]");
        output.println();
        indent += 4;
        iterate(e);
        indent -= 4;
	}
	
	public void visit(WtOrderedList e)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtOrderedList]");
        output.println();
        indent += 4;
        iterate(e);
        indent -= 4;
	}
	
	public void visit(WtListItem item)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtListItem]");
        output.println();
        indent += 4;
        iterate(item);
        indent -= 4;
	}
	
	public void visit(EngPage p)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtListItem]");
        output.println();
        indent += 4;
        iterate(p);
        indent -= 4;
	}
	
	public void visit(WtText text)
	{
		//write(text.getContent());
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtText] ");
        output.print(text.getContent());
        output.println();
	}
	
	public void visit(WtWhitespace w)
	{
		//write(" ");
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtWhitespace] ");
        output.println();
	}
	
	public void visit(WtBold b)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtBold]");
        indent += 4;
        iterate(b);
        indent -= 4;
	}
	
	public void visit(WtItalics i)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtItalics]");
        indent += 4;
        iterate(i);
        indent -= 4;
	}
	
	public void visit(WtXmlCharRef cr)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtXmlCharRef]");
        output.println(Character.toChars(cr.getCodePoint()));
	}

	public void visit(WtXmlEntityRef er)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtXmlEntityRef] ");
		String ch = er.getResolved();
		if (ch == null)
		{
            output.print('&');
            output.print(er.getName());
            output.println(';');
		}
		else
		{
            output.println(ch);
		}
	}
	
	public void visit(WtUrl wtUrl)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtUrl] ");
		if (!wtUrl.getProtocol().isEmpty())
		{
            output.print(wtUrl.getProtocol());
            output.print(':');
		}
        output.println(wtUrl.getPath());
	}
	
	public void visit(WtExternalLink link)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtExternalLink] ");
        output.println(extLinkNum++);
	}
	
	public void visit(WtInternalLink link)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.print("[WtInternalLink] ");
        output.println(link.getPostfix());
		try
		{
			if (link.getTarget().isResolved())
			{
				PageTitle page = PageTitle.make(config, link.getTarget().getAsString());
				if (page.getNamespace().equals(config.getNamespace("Category")))
					return;
			}
		}
		catch (LinkTargetException e)
		{
		}

        indent += 4;
        output.print(link.getPrefix());
		if (!link.hasTitle())
		{
			iterate(link.getTarget());
		}
		else
		{
			iterate(link.getTitle());
		}
        indent -= 4;
	}
	
	public void visit(WtSection s)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtSection] ");
        indent += 4;
        output.print(StringUtils.strrep(' ', indent));
        output.println("--heading-- ");
        indent += 4;
        iterate(s.getHeading());
        indent -= 4;
        output.print(StringUtils.strrep(' ', indent));
        output.println("--body-- ");
        indent += 4;
        iterate(s.getBody());
        indent -= 4;
        indent -= 4;
	}
	
	public void visit(WtParagraph p)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtParagraph]");
        indent += 4;
        iterate(p);
        indent -= 4;
	}
	
	public void visit(WtHorizontalRule hr)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtHorizontalRule] ");
	}
	
	public void visit(WtXmlElement e)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtXmlElement] ");
        indent += 4;
        iterate(e.getBody());
        indent -= 4;
	}
	
	public void visit(WtImageLink n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtImageLink] ");
	}
	
	public void visit(WtIllegalCodePoint n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtIllegalCodePoint] ");
	}


	public void visit(WtXmlComment n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtXmlComment] ");
	}
	
	public void visit(WtTemplate n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtTemplate] ");
        indent += 4;
        iterate(n);
        indent -= 4;
	}
	
	public void visit(WtTemplateArgument n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtTemplateArgument] ");
        indent += 4;
        iterate(n);
        indent -= 4;
	}
	
	public void visit(WtTemplateParameter n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtTemplateParameter] ");
        indent += 4;
        iterate(n);
        indent -= 4;
	}
	
	public void visit(WtTagExtension n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtTagExtension] ");
	}
	
	public void visit(WtPageSwitch n)
	{
        output.print(StringUtils.strrep(' ', indent));
        output.println("[WtPageSwitch] ");
	}

}
