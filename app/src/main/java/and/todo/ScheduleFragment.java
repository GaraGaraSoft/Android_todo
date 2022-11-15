package and.todo;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ScheduleFragment extends Fragment {
    private ArrayList<HashMap<String,String>> futureScheData = new ArrayList<>();//今後の予定
    private ArrayList<String> futureScheTitle = new ArrayList<>();
    private ArrayList<HashMap<String,String>> todayScheData = new ArrayList<>();//本日の予定
    private ArrayList<String> todayScheTitle = new ArrayList<>();
    private ArrayList<HashMap<String,String>> pastScheData = new ArrayList<>();//過去の予定
    private ArrayList<String> pastScheTitle = new ArrayList<>();
    private EditDatabaseHelper helper;
    private ListView futureList,todayList,pastList;
    private int futureid=0,todayid=0,pastid=0; //選択したID
    private ImageButton futureEdit,futureDel,todayEdit,todayDel,pastEdit,pastDel;
    DeleteData dl = null; //データ削除用クラス
    int futureDelIndex=0,todayDelIndex=0,pastDelIndex=0;//データ削除時配列からも削除するためのインデックス変数

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

        futureList = view.findViewById(R.id.futureScheduleList); //今後のスケジュールリスト設定
        futureList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,futureScheTitle));
        futureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                futureid = Integer.parseInt( futureScheData.get(position).get("id"));
                futureEdit.setEnabled(true);
                futureDel.setEnabled(true);
                futureDelIndex = position;
            }
        });

        //ToDo その日の予定数によってスケジュールの大きさ変更

        if(todayScheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.todayListLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_today_schedule, layout);
        }else{
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.todayListLayout);
            //スケジュールにデータ設定
            todayList = view.findViewById(R.id.todayScheduleList);
            todayList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todayScheTitle));
            todayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    todayid = Integer.parseInt( todayScheData.get(position).get("id"));
                    todayEdit.setEnabled(true);
                    todayDel.setEnabled(true);
                    todayDelIndex = position;
                }
            });


            if(todayScheData.size()==1){
                layout.setMinHeight(200);
            }else if(todayScheData.size()==2){
                layout.setMinHeight(350);
            }else{
                layout.setMinHeight(500);
            }

            todayDel = view.findViewById(R.id.todayDelButton); //今日のスケジュール用削除ボタンを取得
            todayDel.setEnabled(false);
            todayDel.setOnClickListener(new editClicker());
            todayEdit = view.findViewById(R.id.todayEditButton); //今日のスケジュール用編集ボタンを取得
            todayEdit.setEnabled(false);
            todayEdit.setOnClickListener(new editClicker());

        }

        pastList = view.findViewById(R.id.pastScheduleList);//過去のスケジュールリスト設定
        pastList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,pastScheTitle));
        pastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                pastid = Integer.parseInt( pastScheData.get(position).get("id"));
                pastEdit.setEnabled(true);
                pastDel.setEnabled(true);
                pastDelIndex = position;
            }
        });

        futureDel = view.findViewById(R.id.futureDelButton); //今後のスケジュール用削除ボタンを取得
        futureDel.setEnabled(false);//初期はボタン無効
        futureDel.setOnClickListener(new editClicker());
        futureEdit = view.findViewById(R.id.futureEditButton); //今後のスケジュール用編集ボタンを取得
        futureEdit.setEnabled(false);//初期はボタン無効
        futureEdit.setOnClickListener(new editClicker());

        pastDel = view.findViewById(R.id.pastDelButton); //過去のスケジュール用削除ボタンを取得
        pastDel.setEnabled(false);
        pastDel.setOnClickListener(new editClicker());
        pastEdit = view.findViewById(R.id.pastEditButton); //過去のスケジュール用編集ボタンを取得
        pastEdit.setEnabled(false);
        pastEdit.setOnClickListener(new editClicker());
    }

    class editClicker implements View.OnClickListener { //編集ボタンクリックで編集画面へ飛ばす
        private String dlevel = ""; //削除データの項目変数
        private int did = 0; //削除データのID変数

        Bundle editData = new Bundle(); //データ送信用
        @Override
        public void onClick(View view) {
            if(view == futureEdit ){ //スケジュール編集ボタン
                editData.putString("level","schedule");
                editData.putInt("id",futureid);

                //編集画面へ飛ばす
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == todayEdit){
                editData.putString("level","schedule");
                editData.putInt("id",todayid);

                //編集画面へ飛ばす
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == pastEdit){
                editData.putString("level","schedule");
                editData.putInt("id",pastid);

                //編集画面へ飛ばす
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == futureDel){//今後のスケジュールデータ削除
                //データベースから削除
                boolean judge = dl.delete("schedule",futureid);

                if(judge){ //データ削除成功時、配列からも消す
                        futureScheData.remove(futureDelIndex);
                        futureScheTitle.remove(futureDelIndex);
                        futureList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,futureScheTitle));
                        futureEdit.setEnabled(false);//ボタンを無効化
                        futureDel.setEnabled(false);

                }
                futureDelIndex=0; //削除するデータのインデックスリセット
            }else if(view == todayDel){//本日のスケジュールデータ削除
                //データベースから削除
                boolean judge = dl.delete("schedule",todayid);

                if(judge){ //データ削除成功時、配列からも消す
                    todayScheData.remove(todayDelIndex);
                    todayScheTitle.remove(todayDelIndex);
                    todayList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todayScheTitle));
                    todayEdit.setEnabled(false);//ボタンを無効化
                    todayDel.setEnabled(false);

                }
                todayDelIndex=0; //削除するデータのインデックスリセット
            }else if(view == pastDel){//過去のスケジュールデータ削除
                //データベースから削除
                boolean judge = dl.delete("schedule",pastid);

                if(judge){ //データ削除成功時、配列からも消す
                    pastScheData.remove(pastDelIndex);
                    pastScheTitle.remove(pastDelIndex);
                    pastList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,pastScheTitle));
                    pastEdit.setEnabled(false);//ボタンを無効化
                    pastDel.setEnabled(false);

                }
                pastDelIndex=0; //削除するデータのインデックスリセット
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
                String[] schelevel = { "schedule" };//スケジュールデータを抽出
                //スケジュールをもっとも先の日付から順に取得
                Cursor schecs = db.query("ToDoData",schecols,"level=?",schelevel,null,null,"date desc",null);

                //各スケジュール配列を空にする
                futureScheData.clear();
                futureScheTitle.clear();
                todayScheData.clear();
                todayScheTitle.clear();
                pastScheData.clear();
                pastScheTitle.clear();
                int scheduleChange = 0; //スケジュール配列切替え用変数

                //Calendarクラスのオブジェクトを生成する
                Calendar cl = Calendar.getInstance();
                //本日の日付データを取得
                String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                while(next){

                    int schedulenum = schecs.getString(2).compareTo(today);
                    if(schedulenum>0){//今後のスケジュール
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        item.put("hold",""+schecs.getInt(8));
                        futureScheData.add(item); //スケジュールデータ配列に追加
                        if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                            futureScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                        }else{//
                            futureScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        }

                    }else if(schedulenum==0){//当日のスケジュール
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        item.put("hold",""+schecs.getInt(8));
                        todayScheData.add(item); //スケジュールデータ配列に追加
                        if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                            todayScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                        }else{
                            todayScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        }

                    }else {//過去のスケジュール
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        item.put("hold",""+schecs.getInt(8));
                        pastScheData.add(item); //スケジュールデータ配列に追加
                        if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                            pastScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                        }else{
                            pastScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        }

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
