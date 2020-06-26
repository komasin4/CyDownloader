package com.komasin4.cydownloader.view;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.komasin4.cydownloader.scapping.CyScrapping;
import com.komasin4.cydownloader.util.DescFieldUtil;
import com.komasin4.cydownloader.util.StringUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import com.komasin4.cydownloader.model.Folder;
import com.komasin4.cydownloader.model.Post;

public class mainView extends Application {


	//		class CookieCheckScheduledJob extends TimerTask {
	//			public void run() {
	//				
	//				
	//				
	//			}
	//		}

	//public static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
	//public static final String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
	//public static final String content_type = "application/x-www-form-urlencoded";
	//public static final String accept_encoding = "gzip, deflate, br";
	//public static final String accept_language = "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4";
	public static final DateFormat df = new SimpleDateFormat("yyyyMMdd");
	//public static final String url = "http://cyxso.cyworld.com/Login.sk?loginsrc=redirect&redirection%3Dhttp%3A%2F%2Fclub.cyworld.com%2Fclub%2Fclubsection2%2Fhome.asp";
	public static final String url = "https://helpdesk.cyworld.com/myContact.html?loginsrc=redirect&redirection=http://club.cyworld.com";

	//private static final boolean bTest = true;
	private static final boolean bTest = false;

	Pane root = new Pane();
	Scene scene = new Scene(root);
	TextArea descField = new TextArea();
	Button buttonCookie = new Button("쿠키확인");
	Button buttonGetPhoto = new Button("사진 가져오기");
	Button buttonRetry = new Button("다시 로그인");
	Button buttonTest = new Button("테스트");
	WebView browser = new WebView();
	WebEngine webEngine = browser.getEngine();

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		buttonCookie.setPrefSize(100, 20);
		buttonGetPhoto.setPrefSize(100, 20);
		buttonRetry.setPrefSize(100, 20);

		descField.relocate(10, 10);
		buttonCookie.relocate(890, 10);
		buttonGetPhoto.relocate(890, 40);
		buttonRetry.relocate(890, 70);
		browser.relocate(10, 110);

		descField.setStyle("-fx-border-color:black; -fx-padding:3px;");
		//descField.setPrefHeight(680);
		descField.setPrefHeight(87);
		descField.setPrefWidth(870);

		browser.setPrefWidth(970);

		root.getChildren().addAll(buttonCookie, buttonGetPhoto, buttonRetry, browser, descField);

		stage.setTitle("JavaFX WebView (o7planning.org)");
		stage.setScene(scene);
		stage.setWidth(1024);
		stage.setHeight(768);

		stage.show();

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {     
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
						return null;
					} 
					public void checkClientTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) {
					} 
					public void checkServerTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				} 
		}; 

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL"); 
			sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
		} 
		// Now you can access an https URL without having the certificate in the truststore
		try { 
			URL url = new URL("https://hostname/index.html"); 
		} catch (MalformedURLException e) {
		} 
		//now you can load the content:

		//webEngine.load("https://example.com");		

		webEngine.load(url);
		descField.appendText("로그인후 빈 화면이 나타나면 오른쪽의 '사진 가져오기' 버튼 클릭\n");
		
		
		//쿠키확인 버튼 이벤트 처리
		buttonCookie.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
//				long startTime = System.currentTimeMillis();
//				
//				for (; ; )	{
					
