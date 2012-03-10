package com.facinghell.valgrind.model;

public class ValgrindError
{
	private String origin;
	private String uniqueId;	
	private ValgrindErrorKind kind;
	private ValgrindStacktrace stacktrace;
	private String description;
	
	public String toString()
	{
		return 
		"kind: " + kind + "\n" +
		"text: " + description + "\n" +
		"stack: " + stacktrace.toString();
	}	
	
	public ValgrindStacktrace getStacktrace()
	{
		return stacktrace;
	}
	public void setStacktrace(ValgrindStacktrace stacktrace)
	{
		this.stacktrace = stacktrace;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public ValgrindErrorKind getKind()
	{
		return kind;
	}
	public void setKind(ValgrindErrorKind kind)
	{
		this.kind = kind;
	}

	public String getOrigin()
	{
		return origin;
	}

	public void setOrigin(String origin)
	{
		this.origin = origin;
	}

	public String getUniqueId()
	{
		return uniqueId;
	}

	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}

}
