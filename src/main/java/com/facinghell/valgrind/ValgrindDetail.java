package com.facinghell.valgrind;

import hudson.model.AbstractBuild;

import com.facinghell.valgrind.model.ValgrindError;

public class ValgrindDetail
{
	private ValgrindError error;
	final private AbstractBuild<?, ?> owner;
	
	ValgrindDetail( AbstractBuild<?, ?> owner, ValgrindError error )
	{
		this.owner = owner;
		this.error = error;
	}

	public ValgrindError getError()
	{
		return error;
	}

	public void setError(ValgrindError error)
	{
		this.error = error;
	}

	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}
}