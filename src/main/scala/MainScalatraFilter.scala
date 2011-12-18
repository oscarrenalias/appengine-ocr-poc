package net.renalias.appengine.ocr

import org.scalatra._

import com.google.appengine.api.conversion.Asset;
import com.google.appengine.api.conversion.Conversion;
import com.google.appengine.api.conversion.ConversionResult;
import com.google.appengine.api.conversion.ConversionService;
import com.google.appengine.api.conversion.ConversionServiceFactory;
import com.google.appengine.api.conversion.Document;
import org.scalatra.fileupload.FileUploadSupport
import org.apache.commons.io.IOUtils
import scala.collection.JavaConversions._
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.FileItemStream
import javax.servlet.http.HttpServletRequest

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
    
    def handleUpload(req: HttpServletRequest): Option[(String, Array[Byte])] = {
      val upload = new ServletFileUpload();
      val iterator = upload.getItemIterator(req);

      // TODO: quick and dirty for now, but what if there's nothing to iterate?
      val item = iterator.next();

      if (item.isFormField())
        None
      else
        Some((item.getName, IOUtils.toByteArray(item.openStream())))
    }

    handleUpload(this.request).map({case (fileName, fileContent)=>
      // note: we use Commons' IOUtils to make our life easier to convert the input stream to byte[]
      val asset = new Asset(fileType(fileExt(fileName)), fileContent, fileName)
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
