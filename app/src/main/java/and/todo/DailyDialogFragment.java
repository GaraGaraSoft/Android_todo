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
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

public class DailyDialogFragment extends DialogFragment {
    private DailyDialogFragment.DailyDialogListener listener;

    public static DailyDialogFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        DailyDialogFragment fragment = new DailyDialogFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    public interface DailyDialogListener {
        void onDailyDialogPositiveClick(DialogFragment dialog,Bundle data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DailyDialogFragment.DailyDialogListener) getTargetFragment();
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
                .inflate(R.layout.daily_edit,null);
        Bundle data = getArguments(); //Bundleデータ取得
        EditText editContent = layout.findViewById(R.id.editContent); //進捗状況編集
        editContent.setText(data.getString("editContent"));


        Dialog dialog = new AlertDialog.Builder(activity)
                .setTitle(data.getString("title")+"の本日の作業内容")
                .setView(layout)
                .setPositiveButton("OK",(d,w)->{
                    //ToDo 編集内容を戻す

                    Bundle result = new Bundle();
                    result.putString("editcontent",editContent.getText().toString());
                    result.putInt("id",data.getInt("id"));
                    listener.onDailyDialogPositiveClick(DailyDialogFragment.this,result);
                })
                .setNeutralButton("キャンセル",null)
                .create();

        return dialog;
    }
}
