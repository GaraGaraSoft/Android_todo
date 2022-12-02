package and.todo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class EditDatabaseHelper extends SQLiteOpenHelper {
    static final private String DBNAME = "todo"; //データベースファイル名
    static final private int VERSION = 7; //バージョン番号

    public EditDatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    //
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    //データベース作成時にテーブル作成
    @Override
    public void onCreate(SQLiteDatabase db) {
        if(db!=null){
            //登録した目標、スケジュールデータのテーブル
            db.execSQL("create table if not exists ToDoData (\n" +
                    "id integer primary key autoincrement,\n" +
                    "title text,\n" +
                    "content text,\n" +
                    "level text,\n" +
                    "big integer,\n" +
                    "bigtitle text,\n"+
                    "bighold integer,\n"+
                    "middle integer,\n" +
                    "middletitle text,\n"+
                    "middlehold integer,\n"+
                    "date text,\n" +
                    "hold integer,\n" +
                    "important integer," +
                    "memo text," +
                    "proceed integer," +
                    "fin integer)");
            //データ編集の記録テーブル
            db.execSQL("create table if not exists LogData (\n" +
                    "logid integer primary key autoincrement,\n" +
                    "ope text,\n" + //操作した内容
                    "id integer,\n" + //操作するデータのID
                    "beforetitle text,\n" + //変更まえのデータ
                    "beforecontent text,\n" +
                    "beforelevel text,\n" +
                    "beforebig integer,\n" +
                    "beforebigtitle text,\n"+
                    "beforebighold integer,\n"+
                    "beforemiddle integer,\n" +
                    "beforemiddletitle text,\n"+
                    "beforemiddlehold integer,\n"+
                    "beforedate text,\n" +
                    "beforehold integer,\n" +
                    "beforeimportant integer," +
                    "beforememo text," +
                    "beforeproceed integer," +
                    "beforefin integer," +
                    "aftertitle text,\n" + //変更後のデータ
                    "aftercontent text,\n" +
                    "afterlevel text,\n" +
                    "afterbig integer,\n" +
                    "afterbigtitle text,\n"+
                    "afterbighold integer,\n"+
                    "aftermiddle integer,\n" +
                    "aftermiddletitle text,\n"+
                    "aftermiddlehold integer,\n"+
                    "afterdate text,\n" +
                    "afterhold integer,\n" +
                    "afterimportant integer," +
                    "aftermemo text," +
                    "afterproceed integer," +
                    "afterfin integer)");
            //日々のこなしたタスクを記録するテーブル
            db.execSQL("create table if not exists DailyData (\n" +
                    "id integer primary key autoincrement,\n" + //主キーID
                    "dailytaskid integer,\n"+ //こなしたタスクID
                    "dailytasktitle text,\n"+ //こなしたタスクタイトル
                    "dailycontent text,\n"+ //こなした量
                    "level text,\n"+ //データの目標レベル
                    "date text)"); //日付

        }
    }

    //データベースのバージョンアップ時テーブルを再生成
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        if(db != null){
            db.execSQL("drop table if exists DailyData");
            onCreate(db);
        }
    }
}
