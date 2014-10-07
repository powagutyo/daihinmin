

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * 描画用パネル
 * @author satoru
 *
 */
public class DrawingPanel extends JPanel {
	/**
	 * バックで描画するイメージバッファ―
	 */
	private BufferedImage image;
	static final long serialVersionUID = 0L;

	/**
	 * コンストラクタ
	 * @param image イメージバッファ
	 */
	DrawingPanel(BufferedImage image) {
		this.image = image;
	}

	/**
	 * 表示ルーチン
	 * イメージバッファの内容を全面的にコピーする
	 */
	@Override
	public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
	}

	/**
	 * イメージバッファの設定
	 * @param image イメージバッファ
	 */
	void setImage(BufferedImage image) {
		this.image = image;
	}
}