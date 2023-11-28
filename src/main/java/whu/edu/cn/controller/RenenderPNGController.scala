package whu.edu.cn.controller

import geotrellis.layer.SpatialKey
import geotrellis.raster._
import geotrellis.raster.render.Png
import geotrellis.raster.{DoubleConstantNoDataCellType, MultibandTile}
import geotrellis.store.{AttributeStore, LayerId, Reader, ValueNotFoundError, ValueReader}
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, ResponseBody, RestController}



/**
 * 渲染栅格影像
 */
@RestController
class RenenderPNGController {


  @org.springframework.beans.factory.annotation.Value("${outputPath}")
  var outputPath: String = _

  /** raster transformation to perform at request time */
  def rasterFunction(multibandTile: MultibandTile): MultibandTile = {
    multibandTile.convert(DoubleConstantNoDataCellType)
  }


  /**
   *
   *
   * @param layerId id
   * @param zoom 缩放级别
   * @param x x
   * @param y y
   * @param a 占位符
   * @param b 占位符
   * @param c 占位符
   * @return
   */
  @RequestMapping(value = Array("{layerId}/{zoom}/{x}/{y}.png/{a}/{b}/{c}.png"), produces = Array(MediaType.IMAGE_PNG_VALUE))
  @ResponseBody
  def renderBean(@PathVariable layerId: String, @PathVariable zoom: Int, @PathVariable x: Int, @PathVariable y: Int,@PathVariable a: Int, @PathVariable b: Int, @PathVariable c: Int): Array[Byte] = {

    val catalogPath = new java.io.File(outputPath).toURI
    // 创建存储区
    val attributeStore: AttributeStore = AttributeStore(catalogPath)
    // 创建valuereader，用来读取每个tile的value
    val valueReader: ValueReader[LayerId] = ValueReader(attributeStore, catalogPath)

    val tileOpt: Option[MultibandTile] =
      try {
        val reader: Reader[SpatialKey, MultibandTile] = valueReader.reader[SpatialKey, MultibandTile](LayerId(layerId, zoom))
        Some(reader.read(x, y))
      } catch {
        case _: ValueNotFoundError =>
          None
      }


    var png: Png = null


    for (tile <- tileOpt) yield {
      val product: MultibandTile = rasterFunction(tile)
      val bandCount: Int = product.bandCount
      if (bandCount == 1) {
        png = product.band(0).renderPng()
      }
      else if (bandCount == 2) {
        png = MultibandTile(product.bands.take(2)).renderPng()
      }
      else if (bandCount == 3) {
        png = MultibandTile(product.bands.take(3)).renderPng()
      }
      else {
        throw new RuntimeException("波段数量不是1或3，无法渲染！")
      }

    }

    png.bytes

  }


  /**
   *
   *
   * @param layerId id
   * @param zoom    缩放级别
   * @param x       x
   * @param y       y
   * @param a       占位符
   * @param b       占位符
   * @param c       占位符
   * @return
   */
  @RequestMapping(value = Array("{layerId}/{zoom}/{x}/{y}.png"), produces = Array(MediaType.IMAGE_PNG_VALUE))
  @ResponseBody
  def renderBean_(@PathVariable layerId: String, @PathVariable zoom: Int, @PathVariable x: Int, @PathVariable y: Int): Array[Byte] = {

    val catalogPath = new java.io.File(outputPath).toURI
    // 创建存储区
    val attributeStore: AttributeStore = AttributeStore(catalogPath)
    // 创建valuereader，用来读取每个tile的value
    val valueReader: ValueReader[LayerId] = ValueReader(attributeStore, catalogPath)

    val tileOpt: Option[MultibandTile] =
      try {
        val reader: Reader[SpatialKey, MultibandTile] = valueReader.reader[SpatialKey, MultibandTile](LayerId(layerId, zoom))
        Some(reader.read(x, y))
      } catch {
        case _: ValueNotFoundError =>
          None
      }


    var png: Png = null


    for (tile <- tileOpt) yield {
      val product: MultibandTile = rasterFunction(tile)
      val bandCount: Int = product.bandCount
      if (bandCount == 1) {
        png = product.band(0).renderPng()
      }
      else if (bandCount == 2) {
        png = MultibandTile(product.bands.take(2)).renderPng()
      }
      else if (bandCount == 3) {
        png = MultibandTile(product.bands.take(3)).renderPng()
      }
      else {
        throw new RuntimeException("波段数量不是1或3，无法渲染！")
      }

    }

    png.bytes

  }

}


