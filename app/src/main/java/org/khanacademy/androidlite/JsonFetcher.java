package org.khanacademy.androidlite;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class JsonFetcher {
    private static final int TIMEOUT_MS = 10000;

    public static void prefetchJson(final URL url) {
        fetchJsonAsync(url, jsonObject -> {});
    }

    public static JSONObject fetchJson(final URL url) throws IOException, JSONException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);

        try {
            connection.connect();

            switch (connection.getResponseCode()) {
                case 200:
                case 201:
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()
                    ));

                    final StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                    reader.close();

                    return new JSONObject(builder.toString());
            }

            throw new IOException(String.valueOf(connection.getResponseCode()));
        } finally {
            connection.disconnect();
        }
    }

    public static void fetchJsonAsync(final URL url, final Action1<JSONObject> onFetch) {
        final FetchJsonTask asyncTask = new FetchJsonTask(onFetch);
        asyncTask.execute(url);
    }

    private static class FetchJsonTask extends AsyncTask<URL, Void, JSONObject> {
        private final Action1<JSONObject> mOnFetch;

        FetchJsonTask(final Action1<JSONObject> onFetch) {
            mOnFetch = onFetch;
        }

        @Override
        protected JSONObject doInBackground(final URL... urls) {
            try {
                return JsonFetcher.fetchJson(urls[0]);
            } catch (final IOException | JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final JSONObject jsonObject) {
            mOnFetch.call(jsonObject);
        }
    }
}
