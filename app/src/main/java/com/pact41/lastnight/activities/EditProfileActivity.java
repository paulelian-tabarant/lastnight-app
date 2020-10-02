package com.pact41.lastnight.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.model.LastNightStateManager;

import java.util.AbstractMap;
import java.util.Map;

public class EditProfileActivity extends NetworkActivity {

    private EditText last_name,first_name,mail,confirm_mail,password,confirm_password;

    private ImageView currentPicture;
    private Button loadPicture;
    private final int PHOTO_RESULT=0;
    private final int GALLERY_RESULT=1;
    private Bitmap profilePicture;

    private Button confirmChanges;


    private class EditTask extends AsyncTask<String, Integer, Boolean>{
        @Override
        public Boolean doInBackground(String... params) {
            String myId = params[0];
            String firstName = params[1];
            String lastName = params[2];
            String email = params[3];
            String password = params[4];
            return getConnection().changeUtilisateurInfo(myId,firstName,lastName,email,password);
        }

        @Override
        protected void onPostExecute(Boolean isModified) {
            if(isModified)
                setResult(RESULT_OK);
            else {
                setResult(RESULT_CANCELED);
            }
            finish();

        }
    }

    private class FillBlanksTask extends AsyncTask<String, Integer, String[]>{
        @Override
        public String[] doInBackground(String... params) {
            String myId = params[0];
            String[] infos={getConnection().getUserLstNme(myId),getConnection().getUserFstNme(myId),getConnection().getUserEmail(myId),getConnection().getUserPssWd(myId),myId};
            return infos;
        }
        public void onPostExecute(String[] infos){
            if (isEmptyEditText(last_name)) last_name.setText(infos[0]);
            if (isEmptyEditText(first_name)) first_name.setText(infos[1]);
            if (isEmptyEditText(mail)) mail.setText(infos[2]);
            if (isEmptyEditText(password)) password.setText(infos[3]);
            new EditTask().execute(
                    infos[4].trim(),
                    first_name.getText().toString().trim(),
                    last_name.getText().toString().trim(),
                    mail.getText().toString().trim(),
                    password.getText().toString().trim());
        }
    }

    private class getPhotoTask extends AsyncTask<String, Integer, Bitmap>{

        public Bitmap doInBackground(String... params){
            return getConnection().getProfilePhoto(params[0]);
        }

        public void onPostExecute(Bitmap bmp){
            profilePicture=bmp;
        }
    }

    private class updatePhotoTask extends AsyncTask<Map.Entry<String,Bitmap>, Integer, Boolean>{
        public Boolean doInBackground(Map.Entry<String,Bitmap>... params){
            String myId = params[0].getKey();
            Bitmap photo= params[0].getValue();
            return getConnection().addProfilePhoto(myId,photo);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);



        last_name=(EditText) findViewById(R.id.edit_profile_last_name);
        first_name=(EditText) findViewById(R.id.edit_profile_first_name);
        mail=(EditText) findViewById(R.id.edit_profile_mail);
        confirm_mail=(EditText) findViewById(R.id.edit_profile_confirm_mail);
        password = (EditText)findViewById(R.id.edit_profile_password);
        password.setTransformationMethod(new PasswordTransformationMethod());
        confirm_password = (EditText)findViewById(R.id.edit_profile_confirm_password);
        confirm_password.setTransformationMethod(new PasswordTransformationMethod());

        currentPicture = (ImageView)findViewById(R.id.edit_profile_current_picture);
        loadPicture = (Button)findViewById(R.id.edit_profile_load_new_picture);

        LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();
        final String currentUser=stateManager.getCurrentUser();

        new getPhotoTask().execute(currentUser);

        confirmChanges = (Button)findViewById(R.id.edit_profile_confirm_changes);
        confirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                confirm_password.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                mail.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                confirm_mail.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                first_name.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                last_name.setBackgroundColor(getResources().getColor(R.color.colorBackground));

