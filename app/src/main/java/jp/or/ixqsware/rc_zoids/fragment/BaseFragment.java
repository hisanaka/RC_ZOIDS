package jp.or.ixqsware.rc_zoids.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * 基本フラグメント
 *
 * Created by hnakadate on 2015/09/16.
 */
public class BaseFragment extends Fragment {
    protected FragmentCallbackListener mListener;

    public interface FragmentCallbackListener {
        void scanDevices();
        void connectToDevice(BluetoothDevice device);
        void disconnectDevice();
        void writeCharacteristic(int[] charValues);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentCallbackListener) {
            mListener = (FragmentCallbackListener) context;
        }
    }
}
