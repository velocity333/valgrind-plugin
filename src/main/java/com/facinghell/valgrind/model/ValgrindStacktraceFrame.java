package com.facinghell.valgrind.model;

import java.io.Serializable;

public class ValgrindStacktraceFrame implements Serializable
{
	private static final long serialVersionUID = -2774574337822108808L;
	
	private String objectName;
	private String directoryName;
	private String fileName;
	private Integer lineNumber;
	private String functionName;
	
	private String sourceCode;
	
	public String toString()
	{
		return 
		"object    : " + objectName + "\n" +
		"function  : " + functionName + "\n" +
		"directory : " + directoryName + "\n" +
		"file      : " + fileName + "\n" +
		"lineNumber: " + lineNumber;
	}
	
	public String getObjectName()
	{
		return objectName;
	}
	public void setObjectName(String objectName)
	{
		this.objectName = objectName;
	}
	public String getDirectoryName()
	{
		return directoryName;
	}
	public void setDirectoryName(String directoryName)
	{
		this.directoryName = directoryName;
	}
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public Integer getLineNumber()
	{
		return lineNumber;
	}
	public void setLineNumber(Integer lineNumber)
	{
		this.lineNumber = lineNumber;
	}
	public String getFunctionName()
	{
		return functionName;
	}
	public void setFunctionName(String functionName)
	{
		this.functionName = functionName;
	}

	public String getSourceCode()
	{
		return sourceCode;
	}

	public void setSourceCode(String sourceCode)
	{
		this.sourceCode = sourceCode;
	}

}
