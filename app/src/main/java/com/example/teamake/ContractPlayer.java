package com.example.teamake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ContractPlayer extends ActivityResultContract<Integer, Uri> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull Integer position) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra("positionPlayer",position);
        return intent;
    }

    @Override
    public Uri parseResult(int resultCode, @Nullable Intent result) {
        if (resultCode != Activity.RESULT_OK || result == null) {
            return null;
        }
        return result.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
    }
}
