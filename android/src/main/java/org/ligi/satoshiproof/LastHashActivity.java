package org.ligi.satoshiproof;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.androidquery.AQuery;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.ligi.axt.AXT;
import org.ligi.axt.listeners.ActivityFinishingOnClickListener;

public class LastHashActivity extends AppCompatActivity {

    private AQuery aQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Last Hash");
        setContentView(R.layout.last_hash);
        aQuery = new AQuery(this);
        new FetchLastHashAsyncTask().execute();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class FetchLastHashAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(@NonNull Void... voids) {
            try {
                final URL url = new URL("https://api.biteasy.com/blockchain/v1/blocks?per_page=1");
                final String s = AXT.at(url).downloadToString();
                if (s != null) {
                    final JSONObject jsonObject = new JSONObject(s);
                    return jsonObject.getJSONObject("data").getJSONArray("blocks").getJSONObject(0).getString("hash");
                }

            } catch (MalformedURLException | JSONException ignored) {
            }

            try {
                // fallback - was not working recently but might come back and then be a fallback
                final URL url = new URL("https://blockexplorer.com/q/latesthash");
                return AXT.at(url).downloadToString();
            } catch (Exception ignored) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable String s) {
            if (s == null) {
                new AlertDialog.Builder(LastHashActivity.this).setMessage("Could not connect to network - please try again later.")
                                                              .setPositiveButton(android.R.string.ok,
                                                                                 new ActivityFinishingOnClickListener(LastHashActivity.this))
                                                              .show();
                return;
            }
            final String url = "http://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=" + s.replace("\n", "");
            final AQuery hashImage = aQuery.id(R.id.hash_image);
            hashImage.visible();
            hashImage.image(url, true, false); // memcache yes - disk no as we want recent stuff
            aQuery.id(R.id.hash_text).text(s);
            super.onPostExecute(s);
        }
    }

}

