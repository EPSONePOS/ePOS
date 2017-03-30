package com.example.eposeasyselectsample.common.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;

// ------------------------------------------------------------------------------------------------
public class WiFiController
{
	// --------------------------------------------------------------------------------------------
	public static final int REQUEST_ENABLE_BLUETOOTH = 1;

	// --------------------------------------------------------------------------------------------
	WifiManager	mManager	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@return	boolean
	 *
	 */
	public WiFiController(
			Context	context )
	{
		mManager = (WifiManager)context.getSystemService( Context.WIFI_SERVICE );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	supprot Wi-Fi
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isSuport()
	{
		if (null == mManager) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	enable Wi-Fi
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isEnabled()
	{
		if (null == mManager) {
			return false;
		}

		return mManager.isWifiEnabled();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	enable Wi-Fi
	 *
	 *	@param	activity
	 *	@param	enable true:enable / false:disable
	 *
	 *	@return	boolean
	 *
	 */
	public boolean setEnabled(
			boolean	enable )
	{
		if (null == mManager) {
			return false;
		}

		return mManager.setWifiEnabled( enable );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get Wi-Fi status
	 *
	 *
	 *	@return	int
	 *
	 */
	 public int getWiFiStatus()
	 {
		if (null == mManager) {
			return WifiManager.WIFI_STATE_UNKNOWN;
		}

		return mManager.getWifiState();
	 }
}
