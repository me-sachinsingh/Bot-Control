package mesachinsingh.bot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "MainActivity";
    Bitmap bitmap;
    int x_center, y_center, points;
    byte[] buffer;
    String command = "fuzzy";

    private BluetoothAdapter myBluetooth;
    BluetoothSocket mSocket;
    OutputStream oStream;

    CameraBridgeViewBase javaCameraView;
    Mat mRgba, mRgbaR;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
            super.onManagerConnected(status);
        }
    };

    static {
        if(OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV Loaded successfully");
        }
        else {
            Log.i(TAG, "OpenCV not loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView)findViewById(R.id.javacameraview);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setMaxFrameSize(1280, 720);
        javaCameraView.setCvCameraViewListener(this);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(!myBluetooth.isEnabled()) {
            myBluetooth.enable();
        }

        Bundle extras = getIntent().getExtras();
        String address = extras.getString("ADD");

        connectBluetooth(address);
        Toast.makeText(getApplicationContext(), address, Toast.LENGTH_SHORT).show();

    }

    public void connectBluetooth(String MAC) {
        final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothDevice device = myBluetooth.getRemoteDevice(MAC);
        try {
            mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Remote device not in range", Toast.LENGTH_SHORT).show();
        }

        try {
            if(myBluetooth.isDiscovering()) {
                myBluetooth.cancelDiscovery();
            }
            mSocket.connect();
            oStream = mSocket.getOutputStream();
            //Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Output Stream not working!", Toast.LENGTH_SHORT).show();
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView!=null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView!=null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV Loaded succesfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else {
            Log.i(TAG, "OpenCV not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        //Imgproc.cvtColor(mRgba, mRgbaR, Imgproc.Color_Rg);

        try {
            bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);
        }
        catch (CvException e) {
            Log.i(TAG, "Bitmap not loaded");
        }

        int x = 0;
        int y = 0;

        int all_x = 0;
        int all_y = 0;

        while(x < 1280) {                                                                           //For higher resolution
            while(y < 720) {
                int pixel = bitmap.getPixel(x, y);

                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);

                if(redValue > 130 && blueValue < 50 && greenValue < 50) {
                    points++;
                    all_x =all_x + x;
                    all_y =all_y + y;
                }
                y += 6;
            }
            x += 6;
            y = 0;
        }

        x = 0;
        y = 0;

        if(points > 100) {
            x_center = all_x / points;
            y_center = all_y / points;


            //Toast.makeText(getApplicationContext(), String.valueOf(points), Toast.LENGTH_SHORT).show();
            Point center = new Point(x_center, y_center);
            Imgproc.circle(mRgba, center, 30, new Scalar(255, 0, 0), 2, 8, 0);

            //To send the corresponding direction to the Arduino controlled Bot
            Log.i(TAG, "x_center = " + x_center);
            Log.i(TAG, "y_center = " + y_center);

            if(points < 1000) {
                if (x_center > 620 && x_center < 660 /*&& y_center > 340 && y_center < 380*/) {         //To move forward
                    Send("f");
                }
                else if(x_center >= 660 ) {
                    Send("r");
                }
                else {
                    Send("l");
                }
            }
            else {
                Send("s");
            }

            points = 0;
        }
        return mRgba;
    }

    public void Send(String dir) {                                                                      //To send commands to bot
        buffer = dir.getBytes();
        try
        {
            oStream.write(buffer);
        } catch (IOException e) {
            Log.i(TAG, "Sending data to bot failed!");
        }
    }
}
