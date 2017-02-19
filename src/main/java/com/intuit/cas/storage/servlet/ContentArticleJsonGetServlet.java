package com.intuit.cas.storage.servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


@Component(
        metatype = false,
        immediate = true
)
@SlingServlet(
        generateComponent = false,
        selectors = {"ttarticle"},
        resourceTypes = {"turbotax/article"},
        extensions = {"json"},
        methods = {"GET"}
)
public class ContentArticleJsonGetServlet extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(ContentArticleJsonGetServlet.class);

    private void addJsonArrayValue(JSONWriter jsonWriter, List<String> arrylist) {
        try {

            JSONWriter jsonarry = jsonWriter.array();
            Iterator<String> iter = arrylist.iterator();
            while (iter.hasNext()) {
                jsonarry.value(iter.next());
            }

            jsonarry.endArray();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void addJsonArticleBody(JSONWriter jsonWriter, Resource parentResource) throws JSONException {
        JSONWriter jsonarry = jsonWriter.array();
        Node parentRes = parentResource.adaptTo(Node.class);
        try {
            Node bodyNode  = parentRes.getNode("body");
                NodeIterator childnodeIter = bodyNode.getNodes();
                while (childnodeIter.hasNext()) {
                    Node childNode = childnodeIter.nextNode();

                        jsonWriter.object()
                                .key("expandable")
                                .value(childNode.hasProperty("expandable") ? childNode.getProperty("expandable").getBoolean() : false)

                                .key("title")
                                .value(childNode.hasProperty("title") ? childNode.getProperty("title").getString() : "")

                                .key("content")
                                .value(childNode.hasProperty("content") ? childNode.getProperty("content").getString() : "")

                                .endObject();



            }
            jsonWriter.endArray();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }

    private void addJsonRelatedArticle(JSONWriter jsonWriter, Resource parentResource) throws JSONException {
        JSONWriter jsonarry = jsonWriter.array();
        Node parentRes = parentResource.adaptTo(Node.class);
        try {
           Node relarticleNode  = parentRes.getNode("relatedarticles");
            NodeIterator childnodeIter = relarticleNode.getNodes();
                while (childnodeIter.hasNext()) {
                    Node childNode = childnodeIter.nextNode();

                        jsonWriter.object()

                                .key("title")
                                .value(childNode.hasProperty("title") ? childNode.getProperty("title").getString() : "")
                                .key("fullPath")
                                .value(childNode.hasProperty("fullpath") ? childNode.getProperty("fullpath").getString() : "")


                                .key("documentId")
                                .value(childNode.hasProperty("documentId") ? childNode.getProperty("documentId").getString() : "")

                                .key("ffaSafe")
                                .value(childNode.hasProperty("ffaSafe") ? childNode.getProperty("ffaSafe").getBoolean() : false)

                                .key("noFollow")
                                .value(childNode.hasProperty("noFollow") ? childNode.getProperty("noFollow").getBoolean() : false)


                                .endObject();



            }
            jsonWriter.endArray();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Resource parentResource = request.getResource();
        ValueMap vm = parentResource.adaptTo(ValueMap.class);
        JSONWriter jsonWriter = new JSONWriter(response.getWriter());
        List<String> usergroupsel = java.util.Arrays.asList(vm.get("userGroupSelection", String[].class));
        List<String> repositoryViewList = java.util.Arrays.asList(vm.get("repositoryView", String[].class));
        List<String> categoriesList = java.util.Arrays.asList(vm.get("categories", String[].class));

        try {


            jsonWriter.object()
                    .key("docId")
                    .value(parentResource.getName())


                    .key("docType")
                    .value(vm.get("docType", String.class))

                    .key("createDate")
                    .value(vm.get("jcr:created", String.class))

                    .key("lastModifiedDate")
                    .value(vm.get("jcq:lastModified", String.class))


                    .key("title")
                    .value(vm.get("jcr:title", String.class))

                    .key("fullPath")
                    .value(vm.get("fullPath", String.class))

                    .key("ffaSafe")
                    .value(vm.get("ffSage", false))

                    .key("hideFromSearch")
                    .value(vm.get("hideFromSearch", false))

                    .key("experience")
                    .value(vm.get("experience", ""))

                    .key("locale")
                    .value(null)
                    .key("view")
                    .value(null)

                    .key("body");
            addJsonArticleBody(jsonWriter, parentResource);
            jsonWriter.key("relatedArticles");
            addJsonRelatedArticle(jsonWriter, parentResource);

            jsonWriter.key("userGroupSelection");

            addJsonArrayValue(jsonWriter, usergroupsel);

            jsonWriter.key("repositoryView");

            addJsonArrayValue(jsonWriter, repositoryViewList);

            jsonWriter.key("categories");

            addJsonArrayValue(jsonWriter, categoriesList);

            jsonWriter.key("shortTitle")
                    .value(vm.get("shortTitle", ""))
                    .key("fullDescription")
                    .value(vm.get("fullDescription", ""))
                    .key("description")
                    .value(vm.get("description", ""));

            jsonWriter.endObject();


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}