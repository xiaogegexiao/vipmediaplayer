package io.vov.vitamio.activity;

import io.vov.vitamio.Vitamio;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.mx.vipmediaplayer.R;

public class InitActivity extends Activity {
  public static final String FROM_ME = "fromVitamioInitActivity";
  private ProgressDialog mPD;
  private UIHandler uiHandler;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    uiHandler = new UIHandler(this);

    new AsyncTask<Object, Object, Boolean>() {
      @Override
      protected void onPreExecute() {
        mPD = new ProgressDialog(InitActivity.this);
        mPD.setCancelable(false);
        mPD.setMessage(InitActivity.this.getString(R.string.vitamio_init_decoders));
        mPD.show();
      }

      @Override
      protected Boolean doInBackground(Object... params) {
        return Vitamio.initialize(InitActivity.this);
      }

      @Override
      protected void onPostExecute(Boolean inited) {
        if (inited) {
          uiHandler.sendEmptyMessage(0);
        }
      }

    }.execute();
  }

  private static class UIHandler extends Handler {
    private WeakReference<Context> mContext;

    public UIHandler(Context c) {
      mContext = new WeakReference<Context>(c);
    }

    public void handleMessage(Message msg) {
      InitActivity ctx = (InitActivity) mContext.get();
      switch (msg.what) {
        case 0:
          ctx.mPD.dismiss();
          Intent src = ctx.getIntent();
          Intent i = new Intent();
          i.setClassName(src.getStringExtra("package"), src.getStringExtra("className"));
          i.setData(src.getData());
          i.putExtras(src);
          i.putExtra(FROM_ME, true);
          ctx.startActivity(i);
          ctx.finish();
          break;
      }
    }
  }
}
