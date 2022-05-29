package com.example.alarmapp;

public class ReaderContract {
    private ReaderContract() {

    }

    public static class Entry {
        public static final String TABLE_NAME = "alarmTable";
        public static final String ID = "id";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_MINUTE = "minute";
    }
}
