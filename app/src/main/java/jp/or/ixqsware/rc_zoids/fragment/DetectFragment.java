package jp.or.ixqsware.rc_zoids.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.or.ixqsware.rc_zoids.R;

import static jp.or.ixqsware.rc_zoids.Constants.ARG_SECTION_NUMBER;

/**
 * BLEデバイスリスト表示フラグメント
 *
 * Created by hisanaka on 15/09/14.
 */
public class DetectFragment extends BaseFragment {
    private ListView listView;

    public static DetectFragment newInstance(int sectionNumber) {
        DetectFragment instance = new DetectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        instance.setArguments(args);
        return instance;
    }

    public DetectFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detect, container, false);

        listView = (ListView) rootView.findViewById(R.id.device_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
                if (mListener != null) {
                    mListener.connectToDevice(device);
                }
            }
        });
        scanDevice();

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detect, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_list:
                scanDevice();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanDevice() {
        if (mListener != null) {
            mListener.scanDevices();
        }
    }

    public void updateLists(ArrayList<BluetoothDevice> arrDevices) {
        DeviceAdapter adapter = new DeviceAdapter(
                getContext(),
                R.layout.device_list_item,
                arrDevices
        );
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    private class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        private LayoutInflater layoutInflater_;

        public DeviceAdapter(Context context, int resource, List<BluetoothDevice> devices) {
            super(context, resource, devices);
            layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater_.inflate(R.layout.device_list_item, null);
            }

            TextView deviceNameView = (TextView) convertView.findViewById(R.id.device_name);
            TextView addressView = (TextView) convertView.findViewById(R.id.address_view);

            BluetoothDevice item = getItem(position);

            deviceNameView.setText(item.getName());
            addressView.setText(item.getAddress());

            return convertView;
        }
    }
}
