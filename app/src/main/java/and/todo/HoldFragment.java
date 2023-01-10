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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class HoldFragment extends Fragment implements DelDialogFragment.DelDialogListener,HoldDialogFragment.HoldDialogListener,FinDialogFragment.FinDialogListener{
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
    Button bhold,mhold,shold,todohold;//各保留ボタン変数
    Button bfin,mfin,sfin,todofin;//各完了ボタン変数
    DeleteData del = null; //データ削除用クラス
    int bigDel=0,middleDel=0,smallDel=0,todoDel=0; //データ削除時に配列から消去するための配列インデックス変数
    private int did;
    private String dlevel;
    private boolean content = false;

    //データ渡し用のBundleデータ
    Bundle sendData;

    CustomSpinner bigTarget,middleTarget; //大中目標のスピナー変数
    RecyclerView sList,todoList; //小目標スケジュールのリスト変数

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

        View decor = requireActivity().getWindow().getDecorView();//バーを含めたぜびゅー全体

        ConstraintLayout top = view.findViewById(R.id.topConstraint);
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


        //Bundleデータ取得
        Bundle Data = getArguments();

        //データベースから各データを取得
        helper = new EditDatabaseHelper(requireActivity());

        //TOPに表示するデータの配列を取得
        getArrays();

        del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成

        //大目標にデータ設定
        bigTarget = (CustomSpinner) view.findViewById(R.id.bigTarget);
        if(bigTitle.size()==0){
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.bigLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("大目標なし");
        }else {
            ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle);
            bigTarget.setAdapter(bAdapter);
            bid = Integer.parseInt(bigData.get(0).get("id")); //大目標の初期位置ID
        bigTarget.setOnItemSelectedListener(new SpinSelecter());

    }
        //中目標にデータ設定
        middleTarget = (CustomSpinner) view.findViewById(R.id.middleTarget);
        if(middleTitle.size()==0){
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.middleLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("中目標なし");
        }else{
            ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
            middleTarget.setAdapter(mAdapter);
            mid = Integer.parseInt(middleData.get(0).get("id")); //中目標の初期位置ID

        middleTarget.setOnItemSelectedListener(new SpinSelecter());

    }

        if(smallData.size()==0){ //小目標がない時レイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.smallLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("小目標なし");
        }else {

            //小目標にデータ設定
            sList = view.findViewById(R.id.smallTargetList);
            sList.setHasFixedSize(true);
            LinearLayoutManager  rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            sList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            sList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(smallTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onSmallItemClick(view,position,itemData);
                }
            };
            sList.setAdapter(adapter);



            /*            sList.setOnTouchListener(this);*/
            //LayoutParamsを取得
            ViewGroup.LayoutParams params = sList.getLayoutParams();
            if (smallData.size() == 1) { //リストの項目数で高さを変える
                params.height = 200;
            } else if (smallData.size() == 2) {
                params.height = 300;
            } else if(smallData.size() == 3){
                params.height = 400;
            } else if(smallData.size() == 4){
                params.height = 500;
            }else if(smallData.size() == 5){
                params.height = 600;
            }else if (smallData.size() == 6) {
                params.height = 700;
            } else if(smallData.size() == 7){
                params.height = 800;
            }else if(smallData.size() == 8){
                params.height = 900;
            }else if (smallData.size() >= 9) {
                params.height = 1000;
            }
            sList.setLayoutParams(params);

        }
            sedit = (ImageButton) view.findViewById(R.id.SeditButton);
            sedit.setEnabled(false); //項目選択まで無効化
            sedit.setOnClickListener(new editClicker());
            sdl = (ImageButton) view.findViewById(R.id.SdeleteButton);
            sdl.setEnabled(false); //項目選択まで削除ボタン無効化
            sdl.setOnClickListener(new editClicker());
            shold = view.findViewById(R.id.SholdButton);//小目標、保留ボタン
            shold.setEnabled(false);
            shold.setOnClickListener(new editClicker());
            sfin = view.findViewById(R.id.SfinishButton);//小目標、完了ボタン
            sfin.setEnabled(false);
            sfin.setOnClickListener(new editClicker());


        if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.todoLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("TODOリストなし");
        }else {

            //やることリストにデータ設定
            todoList = view.findViewById(R.id.todoList);
            todoList.setHasFixedSize(true);
            LinearLayoutManager  rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            todoList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            todoList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(todoTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onToDoItemClick(view,position,itemData);
                }
            };
            todoList.setAdapter(adapter);


            //LayoutParamsを取得
            ViewGroup.LayoutParams params = todoList.getLayoutParams();
            if (todoData.size() == 1) {
                params.height = 200;
            } else if (todoData.size() == 2) {
                params.height = 300;
            } else if(todoData.size() == 3){
                params.height = 400;
            } else if(todoData.size() == 4){
                params.height = 500;
            }else if(todoData.size() == 5){
                params.height = 600;
            }else if (todoData.size() == 6) {
                params.height =700;
            } else if(todoData.size() == 7){
                params.height = 800;
            }else if(todoData.size() == 8){
                params.height = 900;
            }else if (todoData.size() >= 9) {
                params.height = 1000;
            }
            todoList.setLayoutParams(params);

        }
            todoedit = (ImageButton) view.findViewById(R.id.todoEdit);
            todoedit.setEnabled(false);
            todoedit.setOnClickListener(new editClicker());
            tododl = (ImageButton) view.findViewById(R.id.todoDelete);
            tododl.setEnabled(false);
            tododl.setOnClickListener(new editClicker());
            todohold = view.findViewById(R.id.todoholdButton);//やること、保留ボタン
            todohold.setEnabled(false);
            todohold.setOnClickListener(new editClicker());
            todofin = view.findViewById(R.id.todofinishButton);//やること、完了ボタン
            todofin.setEnabled(false);
            todofin.setOnClickListener(new editClicker());


        CustomSpinner cspinner = view.findViewById(R.id.modeChange); //編集モード選択スピナー取得
        String[] spinnerItems = { "モード選択","標準モード", "内容表示モード" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,
                spinnerItems);
        cspinner.setAdapter(adapter);
        cspinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                if(position==1){ //標準モード（選択しても表示されない）
                    content = false;
                }else if(position==2){ //内容表示モード（項目の内容をトースト表示）
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
        //各編集ボタンのイベントリスナー
        bedit.setOnClickListener(new HoldFragment.editClicker());
        medit.setOnClickListener(new HoldFragment.editClicker());

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
        //各削除ボタンのイベントリスナー
        bdl.setOnClickListener(new HoldFragment.editClicker());
        mdl.setOnClickListener(new HoldFragment.editClicker());

        //各保留ボタン要素を取得
        bhold = view.findViewById(R.id.BholdButton);
        if(bigData.size()>0){ //大目標データがあれば保留ボタン有効化
            bhold.setEnabled(true);
        }else{ //大目標データがなければ保留ボタン非表示
            bhold.setEnabled(false);
        }
        mhold = view.findViewById(R.id.MholdButton);
        if(middleData.size()>0){ // 中目標データがあれば保留ボタン有効化
            mhold.setEnabled(true);
        }else{ //中目標データがなければ保留ボタン非表示
            mhold.setEnabled(false);
        }
        //各保留ボタンのイベントリスナー
        bhold.setOnClickListener(new editClicker());
        mhold.setOnClickListener(new editClicker());

        //各完了ボタン要素を取得
        bfin = view.findViewById(R.id.BfinishButton);
        if(bigData.size()>0){ //大目標データがあれば完了ボタン有効化
            bfin.setEnabled(true);
        }else{ //大目標データがなければ完了ボタン非表示
            bfin.setEnabled(false);
        }
        mfin = view.findViewById(R.id.MfinishButton);
        if(middleData.size()>0){ // 中目標データがあれば完了ボタン有効化
            mfin.setEnabled(true);
        }else{ //中目標データがなければ完了ボタン非表示
            mfin.setEnabled(false);
        }
        //各完了ボタンのイベントリスナー
        bfin.setOnClickListener(new editClicker());
        mfin.setOnClickListener(new editClicker());

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
            }else if(view == bhold){//大目標の保留ボタン
                editData.putString("title",bigTitle.get(bigDel));
                editData.putString("level","big");
                dlevel = "big";
                did = bid;
            }else if(view == mhold){//中目標の保留ボタン
                editData.putString("title",middleTitle.get(middleDel));
                editData.putString("level","middle");
                dlevel = "middle";
                did = mid;
            }else if(view == shold){//小目標の保留ボタン
                editData.putString("title",smallTitle.get(smallDel));
                editData.putString("level","small");
                dlevel = "small";
                did = sid;
            }else if(view == todohold){//やることリスト保留ボタン
                editData.putString("title",todoTitle.get(todoDel));
                editData.putString("level","todo");
                dlevel = "todo";
                did = todoid;
            }else if(view == bfin){//大目標の完了ボタン
                editData.putString("title",bigTitle.get(bigDel));
                editData.putString("level","big");
                dlevel = "big";
                did = bid;
            }else if(view == mfin){//中目標の完了ボタン
                editData.putString("title",middleTitle.get(middleDel));
                editData.putString("level","middle");
                dlevel = "middle";
                did = mid;
            }else if(view == sfin){//小目標の完了ボタン
                editData.putString("title",smallTitle.get(smallDel));
                editData.putString("level","small");
                dlevel = "small";
                did = sid;
            }else if(view == todofin){//やることリスト完了ボタン
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

            }else if(view == bhold || view == mhold || view == shold || view == todohold){
                //保留確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                HoldDialogFragment dialog =
                        HoldDialogFragment.newInstance(editData);
                dialog.setTargetFragment(HoldFragment.this, 0);

                dialog.show(fragmentManager,"dialog_hold");

            }else if(view == bfin || view == mfin || view == sfin || view == todofin){
                //完了確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                FinDialogFragment dialog =
                        FinDialogFragment.newInstance(editData);
                dialog.setTargetFragment(HoldFragment.this, 0);

                dialog.show(fragmentManager,"dialog_finish");

            }

        }
    }

    public void onSmallItemClick(View view,int position,String itemData){//小目標項目選択時のID取得,処理
        sid = Integer.parseInt( smallData.get(position).get("id"));
        smallDel = position;
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),smallTitle.get(position)+"の内容:\n"+smallData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }
        sedit.setEnabled(true); //編集ボタン有効化
        sdl.setEnabled(true); //削除ボタン有効化
        shold.setEnabled(true);//保留ボタン有効化
        sfin.setEnabled(true);//完了ボタン有効化
    }
    public void onToDoItemClick(View view,int position,String itemData){  //やることリスト項目選択時のID取得,処理
        todoid = Integer.parseInt( todoData.get(position).get("id") );
        todoDel = position;
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),todoTitle.get(position)+"の内容:\n"+todoData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }
        todoedit.setEnabled(true);
        tododl.setEnabled(true);
        todohold.setEnabled(true);
        todofin.setEnabled(true);
    }

    class SpinSelecter implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            //項目選択時のIDを取得
            if(adapterView == bigTarget){ //大目標選択時のID取得
                bid = Integer.parseInt( bigData.get(position).get("id"));
                bigDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),bigTitle.get(position)+"の内容:\n"+bigData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
            }else if(adapterView == middleTarget){ //中目標選択時のID取得
                mid = Integer.parseInt( middleData.get(position).get("id"));
                middleDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),middleTitle.get(position)+"の内容:\n"+middleData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }


    //データ削除確定時の処理
    @Override
    public void onDelDialogPositiveClick(DialogFragment dialog) {
        try {
            DeleteData del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成
            del.delete(dlevel, did);

            if (dlevel.equals("big")) { //大目標削除時の処理
                bigData.remove(bigDel);
                bigTitle.remove(bigDel);
                if(bigTitle.size()==0){
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("大目標なし");
                }else {
                    ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle);
                    bigTarget.setAdapter(bAdapter);
                }

                //中小目標データを取得し直す
                try (SQLiteDatabase db = helper.getReadableDatabase()) {
                    //トランザクション開始
                    db.beginTransaction();
                    try {

                        //中目標を取得
                        String[] mcols = {"id", "title", "big", "bigtitle", "bighold", "content", "hold", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] mlevel = {"middle", "0"};//中目標,未完了を抽出
                        Cursor mcs = db.query("ToDoData", mcols, "level=? and fin=?", mlevel, null, null, null, null);

                        middleData.clear(); //いったん配列を空にする
                        middleTitle.clear();
                        boolean next = mcs.moveToFirst();//カーソルの先頭に移動
                        while (next) {
                            int big = mcs.getInt(2);
                            int hold = mcs.getInt(6);
                            if (hold == 1) { //保留中の中目標配列データ保存
                                HashMap<String, String> item = new HashMap<>();
                                item.put("id", "" + mcs.getInt(0));
                                item.put("title", mcs.getString(1));
                                item.put("big", "" + big);
                                item.put("bigtitle", mcs.getString(3));
                                item.put("bighold", "" + mcs.getInt(4));
                                item.put("content", mcs.getString(5));
                                item.put("hold", "" + hold);
                                item.put("important", "" + mcs.getInt(7));
                                item.put("memo", mcs.getString(8));
                                item.put("proceed", "" + mcs.getInt(9));
                                item.put("fin", "" + mcs.getInt(10));

                                if (big != 0) { //大目標が存在するとき

                                    if (Integer.parseInt(item.get("bighold")) == 1) { //大目標保留中
                                        middleTitle.add(String.format("(%s(保))-%s(保)", item.get("bigtitle"), mcs.getString(1)));
                                    } else {
                                        middleTitle.add(String.format("(%s)-%s(保)", item.get("bigtitle"), mcs.getString(1)));

                                    }

                                } else { //大目標未設定時
                                    middleTitle.add(String.format("大目標未設定-%s(保)", mcs.getString(1)));
                                }

                                middleData.add(item); //中目標データ配列に追加
                            }
                            next = mcs.moveToNext();
                        }


                        //小目標を取得
                        String[] scols = {"id", "title", "big", "bigtitle", "bighold", "middle", "middletitle", "middlehold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] slevel = {"small", "1", "0"};//小目標,保留中,未完了を抽出
                        Cursor scs = db.query("ToDoData", scols, "level=? and hold=? and fin=?", slevel, null, null, null, null);

                        smallData.clear(); //いったん配列を空にする
                        smallTitle.clear();
                        next = scs.moveToFirst();//カーソルの先頭に移動
                        while (next) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + scs.getInt(0));
                            item.put("title", scs.getString(1));
                            item.put("big", "" + scs.getInt(2));
                            item.put("bigtitle", scs.getString(3));
                            item.put("bighold", "" + scs.getInt(4));
                            item.put("middle", "" + scs.getInt(5));
                            item.put("middletitle", scs.getString(6));
                            item.put("middlehold", "" + scs.getInt(7));
                            item.put("content", scs.getString(8));
                            item.put("important", scs.getString(9));
                            item.put("memo", scs.getString(10));
                            item.put("proceed", scs.getString(11));
                            item.put("fin", scs.getString(12));
                            //todo
                            if (scs.getInt(5) != 0) { //中目標が存在するとき
                                if (scs.getInt(2) != 0) { //大目標が存在するとき
                                    if (scs.getInt(4) == 1) {//大目標保留中
                                        if (scs.getInt(7) == 1) { //中目標保留中
                                            smallTitle.add(String.format("(%s(保))-(%s(保))-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        } else {//中目標保留無し
                                            smallTitle.add(String.format("(%s(保))-(%s)-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        }
                                    } else {//大目標保留無し
                                        if (scs.getInt(7) == 1) { //中目標保留中
                                            smallTitle.add(String.format("(%s)-(%s(保))-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        } else {//中目標保留無し
                                            smallTitle.add(String.format("(%s)-(%s)-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        }
                                    }
                                } else { //大目標が未設定
                                    if (scs.getInt(7) == 1) { //中目標保留中
                                        smallTitle.add(String.format("大目標未設定-(%s(保))-%s(保)", item.get("middletitle"), item.get("title")));
                                    } else { //中目標保留無し
                                        smallTitle.add(String.format("大目標未設定-(%s)-%s(保)", item.get("middletitle"), item.get("title")));
                                    }
                                }
                            } else { //中目標未設定時
                                smallTitle.add(String.format("大目標未設定-中目標未設定-%s(保)", item.get("title")));
                            }
                            smallData.add(item); //中目標データ配列に追加
                            next = scs.moveToNext();
                        }

                        //トランザクション成功
                        db.setTransactionSuccessful();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        //トランザクションを終了
                        db.endTransaction();
                    }
                }
                //中小目標をリストに再設定
                if(middleTitle.size()==0){
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("中目標なし");
                }else{
                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
                    middleTarget.setAdapter(mAdapter);

                }


                if(smallData.size()==0){ //小目標がない時レイアウトを消す
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("小目標なし");
                }else {

                    MyAdapter sadapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onSmallItemClick(view, position, itemData);
                        }
                    };
                    sList.setAdapter(sadapter);
                }
                sedit.setEnabled(false);
                sdl.setEnabled(false);
                shold.setEnabled(false);
                sfin.setEnabled(false);
                if (bigData.size() > 0) { //大目標が存在するとき
                    bigTarget.setSelection(0);
                    bigDel = 0;
                    bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
                } else {//大目標が存在しないとき
                    bid = 0;
                    bedit.setEnabled(false); //編集ボタン無効化
                    bdl.setEnabled(false); //削除ボタン無効化
                    bhold.setEnabled(false);
                    bfin.setEnabled(false);
                }
                if (middleData.size() > 0) { //中目標が存在するとき
                    middleTarget.setSelection(0);
                    middleDel = 0;
                    mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                } else {//中目標が存在しないとき
                    mid = 0;
                    medit.setEnabled(false); //編集ボタン無効化
                    mdl.setEnabled(false); //削除ボタン無効化
                    mhold.setEnabled(false);
                    mfin.setEnabled(false);
                }
            } else if (dlevel.equals("middle")) { //中目標削除時の処理
                middleData.remove(middleDel);
                middleTitle.remove(middleDel);
                if(middleTitle.size()==0){
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("中目標なし");
                }else{
                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
                    middleTarget.setAdapter(mAdapter);

                }

                //小目標データを取得し直す
                try (SQLiteDatabase db = helper.getReadableDatabase()) {
                    //トランザクション開始
                    db.beginTransaction();
                    try {

                        //小目標を取得
                        String[] scols = {"id", "title", "big", "bigtitle", "bighold", "middle", "middletitle", "middlehold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] slevel = {"small", "1", "0"};//小目標のみ,保留中,未完了を抽出
                        Cursor scs = db.query("ToDoData", scols, "level=? and hold=? and fin=?", slevel, null, null, null, null);

                        smallData.clear(); //いったん配列を空にする
                        smallTitle.clear();
                        boolean next = scs.moveToFirst();//カーソルの先頭に移動
                        while (next) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + scs.getInt(0));
                            item.put("title", scs.getString(1));
                            item.put("big", "" + scs.getInt(2));
                            item.put("bigtitle", scs.getString(3));
                            item.put("bighold", "" + scs.getInt(4));
                            item.put("middle", "" + scs.getInt(5));
                            item.put("middletitle", scs.getString(6));
                            item.put("middlehold", "" + scs.getInt(7));
                            item.put("content", scs.getString(8));
                            item.put("important", scs.getString(9));
                            item.put("memo", scs.getString(10));
                            item.put("proceed", scs.getString(11));
                            item.put("fin", scs.getString(12));
                            //todo
                            if (scs.getInt(5) != 0) { //中目標が存在するとき
                                if (scs.getInt(2) != 0) { //大目標が存在するとき
                                    if (scs.getInt(4) == 1) {//大目標保留中
                                        if (scs.getInt(7) == 1) { //中目標保留中
                                            smallTitle.add(String.format("(%s(保))-(%s(保))-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        } else {//中目標保留無し
                                            smallTitle.add(String.format("(%s(保))-(%s)-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        }
                                    } else {//大目標保留無し
                                        if (scs.getInt(7) == 1) { //中目標保留中
                                            smallTitle.add(String.format("(%s)-(%s(保))-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        } else {//中目標保留無し
                                            smallTitle.add(String.format("(%s)-(%s)-%s(保)", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                        }
                                    }
                                } else { //大目標が未設定
                                    if (scs.getInt(7) == 1) { //中目標保留中
                                        smallTitle.add(String.format("大目標未設定-(%s(保))-%s(保)", item.get("middletitle"), item.get("title")));
                                    } else { //中目標保留無し
                                        smallTitle.add(String.format("大目標未設定-(%s)-%s(保)", item.get("middletitle"), item.get("title")));
                                    }
                                }
                            } else { //中目標未設定時
                                smallTitle.add(String.format("大目標未設定-中目標未設定-%s(保)", item.get("title")));
                            }
                            smallData.add(item); //中目標データ配列に追加
                            next = scs.moveToNext();
                        }

                        //トランザクション成功
                        db.setTransactionSuccessful();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        //トランザクションを終了
                        db.endTransaction();
                    }
                }

                if(smallData.size()==0){ //小目標がない時レイアウトを消す
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("小目標なし");
                }else {
                    MyAdapter sadapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onSmallItemClick(view, position, itemData);
                        }
                    };
                    sList.setAdapter(sadapter);
                }
                sedit.setEnabled(false);
                sdl.setEnabled(false);
                shold.setEnabled(false);
                sfin.setEnabled(false);
                if (middleData.size() > 0) { //中目標が存在するとき
                    middleTarget.setSelection(0);
                    middleDel = 0;
                    mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                } else {//中目標が存在しないとき
                    mid = 0;
                    medit.setEnabled(false); //編集ボタン無効化
                    mdl.setEnabled(false); //削除ボタン無効化
                    mhold.setEnabled(false);
                    mfin.setEnabled(false);
                }
            } else if (dlevel.equals("small")) {
                smallData.remove(smallDel);
                smallTitle.remove(smallDel);

                if(smallData.size()==0){ //小目標がない時レイアウトを消す
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("小目標なし");
                }else {
                    MyAdapter sadapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onSmallItemClick(view, position, itemData);
                        }
                    };
                    sList.setAdapter(sadapter);
                }
                sedit.setEnabled(false); //ボタンを無効化
                sdl.setEnabled(false);
                shold.setEnabled(false);
                sfin.setEnabled(false);
            } else if (dlevel.equals("todo")) {
                todoData.remove(todoDel);
                todoTitle.remove(todoDel);
                if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("TODOリストなし");
                }else {
                    MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onToDoItemClick(view, position, itemData);
                        }
                    };
                    todoList.setAdapter(adapter);
                }
                todoedit.setEnabled(false);//ボタンを無効化
                tododl.setEnabled(false);
                todohold.setEnabled(false);
                todofin.setEnabled(false);
                todoid = 0;
                todoDel = 0; //削除するデータのインデックスリセット
            }
        }catch(Exception e){
            //Log.e("HOLDのエラー",e.getClass().getName()+","+e.getMessage());
            e.printStackTrace();
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


    //データ保留解除時の処理
    @Override
    public void onHoldDialogPositiveClick(DialogFragment dialog) {
        boolean hold_off=false;//保留解除処理を行った時の判定変数

        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {

                int bhold,mhold;//上位目標の保留状態
                ContentValues cv = new ContentValues();
                ContentValues lcv = new ContentValues(); //データ変更時のContentValues

                if(dlevel.equals("middle")){//中目標の保留状態を変更するとき

                    Cursor cs = db.query("ToDoData", new String[]{"bighold"},
                            "id=?",
                            new String[]{""+did}, null, null, null, null);
                    boolean next = cs.moveToFirst();
                    bhold=1; //大目標の保留状態
                    if(next){
                        bhold = cs.getInt(0);
                    }

                    if(bhold==0){//大目標が非保留状態のときのみ保留状態解除
                        hold_off = true;
                        cv.put("hold",0);
                        db.update("ToDoData",cv,"id=?",new String[]{""+did});

                        //中目標保留解除時小目標の中目標保留状態を変更
                        ContentValues mcv = new ContentValues();
                        mcv.put("middlehold",0);
                        db.update("ToDoData",mcv,"middle=?",new String[]{""+did});
                    }

                }else if(dlevel.equals("small")){//小目標の保留状態を変更するとき

                    Cursor cs = db.query("ToDoData", new String[]{"bighold",
                                    "middlehold"},
                            "id=?",
                            new String[]{""+did}, null, null, null, null);
                    boolean next = cs.moveToFirst();
                    bhold=1;mhold=1; //大中目標の保留状態

                    if(next){
                        bhold = cs.getInt(0);
                        mhold = cs.getInt(1);
                    }

                    if(mhold==0 && bhold==0){//大中目標が非保留状態のときのみ保留状態解除
                        hold_off = true;
                        cv.put("hold",0);
                        db.update("ToDoData",cv,"id=?",new String[]{""+did});
                    }
                }else{
                    hold_off = true;
                    cv.put("hold",0);
                    db.update("ToDoData",cv,"id=?",new String[]{""+did});

                    if(dlevel.equals("big")){//大目標保留解除時中小目標の大目標保留状態を変更
                        ContentValues bcv = new ContentValues();
                        bcv.put("bighold",0);
                        db.update("ToDoData",bcv,"big=?",new String[]{""+did});
                    }
                }

                if(hold_off){//保留解除更新時

                    //更新ログへ書き込み
                    String[] cols = {"title","content","big","bigtitle","bighold"
                            ,"middle","middletitle","middlehold","date",
                            "important","memo","proceed","fin"};
                    // データから取得する列
                    Cursor cs = db.query("ToDoData", cols, "id=?",
                            new String[]{""+did}, null, null, null, null);

                    boolean next = cs.moveToFirst();
                    if(next){
                        lcv.put("ope","update");
                        lcv.put("id",did);
                        lcv.put("beforetitle",cs.getString(0));
                        lcv.put("aftertitle",cs.getString(0));
                        lcv.put("beforecontent",cs.getString(1));
                        lcv.put("aftercontent",cs.getString(1));
                        lcv.put("beforelevel",dlevel);
                        lcv.put("afterlevel",dlevel);
                        lcv.put("beforebig",cs.getInt(2));
                        lcv.put("afterbig",cs.getInt(2));
                        lcv.put("beforebigtitle",cs.getString(3));
                        lcv.put("afterbigtitle",cs.getString(3));
                        lcv.put("beforebighold",cs.getInt(4));
                        lcv.put("afterbighold",cs.getInt(4));
                        lcv.put("beforemiddle",cs.getInt(5));
                        lcv.put("aftermiddle",cs.getInt(5));
                        lcv.put("beforemiddletitle",cs.getString(6));
                        lcv.put("aftermiddletitle",cs.getString(6));
                        lcv.put("beforemiddlehold",cs.getInt(7));
                        lcv.put("aftermiddlehold",cs.getInt(7));
                        lcv.put("beforedate",cs.getString(8));
                        lcv.put("afterdate",cs.getString(8));
                        lcv.put("beforehold",1);
                        lcv.put("afterhold",0);
                        lcv.put("beforeimportant",cs.getInt(9));
                        lcv.put("afterimportant",cs.getInt(9));
                        lcv.put("beforememo",cs.getString(10));
                        lcv.put("aftermemo",cs.getString(10));
                        lcv.put("beforeproceed",cs.getInt(11));
                        lcv.put("afterproceed",cs.getInt(11));
                        lcv.put("beforefin",cs.getInt(12));
                        lcv.put("afterfin",cs.getInt(12));

                        db.insert("LogData",null,lcv); //データ変更をログに追加

                        Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                        if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                            String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                            db.execSQL(dsql);
                        }
                    }


                    Toast.makeText(requireActivity(), "データを保留状態にしました",
                            Toast.LENGTH_SHORT).show();


                }


                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }

        if(hold_off){//保留解除更新時
            //データを保留解除した後、配列から消す
            if(dlevel.equals("big")){
                bigData.remove(bigDel);
                bigTitle.remove(bigDel);
                if(bigTitle.size()==0){
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("大目標なし");
                }else {
                    ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle);
                    bigTarget.setAdapter(bAdapter);
                }

                if(bigData.size()>0){ //大目標が存在するとき
                    bigTarget.setSelection(0);
                    bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
                    bigDel = 0; //大目標の位置を一番上に
                }else{//大目標が存在しないとき
                    bid = 0;
                    bedit.setEnabled(false); //編集ボタン無効化
                    bdl.setEnabled(false); //削除ボタン無効化
                    bhold.setEnabled(false);
                    bfin.setEnabled(false);
                }
            }else if(dlevel.equals("middle")){
                middleData.remove(middleDel);
                middleTitle.remove(middleDel);
                if(middleTitle.size()==0){
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("中目標なし");
                }else{
                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
                    middleTarget.setAdapter(mAdapter);

                }

                if(middleData.size()>0){ //中目標が存在するとき
                    middleTarget.setSelection(0);
                    mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                    middleDel = 0;
                }else{//中目標が存在しないとき
                    mid = 0;
                    medit.setEnabled(false); //編集ボタン無効化
                    mdl.setEnabled(false); //削除ボタン無効化
                    mhold.setEnabled(false);
                    mfin.setEnabled(false);
                }
            }else if(dlevel.equals("small")){
                smallData.remove(smallDel);
                smallTitle.remove(smallDel);

                if(smallData.size()==0){ //小目標がない時レイアウトを消す
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("小目標なし");
                }else {
                    MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onSmallItemClick(view, position, itemData);
                        }
                    };
                    sList.setAdapter(adapter);
                }
                sid = 0;
                sedit.setEnabled(false); //ボタンを無効化
                sdl.setEnabled(false);
                shold.setEnabled(false);
                sfin.setEnabled(false);

                smallDel = 0;
            }else if(dlevel.equals("todo")){
                todoData.remove(todoDel);
                todoTitle.remove(todoDel);
                if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                    //レイアウトを取得して消去しブランクにする
                    ConstraintLayout layout;
                    layout =
                            (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                    layout.removeAllViews();
                    getLayoutInflater().inflate(R.layout.non_items, layout);
                    TextView non = layout.findViewById(R.id.noItems);
                    non.setText("TODOリストなし");
                }else {
                    MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onToDoItemClick(view, position, itemData);
                        }
                    };
                    todoList.setAdapter(adapter);
                }
                todoid = 0;
                todoedit.setEnabled(false);//ボタンを無効化
                tododl.setEnabled(false);
                todohold.setEnabled(false);
                todofin.setEnabled(false);

                todoDel = 0;
            }

        }

    }
    //データ保留キャンセル時
    @Override
    public void onHoldDialogNegativeClick(DialogFragment dialog) {
    }
    //データ保留スルー
    @Override
    public void onHoldDialogNeutralClick(DialogFragment dialog) {
    }


    //データ完了確定時の処理
    @Override
    public void onFinDialogPositiveClick(DialogFragment dialog) {
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {

                ContentValues cv = new ContentValues();
                ContentValues lcv = new ContentValues(); //データ変更時のContentValues
                cv.put("fin",1);
                cv.put("proceed",100);
                db.update("ToDoData",cv,"id=?",new String[]{""+did});

                //大目標を完了にした時、中小目標も完了状態にする
                if(dlevel.equals("big")){
                    //中小目標を完了状態にする
                    db.update("ToDoData",cv,"big=?",new String[]{""+did});

                }
                //中目標を完了にしたとき、小目標も完了状態にする
                if(dlevel.equals("middle")){
                    //小目標を完了状態にする
                    db.update("ToDoData",cv,"middle=?",new String[]{""+did});
                }

                //更新ログへ書き込み
                String[] cols = {"title","content","big","bigtitle","bighold"
                        ,"middle","middletitle","middlehold","date",
                        "important","memo","proceed","hold"};
                // データから取得する列
                Cursor cs = db.query("ToDoData", cols, "id=?",
                        new String[]{""+did}, null, null, null, null);

                boolean next = cs.moveToFirst();
                if(next){
                    lcv.put("ope","update");
                    lcv.put("id",did);
                    lcv.put("beforetitle",cs.getString(0));
                    lcv.put("aftertitle",cs.getString(0));
                    lcv.put("beforecontent",cs.getString(1));
                    lcv.put("aftercontent",cs.getString(1));
                    lcv.put("beforelevel",dlevel);
                    lcv.put("afterlevel",dlevel);
                    lcv.put("beforebig",cs.getInt(2));
                    lcv.put("afterbig",cs.getInt(2));
                    lcv.put("beforebigtitle",cs.getString(3));
                    lcv.put("afterbigtitle",cs.getString(3));
                    lcv.put("beforebighold",cs.getInt(4));
                    lcv.put("afterbighold",cs.getInt(4));
                    lcv.put("beforemiddle",cs.getInt(5));
                    lcv.put("aftermiddle",cs.getInt(5));
                    lcv.put("beforemiddletitle",cs.getString(6));
                    lcv.put("aftermiddletitle",cs.getString(6));
                    lcv.put("beforemiddlehold",cs.getInt(7));
                    lcv.put("aftermiddlehold",cs.getInt(7));
                    lcv.put("beforedate",cs.getString(8));
                    lcv.put("afterdate",cs.getString(8));
                    lcv.put("beforehold",cs.getInt(12));
                    lcv.put("afterhold",cs.getInt(12));
                    lcv.put("beforeimportant",cs.getInt(9));
                    lcv.put("afterimportant",cs.getInt(9));
                    lcv.put("beforememo",cs.getString(10));
                    lcv.put("aftermemo",cs.getString(10));
                    lcv.put("beforeproceed",0);
                    lcv.put("afterproceed",100);
                    lcv.put("beforefin",0);
                    lcv.put("afterfin",1);

                    db.insert("LogData",null,lcv); //データ変更をログに追加

                    Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                    if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                        String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                        db.execSQL(dsql);
                    }
                }


                Toast.makeText(requireActivity(), "データを完了状態にしました",
                        Toast.LENGTH_SHORT).show();


                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }


        //データを完了にした後、配列から消す
        if(dlevel.equals("big")){
            bigData.remove(bigDel);
            bigTitle.remove(bigDel);
            if(bigTitle.size()==0){
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("大目標なし");
            }else {
                ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle);
                bigTarget.setAdapter(bAdapter);
            }

            for(int i=0;i<middleData.size();i++){//中目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(middleData.get(i).get("big"))){
                    middleData.remove(i);
                    middleTitle.remove(i);
                    i--;//削除した分インデックスを戻す
                    if(middleTitle.size()==0){
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("中目標なし");
                    }else{
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
                        middleTarget.setAdapter(mAdapter);

                    }

                }
            }
            for(int i=0;i<smallData.size();i++){//小目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(smallData.get(i).get("big"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){ //小目標がない時レイアウトを消す
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(bigData.size()>0){ //大目標が存在するとき
                bigTarget.setSelection(0);
                bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
                bigDel = 0; //大目標の位置を一番上に
            }else{//大目標が存在しないとき
                bid = 0;
                bedit.setEnabled(false); //編集ボタン無効化
                bdl.setEnabled(false); //削除ボタン無効化
                bhold.setEnabled(false);
                bfin.setEnabled(false);
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;//中目標の位置を一番上に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("middle")){
            middleData.remove(middleDel);
            middleTitle.remove(middleDel);
            if(middleTitle.size()==0){
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("中目標なし");
            }else{
                ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
                middleTarget.setAdapter(mAdapter);

            }

            for(int i=0;i<smallData.size();i++){//小目標のうち削除した中目標が上にあるデータを配列から削除
                if(mid == Integer.parseInt(smallData.get(i).get("middle"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){ //小目標がない時レイアウトを消す
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("small")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);

            if(smallData.size()==0){ //小目標がない時レイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("小目標なし");
            }else {
                MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onSmallItemClick(view, position, itemData);
                    }
                };
                sList.setAdapter(adapter);
            }
            sid = 0;
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
            shold.setEnabled(false);
            sfin.setEnabled(false);

            smallDel = 0;
        }else if(dlevel.equals("todo")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);
            if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("TODOリストなし");
            }else {
                MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onToDoItemClick(view, position, itemData);
                    }
                };
                todoList.setAdapter(adapter);
            }
            todoid = 0;
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todohold.setEnabled(false);
            todofin.setEnabled(false);

            todoDel = 0;
        }

    }
    //データ完了キャンセル時
    @Override
    public void onFinDialogNegativeClick(DialogFragment dialog) {
    }
    //データ完了スルー
    @Override
    public void onFinDialogNeutralClick(DialogFragment dialog) {
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
