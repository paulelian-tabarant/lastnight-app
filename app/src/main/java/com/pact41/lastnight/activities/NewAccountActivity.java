package com.pact41.lastnight.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.pact41.lastnight.R;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by Robin on 26/01/2017.
 */

public class NewAccountActivity extends NetworkActivity{

    private EditText login,password,confirm_password,last_name,first_name,mail,confirm_mail;
    private Button select_picture;
    private Button sign_up;
    private ImageView selected_picture;
    private final int PHOTO_RESULT=0;
    private final int GALLERY_RESULT=1;
    private Bitmap photo;

    private class SubscribeTask extends AsyncTask<String, Integer, Boolean>{
        public Boolean doInBackground(String... params){
            String myId = params[0];
            String firstName = params[1];
            String lastName = params[2];
            String email = params[3];
            String password = params[4];
            return getConnection().addUtilisateur(myId, firstName, lastName, email, password);
        }

        public void onPostExecute(Boolean isRegistered){
            if(isRegistered) {
                setResult(RESULT_OK);
                finish();
            }
            else
                Toast.makeText(NewAccountActivity.this, R.string.new_account_creation_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private class PhotoTask extends AsyncTask<Map.Entry<String,Bitmap>, Integer, Boolean>{
        public Boolean doInBackground(Map.Entry<String,Bitmap>... params){
            String myId = params[0].getKey();
            Bitmap photo= params[0].getValue();
            return getConnection().addProfilePhoto(myId,photo);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_account);

        login=(EditText) findViewById(R.id.new_account_login);

        password= (EditText) findViewById(R.id.new_account_password);
        password.setTransformationMethod(new PasswordTransformationMethod());

        confirm_password=(EditText) findViewById(R.id.new_account_confirm_password);
        confirm_password.setTransformationMethod(new PasswordTransformationMethod());

        last_name=(EditText) findViewById(R.id.new_account_last_name);

        first_name=(EditText) findViewById(R.id.new_account_first_name);

        mail=(EditText) findViewById(R.id.new_account_mail);
        confirm_mail=(EditText) findViewById(R.id.new_account_confirm_mail);

        selected_picture=(ImageView) findViewById(R.id.new_account_picture);

        final CharSequence[] picture_choice = {"Camera","Galerie"};

        photo =BitmapFactory.decodeResource(getResources(),R.drawable.default_profile_picture);

        select_picture=(Button) findViewById(R.id.new_account_load_picture);
        select_picture.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                AlertDialog.Builder alert = new AlertDialog.Builder(NewAccountActivity.this);
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


        sign_up=(Button) findViewById(R.id.new_account_sign_up);
        sign_up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                login.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                password.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                confirm_password.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                mail.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                confirm_mail.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                first_name.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                last_name.setBackgroundColor(getResources().getColor(R.color.colorBackground));

                if (!isEmptyEditText(login) && !isEmptyEditText(password) && !isEmptyEditText(confirm_password) && !isEmptyEditText(mail) && !isEmptyEditText(confirm_mail) && !isEmptyEditText(first_name) && !isEmptyEditText(last_name)) {
                    if (isValidEmail(mail.getText())) {
                        if (mail.getText().toString().equals(confirm_mail.getText().toString())) {
                            if (password.getText().toString().equals(confirm_password.getText().toString())) {
                                new SubscribeTask().execute(
                                        login.getText().toString().trim(),
                                        first_name.getText().toString().trim(),
                                        last_name.getText().toString().trim(),
                                        mail.getText().toString().trim(),
                                        password.getText().toString().trim());
                                String logintext= login.getText().toString();
                                Map.Entry<String,Bitmap> pair=new AbstractMap.SimpleEntry<>(logintext,photo);
                                new PhotoTask().execute(pair);
                            } else {
                                Toast toast = Toast.makeText(NewAccountActivity.this, R.string.new_account_fail_passwords_notsame, Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(NewAccountActivity.this, R.string.new_account_fail_mails_notsame, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    } else {
                        Toast toast = Toast.makeText(NewAccountActivity.this, R.string.new_account_fail_mail, Toast.LENGTH_LONG);
                        toast.show();
                    }
                } else {
                    Toast toast= Toast.makeText(NewAccountActivity.this, R.string.new_account_null, Toast.LENGTH_LONG );
                    toast.show();
                    if (isEmptyEditText(login)) login.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(password)) password.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(confirm_password)) confirm_password.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(mail)) mail.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(first_name)) first_name.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(confirm_mail)) confirm_mail.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(last_name)) last_name.setBackgroundColor(getResources().getColor(R.color.colorError));


                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        switch(requestCode) {
            case PHOTO_RESULT:
                if(resultCode == RESULT_OK){
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    selected_picture.setImageBitmap(bitmap);
                    photo=bitmap;

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
                        selected_picture.setImageBitmap(rotatedBitmap);
                        photo=rotatedBitmap;
                    }
                    catch (Exception e){
                        Toast toast=Toast.makeText(NewAccountActivity.this, R.string.new_account_fail_picture, Toast.LENGTH_LONG);
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
