package and.todo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    private ProgressDialogListener listener;
    SeekBar seekProg;
    EditText editProg;
    int fin;

    public static ProgressDialogFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        ProgressDialogFragment fragment = new ProgressDialogFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    public interface ProgressDialogListener {
        void onDialogPositiveClick(DialogFragment dialog,Bundle data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ProgressDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // 親フラグメントがインターフェースを実装していない場合は例外を投げる
            throw new ClassCastException(getTargetFragment().toString() + "はインターフェースを実装していません");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = requireActivity();
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(activity)
                .inflate(R.layout.progress_edit,null);
        Bundle data = getArguments(); //Bundleデータ取得
        EditText editProgCon = layout.findViewById(R.id.editProgCon); //進捗状況編集
        seekProg = layout.findViewById(R.id.seekProg); //達成度調整
        editProg = layout.findViewById(R.id.editProg); //達成度調整
        editProgCon.setText(data.getString("editcontent"));
        seekProg.setProgress(data.getInt("editProg"));
        editProg.setText(String.valueOf( data.getInt("editProg")));


        //ToDo seekProgとeditProgの連動
        //EditTextイベントリスナー
        editProg.addTextChangedListener(new EditProgListener());

        // SeekBarイベントリスナー
        seekProg.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                editProg.setText(""+i); //seekbarを移動したときEditTextも反映
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        CheckBox chkFin = layout.findViewById(R.id.chkFin);//終了
        fin = data.getInt("editFin");
        if(fin==1) //完了変数設定時チェック
            chkFin.setChecked(true);
        chkFin.setOnCheckedChangeListener((buttonView, isChecked)-> {
                fin = isChecked ? 1:0;
        });

        Dialog dialog = new AlertDialog.Builder(activity)
                .setTitle(data.getString("editTitle")+"の進捗状況")
                .setView(layout)
                .setPositiveButton("OK",(d,w)->{
                    //ToDo 編集内容をTopへ戻す

                    if(seekProg.getProgress()==100){ //達成率100のとき完了状態にする
                        fin = 1;
                    }else{
                        fin = 0;
                    }

                    Bundle result = new Bundle();
                    result.putString("editcontent",editProgCon.getText().toString());
                    if(fin == 0)
                        result.putInt("editProg", seekProg.getProgress() );
                    else //finチェック時進捗度100にする
                        result.putInt("editProg",100);
                    result.putInt("id",data.getInt("id"));
                    result.putInt("fin",fin);
                    //result.putInt("editFin",);
                    //getParentFragmentManager().setFragmentResult("requestKey", result);
                    listener.onDialogPositiveClick(ProgressDialogFragment.this,result);
                })
                .setNeutralButton("キャンセル",null)
                .create();

        return dialog;
    }

    private class EditProgListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            try{
                if(Integer.parseInt(s.toString())>100){ //EditTextに100より上を入力したときは100に変更
                    editProg.setText("100");
                }else if(Integer.parseInt(s.toString())<0){//EditTextに0未満を入力したときは0に変更
                    editProg.setText("0");
                }else {//EditTextを変更したときSeekBarにも反映
                    seekProg.setProgress(Integer.parseInt(s.toString()));
                }
            }catch(NumberFormatException e){
                editProg.setText("0");
            }
        }
    }

}
