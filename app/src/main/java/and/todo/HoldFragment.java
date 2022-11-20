package and.todo;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class HoldFragment extends Fragment implements DelDialogFragment.DelDialogListener{
    EditDatabaseHelper helper;
    ArrayList<String> bigTitle = new ArrayList<>(); //表示する大目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> bigData = new ArrayList<>(); //表示する大目標データを保管するための配列
    ArrayList<String> middleTitle = new ArrayList<>();//表示する中目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> middleData = new ArrayList<>();//表示する中目標データを保管するための配列
    ArrayList<String> smallTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> smallData = new ArrayList<>();//表示する小目標データを保管するための配列
    ArrayList<String> todoTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> todoData = new ArrayList<>(); //表示するやることリストデータを保管するための配列
    int bid=0,mid=0,sid=0,todoid=0;//項目選択時のID保管用変数
    ImageButton bedit,medit,sedit,todoedit;//各編集ボタン変数
    ImageButton bdl,mdl,sdl,tododl;//各削除ボタン変数
    DeleteData del = null; //データ削除用クラス
    int bigDel=0,middleDel=0,smallDel=0,scheDel=0,todoDel=0; //データ削除時に配列から消去するための配列インデックス変数
    private int did;
    private String dlevel;
    private boolean content = false;

    //データ渡し用のBundleデータ
    Bundle sendData;

    CustomSpinner bigTarget,middleTarget; //大中目標のスピナー変数
    ListView sList,scheList,todoList; //小目標スケジュールのリスト変数


    public static HoldFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        HoldFragment fragment = new HoldFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_hold,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Bundleデータ取得
        Bundle Data = getArguments();

        //データベースから各データを取得
        helper = new EditDatabaseHelper(requireActivity());

        //TOPに表示するデータの配列を取得
        getArrays();

        del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成

        //大目標にデータ設定
        bigTarget = (CustomSpinner) view.findViewById(R.id.bigTarget);
        ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle);
        bigTarget.setAdapter(bAdapter);
        bigTarget.setOnItemSelectedListener(new HoldFragment.SpinSelecter());
        if(bigData.size()>0) { //大目標が１つ以上あるときの初期設定
            bid = Integer.parseInt(bigData.get(0).get("id")); //大目標の初期位置ID
        }

        //中目標にデータ設定
        middleTarget = (CustomSpinner) view.findViewById(R.id.middleTarget);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
        middleTarget.setAdapter(mAdapter);
        middleTarget.setOnItemSelectedListener(new HoldFragment.SpinSelecter());
        if(middleData.size()>0) { //中目標が１つ以上あるときの初期設定
            mid = Integer.parseInt(middleData.get(0).get("id")); //中目標の初期位置ID
        }

        //小目標にデータ設定
        sList = view.findViewById(R.id.smallTargetList);
        sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));
        sList.setOnItemClickListener(new HoldFragment.ListSelecter());

        //やることリストにデータ設定
        todoList = view.findViewById(R.id.todoList);
        todoList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todoTitle));
        todoList.setOnItemClickListener(new HoldFragment.ListSelecter());


        CustomSpinner cspinner = view.findViewById(R.id.modeChange); //編集モード選択スピナー取得
        String[] spinnerItems = { "標準モード", "内容表示モード" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,
                spinnerItems);
        cspinner.setAdapter(adapter);
        cspinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                if(position==0){ //標準モード（選択しても表示されない）
                    content = false;
                }else{ //内容表示モード（項目の内容をトースト表示）
                    content = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //各編集ボタン要素を取得
        bedit = (ImageButton) view.findViewById(R.id.BeditButton);
        if(bigData.size()>0){ //大目標データがあれば編集ボタン有効化
            bedit.setEnabled(true);
        }else{ //大目標データがなければ編集ボタン無効化
            bedit.setEnabled(false);
        }
        medit = (ImageButton) view.findViewById(R.id.MeditButton);
        if(middleData.size()>0){ //中目標データがあれば編集ボタン有効化
            medit.setEnabled(true);
        }else{ //中目標データがあれば編集ボタン無効化
            medit.setEnabled(false);
        }
        sedit = (ImageButton) view.findViewById(R.id.SeditButton);
        sedit.setEnabled(false); //項目選択まで無効化
        todoedit = (ImageButton) view.findViewById(R.id.todoEdit);
        todoedit.setEnabled(false);
        //各編集ボタンのイベントリスナー
        bedit.setOnClickListener(new HoldFragment.editClicker());
        medit.setOnClickListener(new HoldFragment.editClicker());
        sedit.setOnClickListener(new HoldFragment.editClicker());
        todoedit.setOnClickListener(new HoldFragment.editClicker());

        //各削除ボタン要素を取得
        bdl = (ImageButton) view.findViewById(R.id.BdeleteButton);
        if(bigData.size()>0){ //大目標データがあれば削除ボタン有効化
            bdl.setEnabled(true);
        }else{ //大目標データがなければ削除ボタン無効化
            bdl.setEnabled(false);
        }
        mdl = (ImageButton) view.findViewById(R.id.MdeleteButton);
        if(middleData.size()>0){ // 中目標データがあれば削除ボタン有効化
            mdl.setEnabled(true);
        }else{ //中目標データがなければ削除ボタン無効化
            mdl.setEnabled(false);
        }
        sdl = (ImageButton) view.findViewById(R.id.SdeleteButton);
        sdl.setEnabled(false); //項目選択まで削除ボタン無効化
        tododl = (ImageButton) view.findViewById(R.id.todoDelete);
        tododl.setEnabled(false);
        //各削除ボタンのイベントリスナー
        bdl.setOnClickListener(new HoldFragment.editClicker());
        mdl.setOnClickListener(new HoldFragment.editClicker());
        sdl.setOnClickListener(new HoldFragment.editClicker());
        tododl.setOnClickListener(new HoldFragment.editClicker());


    }


    class editClicker implements View.OnClickListener { //編集ボタンクリックで編集画面へ飛ばす

        Bundle editData = new Bundle(); //データ送信用
        @Override
        public void onClick(View view) {
            if(view == bedit){ //大目標の編集ボタン
                editData.putString("level","big");
                editData.putInt("id",bid);
            }else if(view == medit){ //中目標の編集ボタン
                editData.putString("level","middle");
                editData.putInt("id",mid);
            }else if(view == sedit){ //小目標の編集ボタン
                editData.putString("level","small");
                editData.putInt("id",sid);
            }else if(view == todoedit){//やること編集ボタン
                editData.putString("level","todo");
                editData.putInt("id",todoid);
            }else if(view == bdl){//大目標の削除ボタン
                editData.putString("title",bigTitle.get(bigDel));
                editData.putString("level","big");
                dlevel = "big";
                did = bid;
            }else if(view == mdl){//中目標の削除ボタン
                editData.putString("title",middleTitle.get(middleDel));
                editData.putString("level","middle");
                dlevel = "middle";
                did = mid;
            }else if(view == sdl){//小目標の削除ボタン
                editData.putString("title",smallTitle.get(smallDel));
                editData.putString("level","small");
                dlevel = "small";
                did = sid;
            }else if(view == tododl){//やることリスト削除ボタン
                editData.putString("title",todoTitle.get(todoDel));
                editData.putString("level","todo");
                dlevel = "todo";
                did = todoid;
            }

            if(view == bedit || view == medit || view == sedit || view == todoedit ){
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

            }else if(view == bdl || view == mdl || view == sdl || view == tododl){
                //ToDo 削除確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                dialog.setTargetFragment(HoldFragment.this, 0);

                dialog.show(fragmentManager,"dialog_delete");

            }

        }
    }



    class SpinSelecter implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            //項目選択時のIDを取得
            if(adapterView == bigTarget){ //大目標選択時のID取得
                bid = Integer.parseInt( bigData.get(position).get("id"));
                bigDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),bigData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
            }else if(adapterView == middleTarget){ //中目標選択時のID取得
                mid = Integer.parseInt( middleData.get(position).get("id"));
                middleDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),middleData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class ListSelecter implements AdapterView.OnItemClickListener{ //クリックされた項目のIDを取得

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if(adapterView == sList){ //小目標選択時のID取得
                sid = Integer.parseInt( smallData.get(position).get("id"));
                smallDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),smallData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                sedit.setEnabled(true); //編集ボタン有効化
                sdl.setEnabled(true); //削除ボタン有効化
            }else if(adapterView == todoList){ //やることリスト選択時のID取得
                todoid = Integer.parseInt( todoData.get(position).get("id") );
                todoDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),todoData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                todoedit.setEnabled(true);
                tododl.setEnabled(true);
            }
        }
    }


    //データ削除確定時の処理
    @Override
    public void onDelDialogPositiveClick(DialogFragment dialog) {
        DeleteData del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成
        del.delete(dlevel,did);

        if(dlevel.equals("big")){ //大目標削除時の処理
            bigData.remove(bigDel);
            bigTitle.remove(bigDel);
            bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle));

            //中小目標データを取得し直す
            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //中目標を取得
                    String[] mcols = {"id","title","big","bigtitle","bighold","content","hold","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] mlevel = { "middle","0" };//中目標,未完了を抽出
                    Cursor mcs = db.query("ToDoData",mcols,"level=? and fin=?",mlevel,null,null,null,null);

                    middleData.clear(); //いったん配列を空にする
                    middleTitle.clear();
                    boolean next = mcs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        if(mcs.getInt(6)==1){ //保留中の中目標配列データ保存
                            HashMap<String,String> item = new HashMap<>();
                            item.put("id",""+mcs.getInt(0));
                            item.put("title",mcs.getString(1));
                            item.put("big",""+mcs.getInt(2));
                            item.put("bigtitle",mcs.getString(3));
                            item.put("bighold",""+mcs.getInt(4));
                            item.put("content",mcs.getString(5));
                            item.put("hold",""+mcs.getInt(6));
                            item.put("important",""+mcs.getInt(7));
                            item.put("memo",mcs.getString(8));
                            item.put("proceed",""+mcs.getInt(9));
                            item.put("fin",""+mcs.getInt(10));

                            if(mcs.getInt(2) !=0){ //大目標が存在するとき

                                if(Integer.parseInt(item.get("bighold"))==1){ //大目標保留中
                                    middleTitle.add(String.format("(%s(保))-%s(保)",item.get("bigtitle"),mcs.getString(1)));
                                }else{
                                    middleTitle.add(String.format("(%s)-%s(保)",item.get("bigtitle"),mcs.getString(1)));

                                }

                            }else{ //大目標未設定時
                                middleTitle.add(String.format("大目標未設定-%s(保)",mcs.getString(1)));
                            }

                            middleData.add(item); //中目標データ配列に追加
                        }
                        next = mcs.moveToNext();
                    }


                    //小目標を取得
                    String[] scols = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] slevel = { "small","1","0" };//小目標,保留中,未完了を抽出
                    Cursor scs = db.query("ToDoData",scols,"level=? and hold=? and fin=?",slevel,null,null,null,null);

                    smallData.clear(); //いったん配列を空にする
                    smallTitle.clear();
                    next = scs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+scs.getInt(0));
                        item.put("title",scs.getString(1));
                        item.put("big",""+scs.getInt(2));
                        item.put("bigtitle",scs.getString(3));
                        item.put("bighold",""+scs.getInt(4));
                        item.put("middle",""+scs.getInt(5));
                        item.put("middletitle",scs.getString(6));
                        item.put("middlehold",""+scs.getInt(7));
                        item.put("content",scs.getString(8));
                        item.put("important",scs.getString(9));
                        item.put("memo",scs.getString(10));
                        item.put("proceed",scs.getString(11));
                        item.put("fin",scs.getString(12));
                        //todo
                        if(scs.getInt(5) !=0){ //中目標が存在するとき
                            if(scs.getInt(2) != 0){ //大目標が存在するとき
                                if(scs.getInt(4)==1){//大目標保留中
                                    if(scs.getInt(7)==1){ //中目標保留中
                                        smallTitle.add(String.format("(%s(保))-(%s(保))-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }else{//中目標保留無し
                                        smallTitle.add(String.format("(%s(保))-(%s)-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }
                                }else{//大目標保留無し
                                    if(scs.getInt(7)==1){ //中目標保留中
                                        smallTitle.add(String.format("(%s)-(%s(保))-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }else{//中目標保留無し
                                        smallTitle.add(String.format("(%s)-(%s)-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }
                                }
                            }else { //大目標が未設定
                                if (scs.getInt(7) == 1) { //中目標保留中
                                    smallTitle.add(String.format("大目標未設定-(%s(保))-%s(保)", item.get("middletitle"), item.get("title")));
                                } else { //中目標保留無し
                                    smallTitle.add(String.format("大目標未設定-(%s)-%s(保)", item.get("middletitle"), item.get("title")));
                                }
                            }
                        }else{ //中目標未設定時
                            smallTitle.add(String.format("大目標未設定-中目標未設定-%s(保)",item.get("title")));
                        }
                        smallData.add(item); //中目標データ配列に追加
                        next = scs.moveToNext();
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
            //中小目標をリストに再設定
            middleTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle));
            sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));

            if(bigData.size()>0){ //大目標が存在するとき
                bigTarget.setSelection(0);
                bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
            }else{//大目標が存在しないとき
                bid = 0;
                bedit.setEnabled(false); //編集ボタン無効化
                bdl.setEnabled(false); //削除ボタン無効化
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
            }
            bigDel = 0;
        }else if(dlevel.equals("middle")){ //中目標削除時の処理
            middleData.remove(middleDel);
            middleTitle.remove(middleDel);
            middleTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle));

            //小目標データを取得し直す
            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //小目標を取得
                    String[] scols = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] slevel = { "small","1","0" };//小目標のみ,保留中,未完了を抽出
                    Cursor scs = db.query("ToDoData",scols,"level=? and hold=? and fin=?",slevel,null,null,null,null);

                    smallData.clear(); //いったん配列を空にする
                    smallTitle.clear();
                    boolean next = scs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+scs.getInt(0));
                        item.put("title",scs.getString(1));
                        item.put("big",""+scs.getInt(2));
                        item.put("bigtitle",scs.getString(3));
                        item.put("bighold",""+scs.getInt(4));
                        item.put("middle",""+scs.getInt(5));
                        item.put("middletitle",scs.getString(6));
                        item.put("middlehold",""+scs.getInt(7));
                        item.put("content",scs.getString(8));
                        item.put("important",scs.getString(9));
                        item.put("memo",scs.getString(10));
                        item.put("proceed",scs.getString(11));
                        item.put("fin",scs.getString(12));
                        //todo
                        if(scs.getInt(5) !=0){ //中目標が存在するとき
                            if(scs.getInt(2) != 0){ //大目標が存在するとき
                                if(scs.getInt(4)==1){//大目標保留中
                                    if(scs.getInt(7)==1){ //中目標保留中
                                        smallTitle.add(String.format("(%s(保))-(%s(保))-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }else{//中目標保留無し
                                        smallTitle.add(String.format("(%s(保))-(%s)-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }
                                }else{//大目標保留無し
                                    if(scs.getInt(7)==1){ //中目標保留中
                                        smallTitle.add(String.format("(%s)-(%s(保))-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }else{//中目標保留無し
                                        smallTitle.add(String.format("(%s)-(%s)-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                    }
                                }
                            }else { //大目標が未設定
                                if (scs.getInt(7) == 1) { //中目標保留中
                                    smallTitle.add(String.format("大目標未設定-(%s(保))-%s(保)", item.get("middletitle"), item.get("title")));
                                } else { //中目標保留無し
                                    smallTitle.add(String.format("大目標未設定-(%s)-%s(保)", item.get("middletitle"), item.get("title")));
                                }
                            }
                        }else{ //中目標未設定時
                            smallTitle.add(String.format("大目標未設定-中目標未設定-%s(保)",item.get("title")));
                        }
                        smallData.add(item); //中目標データ配列に追加
                        next = scs.moveToNext();
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
            sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));

            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
            }
            middleDel = 0;
        }else if(dlevel.equals("small")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);
            sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
        }else if(dlevel.equals("todo")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);
            todoList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todoTitle));
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todoDel=0; //削除するデータのインデックスリセット
        }

    }
    //データ削除キャンセル時
    @Override
    public void onDelDialogNegativeClick(DialogFragment dialog) {
    }
    //データ削除スルー
    @Override
    public void onDelDialogNeutralClick(DialogFragment dialog) {
    }



    void getArrays(){ //データベースから取得してデータ配列に挿入する

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{
                //大目標を取得

                String[] bcols = {"id","title","content","hold","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] blevel = { "big","0" };//大目標を抽出
                Cursor bcs = db.query("ToDoData",bcols,"level=? and fin=?",blevel,null,null,null,null);

                bigData.clear(); //いったん配列を空にする
                bigTitle.clear();

                boolean next = bcs.moveToFirst();//カーソルの先頭に移動
                while(next){ //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                    if(bcs.getInt(3)==1){ //保留中データを配列に保管
                        bigTitle.add(bcs.getString(1)+"(保)");//大目標のタイトル
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+bcs.getInt(0));
                        item.put("content",bcs.getString(2));
                        item.put("hold",""+bcs.getInt(3));
                        item.put("important",""+bcs.getInt(4));
                        item.put("memo",bcs.getString(5));
                        item.put("proceed",""+bcs.getInt(6));
                        item.put("fin",""+bcs.getInt(7));
                        bigData.add(item);
                    }
                    next = bcs.moveToNext();
                }

                //中目標を取得
                String[] mcols = {"id","title","big","bigtitle","bighold","content","hold","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] mlevel = { "middle","0" };//中目標のみを抽出
                Cursor mcs = db.query("ToDoData",mcols,"level=? and fin=?",mlevel,null,null,null,null);

                middleData.clear(); //いったん配列を空にする
                middleTitle.clear();
                next = mcs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    if(mcs.getInt(6)==1){ //保留中の中目標配列データ保存
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+mcs.getInt(0));
                        item.put("title",mcs.getString(1));
                        item.put("big",""+mcs.getInt(2));
                        item.put("bigtitle",mcs.getString(3));
                        item.put("bighold",""+mcs.getInt(4));
                        item.put("content",mcs.getString(5));
                        item.put("hold",""+mcs.getInt(6));
                        item.put("important",""+mcs.getInt(7));
                        item.put("memo",mcs.getString(8));
                        item.put("proceed",""+mcs.getInt(9));
                        item.put("fin",""+mcs.getInt(10));

                        if(mcs.getInt(2) !=0){ //大目標が存在するとき

                            if(Integer.parseInt(item.get("bighold"))==1){ //大目標保留中
                                middleTitle.add(String.format("(%s(保))-%s(保)",item.get("bigtitle"),mcs.getString(1)));
                            }else{
                                middleTitle.add(String.format("(%s)-%s(保)",item.get("bigtitle"),mcs.getString(1)));

                            }

                        }else{ //大目標未設定時
                            middleTitle.add(String.format("大目標未設定-%s(保)",mcs.getString(1)));
                        }

                        middleData.add(item); //中目標データ配列に追加
                    }
                    next = mcs.moveToNext();
                }

                //小目標を取得
                String[] scols = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] slevel = { "small","1","0" };//小目標のみを抽出
                Cursor scs = db.query("ToDoData",scols,"level=? and hold=? and fin=?",slevel,null,null,null,null);

                smallData.clear(); //いったん配列を空にする
                smallTitle.clear();
                next = scs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+scs.getInt(0));
                    item.put("title",scs.getString(1));
                    item.put("big",""+scs.getInt(2));
                    item.put("bigtitle",scs.getString(3));
                    item.put("bighold",""+scs.getInt(4));
                    item.put("middle",""+scs.getInt(5));
                    item.put("middletitle",scs.getString(6));
                    item.put("middlehold",""+scs.getInt(7));
                    item.put("content",scs.getString(8));
                    item.put("important",scs.getString(9));
                    item.put("memo",scs.getString(10));
                    item.put("proceed",scs.getString(11));
                    item.put("fin",scs.getString(12));
                    //todo
                    if(scs.getInt(5) !=0){ //中目標が存在するとき
                        if(scs.getInt(2) != 0){ //大目標が存在するとき
                            if(scs.getInt(4)==1){//大目標保留中
                                if(scs.getInt(7)==1){ //中目標保留中
                                    smallTitle.add(String.format("(%s(保))-(%s(保))-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                }else{//中目標保留無し
                                    smallTitle.add(String.format("(%s(保))-(%s)-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                }
                            }else{//大目標保留無し
                                if(scs.getInt(7)==1){ //中目標保留中
                                    smallTitle.add(String.format("(%s)-(%s(保))-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                }else{//中目標保留無し
                                    smallTitle.add(String.format("(%s)-(%s)-%s(保)",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                                }
                            }
                        }else { //大目標が未設定
                            if (scs.getInt(7) == 1) { //中目標保留中
                                smallTitle.add(String.format("大目標未設定-(%s(保))-%s(保)", item.get("middletitle"), item.get("title")));
                            } else { //中目標保留無し
                                smallTitle.add(String.format("大目標未設定-(%s)-%s(保)", item.get("middletitle"), item.get("title")));
                            }
                        }
                    }else{ //中目標未設定時
                        smallTitle.add(String.format("大目標未設定-中目標未設定-%s(保)",item.get("title")));
                    }
                    smallData.add(item); //中目標データ配列に追加
                    next = scs.moveToNext();
                }

                //やることリストを取得
                String[] todocols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] todolevel = { "todo","1","0" };//やることリストのみを抽出
                Cursor todocs = db.query("ToDoData",todocols,"level=? and hold=? and fin=?",todolevel,null,null,null,null);

                todoData.clear(); //いったん配列を空にする
                todoTitle.clear();
                next = todocs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+todocs.getInt(0));
                    item.put("title",todocs.getString(1));
                    item.put("content",todocs.getString(2));
                    item.put("important",todocs.getString(3));
                    item.put("memo",todocs.getString(4));
                    item.put("proceed",todocs.getString(5));
                    item.put("fin",todocs.getString(6));
                    todoData.add(item); //中目標データ配列に追加
                    todoTitle.add(String.format("%s(保)",item.get("title")));
                    next = todocs.moveToNext();
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
