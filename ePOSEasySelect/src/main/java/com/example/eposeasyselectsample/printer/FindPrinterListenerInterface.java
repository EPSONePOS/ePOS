package com.example.eposeasyselectsample.printer;

import java.util.EventListener;

import com.epson.epsonio.DeviceInfo;

public interface FindPrinterListenerInterface
	extends	EventListener
{
	// --------------------------------------------------------------------------------------------
	/**
	 *	find result listener
	 *
	 *	@param	deviceInfoList
	 *
	 */
	public void findPrinterListener(
			DeviceInfo[]	deviceInfoList );
}