                if(isEmptyEditText(first_name) && isEmptyEditText(last_name) && isEmptyEditText(mail) && isEmptyEditText(confirm_mail) && isEmptyEditText(password) && isEmptyEditText(confirm_password) && currentPicture.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.default_profile_picture).getConstantState())){
                    password.setBackgroundColor(getResources().getColor(R.color.colorError));
                    confirm_password.setBackgroundColor(getResources().getColor(R.color.colorError));
                    mail.setBackgroundColor(getResources().getColor(R.color.colorError));
                    confirm_mail.setBackgroundColor(getResources().getColor(R.color.colorError));
                    first_name.setBackgroundColor(getResources().getColor(R.color.colorError));
                    last_name.setBackgroundColor(getResources().getColor(R.color.colorError));
                    Toast.makeText(EditProfileActivity.this, R.string.edit_profile_error, Toast.LENGTH_LONG).show();
                }
                else{
                    if(!(mail.getText().toString().equals(confirm_mail.getText().toString()))){
                        Toast.makeText(EditProfileActivity.this, R.string.new_account_fail_mails_notsame, Toast.LENGTH_LONG).show();
                        mail.setBackgroundColor(getResources().getColor(R.color.colorError));
                        confirm_mail.setBackgroundColor(getResources().getColor(R.color.colorError));
                    }
                    else{
                        if(!(password.getText().toString().equals(confirm_password.getText().toString()))) {
                            Toast.makeText(EditProfileActivity.this, R.string.new_account_fail_passwords_notsame, Toast.LENGTH_LONG).show();
                            password.setBackgroundColor(getResources().getColor(R.color.colorError));
                            confirm_password.setBackgroundColor(getResources().getColor(R.color.colorError));
                        }
                        else{
                            if (!isValidEmail(mail.getText().toString()) && !isEmptyEditText(mail)){
                                Toast.makeText(EditProfileActivity.this, R.string.new_account_fail_mail, Toast.LENGTH_LONG).show();
                            }
                            else{
                                Map.Entry<String,Bitmap> pair=new AbstractMap.SimpleEntry<>(currentUser,profilePicture);
                                new updatePhotoTask().execute(pair);
                                new FillBlanksTask().execute(currentUser);
                            }

                        }
                    }
                }

            }
        });

        final CharSequence[] picture_choice = {"Camera","Galerie"};

        loadPicture.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                AlertDialog.Builder alert = new AlertDialog.Builder(EditProfileActivity.this);
                alert.setTitle("Comment choisir la photo?");
                alert.setSingleChoiceItems(picture_choice,-1, new
                        DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(picture_choice[which]=="Camera") {
                                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(cameraIntent,PHOTO_RESULT);
                                    dialog.dismiss();

                                }

                                else if (picture_choice[which]=="Galerie") {
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galleryIntent,GALLERY_RESULT);
                                    dialog.dismiss();
                                }
                            }
                        });
                alert.show();
            }


        });
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        switch(requestCode) {
            case PHOTO_RESULT:
                if(resultCode == RESULT_OK){
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    currentPicture.setImageBitmap(photo);
                    profilePicture=photo;
                }

                break;
            case GALLERY_RESULT:
                if(resultCode == RESULT_OK){
                    Uri imageUri = data.getData();
                    String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                    Cursor cur = managedQuery(imageUri, orientationColumn, null, null, null);
                    int orientation = -1;
                    if (cur != null && cur.moveToFirst()) {
                        orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                    }
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        currentPicture.setImageBitmap(rotatedBitmap);
                        profilePicture=rotatedBitmap;
                    }
                    catch (Exception e){
                        Toast toast=Toast.makeText(EditProfileActivity.this, R.string.new_account_fail_picture, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

                break;

        }

    }
    private final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private final static boolean isEmptyEditText(EditText et){
        if(et.getText().toString().trim().isEmpty()){
            return true;
        }
        return false;
    }
}
