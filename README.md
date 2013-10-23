justdrive
=========

A really simple car home screen for Android 4.x devices.

The app runs a background service that attempts to detect the user's activity. If the user is in a vehicle,
it automatically turns Bluetooth on and enters Car Mode (as if the phone were docked).

The app home screen is set to display even when locked and provides a button to launch a voice search. The voice search can trigger any voice actions the device supports.

TODO:
 * better way of backing off in the event the user manually exits car mode (for false positives and when the user is not the driver)
