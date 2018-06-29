package com.ibmcloud.example.cosgallerystarter.master;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ibmcloud.example.cosgallerystarter.R;
import com.ibmcloud.example.cosgallerystarter.detail.ImageDetailFragment;

import java.util.List;


public class ImageListRecyclerViewAdapter
        extends RecyclerView.Adapter<ImageListRecyclerViewAdapter.ImageThumbnailViewHolder> {

    private final ImageListActivity mParentActivity;
    private final List<S3ObjectSummary> mValues;
    private final boolean mTwoPane;

    ImageListRecyclerViewAdapter(ImageListActivity parent, List<S3ObjectSummary> items, boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    @Override @NonNull
    public ImageThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.master_list_entry, parent, false);
        return new ImageThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull ImageThumbnailViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                S3ObjectSummary item = mValues.get(holder.getAdapterPosition());

                Bundle arguments = new Bundle();
                ImageView imageDetail = view.findViewById(R.id.list_detail_image);
                if (!item.getETag().equals(ViewCompat.getTransitionName(imageDetail))) {
                    throw new AssertionError();
                }
                arguments.putString(ImageDetailFragment.ARG_ITEM_ID, item.getETag());
                ImageDetailFragment fragment = new ImageDetailFragment();
                fragment.setArguments(arguments);

                fragment.setEnterTransition(new AutoTransition());
                mParentActivity.get().setExitTransition(new AutoTransition());


                FragmentManager fragmentManager = mParentActivity.getSupportFragmentManager();

                fragmentManager.popBackStackImmediate();
                fragmentManager
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .addSharedElement(imageDetail, ViewCompat.getTransitionName(imageDetail))
                        .replace(R.id.image_container_detail, fragment)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ImageThumbnailViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final TextView mContentView;

        ImageThumbnailViewHolder(View view) {
            super(view);
            mIdView = view.findViewById(R.id.id_text);
            mContentView = view.findViewById(R.id.content);
        }
    }
}
