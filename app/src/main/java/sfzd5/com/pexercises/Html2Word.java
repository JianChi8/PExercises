package sfzd5.com.pexercises;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by fsp on 17-11-15.
 */

public class Html2Word {
    public void htmlToWord2(String html, File wordFile) throws Exception {
        //拼一个标准的HTML格式文档
        String content = "<html><body>" + html + "</body></html>";
        InputStream is = new ByteArrayInputStream(content.getBytes("GBK"));
        OutputStream os = new FileOutputStream(wordFile);
        inputStreamToWord(is, os);
    }

    /**
     * 把is写入到对应的word输出流os中
     * 不考虑异常的捕获，直接抛出
     * @param is
     * @param os
     * @throws IOException
     */
    private void inputStreamToWord(InputStream is, OutputStream os) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem();
        //对应于org.apache.poi.hdf.extractor.WordDocument
        fs.createDocument(is, "WordDocument");
        fs.writeFilesystem(os);
        os.close();
        is.close();
    }

    /**
     * 把输入流里面的内容以UTF-8编码当文本取出。
     * 不考虑异常，直接抛出
     * @param ises
     * @return
     * @throws IOException
     */
    private String getContent(InputStream... ises) throws IOException {
        if (ises != null) {
            StringBuilder result = new StringBuilder();
            BufferedReader br;
            String line;
            for (InputStream is : ises) {
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line=br.readLine()) != null) {
                    result.append(line);
                }
            }
            return result.toString();
        }
        return null;
    }
}
