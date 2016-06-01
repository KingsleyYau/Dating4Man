package com.qpidnetwork.dating.livechat.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.BgItem;
import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.BgLocaType;
import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.IconItem;
import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.IconLocaType;
import com.qpidnetwork.dating.livechat.theme.ThemeConfigItem.MotionLocaType;
import com.qpidnetwork.manager.FileCacheManager;

/**
 * @author Yanni
 * 
 * @version 2016-4-19
 */
public class ThemeParse {
	
	private static final String CONFIG_FILE_NAME = "configure.xml";
	
	public static ThemeConfigItem parseThemeConfig(Context context, String themeId){
		ThemeConfigItem themeConfig = null;
		String rootPath = FileCacheManager.getInstance().getThemeSavePath() + themeId + "/";
		String configFilePath = rootPath + CONFIG_FILE_NAME;
		if(!TextUtils.isEmpty(configFilePath) && (new File(configFilePath).exists())){
			File configFile = new File(configFilePath);
			try {
				InputStream in = new FileInputStream(configFile);
				themeConfig = localParseThemeConfig(themeId, rootPath, in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return themeConfig;
	}

	/**
	 * 解析配置文件 转换为ThemeConfigItem对象
	 * 
	 * @param inputStream
	 * @return
	 */
	private static ThemeConfigItem localParseThemeConfig(String themeId, String rootpath, InputStream inputStream) {
		ThemeConfigItem themeConfigItem = new ThemeConfigItem();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);

			Element root = document.getDocumentElement();

			NodeList configure = root.getElementsByTagName("dpi");
			themeConfigItem.mDpi = Double
					.valueOf(configure.item(0).getTextContent());

			NodeList background = root.getElementsByTagName("background");
			NodeList bgChilds = background.item(0).getChildNodes();
			for (int i = 0; i < bgChilds.getLength(); i++) {
				if (bgChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if ("color".equals(bgChilds.item(i).getNodeName())) {
						themeConfigItem.mColor = Color.parseColor(bgChilds.item(i)
								.getTextContent());
					} else if ("img".equals(bgChilds.item(i).getNodeName())) {
						BgItem bgItem = new BgItem();
						NodeList imgs = bgChilds.item(i).getChildNodes();
						for (int j = 0; j < imgs.getLength(); j++) {
							if ("loca".equals(imgs.item(j).getNodeName())) {
								bgItem.mLoca = BgLocaType.valueOf(imgs.item(j).getTextContent().toUpperCase());
							} else if ("path".equals(imgs.item(j).getNodeName())) {
								bgItem.mFilePath = rootpath + imgs.item(j).getTextContent();
							}
						}
						themeConfigItem.mBgImageList.add(bgItem);
					}
				}
			}

			NodeList icon = root.getElementsByTagName("icon");
			NodeList iconChilds = icon.item(0).getChildNodes();
			for (int i = 0; i < iconChilds.getLength(); i++) {
				if (iconChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if ("img".equals(iconChilds.item(i).getNodeName())) {
						IconItem iconItem = new IconItem();
						NodeList imgs = iconChilds.item(i).getChildNodes();
						for (int j = 0; j < imgs.getLength(); j++) {
							if ("loca".equals(imgs.item(j).getNodeName())) {
								iconItem.mLoca = IconLocaType.valueOf(imgs
										.item(j).getTextContent()
										.replace("_", "").toUpperCase());
							} else if ("path".equals(imgs.item(j).getNodeName())) {
								iconItem.mFilePath = rootpath + imgs.item(j).getTextContent();
							}
						}
						themeConfigItem.mIconList.add(iconItem);
					}
				}
			}

			NodeList motions = root.getElementsByTagName("motion");
			NodeList motionChilds = motions.item(0).getChildNodes();
			String motionImagePath = "";
			int motionImageBegin = 0;
			String motionImageDefault = "";
			int motionImageDigit = 0;
			for (int i = 0; i < motionChilds.getLength(); i++) {
				if (motionChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if ("loca".equals(motionChilds.item(i).getNodeName())) {
						themeConfigItem.mMotionLoca = MotionLocaType
								.valueOf(motionChilds.item(i).getTextContent()
										.toUpperCase());
					} else if ("frame".equals(motionChilds.item(i)
							.getNodeName())) {
						themeConfigItem.mMotionFrame = Integer.valueOf(motionChilds
								.item(i).getTextContent());
					} else if ("repeat".equals(motionChilds.item(i)
							.getNodeName())) {
						themeConfigItem.mMotionRepeat = Integer.valueOf(motionChilds
								.item(i).getTextContent());
					} else if ("path"
							.equals(motionChilds.item(i).getNodeName())) {
						motionImagePath = motionChilds.item(i).getTextContent();
					} else if ("begin".equals(motionChilds.item(i)
							.getNodeName())) {
						motionImageBegin = Integer.valueOf(motionChilds.item(i)
								.getTextContent());
					} else if ("default".equals(motionChilds.item(i)
							.getNodeName())) {
						motionImageDefault = motionChilds.item(i).getTextContent();
					} else if ("digit".equals(motionChilds.item(i)
							.getNodeName())) {
						motionImageDigit = Integer.valueOf(motionChilds.item(i)
								.getTextContent());
					}
				}
			}
			
			String tempPath = null;

			for (int i = motionImageBegin; i < Math.pow(10, motionImageDigit); i++) {
				tempPath = getPath(i, motionImageDigit, motionImageDefault,
						rootpath, motionImagePath);
				if (!exists(tempPath)) {
					break;
				}
				// 加入队列中
				themeConfigItem.mMotionFiles.add(tempPath);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		

		return themeConfigItem;
	}

	/**
	 * 拼接动画Image路径
	 * 
	 * @param i
	 * @return
	 */
	private static String getPath(int i, int digit, String def, String rootPath, String motionPath) {
		String imgPath = String.valueOf(i);
		while (imgPath.length() < digit) {
			imgPath = def + imgPath;
		}
		return rootPath + motionPath + imgPath + ".png";
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param pt
	 * @return
	 */
	private static boolean exists(String iconPath) {
		File file = new File(iconPath);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

}
