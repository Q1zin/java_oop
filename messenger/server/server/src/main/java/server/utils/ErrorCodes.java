package server.utils;

public class ErrorCodes {
    // Общие ошибки
    public static final int GEN_MISSING_FIELDS = 1000;
    public static final int GEN_UNEXPECTED_ERROR = 1001;

    // LOGIN
    public static final int LOGIN_INVALID_PASSWORD = 1100;
    public static final int LOGIN_USER_NOT_FOUND = 1101;

    // REGISTER
    public static final int REGISTER_USER_EXISTS = 1200;
    public static final int REGISTER_INSERT_FAILED = 1201;
    public static final int REGISTER_SQL_ERROR = 1202;

    // GET_USERS
    public static final int GET_USERS_INVALID_HASH = 1300;
    public static final int GET_USERS_SQL_ERROR = 1301;

    // SEND_MESSAGE
    public static final int SEND_MESSAGE_INVALID_HASH = 1400;
    public static final int SEND_MESSAGE_SQL_ERROR = 1401;
    public static final int SEND_MESSAGE_MISSING_FIELDS = 1402;

    // GET_MESSAGES
    public static final int GET_MESSAGES_INVALID_HASH = 1500;
    public static final int GET_MESSAGES_SQL_ERROR = 1501;
    public static final int GET_MESSAGES_MISSING_FIELDS = 1502;

    // CHECK_LOGIN
    public static final int CHECK_LOGIN_INVALID_HASH = 1600;
    public static final int CHECK_LOGIN_SQL_ERROR = 1601;
    public static final int CHECK_LOGIN_MISSING_FIELDS = 1602;

    // LOGOUT
    public static final int LOGOUT_SQL_ERROR = 1700;

    // HEARTBEAT
    public static final int HEARTBEAT_INVALID_HASH = 1800;
    public static final int HEARTBEAT_SQL_ERROR = 1801;
    public static final int HEARTBEAT_MISSING_FIELDS = 1802;

    // REQUEST
    public static final int REQUEST_UNKNOWN_TYPE = 1900;
    public static final int REQUEST_INVALID_JSON = 1901;

    private ErrorCodes() {}
}