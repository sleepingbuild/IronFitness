package com.homefit.app.ui.discover;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.homefit.app.R;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.dto.DiscoverDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDiscoverData(view);
    }

    private void loadDiscoverData(View view) {
        ApiClient.get(requireContext()).getDiscoverItems().enqueue(new Callback<List<DiscoverDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<DiscoverDto>> call, @NonNull Response<List<DiscoverDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUi(view, response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DiscoverDto>> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi nạp dữ liệu Khám phá từ Server: " + t.getMessage());
            }
        });
    }

    private void updateUi(View view, List<DiscoverDto> items) {
        int tipCount = 0;
        int appCount = 0;

        for (DiscoverDto item : items) {
            if (!item.active) continue;

            String cat = item.category != null ? item.category : "";

            if (cat.contains("Giới thiệu App") || cat.contains("Banner")) {
                TextView tvTitle = view.findViewById(R.id.tv_app_title);
                TextView tvDesc = view.findViewById(R.id.tv_app_desc);
                if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
            } else if (cat.contains("Mẹo") || cat.contains("Kiến Thức")) {
                tipCount++;
                if (tipCount == 1) {
                    TextView tvTitle = view.findViewById(R.id.tv_tip1_title);
                    TextView tvDesc = view.findViewById(R.id.tv_tip1_desc);
                    if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                    if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
                } else if (tipCount == 2) {
                    TextView tvTitle = view.findViewById(R.id.tv_tip2_title);
                    TextView tvDesc = view.findViewById(R.id.tv_tip2_desc);
                    if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                    if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
                } else if (tipCount == 3) {
                    TextView tvTitle = view.findViewById(R.id.tv_tip3_title);
                    TextView tvDesc = view.findViewById(R.id.tv_tip3_desc);
                    if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                    if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
                }
            } else if (cat.contains("Hệ Sinh Thái") || cat.contains("Dự Án")) {
                appCount++;
                if (appCount == 1) {
                    TextView tvTitle = view.findViewById(R.id.tv_app1_title);
                    TextView tvDesc = view.findViewById(R.id.tv_app1_desc);
                    if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                    if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
                } else if (appCount == 2) {
                    TextView tvTitle = view.findViewById(R.id.tv_app2_title);
                    TextView tvDesc = view.findViewById(R.id.tv_app2_desc);
                    if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                    if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
                } else if (appCount == 3) {
                    TextView tvTitle = view.findViewById(R.id.tv_app3_title);
                    TextView tvDesc = view.findViewById(R.id.tv_app3_desc);
                    if (tvTitle != null && item.titleVi != null) tvTitle.setText(item.titleVi);
                    if (tvDesc != null && item.description != null) tvDesc.setText(item.description);
                }
            }
        }
    }
}
