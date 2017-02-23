package com.intuit.cas.storage.listener;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.felix.scr.annotations.*;
import org.apache.jackrabbit.oak.commons.PropertiesUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import org.apache.http.auth.AuthScope;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import org.apache.http.impl.client.HttpClientBuilder;
import java.util.Base64;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.impl.client.DefaultHttpClient;
import java.util.ArrayList;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.impl.client.HttpClient;

/**
 * A component to Pricing card panels up to date
 */


@Component(
        immediate = true,
        metatype = false,
        label = " Rendering Engine Service",
        description = "Rendering Engine service "
)

public class RenderingEngineListener implements EventListener {
    private static final String DEFAULT_PRODUCT = "productData";
    private static final String DEFAULT_PROPERTY = "sling:resourceType";
    private static final String DEFAULT_PATH =  "pluto/components/content/ecommerce/productcomponent";
    private static final String DEFAULT_DIR = "/content/turbotax";
    private static final Logger log = LoggerFactory.getLogger(RenderingEngineListener.class);
    ArrayList<NameValuePair> postParameters;
    @Property(label = "Product Feature", description = "Feature that identifies product node", value = DEFAULT_PRODUCT)
    private static final String PRODUCT_FEATURE =  DEFAULT_PRODUCT  ;


    @Property(label = "Target Property", description = "Property that must be changed", value = DEFAULT_PROPERTY)
    private static final String TARGET_PROPERTY = DEFAULT_PROPERTY ;

    @Property(label = "Path", description = "Path to resourceType", value = DEFAULT_PATH)
    private static final String RESOURCE_PATH =  DEFAULT_PATH ;

    @Property(label = "Affected Directory", description = "Path to affected directory", value = DEFAULT_DIR )
    private static final String DIR_PATH = DEFAULT_DIR;




    @Reference
    private SlingRepository repository;

    private Session session;

    private ObservationManager observationManager;

    private String product;
    private String property;
    private String path;
    private String dir;


    private String findParentPage(Node childNode) throws RepositoryException {
        if(childNode.getPath().equals("/content")) {
            return null;
        }
        else if ( childNode.hasProperty("sling:resourceType") && childNode.getProperty("sling:resourceType").getString().equals("turbotax/article")) {
            return childNode.getPath();

        }   else {
            return findParentPage(childNode.getParent());
        }
    }

    private void pageModified(Node childNode) throws Exception{
       					 String server = "http://50.112.29.244/wordpress";
                        String uri = "/?rest_route=/wp/v2/pages/"+childNode.getProperty("page_id").getString();
                        log.error("id value:"+childNode.getProperty("page_id").getString());
                      
                       DefaultHttpClient httpclient = new DefaultHttpClient();
                        String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
                        HttpPost httppost = new HttpPost(server+uri);
                        httppost.setHeader("Authorization", "Basic " + encoding);
                         postParameters = new ArrayList<NameValuePair>();
                        postParameters.add(new BasicNameValuePair("title",childNode.getProperty("page_title").getString()));
                        postParameters.add(new BasicNameValuePair("content",childNode.getProperty("page_content").getString()));
                        httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                        log.error("title:"+childNode.getProperty("page_title").getString());
                        log.error("Content:"+childNode.getProperty("page_content").getString());
                        log.info("executing request " + httppost.getRequestLine());
                        HttpResponse response = httpclient.execute(httppost);
                      //  PostMethod post = new PostMethod("http://" + server + uri);
                       // post.setRequestHeader("CQ-Action", "Activate");
                       // post.setRequestHeader("CQ-Handle", handle);
                        
                       
                       // post.releaseConnection();
                        //log the results
                         log.error("result: " + response.getStatusLine().getStatusCode());
    }
      private void commentModified(Node childNode) throws Exception{
        				String server = "http://50.112.29.244/wordpress";
                        String uri = "/?rest_route=/wp/v2/comments/"+childNode.getProperty("user_ID").getString();
                        log.error("id value:"+childNode.getProperty("user_ID").getString());
                      
                       DefaultHttpClient httpclient = new DefaultHttpClient();
                        String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
                        HttpPost httppost = new HttpPost(server+uri);
                        httppost.setHeader("Authorization", "Basic " + encoding);
                         postParameters = new ArrayList<NameValuePair>();
                       
                        postParameters.add(new BasicNameValuePair("content",childNode.getProperty("comment_content").getString()));
                        httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                        log.error("comment_post_Id:"+childNode.getProperty("comment_post_ID").getString());
                        log.error("Content:"+childNode.getProperty("comment_content").getString());
                        log.info("executing request " + httppost.getRequestLine());
                        HttpResponse response = httpclient.execute(httppost);
                      //  PostMethod post = new PostMethod("http://" + server + uri);
                       // post.setRequestHeader("CQ-Action", "Activate");
                       // post.setRequestHeader("CQ-Handle", handle);
                        
                       
                       // post.releaseConnection();
                        //log the results
                         log.error("result: " + response.getStatusLine().getStatusCode());
    }
    
