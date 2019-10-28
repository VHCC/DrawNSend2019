package ichen.chu.drawableviewlibs.gestures.creator;


import ichen.chu.drawableviewlibs.draw.SerializablePath;

public interface GestureCreatorListener {
  void onGestureCreated(SerializablePath serializablePath);

  void onCurrentGestureChanged(SerializablePath currentDrawingPath);
}
