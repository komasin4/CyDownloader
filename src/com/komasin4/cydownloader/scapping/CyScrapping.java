package com.komasin4.cydownloader.scapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.komasin4.cydownloader.common.Common;
import com.komasin4.cydownloader.model.Folder;
import com.komasin4.cydownloader.model.Post;
import com.komasin4.cydownloader.util.DescFieldUtil;
import com.komasin4.cydownloader.util.StringUtil;
import com.komasin4.cydownloader.util.UrlUtil;

import javafx.scene.control.TextArea;

public class CyScrapping extends Common {

	SimpleDateFormat formatYYYYMM = new SimpleDateFormat("yyyyMM");
	SimpleDateFormat formatYYYYMMDD = new SimpleDateFormat("yyyyMMdd");

	public String getTid(Map<String,String> loginCookie) throws Exception	{
		String tid = null;

		String connUrl =  "http://club.cyworld.com/club/clubsection2/home.asp";

		Document cyMain = Jsoup.connect(connUrl)
				.userAgent(userAgent)
				.header("Accept", accept)
				.header("Content-Type", content_type)
				.header("Accept-Encoding", accept_encoding)
				.header("Accept-Language", accept_language)
				.cookies(loginCookie) // 위에서 얻은 '로그인 된' 쿠키
				.get();

		//html에서 tid 추출
		//String tid = null;
		for(Element el:cyMain.select("script[type]"))	{
			String unescapedHtml = el.data();
			//int idx = unescapedHtml.indexOf("var tid=");
			int idx = unescapedHtml.indexOf("var statUserId = ");
			if(idx > -1)	{
				String tid_src = unescapedHtml.substring(idx);
				//tid = StringUtil.getVariable(tid_src, "tid");
				tid = StringUtil.getVariable(tid_src, "statUserId");
			}
		}

		return tid;
	}

	/*
	public void getScrappingStart(Map<String,String> loginCookie, String tid, TextArea descField)	{

		descField.appendText("\n폴더 리스트를 가져오는 중....\n");

		List<Folder> folderList = new ArrayList<Folder> (); 

		try {
			folderList = getFolderList(loginCookie, tid, descField);

			descField.appendText("총 " + folderList.size() + "개의 폴더가 있습니다.\n");

		} catch (Exception e)	{
			descField.appendText(e.getMessage() + "\n" + "폴더 목록을 가져오는 중 오류가 발생하여 사진을 가져올 수 없습니다.\n");
			return;
		}

		if(folderList == null || folderList.size() < 1)	{
			descField.appendText("사진폴더가 없습니다.\n");
			return;
		}

		descField.appendText("\n사진을 가져옵니다....\n");

		for(Folder folder:folderList)	{
			String folderName = folder.getDepth1Name() + "/" + folder.getDepth2Name() + "/" + folder.getName();
			descField.appendText("\n" + folderName + " 폴더의 사진을 가져옵니다.\n");

			try {
				getPhoto(loginCookie, tid, folder, descField);
			} catch (Exception e) {
				descField.appendText(e.getMessage() + "\n" + folderName + " 폴더의 사진을 가져오는 중 오류가 발생하였습니다.\n");
			}
		}



	}

	 */

	public List<Folder> getFolderList(Map<String,String> loginCookie, String tid, TextArea descField) throws Exception	{


		//JOptionPane.showMessageDialog(null, "폴더리스트가져오기");

		List<Folder> folderList = new ArrayList<Folder>();

		String folderListString = "";

		Document cyHome = Jsoup.connect("http://cy.cyworld.com/home/" + tid + "/menu/?type=folder")
				.userAgent(userAgent)
				.header("Accept", accept)
				.header("Content-Type", content_type)
				.header("Accept-Encoding", accept_encoding)
				.header("Accept-Language", accept_language)
				.cookies(loginCookie) // 위에서 얻은 '로그인 된' 쿠키
				.ignoreHttpErrors(true).validateTLSCertificates(false).followRedirects(true)
				.get();

		String depth1Name = "";
		String depth2Name = "";

		for(Element el:cyHome.select("label"))	{

			String depth = el.getElementsByTag("input").attr("name");

			if(depth != null && depth.equals("depth1"))
				depth1Name = el.getElementsByTag("em").text();

			if(depth != null && depth.equals("depth2"))
				depth2Name = el.getElementsByTag("em").text();

			//for(Element el:cyHome.select("label[class=menuD03]"))	{
			//Folder folder = new Folder(el.getElementsByTag("input").attr("value"), el.getElementsByTag("em").text(), el.getElementsByTag("input").attr("name"));
			if(depth != null && depth.equals("depth3"))	{
				Folder folder = new Folder(el.getElementsByTag("input").attr("value"), el.getElementsByTag("em").text(), el.getElementsByTag("input").attr("name"), depth1Name, depth2Name);
				folderList.add(folder);
				DescFieldUtil.AppendString(descField, "\t" + folder.getDepth1Name() + "/" + folder.getDepth2Name() + "/" + folder.getName());
				//descField.appendText(folder.getDepth1Name() + "/" + folder.getDepth2Name() + "/" + folder.getName() + "\n");
				//System.out.println(folder.getDepth1Name() + "/" + folder.getDepth2Name() + "/" + folder.getName() + "\n");
			}
		}

		return folderList;

	}

