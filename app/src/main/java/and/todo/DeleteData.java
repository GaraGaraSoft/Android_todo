package and.todo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DeleteData {
    EditDatabaseHelper helper;
    Activity activity;
    ContentValues lcv = null;


    public DeleteData(Activity activity){
        this.activity = activity;
        lcv = new ContentValues(); //ログデータ用のContentValues
        helper = new EditDatabaseHelper(activity);

    }

    boolean delete(String dlevel,int did){ //データベースから当該データを削除する
        boolean success = false; //削除の成功判定

        try(SQLiteDatabase db = helper.getWritableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try {


                //選択IDデータを取得
                String[] sels = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","date","content","hold","important","memo","proceed","fin"};
                String[] sellevel = {dlevel, String.valueOf(did)};
                Cursor selcs = db.query("ToDoData",sels,"level=? and id=?",sellevel,null,null,null,null);
                boolean next = selcs.moveToFirst();
                if(next){
                    lcv.put("ope","delete");
                    lcv.put("beforelevel",dlevel);
                    lcv.put("id",did);
                    lcv.put("beforetitle",selcs.getString(1));
                    lcv.put("beforebig",selcs.getInt(2));
                    lcv.put("beforebigtitle",selcs.getString(3));
                    lcv.put("beforebighold",selcs.getInt(4));
                    lcv.put("beforemiddle",selcs.getInt(5));
                    lcv.put("beforemiddletitle",selcs.getString(6));
                    lcv.put("beforemiddlehold",selcs.getInt(7));
                    lcv.put("beforedate",selcs.getString(8));
                    lcv.put("beforecontent",selcs.getString(9));
                    lcv.put("beforehold",selcs.getInt(10));
                    lcv.put("beforeimportant",selcs.getInt(11));
                    lcv.put("beforememo",selcs.getString(12));
                    lcv.put("beforeproceed",selcs.getInt(13));
                    lcv.put("beforefin",selcs.getInt(14));
                }

                String[] params = {dlevel, "" + did};
                db.delete("ToDoData", "level=? and id=?", params);

                if(dlevel.equals("big")) { //大目標削除時中小目標のbigを削除
                    ContentValues cv = new ContentValues();
                    cv.put("big",0);
                    cv.put("bigtitle","");
                    cv.put("bighold",0);
                    cv.put("hold",1); //大目標削除時保留に移動
                    db.update("ToDoData",cv,"level=? and big=?",new String[]{"middle",""+did});
                    cv.put("middlehold",1); //小目標の上の中目標も保留に移動
                    db.update("ToDoData",cv,"level=? and big=?",new String[]{"small",""+did});
                }

                if(dlevel.equals("middle")){ //中目標削除時小目標のbig,middleを削除
                    ContentValues cv = new ContentValues();
                    cv.put("big",0);
                    cv.put("bigtitle","");
                    cv.put("bighold",0);
                    cv.put("middle",0);
                    cv.put("middletitle","");
                    cv.put("middlehold",0);
                    cv.put("hold",1); //中目標削除時保留に移動
                    db.update("ToDoData",cv,"middle=?",new String[]{""+did});
                }


                db.insert("LogData",null,lcv); //データ変更をログに追加

                Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                    String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                    db.execSQL(dsql);
                }

                Toast.makeText(activity, "データ削除しました", Toast.LENGTH_SHORT).show();

                //トランザクション成功
                success = true;
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                //トランザクションを終了
                db.endTransaction();

            }
        }
        return success;
    }

}
