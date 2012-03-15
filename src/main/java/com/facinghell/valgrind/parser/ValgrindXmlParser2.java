package com.facinghell.valgrind.parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindErrorKind;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktrace;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

public class ValgrindXmlParser2 implements Serializable
{
	private static final long serialVersionUID = -3804982443628621529L;
	
	XPathFactory xpathFactory = XPathFactory.newInstance();
	XPath xpath = XPathFactory.newInstance().newXPath(); 
	
	public ValgrindXmlParser2()
	{
	}	

	public ValgrindReport parse( final File file ) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
	{
		ValgrindReport valgrindReport = new ValgrindReport();
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(file);

		NodeList errorList = (NodeList) xpath.evaluate("/valgrindoutput/error", document, XPathConstants.NODESET);
		
    
	    for( int i = 0; i < errorList.getLength(); ++i )
	    {
	    	try
	    	{
	    		valgrindReport.addError( parseError( errorList.item( i ) ) );
	    	}
	    	catch( Exception e )
	    	{	    	
	    	}	    		    	
	    }	

		return valgrindReport;
	}	

	private ValgrindError parseError( Node errorNode )
	{
		ValgrindError error = new ValgrindError();		
		error.setKind( ValgrindErrorKind.valueOf( stringOf(errorNode, "kind") ) );		
		error.setUniqueId( stringOf(errorNode, "unique") );
		error.setExecutable( stringOf(errorNode, "/valgrindoutput/args/argv/exe") );		
		error.setLeakedBytes( integerOf(errorNode, "xwhat/leakedbytes") );
		error.setLeakedBlocks( integerOf(errorNode, "xwhat/leakedblocks") );		
		error.setDescription( stringOf(errorNode, "what") );
		if ( error.getDescription() == null || error.getDescription().equals("") )
			error.setDescription( stringOf(errorNode, "xwhat/text") );
		
		error.setStacktrace( parseStack( errorNode ) );
		return error;	
	}
	
	private ValgrindStacktrace parseStack( Object object )
	{
		ValgrindStacktrace stacktrace = new ValgrindStacktrace();
		
		try 
		{
			NodeList frameList = (NodeList) xpath.evaluate("stack/frame", object, XPathConstants.NODESET);
			
			for( int i = 0; i < frameList.getLength(); ++i )
				stacktrace.addFrame( parseStacktraceFrame( frameList.item( i ) ) );

		} catch (XPathExpressionException e) 
		{
			return null;
		}		
		
		return stacktrace;
	}
	
	private String stringOf( Node parentNode, String path )
	{	
		try 
		{
			Node n = (Node)xpath.evaluate(path, parentNode, XPathConstants.NODE);
			
			if ( n == null )
				return null;
			
			return n.getTextContent();			
		} 
		catch (XPathExpressionException e) 
		{
			return null;						
		}		
	}
	
	private Integer integerOf( Node parentNode, String path )
	{		
		String s = stringOf( parentNode, path );
		if ( s == null )
			return null;
		
		try
		{
			return Integer.valueOf( s );			
		}
		catch( NumberFormatException e )
		{
			return null;
		}		
	}

	private ValgrindStacktraceFrame parseStacktraceFrame( Node object )
	{
		ValgrindStacktraceFrame frame = new ValgrindStacktraceFrame();
		
		frame.setDirectoryName( stringOf(object, "dir") );
		frame.setFileName( stringOf(object, "file") );
		frame.setObjectName( stringOf(object, "obj") );
		frame.setFunctionName( stringOf(object, "fn") );
		
		String lineNumberString = stringOf(object, "line");
		
		if ( lineNumberString != null )
		{
			lineNumberString = lineNumberString.trim();
			
			if ( !lineNumberString.isEmpty() )
				frame.setLineNumber( Integer.valueOf(lineNumberString) );			
		}
		
		return frame;		
	}
	
	public static void main(String[] args) 
	{
		try 
		{
			ValgrindReport report = new ValgrindXmlParser2().parse( new File("vg.xml") );
			
			System.out.println("error count:" + report.getErrorCount() );
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}
}
