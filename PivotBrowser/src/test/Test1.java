package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import model.NeteaseVedioData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Test1 {

	public static List<NeteaseVedioData> parseData(File file) {
		try {
			// 返回值
			List<NeteaseVedioData> result;
			// dom
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);

			// xpath解析xml文件
			XPath xpath = XPathFactory.newInstance().newXPath();

			// 获取数据
			result = retrieveContents(xpath, doc, "//orz/item");

			// 构造NeteaseVedioData对象
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ArrayList<NeteaseVedioData> retrieveContents(XPath xpath,
			Document document, String xPathQuery) {
		try {
			XPathExpression expression = xpath.compile(xPathQuery);
			NodeList nodes = (NodeList) expression.evaluate(document,
					XPathConstants.NODESET);

			ArrayList<NeteaseVedioData> contents = new ArrayList<NeteaseVedioData>();
			for (int i = 0; i < nodes.getLength(); i++) {
				NeteaseVedioData data = new NeteaseVedioData();
				Node item = nodes.item(i);

				// 创建数据
				NodeList children = item.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node n = children.item(j);
					String nodeName = n.getNodeName();
					if (nodeName.equals("title")) {
						data.setTitle(n.getTextContent());
					} else if (nodeName.equals("description")) {
						data.setDescription(n.getTextContent());
					} else if (nodeName.equals("tags")) {
						data.setTags(n.getTextContent());
					} else if (nodeName.equals("videourl")) {
						data.setVideourl(n.getTextContent());
					} else if (nodeName.equals("snapshot")) {
						data.setSnapshot(n.getTextContent());
					}
				}
				contents.add(data);
			}
			return contents;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		File file = new File("orz.xml");
		List<NeteaseVedioData> list = parseData(file);
		
		for(NeteaseVedioData vedioData : list) {
			System.out.println(vedioData.getTitle());
			System.out.println(vedioData.getDescription());
			System.out.println(vedioData.getTags());
			System.out.println(vedioData.getSnapshot());			
			System.out.println(vedioData.getVideourl());
			System.out.println("");
		}
	}
}
