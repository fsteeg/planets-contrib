//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.06.30 at 01:03:16 PM GMT+01:00 
//


package eu.planets_project.fedora.planetsdatastream;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for datastreamtype.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="datastreamtype">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="managed"/>
 *     &lt;enumeration value="external"/>
 *     &lt;enumeration value="inline"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "datastreamtype")
@XmlEnum
public enum Datastreamtype {

    @XmlEnumValue("managed")
    MANAGED("managed"),
    @XmlEnumValue("external")
    EXTERNAL("external"),
    @XmlEnumValue("inline")
    INLINE("inline");
    private final String value;

    Datastreamtype(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Datastreamtype fromValue(String v) {
        for (Datastreamtype c: Datastreamtype.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
