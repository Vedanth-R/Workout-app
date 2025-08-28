package com.example.myapp;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.myapp.R;
import com.example.myapp.achievements.*;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class AchievementsFragment extends Fragment {

    private AchievementsViewModel vm;
    private InProgressAdapter inProgressAdapter;
    private UnlockedAdapter unlockedAdapter;
    private PRAdapter prAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        vm = new ViewModelProvider(this).get(AchievementsViewModel.class);

        RecyclerView rvInProgress = v.findViewById(R.id.rvInProgress);
        RecyclerView rvUnlocked   = v.findViewById(R.id.rvUnlocked);
        RecyclerView rvPRs        = v.findViewById(R.id.rvPRs);

        inProgressAdapter = new InProgressAdapter();
        unlockedAdapter = new UnlockedAdapter();
        prAdapter = new PRAdapter();

        // In Progress: vertical list
        rvInProgress.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInProgress.setAdapter(inProgressAdapter);

        // Unlocked: small grid (e.g., 3 columns)
        rvUnlocked.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvUnlocked.setAdapter(unlockedAdapter);

        // PRs: vertical list
        rvPRs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPRs.addItemDecoration(new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        rvPRs.setAdapter(prAdapter);

        // Observe
        vm.inProgressLimited.observe(getViewLifecycleOwner(), inProgressAdapter::submit);
        vm.unlockedCapped.observe(getViewLifecycleOwner(), unlockedAdapter::submit);
        vm.personalRecords.observe(getViewLifecycleOwner(), prAdapter::submit);
    }
}
