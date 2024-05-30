package com.papertrading;

import static android.app.DownloadManager.COLUMN_ID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "StocksDB";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_STOCKS = "stocks";
    private static final String TABLE_WATCHLIST = "watchlist";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_UNIQUE_IDS = "unique_ids";

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
            "id String PRIMARY KEY," +
            "price REAL, " +
            "type TEXT, " +
            "instrument_token INTEGER, " +
            "name TEXT, " +
            "exchange_token INTEGER, " +
            "tradingsymbol TEXT, " +
            "exchange TEXT, " +
            "status TEXT, " +
            "quantity INTEGER" +
            ")";

    private static final String TABLE_LAST_DOWNLOAD = "last_download";
    private static final String COLUMN_LAST_DOWNLOAD_DATE = "last_download_date";

    private static final String CREATE_TABLE_LAST_DOWNLOAD = "CREATE TABLE IF NOT EXISTS " +
            TABLE_LAST_DOWNLOAD + "(" +
            COLUMN_LAST_DOWNLOAD_DATE + " TEXT" + ")";

    private static final String CREATE_TABLE_UNIQUE_IDS = "CREATE TABLE IF NOT EXISTS " +
            TABLE_UNIQUE_IDS + " (" +
            COLUMN_ID+ " TEXT PRIMARY KEY)";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_STOCKS);
        db.execSQL(CREATE_TABLE_WATCHLIST);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_LAST_DOWNLOAD);
        db.execSQL(CREATE_TABLE_UNIQUE_IDS);
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
        long instrumentToken = getInstrumentTokenByTradingSymbol(tradingsymbol);
        cv.put("instrument_token", instrumentToken);
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
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                "id",
                "price",
                "type",
                "instrument_token",
                "name",
                "exchange_token",
                "tradingsymbol",
                "exchange",
                "quantity",
                "status"
        };

        // Define a selection
        String selection = null;

        // Define selection arguments
        String[] selectionArgs = null;

        // Define the sort order
        String sortOrder = "id DESC";

        // Perform the query
        Cursor cursor = db.query(
                "orders",           // The table to query
                projection,         // The array of columns to return (null to return all)
                selection,          // The columns for the WHERE clause
                selectionArgs,      // The values for the WHERE clause
                null,               // don't group the rows
                null,               // don't filter by row groups
                sortOrder           // The sort order
        );

        // Iterate through the cursor and add orders to the list
        while (cursor.moveToNext()) {
            // Extract the values from the cursor
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            long instrumentToken = cursor.getLong(cursor.getColumnIndexOrThrow("instrument_token"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            long exchangeToken = cursor.getLong(cursor.getColumnIndexOrThrow("exchange_token"));
            String tradingSymbol = cursor.getString(cursor.getColumnIndexOrThrow("tradingsymbol"));
            String exchange = cursor.getString(cursor.getColumnIndexOrThrow("exchange"));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));

            // Create an Order object and add it to the list
            Order order = new Order(id, price, type, instrumentToken, name, exchangeToken, tradingSymbol, exchange, quantity,status);
            orders.add(order);
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        // Return the list of orders
        return orders;
    }

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

    public Stock getStockByTradingSymbol(String tradingSymbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        Stock stock = null;
        Cursor cursor = null;

        try {
            // Define the columns you want to retrieve
            String[] projection = {
                    "name",
                    "last_price",
                    "instrument_token",
                    "exchange_token",
                    "exchange"
            };

            // Define the selection criteria
            String selection = "tradingsymbol = ?";
            String[] selectionArgs = {tradingSymbol};

            // Query the database
            cursor = db.query(
                    "stocks",
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            // Check if a stock was found
            if (cursor != null && cursor.moveToFirst()) {
                // Extract the stock details from the cursor
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double lastPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("last_price"));
                long instrumentToken = cursor.getLong(cursor.getColumnIndexOrThrow("instrument_token"));
                int exchangeToken = cursor.getInt(cursor.getColumnIndexOrThrow("exchange_token"));
                String exchange = cursor.getString(cursor.getColumnIndexOrThrow("exchange"));

                // Create a new Stock object with the retrieved details
                stock = new Stock(name, tradingSymbol, lastPrice);
                stock.setInstrumentToken(instrumentToken);
                stock.setExchange_token(exchangeToken);

                stock.setExchange(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the cursor to release its resources
            if (cursor != null) {
                cursor.close();
            }
        }

        // Return the retrieved stock (or null if not found)
        return stock;
    }

    public boolean isDownloadedToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LAST_DOWNLOAD, null, null, null, null, null, null);
        String lastDownloadDate = null;

        if (cursor != null && cursor.moveToFirst()) {
            lastDownloadDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_DOWNLOAD_DATE));
            cursor.close();
        }

        db.close();

        // Compare current date with last download date
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return todayDate.equals(lastDownloadDate);
    }

    public void setDownloadedToday() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_DOWNLOAD_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_LAST_DOWNLOAD, null, values);
        db.close();
    }

    public String getTradingSymbolByInstrumentToken(long instrumentToken) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tradingSymbol = null;
        Cursor cursor = null;

        try {
            // Define the columns you want to retrieve
            String[] projection = {"tradingsymbol"};

            // Define the selection criteria
            String selection = "instrument_token = ?";
            String[] selectionArgs = {String.valueOf(instrumentToken)};

            // Query the database
            cursor = db.query("stocks", projection, selection, selectionArgs, null, null, null);

            // Check if a stock was found
            if (cursor != null && cursor.moveToFirst()) {
                // Extract the trading symbol from the cursor
                tradingSymbol = cursor.getString(cursor.getColumnIndexOrThrow("tradingsymbol"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the cursor to release its resources
            if (cursor != null) {
                cursor.close();
            }
        }

        // Return the retrieved trading symbol (or null if not found)
        return tradingSymbol;
    }
    public long getInstrumentTokenByTradingSymbol(String tradingsymbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        long instrumentToken = -1; // Default value in case no token is found
        Cursor cursor = null;

        try {
            // Define the columns you want to retrieve
            String[] projection = {"instrument_token"};

            // Define the selection criteria
            String selection = "tradingsymbol = ?";
            String[] selectionArgs = {tradingsymbol};

            // Query the database
            cursor = db.query("stocks", projection, selection, selectionArgs, null, null, null);

            // Check if a token was found
            if (cursor != null && cursor.moveToFirst()) {
                // Extract the instrument token from the cursor
                instrumentToken = cursor.getLong(cursor.getColumnIndexOrThrow("instrument_token"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the cursor to release its resources
            if (cursor != null) {
                cursor.close();
            }
        }

        // Return the retrieved instrument token
        return instrumentToken;
    }

    public List<Long> getWatchlistedInstrumentTokens() {
        List<Long> instrumentTokens = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT instrument_token FROM watchlist", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long instrumentToken = cursor.getLong(cursor.getColumnIndex("instrument_token"));
                instrumentTokens.add(instrumentToken);
            }
            cursor.close();
        }
        db.close();
        return instrumentTokens;
    }

    public String generateAndStoreUniqueId() {
        String uuid = null;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Check if UUID already exists
            if (!hasUniqueId()) {
                // Generate UUID
                uuid = UUID.randomUUID().toString();

                // Store UUID in the database
                storeUniqueId(uuid);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating and storing unique ID: " + e.getMessage());
        } finally {
            db.close();
        }
        return uuid;
    }

    public boolean hasUniqueId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_UNIQUE_IDS, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return true; // If there is at least one row in the unique_ids table
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return false; // If the unique_ids table is empty
    }


    public void storeUniqueId(String uuid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, uuid);
        db.insert(TABLE_UNIQUE_IDS, null, values);
        db.close(); // Close the database connection after the operation
    }


    public String retrieveUniqueId() {
        String uuid = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_UNIQUE_IDS, new String[]{COLUMN_ID}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                uuid = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving unique ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return uuid;
    }




}