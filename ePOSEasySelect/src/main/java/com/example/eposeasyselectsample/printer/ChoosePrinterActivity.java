package com.example.eposeasyselectsample.printer;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.epson.epsonio.DevType;
import com.epson.epsonio.DeviceInfo;
import com.example.eposeasyselectsample.R;
import com.example.eposeasyselectsample.common.ActivityInfo;
import com.example.eposeasyselectsample.common.BroadcastCallback;
import com.example.eposeasyselectsample.common.BroadcastManager;
import com.example.eposeasyselectsample.common.MessageBox;
import com.example.eposeasyselectsample.common.bluetooth.BluetoothController;
import com.example.eposeasyselectsample.common.bluetooth.IntentListBluetooth;
import com.example.eposeasyselectsample.common.wifi.IntentListWiFi;
import com.example.eposeasyselectsample.common.wifi.WiFiController;

// ------------------------------------------------------------------------------------------------
public class ChoosePrinterActivity
	extends	Activity
	implements	OnItemClickListener,
				OnCheckedChangeListener,
				FindPrinterListenerInterface,
				PrinterInfoCallback,
				BroadcastCallback
{
	// --------------------------------------------------------------------------------------------
	private final static int	DISCOVERY_INTERVAL			= 100;
	private final static int	FINDING_TIME_WIFI			= 10000;
	private final static int	FINDING_TIME_BLUETOOTH		= 5000;

	private final static String	LV_PRINTER_NAME		= "PrinterName";
	private final static String	LV_ADDRESS			= "Address";

	private final static int	FIND_FILTER[] = {
		DevType.TCP,
		DevType.BLUETOOTH,

		DevType.ANY
	};

	private final static int mFilterRadioID[] = {
			R.id.CP_Rdo_FindFilter_Network,
			R.id.CP_Rdo_FindFilter_Bluetooth,
		};

	// --------------------------------------------------------------------------------------------
	private TextView							mtxtFindMessage			= null;
	private ProgressBar							mprgFinding				= null;
	private ListView							mlistFindPrinter		= null;

	private FindPrinter							mFindPrinter			= null;
	private int									mFilter					= DevType.TCP;

	private ArrayList<HashMap<String, String>>	mPrinterList			= null;
	private SimpleAdapter						mPrinterListAdapter		= null;

	private long		 						mFindStartTime			= 0;

	private DeviceInfo[]						mDeviceInfoList			= null;

	private BroadcastManager					mBroadcastManager		= null;

	private WiFiController						mWifiController			= null;
	private BluetoothController					mBluetoothController	= null;

	private int									mFindTimeout			= FINDING_TIME_WIFI;

	private Thread								mFindStartThread		= null;

	private boolean								mIsConnecting			= false;

	private ActivityInfo						mActivityInfo			= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	onCreate
	 *
	 *	@param	savedInstanceState
	 *
	 */
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		mIsConnecting = false;

		setContentView( R.layout.activity_choose_printer );
		setResult( RESULT_CANCELED );

		mActivityInfo = (ActivityInfo)getIntent().getSerializableExtra( ActivityInfo.CLASS_NAME );

		mtxtFindMessage = (TextView) findViewById( R.id.CP_Msg_FindMessage );
		mprgFinding = (ProgressBar) findViewById( R.id.CP_Prg_Finding );
		mlistFindPrinter = (ListView)findViewById( R.id.CP_List_Printer );
		mlistFindPrinter.setVisibility( View.GONE );

		// Bloadcast
		mBroadcastManager = new BroadcastManager();

		initPrinterList();

		mWifiController = new WiFiController( this );
		mBluetoothController = new BluetoothController();

		// find
		mFindPrinter = new FindPrinter( this );

		initFindFilter();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onResume
	 *
	 */
	@Override
	protected void onResume()
	{
		super.onResume();

		if (false != checkInterface()) {
			if (false == mIsConnecting) {
				startFindPrinter();
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onDestroy
	 *
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onPause
	 *
	 */
	@Override
	protected void onPause()
	{
		super.onPause();

		mFindStartThread = null;

		stopFindPrinter();

		if (null != mBroadcastManager) {
			mBroadcastManager.unregisterFilter( this );
			mBroadcastManager.unregistCallback();
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onItemSelected
	 *
	 *	@param	parent
	 *	@param	view
	 *	@param	position
	 *	@param	id
	 *
	 */
	@Override
	public void onItemClick(
			AdapterView<?>	parent,
			View			view,
			int				position,
			long			id )
	{
		switch (parent.getId()) {
		case R.id.CP_List_Printer:
			runPrinterInfoTask( position );
			break;

		default:
			break;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onCheckedChanged
	 *
	 *	@param	group
	 *	@param	checkedId
	 *
	 */
	@Override
	public void onCheckedChanged(
			RadioGroup	group,
			int			checkedId )
	{
		switch (group.getId()) {
		case R.id.CP_RdoGp_FindFilter:
			RadioButton radioButton = (RadioButton) findViewById( checkedId );

			if (false == radioButton.isChecked()) {
				break;
			}

			// stop find printer
			stopFindPrinter();

			int id = 0;

			for (int filter : mFilterRadioID) {
				if (checkedId == filter) {
					break;
				}
				id++;
			}

			if (mFilterRadioID.length < (id + 1)) {
				id = 0;
			}

			changeFindFilter( id );

			if (false != checkInterface()) {
				// restrat
				startFindPrinter();
			}
			break;

		default:
			break;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	find result
	 *
	 *	@param	deviceInfoList
	 *
	 */
	@Override
	public void findPrinterListener(
			DeviceInfo[]	deviceInfoList )
	{
		updatePrinterList( deviceInfoList );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	検索結果
	 *
	 *	@param	requestCode
	 *	@param	resultCode
	 *	@param	resultPrinterInfo
	 *
	 */
	@Override
	public void printerInfoCallback(
			final int					requestCode,
			final int					resultCode,
			final PrinterInformation	resultPrinterInfo )
	{
		if (PrinterInfoAsyncTask.REQUEST_PRINTER_INFO != requestCode) {
			return ;
		}

		mIsConnecting = false;

		if (ComEpsonIo.RESULT_SUCCESS != resultCode) {
			msgCommunicationError( resultCode );

			startFindPrinter();

			return ;
		}

		if (null == resultPrinterInfo) {
			startFindPrinter();
			return ;
		}

		PrinterInformation printerInfo = new PrinterInformation();

		printerInfo.setDeviceType( resultPrinterInfo.getDeviceType() );
		printerInfo.setPrinterName( resultPrinterInfo.getPrinterName() );
		printerInfo.setAddress( resultPrinterInfo.getAddress() );
		printerInfo.setLanguage( resultPrinterInfo.getLanguage() );

		// return before activity

		if (null != mActivityInfo) {
			Intent intent = new Intent( this, mActivityInfo.getReturnActivity() );

			intent.putExtra( PrinterInformation.CLASS_NAME, printerInfo );

			intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
			intent.addFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );

			setResult( RESULT_OK, intent );
		}

		finish();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	BloaccastReceiver onReceive
	 *
	 *	@param	context
	 *	@param	intent
	 *
	 */
	@Override
	public void broadcastCallback(
			Context	context,
			Intent	intent )
	{
		if (false != intent.getAction().equals( WifiManager.WIFI_STATE_CHANGED_ACTION )) {
			int status	= intent.getIntExtra( WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN );

			// enable Wi-Fi
			if (WifiManager.WIFI_STATE_ENABLED == status) {
				mBroadcastManager.unregisterFilter( this );
				mBroadcastManager.unregistCallback();

				// find start
				startFindPrinter();
			}

		} else if (false != intent.getAction().equals( BluetoothAdapter.ACTION_STATE_CHANGED )) {
			int status	= intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF );

			// eanble Bluetooth
			if (BluetoothAdapter.STATE_ON == status) {
				mBroadcastManager.unregisterFilter( this );
				mBroadcastManager.unregistCallback();

				// find start
				startFindPrinter();
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	initialize printer list
	 *
	 */
	private void initPrinterList()
	{
		mPrinterList		= new ArrayList<HashMap<String, String>>();
		mPrinterListAdapter	= new SimpleAdapter(	this,
													mPrinterList,
													R.layout.list_layout_find_printer,
													new String[] {
														LV_PRINTER_NAME,
														LV_ADDRESS },
													new int[] {
														R.id.list_item_PrinterName,
														R.id.list_item_MacAddress } );

		ListView list = (ListView)findViewById( R.id.CP_List_Printer );

		list.setAdapter( mPrinterListAdapter );
		list.setOnItemClickListener( this );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	initialize find filter
	 *
	 */
	private void initFindFilter()
	{
		int id = 0;

		for (int filter : FIND_FILTER) {
			if (mFilter == filter) {
				break;
			}
			id++;
		}

		if (FIND_FILTER.length < (id + 1)) {
			id = 0;
		}

		RadioButton filterButton = (RadioButton) findViewById( mFilterRadioID[id] );

		filterButton.setChecked( true );

		RadioGroup radioGroup = (RadioGroup) findViewById( R.id.CP_RdoGp_FindFilter );

		radioGroup.setOnCheckedChangeListener( this );

		// update
		changeFindFilter( id );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	change find filter
	 *
	 *	@param	filter
	 *
	 */
	private void changeFindFilter(
			int	filter )
	{
		mFilter = FIND_FILTER[filter];

		if (DevType.TCP == mFilter) {
			mFindTimeout = FINDING_TIME_WIFI;
		} else {
			mFindTimeout = FINDING_TIME_BLUETOOTH;
		}

		mlistFindPrinter.setVisibility( View.GONE );
		mtxtFindMessage.setText( getString( R.string.CP_Msg_NowFinding ) );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	check interface
	 *
	 *	@return	boolean
	 */
	private boolean checkInterface()
	{
		boolean	result	= true;

		if (DevType.TCP == mFilter) {
			if (false == mWifiController.isSuport()) {
				result = false;

			} else {
				// check Wi-Fi settign
				if (false == mWifiController.isEnabled()) {
					msgWiFiEnabled();

					result = false;
				}
			}
		}

		if (DevType.BLUETOOTH == mFilter) {
			if (false == mBluetoothController.isSuport()) {
				result = false;

			} else {
				// check Bluetooth setting
				if (false == mBluetoothController.isEnabled()) {
					msgBluetoothEnabled();

					result = false;
				}
			}
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	start find printer
	 *
	 */
	private void startFindPrinter()
	{
		if (null == mFindPrinter) {
			return ;
		}

		mlistFindPrinter.setVisibility( View.GONE );

		stopFindPrinter();

		mFindStartTime = System.currentTimeMillis();

		// clear list
		updatePrinterList( null );

		try{
			boolean	result	= false;

			// find setting
			mFindPrinter.setFilter( mFilter );
			mFindPrinter.setInterval( DISCOVERY_INTERVAL );
			mFindPrinter.registFindPrinterListener( this );

			mFindStartThread = null;

			result = mFindPrinter.startFindPrinter();
			if (false == result) {
				// If you're just after you enable Wi-Fi, Do not be connected immediately.
				// wait until the search is able to start.
				mFindStartThread = new Thread( new Runnable()
				{
					public void run()
					{
						while (false == mFindPrinter.startFindPrinter()) {
							try {
								Thread.sleep( 500 );
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
    			});

				mFindStartThread.start();
			}

		} catch (Exception e) {
			mFindStartTime = 0;
			return ;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	stop find printer
	 *
	 */
	private void stopFindPrinter()
	{
		if (null == mFindPrinter) {
			return ;
		}

		mFindStartTime = 0;

		mFindPrinter.stopFindPrinter();

		mFindPrinter.unregistFindPrinterListener();

		// clear printer list
		mPrinterList.clear();
		mPrinterListAdapter.notifyDataSetChanged();

		mlistFindPrinter.setVisibility( View.GONE );
		mtxtFindMessage.setVisibility( View.GONE );
		mprgFinding.setVisibility( View.GONE );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	update printer list
	 *
	 *	@param	deviceInfoList
	 *
	 */
	private void updatePrinterList(
			DeviceInfo[]	deviceInfoList )
	{
		Boolean	update	= false;
		int		visibleText		= View.VISIBLE;
		int		visibleProgress	= View.VISIBLE;

		mDeviceInfoList = deviceInfoList;

		if ((null == deviceInfoList) || (0 == deviceInfoList.length)) {
			// not found
			mPrinterList.clear();
			visibleText = View.VISIBLE;

			if (0 != mFindStartTime) {
				long findResultTime = System.currentTimeMillis();

				if (mFindTimeout < (findResultTime - mFindStartTime) ) {
					mFindStartTime = 0;
				}
			}

		} else {
			// printer is find
			visibleText		= View.GONE;
			visibleProgress	= View.GONE;
			mFindStartTime	= 0;

			if (deviceInfoList.length != mPrinterList.size()) {
				update = true;

			} else {
				// check resutl
				for (DeviceInfo info : deviceInfoList) {
					update = true;

					try {
						for (HashMap<String, String> printer : mPrinterList) {
							String	address = printer.get( LV_ADDRESS );
							boolean	find	= false;

							if (false == printer.get( LV_PRINTER_NAME ).equalsIgnoreCase( info.getPrinterName()) ) {
								update = false;
								break;
							}

							switch (info.getDeviceType()) {
							case DevType.TCP:		find = address.equalsIgnoreCase( info.getMacAddress() );	break;
							case DevType.BLUETOOTH:	find = address.equalsIgnoreCase( info.getDeviceName() );	break;
							default:																			break; }

							if (false == find) {
								update = false;
								break;
							}
						}
					} catch (Exception e) {
						// nothing
					}

					if (false != update) {
						break;
					}
				}
			}
		}

		// update printer list
		if (false != update) {
			mPrinterList.clear();

			for (DeviceInfo info : deviceInfoList) {
				HashMap<String, String> item = new HashMap<String, String>();
				String	address = "";

				// printer name
				item.put( LV_PRINTER_NAME, info.getPrinterName() );

				// Mac address
				switch (info.getDeviceType()) {
				case DevType.TCP:		address = info.getMacAddress();	break;
				case DevType.BLUETOOTH:	address = info.getDeviceName();	break;
				default:												break; }

				item.put( LV_ADDRESS, address );

				mPrinterList.add( item );
			}

			// show control
			if (View.GONE == mlistFindPrinter.getVisibility()) {
				mlistFindPrinter.setVisibility( View.VISIBLE );
			}
		}

		mPrinterListAdapter.notifyDataSetChanged();
		mtxtFindMessage.setVisibility( visibleText );

		// hide progress
		mprgFinding.setVisibility( visibleProgress );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	run get printer info thread
	 *
	 *	@param	id
	 *
	 */
	private void runPrinterInfoTask(
			int	id )
	{
		PrinterInfoAsyncTask task = new PrinterInfoAsyncTask( this, this, mDeviceInfoList[id], true );
		stopFindPrinter();

		try {
			mIsConnecting = true;

			task.execute();
		} catch (IllegalStateException e) {
			// nothing
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	message box enable Wi-Fi
	 *
	 */
	 private void msgWiFiEnabled()
	 {
		MessageBox	msgBox = new MessageBox( this )
		{
			// ------------------------------------------------------------------------------------
			/**
			 *	onButtonClick
			 *
			 *	@param	dialog
			 *	@param	which
			 *
			 */
			@Override
			protected void onButtonClick(
					DialogInterface	dialog,
					int				which )
			{
				switch (which) {
				case (DialogInterface.BUTTON_POSITIVE):
					// enable Wi-Fi
					IntentFilter	broadcastFilter = null;

					broadcastFilter = new IntentFilter();
					for (IntentListWiFi i : IntentListWiFi.values()) {
						broadcastFilter.addAction( i.getAction() );
					}

					// check to Broadcast
					mBroadcastManager.registFilter( ChoosePrinterActivity.this, broadcastFilter );
					mBroadcastManager.registCallback( ChoosePrinterActivity.this );

					mWifiController.setEnabled( true );
					break;

				default:
					break;
				}
			}
		};

		msgBox.intMessageBox(	null,
								getString( R.string.CP_Msg_TurnOnWiFi ),
								getString( R.string.dialog_btn_yes ),
								getString( R.string.dialog_btn_no ),
								null );
		msgBox.show();
	 }

	// --------------------------------------------------------------------------------------------
	/**
	 *	message box enable Bluetooth
	 *
	 */
	 private void msgBluetoothEnabled()
	 {
		MessageBox	msgBox = new MessageBox( this )
		{
			// ------------------------------------------------------------------------------------
			/**
			 *	onButtonClick
			 *
			 *	@param	dialog
			 *	@param	which
			 *
			 */
			@Override
			protected void onButtonClick(
					DialogInterface	dialog,
					int				which )
			{
				switch (which) {
				case (DialogInterface.BUTTON_POSITIVE):
					// enable Bluetooth
					IntentFilter	broadcastFilter	= null;

					broadcastFilter = new IntentFilter();
					for (IntentListBluetooth i : IntentListBluetooth.values()) {
						broadcastFilter.addAction( i.getAction() );
					}

					// check to Broadcast
					mBroadcastManager.registFilter( ChoosePrinterActivity.this, broadcastFilter );
					mBroadcastManager.registCallback( ChoosePrinterActivity.this );

					mBluetoothController.setEnabled( null, true );
					break;

				default:
					break;
				}
			}
		};

		msgBox.intMessageBox(	null,
								getString( R.string.CP_Msg_TurnOnBluetooth ),
								getString( R.string.dialog_btn_yes ),
								getString( R.string.dialog_btn_no ),
								null );
		msgBox.show();
	 }

	// --------------------------------------------------------------------------------------------
	/**
	 *	error message box
	 *
	 *	@param	resultCode
	 *
	 */
	 private void msgCommunicationError(
	 		int resultCode )
	 {
		MessageBox	msgBox = new MessageBox( this )
		{
			// ------------------------------------------------------------------------------------
			/**
			 *	onButtonClick
			 *
			 *	@param	dialog
			 *	@param	which
			 *
			 */
			@Override
			protected void onButtonClick(
					DialogInterface	dialog,
					int				which )
			{
				// nothing
			}
		};

		int	id	= R.string.CP_Msg_ErrorCommunication;

		switch (resultCode) {
		case (ComEpsonIo.RESULT_ERROR_OPEN):		id = R.string.CP_Msg_ErrorCommunication;	break;
		case (ComEpsonIo.RESULT_ERROR_TIME_OUT):	id = R.string.CP_Msg_ErrorCommunication;	break;
		case (ComEpsonIo.RESULT_ERROR_NOT_SUPPORT):	id = R.string.CP_Msg_ErrorNotSupport;		break;
		default:									id = R.string.CP_Msg_ErrorCommunication;	break; }

		msgBox.intMessageBox(	null,
								getString( id ),
								getString( R.string.dialog_btn_ok ),
								null,
								null );
		msgBox.show();
	 }
}
