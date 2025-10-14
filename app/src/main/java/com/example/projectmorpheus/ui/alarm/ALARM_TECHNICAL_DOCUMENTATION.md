# alarm system technical documentation

## architecture overview

the alarm system follows mvvm architecture with room database persistence. the implementation uses android's alarmmanager api for scheduling exact time triggers and broadcasts alarm events through a broadcastreceiver. all alarm state is stored in a local sqlite database managed by room.

## data layer

### alarm entity

the alarm data class is a room entity that defines the schema for alarm persistence. each alarm contains an auto generated primary key, hour and minute fields for time specification, an enabled flag, a text label, vibration preference, and an optional ringtone uri string. the entity lives in the alarms table.

### alarm dao

the data access object provides three operations: insert returns the generated alarm id as a long, delete removes an alarm by instance, and update modifies an existing alarm. the getalarms method returns a flow that emits the complete alarm list whenever the database changes, enabling reactive ui updates.

### alarm database

a room database singleton provides thread safe access to the alarm dao. the database uses the fallback to destructive migration strategy and implements the singleton pattern through a companion object with double checked locking.

## scheduling layer

### alarm scheduler interface

the scheduler defines three operations: schedule accepts an alarm and registers it with android's alarm manager, cancel removes a scheduled alarm by id, and canscheduleexactalarms checks for the required android 12+ permission.

### alarm scheduler implementation

the implementation wraps android's alarmmanager. the canscheduleexactalarms method returns true on pre android 12 devices and checks the actual permission status on android 12+. if permission is denied, the schedule method throws a securityexception with a descriptive message.

alarm scheduling uses setexactandallowwhileidle to ensure alarms fire even during doze mode. the method accepts an rtc wakeup alarm type, calculated trigger time in milliseconds, and a pendingintent targeting the alarm receiver. the pending intent includes the alarm id, label, vibration preference, and ringtone uri as extras.

the trigger time calculation lives in an extension function on the alarm class. it creates a calendar set to the alarm's hour and minute with seconds and milliseconds zeroed. if the calculated time is in the past, it adds one day to schedule for tomorrow.

### alarm receiver

the broadcastreceiver handles alarm trigger events. on receive, it extracts alarm metadata from the intent extras, creates a notification channel on android 8+, triggers vibration if enabled, and displays a full screen notification.

the notification channel uses importance high with alarm sound and vibration enabled. the sound comes from ringtonemanager's default alarm uri with audio attributes configured for alarm usage.

vibration uses a repeating pattern: zero millisecond delay, one second vibration, one second pause. on android 8+, this becomes a vibrationeffect waveform. pre android 8 uses the deprecated vibrate method.

the full screen notification includes a pendingintent that launches the alarm dismiss activity. the notification uses category alarm with max priority and the fullscreenintent flag set to true, allowing it to appear over the lock screen.

## presentation layer

### alarm view model

the viewmodel exposes a livedata stream of all alarms by converting the dao's flow using asLivedata. it also exposes error messages through a mutablelivedata that observers can watch for permission failures.

the createalarm method uses viewmodelscope to launch a coroutine that inserts the alarm into the database, schedules it with the scheduler, and logs success. if scheduling throws a securityexception, the viewmodel captures the error message, exposes it via livedata, and deletes the alarm from the database to maintain consistency.

the togglealarm method creates a copy of the alarm with inverted enabled status, updates the database, and either schedules or cancels based on the new state. the deletealarm method removes from database and cancels any scheduled trigger.

### alarm fragment

the fragment initializes the database, creates the scheduler implementation, and constructs the viewmodel using a factory. it observes the alarm list to update the recyclerview adapter and show or hide the empty state view.

the fragment also observes the error message livedata. when a permission error occurs, it displays an alertdialog explaining the issue with a button that opens android settings to the schedule exact alarm permission screen. the intent includes the app's package name as data to navigate directly to the correct settings page.

