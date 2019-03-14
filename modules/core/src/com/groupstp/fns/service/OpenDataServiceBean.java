package com.groupstp.fns.service;

import com.groupstp.fns.core.bean.FnsUpdateWorker;
import com.haulmont.cuba.core.global.AppBeans;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

@Service(OpenDataService.NAME)
public class OpenDataServiceBean implements OpenDataService {
    @Override
    public void refreshOpenData() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        FnsUpdateWorker worker = AppBeans.get(FnsUpdateWorker.class);
        worker.refreshTaxpayers();
    }
}