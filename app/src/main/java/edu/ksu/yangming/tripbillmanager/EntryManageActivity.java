package edu.ksu.yangming.tripbillmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EntryManageActivity extends Activity implements LocationListener {
    private EditText mEntryName;
    private EditText mSum;
    private TextView mDateDisplay;
    private TextView mTimeDisplay;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private GregorianCalendar calendar;
    // private ImageView image;
    private TextView mMapDisplay;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_SELECT = 3;
    private Location myLocation;
    private LocationManager mLocationManager;
    private String mPlaceName;
    private LinearLayout mImageGallery;
    private Data.Account account;
    private Data.Entry entry;
    private int entryPos;
    private Bundle bundle;
    private final String IMAGE_FOLDER = "BillImages/";
    private ArrayList<String> imageUrls = new ArrayList<String> ();
    protected void createDateTimeDialog() {
        mDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mDateDisplay.setText(dateFormatter.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        mTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                mTimeDisplay.setText(timeFormatter.format(calendar.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_manage);
        bundle = getIntent().getExtras();
        int accountPos = bundle.getInt("account");
        entryPos = bundle.getInt("entry");
        account = ((Data)getApplication()).accounts.get(accountPos);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        timeFormatter = new SimpleDateFormat("HH:mm:ss");


        if (entryPos == account.entries.size()) {
            calendar = new GregorianCalendar();
            getCurrentLocation();
            entry = new Data.Entry(accountPos, account, calendar, myLocation);
            account.addEntry(entry);
        }
        else {
            entry = account.entries.get(entryPos);
            calendar = entry.time;
            myLocation = entry.location;
            mPlaceName = entry.locStr;
            imageUrls.addAll(entry.imageUrls);
        }
        mEntryName = (EditText) findViewById(R.id.entry_name);
        mEntryName.setRawInputType(InputType.TYPE_CLASS_TEXT);
        mEntryName.setText(entry.entry_name);
        mSum = (EditText) findViewById(R.id.sum);
        mSum.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mSum.setText(Double.toString(entry.sum_bill));
        mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
        mTimeDisplay.setInputType(InputType.TYPE_NULL);
        mTimeDisplay.setText(timeFormatter.format(calendar.getTime()));
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mDateDisplay.setInputType(InputType.TYPE_NULL);
        mDateDisplay.setText(dateFormatter.format(calendar.getTime()));
        createDateTimeDialog();
        mTimeDisplay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mTimePickerDialog.show();
            }
        });
        mDateDisplay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });
        mMapDisplay = (TextView) findViewById(R.id.mapDisplay);

        mMapDisplay.setText(mPlaceName);
        mMapDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Context context = getApplicationContext();
                try {
                    startActivityForResult(builder.build(context), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                }

            }
        });
        Button takePhoto = (Button) this.findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                String filename = dateFormatter.format(calendar.getTime()) + "/" + timeFormatter.format(calendar.getTime())+ "_bill.JPG";
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(IMAGE_FOLDER + filename)));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
        Button selectPhoto = (Button) this.findViewById(R.id.selectPhoto);
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECT);
            }
        });
        mImageGallery = (LinearLayout)findViewById(R.id.images);
        for (String imageUrl : imageUrls) {
            InputStream is = null;
            try {
                is = (InputStream) new URL(imageUrl).getContent();
                if(is != null ) {
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    insertPhoto(bitmap);
                }
            } catch (IOException e) {
                imageUrls.remove(imageUrl);

            }

        }
        final Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entry.location = myLocation;
                entry.entry_name = mEntryName.getText().toString();
                entry.sum_bill = Double.parseDouble(mSum.getText().toString());
                entry.time = calendar;
                entry.imageUrls = imageUrls;
                entry.locStr = mPlaceName;
                account.updateEntry(entry);
                bundle.putInt("entry", entryPos);
                bundle.putBoolean("isSave", true);
                Intent resultIntent = new Intent();
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK, resultIntent);
                EntryManageActivity.this.finish();

            }
        });
        final Button deleteButton = (Button) findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                account.removeEntry(entry);
                bundle.putInt("entry", entryPos);
                bundle.putBoolean("isSave", false);
                Intent resultIntent = new Intent();
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK, resultIntent);
                EntryManageActivity.this.finish();

            }
        });
        // image = (ImageView) findViewById(R.id.image);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place myPlace = PlacePicker.getPlace(data, this);
                mPlaceName = String.valueOf(myPlace.getName()) + ", " + myPlace.getAddress();
                mMapDisplay.setText(mPlaceName);
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageUrls.add((String) data.getExtras().get(MediaStore.EXTRA_OUTPUT));
                insertPhoto(photo);
            }
        }
        if (requestCode == REQUEST_IMAGE_SELECT) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Bitmap photo = BitmapFactory.decodeFile(picturePath);
                imageUrls.add(picturePath);
                insertPhoto(photo);
            }
        }
    }

    public void insertPhoto(Bitmap photo) {
        // image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // image.setImageBitmap(photo);
        RelativeLayout layout = new RelativeLayout(getApplicationContext());
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(250, 250);
        layout.setLayoutParams(param);
        layout.setGravity(Gravity.CENTER);
        final ImageButton deleteButton = new ImageButton(getApplicationContext());
        LinearLayout.LayoutParams buttonLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLP.gravity = (Gravity.TOP | Gravity.RIGHT);
        deleteButton.setLayoutParams(buttonLP);
        deleteButton.setImageResource(R.drawable.ic_delete_white_18dp);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageGallery.removeView((View) v.getParent());
            }
        });
        deleteButton.setVisibility(View.INVISIBLE);
        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, 0, 10, 0);
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(photo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteButton.getVisibility() == View.INVISIBLE)
                    deleteButton.setVisibility(View.VISIBLE);
                else
                    deleteButton.setVisibility(View.INVISIBLE);

            }
        });
        layout.addView(imageView);
        layout.addView(deleteButton);
        mImageGallery.addView(layout);


        /*
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(220, 220));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(photo);
        mImages.addView(layout);
        */
    }
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void getCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        myLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (myLocation != null) {
            Geocoder geocoder = new Geocoder(this);
            try {
                Address address = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1).get(0);
                mPlaceName = address.getAddressLine(0) +", " + address.getLocality() + ", " + address.getCountryName() + ", " + address.getPostalCode();
            } catch(Exception e) {
                mPlaceName = "address not found";
            }
        }
        else {
            mPlaceName = "location not found";
        }
    }


    @Override
    protected void onStop (){

        super.onStop();
    }

/*
    public void pickPlace() {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                myPlace = likelyPlaces.get(0).getPlace();
                likelyPlaces.release();
            }
        });
    }
*/
}
