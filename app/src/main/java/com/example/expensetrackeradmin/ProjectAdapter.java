package com.example.expensetrackeradmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.text.DecimalFormat;
import java.util.List;

import models.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;

    public ProjectAdapter(List<Project> projectList) {
        this.projectList = projectList;
    }

    public void updateData(List<Project> newProjects) {
        this.projectList.clear();
        this.projectList.addAll(newProjects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);

        holder.tvProjectTitle.setText(project.getName());
        holder.tvManager.setText(project.getManager());
        holder.tvEndDate.setText("Ends " + project.getEndDate());
        holder.chipStatus.setText(project.getStatus().toUpperCase());

        holder.chipStatus.setText(project.getStatus().toUpperCase());

        android.content.Context context = holder.itemView.getContext();
        String currentStatus = project.getStatus();

        if (currentStatus.equalsIgnoreCase("Active")) {
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_active_bg);
            holder.chipStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.status_active_text));

        } else if (currentStatus.equalsIgnoreCase("Completed")) {
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_completed_bg);
            holder.chipStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.status_completed_text));

        } else if (currentStatus.equalsIgnoreCase("On Hold")) {
            holder.chipStatus.setChipBackgroundColorResource(R.color.status_on_hold_bg);
            holder.chipStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.status_on_hold_text));
        }

        DecimalFormat formatter = new DecimalFormat("$#,##0.00");
        holder.tvBudget.setText(formatter.format(project.getBudget()));

        double budget = project.getBudget();
        double spent = project.getSpentAmount();

        int progressPercentage = 0;
        if (budget > 0) {
            progressPercentage = (int) Math.round((spent / budget) * 100);
        }

        holder.progressBar.setProgress(Math.min(progressPercentage, 100));

        holder.tvProgressText.setText(progressPercentage + "% OF BUDGET UTILIZED");

        if (progressPercentage > 100) {
            holder.tvProgressText.setTextColor(android.graphics.Color.RED);
            holder.tvProgressText.setText("OVER BUDGET: " + progressPercentage + "% UTILIZED!");
        } else {
            holder.tvProgressText.setTextColor(android.graphics.Color.parseColor("#79747E")); // Màu outline
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, ProjectDetailsActivity.class);
            intent.putExtra("PROJECT_ID", project.getProjectId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectTitle, tvManager, tvEndDate, tvBudget, tvProgressText;
        Chip chipStatus;
        ProgressBar progressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvManager = itemView.findViewById(R.id.tvManager);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}