//					long checkTime = startTime;
//					int checkCnt = 0;

					//DescFieldUtil.AppendString(descField, new Date().toGMTString());

					Map<String,String> loginCookie = new HashMap<String,String>();
					String tid = new String();

					CookieManager manager = new CookieManager();
					CookieHandler cookieHandler = manager.getDefault();
					Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
					List<String> cookieList = new ArrayList<String>();
					try {
						tempMap = cookieHandler.get(URI.create(url), new HashMap<String, List<String>>());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					cookieList = tempMap.get("Cookie");

					String cookieString = cookieList.get(0);
					String[] cookieStringList = cookieString.split(";");

					for(String a : cookieStringList) {
						String[] t = a.split("=");
						loginCookie.put(t.length < 1?"":t[0].trim(),  
								t.length < 2?"":t[1].trim());
					}


					if(loginCookie.get("CFN") == null || loginCookie.get("CFN").isEmpty())	{
//						if(System.currentTimeMillis() - checkTime > 1000)	{
							DescFieldUtil.AppendString(descField, new Date() + ":쿠키가 없습니다. 로그인이 될때 까지 기다리세요.");
//							checkTime = System.currentTimeMillis();
//						}
					} else {
						DescFieldUtil.AppendString(descField, "CFN cookie:" + loginCookie.get("CFN").substring(1,50) + "....");
//						break;
					}

//					if(System.currentTimeMillis() - startTime > 30000)	{
//						DescFieldUtil.AppendString(descField, "로그인이 원활하지 않습니다. 다시 시도해 주세요.");
//						break;
//					}
				}
