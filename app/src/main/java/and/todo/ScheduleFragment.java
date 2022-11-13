package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ScheduleFragment extends Fragment {
    private ArrayList<HashMap<String,String>> scheData = new ArrayList<>();
    private ArrayList<String> scheTitle = new ArrayList<>();
    private EditDatabaseHelper helper;
    private ListView scheList;
    private int selectId=0; //選択したID
    private ImageButton del,edit;
    DeleteData dl = null; //データ削除用クラス
    int delnum = 0;//データ削除時配列からも削除するためのインデックス変数

    public static ScheduleFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        ScheduleFragment fragment = new ScheduleFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    @Nullable
    @Override //フラグメントのviewを作成する処理
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_sche,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helper = new EditDatabaseHelper(requireActivity());

        getSchedule(); //スケジュールデータを取得する

        dl = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成

        scheList = view.findViewById(R.id.scheduleList);
        scheList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,scheTitle));
        scheList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectId = Integer.parseInt( scheData.get(position).get("id"));
                del.setEnabled(true);
                edit.setEnabled(true);
                delnum = position;
            }
        });

        del = view.findViewById(R.id.delButton); //削除ボタンを取得
        del.setEnabled(false);
        del.setOnClickListener(new editClicker());
        edit = view.findViewById(R.id.editButton); //編集ボタンを取得
        edit.setEnabled(false);
        edit.setOnClickListener(new editClicker());


    }

    class editClicker implements View.OnClickListener { //編集ボタンクリックで編集画面へ飛ばす
        private String dlevel = ""; //削除データの項目変数
        private int did = 0; //削除データのID変数

        Bundle editData = new Bundle(); //データ送信用
        @Override
        public void onClick(View view) {
            if(view == edit){ //スケジュール編集ボタン
                editData.putString("level","schedule");
                editData.putInt("id",selectId);
            }

            if(view == edit){
                //編集画面へ飛ばす
                Log.e("ID",""+selectId);
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == del){
                //データベースから削除
                boolean judge = dl.delete("schedule",selectId);

                if(judge){ //データ削除成功時、配列からも消す
                        scheData.remove(delnum);
                        scheTitle.remove(delnum);
                        scheList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,scheTitle));
                        del.setEnabled(false);//ボタンを無効化
                        edit.setEnabled(false);

                }
                delnum=0; //削除するデータのインデックスリセット
            }

        }
    }


    void getSchedule(){ //データベースから取得してデータ配列に挿入する

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{

                //スケジュールを取得
                String[] schecols = {"id","title","date","content","important","memo","proceed","fin","hold"};//SQLデータから取得する列
                String[] schelevel = { "schedule" };//中目標,保留なしのデータを抽出
                Cursor schecs = db.query("ToDoData",schecols,"level=?",schelevel,null,null,"date desc",null);

                scheData.clear(); //いったん配列を空にする
                scheTitle.clear();
                boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+schecs.getInt(0));
                    item.put("title",schecs.getString(1));
                    item.put("date",""+schecs.getString(2));
                    item.put("content",schecs.getString(3));
                    item.put("important",schecs.getString(4));
                    item.put("memo",schecs.getString(5));
                    item.put("proceed",schecs.getString(6));
                    item.put("fin",schecs.getString(7));
                    item.put("hold",""+schecs.getInt(8));
                    scheData.add(item); //スケジュールデータ配列に追加
                    if(Integer.parseInt(item.get("hold"))==1){
                        scheTitle.add(String.format("%s[%s](保)",item.get("title"),item.get("date")));
                    }else{
                        scheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                    }
                    next = schecs.moveToNext();
                }


                //トランザクション成功
                db.setTransactionSuccessful();
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                //トランザクションを終了
                db.endTransaction();
            }
        }
    }

}
