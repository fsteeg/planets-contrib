//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.10 at 03:21:55 PM CEST 
//


package eu.planets_project.ifr.core.services.comparison.comparator.config.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for labValType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="labValType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="int"/>
 *     &lt;enumeration value="XCLLabel"/>
 *     &lt;enumeration value="string"/>
 *     &lt;enumeration value="rational"/>
 *     &lt;enumeration value="time"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "labValType")
@XmlEnum
public enum LabValType {

    @XmlEnumValue("int")
    INT("int"),
    @XmlEnumValue("XCLLabel")
    XCL_LABEL("XCLLabel"),
    @XmlEnumValue("string")
    STRING("string"),
    @XmlEnumValue("rational")
    RATIONAL("rational"),
    @XmlEnumValue("time")
    TIME("time");
    private final String value;

    LabValType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LabValType fromValue(String v) {
        for (LabValType c: LabValType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