//			}
		});

		//사진가져오기 버튼 이벤트 처리
		buttonGetPhoto.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				CookieManager manager = new CookieManager();
				descField.setPrefHeight(680);
				startGetPhoto(manager);
			}
		});

		//다시로그인 버튼 이벤트 처리
		buttonRetry.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				descField.setPrefHeight(55);
				//browser.setVisible(true);
				webEngine.load(url);
				descField.setText("로그인후 빈 화면이 나타나면 오른쪽의 '사진 가져오기' 버튼 클릭\n");
			}
		});

		buttonTest.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				CookieManager manager = new CookieManager();
				descField.setPrefHeight(680);
				startGetPhotoTest(manager);
			}
		});

		descField.textProperty().addListener( new ChangeListener <String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				int location = descField.getText().length();
				Platform.runLater(() -> {
					descField.positionCaret(location);
				});
			}
		});
	}

	public void startGetPhoto(CookieManager manager) {

		Runnable task = new Runnable()
		{
			public void run()
			{
				//runGetPhoto(descField, url, manager);
				if(bTest)
					runGetPhotoTest(manager);
				else
					runGetPhoto(manager);
			}
		};

		// Run the task in a background thread
		Thread backgroundThread = new Thread(task);
		// Terminate the running thread if the application exits
		backgroundThread.setDaemon(true);
		// Start the thread
		backgroundThread.start();

	}

	public void runGetPhoto(CookieManager manager) 
	{

		Map<String,String> loginCookie = new HashMap<String,String>();
		String tid = new String();
		CyScrapping cyScrapping = new CyScrapping();

		DescFieldUtil.AppendString(descField, "쿠키 추출중...");

		try	{
			//쿠키 가져오기
			CookieHandler cookieHandler = manager.getDefault();
			Map<String, List<String>> tempMap = cookieHandler.get(URI.create(url), new HashMap<String, List<String>>());
			List<String> cookieList = tempMap.get("Cookie");

			String cookieString = cookieList.get(0);
			String[] cookieStringList = cookieString.split(";");

			for(String a : cookieStringList) {
				String[] t = a.split("=");
				loginCookie.put(t.length < 1?"":t[0].trim(),  
						t.length < 2?"":t[1].trim());
			}


			if(loginCookie.get("CFN") == null || loginCookie.get("CFN").isEmpty())	{
				DescFieldUtil.AppendString(descField, "쿠키가 없습니다.");
				DescFieldUtil.AppendString(descField, "'다시 로그인' 버튼을 누른후 로그인 화면에서 다시 로그인을 시도 하세요.");
				return;
			} else {
				DescFieldUtil.AppendString(descField, "CFN cookie:" + loginCookie.get("CFN").substring(1,50) + "....");
			}

			//tid 가져오기
			DescFieldUtil.AppendString(descField, "user key 가져 오는 중....");

			tid = cyScrapping.getTid(loginCookie);

			DescFieldUtil.AppendString(descField, "userkey:" + tid);

			if(tid == null || tid.isEmpty())	{
				DescFieldUtil.AppendString(descField, "userkey를 알 수 없어 사진을 가져올 수 없습니다.");
				return;
			}

			DescFieldUtil.AppendString(descField, "폴더 리스트를 가져오는 중....");


			List<Folder> folderList = new ArrayList<Folder> (); 
			List<Post> postListAll = new ArrayList<Post> (); 

			try {
				//folderList = getFolderList(loginCookie, tid);
				folderList = cyScrapping.getFolderList(loginCookie, tid, descField);

				/*
				for(Folder folder:folderList) {
					descField.appendText(folder.getDepth1Name() + "/" + folder.getDepth2Name() + "/" + folder.getName() + "\n");
				}
				 */

				DescFieldUtil.AppendString(descField, "총 " + folderList.size() + "개의 폴더가 있습니다.\n");

			} catch (Exception e)	{
				DescFieldUtil.AppendString(descField, e.getMessage() + "\n" + "폴더 목록을 가져오는 중 오류가 발생하여 사진을 가져올 수 없습니다.");
				return;
			}

			if(folderList == null || folderList.size() < 1)	{
				DescFieldUtil.AppendString(descField, "사진폴더가 없습니다.");
				return;
			}

			//DescFieldUtil.AppendString(descField, "포스트 목록을 가져옵니다....");

			for(Folder folder:folderList)	{

				String folderName = folder.getDepth1Name() + "/" + folder.getDepth2Name() + "/" + folder.getName();
				DescFieldUtil.AppendString(descField, "(폴더) \"" + folderName + "\" 처리중.");

				int page = 1;

				List<Post> postList = cyScrapping.getPostListPageOne(loginCookie, tid, folder, descField);

				//DescFieldUtil.AppendString(descField, folderName + " 폴더의  포스트 목록을 가져옵니다.(page-" + page + ")");

				try {
					/*
					for(Post post:postListPageOne)	{
						//DescFieldUtil.AppendString(descField, post.getYyyymm() + ":" + post.getTitle());
						//System.out.println(post.getCreateAt()!=null?df.format(post.getCreateAt()):"" + ":" + post.getTitle());
					}
					 */

					if(postList == null || postList.size() < 1)	{
						DescFieldUtil.AppendString(descField, " - 포스트가 없습니다.");
						continue;
					}

					postListAll.addAll(postList);

					DescFieldUtil.AppendString(descField, "\n(폴더) " + folder.getName() + ": " + page + " 페이지 처리 완료");

					Post lastPost_before = postList.get(postList.size() - 1);
					Post lastPost = null;

					for(int i = 0;;i++)	{

						try {

							if(lastPost == null)
								lastPost = lastPost_before;
							else if(lastPost_before.getId().equals(lastPost.getId()))
								break;

							page++;

							descField.appendText("\n(폴더) " + folder.getName() + ": " + page + " 페이지 가져오는 중");

							List<Post> morePostList = cyScrapping.getMorePostList(tid, loginCookie, folder.getId(), lastPost.getId(), lastPost.getCreateAt(), lastPost.getYyyymm(), folder, descField);
							if(morePostList == null || morePostList.size() < 1)
								break;
							else	{
								postList.addAll(morePostList);
								postListAll.addAll(morePostList);
							}

							lastPost = postList.get(postList.size() - 1);

							DescFieldUtil.AppendString(descField, "\n(폴더) " + folder.getName() + ": " + page + " 페이지 처리 완료");

						} catch (Exception e)	{
							//descField.appendText(e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(3)+(" + i + ")\n");
							//System.out.println("error:" + e.getMessage() + "\n 포스트 목록 처리중 오류가 발생하였습니다.(3)+(" + i + ")\n");
							DescFieldUtil.AppendString(descField, e.getMessage() + "\n" + folderName + " 폴더의 게시물 목록을 가져오는 중 오류가 발생하였습니다.(2)");
						}
						
						//DescFieldUtil.AppendString(descField, ", " + page, page%30==0?true:false);
					}

				} catch (Exception e) {
					e.printStackTrace();
					DescFieldUtil.AppendString(descField, e.getMessage() + "\n" + folderName + " 폴더의 게시물 목록을 가져오는 중 오류가 발생하였습니다.(1)");
				}

				folder.setPostCount(postList.size());
				DescFieldUtil.AppendString(descField, "\n(폴더-처리완료) " + folder.getName() + " " + postList.size() + "개의 포스트.\n");
			}

			DescFieldUtil.AppendString(descField, "\n전체 포스트 " + postListAll.size() + "개 처리 완료.");

		} catch (Exception e)	{
			DescFieldUtil.AppendString(descField, "오류가 발생하였습니다.");
		}

		//

		DescFieldUtil.AppendString(descField, "처리가 완료 되었습니다.");

		/*
    	for(int i = 0 ; i < 1000 ; i++)	{
    		try {
    			final String status = "Processing " + i + " of " + 10;
       			System.out.println(status);
       			DescFieldUtil.AppendString(descField, status);
                Thread.sleep(100);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
		 */

		/*    	
        for(int i = 1; i <= 10; i++) 
        {
            try
            {
                // Get the Status
                final String status = "Processing " + i + " of " + 10;

                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        statusLabel.setText(status);
                    }
                });

                textArea.appendText(status+"\n");

                Thread.sleep(1000);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
		 */
	}


	public void startGetPhotoTest(CookieManager manager) {

		Runnable task = new Runnable()
		{
			public void run()
			{
				//runGetPhoto(descField, url, manager);
				runGetPhotoTest(manager);
			}
		};

		// Run the task in a background thread
		Thread backgroundThread = new Thread(task);
		// Terminate the running thread if the application exits
		backgroundThread.setDaemon(true);
		// Start the thread
		backgroundThread.start();

	}

	public void runGetPhotoTest(CookieManager manager) 
	{

		Map<String,String> loginCookie = new HashMap<String,String>();
		String tid = new String();
		CyScrapping cyScrapping = new CyScrapping();

		DescFieldUtil.AppendString(descField, "쿠키 추출중...");

		try	{
			//쿠키 가져오기
			CookieHandler cookieHandler = manager.getDefault();
			Map<String, List<String>> tempMap = cookieHandler.get(URI.create(url), new HashMap<String, List<String>>());
			List<String> cookieList = tempMap.get("Cookie");

			String cookieString = cookieList.get(0);
			String[] cookieStringList = cookieString.split(";");

			for(String a : cookieStringList) {
				String[] t = a.split("=");
				loginCookie.put(t.length < 1?"":t[0].trim(),  
						t.length < 2?"":t[1].trim());
			}


			if(loginCookie.get("CFN") == null || loginCookie.get("CFN").isEmpty())	{
				DescFieldUtil.AppendString(descField, "쿠키가 없습니다.");
				DescFieldUtil.AppendString(descField, "'다시 로그인' 버튼을 누른후 로그인 화면에서 다시 로그인을 시도 하세요.");
				return;
			} else {
				DescFieldUtil.AppendString(descField, "CFN cookie:" + loginCookie.get("CFN").substring(1,50) + "....");
			}

			//tid 가져오기
			DescFieldUtil.AppendString(descField, "user key 가져 오는 중....");

			tid = cyScrapping.getTid(loginCookie);

			DescFieldUtil.AppendString(descField, "userkey:" + tid);

			if(tid == null || tid.isEmpty())	{
				DescFieldUtil.AppendString(descField, "userkey를 알 수 없어 사진을 가져올 수 없습니다.");
				return;
			}

			//특정 포스트의 이미지 리스트
			//https://cy.cyworld.com/home/12669268/post/4299EC282E800182A2A86401/layer
			Post postTemp = new Post();

			postTemp.setId("4299EC282E800182A2A86401");

			cyScrapping.makePost("12669268", loginCookie, postTemp);


			DescFieldUtil.AppendString(descField, "처리가 완료 되었습니다.");
		} catch (Exception e)	{

		}

	}
}
