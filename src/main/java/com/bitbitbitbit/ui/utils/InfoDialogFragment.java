package com.bitbitbitbit.ui.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.bitbitbitbit.thefreebees.R;

public class InfoDialogFragment extends DialogFragment {

    public static final int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
    private static final String ARGUMENT_TITLE = "title";
    private static final String ARGUMENT_MESSAGE = "message";


    public static InfoDialogFragment newInstance(final String title, final String message) {
        final InfoDialogFragment infoDialog = new InfoDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARGUMENT_TITLE, title);
        args.putString(ARGUMENT_MESSAGE, message);
        infoDialog.setArguments(args);
        return infoDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        final String title = arguments.getString(ARGUMENT_TITLE);
        final String message = arguments.getString(ARGUMENT_MESSAGE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        OnClickListener onClickListener = new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        };
        builder.setTitle(title).setMessage(message).setPositiveButton(getActivity().getString(R.string.ok),
                onClickListener);
        return builder.create();
    }

    //TODO remove?s
    public interface InfoFragmentDialogOnClickListener {
        void onClick();
    }
}
