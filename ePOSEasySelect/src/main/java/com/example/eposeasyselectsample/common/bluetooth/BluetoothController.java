package com.example.eposeasyselectsample.common.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;

// ------------------------------------------------------------------------------------------------
public class BluetoothController
{
	// --------------------------------------------------------------------------------------------
	public static final int REQUEST_ENABLE_BLUETOOTH = 1;

	// --------------------------------------------------------------------------------------------
	BluetoothAdapter	mAdapter = null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@return	boolean
	 *
	 */
	public BluetoothController()
	{
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	support Bluetooth
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isSuport()
	{
		if (null == mAdapter) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	check enable Bluetooth
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isEnabled()
	{
		if (null == mAdapter) {
			return false;
		}

		return mAdapter.isEnabled();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	enable Bluetooth
	 *
	 *	@param	activity
	 *	@param	enable true:enable / false:disable
	 *
	 *	@return	boolean
	 *
	 */
	public boolean setEnabled(
			Activity	activity,
			boolean		enable )
	{
		boolean	result	= false;

		if (null == mAdapter) {
			return false;
		}

		if (false != enable) {
			if (null != activity) {
				Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );

				try {
					activity.startActivityForResult( intent, REQUEST_ENABLE_BLUETOOTH );

				} catch (ActivityNotFoundException e) {
					// nothing
				}

				// if send to Activity, check to onActivityResult
				result = false;

			} else {
				result = mAdapter.enable();
			}

		} else {
			result = mAdapter.disable();
		}

		return result;
	}
}
