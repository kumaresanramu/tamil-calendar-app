package com.example

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.imageio.ImageIO

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun generateLauncherIcons() {
    val srcFile = File("src/main/res/drawable/img_app_icon_1781361610489.jpg")
    assertTrue("Source image must exist at ${srcFile.absolutePath}", srcFile.exists())
    
    val originalImage = ImageIO.read(srcFile)
    assertNotNull("Loaded image must not be null", originalImage)

    // Save standard full-res PNG in assets/PlayStore_icon.png and in standard paths
    val pStoreFile = File("src/main/PlayStore_icon.png")
    ImageIO.write(originalImage, "png", pStoreFile)
    println("Saved play store icon to ${pStoreFile.absolutePath}")

    val drawableIconPng = File("src/main/res/drawable/img_app_icon.png")
    ImageIO.write(originalImage, "png", drawableIconPng)
    println("Saved master png to ${drawableIconPng.absolutePath}")

    // Standard sizes
    val sizes = mapOf(
      "mipmap-mdpi" to 48,
      "mipmap-hdpi" to 72,
      "mipmap-xhdpi" to 96,
      "mipmap-xxhdpi" to 144,
      "mipmap-xxxhdpi" to 192
    )

    for ((folder, size) in sizes) {
      val destDir = File("src/main/res/$folder")
      if (!destDir.exists()) {
        destDir.mkdirs()
      }

      val resized = resizeImage(originalImage, size, size)
      
      val launcherPng = File(destDir, "ic_launcher.png")
      ImageIO.write(resized, "png", launcherPng)
      
      val launcherRoundPng = File(destDir, "ic_launcher_round.png")
      ImageIO.write(resized, "png", launcherRoundPng)

      println("Written $size x $size icons to $folder")
    }
  }

  private fun resizeImage(src: BufferedImage, width: Int, height: Int): BufferedImage {
    val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g: Graphics2D = resized.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.drawImage(src, 0, 0, width, height, null)
    g.dispose()
    return resized
  }
}

