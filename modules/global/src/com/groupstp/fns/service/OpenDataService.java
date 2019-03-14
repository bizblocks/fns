package com.groupstp.fns.service;


import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public interface OpenDataService {
    String NAME = "fns_OpenDataService";

    /**
     * Запускает процесс обновления открытх данных ФНС
     */
    void refreshOpenData() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException;
}