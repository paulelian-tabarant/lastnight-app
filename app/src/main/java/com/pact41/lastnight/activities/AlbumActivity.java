package com.pact41.lastnight.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.pact41.lastnight.R;
import com.pact41.lastnight.model.LastNightStateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Robin on 14/04/2017.
 */

public class AlbumActivity extends NetworkActivity {

    private ListView album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album);

        album= (ListView) findViewById(R.id.album_photos);

        String partyName=getIntent().getStringExtra(PartyRecapActivity.PARTY_NAME_RECAP);

        new ShowPhotosTask().execute(partyName);

    }

    private class ShowPhotosTask extends AsyncTask<String, Integer, AlbumAdapter> {

        @Override
        protected AlbumAdapter doInBackground(String... params){
            ArrayList<AlbumItem> list_of_photos= new ArrayList<AlbumItem>();
            ArrayList<Bitmap> photos=getConnection().getAlbum(params[0]);
            AlbumItem item;
            for (Bitmap photo : photos){
                item=new AlbumItem("",photo);
                list_of_photos.add(item);
            }
            AlbumAdapter adapter = new AlbumAdapter(AlbumActivity.this,list_of_photos);
            return adapter;
        }

        @Override
        protected void onPostExecute(AlbumAdapter adapter) {

            album.setAdapter(adapter);

        }
    }

    private class AlbumAdapter extends ArrayAdapter<AlbumItem> {

        private Context context;
        private List<AlbumItem> photos;

        public AlbumAdapter(Context context, List<AlbumItem> photos) {
            super(context, 0, photos);
            this.context=context;
            this.photos=photos;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if(row == null) {
                // inflate row layout and assign to 'row'
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row=inflater.inflate(R.layout.album_single_image, parent, false);

            }
            final AlbumItem thisItem = getItem(position);
            final ImageView photo = (ImageView) row.findViewById(R.id.album_photo);
            photo.setImageBitmap(thisItem.bmp);

            return row;
        }
    }

    private class AlbumItem{
        private String infos;
        private Bitmap bmp;

        public AlbumItem(String infos, Bitmap bmp){
            this.infos=infos;
            this.bmp=bmp;
        }
    }

}
