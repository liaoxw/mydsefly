package com.myds.efly;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class EflyServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {		
		System.out.println(req.getQueryString());
		resp.setContentType("text/html");
		resp.setCharacterEncoding("utf-8");
		PrintWriter out = resp.getWriter();
		try{
			Params.MODEL model = Params.MODEL.valueOf(req.getParameter("model"));
			String hotelId = req.getParameter("hotel_id");
			switch(model){
			case CITY:
				break;
			case HOTEL_IMAGE:
				this.anlysisHotelImage(out, hotelId);
				break;
			case HOTEL_INFO:
				this.anlysisHotelInfo(out,hotelId);
				break;
			case HOTEL_ROOM_STATUS:
				this.anlysisRoomStatus(out,hotelId, req.getParameter("type"), req.getParameter("year"), req.getParameter("month"));
				break;
			case HOTEL_ROOM_INFO:
				this.anlysisHotelRoom(out, hotelId);
				break;
			case HOTEL_DESC:
				this.anlysisHotelDesc(out, hotelId);
				break;
			default:
				out.print("null");
				break;
			}
		}catch(Exception e){
			resp.getWriter().println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	private String getContent(String url) throws Exception{
		HTTPResponse response = URLFetchServiceFactory.getURLFetchService().fetch(new URL(url));
		byte[] data = response.getContent();
		if(data != null){
			return new String(data,Charset.forName("utf-8"));
		}
		return null;
	}
	
	/**
	 * 分析酒店资料
	 * @throws Exception
	 */
	private void anlysisHotelInfo(PrintWriter out,String hotelId) throws Exception{
		//简介				
		String url = "http://www.eztravel.com.tw/ezec/htl_tw/htltw_htl_detail.jsp?prod_no="+hotelId;
		String content = this.getContent(url);
		JSONObject json = new JSONObject();
		if (content != null) {			
			Document doc = Jsoup.parse(content);
			Element name = doc.select("td.green_big").first();
			if(name == null) return;
			json.put("name", name.text());
			Elements greenTxt = doc.select("td.green_txt");
			String address = greenTxt.get(1).text();
			json.put("address", address.replaceAll("地址：", ""));
			String telphone = greenTxt.get(2).text();
			json.put("telphone", telphone.replaceAll("電話：", ""));
			String[] checkTimes = greenTxt.get(3).text().split(" ");
			String checkInTime ="";
			if(checkTimes[0].split("：").length>1){
				checkInTime = checkTimes[0].split("：")[1];
			}
			json.put("checkInTime", checkInTime);
			String checkOutTime = checkTimes[1].split("：")[1];
			json.put("checkOutTime", checkOutTime);
			String sendPickService = greenTxt.get(4).text();
			json.put("sendPickService", sendPickService.replaceAll("接送服務：", ""));
			String website = greenTxt.get(5).text();
			json.put("website", website.replaceAll("飯店網址：", ""));
			Elements txt = doc.select("td.txt");
			String roomFacility = txt.get(2).text();
			json.put("roomFacility", roomFacility);
			String diningFacility = txt.get(4).text();
			json.put("diningFacility", diningFacility);
			String leisureFacility = txt.get(6).text();
			json.put("leisureFacility", leisureFacility);
			String transportationInfo = txt.get(8).text();
			json.put("transportationInfo", transportationInfo);
			String sendcancelPolicy = txt.get(10).text();
			json.put("sendcancelPolicy", sendcancelPolicy.replaceAll("取消訂房相關規定：", ""));
			String relatedExplanation = txt.get(12).text();
			json.put("relatedExplanation", relatedExplanation);			
		}
		out.println(json.toString());		
	}
	
	private void anlysisHotelImage(PrintWriter out,String hotelId) throws Exception{
		//简介				
		String url = "http://www.eztravel.com.tw/ezec/htl_tw/htltw_puppic.jsp?prod_no="+hotelId;
		String content = this.getContent(url);
		JSONArray json = new JSONArray();
		if (content != null) {
			String regEx = "http://www.eztravel.com.tw/ss_static/images/htl/more/"+hotelId+"_m([0-9][0-9]|[0-9]).(gif|jpg)";
			Pattern p = Pattern.compile(regEx,Pattern.DOTALL);
			Matcher m = p.matcher(content);
			while(m.find()){
				String[] s = m.group().split("/");
				JSONObject img = new JSONObject();
				img.put("filename", s[s.length-1]);
				img.put("sourceUrl", m.group());
				img.put("hotelId", hotelId);
				json.put(img);
			}
		}
		out.println(json.toString());
	}
	
	private void anlysisRoomStatus(PrintWriter out,String hotelId,String type,String year,String month) throws Exception{
		String url = "http://www.eztravel.com.tw/ezec/htl_tw/htltw_month_room_np.jsp?prod_no="+hotelId+"&cond1_type="+type+"&year="+year+"&month="+month;
		String content = this.getContent(url);
		JSONArray json = new JSONArray();
		if(content != null){
			Document doc = Jsoup.parse(content);
			Elements td = doc.select("td");
			for(int i=0;i<td.size();i++){				
				Elements p = td.get(i).select("p");
				if(p.size() > 0){
					JSONObject obj = new JSONObject();
					String price = p.get(1).text().replaceAll("元", "").replaceAll(",", "");
					String count =  p.get(2).text();
					if(count.equals("額滿")){
						obj.put("count",0);
					}else{
						obj.put("count", count.replaceAll("餘", "").replaceAll("間", ""));
					}
					String day = p.get(0).text();
					obj.put("day", day);
					obj.put("price", price);
					obj.put("month",month);
					obj.put("date", year+"/"+month+"/"+day);
					json.put(obj);
				}
			}
		}
		out.println(json.toString());
	}
	
	private void anlysisHotelRoom(PrintWriter out,String hotelId) throws Exception{
		String url = "http://www.eztravel.com.tw/ezec/htl_tw/htltw_room_desc.jsp?prod_no="+hotelId;
		String content = this.getContent(url);
		JSONArray json = new JSONArray();
		if(content != null){
			Document doc = Jsoup.parse(content);
			Elements table = doc.select("table.tb-1");
			for(int i=0;i<table.size();i++){				
				Elements txt = table.get(i).select(".txt-s2");
				//String id = txt.get(0).text();
				if(txt.size()<2) return;
				String roomName = txt.get(1).text();
				String summary = txt.get(2).text();
				String[] ps = txt.get(3).select(".txt-or").first().text().replaceAll(",", "").replaceAll("元起","").split(" ");
				String price = ps[0];
				String priceX = "";
				if(ps.length > 1) priceX = ps[1];
				String roomType = txt.get(4).select("a.listmore-link").first().attr("onclick").substring(69,72);
				String intro = txt.get(5).html();
//				Element projs = txt.get(5).select(".txt-or").first();
//				String proj = "";
//				if(projs != null) proj = projs.text().replaceAll("【專案名稱】", "");
//				Elements intros = txt.get(5).select("p");
//				String intro = "";
//				if(intros.size()>2){
//					intro = intros.get(2).text();
//				}
				JSONObject obj = new JSONObject();
				obj.put("hotelId", hotelId);
				obj.put("intro", intro);
				obj.put("roomName", roomName);
				obj.put("price",price );
				obj.put("proj", "");
				obj.put("summary", summary);
				obj.put("priceX", priceX);
				obj.put("type", roomType);
				json.put(obj);
			}
		}
		out.println(json.toString());
	}
	
	private void anlysisHotelDesc(PrintWriter out,String hotelId) throws Exception{
		String url = "http://www.eztravel.com.tw/ezec/htl_tw/htltw_prod_desc.jsp?prod_no="+hotelId;
		String content = this.getContent(url);
		JSONObject json = new JSONObject();
		if(content != null){
			Document doc = Jsoup.parse(content);
			Elements intro = doc.select("div.intro p");
			if(intro.size() == 0) return;
			Element featuress = intro.get(0);
			String features = "";
			if(featuress != null){
				features = featuress.text().replaceAll("【飯店特色】", "");
			}
			Element evaluates = intro.get(6).select("img").first();
			String evaluate = "00";
			if(evaluates != null){
				evaluate = evaluates.attr("src");
				evaluate = evaluate.substring(evaluate.length()-6).replaceAll(".gif", "");
			}
			Element caption = doc.select("div.box-1 p").first();
			json.put("features", features);
			json.put("evaluate", evaluate);
			json.put("caption", caption.html());
			json.put("hotel_id", hotelId);
		}
		out.print(json.toString());
	}
}
