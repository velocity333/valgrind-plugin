package com.facinghell.valgrind.model;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

public class ValgrindReport
{
	private List<ValgrindInvalidReadError> invalidReadErrors;	
	private List<ValgrindInvalidWriteError> invalidWriteErrors;
	private List<ValgrindLeakError> leakErrors;
	
	public void print()
	{
		System.out.println("invalid reads: ");
		if (invalidReadErrors != null)
			for (ValgrindInvalidReadError error : invalidReadErrors)
				System.out.println(error.toString());

		System.out.println("invalid writes: ");
		if (invalidWriteErrors != null)
			for (ValgrindInvalidWriteError error : invalidWriteErrors)
				System.out.println(error.toString());

		System.out.println("leaks: ");
		if (leakErrors != null)
			for (ValgrindLeakError error : leakErrors)
				System.out.println(error.toString());
	}
	
	public void integrate( ValgrindReport valgrindReport )
	{
		if ( valgrindReport.invalidReadErrors != null )
		{
			for( ValgrindInvalidReadError error : valgrindReport.invalidReadErrors )
				addInvalidReadError( error );
		}
		
		if ( valgrindReport.invalidWriteErrors != null )
		{
			for( ValgrindInvalidWriteError error : valgrindReport.invalidWriteErrors )
				addInvalidWriteError( error );
		}
		
		if ( valgrindReport.leakErrors != null )
		{
			for( ValgrindLeakError error : valgrindReport.leakErrors )
				addLeakError( error );
		}		
	}
	
	@Exported
	public int getErrorCount()
	{
		return getInvalidReadErrorCount() + getInvalidWriteErrorCount() + getLeakErrorCount();
	}
	
	@Exported
	public int getInvalidReadErrorCount()
	{
		if ( invalidReadErrors != null )
			return invalidReadErrors.size();
		
		return 0;
	}
	
	@Exported
	public int getInvalidWriteErrorCount()
	{
		if ( invalidWriteErrors != null )
			return invalidWriteErrors.size();
		
		return 0;
	}
	
	@Exported
	public int getLeakErrorCount()
	{
		if ( leakErrors != null )
			return leakErrors.size();
		
		return 0;
	}	
	
	public void addInvalidReadError( ValgrindInvalidReadError invalidReadError )
	{
		if ( invalidReadErrors == null )
			invalidReadErrors = new ArrayList<ValgrindInvalidReadError>();
		
		invalidReadErrors.add( invalidReadError );
	}
	
	public void addInvalidWriteError( ValgrindInvalidWriteError invalidWriteError )
	{
		if ( invalidWriteErrors == null )
			invalidWriteErrors = new ArrayList<ValgrindInvalidWriteError>();
		
		invalidWriteErrors.add( invalidWriteError );
	}
	
	public void addLeakError( ValgrindLeakError leakError )
	{
		if ( leakErrors == null )
			leakErrors = new ArrayList<ValgrindLeakError>();
		
		leakErrors.add( leakError );
	}

	public List<ValgrindInvalidReadError> getInvalidReadErrors()
	{
		return invalidReadErrors;
	}

	public void setInvalidReadErrors(List<ValgrindInvalidReadError> invalidReadErrors)
	{
		this.invalidReadErrors = invalidReadErrors;
	}

	public List<ValgrindInvalidWriteError> getInvalidWriteErrors()
	{
		return invalidWriteErrors;
	}

	public void setInvalidWriteErrors(List<ValgrindInvalidWriteError> invalidWriteErrors)
	{
		this.invalidWriteErrors = invalidWriteErrors;
	}

	public List<ValgrindLeakError> getLeakErrors()
	{
		return leakErrors;
	}

	public void setLeakErrors(List<ValgrindLeakError> leakErrors)
	{
		this.leakErrors = leakErrors;
	}	
}