	//	public void getPhoto(Map<String,String> loginCookie, String tid, Folder folder, TextArea descField)	{
	public List<Post> getPostListPageOne(Map<String,String> loginCookie, String tid, Folder folder, TextArea descField) throws Exception	{		
		//Post 목록 추출
		List<Post> postList = new ArrayList<Post>();
		
		Document posts = null;
		
		try	{

			posts = Jsoup.connect("http://cy.cyworld.com/home/" + tid + "/postlist?folderid=" + folder.getId() + "&listsize=10")
				.userAgent(userAgent)
				.header("Referer", "http://cy.cyworld.com/home/" + tid)
				.header("Accept", accept)
				.header("Content-Type", content_type)
				.header("Accept-Encoding", accept_encoding)
				.header("Accept-Language", accept_language)
				.cookies(loginCookie) // 위에서 얻은 '로그인 된' 쿠키
				.ignoreHttpErrors(true).validateTLSCertificates(false).followRedirects(true)
				.get();
		} catch (Exception e)	{
			DescFieldUtil.AppendString(descField, "폴더:" + folder.getName() + "...포스트 목록 가져오기 실패");
		}
		
		if(posts != null)

			for(Element el:posts.select("article"))	{
				Post post = new Post();
				post.setId(el.attr("id").substring(0, el.attr("id").indexOf("_")));
				post.setTitle(el.getElementsByTag("h3").text());
	
				Long createAt = Long.valueOf(el.attr("id").substring(el.attr("id").indexOf("_") + 1));
				Date createDate = new Date(createAt);
				String yyyymm = formatYYYYMM.format(createDate);
				String yyyymmdd = formatYYYYMMDD.format(createDate);
	
				post.setCreateAt(createAt);
				post.setYyyymm(yyyymm);
				post.setYyyymmdd(yyyymmdd);
	
				String style_thumbString = el.getElementsByTag("figure").attr("style");
				//post.setThumb(UrlUtil.getUrlFromStyle(style_thumbString));
				//post.setImgs(getImageFromPost(tid, loginCookie, post));
				post.setFolderId(folder.getId());
				postList.add(post);
	
				//DescFieldUtil.AppendString(descField, "getPostListPageOne:" + post.getYyyymmdd() + ":" + post.getTitle());
	
				//사진가져오기

				getImageFromPost(tid, loginCookie, folder, post, descField);
	
			}

		/*
			Post lastPost_before = postList.get(postList.size() - 1);
			Post lastPost = null;

			for(int i = 0;;i++)	{

				try {

				if(lastPost == null)
					lastPost = lastPost_before;
				else if(lastPost_before.getId().equals(lastPost.getId()))
					break;

				List<Post> morePostList = getMorePostList(tid, loginCookie, folder.getId(), lastPost.getId(), lastPost.getCreateAt(), lastPost.getYyyymm(), descField);
				if(morePostList == null || morePostList.size() < 1)
					break;
				else
					postList.addAll(morePostList);

				lastPost = postList.get(postList.size() - 1);
				} catch (Exception e)	{
					//descField.appendText(e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(3)+(" + i + ")\n");
					//System.out.println("error:" + e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(3)+(" + i + ")\n");
				}
			}
		 */

		//descField.appendText(folder.getName() + ":" + postList.size() + "개의 포스트가 있습니다.\n");

		return postList;
	}

