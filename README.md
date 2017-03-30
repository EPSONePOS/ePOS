#  ePOS-Print SDK for Android

![N|Solid](http://global.epson.com/assets/img/logo.png)
 Version 1.9.0a
 Copyright Seiko Epson Corporation 2012-2015 All rights reserved.

# About this software!
The ePOS-Print SDK for Android is an SDK aimed at development engineers who 
are developing Android applications for printing on an EPSON TM printer.
Applications are developed using the APIs provided by ePOS-Print SDK.
ePOS-Print SDK for iOS for iOS devices is also provided in ePOS-Print SDK.
For detailed information, please see ePOS-Print SDK for Android User's Manual.
  - Android Versions
      Android 2.3.3 to 2.3.7
      Android 3.1 to 3.2.2
      Android 4.0 to 4.4
      Android 5.0 to 5.1
 - Android Devices
      ARMv5TE-based Android devices 
 -  Supported Printers
     EPSON TM-T88V
     EPSON TM-T70
     EPSON TM-T70II
  EPSON TM-T20
  EPSON TM-T82
  EPSON TM-T81II
  EPSON TM-T82II
  EPSON TM-U220 series
  EPSON TM-P60
  EPSON TM-P60II
  EPSON TM-T90II
  EPSON TM-T20II
  EPSON TM-P80
  EPSON TM-U330 series
  EPSON TM-T83II
  EPSON TM-P20
  EPSON TM-m10
 - Supported Interfaces
     Wired LAN
     Wireless LAN
     Bluetooth
     USB


### Supplied Files

- ePOS-Print.jar
  Compiled Java class file, archived into a jar format file to allow APIs 
  to be used from Java programs.
- ePOSEasySelect.jar
  A Java class file for selecting a printer easily.
- libeposprint.so
  Library for function execution. (ARMv5TE supported)
- libeposeasyselect.so
  Library for the ePOSEasySelect function execution. (ARMv5TE supported)
- ePOS-Print_Sample_Android.zip
  A sample program file.
- EULA.en.txt
  Contains the SOFTWARE LICENSE AGREEMENT.
- EULA.jp.txt
  Contains the SOFTWARE LICENSE AGREEMENT. (The Japanese-language edition)
- ePOS-Print_SDK_Android_en_revP.pdf
  A user's manual.
- ePOS-Print_SDK_Android_ja_revL.pdf
  A user's manual. (The Japanese-language edition)
- ePOS-Print_SDK_Android_AppDevGuide_E_RevB.pdf
  A developer's guide
- ePOS-Print_SDK_Android_AppDevGuide_J_RevB.pdf
  A developer's guide (The Japanese-language edition)
- README.en.txt
  This file.
- README.jp.txt
  The Japanese-language edition of this file.

### Remarks
- For detailed information, please see ePOS-Print SDK for Android User's Manual.
- In the case of USB interface, it is recommended that you obtain permission to access the
  USB device in the application in advance.
  Noted below, how to get the permission.
   1. Enter the following code into the AndroidManifest.xml file.
      <manifest ...>
          <application>
              <activity ...>
                  <intent-filter>
                      <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
                  </intent-filter>
                  <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
                    android:resource="@xml/device_filter" />
              </activity>
          </application>
      </manifest>
   2. Add the res/xml/device_filter.xml in resource file,
      enter the following code into the device_filter.xml file.
      <?xml version="1.0" encoding="utf-8"?>  
      <resources>  
          <usb-device vendor-id="1208" />
      </resources>
  Please select the OK button when you get the permission dialog is displayed.

  If you don't obtain permission to access the USB device in advance,
  there are the following notes when using the openPrinter method.
    - When you select the OK button in the Permissions dialog box, it takes a long time of about 10 seconds to open port.
    - When you select the Cancel button in the Permissions dialog box, it wait for a timeout of 30 seconds.

### Modification from the old version

Version 1.9.0a
    - Improved the parseNFC API.
      - parseNFC API supports TM-m10 Ethernet model.
    - Bug Fixed.
      - In ePOSReceiptPrintSample, when the TM-T88V is to print on the paper of the paper width 58mm,
        a new line enters the middle of a line.
      - In ePOSReceiptPrintSample, print size of the logo is changed by the resolution of the screen.

  Version 1.9.0
    - Added the beginTransaction API for beginning transaction.
    - Added the endTransaction API for ending transaction.
    - Added the support printers.
      - TM-m10 Ethernet model
      - TM-P80 Autocutter model

  Version 1.8.0
    - Added the support printers.
      - TM-m10 USB model
    - Added getStatus API.
    - Bug Fixed.
      - Under Bluetooth connection, the openPrinter method may fail if application calls it immediately
        after the closePrinter method.

  Version 1.7.0
    - Added the support Android version.
      - Android 5.0 to 5.1
    - Added the support languages.
      - Indian
    - Improved the openPrinter API.
      - The openPrinter method succeeds even if the small letter is specified for the MAC address.
        (Only Bluetooth connection)

  Version 1.6.0
    - Added the support printers.
      - TM-P20
    - Added an printer search API for getting the device name.
    - Added the openPrinter API for timeout settings.
    - Improved the openPrinter API.
      - Not only IP address also MAC address or the host name can be set to the deviceName.
        (Only TCP/IP connection)
    - Improved the log function.
      - Compression of the backup file.
      - Improved the output content.
    - Specification change.
      - openPrinter returns an error after specified time passed if the target
        printer is already opened. (Only TCP/IP connection)

  Version 1.5.0
    - Added the support printers.
      - TM-U330
      - TM-T83II
    - Added an API for compression image data processing.

  Version 1.4.2
    - Bug Fixed.
      - Status when calling sendData method was incorreect.

  Version 1.4.1
    - Bug Fixed.
      - The openPrinter and sendData method causes one second delay,
        when printer doesn't have a battery.

  Version 1.4.0
    - Added the support interface.
      - USB
    - Improved the status monitoring function.
      - Improved the communication efficiency of status acquisition.
    - Bug Fixed.
      - Added the support API of TM-P80 the addCut, addFeedPosition, addLayout API.

  Version 1.3.4a
    - Added the support Android version.
      - Android 4.4

  Version 1.3.4
    - Bug Fixed.
      - The closePriner method may fail during printer status monitoring.
      - When printer is offline, the openPrinter method may not return 
        the correct printer status under Bluetooth connection.

  Version 1.3.3
    - Improved the text printing speed.

  Version 1.3.2
    - Added the support printers.
      - TM-T20II
      - TM-P80

  Version 1.3.1
    - Added the support printers.
      - TM-T90II
    - Added the support Android version.
      - Android 4.2.2

  Version 1.3.0
    - Added the log output function.
    - Added the support printers.
      - TM-T70II
    - Added the support Android version.
      - Android 4.2.1

  Version 1.2.1
    - Bug Fixed.

  Version 1.2.0
    - Added a command generation API for controlling the label.
    - Added a command generation API to set the paper layout.
    - Added the support printers.
      - TM-T82II

  Version 1.1.0
    - Added the printer status monitoring function.
    - Added the halftone method for raster graphic printing.
    - Support multiple tone printing.
    - Added the support languages.
      - Simplified Chinese
      - Traditional Chinese
      - Korean
      - Thai
      - Vietnamese
    - Added the battery status monitoring function.
    - Improved the performance of graphic printing.
    - Added the support printers.
      - TM-T20
      - TM-T82
      - TM-T81II
      - TM-P60II
      - TM-P60 (Wireless LAN)

  Version 1.0.0
    - New release. 
