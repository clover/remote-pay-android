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

package com.clover.remote.client.lib.example.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import com.clover.common2.Signature2;

public class SignaturePanel extends View {

  Signature2 signature;
  Paint signaturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);


  public SignaturePanel(Context context) {
    super(context);
    signaturePaint.setStyle(Paint.Style.STROKE);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (signature != null) {
      for (Signature2.Stroke stroke : signature.strokes) {

        for (int j = 1; j < stroke.points.size(); j++) {
          Signature2.Point point1 = stroke.points.get(j);
          Signature2.Point point2 = stroke.points.get(j);
          canvas.drawLine(point1.x, point1.y, point2.x, point2.y, signaturePaint);
        }
      }
    } else {
      // unless first draw call, this is probably indicates sign on receipt
      // TODO: draw a message for 'Check signature on paper.'?
    }
  }

  public void setSignature(Signature2 signature) {
    this.signature = signature;
  }
}
