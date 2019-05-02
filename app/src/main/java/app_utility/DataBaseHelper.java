package app_utility;


public class DataBaseHelper {

    //private variables
    private int _id;
    //private String _phone_number;
    private int _type;
    private String _message;
    private String _time;
    private int _status;

    // Empty constructor
    public DataBaseHelper() {

    }

    public DataBaseHelper(int _type, String _message, String _time, int _status){
        this._type = _type;
        this._message = _message;
        this._time = _time;
        this._status = _status;
    }

    public DataBaseHelper(int _status){
        this._status = _status;
    }

    // getting ID
    public int get_id() {
        return this._id;
    }

    // setting id
    public void set_id(int id) {
        this._id = id;
    }

    public int get_type() {
        return this._type;
    }

    // setting id
    public void set_type(int type) {
        this._type = type;
    }

    public String get_message() {
        return this._message;
    }

    public void set_message(String message) {
        this._message = message;
    }

    public String get_time() {
        return this._time;
    }

    public void set_time(String time) {
        this._time = time;
    }

    public void set_status(int status) {
        this._status = status;
    }

    public int get_status() {
        return this._status;
    }
}
