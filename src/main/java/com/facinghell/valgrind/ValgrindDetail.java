package com.facinghell.valgrind;

import hudson.model.AbstractBuild;

import com.facinghell.valgrind.model.ValgrindStacktrace;

public class ValgrindDetail
{
	private ValgrindStacktrace stacktrace;
	final private AbstractBuild<?, ?> owner;
	
	ValgrindDetail( AbstractBuild<?, ?> owner, ValgrindStacktrace strackrace )
	{
		this.owner = owner;
		this.stacktrace = strackrace;
	}

	public ValgrindStacktrace getStacktrace()
	{
		return stacktrace;
	}

	public void setStacktrace(ValgrindStacktrace stacktrace)
	{
		this.stacktrace = stacktrace;
	}

	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}

}