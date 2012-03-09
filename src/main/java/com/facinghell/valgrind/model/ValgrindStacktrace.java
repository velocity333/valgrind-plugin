package com.facinghell.valgrind.model;

import java.util.ArrayList;
import java.util.List;

public class ValgrindStacktrace
{
	private List<ValgrindStacktraceFrame> frames;
	
	public String toString()
	{		
		String s = "";
		
		if ( frames == null )
			return "";
		
		for( ValgrindStacktraceFrame frame : frames )
		{
			s += frame.toString() + "\n\n";			
		}
		return s;
	}
	
	public void addFrame( ValgrindStacktraceFrame frame )
	{
		if ( frames == null )
			frames = new ArrayList<ValgrindStacktraceFrame>();
		
		frames.add( frame );
	}
	
	public int size()
	{
		if ( frames == null )
			return 0;
		
		return frames.size();
	}
	
	public boolean isEmpty()
	{
		return (frames == null || frames.isEmpty());
	}
	
	public ValgrindStacktraceFrame getFrame( int index )
	{
		if ( isEmpty() )
			throw new IllegalStateException("valgrind stacktrace is empty");
		
		return frames.get( index );
	}

	public List<ValgrindStacktraceFrame> getFrames()
	{
		return frames;
	}

	public void setFrames(List<ValgrindStacktraceFrame> frames)
	{
		this.frames = frames;
	}

}
