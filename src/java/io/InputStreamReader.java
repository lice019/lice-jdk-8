
package java.io;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import sun.nio.cs.StreamDecoder;


/**
 * InputStreamReader是从字节流到字符流的桥：它读取字节，并使用指定的charset将其解码为字符 。 它使用的字符集可以由名称指定，也可以被明确指定，或者可以接受平台的默认字符集。
 * 每个调用InputStreamReader的read（）方法之一可能会导致从底层字节输入流读取一个或多个字节。 为了使字节有效地转换为字符，可以从底层流读取比满足当前读取操作所需的更多字节。
 * <p>
 * 为了最大的效率，请考虑在BufferedReader中包装一个InputStreamReader。 例如：
 * <p>
 * BufferedReader in
 * = new BufferedReader(new InputStreamReader(System.in));
 */

public class InputStreamReader extends Reader {

    //InputStreamReader为字节流和字符流的桥梁，主要有流的解码器操作转换
    //流解码器----StreamDecoder是sun包下NIO的
    private final StreamDecoder sd;

    //创建一个使用默认字符集的InputStreamReader。传入字节流，以字节为单位来读取，再根据相应的字符编码转成字符
    public InputStreamReader(InputStream in) {
        super(in);
        try {
            sd = StreamDecoder.forInputStreamReader(in, this, (String) null); // ## check lock object
        } catch (UnsupportedEncodingException e) {
            // The default encoding should always be available
            throw new Error(e);
        }
    }

    //创建一个使用给定字符集的InputStreamReader。
    public InputStreamReader(InputStream in, String charsetName)
            throws UnsupportedEncodingException {
        super(in);
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        sd = StreamDecoder.forInputStreamReader(in, this, charsetName);
    }

    //创建一个使用命名字符集的InputStreamReader。
    public InputStreamReader(InputStream in, Charset cs) {
        super(in);
        if (cs == null)
            throw new NullPointerException("charset");
        sd = StreamDecoder.forInputStreamReader(in, this, cs);
    }

    //创建一个使用给定字符集解码器的InputStreamReader。
    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        if (dec == null)
            throw new NullPointerException("charset decoder");
        sd = StreamDecoder.forInputStreamReader(in, this, dec);
    }

    //返回指定的字符编码
    public String getEncoding() {
        return sd.getEncoding();
    }


    public int read() throws IOException {
        //流解码器来读取
        return sd.read();
    }


    public int read(char cbuf[], int offset, int length) throws IOException {
        return sd.read(cbuf, offset, length);
    }


    public boolean ready() throws IOException {
        return sd.ready();
    }

    public void close() throws IOException {
        sd.close();
    }
}
