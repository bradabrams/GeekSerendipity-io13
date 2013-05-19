solutions-mobile-backend-starter-android-client
=====================================

This application implements a client for Mobile Backend using
Google Cloud Endpoints, App Engine, Java, and Android.

Disclaimer: This is not an official Google Product.

## Products
- [App Engine][1]
- [Android][2]

## Language
- [Java][3]

## APIs
- [Google Cloud Endpoints][4]

## Setup Instructions
1. Make sure you have Android SDK with Google APIs level 14 or above installed.
2. Import the project into Eclipse.
3. In project properties, select Android on the left and change Project Build Target to Google APIs with API Level 14 or above.
3. Update the value of `PROJECT_ID` in
   `src/com/google/cloud/backend/android/Consts.java` to the app_id of your 
   deployed Mobile Backend [5]. Make sure that your Mobile Backend is configured
   with OPEN mode.
4. Run the application.

[1]: https://developers.google.com/appengine
[2]: http://developer.android.com/index.html
[3]: http://java.com/en/
[4]: https://developers.google.com/appengine/docs/java/endpoints/
[5]: https://github.com/GoogleCloudPlatform/solutions-mobile-backend-starter-java

