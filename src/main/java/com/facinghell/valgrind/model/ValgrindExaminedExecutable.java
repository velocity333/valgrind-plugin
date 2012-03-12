package com.facinghell.valgrind.model;

public class ValgrindExaminedExecutable 
{
	private String name;
	private ValgrindReport report;
	
	public ValgrindExaminedExecutable( String name, ValgrindReport report )
	{
		this.name = name;
		this.report = report;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public int getInvalidReadErrorCount()
	{		
		return report.getErrorCountByKindAndExecutable( ValgrindErrorKind.InvalidRead, name );
	}
	
	public int getInvalidWriteErrorCount()
	{
		return report.getErrorCountByKindAndExecutable( ValgrindErrorKind.InvalidWrite, name );
	}
	
	public int getLeakedDefinitelyErrorCount()
	{
		return report.getErrorCountByKindAndExecutable( ValgrindErrorKind.Leak_DefinitelyLost, name );
	}
	
	public int getLeakedDefinitelyByteCount()
	{
		return report.getLeakedBytes( ValgrindErrorKind.Leak_DefinitelyLost, name );
	}	
	
	public int getLeakedPossiblyErrorCount()
	{
		return report.getErrorCountByKindAndExecutable( ValgrindErrorKind.Leak_PossiblyLost, name );
	}	
	
	public int getLeakedPossiblyByteCount()
	{
		return report.getLeakedBytes( ValgrindErrorKind.Leak_PossiblyLost, name );
	}	
	
	public int getUninitValueErrorCount()
	{
		return report.getErrorCountByKindAndExecutable( ValgrindErrorKind.UninitValue, name );
	}	
	
	public int getUninitCondErrorCount()
	{
		return report.getErrorCountByKindAndExecutable( ValgrindErrorKind.UninitCondition, name );
	}	
}
