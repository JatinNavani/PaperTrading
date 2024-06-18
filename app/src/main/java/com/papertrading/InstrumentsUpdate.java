package com.papertrading;

import android.os.AsyncTask;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class InstrumentsUpdate extends AsyncTask<Void,Void,Void> {
    private DatabaseHelper dbHelper;

    private InstrumentsUpdate() {
    }




    public InstrumentsUpdate(DatabaseHelper _dbHelper) {
        this.dbHelper = _dbHelper;

    }
    @Override
    protected Void doInBackground(Void... voids) {

            try {
                Log.d(Thread.currentThread().getId() +"", Thread.currentThread().getId()  + " in thread " );


                String urlString = "YOUR_URL"; //Put your url to download csv of stocks

                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                //connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                Log.d("file downloaded ", "file downloaded" );

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    String[] columns = line.split(",");
                    // Skip headers or add condition to ignore malformed lines
                    if (columns.length == 12) {

                        // Call insertStockIntoDatabase using dbHelper instance
                        dbHelper.insertStockIntoDatabase(columns);

                    }
                }
                Log.d("CSV_DOWNLOAD", "Inserted all stocks " );

                reader.close();
                inputStream.close();

            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;

        }
        // yooooo
    }


