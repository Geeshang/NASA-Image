/****************************************************************************
 Copyright (c) 2015 Geeshang Xu (Geeshangxu@gmail.com)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package geeshang.nasaimage;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * This class encapsulate some image operations use Matrix class.
 */
class ImageTransformHandler implements View.OnTouchListener {
    boolean isTurnOnModeDrag = true;
    boolean isTurnOnModeZoom = true;
    boolean isTurnOnModeRotate = false;
    boolean isTurnOnModeSkew = false;
    private int mMode;
    private static final int MODE_NOTHING = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;
    private static final int MODE_ROTATE = 3;
    private static final int MODE_SKEW = 4;
    private final Matrix mMatrix = new Matrix();
    private final PointF mPoint = new PointF();
    private PointF mMiddlePoint = new PointF();
    private float mDistance;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView imageView = (ImageView) v;
        mMatrix.set(imageView.getImageMatrix());
        //Must be & MotionEvent.ACTION_MASK, otherwise can't trigger ACTION_POINTER_DOWN event.
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mMode = MODE_DRAG;
                mPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mMode = MODE_NOTHING;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mMode = MODE_ZOOM;
                mMiddlePoint = getMiddlePoint(event);
                mDistance = getPointsDistance(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mMode = MODE_NOTHING;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mMode) {
                    case MODE_DRAG:
                        if (isTurnOnModeDrag) {
                            float x = event.getX() - mPoint.x;
                            float y = event.getY() - mPoint.y;
                            //Between two "event.getX",there is "mMatrix.postTranslate()" which consume some time so
                            //that image translate can achieve.
                            mMatrix.postTranslate(x, y);
                            mPoint.set(event.getX(), event.getY());
                        }
                        break;
                    case MODE_ZOOM:
                        if (isTurnOnModeZoom) {
                            float zoomFactor = (getPointsDistance(event) / mDistance);
                            //Between two "getPointsDistance()",there is "mMatrix.postScale()" which consume some time so
                            //that image zoom can achieve.
                            mMatrix.postScale(zoomFactor, zoomFactor, mMiddlePoint.x, mMiddlePoint.y);
                            mDistance = getPointsDistance(event);
                        }
                        break;
                    case MODE_ROTATE:
                        if (isTurnOnModeRotate) {
                            //implement if you want
                        }
                        break;
                    case MODE_SKEW:
                        if (isTurnOnModeSkew) {
                            //implement if you want
                        }
                        break;
                }
                break;
        }
        imageView.setImageMatrix(mMatrix);
        return true;
    }

    private PointF getMiddlePoint(MotionEvent event) {
        float x = (event.getX(1) + event.getX(0)) / 2;
        float y = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(x, y);
    }

    private float getPointsDistance(MotionEvent event) {
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        return FloatMath.sqrt(x * x + y * y);
    }
}
