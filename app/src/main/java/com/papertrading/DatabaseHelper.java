package com.papertrading;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StocksDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_STOCKS = "stocks";
    private static final String CREATE_TABLE_STOCKS = "CREATE TABLE " + TABLE_STOCKS + "("
            + "exchange TEXT,"
            + "exchange_token INTEGER,"
            + "expiry TEXT,"
            + "instrument_token INTEGER PRIMARY KEY,"
            + "instrument_type TEXT,"
            + "last_price REAL,"
            + "lot_size INTEGER,"
            + "name TEXT,"
            + "segment TEXT,"
            + "strike REAL,"
            + "tick_size REAL,"
            + "tradingsymbol TEXT,"
            + "inWatchlist INTEGER DEFAULT 0" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STOCKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database version upgrades here
    }

    public void insertStockIntoDatabase(String[] columns) {
        SQLiteDatabase db = this.getWritableDatabase(); // Use getWritableDatabase directly
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
        cv.put("tradingsymbol", columns[11]);

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
        return stockList;
    }
    public void updateStockInWatchlist(Stock stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("inWatchlist", stock.isInWatchlist() ? 1 : 0);

        // Updating row
        db.update("stocks", values, "instrument_token = ?",
                new String[]{String.valueOf(stock.getInstrumentToken())});
        db.close();
    }
    public List<Stock> getWatchlistStocks() {
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
        return watchlistStocks;
    }
}
