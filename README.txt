Dependencies
	Our GoogleMaps API Key and Parse API information are hard-coded into the app and noone will need to put anything in.  We have been compiling under SDK 21, but we have tested under SDK 16 and all appears to be working.  There are two modules, the main app and ParseLoginUI.  ParseLoginUI handles the login window so do not edit it or bad things will happen.
	We also have tested this on the emulator and real devices including a Nexus 7 tablet.  It does not require Wifi, but be careful about data usage.  It shouldn't do much, but be sure to close the app when you are done.  It will auto-refresh while active.
	If you want it to work on an emulator, you have to manually set the location in the emulator.  This can be done through the android device manager, or by telnetting into the running emulator with "telnet localhost <port number at top of window>"  Once open, type in "geo fix <longitude> <latitude>".

Instructions
	GeoPost is designed to be a simple to use application.  Upon first installing the app, a registration screen will appear.  You can either type in your username and password to log in, or select the "Sign up" button.  We ask for a username, password and email.  Each email is associated with exactly one username.  It is possible to reset your password with a link below the login/sign up buttons.
	Once you are signed in, a Google Map view is shown and centered on your location.  On the lower right is a green plus button to make a new post.  The post view allows you to put in a title (required) and a message body.  Once you are satisfied, you can click “Post” and it will bring you back to the Map screen with a marker placed at your location.  You can click on it to view the title and username.  We are working on a better view window.
	On the left is a navigation drawer than can be slid out with a drag, or clicking the “hamburger” button in the action bar.  It displays the currently logged in user, and has a logout button to switch users.  There are also tabs below the action bar to switch to a list view of posts and a profile (To be implemented in the Beta). 
	Posts can only be viewed within a particular radius from your current location.  This is relatively small (It’s about 1/8 of a mile).  One can view where posts are made with a much larger radius however.  This is currently ~100 miles.


