package com.example.eposeasyselectsample.quickpairing.nfc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.provider.Settings;

// ------------------------------------------------------------------------------------------------
public class NFCController
{
	// --------------------------------------------------------------------------------------------
	public static final int REQUEST_SHOW_NFC_SETTING = 1;

	// --------------------------------------------------------------------------------------------
	private NfcAdapter		mNfcAdapter		= null;
	private PendingIntent	mPendingIntent	= null;
	private IntentFilter[]	mFilters		= null;
	private String[][]		mTechLists		= null;

	private boolean			mWaiteScanNfc	= false;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@return	boolean
	 *
	 */
	public NFCController(
			Context	context )
	{
		mNfcAdapter = NfcAdapter.getDefaultAdapter( context );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	Support confirmation of NFC
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isSuport()
	{
		if (null == mNfcAdapter) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	enable check
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isEnabled()
	{
		if (null == mNfcAdapter) {
			return false;
		}

		return mNfcAdapter.isEnabled();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	show NFC setting (OS setting)
	 *
	 *	@param	context
	 *
	 */
	@SuppressLint("InlinedApi")
	public void showNfcSetting(
			Context	context )
	{
		if (null == mNfcAdapter) {
			return ;
		}

		String	settiing = Settings.ACTION_AIRPLANE_MODE_SETTINGS;

		// API Level 16 or higher
		if (16 <= Build.VERSION.SDK_INT) {
			settiing = Settings.ACTION_NFC_SETTINGS;
		}

		Intent intent = new Intent( settiing );

		try {
			context.startActivity( intent );
		} catch (ActivityNotFoundException e) {
			// nothing
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set PendingIntent
	 *
	 *	@param	pendingIntent
	 *
	 */
	public void setPendingIntent(
			PendingIntent	pendingIntent )
	{
		mPendingIntent = pendingIntent;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set NFC filter
	 *
	 *	@param	filters
	 *
	 */
	public void setNfcfilter(
			IntentFilter[]	filters )
	{
		mFilters = filters;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set Tech list
	 *
	 *	@param	techListes
	 *
	 */
	public void setTechLists(
			String[][]	techListes )
	{
		mTechLists = techListes;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	Dispatch switching NFC
	 *
	 *	@param	activity
	 *	@param	enable
	 *
	 */
	public void enableDispatch(
			Activity	activity,
			boolean		enable )
	{
		if (null == mNfcAdapter) {
			return ;
		}

		try {
			if (false != enable) {
				mNfcAdapter.enableForegroundDispatch( activity, mPendingIntent, mFilters, mTechLists );

			} else {
				mNfcAdapter.disableForegroundDispatch( activity );
			}

		} catch (IllegalStateException se) {
			return ;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	wait scan
	 *
	 *	@param	waite
	 *
	 */
	public void waiteScanNfc(
			boolean waite )
	{
		mWaiteScanNfc = waite;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	Scan start of NFC
	 *
	 *	@param	intent
	 *
	 *	@return	Tag
	 *
	 */
	public Tag scanNfc(
			Intent	intent )
	{
		if (false != mWaiteScanNfc) {
			return null;
		}

		Tag tag = (Tag) intent.getParcelableExtra( NfcAdapter.EXTRA_TAG );

		return tag;
	}
}
