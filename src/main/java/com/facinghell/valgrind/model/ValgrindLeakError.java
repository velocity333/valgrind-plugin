package com.facinghell.valgrind.model;

import java.io.Serializable;

public class ValgrindLeakError extends ValgrindError implements Serializable
{
	private static final long serialVersionUID = 1564109517892278778L;
	
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
