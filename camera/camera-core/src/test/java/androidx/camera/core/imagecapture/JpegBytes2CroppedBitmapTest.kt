/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.core.imagecapture

import android.graphics.Color.BLUE
import android.graphics.Color.YELLOW
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.util.Size
import androidx.camera.core.imagecapture.Utils.HEIGHT
import androidx.camera.core.imagecapture.Utils.WIDTH
import androidx.camera.core.processing.Packet
import androidx.camera.testing.ExifUtil.createExif
import androidx.camera.testing.TestImageUtil.createJpegBytes
import androidx.camera.testing.TestImageUtil.getAverageDiff
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.internal.DoNotInstrument

/**
 * Unit tests for [JpegBytes2CroppedBitmap].
 */
@RunWith(RobolectricTestRunner::class)
@DoNotInstrument
@Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
class JpegBytes2CroppedBitmapTest {

    private val processor = JpegBytes2CroppedBitmap()

    @Test
    fun process_verifyOutput() {
        // Arrange.
        val jpegBytes = createJpegBytes(WIDTH, HEIGHT)
        val cropRect = Rect(0, 240, 640, 480)
        // A mirroring on the x-axis.
        val transform = Matrix().apply { this.setScale(-1F, 1F, 320F, 240F) }
        val input = Packet.of(
            jpegBytes,
            createExif(jpegBytes),
            ImageFormat.JPEG,
            Size(WIDTH, HEIGHT),
            cropRect,
            90,
            transform
        )

        // Act.
        val output = processor.process(input)

        // Assert: only the yellow and blue blocks exist after the cropping.
        assertThat(getAverageDiff(output.data, Rect(0, 0, 320, 240), YELLOW)).isEqualTo(0)
        assertThat(getAverageDiff(output.data, Rect(321, 0, WIDTH, 240), BLUE)).isEqualTo(0)
        // Assert: the packet info is correct.
        assertThat(output.cropRect).isEqualTo(Rect(0, 0, cropRect.width(), cropRect.height()))
        assertThat(output.exif).isEqualTo(input.exif)
        assertThat(output.format).isEqualTo(ImageFormat.FLEX_RGBA_8888)
        assertThat(output.size).isEqualTo(Size(cropRect.width(), cropRect.height()))
        assertThat(output.rotationDegrees).isEqualTo(input.rotationDegrees)
        // Assert: after mirroring and cropping, the bottom-right corner should map to the
        // bottom-left corner in the new image.
        val points = floatArrayOf(WIDTH.toFloat(), HEIGHT.toFloat())
        output.sensorToBufferTransform.mapPoints(points)
        assertThat(points).usingTolerance(1E-4).containsExactly(0, 240)
    }
}