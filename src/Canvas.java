

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * キャンバス簡易表示のためのクラス
 * @author satoru
 */
public class Canvas {
	/**
	 * キャンバスに表示されるパネル
	 */
	private static DrawingPanel panel;

	/**
	 * 基本フレーム
	 */
	private static JFrame frame;

	/**
	 * キャンバスの幅
	 */
	private static int width = 500;

	/**
	 * キャンバスの高さ
	 */
	private static int height = 500;

	/**
	 * バックエンドに置かれる書き込みイメージバッファ
	 */
	private static BufferedImage image;

	/**
	 * イメージバッファへ書くための Graphics インスタンス
	 */
	private static Graphics g;

	/**
	 * ポイントされたX座標
	 */
	private static int pointedX;

	/**
	 * ポイントされたY座標
	 */
	private static int pointedY;

	/**
	 * 自動再描画を行うフラグ
	 */
	private static boolean repaintFlag = true;

	/**
	 * クラス初期化
	 */
	static {
		frame = new JFrame("Canvas");
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		panel = new DrawingPanel(image);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.setSize(width, height);
		g = image.getGraphics();
		g.clearRect(0, 0, width, height);
		g.setColor(new Color(255, 255,255));
		g.fillRect(0, 0, width, height);
	}

	/**
	 * キャンバスの表示
	 * @param w 幅
	 * @param h 高さ
	 */
	public static void show(int w, int h) {
		setSize(w, h);
		show();
	}

