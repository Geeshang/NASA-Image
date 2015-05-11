# NASA-Image
This is an Android project display the NASA  Image of the Day.

Everyday NASA will publish one image related to space exploration in their website.This android
app parse the RSS from NASA official website to show you Image of the Day.

##APP Features
- **Translation**
  - Provide many kinds language translation from English using BAIDU public service.
- **Save Image**
  - Save the image to Sdcard , and synchronize to Gallery App.
- **Share Image**
  - Share image with your friend.
- **Check Image in Detail**
  - Manipulate the image in many ways.Such as Drag, Zoom, Rotate.

##Wrapper Class May Help You
- **Downloader.java**
  - This class encapsulate some network operations  use "Get" method through Http protocol.
- **ImageTransformHandler.java**
  - This class encapsulate some image operations use Matrix class, such as drag, zoom, rotate, skew.
- **ExternalStorageHandler.java**
  - This class encapsulate some external storage operations.
  
## Building

### With Gradle

The easiest way to build is to install [Android Studio](https://developer.android.com/sdk/index.html) v1.+
with [Gradle](https://www.gradle.org/) v2.2.1.
Once installed, then you can import the project into Android Studio:

1. Open `File`
2. Import Project
3. Select `build.gradle` under the project directory
4. Click `OK`

Then, Gradle will do everything for you.

### With Maven

The build requires [Maven](http://maven.apache.org/download.html)
v3.1.1+ and the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed in your development environment. In addition you'll need to set
the `ANDROID_HOME` environment variable to the location of your SDK:

```bash
export ANDROID_HOME=/opt/tools/android-sdk
```

After satisfying those requirements, the build is pretty simple:

* Run `mvn clean package` from the `app` directory to build the APK only
* Run `mvn clean install` from the root directory to build the app and also run
  the integration tests, this requires a connected Android device or running
  emulator
