package com.example.myapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class TimerFragment extends Fragment {

    private TextView tvTimer;
    private MaterialButton btnStart, btnStop, btnReset;
    private boolean isRunning = false;
    private int seconds = 0;
    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        tvTimer = view.findViewById(R.id.tvTimer);
        btnStart = view.findViewById(R.id.btnStart);
        btnStop = view.findViewById(R.id.btnStop);
        btnReset = view.findViewById(R.id.btnReset);

        updateTimerDisplay();

        btnStart.setOnClickListener(v -> startTimer());
        btnStop.setOnClickListener(v -> stopTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        return view;
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        seconds++;
                        updateTimerDisplay();
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(runnable);
        }
    }

    private void stopTimer() {
        isRunning = false;
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void resetTimer() {
        stopTimer();
        seconds = 0;
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        String time = String.format("%02d:%02d:%02d", hours, minutes, secs);
        tvTimer.setText(time);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}
