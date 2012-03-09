package com.facinghell.valgrind.model;

public class ValgrindLeakError extends ValgrindError
{
	private Integer leakedBytes;
	private Integer leakedBlocks;
	
	public Integer getLeakedBytes()
	{
		return leakedBytes;
	}
	public void setLeakedBytes(Integer leakedBytes)
	{
		this.leakedBytes = leakedBytes;
	}
	public Integer getLeakedBlocks()
	{
		return leakedBlocks;
	}
	public void setLeakedBlocks(Integer leakedBlocks)
	{
		this.leakedBlocks = leakedBlocks;
	}

}
