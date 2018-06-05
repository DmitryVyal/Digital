/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.lang.Lang;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Helper to import an SVG file
 */
public class SvgImporter {
    private final Document svg;

    /**
     * Create a new importer instance
     *
     * @param svgFile the svg file to import
     * @throws IOException IOException
     */
    public SvgImporter(File svgFile) throws IOException {
        if (!svgFile.exists())
            throw new FileNotFoundException(svgFile.getPath());
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);
        } catch (Exception e) {
            throw new IOException(Lang.get("err_parsingSVG"), e);
        }
    }

    /**
     * Parses and draws the svg file.
     *
     * @return the custom shape description
     * @throws SvgException SvgException
     */
    public CustomShapeDescription create() throws SvgException {
        NodeList gList = svg.getElementsByTagName("svg").item(0).getChildNodes();
        Context c = new Context();
        try {
            CustomShapeDescription csd = new CustomShapeDescription();
            create(csd, gList, c);
            return csd;
        } catch (RuntimeException e) {
            throw new SvgException(Lang.get("err_parsingSVG"), e);
        }
    }

    private void create(CustomShapeDescription csd, NodeList gList, Context c) throws SvgException {
        for (int i = 0; i < gList.getLength(); i++) {
            final Node node = gList.item(i);
            if (node instanceof Element)
                create(csd, (Element) node, c);
        }
    }

    private void create(CustomShapeDescription csd, Element element, Context parent) throws SvgException {
        Context c = new Context(parent, element);
        switch (element.getNodeName()) {
            case "a":
            case "g":
                create(csd, element.getChildNodes(), c);
                break;
            case "line":
                csd.addLine(
                        vec(element.getAttribute("x1"), element.getAttribute("y1"), c).round(),
                        vec(element.getAttribute("x2"), element.getAttribute("y2"), c).round(),
                        c.getThickness(), c.getColor());
                break;
            case "rect":
                drawRect(csd, element, c);
                break;
            case "path":
                try {
                    drawPolygon(csd, c, new PolygonParser(element.getAttribute("d")).create());
                } catch (PolygonParser.ParserException e) {
                    throw new SvgException("invalid path", e);
                }
                break;
            case "polygon":
                try {
                    drawPolygon(csd, c, new PolygonParser(element.getAttribute("points")).parsePolygon());
                } catch (PolygonParser.ParserException e) {
                    throw new SvgException("invalid points", e);
                }
                break;
            case "polyline":
                try {
                    drawPolygon(csd, c, new PolygonParser(element.getAttribute("points")).parsePolyline());
                } catch (PolygonParser.ParserException e) {
                    throw new SvgException("invalid points", e);
                }
                break;
            case "circle":
            case "ellipse":
                drawCircle(csd, element, c);
                break;
            case "text":
                drawText(csd, c, element);
                break;
        }
    }

    private void drawPolygon(CustomShapeDescription csd, Context c, Polygon polygon) {
        if (c.getFilled() != null)
            csd.addPolygon(polygon.transform(c.getTransform()), c.getThickness(), c.getFilled(), true);
        if (c.getColor() != null)
            csd.addPolygon(polygon.transform(c.getTransform()), c.getThickness(), c.getColor(), false);
    }

    private VectorFloat vec(String xStr, String yStr, Context c) {
        return c.getTransform().transform(vec(xStr, yStr));
    }

    private VectorFloat vec(String xStr, String yStr) {
        float x = xStr.isEmpty() ? 0 : Float.parseFloat(xStr);
        float y = yStr.isEmpty() ? 0 : Float.parseFloat(yStr);
        return new VectorFloat(x, y);
    }

    private VectorFloat vecD(String xStr, String yStr, Context c) {
        float x = xStr.isEmpty() ? 0 : Float.parseFloat(xStr);
        float y = yStr.isEmpty() ? 0 : Float.parseFloat(yStr);
        return c.getTransform().getMatrix().transformDirection(new VectorFloat(x, y));
    }

    private void drawRect(CustomShapeDescription csd, Element element, Context c) {
        VectorInterface size = vec(element.getAttribute("width"), element.getAttribute("height"));
        VectorInterface pos = vec(element.getAttribute("x"), element.getAttribute("y"));
        VectorInterface rad = vec(element.getAttribute("rx"), element.getAttribute("ry"));

        float x = pos.getXFloat();
        float y = pos.getYFloat();
        float width = size.getXFloat();
        float height = size.getYFloat();
        if (rad.getXFloat() * rad.getYFloat() != 0) {
            float w = size.getXFloat() - 2 * rad.getXFloat();
            float h = size.getYFloat() - 2 * rad.getYFloat();
            float rx = rad.getXFloat();
            float ry = rad.getYFloat();
            csd.addPolygon(new Polygon(true)
                    .add(c.tr(new VectorFloat(x + rx, y)))
                    .add(c.tr(new VectorFloat(x + rx + w, y)))
                    .add(c.tr(new VectorFloat(x + width, y + ry)))
                    .add(c.tr(new VectorFloat(x + width, y + ry + h)))
                    .add(c.tr(new VectorFloat(x + rx + w, y + height)))
                    .add(c.tr(new VectorFloat(x + rx, y + height)))
                    .add(c.tr(new VectorFloat(x, y + ry + h)))
                    .add(c.tr(new VectorFloat(x, y + ry))), c.getThickness(), c.getColor(), false);
        } else
            csd.addPolygon(new Polygon(true)
                    .add(c.tr(new VectorFloat(x, y)))
                    .add(c.tr(new VectorFloat(x + width, y)))
                    .add(c.tr(new VectorFloat(x + width, y + height)))
                    .add(c.tr(new VectorFloat(x, y + height))), c.getThickness(), c.getColor(), false);
    }

    private void drawCircle(CustomShapeDescription csd, Element element, Context c) {
        VectorFloat r = null;
        if (element.hasAttribute("r")) {
            final String rad = element.getAttribute("r");
            r = vecD(rad, rad, c);
        }
        if (element.hasAttribute("rx")) {
            r = vecD(element.getAttribute("rx"), element.getAttribute("ry"), c);
        }
        if (r != null) {
            VectorFloat pos = vec(element.getAttribute("cx"), element.getAttribute("cy"), c);
            VectorFloat oben = pos.sub(r);
            VectorFloat unten = pos.add(r);

            if (c.getFilled() != null)
                csd.addCircle(oben.round(), unten.round(), c.getThickness(), c.getFilled(), true);
            if (c.getColor() != null)
                csd.addCircle(oben.round(), unten.round(), c.getThickness(), c.getColor(), false);
        }
    }

    private void drawText(CustomShapeDescription csd, Context c, Element element) throws SvgException {
        VectorFloat p = vec(element.getAttribute("x"), element.getAttribute("y"));
        VectorInterface pos0 = p.transform(c.getTransform());
        VectorInterface pos1 = p.add(new VectorFloat(1, 0)).transform(c.getTransform());

        drawTextElement(csd, c, element, pos0, pos1);
    }

    private void drawTextElement(CustomShapeDescription csd, Context c, Element element, VectorInterface pos0, VectorInterface pos1) throws SvgException {
        NodeList nodes = element.getElementsByTagName("*");
        if (nodes.getLength() == 0) {
            String text = element.getTextContent();
            csd.addText(pos0.round(), pos1.round(), text, c.getTextOrientation(), (int) c.getFontSize(), c.getColor());
        } else {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    drawTextElement(csd, new Context(c, el), el, pos0, pos1);
                }
            }
        }
    }

}
