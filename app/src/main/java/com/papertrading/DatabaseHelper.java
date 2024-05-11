package com.papertrading;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "StocksDB";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_STOCKS = "stocks";
    private static final String TABLE_WATCHLIST = "watchlist";
    private static final String TABLE_ORDERS = "orders";

    private static final String CREATE_TABLE_STOCKS = "CREATE TABLE IF NOT EXISTS " +
            "stocks (" +
            "segment TEXT, " +
            "strike REAL, " +
            "instrument_token INTEGER, " +
            "name TEXT, " +
            "exchange_token INTEGER, " +
            "tradingsymbol TEXT, " +
            "exchange TEXT, " +
            "lot_size INTEGER, " +
            "expiry TEXT, " +
            "instrument_type TEXT, " +
            "tick_size REAL, " +
            "last_price REAL" +
            ")";

    private static final String CREATE_TABLE_WATCHLIST = "CREATE TABLE IF NOT EXISTS " +
            "watchlist (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "tradingsymbol TEXT, " +
            "name TEXT, " +
            "instrument_token INTEGER" +
            ")";

    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE IF NOT EXISTS " +
            "orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "price REAL, " +
            "type TEXT, " +
            "instrument_token INTEGER, " +
            "name TEXT, " +
            "exchange_token INTEGER, " +
            "tradingsymbol TEXT, " +
            "exchange TEXT, " +
            "quantity INTEGER" +
            ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STOCKS);
        db.execSQL(CREATE_TABLE_WATCHLIST);
        db.execSQL(CREATE_TABLE_ORDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database version upgrades here
    }

    public void insertStockIntoDatabase(String[] columns) {
        SQLiteDatabase db = this.getWritableDatabase(); // Use getWritableDatabase directly
        Log.d("inserting", " inserting " +  columns[7] );
        ContentValues cv = new ContentValues();
        cv.put("exchange", columns[0]);
        cv.put("exchange_token", columns[1]);
        cv.put("expiry", columns[2]);
        cv.put("instrument_token", columns[3]);
        cv.put("instrument_type", columns[4]);
        cv.put("last_price", columns[5]);
        cv.put("lot_size", columns[6]);
        cv.put("name", columns[7]);
        cv.put("segment", columns[8]);
        cv.put("strike", columns[9]);
        cv.put("tick_size", columns[10]);
        cv.put("tradingsymbol",columns[11]);

        db.insert("stocks", null, cv);


    }

    public List<Stock> getAllStocks() {
        List<Stock> stockList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase(); // Use getReadableDatabase directly
        Cursor cursor = db.query("stocks", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Stock stock = new Stock(
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("tradingsymbol")),
                        cursor.getDouble(cursor.getColumnIndex("last_price"))
                );

                stockList.add(stock);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "getAllStocks: Fetched " + stockList.size() + " stocks from database.");
        return stockList;
    }

    public void addToWatchlist(String tradingsymbol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tradingsymbol", tradingsymbol);
        db.insert(TABLE_WATCHLIST, null, cv);
        db.close();
    }
    public void updateStockInWatchlist(Stock stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("inWatchlist", stock.isInWatchlist() ? 1 : 0);

        // Updating row
        int rows = db.update("stocks", values, "instrument_token = ?",
                new String[]{String.valueOf(stock.getInstrumentToken())});
        db.close();
        Log.d(TAG, "updateStockInWatchlist: Updated " + rows + " rows for instrument token: " + stock.getInstrumentToken());


    }
    public List<Stock> searchStocks(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Stock> searchResults = new ArrayList<>();

        String[] columns = {"name", "tradingsymbol", "last_price"};
        String selection = "tradingsymbol LIKE ?";
        String[] selectionArgs = {"%" + query + "%"};
        String limit = "25";

        Cursor cursor = db.query(TABLE_STOCKS, columns, selection, selectionArgs, null, null, null,limit);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String tradingsymbol = cursor.getString(cursor.getColumnIndex("tradingsymbol"));
                double lastPrice = cursor.getDouble(cursor.getColumnIndex("last_price"));
                Stock stock = new Stock(name, tradingsymbol, lastPrice);
                searchResults.add(stock);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return searchResults;
    }




    //new
    /*
    public List<Stock> getFilteredStocks(String query) {
        List<Stock> filteredStocks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(TAG, "Search Query: " + query);
        Cursor cursor = db.rawQuery("SELECT * FROM stocks WHERE tradingsymbol LIKE ?", new String[]{"%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String tradingSymbol = cursor.getString(cursor.getColumnIndex("tradingsymbol"));
                double lastPrice = cursor.getDouble(cursor.getColumnIndex("last_price"));
                Log.d(TAG, "Found Stock - Name: " + name + ", Symbol: " + tradingSymbol + ", Price: " + lastPrice);

                // Create a Stock object and add it to the list of filtered stocks
                Stock stock = new Stock(name, tradingSymbol, lastPrice);
                filteredStocks.add(stock);
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "No stocks found for query: " + query);
        }

        cursor.close();

        return filteredStocks;
    }
    */


    /*public List<Stock> getWatchlistStocks() {
        List<Stock> watchlistStocks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("stocks", null, "inWatchlist = ?", new String[]{"1"}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Stock stock = new Stock(
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("tradingsymbol")),
                        cursor.getDouble(cursor.getColumnIndex("last_price"))
                );

                watchlistStocks.add(stock);
            } while (cursor.moveToNext());
        }

        cursor.close();
        Log.d(TAG, "getWatchlistStocks: Retrieved " + watchlistStocks.size() + " stocks in watchlist.");
        return watchlistStocks;
    } */
    public List<Stock> getWatchlistStocks() {
        List<Stock> watchlistStocks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WATCHLIST, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                long instrumentToken = cursor.getLong(cursor.getColumnIndex("instrument_token"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String tradingsymbol = cursor.getString(cursor.getColumnIndex("tradingsymbol"));
                Stock stock = new Stock(name, tradingsymbol, instrumentToken);
                watchlistStocks.add(stock);



            } while (cursor.moveToNext());
        }

        cursor.close();
        return watchlistStocks;
    }


    // Helper method to fetch a Stock object based on instrumentToken
    private Stock getStockByInstrumentToken(long instrumentToken) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCKS, null, "instrument_token = ?", new String[]{String.valueOf(instrumentToken)}, null, null, null);
        Stock stock = null;

        if (cursor.moveToFirst()) {
            stock = new Stock(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("tradingsymbol")),
                    cursor.getDouble(cursor.getColumnIndex("last_price")),
                    true // Assuming all stocks in the watchlist are marked as in watchlist
            );
        }

        cursor.close();
        return stock;
    }
}