package mhacks.six.tunedemic;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import org.w3c.dom.Node;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;


/**
 * A simple {@link Fragment} subclass.
 */
public class songFragment extends Fragment{

    Bundle args;
    String nodeID;
    MobileServiceClient mClient;
    String playURL;
    String uploader;
    String title = "", artist = "";
    float nodeScore;
    int scoreCount;
    Boolean playing, already;

    MediaPlayer mediaPlayer;

    ImageButton playButton;
    ImageButton viewVoteDown;
    ImageButton viewVoteUp;
    ImageButton pickup;
    View rootview;
    TextView viewTitle;
    TextView viewArtist;
    TextView viewScore;
    TextView viewUploader;

    Nodes n;


    public songFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        args = getArguments();
        nodeID = args.getString("nodeID");
        playURL = "";
        playing = false;
        rootview =  inflater.inflate(R.layout.fragment_song, container, false);
        viewTitle = (TextView) rootview.findViewById(R.id.viewTitle);
        viewArtist = (TextView) rootview.findViewById(R.id.viewArtist);
        viewScore = (TextView) rootview.findViewById(R.id.viewScore);
        viewUploader = (TextView) rootview.findViewById(R.id.viewUploader);
        try{
            mClient = new MobileServiceClient(
                    "https://tunedemic.azure-mobile.net/",
                    "ceLnwHzMkiIjIHQqCTZSLHjGyfVxPJ90",
                    this.getActivity()
            );
        }
        catch(MalformedURLException murl){
            Log.e("TAG", "Malformed URL");
        }
        getSong();
        return rootview;
    }

    public void getSong(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceTable<Songs> mSongsTable;
                    MobileServiceTable<Nodes> mNodesTable;
                    mSongsTable = mClient.getTable(Songs.class);
                    mNodesTable = mClient.getTable(Nodes.class);
                    MobileServiceList<Nodes> getNodes = mNodesTable.where().field("id").eq(nodeID).execute().get();
                    MobileServiceList<Songs> result = null;
                    for (Nodes str : getNodes){
                        uploader = str.owner;
                        playURL = str.url;
                        nodeScore = str.votes;
                        n = str;
                        result = mSongsTable.where().field("url").eq(playURL).execute().get();
                    }
                    if (result != null) {
                        for (Songs so : result) {
                            artist = so.artist;
                            title = so.name;
                        }
                    }
                } catch (Exception exception) {
                    Log.e("NODES", "THERE WAS A PROBLEM");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                viewTitle.setText("\"" + title + "\"");
                viewArtist.setText(artist);
                viewScore.setText(""+(int)nodeScore);
                viewUploader.setText(uploader);
                //Toast.makeText(getActivity().getApplicationContext(), "PostExecute", Toast.LENGTH_LONG).show();
                setUpInit();

            }
        }.execute();
    }

    public void setUpInit() {
        playButton = (ImageButton) rootview.findViewById(R.id.playButton);
        pickup = (ImageButton) rootview.findViewById(R.id.pickUpButton);
        already = false;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playing = false;
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    if (!already) {
                        songInit();
                        already = true;
                    }
                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.pause);

                } else {
                    mediaPlayer.pause();
                    playButton.setImageResource(R.drawable.playl);
                }
            }
        });
        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File outFile = new File(getActivity().getFilesDir(), "mytunes");
                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(new FileOutputStream(outFile));
                    out.write(playURL.getBytes());
                    Toast.makeText(getActivity().getApplicationContext(), "Song Saved!", Toast.LENGTH_LONG).show();
                        if (out != null)
                            out.close();

                }
                catch(IOException ioe){

                }
            }
        });

        viewVoteUp = (ImageButton) rootview.findViewById(R.id.viewVoteUp);
        viewVoteDown = (ImageButton) rootview.findViewById(R.id.viewVoteDown);

        viewVoteUp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        n.votes += 1;
                        n.radius += 20;
                        mClient.getTable(Nodes.class).update(n);
                        viewScore.setText(String.valueOf(n.votes));
                    }
                }
        );

        viewVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                n.votes -= 1;
                n.radius -= 20;
                if (n.radius < 0)
                    n.radius = 0;
                mClient.getTable(Nodes.class).update(n);
                viewScore.setText(String.valueOf(n.votes));
            }
        });
    }


    public void songInit() {
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource("http://" + playURL);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

