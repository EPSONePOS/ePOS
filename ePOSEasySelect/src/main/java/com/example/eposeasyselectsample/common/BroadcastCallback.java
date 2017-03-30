package com.example.eposeasyselectsample.common;

import android.content.Context;
import android.content.Intent;

// ------------------------------------------------------------------------------------------------
public interface BroadcastCallback
{
	// --------------------------------------------------------------------------------------------
	/**
	 *	Broadcast Callback
	 *
	 *	@param	context
	 *	@param	intent
	 *
	 */
	public void broadcastCallback(
			Context	context,
			Intent	intent );
}