alarm creation flows through two dialogs. the materialtimepicker lets users select an hour and minute. when confirmed, a second dialog presents a textinput for the label and a switch for vibration preference. saving creates an alarm instance and calls the viewmodel's createalarm method.

### alarm list adapter

the adapter manages a list of alarm views backed by an internal mutable list. it provides callbacks for toggle and delete operations that flow back to the viewmodel. the updatealarms method replaces the entire list and calls notifydatasetchanged to refresh the ui.

## alarm dismiss flow

### alarm dismiss activity

this activity launches when an alarm fires and appears over the lock screen. it uses windowmanager flags to show when locked, turn the screen on, and keep it on. the oncreate method extracts alarm data from the intent, displays the current time and alarm label, plays the alarm sound, and starts vibration.

the back button is disabled by overriding onbackpressed without calling super, forcing the user to press the dismiss button. this ensures the user is conscious when dismissing the alarm.

the dismiss method stops sound and vibration, cancels the notification, and navigates to mainactivity with flags to clear the task stack. a todo comment notes future plans to navigate directly to a journal entry screen.

## permission handling

the manifest declares schedule exact alarm, post notifications, wake lock, vibrate, and use full screen intent permissions. on android 12+, schedule exact alarm is a special permission that requires user approval in system settings.

when scheduling fails due to missing permission, the error propagates from scheduler to viewmodel to fragment. the fragment displays a dialog with a button that opens the settings screen via action request schedule exact alarm intent. the user must manually toggle the alarms and reminders setting.

## database migration strategy

the current implementation uses fallback to destructive migration, which deletes all data when the schema changes. this is acceptable for early development but should be replaced with explicit migrations before production release.

## android api compatibility

the implementation handles api differences across android versions. vibration uses vibratormanager on android 12+ and the deprecated vibrator service on older versions. notification channels are created on android 8+. the show when locked and turn screen on logic uses new methods on android 8.1+ and deprecated window flags on older versions.

alarm scheduling always uses setexactandallowwhileidle regardless of android version. this method is available since api 23 and the app's minimum sdk is 24.

## logging strategy

strategic log statements trace alarm lifecycle at key decision points. alarmfragment logs when the user creates an alarm with the selected time and label. alarmviewmodel logs successful creation with the assigned id. alarmscheduler logs permission checks, calculated trigger times, and scheduling success. alarmreceiver logs when an alarm triggers with id and label, confirming the broadcast was received.

these logs enable debugging of scheduling failures, permission issues, and timing problems without requiring a debugger attached to the device.

## thread safety

database operations run on room's background executor. the viewmodel uses coroutines with viewmodelscope, which automatically cancels when the viewmodel clears. the alarmmanager calls are thread safe and can be invoked from any thread. the fragment observes livedata on the main thread, ensuring all ui updates happen on the correct thread.

## doze mode compatibility

android's doze mode restricts background activity to save battery. the implementation uses setexactandallowwhileidle specifically to bypass doze restrictions. this method is one of the few ways to guarantee alarm delivery during doze, though the system may still batch alarms within a small window.

the receiver's onreceive method has approximately ten seconds to complete before the system may kill it. the current implementation completes quickly by delegating to the alarm dismiss activity rather than performing long running work in the receiver.

## future considerations

the current implementation schedules one time alarms only. adding repeating alarms would require storing repeat patterns in the database and rescheduling the alarm after it fires. the alarm calculation would need to compute the next occurrence based on repeat rules.

notification permission should be requested at runtime on android 13+ rather than relying only on the manifest declaration. without explicit permission request, notifications may be silently blocked.

battery optimization exclusion should be requested for devices with aggressive power management. this is especially important for oneplus, oppo, and realme devices that kill alarms even with proper permissions.

the database migration strategy should be replaced with explicit migrations that preserve user data when the schema evolves.

the alarm dismiss activity includes a placeholder for future puzzle integration to make dismissal more difficult and ensure wakefulness.
