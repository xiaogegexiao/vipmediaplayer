package io.vov.vitamio;

import android.app.Activity;
import android.content.Intent;

/**
 * LibsChecker is a wrapper of {@link Vitamio}, it helps to initialize Vitamio
 * easily.
 * <p/>
 * <pre>
 * public void onCreate(Bundle b) {
 * 	super.onCreate(b);
 * 	if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
 * 		return;
 *
 * 	// Code using Vitamio should go below {@link LibsChecker#checkVitamioLibs}
 * }
 * </pre>
 */
public final class LibsChecker {
  public static final String FROM_ME = "fromVitamioInitActivity";

  public static final boolean checkVitamioLibs(Activity ctx) {
    if (!Vitamio.isInitialized(ctx) && !ctx.getIntent().getBooleanExtra(FROM_ME, false)) {
      Intent i = new Intent();
      i.setClassName(Vitamio.getVitamioPackage(), "io.vov.vitamio.activity.InitActivity");
      i.putExtras(ctx.getIntent());
      i.setData(ctx.getIntent().getData());
      i.putExtra("package", ctx.getPackageName());
      i.putExtra("className", ctx.getClass().getName());
      ctx.startActivity(i);
      ctx.finish();
      return false;
    }
    return true;
  }
}
