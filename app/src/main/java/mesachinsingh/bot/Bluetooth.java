package mesachinsingh.bot;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class Bluetooth extends Activity  {

    Button b1,b2,b3,b4;
    TextView text;

    private BluetoothAdapter mybluetooth;
    BluetoothDevice device;
    Set<BluetoothDevice>pairedDevices;


    ListView lv;
    ArrayList list1 = new ArrayList();
    ArrayList list2 = new ArrayList();


    @Override
    public void onCreate(Bundle savedInstanceState)  {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        b1 = (Button)findViewById(R.id.button1);
        b2 = (Button)findViewById(R.id.button2);
        b3 = (Button)findViewById(R.id.button3);
        b4 = (Button)findViewById(R.id.button4);
        text = (TextView)findViewById(R.id.text1);
        mybluetooth = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);

    }

    public void search(View v) {
        ArrayAdapter empty = new ArrayAdapter(this,R.layout.list_custom_color);
        lv.setAdapter(empty);
        if(!mybluetooth.isEnabled())
        {
            Toast.makeText(this,"Please Turn On Bluetooth",Toast.LENGTH_SHORT).show();
        }
        else {
            mybluetooth.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            text.setText("Nearby Devices");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context,Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                list2.add(device.getName()+" :    "+device.getAddress());
                ArrayAdapter adapter2 = new ArrayAdapter(Bluetooth.this,R.layout.list_custom_color,list2);
                lv.setAdapter(adapter2);
                lv.setOnItemClickListener(new OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> p1, View p2, int pos, long p4)
                    {
                        String item = (String)lv.getItemAtPosition(pos);
                        String MAC = item.substring(item.length() - 17);
                        Intent callMain = new Intent(getApplicationContext(), MainActivity.class);
                        callMain.putExtra("ADD", MAC);
                        startActivity(callMain);

                    }});
            }
        }
    };

    public void on(View v){
        if(!mybluetooth.isEnabled())  {
            Intent BTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTon , 0);
            Toast.makeText(getApplicationContext(),"Turning On Bluetooth",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Already ON",Toast.LENGTH_SHORT).show();
        }

    }

    public void off(View v) {
        ArrayAdapter empty =new ArrayAdapter(this,R.layout.list_custom_color);
        lv.setAdapter(empty);
        text.setText("");
        if(mybluetooth.isEnabled()) {
            mybluetooth.disable();
            Toast.makeText(getApplicationContext(),"Bluetooth Turned Off",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Already OFF",Toast.LENGTH_SHORT).show();
        }
    }

    public void visible(View v) {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible,0);
    }

    public void list(View v) {
        if(!mybluetooth.isEnabled()) {
            Toast.makeText(this,"Please Turn On Bluetooth",Toast.LENGTH_SHORT).show();
        }
        else {
            pairedDevices = mybluetooth.getBondedDevices();
            text.setText("Paired Devices");
            list1.clear();
            if (pairedDevices == null || pairedDevices.size() == 0) {
                Toast.makeText(this,"No Paired Device Found",Toast.LENGTH_SHORT).show();
                text.setText("No Paired Device Found");
            }
            else {
                for(BluetoothDevice bt : pairedDevices)
                    list1.add(bt.getName() +" :    " +bt.getAddress());
                Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();
                final ArrayAdapter adapter1 = new ArrayAdapter(this,R.layout.list_custom_color,list1);
                lv.setAdapter(adapter1);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Bluetooth.this);

        // set title
        alertDialogBuilder.setTitle("Exit");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        Bluetooth.this.finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    protected void onDestroy()
    {
        if(mybluetooth.isDiscovering()){
            mybluetooth.cancelDiscovery();
        }
        mybluetooth.disable();
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {

        super.onPause();
    }

    @Override
    protected void onResume()
    {

        super.onResume();
    }


}

































