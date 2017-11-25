package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.os.Environment;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import static android.support.v7.widget.helper.ItemTouchHelper.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static android.widget.CompoundButton.OnCheckedChangeListener;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private String mCurrentPhotoPath;

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private static final int totalFaces = 0;

    private boolean boxChecked = false;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private CheckBox mFaceDetectorCheckbox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private Button gridButton;

    private Environment env;

    private ArrayList<File> crimeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ImageAdapter mAdapter;



    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);

        //Code for swipe feature
        //------------------------------------------------
        SwipeController swipeController = new SwipeController();
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        //-------------------------------------------------

        mAdapter = new ImageAdapter(crimeList);
        mAdapter.setImages(crimeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        Gson gson = new Gson();

        String json = mCrime.getJson();

        Type type = new TypeToken<ArrayList<File>>() {
        }.getType();

        if (gson.fromJson(json, type) != null) {
            crimeList.addAll((ArrayList) gson.fromJson(json, type));
        }


        //-----------------------------------------------------

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mFaceDetectorCheckbox = (CheckBox) v.findViewById(R.id.face_tracker);
        //mFaceDetectorCheckbox.setChecked(mCrime.isSolved());
        //final Intent secondActivity = new Intent("com.google.android.gms.samples.vision.face.facetracker.intent.action.Launch");
        final Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage("com.google.android.gms.samples.vision.face.facetracker");
        mFaceDetectorCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                /*if (isChecked) {
                    startActivity(secondActivity);
                    if (launchIntent != null) {
                        startActivity(launchIntent);//null pointer check in case package name was not found
                    }
                }*/
                boxChecked = isChecked;
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);


        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**if (boxChecked) {
                    //final Intent ftActivity = new Intent(getActivity(), FaceTrackerActivity.class);
                    startActivity(secondActivity);
                 totalFaces = totalFaces + GraphicFaceTrackerFactory.Tracker.size();
                }*/
                    try {
                        mPhotoFile = createImageFile();

                        //galleryAddPic(mPhotoFile);

                        Uri uri = FileProvider.getUriForFile(getActivity(),
                                "com.bignerdranch.android.criminalintent.fileprovider",
                                mPhotoFile);
                        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                        List<ResolveInfo> cameraActivities = getActivity()
                                .getPackageManager().queryIntentActivities(captureImage,
                                        PackageManager.MATCH_DEFAULT_ONLY);

                        for (ResolveInfo activity : cameraActivities) {
                            getActivity().grantUriPermission(activity.activityInfo.packageName,
                                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }

                        startActivityForResult(captureImage, REQUEST_PHOTO);
                    } catch (IOException ex) {
                        Log.d(CrimeFragment.class.getSimpleName(), ex.getMessage());
                    }
            }
        });

        gridButton = (Button)v.findViewById(R.id.grid_view_button);

        gridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        return v;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = UUID.randomUUID().toString();
        File storageDir = getContext().getFilesDir();
        File img = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
        );
        //mCurrentPhotoPath = img.getAbsolutePath();
        return img;
    }

    /**private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = UUID.randomUUID().toString();
        File storageDir = getContext().getFilesDir();
        File img = new File(storageDir.toString());
        //img.createNewFile();
        return img;
    }*/

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void saveCrime() {
        Gson gson = new Gson();
        String json = gson.toJson(crimeList);
        mCrime.setJson(json);

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onPause() {
        super.onPause();

        saveCrime();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
            crimeList.add(mPhotoFile);
            saveCrime();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
            mAdapter.setImages(crimeList);
        }
    }


    //Creating handler and adapter for crime images
    //--------------------------------------------

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {

        List<File> photos = new ArrayList<>();
        private File mPhotoFile;
        private ImageView mCrimeImageView;

        public void setImages(ArrayList<File> cl) {
            crimeList = cl;
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
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_crime_recycler, parent, false);

            return new ImageHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            File file = photos.get(position);
            Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), getActivity());
            mCrimeImageView.setImageBitmap(bitmap);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }
    boolean canWrite = isExternalStorageWritable();




    //-------------------------------------------------
    //Function for the grid view activity

    public void showAllPhotos() {
//         Context context = getContext();
//        Intent intent = new Intent(context, GridViewActivity.class);
//        intent.putExtra(EXTRA_MESSAGE, mCrime.getId());
//        startActivity(intent);
    }

} //CrimeFragment class


//------------------------------------------------


//Function for swiping and deleting


class SwipeController extends Callback {

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}