package com.example.eposeasyselectsample.printer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;

import com.epson.epsonio.DevType;
import com.epson.epsonio.DeviceInfo;
import com.epson.epsonio.EpsonIoException;
import com.epson.epsonio.FilterOption;
import com.epson.epsonio.Finder;
import com.epson.epsonio.IoStatus;

// ------------------------------------------------------------------------------------------------
public class FindPrinter
		implements	Runnable

{
	// --------------------------------------------------------------------------------------------
	private final static int	DISCOVERY_INTERVAL	= 500;

	// --------------------------------------------------------------------------------------------
	private Context							mContext	= null;
	private int								mFilter		= DevType.ANY;
	private int								mInterval	= DISCOVERY_INTERVAL;	// interval

	private FindPrinterListenerInterface	mListener	= null;

	private ScheduledExecutorService		mScheduler	= null;
	private ScheduledFuture<?>				mFuture		= null;
	private Handler							mHandler	= new Handler();


	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@param	context
	 *
	 */
	protected FindPrinter(
			Context	context )
	{
		mContext = context;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	printer find thred
	 *
	 */
	@Override
	public synchronized void run()
	{
		// ----------------------------------------------------------------------------------------
		class UpdatePrinterListThread extends Thread
		{
			DeviceInfo[] mDeviceInfoList = null;

			// ------------------------------------------------------------------------------------
			/**
			 *	update printer list
			 *
			 *	@param	deviceInfo
			 *
			 */
			protected UpdatePrinterListThread(
					DeviceInfo[]	deviceInfoList )
			{
				mDeviceInfoList = deviceInfoList;
            }

			// ------------------------------------------------------------------------------------
			/**
			 *	update printer list thread
			 *
			 */
			@Override
			public void run()
			{
				if (null != mListener) {
					mListener.findPrinterListener( mDeviceInfoList );
				}
			}
		}

        try{
			DeviceInfo[] deviceInfoList = null;

			deviceInfoList = getDeviceInfo();

			mHandler.post( new UpdatePrinterListThread( deviceInfoList ) );

		} catch (Exception e) {
			return;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regist Listener
	 *
	 *	@param	listener
	 *
	 */
	protected void registFindPrinterListener(
			FindPrinterListenerInterface	listener )
	{
		mListener = listener;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	unregist Listener
	 *
	 */
	protected void unregistFindPrinterListener()
	{
		mListener = null;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	find filter
	 *
	 *	@param	filter
	 *
	 */
	protected void setFilter(
			int	filter )
	{
		mFilter = filter;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set find intreval
	 *
	 *	@param	interval
	 *
	 */
	protected void setInterval(
			int	interval )
	{
		mInterval = interval;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	start find printer
	 *
	 */
	protected boolean startFindPrinter()
	{
		if (null == mContext) {
			return false;
		}

		// stop old finder
		stopFindPrinter();

		if (null == mScheduler) {
			mScheduler = Executors.newSingleThreadScheduledExecutor();
		}

		try{
			String	option	= null;

			switch (mFilter) {
			case DevType.TCP:
				option = "255.255.255.255";
				break;

			case DevType.BLUETOOTH:
				break;

			case DevType.NONE:
			default:
				break;
			}

			Finder.start( mContext, mFilter, option );

		} catch (Exception e) {
			return false;
		}

		// start thread
		mFuture = mScheduler.scheduleWithFixedDelay( this, 0, mInterval, TimeUnit.MILLISECONDS );

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get device info
	 *
	 *	@return	 DeviceInfo[]
	 *
	 */
	protected DeviceInfo[] getDeviceInfo()
	{
		DeviceInfo[]	deviceInfoList	= null;

		try{
			deviceInfoList = Finder.getDeviceInfoList( FilterOption.FILTER_NONE );

		} catch (Exception e) {
			deviceInfoList = null;
		}

		return deviceInfoList;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	stop find printer
	 *
	 */
	protected void stopFindPrinter()
	{
		while (true) {
			try {
				Finder.stop();
				break;

			} catch (EpsonIoException e) {
				if (e.getStatus() != IoStatus.ERR_PROCESSING) {
					break;
				}
			}
		}

		// stop find thread
		if (null != mFuture) {
			mFuture.cancel( false );

			while (!mFuture.isDone()) {
				try{
					Thread.sleep( DISCOVERY_INTERVAL );

				}catch (Exception e){
					break;
				}
			}

			mFuture = null;
		}

		//
		if (null != mScheduler) {
			mScheduler.shutdown();
			mScheduler = null;
		}
	}
}
