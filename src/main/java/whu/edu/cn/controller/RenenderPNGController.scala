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



  /** raster transformation to perform at request time */
  def rasterFunction(multibandTile: MultibandTile): MultibandTile = {
    multibandTile.convert(DoubleConstantNoDataCellType)
  }

  @RequestMapping(value = Array("{layerId}/{zoom}/{x}/{y}.png"), produces = Array(MediaType.IMAGE_PNG_VALUE))
  @ResponseBody
  def renderBean_(@PathVariable layerId: String, @PathVariable zoom: Int, @PathVariable x: Int, @PathVariable y: Int): Array[Byte] = {


    val outputPath = "Path of data" // for example: "/Users/tankenqi/Downloads/data"

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