	public List<Post> getMorePostList(String tid, Map<String,String> loginCookie, String folder_id, String lastid, Long lastdate, String lastyymm, Folder folder, TextArea descField)	{

		List<Post> postList = new ArrayList<Post>();

		try {

			Document posts = null;
			
			try {
				
			posts = Jsoup.connect("http://cy.cyworld.com/home/" + tid + "/postmore?folderid=" + folder_id + "&lastid=" + lastid + "&lastdate=" + lastdate + "&lastyymm=" + lastyymm + "&startdate=&enddate=&tagname=&listsize=10")
					.userAgent(userAgent)
					.header("Referer", "http://cy.cyworld.com/home/" + tid)
					.header("Accept", accept)
					.header("Content-Type", content_type)
					.header("Accept-Encoding", accept_encoding)
					.header("Accept-Language", accept_language)
					.cookies(loginCookie) // 위에서 얻은 '로그인 된' 쿠키
					.ignoreHttpErrors(true).validateTLSCertificates(false).followRedirects(true)
					.get();
			} catch (Exception e)	{
				descField.appendText(e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(2-0)\n");
			}

			for(Element el:posts.select("article"))	{

				try {

					Post post = new Post();
					post.setId(el.attr("id").substring(0, el.attr("id").indexOf("_")));
					post.setTitle(el.getElementsByTag("h3").text());

					Long createAt = Long.valueOf(el.attr("id").substring(el.attr("id").indexOf("_") + 1));
					Date createDate = new Date(createAt);
					String yyyymm = formatYYYYMM.format(createDate);
					String yyyymmdd = formatYYYYMMDD.format(createDate);

					post.setCreateAt(createAt);
					post.setYyyymm(yyyymm);
					post.setYyyymmdd(yyyymmdd);

					String style_thumbString = el.getElementsByTag("figure").attr("style");
					//post.setThumb(UrlUtil.getUrlFromStyle(style_thumbString));
					post.setFolderId(folder.getId());
					//post.setImgs(getImageFromPost(tid, loginCookie, post));
					postList.add(post);

					//사진가져오기
					getImageFromPost(tid, loginCookie, folder, post, descField);

					//DescFieldUtil.AppendString(descField, "getMorePostList:" + post.getYyyymmdd() + ":" + post.getTitle());

				} catch (Exception e)	{
					descField.appendText(e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(2-2)\n");
					//descField.appendText(e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(2-2)\n");
				}
			}


		} catch (Exception e)	{
			descField.appendText(e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(2-1)\n");
		}


		return postList;
	}

	public List<String> getImageFromPost(String tid, Map<String,String> loginCookie, Folder folder, Post post, TextArea descField)	{
		ArrayList<String> imgList = new ArrayList<String>();

		try {

			//DescFieldUtil.AppendString(descField, "이미지 목록 가져오는중...");
			
			Document postDoc = null;
			
			try	{

				postDoc = Jsoup.connect("http://cy.cyworld.com/home/" + tid + "/post/" + post.getId())
					.userAgent(userAgent)
					.header("Referer", "http://cy.cyworld.com/home/" + tid)
					.header("Accept", accept)
					.header("Content-Type", content_type)
					.header("Accept-Encoding", accept_encoding)
					.header("Accept-Language", accept_language)
					.cookies(loginCookie) // 위에서 얻은 '로그인 된' 쿠키
					.ignoreHttpErrors(true).validateTLSCertificates(false).followRedirects(true)
					.get();
			} catch (Exception e)	{
				
			}

			//Element postEl = postDoc.getElementById("postData");
			//이미지
			
			if(postDoc != null)	{
			
			Elements imgBoxEls = postDoc.getElementsByClass("post imageBox cyco-imagelet");

			for(Element tmpEl:imgBoxEls)	{

				Elements imgEls = tmpEl.getElementsByTag("img");

				for(Element el:imgEls)	{
					String srctext = el.attr("srctext");
					if(srctext != null)	{
						srctext = URLDecoder.decode(srctext,  "UTF-8");

						if(srctext.indexOf("/file_down.asp") > -1)	{
							srctext = srctext.replace("/file_down.asp",  "/vm_file_down.asp");
						}

						imgList.add(srctext);

						//DescFieldUtil.AppendString(descField, "img:" + srctext);

					}
				}
			}

			//swf
			Elements mediaBoxEls = postDoc.getElementsByClass("post mediaBox cyco-medialet");
			for(Element tmpEl:mediaBoxEls)	{

				Elements imgEls = tmpEl.getElementsByTag("object");

				for(Element el:imgEls)	{
					String srctext = el.attr("data");
					if(srctext != null)	{
						srctext = URLDecoder.decode(srctext,  "UTF-8");

						if(srctext.indexOf("/file_down.asp") > -1)	{
							srctext = srctext.replace("/file_down.asp",  "/vm_file_down.asp");
						}

						imgList.add(srctext);
					}
				}
			}

			String path = new String();
			String filePrefix = new String();

			if(imgList == null || imgList.size() < 1)
				return imgList;
			else	{
				path = "/cyimg_download/" + StringUtil.convertFilename(folder.getDepth1Name()) + "/" + StringUtil.convertFilename(folder.getDepth2Name()) + "/" + StringUtil.convertFilename(folder.getName());
				filePrefix = path +  "/" + post.getYyyymmdd() + "_" + StringUtil.convertFilename(post.getTitle()) + "_";

				File f = new File(path);
				if(!f.exists())
					f.mkdirs();

			}

			//해당 포스트의 이미지 가져오기
			DescFieldUtil.AppendString(descField,  "\n\t(포스트) \"" + post.getTitle() + "\" 처리중: 이미지 " + imgList.size() + "개");
			int imgCnt = 0;
			for(String img:imgList)	{
				System.out.println(imgList.get(imgCnt++));
				try {
					DescFieldUtil.AppendString(descField,  "\t\t(다운로드 " + imgCnt + ") " + filePrefix + StringUtil.convertFilename(getFileName(img)));
					//DescFieldUtil.AppendString(descField,  "\n\t" + img + ":" + filePrefix + StringUtil.convertFilename(getFileName(img)));
					downloadUsingNIO(img,  filePrefix + StringUtil.convertFilename(getFileName(img)));
					/*
					if(imgCnt == 1)
						DescFieldUtil.AppendString(descField, "*" + String.valueOf(imgCnt), false);
					else
						DescFieldUtil.AppendString(descField, ", *" + imgCnt, false);
					*/
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					DescFieldUtil.AppendString(descField,  "\t\t--->(오류-다운로드실패) " + img);
				}
			}
			//DescFieldUtil.AppendString(descField,  post.getTitle() + ":" + imgCnt + "개 이미지 다운로드");

			//0.1초 쉬기 (포스트 하나당)
			Thread.sleep(100);
			}

		} catch (Exception e)	{

			DescFieldUtil.AppendString(descField,  "\t\t(실패) " + post.getTitle() + ":" + imgList.size() + ":" + "http://cy.cyworld.com/home/" + tid + "/post/" + post.getId());
			e.printStackTrace();
		}


		return imgList;
	}

	private String getFileName(String url)	{

		String fileName = UUID.randomUUID().toString() + ".jpg";

		try {

			if(url.indexOf("vm_file_down.asp?redirect=") > -1)	{
				String path = url.substring(url.indexOf("vm_file_down.asp?redirect=") + 26);
				if(path.lastIndexOf('/') > -1)
					fileName = path.substring(path.lastIndexOf('/') + 1);
				else if(path.lastIndexOf("%2F") > -1)
					fileName = path.substring(path.lastIndexOf("%2F") - 1 + 4);
			} else if(url.indexOf("c2down.cyworld.co.kr") > -1)	{
				fileName = url.substring(url.indexOf("&name=") + 6);
			}	
			fileName = URLDecoder.decode(fileName, "UTF-8");
		} catch (Exception e)	{
		}

		return fileName;

	}

	private static void downloadUsingNIO(String urlStr, String file) throws IOException {
		if(urlStr != null && !urlStr.isEmpty())	{

			URL url = new URL(urlStr);
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(file);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			rbc.close();
		}
	}


	public void makePost(String tid, Map<String,String> loginCookie, Post post)	{

		try	{

			Document postDoc = Jsoup.connect("http://cy.cyworld.com/home/" + tid + "/post/" + post.getId())
					.userAgent(userAgent)
					.header("Referer", "http://cy.cyworld.com/home/" + tid)
					.header("Accept", accept)
					.header("Content-Type", content_type)
					.header("Accept-Encoding", accept_encoding)
					.header("Accept-Language", accept_language)
					.cookies(loginCookie) // 위에서 얻은 '로그인 된' 쿠키
					.ignoreHttpErrors(true).validateTLSCertificates(false).followRedirects(true)
					.get();

			System.out.println(postDoc.html());

		} catch (Exception e)	{

		}
	}
}
