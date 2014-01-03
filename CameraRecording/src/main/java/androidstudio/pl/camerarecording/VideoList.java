package androidstudio.pl.camerarecording;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FilenameFilter;

public class VideoList extends Activity {
    private final String TAG_LOG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_video_list);
        final VideoView videoView = (VideoView) findViewById(R.id.videView);
        final MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_list_videos, getVideos());
        final ListView listView = (ListView) findViewById(R.id.videoList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                final String path = parent.getItemAtPosition(position).toString();
                final Uri vido = Uri.parse(path);

                videoView.setVideoURI(vido);
                videoView.setMediaController(mediaController);
                videoView.start();
            }
        });
    }

    private String[] getVideos() {
        final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Videos");
        final File[] videolist = mediaStorageDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp4");
            }
        });

        if (videolist == null) return new String[]{""};
        final String[] mfiles = new String[videolist.length];
        for (int i = 0; i < videolist.length; i++) {
            mfiles[i] = videolist[i].getAbsolutePath();
        }
        Log.d(TAG_LOG, "" + mfiles.length);
        return mfiles;
    }
}
