/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.util.xml;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import org.mmbase.bridge.*;
import java.util.*;

import org.mmbase.util.logging.*;
import org.mmbase.util.xml.XMLWriter;


/**
 * Uses the XML functions from the bridge to construct a DOM document representing MMBase data structures.
 *
 * @author Michiel Meeuwissen
 * @author Eduard Witteveen
 * @version $Id$
 * @since  MMBase-1.6
 */
public class Generator {

    private static final Logger LOG = Logging.getLoggerInstance(Generator.class);

    public final static String NAMESPACE =  "http://www.mmbase.org/xmlns/objects";
    private final static String DOCUMENTTYPE_PUBLIC =  "-//MMBase//DTD objects config 1.0//EN";
    private final static String DOCUMENTTYPE_SYSTEM = "http://www.mmbase.org/dtd/objects_1_0.dtd";

    private Document document = null;
    private final DocumentBuilder documentBuilder;
    private Cloud cloud = null;

    private boolean namespaceAware = false;

    private long buildCost = 0; // ns
    private int  size      = 0;

    /**
     * To create documents representing structures from the cloud, it
     * needs a documentBuilder, to construct the DOM Document, and the
     * cloud from which the data to be inserted will come from.
     *
     * @param documentBuilder The DocumentBuilder which will be used to create the Document.
     * @param cloud           The cloud from which the data will be.
     * @see   org.mmbase.util.xml.DocumentReader#getDocumentBuilder()
     */
    public Generator(DocumentBuilder documentBuilder, Cloud cloud) {
        if (documentBuilder == null) throw new IllegalArgumentException();
        this.documentBuilder = documentBuilder;
        this.cloud = cloud;

    }

    public Generator(DocumentBuilder documentBuilder) {
        this(documentBuilder, null);
    }

    public Generator(Document doc) {
        if (doc == null) throw new IllegalArgumentException();
        document = doc;
        documentBuilder = null;
        namespaceAware = document.getDocumentElement().getNamespaceURI() != null;
    }

    /**
     * Returns an estimation on how long it took to construct the document.
     * @return a duration in nanoseconds.
     * @since MMBase-1.9
     */
    public long getCost() {
        return buildCost;
    }
    /**

     * The number of presented MMBase nodes in the document.
     * @since MMBase-1.9
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the working DOM document.
     * @return The document, build with the operations done on the generator class
     */
    public  Document getDocument() {
        if (document == null) {
            long start = System.nanoTime();
            DOMImplementation impl = documentBuilder.getDOMImplementation();
            document = impl.createDocument(namespaceAware ? NAMESPACE : null,
                                           "objects",
                                           impl.createDocumentType("objects", DOCUMENTTYPE_PUBLIC, DOCUMENTTYPE_SYSTEM)
                                           );
            if (cloud != null) {
                addCloud();
            }
            buildCost += System.nanoTime() - start;
        }
        return document;
    }

    /**
     * If namespace aware, element are created with the namespace http://www.mmbase.org/objects,
     * otherwise, without namespace.
     * @since MMBase-1.8
     */
    public void setNamespaceAware(boolean n) {
        if (document != null) throw new IllegalStateException("Already started constructing");
        namespaceAware = n;
    }

    /**
     * @since MMBase-1.8
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * @since MMBase-1.8
     */
    protected Element createElement(String name) {
        getDocument();
        if (namespaceAware) {
            return document.createElementNS(NAMESPACE, name);
        } else {
            return document.createElement(name);
        }

    }
    protected final void setAttribute(Element element, String name, String value) {
        // attributes normally have no namespace. You can assign one, but then they will always have
        // to be indicated explicitely (in controdiction to elements).
        // So attributes are created without namespace.
        /*
        if (namespaceAware) {
            element.setAttributeNS(NAMESPACE, name, value);
        } else {
            element.setAttribute(name, value);
        }
        */
        element.setAttribute(name, value);
    }


    protected final String getAttribute(Element element, String name) {
        // see setAttribute
        /*
        if (namespaceAware) {
            return element.getAttributeNS(NAMESPACE, name);
        } else {
            return element.getAttribute(name);
        }
        */
        return element.getAttribute(name);
    }

    /**
     * Returns the document as a String.
     * @return the xml generated as an string
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Returns the document as a String.
     * @param ident if the string has to be idented
     * @return the generated xml as a (formatted) string
     */
    public String toString(boolean ident) {
        return XMLWriter.write(document, ident);
    }

    private void addCloud() {
        setAttribute(document.getDocumentElement(), "cloud", cloud.getName());
    }

