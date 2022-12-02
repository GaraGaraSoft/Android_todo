package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Locale;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class EditFragment extends Fragment implements DateDialogFragment.DateDialogListener {
    private String level;//編集する目標ランク
    private int id = 0;//編集するID
    private String title=""; //目標タイトル
    private String content=""; //目標内容
    private Spinner spinLevel; //目標Spinner
    private int titleNum; //タイトル文字数
    private Button editBtn; //編集完了ボタン
    private int big=0; //上目標
    private String bigtitle=""; //ToDo 上目標タイトル
    private int bighold=0;
    private int middle=0; //中目標
    private String middletitle="";//ToDo 中目標タイトル
    private int middlehold=0;
    private String date = ""; //日付
    private int hold = 0; //保留判定(0は保留なし）
    private int important = 0; //優先度
    private String memo = ""; //進捗内容
    private int proceed = 0; //進捗度合い
    private int fin = 0; //完了判定
    HashMap<String,String> beforeData = new HashMap<>(); //編集前のデータを保管するMAP
    ArrayList<String> bigTitle = new ArrayList<>(); //編集画面で表示する大目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> bigData = new ArrayList<>(); //編集画面で表示する大目標のIDを保管するための配列
    ArrayList<String> middleTitle = new ArrayList<>();//編集画面で表示する中目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> middleData = new ArrayList<>();//編集画面で表示する中目標データを保管するための配列
    private EditDatabaseHelper helper;
    private ContentValues lcv;
    private boolean dateok = false;
    private EditText editTitle;
    //データ渡し用のBundleデータ
    Bundle sendData;

    EditText editdate = null;
    TextView txtCheck = null;



    public static EditFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        EditFragment efragment = new EditFragment ();

        //Bundleで送られたデータを設定
        efragment.setArguments(Data);

        return efragment;
    }

    @Nullable
    @Override //フラグメントのviewを作成する処理
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_edit,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Bundleデータを取得
        Bundle Data = getArguments();
        if(Data != null ){ //Dataにデータがあるときの処理
            level = Data.getString("level");
            id = Data.getInt("id");

        }

        View decor = requireActivity().getWindow().getDecorView();//バーを含めたぜびゅー全体

        ConstraintLayout top = view.findViewById(R.id.editConstraint);
        top.setOnClickListener(v-> {

            if(decor.getSystemUiVisibility() == 0) {//アクションバーに表示されているとき

                decor.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                |View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }else{

                decor.setSystemUiVisibility(0);
            }

        });


        //データベースヘルパー準備
        helper = new EditDatabaseHelper(requireActivity());


        lcv = new ContentValues();
        lcv.put("beforelevel",level);

        //データベースから大目標、中目標、選択IDデータを取得
        getTarget();


        //目標選択スピナー取得
        spinLevel = (Spinner) view.findViewById(R.id.spinLevel);

        //目標タイトル取得
        editTitle = view.findViewById(R.id.EditTitle);
        editTitle.setText(title);
        editTitle.addTextChangedListener(new EditTextWatcher(editTitle));

        //目標内容取得
        EditText editContent = view.findViewById(R.id.EditContent);
        editContent.setText(content);


        //保留チェックボックス取得
        CheckBox checkhold = view.findViewById(R.id.checkHold);
        if(hold==1){ //ホールド状態のデータ編集時はチェックを押しておく
            checkhold.setChecked(true);
        }
        checkhold.setOnCheckedChangeListener((CompoundButton buttonview, boolean ischecked)->{
            if(ischecked==true){ //チェック時には保留状態（1）とする
                hold = 1;
            }else{
                hold = 0;
            }
        });


        //編集完了ボタン取得
        editBtn = (Button) view.findViewById(R.id.EditDone);

        if(title.length()>0 && !level.equals("")){ //タイトル入力、目標選択時にボタン有効化
            editBtn.setEnabled(true);
        }else{
            editBtn.setEnabled(false);
        }


        editBtn.setOnClickListener(v->{ //ボタンクリック時にはデータベース登録処理



            try(SQLiteDatabase db = helper.getWritableDatabase()) {
                //トランザクション開始
                db.beginTransaction();
                try {

                    ContentValues cv = new ContentValues(); //更新用ContentValues
                    ContentValues bcv = new ContentValues(); //大目標変更時の中小変更用ContentValues
                    ContentValues mcv = new ContentValues(); //中目標変更時の小目標変更用ContentValues
                    lcv.put("ope","update");
                    cv.put("id",id);
                    lcv.put("id",id);
                    cv.put("title", editTitle.getText().toString());
                    lcv.put("aftertitle",editTitle.getText().toString());
                    cv.put("content", editContent.getText().toString());
                    lcv.put("aftercontent",editContent.getText().toString());
                    cv.put("level", level);
                    lcv.put("afterlevel",level);
                    if (level.equals("middle") || level.equals("small")) {
                        cv.put("big", big);
                        cv.put("bigtitle",bigtitle);
                        cv.put("bighold",bighold);
                        lcv.put("afterbig",big);
                        lcv.put("afterbigtitle",bigtitle);
                        lcv.put("afterbighold",bighold);
                    } else {
                        cv.put("big", 0);
                        cv.put("bigtitle","");
                        cv.put("bighold",0);
                        lcv.put("afterbig",0);
                        lcv.put("afterbigtitle","");
                        lcv.put("afterbighold",0);
                    }
                    if (level.equals("small")) {
                        cv.put("middle", middle);
                        cv.put("middletitle",middletitle);
                        cv.put("middlehold",middlehold);
                        lcv.put("aftermiddle",middle);
                        lcv.put("aftermiddletitle",middletitle);
                        lcv.put("aftermiddlehold",middlehold);
                    } else {
                        cv.put("middle", 0);
                        cv.put("middletitle","");
                        cv.put("middlehold",0);
                        lcv.put("aftermiddle",0);
                        lcv.put("aftermiddletitle","");
                        lcv.put("aftermiddlehold",0);
                    }
                    if (level.equals("schedule")) {
                        cv.put("date", date);
                        lcv.put("afterdate",date);
                    } else {
                        cv.put("date", "");
                        lcv.put("afterdate","");
                    }

                    if(level.equals("middle")) {
                        if(hold == 0) { //非保留状態のとき
                            if(bighold==1){//大目標が保留状態のとき下の中目標データも保留にする
                                cv.put("hold",1);
                                lcv.put("afterhold",1);
                            }else{//大目標が非保留状態のときは下のデータは入力されたデータのまま
                                cv.put("hold",hold);
                                lcv.put("afterhold",hold);
                            }
                        }else{
                            cv.put("hold",hold);
                            lcv.put("afterhold",hold);
                        }
                    }else if(level.equals("small")){
                        if(hold == 0) { //非保留状態のとき
                            if(middlehold==1){//中目標が保留状態のとき下の小目標も保留にする
                                cv.put("hold",1);
                                lcv.put("afterhold",1);
                            }else{
                                cv.put("hold",hold);
                                lcv.put("afterhold",hold);
                            }

                        }else{
                            cv.put("hold",hold);
                            lcv.put("afterhold",hold);
                        }
                    }else{
                        cv.put("hold", hold);
                        lcv.put("afterhold",hold);
                    }

                    cv.put("important", important);
                    lcv.put("afterimportant",important);
                    cv.put("memo", memo);
                    lcv.put("aftermemo",memo);
                    cv.put("proceed", proceed);
                    lcv.put("afterproceed",proceed);
                    cv.put("fin", fin);
                    lcv.put("afterfin",fin);

                    if(level.equals("big")){
                        bcv.put("big",id);
                        bcv.put("bigtitle",editTitle.getText().toString());
                        bcv.put("bighold",hold);
                        if(hold==1){ //大目標を保留にしたとき中小目標も保留状態にする
                            bcv.put("hold",1);
                        }
                    }else if(level.equals("middle")){
                        mcv.put("big",big);
                        mcv.put("bigtitle",bigtitle);
                        mcv.put("bighold",bighold);
                        mcv.put("middle",id);
                        mcv.put("middletitle",editTitle.getText().toString());
                        mcv.put("middlehold",hold);
                        if(hold==1){//中目標を保留にしたとき小目標も保留状態にする
                            mcv.put("hold",1);
                        }else{ //中目標非保留状態でも大目標が保留状態ならば状態変わらず
                            if(bighold==1){
                                mcv.put("middlehold",1);
                            }
                        }
                    }

                    db.insertWithOnConflict("ToDoData", null, cv,SQLiteDatabase.CONFLICT_REPLACE);

                    if(level.equals("big")){ //大目標変更時その下の中小目標の大目標を変更
                        if(hold==1){//大目標保留時小目標の上の中目標も保留に
                            bcv.put("middlehold",hold);
                            db.update("ToDoData",bcv,"level=? and big=?",new String[]{"small",""+id});
                        }else{//大目標が非保留時中小目標の大目標の保留状態も非保留とする
                            bcv.put("bighold",hold);
                            db.update("ToDoData",bcv,"level=? and big=?",new String[]{"small",""+id});
                        }
                        db.update("ToDoData",bcv,"level=? and big=?",new String[]{"middle",""+id});
                    }else if(level.equals("middle")){ //中目標変更時その下の小目標の中目標を変更
                        db.update("ToDoData",mcv,"middle=?",new String[]{""+id});
                    }

                    db.insert("LogData",null,lcv); //データ変更をログに追加

                    Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                    if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                        String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                        db.execSQL(dsql);
                    }

                    Toast.makeText(requireActivity(), "データ更新に成功しました", Toast.LENGTH_SHORT).show();

                    //トランザクション成功
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    //トランザクションを終了
                    db.endTransaction();

                    //TopFragmentに移動
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager.popBackStack();
                }

            }

        });



        if(level.equals("big")){
            spinLevel.setEnabled(false);
        }else if(level.equals("middle")){ //大目標のSpinを出して選択しておく
            spinLevel.setSelection(1);
            spinLevel.setEnabled(false);
/*
            //Spinlevelを変更する
            ConstraintLayout cl = (ConstraintLayout) view.findViewById(R.id.levelSelect);
            cl.removeAllViews();
            getLayoutInflater().inflate();*/

            //大目標の選択項目を表示
            LinearLayout layout;
            layout = (LinearLayout) view.findViewById(R.id.editItems);
            // 内容を全部消す
            layout.removeAllViews();
            // レイアウトをR.layout.bigtargetに変更する
            getLayoutInflater().inflate(R.layout.bigtarget, layout);

            //中目標を選択時、大目標の選択肢を出す
            Spinner bSpin = (Spinner) view.findViewById(R.id.bSpin);
            ArrayAdapter<String> bigAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle);
            bSpin.setAdapter(bigAdapter);
            middle=0; //中目標を初期化
            middletitle = "";
            middlehold=0;
            date = ""; //日付を初期化
            for(int i=0;i<bigData.size();i++){ //大目標データから元々の選択を探し初期値に設定
                if(big == Integer.parseInt(bigData.get(i).get("id"))){
                    bSpin.setSelection(i);
                    big = Integer.parseInt(bigData.get(i).get("id")); //選択された大目標情報を設定
                    bigtitle = bigData.get(i).get("title"); //選択された大目標情報を設定
                    bighold = Integer.parseInt(bigData.get(i).get("hold")); //選択された大目標情報の保留状態
                    break;
                }
            }

            bSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    big = Integer.parseInt(bigData.get(i).get("id"));
                    bigtitle = bigData.get(i).get("title");
                    bighold = Integer.parseInt(bigData.get(i).get("hold"));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }else if(level.equals("small")){ //中目標のSpinを出して選択しておく
            spinLevel.setSelection(2);
            spinLevel.setEnabled(false);

            //大、中目標の選択項目を表示
            LinearLayout layout;
            layout = (LinearLayout) view.findViewById(R.id.editItems);
            // 内容を全部消す
            layout.removeAllViews();
            // レイアウトをR.layout.sampleに変更する
            getLayoutInflater().inflate(R.layout.middletarget, layout);

            //小目標を選択時、大中目標の選択肢を出す
            Spinner mSpin = (Spinner) view.findViewById(R.id.mSpin);
            ArrayAdapter<String> middleAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
            mSpin.setAdapter(middleAdapter);
            date = ""; //日付を初期化
            for(int i=0;i<middleData.size();i++){ //中目標データから元々の選択を探し初期値に設定
                if(middle == Integer.parseInt(middleData.get(i).get("id")) ){
                    mSpin.setSelection(i);
                    middle = Integer.parseInt(middleData.get(i).get("id")); //選択中の中目標を設定
                    middletitle = middleData.get(i).get("title"); //選択中の中目標のタイトル設定
                    middlehold = Integer.parseInt(middleData.get(i).get("hold"));
                    big = Integer.parseInt(middleData.get(i).get("big"));//選択中の大目標を設定
                    bigtitle = middleData.get(i).get("bigtitle"); //選択中の大目標のタイトル設定
                    bighold = Integer.parseInt(middleData.get(i).get("bighold")); //選択中の大目標の保留状態設定
                    break;
                }
            }


            mSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    big = Integer.parseInt(middleData.get(i).get("big"));
                    bigtitle = middleData.get(i).get("bigtitle");
                    bighold = Integer.parseInt(middleData.get(i).get("bighold"));
                    middle = Integer.parseInt(middleData.get(i).get("id"));
                    middletitle = middleData.get(i).get("title");
                    middlehold = Integer.parseInt(middleData.get(i).get("hold"));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


        }else if(level.equals("schedule")){ //スケジュールデータとして入力しておく
            spinLevel.setSelection(3);
            spinLevel.setEnabled(false);

            //スケジュールの選択項目を表示
            LinearLayout layout;
            layout = (LinearLayout) view.findViewById(R.id.editItems);
            // 内容を全部消す
            layout.removeAllViews();

            getLayoutInflater().inflate(R.layout.date,layout);

            middle = 0;//中目標を初期化
            middletitle = "";
            middlehold = 0;
            big = 0;//大目標を初期化
            bigtitle = "";
            bighold = 0;
            dateok = true;

            //日付データ取得
            editdate = requireActivity().findViewById(R.id.editDate);
            editdate.setText(date);
            editdate.addTextChangedListener(new EditTextWatcher(editdate));
            txtCheck = (TextView) layout.findViewById(R.id.txtCheck);

            Button btnDate = (Button) layout.findViewById(R.id.btnDate);

            btnDate.setOnClickListener(dateView->{
                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DialogFragment dialog = DateDialogFragment.newInstance();
                dialog.setTargetFragment(EditFragment.this, 0);
                dialog.show(fragmentManager,"dialog_date");

            });

        }else if(level.equals("todo")){
            spinLevel.setSelection(4);
            spinLevel.setEnabled(false);

            big = 0;
            bigtitle = "";
            bighold = 0;
            middle = 0;
            middletitle = "";
            middlehold = 0;
            date = "";
        }


    }

    @Override
    public void onDateDialog(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        date =String.format(Locale.JAPAN,"%02d-%02d-%02d",year,monthOfYear+1,dayOfMonth);
        EditText txtDate = requireActivity().findViewById(R.id.editDate);
        txtDate.setText(date);
    }

    public void getTarget(){ //データ編集時、上中目標取得

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{

                if(level.equals("middle") || level.equals("small")) {
                    //大目標を取得
                    String[] bcols = {"id", "title","hold"};//SQLデータから取得する列
                    String[] blevel = {"big","0"};//大目標のみを抽出
                    Cursor bcs = db.query("ToDoData", bcols, "level=? and fin=?", blevel, null, null, null, null);
                    bigData.clear(); //いったん配列を空にする
                    bigTitle.clear();
                    boolean next = bcs.moveToFirst();//カーソルの先頭に移動
                    while (next) { //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+bcs.getInt(0));//大目標のID
                        item.put("title",bcs.getString(1));
                        item.put("hold",""+bcs.getInt(2));//大目標の待機状態
                        bigData.add(item);
                        if(bcs.getInt(2)==1){ //保留中
                            bigTitle.add(bcs.getString(1)+"(保)");//大目標のタイトル
                        }else{ //非保留中
                            bigTitle.add(bcs.getString(1));//大目標のタイトル
                        }
                        next = bcs.moveToNext();
                    }

                    //中目標を取得
                    String[] mcols = {"id", "title", "big","bigtitle","bighold","hold"};//SQLデータから取得する列
                    String[] mlevel = {"middle","0"};//中目標のみを抽出
                    Cursor mcs = db.query("ToDoData", mcols, "level=? and fin=?", mlevel, null, null, null, null);
                    middleData.clear(); //いったん配列を空にする
                    middleTitle.clear();
                    next = mcs.moveToFirst();//カーソルの先頭に移動
                    while (next) {
                        HashMap<String, String> item = new HashMap<>();
                        item.put("id", "" + mcs.getInt(0));
                        item.put("title",mcs.getString(1));
                        item.put("big", "" + mcs.getInt(2));
                        item.put("bigtitle",mcs.getString(3));
                        item.put("bighold",""+mcs.getInt(4));
                        item.put("hold",""+mcs.getInt(5));
                        middleData.add(item); //中目標データ配列に追加
                        if(mcs.getInt(4)==1){ //大目標保留中
                            if(mcs.getInt(5)==1){ //中目標保留中
                                middleTitle.add(String.format("(%s(保))-%s(保)",mcs.getString(3),mcs.getString(1)));

                            }else{
                                middleTitle.add(String.format("(%s(保))-%s",mcs.getString(3),mcs.getString(1)));

                            }
                        }else{
                            if(mcs.getInt(5)==1){ //中目標保留中
                                middleTitle.add(String.format("(%s)-%s(保)",mcs.getString(3),mcs.getString(1)));

                            }else{
                                middleTitle.add(String.format("(%s)-%s",mcs.getString(3),mcs.getString(1)));

                            }
                        }
                        next = mcs.moveToNext();
                    }
                }

                //選択IDデータを取得
                String[] sels = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","date","content","hold","important","memo","proceed","fin"};
                String[] sellevel = {level, String.valueOf(id)};
                Cursor selcs = db.query("ToDoData",sels,"level=? and id=?",sellevel,null,null,null,null);
                boolean next = selcs.moveToFirst();
                if(next){
                    id = selcs.getInt(0);
                    title = selcs.getString(1);
                    lcv.put("beforetitle",title);
                    big = selcs.getInt(2);
                    lcv.put("beforebig",big);
                    bigtitle = selcs.getString(3);
                    lcv.put("beforebigtitle",bigtitle);
                    bighold = selcs.getInt(4);
                    lcv.put("beforebighold",bighold);
                    middle = selcs.getInt(5);
                    lcv.put("beforemiddle",middle);
                    middletitle = selcs.getString(6);
                    lcv.put("beforemiddletitle",middletitle);
                    middlehold = selcs.getInt(7);
                    lcv.put("beforemiddlehold",middlehold);
                    date = selcs.getString(8);
                    lcv.put("beforedate",date);
                    content = selcs.getString(9);
                    lcv.put("beforecontent",content);
                    hold = selcs.getInt(10);
                    lcv.put("beforehold",hold);
                    important = selcs.getInt(11);
                    lcv.put("beforeimportant",important);
                    memo = selcs.getString(12);
                    lcv.put("beforememo",memo);
                    proceed = selcs.getInt(13);
                    lcv.put("beforeproceed",proceed);
                    fin = selcs.getInt(14);
                    lcv.put("beforefin",fin);
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


    private class EditTextWatcher implements TextWatcher {

        private View view;

        private EditTextWatcher(View view) {
            this.view = view;
        }

        //タイトル文字列修正直前に呼び出される
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        //タイトル文字入力時に呼び出される
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        //タイトル入力の最後に呼び出される
        @Override
        public void afterTextChanged(Editable editable) {

            switch (view.getId()) {
                case R.id.EditTitle:

                    titleNum = editable.toString().length();
                    if(titleNum>0){ //タイトル入力時にボタン有効化
                        if(!level.equals("schedule")){ //スケジュール以外はボタン有効化
                            editBtn.setEnabled(true);
                        }else if(dateok){ //スケジュールの日付が正しく入力されていたらボタン有効化
                            editBtn.setEnabled(true);
                        }else{
                            editBtn.setEnabled(false);
                        }
                    }else{
                        editBtn.setEnabled(false);
                    }

                    break;
                case R.id.editDate:

                    // テキスト変更後に変更されたテキストを取り出す
                    String inputDate= editable.toString();

                    // 入力された文字をチェック
                    if(inputDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")){
                        String str = "OK";
                        txtCheck.setText(str);
                        date = inputDate;
                        dateok = true;
                        Log.e("TITLENUM",""+titleNum);
                        if(titleNum>0){ //タイトル入力時にボタン有効化
                            editBtn.setEnabled(true);
                        }
                    }
                    else {
                        txtCheck.setText("〇〇〇〇-××-△△の形で記入");
                        dateok = false;
                        editBtn.setEnabled(false);
                    }
                    break;
            }

        }

    }
}
