package jp.or.ixqsware.rc_zoids;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.UUID;

import jp.or.ixqsware.rc_zoids.fragment.BaseFragment;
import jp.or.ixqsware.rc_zoids.fragment.ControlFragment;
import jp.or.ixqsware.rc_zoids.fragment.DetectFragment;

import static jp.or.ixqsware.rc_zoids.Constants.ARG_SECTION_NUMBER;
import static jp.or.ixqsware.rc_zoids.Constants.REQUEST_ENABLE_BLUETOOTH;
import static jp.or.ixqsware.rc_zoids.Constants.SCAN_PERIOD;
import static jp.or.ixqsware.rc_zoids.Constants.TAG_DETECT;
import static jp.or.ixqsware.rc_zoids.Constants.TAG_OPERATION;
import static jp.or.ixqsware.rc_zoids.Constants.UUID_GPIO_OPERATION_SERVICE;
import static jp.or.ixqsware.rc_zoids.Constants.UUID_WRITE_ACCEL_CHARACTERISTIC;

public class MainActivity extends AppCompatActivity
            implements BaseFragment.FragmentCallbackListener {
    private FrameLayout frameLayout;
    private int currentFragment = 0;
    private ProgressDialog progressDialog = null;

    private ArrayList<BluetoothDevice> arrDevices = new ArrayList<>();
    private BluetoothGatt mBtGatt;
    private BluetoothLeScanner mBtLeScanner;

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (!arrDevices.contains(device)) { arrDevices.add(device); }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Snackbar.make(
                            frameLayout,
                            getString(R.string.disconnect_label),
                            Snackbar.LENGTH_SHORT
                    ).show();
                    showFragment(0);
                }
            } else {
                /* status 133対策(Documentに記載がなく、詳細不明/連続して接続すると発生) */
                Snackbar.make(frameLayout, R.string.congest_label, Snackbar.LENGTH_LONG)
                        .setAction(R.string.ok_label, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                // とりあえず何もしない
                                }
                        })
                        .setActionTextColor(getResources().getColor(R.color.snackbar_action))
                        .show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS){
                Snackbar.make(frameLayout, R.string.service_detection_fail, Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                boolean isZoids = false;
                for (BluetoothGattService service : gatt.getServices()) {
                    UUID uuid = service.getUuid();
                    if (uuid.equals(UUID.fromString(UUID_GPIO_OPERATION_SERVICE))) {
                        isZoids = true;
                        break;
                    }
                }
                if (isZoids) {
                    mBtGatt = gatt;
                    showFragment(1);
                } else {
                    Snackbar.make(
                            frameLayout,
                            getString(R.string.wrong_device),
                            Snackbar.LENGTH_SHORT
                    ).show();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout) findViewById(R.id.container);

        BluetoothManager mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBtManager != null) {
            BluetoothAdapter mBtAdapter = mBtManager.getAdapter();
            mBtLeScanner = mBtAdapter.getBluetoothLeScanner();
            if (mBtAdapter.isEnabled()) {
                showFragment(currentFragment);
            } else {
                Intent intentBtOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBtOn, REQUEST_ENABLE_BLUETOOTH);
            }
        } else {
            Snackbar.make(frameLayout, R.string.unsupported_bluetooth, Snackbar.LENGTH_LONG)
                    .setAction(R.string.ok_label, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBtGatt != null) {
            mBtGatt.close();
            mBtGatt = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent date) {
        super.onActivityResult(requestCode, resultCode, date);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                BluetoothManager mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter mBtAdapter = mBtManager.getAdapter();
                mBtLeScanner = mBtAdapter.getBluetoothLeScanner();
                showFragment(currentFragment);
            } else {
                finish();
            }
        }
    }

    private void showFragment(int sectionNumber) {
        switch (sectionNumber) {
            case 0:
                showDetectFragment();
                break;

            case 1:
                showControlFragment();
                break;
        }
    }

    @Override
    public void scanDevices() {
        if (mBtLeScanner == null) { return; }

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.searching_label));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Handler stopHandler = new Handler();
        stopHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBtLeScanner.stopScan(leScanCallback);
                FragmentManager fragmentManager = getSupportFragmentManager();
                DetectFragment fragment
                        = (DetectFragment) fragmentManager.findFragmentByTag(TAG_DETECT);
                if (fragment != null && fragment.isVisible()) {
                    fragment.updateLists(arrDevices);
                }
                if (progressDialog != null) { progressDialog.dismiss(); }
                progressDialog = null;
            }
        }, SCAN_PERIOD);

        mBtLeScanner.stopScan(leScanCallback);
        mBtLeScanner.startScan(leScanCallback);
    }

    @Override
    public void connectToDevice(BluetoothDevice device) {
        mBtGatt = device.connectGatt(this, false, mGattCallback);
    }

    @Override
    public void disconnectDevice() {
        //mBtGatt.disconnect();
    }

    @Override
    public void writeCharacteristic(int[] charValues) {
        BluetoothGattService mBtGattService
                = mBtGatt.getService(UUID.fromString(UUID_GPIO_OPERATION_SERVICE));
        if (mBtGattService == null) {
            Snackbar.make(
                    frameLayout,
                    getString(R.string.not_found, getString(R.string.service)),
                    Snackbar.LENGTH_SHORT
            ).show();
            return;
        }

        UUID uuid = UUID.fromString(UUID_WRITE_ACCEL_CHARACTERISTIC);
        BluetoothGattCharacteristic mBtChar = mBtGattService.getCharacteristic(uuid);
        if (mBtChar == null) {
            Snackbar.make(
                    frameLayout,
                    getString(R.string.not_found, getString(R.string.characteristic)),
                    Snackbar.LENGTH_SHORT
            ).show();
            return;
        }
        byte[] bytes = new byte[2];
        bytes[0] = (byte) charValues[0];
        bytes[1] = (byte) charValues[1];
        mBtChar.setValue(bytes);
        if (!mBtGatt.writeCharacteristic(mBtChar)) {
            // 書き込みに失敗した場合、停止だけ1.5秒間リトライし続ける
            if (charValues[1] == 0) {
                int n = 0;
                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    n++;
                    if (n > 15) { break; }
                } while (!mBtGatt.writeCharacteristic(mBtChar));
            }
            /*
            Snackbar.make(
                    frameLayout,
                    getString(R.string.write_char_fail, charValues[0] + ", " + charValues[1]),
                    Snackbar.LENGTH_SHORT
            ).show();
             */
        }
    }

    private void showDetectFragment() {
        currentFragment = 0;
        DetectFragment fragment = DetectFragment.newInstance(currentFragment);

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, currentFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment, TAG_DETECT);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    private void showControlFragment() {
        currentFragment = 1;
        ControlFragment fragment = ControlFragment.newInstance(currentFragment);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, currentFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment, TAG_OPERATION);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}