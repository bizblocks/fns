package com.groupstp.fns.web.screens;

import com.groupstp.fns.service.OpenDataService;
import com.haulmont.cuba.gui.components.AbstractWindow;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class Sandbox extends AbstractWindow {

    @Inject
    private OpenDataService openDataService;

    public void onDoitBtnClick() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        openDataService.refreshOpenData();
    }
}