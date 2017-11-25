/**package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GridViewActivity extends AppCompatActivity {

    public class ImageAdapter extends RecyclerView.Adapter<CrimeFragment.ImageAdapter.ImageHolder> {

        List<File> photos = new ArrayList<>();
        List<Crime> crimeList = new ArrayList<>();

        private File mPhotoFile;
        private ImageView mCrimeImageView;

        public void setImages(ArrayList<File> cl) {
            //crimeList = cl;
        }

        public class ImageHolder extends RecyclerView.ViewHolder {

            public ImageHolder(View view) {
                super(view);
                mCrimeImageView = (ImageView) view.findViewById(R.id.crime_image);
            }
        }

        public ImageAdapter(List<File> photos) {
            this.photos = photos;
        }


        @Override
        public GridViewActivity.ImageAdapter.ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_crime_recycler, parent, false);

            return new GridViewActivity.ImageAdapter.ImageHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CrimeFragment.ImageAdapter.ImageHolder holder, int position) {
            File file = photos.get(position);
            Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), getActivity());
            mCrimeImageView.setImageBitmap(bitmap);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }
    }

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View root = super.onCreateView(name, context, attrs);

        return root;
    }
}
 **/
