package com.groupstp.fns.core.bean.fnsupdate;

import com.groupstp.fns.core.bean.FnsUpdateWorker;
import com.groupstp.fns.entity.Taxpayer;
import com.groupstp.fns.entity.TaxpayersType;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import com.sun.mail.iap.ByteArray;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component("fns_FnsUpdateWorker")
public class FnsUpdateWorkerBean implements FnsUpdateWorker {

    private static final Logger log = LoggerFactory.getLogger(FnsUpdateWorker.class);

    List<HashMap<String, String>> taxpayers;

    @Inject
    private DataManager dataManager;

    @Override
    public void refreshTaxpayers() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        taxpayers = new ArrayList<>();
        String openDataFileName = downloadFNSOpenData();
        List<File> fileList = unpackFNSOpenData(openDataFileName);
//        List<File> fileList = new ArrayList<>();
//        File file = new File("/home/lobo/projects/rtneo/fns/deploy/tomcat/temp/data-03102019-structure-08012016.zip_unzip/VO_RRMSPSV_0000_9965_20190310_032fab49-653a-480a-a879-38f7739930ce.xml");
//        File file = new File("/home/lobo/projects/rtneo/fns/deploy/tomcat/temp/1.xml");
//        fileList.add(file);
        parseFNSOpenData(fileList);
        updateDatabase();
    }

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    private void updateDatabase() {
        Transaction tx = persistence.createTransaction();

        try {
            EntityManager em = persistence.getEntityManager();
            int i=0;
            for (HashMap<String, String> tp: taxpayers) {
                if(em.find(Taxpayer.class, tp.get("inn"))!=null)
                    continue;
                Taxpayer taxpayer = metadata.create(Taxpayer.class);
                tp.forEach((k, v) -> {
                    if("type".equals(k))
                        taxpayer.setValue(k, TaxpayersType.fromId(v));
                    else
                        taxpayer.setValue(k, v);
                });
                em.persist(taxpayer);
                i++;
                if(i % 1000 == 0)
                {
                    tx.commitRetaining();
                    em = persistence.getEntityManager();
                }
            }
            tx.commit();
        } finally {
            tx.end();
        }
    }

    private String getAddokved(Node doc)
    {
        List<String> strings = new ArrayList<>();
        NodeList okved = doc.getChildNodes().item(2).getChildNodes();
        for(int i=0;i<okved.getLength();i++)
        {
            if("СвОКВЭДОсн".equals(okved.item(i).getNodeName()))
                continue;
            strings.add(okved.item(i).getAttributes().getNamedItem("КодОКВЭД").getNodeValue());
        }
        return StringUtils.join(strings.toArray(), ",");
    }

    private String getAddress(Node doc)
    {
        try {
            List<String> addrComponents = new ArrayList<>();
            NodeList addrNodes = doc.getChildNodes().item(1).getChildNodes();
            for(int i=0;i<addrNodes.getLength();i++)
            {
                Node addrNode = addrNodes.item(i);
                addrComponents.add(addrNode.getAttributes().getNamedItem("Тип")+"="+addrNode.getAttributes().getNamedItem("Наим"));
            }
            return StringUtils.join(addrComponents, ",");
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private void updateLegal(Node doc) {
        String inn = doc.getFirstChild().getAttributes().getNamedItem("ИННЮЛ").getNodeValue();
        String descr = doc.getFirstChild().getAttributes().getNamedItem("НаимОрг").getNodeValue();
        HashMap<String, String> obj = new HashMap<>();
        obj.put("inn", inn);
        obj.put("description", descr);
        try{
            obj.put("okved", doc.getChildNodes().item(2).getFirstChild().getAttributes().getNamedItem("КодОКВЭД").getNodeValue());
        }
        catch (Exception e){
            obj.put("okved", "");
        }
        try {
            obj.put("addOkved", getAddokved(doc));
        }
        catch (Exception e){
            obj.put("addOkved", "");
        }

        obj.put("address", getAddress(doc));
        obj.put("type", TaxpayersType.LEGAL.getId());
        obj.put("fio", "");
        taxpayers.add(obj);
    }

    private void updateEntrepreneur(Node doc){
        String inn = doc.getFirstChild().getAttributes().getNamedItem("ИННФЛ").getNodeValue();
        NamedNodeMap fio = doc.getFirstChild().getFirstChild().getAttributes();
        HashMap<String, String> obj = new HashMap<>();
        obj.put("inn", inn);
        if(fio.getNamedItem("Отчество")==null)
            obj.put("fio", fio.getNamedItem("Фамилия").getNodeValue()+" "+fio.getNamedItem("Имя").getNodeValue());
        else
            obj.put("fio", fio.getNamedItem("Фамилия").getNodeValue()+" "+fio.getNamedItem("Имя").getNodeValue()+" "+fio.getNamedItem("Отчество").getNodeValue());
        try{
            obj.put("okved", doc.getChildNodes().item(2).getFirstChild().getAttributes().getNamedItem("КодОКВЭД").getNodeValue());
        }
        catch (Exception e){
            obj.put("okved", "");
        }
        try {
            obj.put("addOkved", getAddokved(doc));
        }
        catch (Exception e){
            obj.put("addOkved", "");
        }

        obj.put("address", getAddress(doc));
        obj.put("addOkved", getAddokved(doc));
        obj.put("description", obj.get("fio"));
        obj.put("type", TaxpayersType.ENTERPRENEUR.getId());
        taxpayers.add(obj);
    }

    private void parseFNSOpenData(List<File> fileList) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        int fileCounter = 0;
        for (File file : fileList) {
            fileCounter++;
            log.debug("Parsing file {}, {} of {}", file.getPath(), fileCounter, fileList.size());
            org.w3c.dom.Document doc = builder.parse(file);

            XPathExpression expression = xPath.compile("//Документ/СведМН[@КодРегион='38']");
            //XPathExpression expression = xPath.compile("//Док/aaa[@bbb='38']");
            NodeList nodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
            for(int i=0;i<nodeList.getLength();i++)
            {
                Node node = nodeList.item(i);
                Node docNode = node.getParentNode();
                if("2".equals(docNode.getAttributes().getNamedItem("ВидСубМСП").getNodeValue()))
                {
                    updateEntrepreneur(docNode);
                }
                else
                {
                    updateLegal(docNode);
                }
            }
        }
    }

    private List<File> unpackFNSOpenData(String openDataFileName) throws IOException {
        File file = new File(openDataFileName+"_unzip");
        file.mkdirs();

        ZipFile zip = new ZipFile(openDataFileName);
        Enumeration entries = zip.entries();
        List<File> files = new ArrayList<>();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File unzipped = new File(file.getAbsolutePath(), entry.getName());
            write(zip.getInputStream(entry),
                    new BufferedOutputStream(new FileOutputStream(
                            unzipped)));
            files.add(unzipped);
        }
        return files;
    }

    private void write(InputStream in, BufferedOutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
        in.close();
    }

    final int BUFFER_SIZE = 1024*1204;

    private String downloadFNSOpenData() throws IOException, ParserConfigurationException, SAXException {
        String openDataUrl = getOpenDataUrl();
        //openDataUrl = "http://billstest.groupstp.ru/exch/bills.zip";
        URL url = new URL(openDataUrl);

        String tempDir = FileUtils.getTempDirectoryPath();
        String [] fileNameComponents = url.getFile().split("/");
        File file = new File(tempDir+File.separator+fileNameComponents[fileNameComponents.length-1]);
        if(file.exists())
            return file.getAbsolutePath();
        FileUtils.copyURLToFile(url, file);
        return file.getAbsolutePath();
    }

    @Nullable
    private String getOpenDataUrl() throws IOException {
        String openDataUrl = "https://www.nalog.ru/opendata/7707329152-rsmp/";
        log.debug("Parsing opendata page "+openDataUrl);

        AtomicReference<String> res = new AtomicReference<>();

        Document doc = Jsoup.connect(openDataUrl).get();
        Elements tds = doc.select("td");
        tds.forEach(element -> {
            String inner = element.text();
            if(!"Гиперссылка (URL) на набор".equals(inner))
                return;
            Element a = element.parent().child(2).child(0);
            res.set(a.attr("href"));
        });

        return res.get();
    }

    private ByteArray downloadFile(String url)
    {
        StopWatch sw = new Slf4JStopWatch(log);
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate tmplt = new RestTemplate();
            tmplt.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            ResponseEntity<byte[]> res = tmplt.getForEntity(url, byte[].class);
            return new ByteArray(res.getBody(), 0, res.getBody().length);
        } catch (Exception e) {
            log.error("Failed download file "+url, e);
            throw e;
        } finally {
            sw.stop("FnsUpdateWorker", "downloadFile \""+url+"\" finished");
        }

    }

}
