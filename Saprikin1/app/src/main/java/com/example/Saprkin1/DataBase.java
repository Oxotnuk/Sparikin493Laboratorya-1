package com.example.Saprkin1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DataBase extends SQLiteOpenHelper {
    public DataBase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase localDataBase) {
        String sqlQuerry = "CREATE TABLE message (number INT PRIMARY KEY, date TEXT,time TEXT, senderNickName TEXT, senderIPaddres TEXT,senderPort INT, content TEXT);";
        localDataBase.execSQL(sqlQuerry);
        sqlQuerry = "CREATE TABLE userData (type TEXT,nickname TEXT, IPaddres TEXT ,Port INT);";
        localDataBase.execSQL(sqlQuerry);
    }

    public int getMaxNumber() {
        SQLiteDatabase localDataBase = getReadableDatabase();
        String sql = "SELECT MAX(number) FROM message;";
        Cursor cur = localDataBase.rawQuery(sql, null);
        if (cur.moveToFirst() == true) {
            return cur.getInt(0);
        }
        return 0;
    }

    public void addMessage(int number, String date, String time, String senderNickName, String senderIPaddress, int senderPort, String content) {
        String snumber = String.valueOf(number);
        SQLiteDatabase localDataBase = getWritableDatabase();
        String sql = "INSERT INTO message VALUES (" + snumber + ", '" + date + "', '" + time + "', '" + senderNickName + "', '" + senderIPaddress + "', '" + senderPort + "', '" + content + "');";
        localDataBase.execSQL(sql);
    }

    public void addUser(String type, String NickName, String IPaddress, int Port) {
        SQLiteDatabase localDataBase = getWritableDatabase();
        String sql = "INSERT INTO userData VALUES ('" + type + "', '" + NickName + "', '" + IPaddress + "', '" + Port + "');";
        localDataBase.execSQL(sql);
    }

    public String getMesage(int number) {
        String snumber = String.valueOf(number);
        SQLiteDatabase localDataBase = getReadableDatabase();
        String sql = "SELECT * FROM message Where id = " + snumber + ";";
        Cursor cur = localDataBase.rawQuery(sql, null);
        if (cur.moveToFirst() == true) {
            return cur.getString(0);
        }
        return "";
    }

    public boolean getUser(String type, User user) {
        SQLiteDatabase localDataBase = getReadableDatabase();
        String sql = "SELECT * FROM userData Where type = '" + type + "';";
        Cursor cur = localDataBase.rawQuery(sql, null);
        if (cur.moveToFirst() == true) {
            user.Type = cur.getString(0);
            user.NickName = cur.getString(1);
            user.IPaddress = cur.getString(2);
            user.Port = cur.getInt(3);
            return true;
        } else {
            return false;
        }
    }

    public boolean delAllUser() {
        SQLiteDatabase localDataBase = getWritableDatabase();
        String sql = "DELETE FROM userData;";
        localDataBase.execSQL(sql);
        return true;
    }

    public boolean saveMessage(int number, String content) {
        String snumber = String.valueOf(number);
        SQLiteDatabase localDataBase = getWritableDatabase();
        String sql = "UPDATE message SET content = '" + content + "' WHERE id = " + snumber + ";";
        localDataBase.execSQL(sql);
        return true;
    }

    public boolean delMessage(int number) {
        String snumber = String.valueOf(number);
        SQLiteDatabase localDataBase = getWritableDatabase();
        String sql = "DELETE FROM message WHERE id = " + snumber + ";";
        localDataBase.execSQL(sql);
        return true;
    }

    public boolean delAllMessages() {
        SQLiteDatabase localDataBase = getWritableDatabase();
        String sql = "DELETE FROM message;";
        localDataBase.execSQL(sql);
        return true;
    }

    public void getAllMessages(ArrayList<Message> ALM) {
        SQLiteDatabase localDataBase = getReadableDatabase();
        String sql = "SELECT * FROM message;";
        Cursor cur = localDataBase.rawQuery(sql, null);
        if (cur.moveToFirst() == true) {
            do {
                Message message = new Message();
                message.number = cur.getInt(0);
                message.date = cur.getString(1);
                message.time = cur.getString(2);
                message.senderNickName = cur.getString(3);
                message.senderIPaddress = cur.getString(4);
                message.senderPort = cur.getInt(5);
                message.content = cur.getString(6);
                ALM.add(message);
            } while (cur.moveToNext() == true);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase localDataBase, int oldVersion, int newVersion) {

    }
}
