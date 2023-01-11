package and.todo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class HoldDialogFragment extends DialogFragment {

    public static HoldDialogFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        HoldDialogFragment fragment = new HoldDialogFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    public interface HoldDialogListener {
        public void onHoldDialogPositiveClick(DialogFragment dialog);
        public void onHoldDialogNegativeClick(DialogFragment dialog);
        public void onHoldDialogNeutralClick(DialogFragment dialog);
    }

    HoldDialogFragment.HoldDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (HoldDialogFragment.HoldDialogListener) getTargetFragment();

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle data = getArguments(); //Bundleデータ取得
        String title = data.getString("title");

        HoldDialogFragment.DialogClickListener listener =
                new HoldDialogFragment.DialogClickListener();

        Dialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("保留")
                .setMessage(title+"を保留にしますか？")
                .setPositiveButton("OK", listener)
                .setNegativeButton("キャンセル", listener)
                .setNeutralButton("あとで", listener)
                .create();

        return dialog;
    }



    private class DialogClickListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface d,int w){

            //switchにてタップされたボタンでの条件分岐を行う
            switch(w){
                //OKボタンがタップされたとき
                case DialogInterface.BUTTON_POSITIVE:
                    //OKボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onHoldDialogPositiveClick(HoldDialogFragment.this);
                    break;
                //Cancelボタンがタップされたとき
                case DialogInterface.BUTTON_NEGATIVE:
                    //Cancelボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onHoldDialogNegativeClick(HoldDialogFragment.this);
                    break;
                //Neutralボタンがタップされたとき
                case DialogInterface.BUTTON_NEUTRAL:
                    //Neutralボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onHoldDialogNeutralClick(HoldDialogFragment.this);
                    break;
            }

        }
    }

}