	/**
	 * キャンバスの表示
	 */
	public static void show() {
		frame.setVisible(true);
		panel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				pointedX = e.getX();
				pointedY = e.getY();

				synchronized(panel) {
					panel.notifyAll();
				}
			}

			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
	}

	/**
	 * キャンバスの画面クリア
	 */
	public static void clear() {
		g.clearRect(0, 0, width, height);
		g.setColor(new Color(255, 255,255));
		g.fillRect(0, 0, width, height);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * テキスト描画
	 * @param x X座標
	 * @param y Y座標
	 * @param text テキスト
	 */
	public static void drawString(double x, double y, Object text) {
		g.drawString(text.toString(), round(x), round(y));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * テキストの中央位置への描画
	 * @param x X座標
	 * @param y Y座標
	 * @param text テキスト
	 */
	public static void drawStringCenter(double x, double y, Object text) {
		FontMetrics metrics = g.getFontMetrics();
		int w = metrics.stringWidth(text.toString());
		//int h = metrics.getHeight();

		//g.drawString(text.toString(), round(x) - w / 2, round(y) + h / 2);
		g.drawString(text.toString(), round(x) - w / 2, round(y));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * テキストの右揃え(高さは中央揃え)の描画
	 * @param x X座標
	 * @param y Y座標
	 * @param text テキスト
	 */
	public static void drawStringRight(double x, double y, Object text) {
		FontMetrics metrics = g.getFontMetrics();
		int w = metrics.stringWidth(text.toString());
		//int h = metrics.getHeight();

		//g.drawString(text.toString(), round(x) - w, round(y) + h / 2);
		g.drawString(text.toString(), round(x) - w, round(y));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * テキストの左揃え(高さは中央揃え)の描画
	 * @param x X座標
	 * @param y Y座標
	 * @param text テキスト
	 */
	public static void drawStringLeft(double x, double y, Object text) {
		//FontMetrics metrics = g.getFontMetrics();
		//int h = metrics.getHeight();

		//g.drawString(text.toString(), round(x), round(y) + h / 2);
		g.drawString(text.toString(), round(x), round(y));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 線の描画
	 * @param x0 始点のX座標
	 * @param y0 始点のY座標
	 * @param x1 終点のX座標
	 * @param y1 終点のY座標
	 */
	public static void drawLine(double x0, double y0, double x1, double y1) {
		g.drawLine(round(x0), round(y0), round(x1), round(y1));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 線の描画
	 * @param x X座標
	 * @param y Y座標
	 */
	public static void drawPoint(double x, double y) {
		g.drawRect(round(x), round(y), 1, 1);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 長方形の描画
	 * @param x 左上のX座標
	 * @param y 左上のY座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public static void drawRect(double x, double y, double w, double h) {
		g.drawRect(round(x), round(y), round(w), round(h));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 長方形の塗りつぶし描画
	 * @param x 左上のX座標
	 * @param y 左上のY座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public static void fillRect(double x, double y, double w, double h) {
		g.fillRect(round(x), round(y), round(w), round(h));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 円の描画
	 * @param x 中心のX座標
	 * @param y 中心のY座標
	 * @param r 半径
	 */
	public static void drawCircle(double x, double y, double r) {
		int r2 = round(r * 2);
		g.drawOval(round(x - r), round(y - r), r2, r2);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 円の塗りつぶし描画
	 * @param x 中心のX座標
	 * @param y 中心のY座標
	 * @param r 半径
	 */
	public static void fillCircle(double x, double y, double r) {
		int r2 = round(r * 2);
		g.fillOval(round(x - r), round(y - r), r2, r2);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 楕円の描画
	 * @param x 左上のX座標
	 * @param y 左上のY座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public static void drawOval(double x, double y, double w, double h) {
		g.drawOval(round(x), round(y), round(w), round(h));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 楕円の塗りつぶし描画
	 * @param x 左上のX座標
	 * @param y 左上のY座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public static void fillOval(double x, double y, double w, double h) {
		g.fillOval(round(x), round(y), round(w), round(h));
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 三角形の描画
	 * @param x0 第一のX座標
	 * @param y0 第一のＹ座標
	 * @param x1 第二のX座標
	 * @param y1 第二のＹ座標
	 * @param x2 第三のX座標
	 * @param y2 第三のＹ座標
	 */
	public static void drawTriangle(double x0, double y0, double x1, double y1, double x2, double y2) {
		int[] xPoints = new int[] {
				round(x0), round(x1), round(x2)
		};

		int[] yPoints = new int[] {
				round(y0), round(y1), round(y2)
		};

		g.drawPolygon(xPoints, yPoints, xPoints.length);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 三角形の塗りつぶし描画
	 * @param x0 第一のX座標
	 * @param y0 第一のＹ座標
	 * @param x1 第二のX座標
	 * @param y1 第二のＹ座標
	 * @param x2 第三のX座標
	 * @param y2 第三のＹ座標
	 */
	public static void fillTriangle(double x0, double y0, double x1, double y1, double x2, double y2) {
		int[] xPoints = new int[] {
				round(x0), round(x1), round(x2)
		};

		int[] yPoints = new int[] {
				round(y0), round(y1), round(y2)
		};

		g.fillPolygon(xPoints, yPoints, xPoints.length);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 多角形の描画
	 * @param xPoints X座標の配列
	 * @param yPoints Y座標の配列
	 */
	public static void drawPoligon(int[] xPoints, int[] yPoints) {
		g.drawPolygon(xPoints, yPoints, xPoints.length);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 多角形の塗りつぶし描画
	 * @param xPoints X座標の配列
	 * @param yPoints Y座標の配列
	 */
	public static void fillPoligon(int[] xPoints, int[] yPoints) {
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * ファイルで指定した画像の描画
	 * @param x 左上のX座標
	 * @param y 左上のY座標
	 * @param fileName 画像ファイル名
	 */
	public static void drawImage(double x, double y, String fileName) {
		File file = new File(fileName);
		drawImage(round(x), round(y), file);
	}

	/**
	 * ファイルで指定した画像の描画
	 * @param x 左上のX座標
	 * @param y 左上のY座標
	 * @param file 画像ファイルオブジェクト
	 */
	public static void drawImage(double x, double y, File file) {
		try {
			BufferedImage im = ImageIO.read(file);
			g.drawImage(im, round(x), round(y), null);
			if(repaintFlag) {
				panel.repaint();
			}
		} catch (Exception e) {
			System.err.println(file.getName() + "はオープンできません");
		}
	}

	/**
	 * 色の設定
	 * @param red 赤の値
	 * @param green 緑の値
	 * @param blue 青の値
	 */
	public static void setColor(int red, int green, int blue) {
		g.setColor(new Color(red, green, blue));
	}

	/**
	 * 色の設定
	 * @param red 赤の値
	 * @param green 緑の値
	 * @param blue 青の値
	 * @param alpha アルファ成分の値
	 */
	public static void setColor(int red, int green, int blue, int alpha) {
		g.setColor(new Color(red, green, blue, alpha));
	}

	/**
	 * キャンバスのサイズ変更
	 * @param w キャンバスの幅
	 * @param h キャンバスの高さ
	 */
	public static void setSize(int w, int h) {
		frame.setSize(w, h);

		// バックグランドにある BufferedImage もサイズ変更する
		BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		g = newImage.getGraphics();

		// 古い内容のコピー
		g.drawImage(image, 0, 0, null);
		image = newImage;
		panel.setSize(w, h);
		panel.setImage(image);
		width = w;
		height = h;
		if(repaintFlag) {
			panel.repaint();
		}
	}

	/**
	 * 文字の大きさの変更
	 * @param size 文字サイズ
	 */
	public static void setFontSize(double size) {
		Font font = g.getFont();
		Font newFont = font.deriveFont((float)size);
		g.setFont(newFont);
	}

	/**
	 * マウス入力を待つメソッド
	 */
	public static void waitForPoint() {
		synchronized(panel) {
			try {
				panel.wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * メッセージを出して、マウス入力を待つメソッド
	 */
	public static void waitForPoint(String message) {
		JOptionPane.showMessageDialog(null, message);
		waitForPoint();
	}

	/**
	 * 指定した時間だけスリープする
	 * @param time スリープする時間(ミリ秒)
	 */
	public static void waitForCountdown(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}

	/**
	 * 自動再描画を無効する
	 */
	public static void disableAutoRepaint() {
		repaintFlag = false;
	}

	/**
	 * 自動再描画を有効にする
	 */
	public static void enableAutoRepaint() {
		repaintFlag = true;
	}

	/**
	 * 自動再描画の有効/無効を設定する
	 * @param flag 再描画の有効/無効を指定
	 */
	public static void setAutoRepaint(boolean flag) {
		repaintFlag = flag;
	}

	public static void forceRepaint() {
		panel.repaint();
	}

	/**
	 * ポイントされた X座標を返却
	 * @return X座標
	 */
	public static int getPointedX(){
		return pointedX;
	}

	/**
	 * ポイントされた Y座標を返却
	 * @return Y座標
	 */
	public static int getPointedY() {
		return pointedY;
	}

	/**
	 * 画像の保存
	 * @param file 保存先のファイルオブジェクト
	 */
	public static void save(File file) {
		try {
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 画像の保存
	 * @param fileName 保存先のファイル名
	 */
	public static void save(String fileName) {
		File file = new File(fileName);
		try {
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 画像の読み込み
	 * @param fileName 読み込みファイル名
	 */
	public static void load(String fileName) {
		drawImage(0, 0, fileName);
	}

	/**
	 * 画像の読み込み
	 * @param file 読み込みファイルオブジェクト
	 */
	public static void load(File file) {
		drawImage(0, 0, file);
	}

	private static int round(double x) {
		return (int)(x + 0.5);
	}
}

