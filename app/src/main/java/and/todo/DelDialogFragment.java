package and.todo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DelDialogFragment extends DialogFragment {

    public static DelDialogFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        DelDialogFragment fragment = new DelDialogFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    public interface DelDialogListener {
        public void onDelDialogPositiveClick(DialogFragment dialog);
        public void onDelDialogNegativeClick(DialogFragment dialog);
        public void onDelDialogNeutralClick(DialogFragment dialog);
    }

    DelDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (DelDialogListener) getTargetFragment();

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle data = getArguments(); //Bundleデータ取得
        String title = data.getString("title");

        DialogClickListener listener = new DialogClickListener();

        Dialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("削除")
                .setMessage(title+"を削除しますか？")
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
                    listener.onDelDialogPositiveClick(DelDialogFragment.this);
                    break;
                //Cancelボタンがタップされたとき
                case DialogInterface.BUTTON_NEGATIVE:
                    //Cancelボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onDelDialogNegativeClick(DelDialogFragment.this);
                    break;
                //Neutralボタンがタップされたとき
                case DialogInterface.BUTTON_NEUTRAL:
                    //Neutralボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onDelDialogNeutralClick(DelDialogFragment.this);
                    break;
            }

        }
    }

}
