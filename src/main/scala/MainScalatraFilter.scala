package net.renalias.appengine.ocr

import org.scalatra._

import com.google.appengine.api.conversion.Asset;
import com.google.appengine.api.conversion.Conversion;
import com.google.appengine.api.conversion.ConversionServiceFactory;
import com.google.appengine.api.conversion.Document;
import org.apache.commons.io.IOUtils
import scala.collection.JavaConversions._
import org.apache.commons.fileupload.servlet.ServletFileUpload
import javax.servlet.http.HttpServletRequest
import collection.mutable.HashMap

class MainScalatraFilter extends ScalatraFilter {
  
  get("/") {
    <html>
      <body>
        <h1>Upload</h1>
        <div id="upload-form">
          <form class="form" method="POST" action="/upload" enctype="multipart/form-data">
            <p><input type="file" name="fileToScan" value="" /></p>
            <p><input type="submit" name="Scan" value="Scan" /></p>
          </form>
        </div>
      </body>
    </html>
  }

  post("/upload") {

    def fileExt(name:String) = {
      name.lastIndexOf('.') match {
    			case -1 => ""
    			case x:Int if x==(name.length-1) => ""
    			case x:Int => name.substring(x+1).toLowerCase
    		}
    }

    val fileType = Map("png" -> "image/png", "jpg" -> "image/jpg", "jpeg" -> "image/jpeg") // TODO: what happens if the extension is none of these?

    /**
     * Scalatra's file upload capabilities depend on Commons' FileUpload, which is not fully supported
     * in GAE, so we need to find our way around that...
     */
    case class FileUpload(name:String, size:Int, data:Array[Byte])

    def handleUpload(req: HttpServletRequest): scala.collection.mutable.Map[String, FileUpload] = {
      val upload = new ServletFileUpload();
      val iterator = upload.getItemIterator(req);
      val results = new HashMap[String, FileUpload]()

      while(iterator.hasNext) { // TODO: we can probably Scala-ify this bit...
        val item = iterator.next();
    
        if (!item.isFormField()) {
          val contents = IOUtils.toByteArray(item.openStream())
          results += (item.getFieldName -> FileUpload(item.getName, contents.size, contents))
        }
      }

      results
    }

    handleUpload(this.request).get("fileToScan").map({fileData =>
      // note: we use Commons' IOUtils to make our life easier to convert the input stream to byte[]
      val asset = new Asset(fileType(fileExt(fileData.name)), fileData.data, fileData.name)
      val document = new Document(asset)
      val conversion = new Conversion(document, "text/html")
      val result = ConversionServiceFactory.getConversionService().convert(conversion)

      if(result.success()) {
            <html>
              <body>
                <p>Conversion results:</p>
                <hr/>
                <p>
                  {result.getOutputDoc.getAssets.mkString("")}
                </p>
              </body>
            </html>
      }
      else {
            <html>
              <body>
                <p>There was an error performing the conversion:</p>
                <hr/>
                <p>
                  {result.getErrorCode}
                </p>
              </body>
            </html>
      }
    }).getOrElse("No file was uploaded")
  }

  notFound {
    redirect("/")
  }
}