    /**
     * Adds a field to the DOM Document. This means that there will
     * also be added a Node if this is necessary.
     * @param node An MMbase bridge Node.
     * @param fieldDefinition An MMBase bridge Field.
     */
    public Element add(org.mmbase.bridge.Node node, Field fieldDefinition) {
        long start = System.nanoTime();
        getDocument();
        if (cloud == null) {
            cloud = node.getCloud();
            addCloud();
        }

        Element object = getNode(node);

        if (! (object.getFirstChild() instanceof Element)) {
            LOG.warn("Cannot find first field of " + XMLWriter.write(object, false));
            buildCost += System.nanoTime() - start;
            return object;
        }
        // get the field...
        Element field = (Element)object.getFirstChild();
        while (field != null && !fieldDefinition.getName().equals(getAttribute(field, "name"))) {
            field = (Element)field.getNextSibling();
        }
        // when not found, we are in a strange situation..
        if(field == null) throw new BridgeException("field with name: " + fieldDefinition.getName() + " of node " + node.getNumber() + " with  nodetype: " + fieldDefinition.getNodeManager().getName() + " not found, while it should be in the node skeleton.. xml:\n" + toString(true));
        // when it is filled (allready), we can return
        if (field.getTagName().equals("field")) {
            buildCost += System.nanoTime() - start;
            return field;
        }

        // was not filled, so fill it... first remove the unfilled
        Element filledField = createElement("field");

        field.getParentNode().replaceChild(filledField, field);
        field = filledField;
        // the name
        setAttribute(field, "name", fieldDefinition.getName());
        // now fill it with the new info...
        // format
        setAttribute(field, "format", getFieldFormat(fieldDefinition));
        // the value
        switch (fieldDefinition.getType()) {
        case Field.TYPE_XML :
            Document doc = node.getXMLValue(fieldDefinition.getName());
            // only fill the field, if field has a value..
            if (doc != null) {
                // put the xml inside the field...
                field.appendChild(importDocument(field, doc));
            }
            break;
        case Field.TYPE_BINARY :
            org.mmbase.util.transformers.Base64 transformer = new org.mmbase.util.transformers.Base64();
            field.appendChild(document.createTextNode(transformer.transform(node.getByteValue(fieldDefinition.getName()))));
            break;
        case Field.TYPE_DATETIME :
            // shoudlw e use ISO_8601_LOOSE here or ISO_8601_UTC?
            field.appendChild(document.createTextNode(org.mmbase.util.Casting.ISO_8601_LOOSE.get().format(node.getDateValue(fieldDefinition.getName()))));
            break;
        default :
            field.appendChild(document.createTextNode(node.getStringValue(fieldDefinition.getName())));
        }

        // or do we need more?
        buildCost += System.nanoTime() - start;
        return field;
    }

    /**
     * Adds one Node to a DOM Document.
     * @param node An MMBase bridge Node.
     */
    public Element add(org.mmbase.bridge.Node node) {
        // process all the fields..
        NodeManager nm = node.getNodeManager();
        FieldIterator i = nm.getFields(NodeManager.ORDER_CREATE).fieldIterator();
        while (i.hasNext()) {
            Field field = i.nextField();
            if (field.getType() != Field.TYPE_BINARY) {
                add(node, field);
            }
        }
        return getNode(node);
    }

    /**
     * Adds one Relation to a DOM Document.
     * @param relation An MMBase bridge Node.
     */
    public Element add(Relation relation) {
        return add((org.mmbase.bridge.Node)relation);

    }

    /**
     * Adds a whole MMBase bridge NodeList to the DOM Document.
     * @param nodes An MMBase bridge NodeList.
     */
    public void add(List<? extends org.mmbase.bridge.Node> nodes) {
        for (org.mmbase.bridge.Node n : nodes) {
            if (n instanceof Relation) {
                add((Relation) n);
            } else {
                add(n);
            }
        }
    }


