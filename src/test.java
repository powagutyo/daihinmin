import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;



public class test {
	public static void main(String[] args) {
		test reader = new test();

		String str = "0105999999299999999999999999992999999999990999999929299990000001011010210000000000333332000";
		byte[] bytess = null;
		bytess = str.getBytes(Charset.forName("UTF-8"));

		test writer = new test();
		writer.write(bytess);

		str = "10549,0.02877341079998017,0.031337739934985266,0.012260487158994842,-0.03375879738058256,0.014168777629598218,-0.023066930872668683,-0.04468145620389239,-0.005766725446434326,-0.005301385050583537,0.024172675393805493,1.5409530822605008E-4,0.014906394670188922,-0.009080543234701526,0.009747340203779353,-0.025463318372464353,3.402969407459715E-16,-0.053654624663830405,-0.012699618470088478,-0.004106867559428939";
		bytess = str.getBytes(Charset.forName("UTF-8"));
		writer.write(bytess);

		byte[][] b = read();
		System.out.println(b);
		String xx ="";

		System.out.println(xx);
	}

	/**
	 * バイナリデータをファイルに書き込みます。
	 *
	 * @param bytess
	 */
	public void write(byte[] bytess) {
		BufferedOutputStream fis = null;
		try {
			// 出力先ファイル
			File file = new File("./test.jpg");

			fis = new BufferedOutputStream(new FileOutputStream(file,true));
			fis.write(bytess);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					// ストリームは必ず finally で close します。
					fis.close();
				}
			} catch (IOException e) {
			}
		}
	}

    /**
     * バイナリファイルを読み込みます。
     *
     * @return 読み込んだデータをバイトの2次元配列で返します。
     */
    public static byte[][] read() {
        BufferedInputStream fis = null;
        List<byte[]> list = new ArrayList<byte[]>();
        try {
            // 入力元ファイル
            File file = new File("./test.jpg");

            fis = new BufferedInputStream(new FileInputStream(file));

            int avail;
            // 読み込み可能なバイト数づつ読み込む
            while ((avail = fis.available()) > 0) {
                byte[] bytes = new byte[avail];
                fis.read(bytes);

                list.add(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    // ストリームは必ず finally で close します。
                    fis.close();
                }
            } catch (IOException e) {
            }
        }
        byte[][] result = new byte[list.size()][];
        list.toArray(result);

        return result;
    }
}
