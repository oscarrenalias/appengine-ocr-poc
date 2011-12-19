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
        <h1>Google App Engine OCR PoC</h1>
        <p>
          The following files are currently supported: JPEG, PNG and PDF. The target format for the conversion
          is always plain text, and will appear in an editable text area.
        </p>
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

    val fileType = Map(
      "pdf" -> "application/pdf",
      "png" -> "image/png",
      "jpg" -> "image/jpeg",
      "jpeg" -> "image/jpeg",
      "txt" -> "text/plain"
    ) // TODO: this will dump if the given file does not have a valid extension

    /**
     * Scalatra's file upload capabilities depend on Commons' FileUpload, which is not fully supported
     * in GAE so we need to find our way around it. More information: http://code.google.com/appengine/kb/java.html#fileforms
     */
    case class FileUpload(name:String, size:Int, data:Array[Byte])

    def handleUpload(req: HttpServletRequest): scala.collection.mutable.Map[String, FileUpload] = {
      val upload = new ServletFileUpload();
      val iterator = upload.getItemIterator(req);
      val results = new HashMap[String, FileUpload]()

      // FileItemIterator does not implement java.util.Iterator so we cannot convert to a Scala iterator... ain't that nice
      while(iterator.hasNext) {
        val item = iterator.next();
    
        if (!item.isFormField()) {
          // we use Commons' IOUtils to make our life easier to convert the input stream to byte[]
          val contents = IOUtils.toByteArray(item.openStream())
          results += (item.getFieldName -> FileUpload(item.getName, contents.size, contents))
        }
      }

      results
    }

    handleUpload(this.request).get("fileToScan").map({fileData =>
      val asset = new Asset(fileType(fileExt(fileData.name)), fileData.data, fileData.name)
      val document = new Document(asset)
      val conversion = new Conversion(document, "text/plain")
      val result = ConversionServiceFactory.getConversionService().convert(conversion)

      if(result.success()) {
        <html>
          <body>
            <p>Conversion results for file <b>{fileData.name}</b>, of type <b>{fileType(fileExt(fileData.name))}</b>:</p>
            <hr/>
            <textarea rows="20" cols="80">
              { // asset.getData returns Array[Byte], so we need to turn that back into a String
                result.getOutputDoc.getAssets.map({asset=>new String(asset.getData)}).mkString("")
              }
            </textarea>
            <hr/>
            <p>
              <a href="/">Try again</a>
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
            <p>
              <a href="/">Try again</a>
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
