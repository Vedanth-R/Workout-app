package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Train (Timer) screen.
 * Features: Start/Pause/Resume/End, Complete Set, Rest Timer (+15/Skip), One-time daily Log Workout, End Summary.
 */
public class TimerFragment extends Fragment {

    // --- UI refs ---
    private TextView tvWorkoutTimer, tvSetsCount;
    private MaterialButton btnStart, btnPauseResume, btnEnd,
            btnCompleteSet, btnStartRest, btnLogWorkoutOneTime,
            btnRestAdd15, btnRestSkip, btnSummaryDone;
    private TextView tvRestTimer, tvSummaryTime, tvSummarySets;
    private View restPanel, summaryPanel;

    // --- State ---
    private boolean isRunning = false;
    private boolean isPaused = false;
    private long startEpochMs = 0L;        // when current run started
    private long pausedAccumMs = 0L;       // total time that timer has been paused (deducted)
    private long pausedAtMs = 0L;          // when we paused
    private int setsCompleted = 0;

    // Workout ticker
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (isRunning && !isPaused) {
                updateWorkoutTimer();
                handler.postDelayed(this, 1000L);
            }
        }
    };

    // Rest timer
    private CountDownTimer restCountdown;
    private long restRemainingMs = 0L;
    private static final long DEFAULT_REST_MS = 30_000L; // 30s default—tune as you like

    // One-time log key
    private static final String PREFS = "train_prefs";
    private static final String KEY_LOG_PREFIX = "logged_"; // logged_yyyyMMdd

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_timer, container, false);
        bindViews(root);
        configureOneTimeLogButton();
        wireEvents();
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Avoid leaking callbacks; timer will resume cleanly on demand
        handler.removeCallbacks(ticker);
        if (isRunning && !isPaused) {
            // Soft-pause while off-screen
            onPauseClicked();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(ticker);
        cancelRest();
    }

    // ------------------ UI binding ------------------

    private void bindViews(View root) {
        tvWorkoutTimer = root.findViewById(R.id.tvWorkoutTimer);
        tvSetsCount = root.findViewById(R.id.tvSetsCount);

        btnStart = root.findViewById(R.id.btnStart);
        btnPauseResume = root.findViewById(R.id.btnPauseResume);
        btnEnd = root.findViewById(R.id.btnEnd);

        btnCompleteSet = root.findViewById(R.id.btnCompleteSet);
        btnStartRest = root.findViewById(R.id.btnStartRest);

        restPanel = root.findViewById(R.id.restPanel);
        tvRestTimer = root.findViewById(R.id.tvRestTimer);
        btnRestAdd15 = root.findViewById(R.id.btnRestAdd15);
        btnRestSkip = root.findViewById(R.id.btnRestSkip);

        summaryPanel = root.findViewById(R.id.summaryPanel);
        tvSummaryTime = root.findViewById(R.id.tvSummaryTime);
        tvSummarySets = root.findViewById(R.id.tvSummarySets);
        btnSummaryDone = root.findViewById(R.id.btnSummaryDone);

        btnLogWorkoutOneTime = root.findViewById(R.id.btnLogWorkoutOneTime);
    }

    // ------------------ Event wiring ------------------

    private void wireEvents() {
        btnStart.setOnClickListener(v -> onStartClicked());

        btnPauseResume.setOnClickListener(v -> {
            if (isPaused) onResumeClicked(); else onPauseClicked();
        });

        btnEnd.setOnClickListener(v -> onEndClicked());

        btnCompleteSet.setOnClickListener(v -> {
            setsCompleted++;
            tvSetsCount.setText(String.valueOf(setsCompleted));
            // Auto-start rest after a set
            startRest(DEFAULT_REST_MS);
        });

        btnStartRest.setOnClickListener(v -> startRest(DEFAULT_REST_MS));
        btnRestAdd15.setOnClickListener(v -> addRest(15_000L));
        btnRestSkip.setOnClickListener(v -> cancelRest());

        btnSummaryDone.setOnClickListener(v -> {
            // TODO: Persist workout session details to your data layer if desired
            // For now, just hide summary
            summaryPanel.setVisibility(View.GONE);
            resetUI();
        });

        btnLogWorkoutOneTime.setOnClickListener(v -> {
            markLoggedToday();
            btnLogWorkoutOneTime.setEnabled(false);
            Toast.makeText(requireContext(), "Workout logged for today ✅", Toast.LENGTH_SHORT).show();
        });
    }

    // ------------------ Timer controls ------------------

    private void onStartClicked() {
        if (isRunning) return;
        isRunning = true;
        isPaused = false;
        startEpochMs = System.currentTimeMillis();
        pausedAccumMs = 0L;
        handler.post(ticker);

        btnStart.setEnabled(false);
        btnPauseResume.setEnabled(true);
        btnEnd.setEnabled(true);
        btnCompleteSet.setEnabled(true);
        btnStartRest.setEnabled(true);
    }

    private void onPauseClicked() {
        if (!isRunning || isPaused) return;
        isPaused = true;
        pausedAtMs = System.currentTimeMillis();
        handler.removeCallbacks(ticker);
        btnPauseResume.setText("Resume");
    }

    private void onResumeClicked() {
        if (!isRunning || !isPaused) return;
        isPaused = false;
        long pausedFor = System.currentTimeMillis() - pausedAtMs;
        pausedAccumMs += pausedFor;
        handler.post(ticker);
        btnPauseResume.setText("Pause");
    }

    private void onEndClicked() {
        if (!isRunning) return;
        long totalMs = getElapsedMs();
        isRunning = false;
        isPaused = false;
        handler.removeCallbacks(ticker);
        cancelRest();


        tvSummaryTime.setText("Time: " + formatHhMmSs(totalMs));
        tvSummarySets.setText("Sets: " + setsCompleted);
        summaryPanel.setVisibility(View.VISIBLE);

        // Lock controls until summary flow is done
        btnStart.setEnabled(false);
        btnPauseResume.setEnabled(false);
        btnEnd.setEnabled(false);
        btnCompleteSet.setEnabled(false);
        btnStartRest.setEnabled(false);
    }

    private void resetUI() {
        tvWorkoutTimer.setText("00:00:00");
        tvSetsCount.setText("0");
        setsCompleted = 0;
        isRunning = false;
        isPaused = false;
        startEpochMs = 0L;
        pausedAccumMs = 0L;
        pausedAtMs = 0L;

        cancelRest();
        restPanel.setVisibility(View.GONE);

        btnStart.setEnabled(true);
        btnPauseResume.setText("Pause");
        btnPauseResume.setEnabled(false);
        btnEnd.setEnabled(false);
        btnCompleteSet.setEnabled(false);
        btnStartRest.setEnabled(false);
    }

    private void updateWorkoutTimer() {
        tvWorkoutTimer.setText(formatHhMmSs(getElapsedMs()));
    }

    private long getElapsedMs() {
        if (!isRunning) return 0L;
        long now = System.currentTimeMillis();
        return (now - startEpochMs) - pausedAccumMs;
    }

    // ------------------ Rest timer ------------------

    private void startRest(long durationMs) {
        cancelRest();
        restRemainingMs = durationMs;
        restPanel.setVisibility(View.VISIBLE);
        runRestCountdown();
    }

    private void addRest(long addMs) {
        if (restCountdown == null) return;
        restRemainingMs += addMs;
        // Restart to apply new remaining time
        runRestCountdown();
    }

    private void cancelRest() {
        if (restCountdown != null) {
            restCountdown.cancel();
            restCountdown = null;
        }
        restRemainingMs = 0L;
        if (restPanel != null) restPanel.setVisibility(View.GONE);
    }

    private void runRestCountdown() {
        if (restCountdown != null) restCountdown.cancel();
        // snapshot of remaining to keep restart consistent
        final long startMs = Math.max(0L, restRemainingMs);
        tvRestTimer.setText(formatMmSs(startMs));

        restCountdown = new CountDownTimer(startMs, 1000L) {
            @Override public void onTick(long millisUntilFinished) {
                restRemainingMs = millisUntilFinished;
                tvRestTimer.setText(formatMmSs(millisUntilFinished));
            }
            @Override public void onFinish() {
                restRemainingMs = 0L;
                restPanel.setVisibility(View.GONE);
                // Optional: vibrate or toast to notify
                // TODO: Haptic/notification if desired
            }
        };
        restCountdown.start();
    }

    // ------------------ One-time daily log ------------------

    private void configureOneTimeLogButton() {
        boolean alreadyLogged = isLoggedToday();
        btnLogWorkoutOneTime.setEnabled(!alreadyLogged);
        if (alreadyLogged) {
            btnLogWorkoutOneTime.setText("Logged");
        }
    }

    private boolean isLoggedToday() {
        SharedPreferences sp = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_LOG_PREFIX + todayKey(), false);
    }

    private void markLoggedToday() {
        SharedPreferences sp = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_LOG_PREFIX + todayKey(), true).apply();
        btnLogWorkoutOneTime.setText("Logged");
    }

    private String todayKey() {
        // yyyyMMdd is enough granularity for "once per day"
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    // ------------------ Formatting helpers ------------------

    private String formatHhMmSs(long ms) {
        // Safer manual format to avoid localization surprises in a fitness timer
        long totalSec = ms / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }

    private String formatMmSs(long ms) {
        long totalSec = Math.max(0L, ms / 1000L);
        long m = totalSec / 60L;
        long s = totalSec % 60L;
        return String.format(Locale.US, "%02d:%02d", m, s);
    }
}
