package com.example.eposeasyselectsample.common;

import java.io.Serializable;

public class ActivityInfo
		implements Serializable
{
	// --------------------------------------------------------------------------------------------
	public static final String	CLASS_NAME	= "screen_info";

	// --------------------------------------------------------------------------------------------
	private static final long serialVersionUID = 4910056698827774680L;

	// --------------------------------------------------------------------------------------------
	private Class<?>			mReturnScreen	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	get return activity
	 *
	 *	@return	Class<?>
	 *
	 */
	public Class<?> getReturnActivity()
	{
		return mReturnScreen;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set return activity
	 *
	 *	@param	Class<?>
	 *
	 */
	public void setReturnActivity(
			Class<?> screen )
	{
		mReturnScreen = screen;
	}
}
