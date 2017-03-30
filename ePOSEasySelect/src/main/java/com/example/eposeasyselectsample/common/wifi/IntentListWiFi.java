package com.example.eposeasyselectsample.common.wifi;

public enum IntentListWiFi
{
	INTENT_WIFI_STATE_CHANGE			("android.net.wifi.STATE_CHANGE"),
	INTENT_WIFI_WIFI_STATE_CHANGED		("android.net.wifi.WIFI_STATE_CHANGED"),
	INTENT_WIFI_CONNECTION_CHANGE		("android.net.wifi.supplicant.CONNECTION_CHANGE"),
	INTENT_WIFI_supplicant_STATE_CHANGE	("android.net.wifi.supplicant.STATE_CHANGE");

	private String _action;

	private IntentListWiFi(
			String	action )
	{
		_action = action;
	}

	public String getAction()
	{
		return _action;
	}
}
