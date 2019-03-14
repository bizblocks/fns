package com.groupstp.fns.core.bean;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public interface FnsUpdateWorker {
    String NAME = "fns_FnsUpdateWorker";

    /**
     * Запускает процесс обновления БД налогоплательщиков
     * скачивание архива с сайта ФНС, распаковка, поиск по БД, добавление и обновление сведений
     */
    void refreshTaxpayers() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException;
}
