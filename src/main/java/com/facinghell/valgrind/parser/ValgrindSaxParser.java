package com.facinghell.valgrind.parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindErrorKind;
import com.facinghell.valgrind.model.ValgrindExaminedExecutable;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktrace;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

public class ValgrindSaxParser implements Serializable
{
	private static final long serialVersionUID = -6889121223670989851L;	
	
	private ValgrindSourceCache sourceCache;
	
	private class Handler extends DefaultHandler
	{
		private ValgrindReport currentReport;
		private ValgrindError currentError;
		private ValgrindStacktrace currentStacktrace;
		private ValgrindStacktraceFrame currentStacktraceFrame;
		private StringBuilder data;
		private String currentExecutable = null;
		private boolean xwhat = false;
		private boolean args = false;
		private boolean argv = false;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException
		{
			if ( qName.equalsIgnoreCase("valgrindoutput") )
			{
				currentReport = new ValgrindReport();
			}
			
			if ( qName.equalsIgnoreCase("error") )
			{
				currentError = new ValgrindError();
			}
			
			if ( qName.equalsIgnoreCase("unique") && currentError != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("kind")  && currentError != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("what")  && currentError != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("xwhat")  && currentError != null )
			{
				xwhat = true;
			}
			
			if ( qName.equalsIgnoreCase("args")  && currentReport != null )
			{
				args = true;
			}
			
			if ( qName.equalsIgnoreCase("argv")  && currentReport != null )
			{
				argv = true;
			}
			
			if ( qName.equalsIgnoreCase("exe")  && args && argv )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("text")  && xwhat )
			{
				data = new StringBuilder();				
			}
			
			if ( qName.equalsIgnoreCase("leakedbytes")  && xwhat )
			{
				data = new StringBuilder();				
			}
			
			if ( qName.equalsIgnoreCase("leakedblocks")  && xwhat )
			{
				data = new StringBuilder();				
			}
			
			if ( qName.equalsIgnoreCase("stack") && currentError != null && currentError.getStacktrace() == null )
			{
				currentStacktrace = new ValgrindStacktrace();
			}
			
			if ( qName.equalsIgnoreCase("frame") && currentStacktrace != null )
			{
				currentStacktraceFrame = new ValgrindStacktraceFrame();
			}		
			
			if ( qName.equalsIgnoreCase("obj")  && currentStacktraceFrame != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("fn")  && currentStacktraceFrame != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("dir")  && currentStacktraceFrame != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("file")  && currentStacktraceFrame != null )
			{
				data = new StringBuilder();
			}
			
			if ( qName.equalsIgnoreCase("line")  && currentStacktraceFrame != null )
			{
				data = new StringBuilder();
			}			
		}

		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if ( currentReport == null )
				return;
			
			if ( qName.equalsIgnoreCase("valgrindoutput") && currentReport != null )
			{
				System.out.println( "current report summary: ");
				System.out.println( "        errors: " + currentReport.getErrorCount());
				for( ValgrindExaminedExecutable exe : currentReport.getExaminedExecutables() )
					System.out.println( "        exe: " + exe.getName());				
			}
			
			if ( qName.equalsIgnoreCase("error") && currentError != null )
			{
				currentError.setExecutable( currentExecutable );
				
				if ( currentError.getKind() != null )
					currentReport.addError( currentError );
				
				currentError = null;
			}	
			
			if ( qName.equalsIgnoreCase("unique") && currentError != null )
			{
				currentError.setUniqueId( data.toString() );
			}
			
			if ( qName.equalsIgnoreCase("kind")  && currentError != null )
			{		
				currentError.setKind( ValgrindErrorKind.valueOf( data.toString() ) );							
			}
			
			if ( qName.equalsIgnoreCase("what")  && currentError != null )
			{
				currentError.setDescription( data.toString() );
			}	
			
			if ( qName.equalsIgnoreCase("xwhat") )
			{
				xwhat = false;
			}
			
			if ( qName.equalsIgnoreCase("args") )
			{
				args = false;
			}
			
			if ( qName.equalsIgnoreCase("argv") )
			{
				argv = false;
			}
			
			if ( qName.equalsIgnoreCase("exe")  && args && argv )
			{
				currentExecutable = data.toString();
			}
			
			if ( qName.equalsIgnoreCase("text")  && xwhat )
			{
				currentError.setDescription( data.toString() );				
			}
			
			if ( qName.equalsIgnoreCase("leakedbytes")  && xwhat )
			{
				currentError.setLeakedBytes( Integer.valueOf(data.toString()) );				
			}
			
			if ( qName.equalsIgnoreCase("leakedblocks")  && xwhat )
			{
				currentError.setLeakedBlocks( Integer.valueOf(data.toString()) );
			}
			
			if ( qName.equalsIgnoreCase("stack") && currentStacktrace != null )
			{
				currentError.setStacktrace( currentStacktrace );
				currentStacktrace = null;				
			}
			
			if ( qName.equalsIgnoreCase("frame") && currentStacktraceFrame != null )
			{
				if ( currentStacktraceFrame.getLineNumber() != null )
					currentStacktraceFrame.setSourceCode( sourceCache.get(currentStacktraceFrame.getFilePath(), currentStacktraceFrame.getLineNumber().intValue() ) );
				
				currentStacktrace.addFrame( currentStacktraceFrame );
				currentStacktraceFrame = null;
			}
			
			if ( qName.equalsIgnoreCase("obj")  && currentStacktraceFrame != null )
			{
				currentStacktraceFrame.setObjectName( data.toString() );
			}
			
			if ( qName.equalsIgnoreCase("fn")  && currentStacktraceFrame != null )
			{
				currentStacktraceFrame.setFunctionName( data.toString() );
			}
			
			if ( qName.equalsIgnoreCase("dir")  && currentStacktraceFrame != null )
			{
				currentStacktraceFrame.setDirectoryName( data.toString() );
			}
			
			if ( qName.equalsIgnoreCase("file")  && currentStacktraceFrame != null )
			{
				currentStacktraceFrame.setFileName( data.toString() );
			}
			
			if ( qName.equalsIgnoreCase("line")  && currentStacktraceFrame != null )
			{		
				try
				{
					currentStacktraceFrame.setLineNumber( Integer.valueOf( data.toString() ) );
				}
				catch( NumberFormatException e )
				{
				}
			}			
			
			data = null;
		}

		public void characters(char ch[], int start, int length) throws SAXException
		{
			if ( data == null )
				return;
			
			data.append(new String(ch,start,length));
		}
		
		public ValgrindReport getReport()
		{
			return currentReport;
		}
	}
	
	public ValgrindSaxParser( ValgrindSourceCache sourceCache )
	{
		this.sourceCache = sourceCache;
	}
	
	public ValgrindReport parse( final File file ) throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = factory.newSAXParser();
		
		Handler handler = new Handler();
		
		saxParser.parse(file, handler);
		
		return handler.getReport();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new ValgrindSaxParser(new ValgrindSourceCache(10, 5)).parse(new File("core_test.memcheck") );
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
