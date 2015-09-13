package mhacks.six.tunedemic;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TuneFragment extends Fragment {

    View rootview;
    ArrayList<Songs> myTunes;
    MobileServiceClient mClient;
    MobileServiceTable<Songs> mSongsTable;
    BufferedReader br;

    Button bDrop;
    TextView tSong;
    TextView tArtist;

    public TuneFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_tune, container, false);
        myTunes = new ArrayList<>();

        bDrop = (Button) rootview.findViewById(R.id.dropbutton);
        tArtist = (TextView) rootview.findViewById(R.id.artisttext);
        tSong = (TextView) rootview.findViewById(R.id.titletext);

        try {
            mClient = new MobileServiceClient(
                    "https://tunedemic.azure-mobile.net/",
                    "ceLnwHzMkiIjIHQqCTZSLHjGyfVxPJ90",
                    this.getActivity()
            );
        } catch (MalformedURLException murl) {
            Log.e("TAG", "Malformed URL");
        }

        getSongs();

        return rootview;
    }

    public void getSongs() {
        final File inFile = new File(getActivity().getFilesDir(), "mytunes");
        if (inFile.exists()){

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        br = new BufferedReader(new FileReader(inFile));
                        String line = "";

                        mSongsTable = mClient.getTable(Songs.class);
                        MobileServiceList<Songs> result = mSongsTable.execute().get();

                        while((line=br.readLine()) != null){
                            for(Songs oneSong : result){
                                MobileServiceList<Songs> second = mSongsTable.where().field("url").eq(line).execute().get();
                                if (second.getTotalCount() != 0)
                                    myTunes.add(second.get(0));
                            }
                        }
                    } catch (Exception exception) {
                        //   Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void myVoid) {

                    updateUI();
                }
            }.execute();
        }
        else {
            tArtist.setText("No songs in your library");
            tSong.setText("Empty!");
            bDrop.setVisibility(View.GONE);
        }
    }

    public void updateUI(){
        //use arraylist myTunes here to populate array adapters and list items


        //String[] items = new String[] { "Vegetables","Fruits","Flower Buds","Legumes","Bulbs","Tubers" };
         //ArrayAdapter<Songs> songsAdapter = new ArrayAdapter<Songs>(this, android.R.layout.list_item, myTunes);
        //ArrayAdapter<String> mlist=  new ArrayAdapter<String>(this, 3 , items);

        tSong.setText(myTunes.get(0).name);
        tArtist.setText(myTunes.get(0).artist);

        bDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nodes node = new Nodes();
                node.url = myTunes.get(0).url;
                node.owner = " ";
                LatLng myPos = MainActivity.globalPos;
                node.latitude = ((float) myPos.latitude);
                node.longitude = ((float) myPos.longitude);
                node.radius = 20;
                node.votes = 0;
                mClient.getTable(Nodes.class).insert(node, new TableOperationCallback<Nodes>() {
                    public void onCompleted(Nodes entity, Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            // Insert succeeded
                            Toast.makeText(getActivity().getApplicationContext(), "Added", Toast.LENGTH_LONG).show();

                        } else {
                            // Insert failed
                            Toast.makeText(getActivity().getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });
    }
}

