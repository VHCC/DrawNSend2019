package ichen.chu.drawableviewlibs.gestures.scroller;

import android.graphics.RectF;

public interface ScrollerListener {
  void onViewPortChange(RectF currentViewport);
  void onCanvasChanged(RectF canvasRect);
}
