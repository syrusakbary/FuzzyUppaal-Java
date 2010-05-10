import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLReader {
	Document document;
	XPath xPath; 
	public XMLReader () {
		
	}
	public void open (String file) {
		this.open(new File(file));
	}
	public void open (File file) {
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		xPath = XPathFactory.newInstance().newXPath();
	}
	public void read (String data) {
		try {
			StringReader reader = new StringReader(data);
			InputSource inputSource = new InputSource( reader );
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		xPath = XPathFactory.newInstance().newXPath();		
	}
	public static String escape (String str) {
		return str.replaceAll("&", "&amp;")
			.replaceAll(">", "&gt;")
			.replaceAll("<", "&lt;");
	}
	public static String unescape (String str) {
		return str
			.replaceAll("&amp;", "&")
			.replaceAll("&gt;", ">")
			.replaceAll("&lt;", "<");
	}
	public void save (String file) {
		this.save(new File(file));
	}
	public void save (File file) {
	    Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
		    xformer.transform(new DOMSource(document), new StreamResult(file));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String toString () {
		Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			StreamResult st = new StreamResult(new StringWriter());
			xformer.transform(new DOMSource(document), st);
			return st.getWriter().toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	public void modifyNodes (NodeList nodes,XMLModificator modificator) {
		if (nodes == null) return;
	    for (int idx = 0; idx < nodes.getLength(); idx++) {
    		modificator.modifyNode(nodes.item(idx));
	    }
	}
	public NodeList getNodes (String expression) {
		try {
			return (NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) throws Exception {
		XMLReader r = new XMLReader();
		r.open("lib/example.xml");
		r.modifyNodes(r.getNodes("//label[@kind='guard' or @kind='invariant']"),new XMLModificator() {
			public void modifyNode(Node node) {
				node.setTextContent("new value");
				System.out.println(XMLReader.unescape("asdfasdf &lt; asdf"));
			}
		});
		r.save("lib/example_new.xml");

	}

}

   
    