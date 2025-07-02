package com.example.crime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final Context context;
    private final List<Report> reports;
    private final List<String> usernames;
    private List<String> profileImages = new ArrayList<>();

    public ReportAdapter(Context context, List<Report> reports, List<String> usernames) {
        this.context = context;
        this.reports = reports;
        this.usernames = usernames;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        String username = usernames.get(position);

        holder.username.setText(username);
        holder.title.setText(report.crime + " in " + report.location);
        holder.description.setText(report.description);

        // Load image
        Glide.with(context).load(report.imageUrl).into(holder.imageView);

        // Load profile image if available
        if (profileImages != null && position < profileImages.size()) {
            Glide.with(context).load(profileImages.get(position)).circleCrop().into(holder.profileImage);
        }
    }


    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setProfileImages(List<String> profileImages) {
        this.profileImages = profileImages;
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView username, title, description;
        ImageView imageView, profileImage;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.text_username);
            title = itemView.findViewById(R.id.text_crime_title);
            description = itemView.findViewById(R.id.text_description);
            imageView = itemView.findViewById(R.id.image_report);
            profileImage = itemView.findViewById(R.id.image_profile); // NEW
        }
    }

}
