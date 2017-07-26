/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client.lib.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.clover.common2.Signature2;
import com.clover.common2.Signature2.Point;

public class SignatureView extends View {

  Signature2 signature;

  public SignatureView(Context ctx, AttributeSet attrSet) {
    super(ctx, attrSet);
  }

  public SignatureView(Context ctx) {
    super(ctx);
  }

  public void onDraw(Canvas canvas) {
    Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    linePaint.setColor(Color.BLACK);
    linePaint.setStrokeWidth(1.5f);
    if (signature != null && signature.strokes != null) {
      for (Signature2.Stroke stroke : signature.strokes) {
        for (int i = 0; i < stroke.points.size() - 1; i++) {
          Point pt1 = stroke.points.get(i);
          Point pt2 = stroke.points.get(i + 1);
          canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, linePaint);
        }
      }
    }
  }

  public Signature2 getSignature() {
    return signature;
  }

  public void setSignature(Signature2 signature) {
    this.signature = signature;

    post(new Runnable(){
      @Override public void run() {
        invalidate();
      }
    });
  }
}
