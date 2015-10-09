package jp.or.ixqsware.rc_zoids.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import jp.or.ixqsware.rc_zoids.R;
import jp.or.ixqsware.rc_zoids.view.CustomSeekBar;

import static jp.or.ixqsware.rc_zoids.Constants.ARG_SECTION_NUMBER;

/**
 * 操作用フラグメント
 *
 * Created by hisanake on 15/09/25.
 */
public class ControlFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {

    public static ControlFragment newInstance(int sectionNumber) {
        ControlFragment instance = new ControlFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        instance.setArguments(args);
        return instance;
    }

    public ControlFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentCallbackListener) {
            mListener = (FragmentCallbackListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_control, container, false);

        CustomSeekBar controlBar = (CustomSeekBar) rootView.findViewById(R.id.control_bar);
        controlBar.setMax(510);
        controlBar.setProgress(255);
        controlBar.setOnSeekBarChangeListener(this);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.disconnectDevice();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mListener != null) {
            int mDirection = progress - 255;
            if (mDirection < 0) {
                mListener.writeCharacteristic(new int[] {1, Math.abs(mDirection)});
            } else {
                mListener.writeCharacteristic(new int[] {0, mDirection});
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekBar.setProgress(255);
    }
}
