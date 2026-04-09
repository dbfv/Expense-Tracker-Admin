package com.example.expensetrackeradmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import models.Expense;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenseList, OnExpenseClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    public void updateData(List<Expense> newExpenses) {
        List<Expense> snapshot = new ArrayList<>(newExpenses);
        this.expenseList.clear();
        this.expenseList.addAll(snapshot);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvExpenseType.setText(expense.getType());
        holder.tvExpenseDate.setText(expense.getDate());

        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        holder.tvExpenseAmount.setText(formatter.format(expense.getAmount()));
        holder.tvExpenseCurrency.setText(expense.getCurrency());
        String claimantDisplay = expense.getClaimantDisplay();
        if (claimantDisplay == null || claimantDisplay.trim().isEmpty()) {
            claimantDisplay = expense.getClaimant();
        }
        if (claimantDisplay == null || claimantDisplay.trim().isEmpty()) {
            claimantDisplay = "-";
        }
        holder.tvExpenseClaimant.setText("Claimant: " + claimantDisplay);

        String status = expense.getStatus();
        holder.chipExpenseStatus.setText(status.toUpperCase());

        android.content.Context context = holder.itemView.getContext();
        if (status.equalsIgnoreCase("Paid")) {
            holder.chipExpenseStatus.setChipBackgroundColorResource(R.color.status_completed_bg);
            holder.chipExpenseStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.status_completed_text));
        } else if (status.equalsIgnoreCase("Pending")) {
            holder.chipExpenseStatus.setChipBackgroundColorResource(R.color.status_on_hold_bg);
            holder.chipExpenseStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.status_on_hold_text));
        } else if (status.equalsIgnoreCase("Reimbursed")) {
            holder.chipExpenseStatus.setChipBackgroundColorResource(R.color.status_active_bg);
            holder.chipExpenseStatus.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.status_active_text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseType, tvExpenseDate, tvExpenseAmount, tvExpenseCurrency, tvExpenseClaimant;
        Chip chipExpenseStatus;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseType = itemView.findViewById(R.id.tvExpenseType);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseCurrency = itemView.findViewById(R.id.tvExpenseCurrency);
            tvExpenseClaimant = itemView.findViewById(R.id.tvExpenseClaimant);
            chipExpenseStatus = itemView.findViewById(R.id.chipExpenseStatus);
        }
    }
}