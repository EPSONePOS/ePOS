package com.example.eposeasyselectsample.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

// --------------------------------------------------------------------------------------------
public class BroadcastManager
	extends BroadcastReceiver
{
	// --------------------------------------------------------------------------------------------
	BroadcastCallback	mCallback	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	onReceive
	 *
	 *	@param	context
	 *	@param	intent
	 *
	 */
	@Override
	public void onReceive(
			Context	context,
			Intent	intent )
	{
		if (null != mCallback) {
			mCallback.broadcastCallback( context, intent );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regisg Broadcast callack
	 *
	 *	@param	callback
	 *
	 */
	public void registCallback(
			BroadcastCallback	callback )
	{
		mCallback = callback;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	unregisg Bloaccast callbacl
	 *
	 */
	public void unregistCallback()
	{
		mCallback = null;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regisg Bloaccast filter
	 *
	 *	@param	context
	 *	@param	filter Broadcast filter
	 *
	 */
	public void registFilter(
			Context			context,
			IntentFilter	filter )
	{
		context.registerReceiver( BroadcastManager.this, filter );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	unregisg Bloaccast filter
	 *
	 */
	public void unregisterFilter(
			Context	context )
	{
		try {
			context.unregisterReceiver( BroadcastManager.this );
		} catch (Exception e) {
			// nothing
		}
	}
}