    public void onEvent(EventIterator it) {
        log.error("ENTERED HANDLER");
        while (it.hasNext()) {
            Event event = it.nextEvent();
            try {
                if(event.getType() == Event.NODE_ADDED || event.getType() == Event.PROPERTY_CHANGED) {
                    try {
                        log.error("**************** Path changed :: " + event.getPath());
                        String invalidatedpath = event.getPath();

                        //retrieve the request parameters
                        String handle = invalidatedpath;
                        String parentPagepath = invalidatedpath.substring(0,invalidatedpath.lastIndexOf("/"));
                        Node childNode = session.nodeExists(invalidatedpath) ? session.getNode(invalidatedpath) : session.getNode(parentPagepath);

                        String page = findParentPage(childNode);
                        if(invalidatedpath.contains("page")){
                            pageModified(childNode);
                        }
                        else if(invalidatedpath.contains("post-comment")){
                            commentModified(childNode);
                        }
                        else{

                        //hard-coding connection properties is a bad practice, but is done here to simplify the example
                       String server ="http://50.112.29.244/wordpress";
					   String uri = "/?rest_route=/wp/v2/posts/"+childNode.getProperty("post_id").getString();
                        log.error("id value:"+childNode.getProperty("post_id").getString());
                      
                       DefaultHttpClient httpclient = new DefaultHttpClient();
                        String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
                        
                        
                        
                        HttpPost httppost = new HttpPost(server+uri);
                        httppost.setHeader("Authorization", "Basic " + encoding);
                         postParameters = new ArrayList<NameValuePair>();
                        postParameters.add(new BasicNameValuePair("title",childNode.getProperty("post_title").getString()));
                        postParameters.add(new BasicNameValuePair("content",childNode.getProperty("post_content").getString()));
                        httppost.setEntity(new UrlEncodedFormEntity(postParameters));
                        log.error("title:"+childNode.getProperty("post_title").getString());
                        log.error("Content:"+childNode.getProperty("post_content").getString());
                        log.info("executing request " + httppost.getRequestLine());
                        HttpResponse response = httpclient.execute(httppost);
                      //  PostMethod post = new PostMethod("http://" + server + uri);
                       // post.setRequestHeader("CQ-Action", "Activate");
                       // post.setRequestHeader("CQ-Handle", handle);
                        
                       
                       // post.releaseConnection();
                        //log the results
                         log.error("result: " + response.getStatusLine().getStatusCode());
                        }
                       // log.error("result: " + response.getResponseBodyAsString());
                    } catch (RepositoryException e) {
                        log.error("Repsotiry Exception : " ,e);
                    }
                }
                }catch(Exception e){
                    log.error("Flushcache servlet exception: " ,e);
                }
        }
    }

    @Activate
    protected void activate() {
        this.product = PropertiesUtil.toString(PRODUCT_FEATURE,DEFAULT_PRODUCT);
        this.property = PropertiesUtil.toString(TARGET_PROPERTY,DEFAULT_PROPERTY );
        this.path  = PropertiesUtil.toString(RESOURCE_PATH, DEFAULT_PATH  );
        this.dir = PropertiesUtil.toString(DIR_PATH,DEFAULT_DIR);

        try {
            session = repository.loginAdministrative(null);
            observationManager = session.getWorkspace().getObservationManager();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        /*
         * Event types are bitwise OR'ed. The last parameter prevents changes done by this session to be sent to the
         * listener (which would result in an endless loop in this case)
         */

            try {
                observationManager.addEventListener(this, Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.NODE_ADDED , this.dir, true, null, null, true);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }

    }

    @Deactivate
    protected void deactivate() {

        this.product = DEFAULT_PRODUCT;
        this.property = DEFAULT_PROPERTY;
        this.path  = DEFAULT_PATH;
        this.dir = DEFAULT_DIR;
        try {
            if (observationManager != null) {
                observationManager.removeEventListener(this);
            }
        }
        catch (RepositoryException re) {
            log.error("Error removing the DAM Listener", re);
        }
        finally {
            if (session != null) {
                session.logout();
                session = null;
            }
        }
    }
}