    /**
     * Creates an Element which represents a bridge.Node with all fields unfilled.
     * @param node MMbase node
     * @return Element which represents a bridge.Node
     */
    private Element getNode(org.mmbase.bridge.Node node) {

        // if we are a relation,.. behave like one!
        // why do we find it out now, and not before?
        Element object = getDocument().getElementById("" + node.getNumber());

        if (object != null) {
            return object;
        }

        // if it is a realtion... first add source and destination attributes..
        // can only happen after the node = node.getCloud().getNode(node.getNumber()); thing!
        if (node instanceof Relation) {
            Relation relation = (Relation)node;
            getNode(relation.getSource()).appendChild(createRelationEntry(relation, relation.getSource()));
            getNode(relation.getDestination()).appendChild(createRelationEntry(relation, relation.getDestination()));
        }

        // node didnt exist, so we need to create it...
        object = createElement("object");
        size++;

        setAttribute(object, "id", "" + node.getNumber());
        object.setIdAttribute("id", true);
        setAttribute(object, "type", node.getNodeManager().getName());
        StringBuilder ancestors = new StringBuilder(" "); // having spaces before and after the attribute's value, makes it easy to use xsl's 'contains' function.
        if (! node.getNodeManager().getName().equals("object")) {
            NodeManager parent = node.getNodeManager();
            do {
                parent = parent.getParent();
                ancestors.append(parent.getName());
                ancestors.append(" ");
            } while(! parent.getName().equals("object"));
        }
        setAttribute(object, "ancestors", ancestors.toString());

        // and the otype (type as number)
        setAttribute(object, "otype", node.getStringValue("otype"));

        // add the fields (empty)
        // While still having 'unfilledField's
        // you know that the node is not yet presented completely.

        for (Field fieldDefinition :  node.getNodeManager().getFields(NodeManager.ORDER_CREATE)) {
            Element field = createElement("unfilledField");
            // the name
            setAttribute(field, "name", fieldDefinition.getName());
            // add it to the object
            object.appendChild(field);
        }
        document.getDocumentElement().appendChild(object);
        return object;
    }

    /**
     * Imports an XML document as a value of a field. Can be any XML, so the namespace is set.
     *
     * @param fieldElement The Element describing the field
     * @param toImport     The Document to set as the field's value
     * @return             The fieldContent.
     */
    private Element importDocument(Element fieldElement, Document toImport) {
        DocumentType dt = toImport.getDoctype();
        String tagName = toImport.getDocumentElement().getTagName();

        String namespace;
        if (dt != null) {
            namespace = dt.getSystemId();
        } else {
            namespace = "http://www.mmbase.org/xmlns/" + tagName;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("using namepace: " + namespace);
        }
        Element fieldContent = (Element)document.importNode(toImport.getDocumentElement(), true);
        fieldContent.setAttribute("xmlns", namespace);
        fieldElement.appendChild(fieldContent);
        return fieldContent;
    }

    @SuppressWarnings("fallthrough")
    private String getFieldFormat(Field field) {
        switch (field.getType()) {
        case Field.TYPE_XML :
            return "xml";
        case Field.TYPE_STRING :
            return "string";
        case Field.TYPE_NODE :
            return "object"; // better would be "node" ?
        case Field.TYPE_INTEGER :
        case Field.TYPE_LONG :
            // was it a builder?
            String fieldName = field.getName();
            String guiType = field.getGUIType();

            // I want a object database type!
            if (fieldName.equals("otype")
                || fieldName.equals("number")
                || fieldName.equals("snumber")
                || fieldName.equals("dnumber")
                || fieldName.equals("rnumber")
                || fieldName.equals("role")
                || guiType.equals("reldefs")) {
                    return "object"; // better would be "node" ?
            }
            if (guiType.equals("eventtime")) {
                return "date";
            }
        case Field.TYPE_FLOAT :
        case Field.TYPE_DOUBLE :
            return "numeric";
        case Field.TYPE_BINARY :
            return "bytes";
        case Field.TYPE_DATETIME:
            return "datetime";
        case Field.TYPE_BOOLEAN:
            return "boolean";
        case Field.TYPE_LIST:
            return "list";
        default :
            throw new RuntimeException("could not find field-type for:" + field.getType() + " for field: " + field);
        }
    }

    private Element createRelationEntry(Relation relation, org.mmbase.bridge.Node relatedNode) {
        Element fieldElement = createElement("relation");
        // we have to know what the relation type was...
        org.mmbase.bridge.Node reldef = cloud.getNode(relation.getStringValue("rnumber"));

        setAttribute(fieldElement, "object", "" + relation.getNumber());

        if (relation.getSource().getNumber() == relatedNode.getNumber()) {
            setAttribute(fieldElement, "role", reldef.getStringValue("sname"));
            setAttribute(fieldElement, "related", "" + relation.getDestination().getNumber());
            setAttribute(fieldElement, "type", "source");
        } else {
            setAttribute(fieldElement, "role", reldef.getStringValue("dname"));
            setAttribute(fieldElement, "related", "" + relation.getSource().getNumber());
            setAttribute(fieldElement, "type", "destination");
        }
        return fieldElement;
    }
}
