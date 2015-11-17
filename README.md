# Custom Camera Gallery Library
Since almost developers need get the file path of seleected file to serve for something like downloading, uploading, showing ... Therefore, it is useful library for developer to attach the library into application to do it more easily via friendly UI.

![Custom Camera]({{site.baseurl}}/http://www.4shared.com/download/5DcwZoMuce/Screenshot_2015-11-17-15-36-23.jpg?sbsr=344f12e97ee659a8bcea405f252079b2959&lgfp=3000)
![Custom Gallery]({{site.baseurl}}/http://www.4shared.com/download/bJsQ0s7uce/Screenshot_2015-11-17-15-35-47.jpg?sbsr=d00e049cd0beb270922e1f2fb7ff5d69959&lgfp=3000)

# Feature
1 - Support both Camera & Gallery feature.

2 - Support both Photo (*.jpg) and Video type (*.mp4).

3 - Support take picture and record video while rotating device (0, 90, 180, 270).

4 - Support front camera, back camera, or device only has one camera (front or back). 

5 - Support Square mode and Full Screen mode when using with Camera feature.  

6 - Support for multi-screen devices.  

7 - Good UI design, very friendly.  

8 - Easily get File Path after get single file by using Camera or multiple files by using Gallery.

Android 2.3.3 and above supported

#Download
[Library Jar](https://bintray.com/artifact/download/mirrortowers/maven/android/mirrortowers/custom_camera_gallery/1.0.3/custom_camera_gallery-1.0.3-sources.jar "Bintray")

[Sample Application](https://play.google.com/store/apps/details?id=mirrortowers.beautiful_bag.android.custom_camera.custom_gallery&hl=en "Google Play Store")

#Documentation
Sync the library was put at jCenter (Bintray) so need put in build.gradle file in Project this line first 

> allprojects {
    
    repositories {
    
        jcenter()
        
        maven {
        
            url  "http://dl.bintray.com/mirrortowers/maven"
            
        }
        
       }
       
     }

for Gradle can compile it

> dependencies {

    compile fileTree(include: ['*.jar'], dir: 'libs')
    
    compile 'com.android.support:appcompat-v7:23.0.1'
    
    compile 'com.android.support:support-v4:23.0.1'
    
    compile 'android.mirrortowers:custom_camera_gallery:1.0.3'
    }

So now, you can access all class in the library to begin use Custom Camera feature and Custom Gallery feature.

[Sample Project used this library](https://github.com/mirrortowers/custom_camera_gallery "Custom Camera Gallery sample")

#Change Log
- version 1.0.3 (17.11.2015) : Published at jCenter (bintray) for developers can get via maven method.
- version 1.0.1 & 1.0.2 : minor changes & fixed some crashes.
- versionn 1.0.0 : First release version

#Usage
Since almost developers want to get the file path of selected files so need following these steps :
Firstly, initial activities with following extras : Custom Camera and Custom Gallery for user select file for us to get file path of them :


> @Override

    public void onClick(View v) {
    
        switch (v.getId()) {
            case R.id.btn_about:
                // Show Dialog Activity
                startActivity(new Intent(this, DonateActivity.class));
                break;
            case R.id.btn_custom_camera:
                // Open Custom Camera activity
                Intent mIntentCamera = new Intent(this, CustomCamera.class);
                // IF USER WANT GET FILE PATH OF ONE SELECTED FILE, SHOULD PUT ACTION_CHOSE_SINGLE_FILE
                mIntentCamera.putExtra(
                        Receiver.EXTRAS_ACTION, Receiver.ACTION_CHOSE_SINGLE_FILE);

                startActivity(mIntentCamera);
                break;
            case R.id.btn_custom_gallery:
                // Open Custom Gallery activity
                Intent mIntent = new Intent(this, CustomGallery.class);
                // IF USER WANT GET FILE PATH OF MULTIPLE SELECTED FILE, SHOULD PUT ACTION_CHOSE_MULTIPLE_FILE
                mIntent.putExtra(
                        Receiver.EXTRAS_ACTION, Receiver.ACTION_CHOSE_MULTIPLE_FILE);
                startActivity(mIntent);
                break;
        }
    }
    
Then, define Broadcast Receiver to get file path of selected files :

public class BroadcastReceiverFileList extends BroadcastReceiver {

    > @Override
    
    public void onReceive(Context context, Intent intent) {
    
        /**
         * Receive file path in here
         */
        if (intent.getAction().equals(Receiver.ACTION_CHOSE_SINGLE_FILE)) {
            /**
             * Single file
             */
            String FILE_PATH = intent.getExtras().getString(Receiver.EXTRAS_FILE_PATH);

            Log.i("", "FILE_PATH " + FILE_PATH);
            
        } else if (intent.getAction().equals(Receiver.ACTION_CHOSE_MULTIPLE_FILE)) {
            /**
             * Multiple files
             */

            ArrayList<String mAlFilePath =
                    intent.getStringArrayListExtra(Receiver.EXTRAS_FILE_PATH);

            for (int i = 0; i < mAlFilePath.size(); i++) {
                Log.i("", "get i " + mAlFilePath.get(i));
            }
        }
    }
}

#Donation
p/s : Every improve also need every support also, please support me if you can.
Rate app, like it, subscribe it, or donate .v.v.

Really thank you for your help.

p/s : Can transfer to [Paypal Account](https://www.paypal.com "Huy Tower") : huytower1990@gmail.com

