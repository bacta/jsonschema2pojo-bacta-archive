package com.ocdsoft.bacta.soe.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;

/**
 * Created by kburkhardt on 1/4/15.
 */
public class SoeTypeRule implements Rule<JClassContainer, JType> {
    private static final String DEFAULT_TYPE_NAME = "any";

    private final RuleFactory ruleFactory;

    protected SoeTypeRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * When applied, this rule reads the details of the given node to determine
     * the appropriate Java type to return. This may be a newly generated type,
     * it may be a primitive type or other type such as {@link java.lang.String}
     * or {@link java.lang.Object}.
     * <p>
     * JSON schema types and their Java type equivalent:
     * <ul>
     * <li>"type":"any" =&gt; {@link java.lang.Object}
     * <li>"type":"array" =&gt; Either {@link java.util.Set} or
     * <li>"type":"boolean" =&gt; <code>boolean</code>
     * <li>"type":"integer" =&gt; <code>int</code>
     * <li>"type":"null" =&gt; {@link java.lang.Object}
     * <li>"type":"number" =&gt; <code>double</code>
     * <li>"type":"object" =&gt; Generated type (see {@link org.jsonschema2pojo.rules.ObjectRule})
     * {@link java.util.List}, see {@link org.jsonschema2pojo.rules.ArrayRule}
     * <li>"type":"string" =&gt; {@link java.lang.String} (or alternative based on
     * presence of "format", see {@link org.jsonschema2pojo.rules.FormatRule})
     * </ul>
     *
     * @param nodeName
     *            the name of the node for which this "type" rule applies
     * @param node
     *            the node for which this "type" rule applies
     * @param jClassContainer
     *            the package into which any newly generated type may be placed
     * @return the Java type which, after reading the details of the given
     *         schema node, most appropriately matches the "type" specified
     */
    @Override
    public JType apply(String nodeName, JsonNode node, JClassContainer jClassContainer, Schema schema) {

        String propertyTypeName = getTypeName(node);

        JType type;

        if (propertyTypeName.equals("string")) {

            type = jClassContainer.owner().ref(String.class);
        } else if (propertyTypeName.equals("number")) {

            JType typeToUseForNumbers = getNumberType(jClassContainer.owner(), ruleFactory.getGenerationConfig());
            type = unboxIfNecessary(typeToUseForNumbers, ruleFactory.getGenerationConfig());
        } else if (propertyTypeName.equals("integer")) {

            JType typeToUseForIntegers = getIntegerType(jClassContainer.owner(), ruleFactory.getGenerationConfig());
            type = unboxIfNecessary(typeToUseForIntegers, ruleFactory.getGenerationConfig());
        } else if (propertyTypeName.equals("short")) {

            JType typeToUseForShorts = jClassContainer.owner().ref(Short.class);
            type = unboxIfNecessary(typeToUseForShorts, ruleFactory.getGenerationConfig());

        } else if (propertyTypeName.equals("byte")) {

            JType typeToUseForBytes = jClassContainer.owner().ref(Byte.class);
            type = unboxIfNecessary(typeToUseForBytes, ruleFactory.getGenerationConfig());
        } else if (propertyTypeName.equals("boolean")) {

            type = unboxIfNecessary(jClassContainer.owner().ref(Boolean.class), ruleFactory.getGenerationConfig());
        } else if (propertyTypeName.equals("object") || (node.has("properties") && node.path("properties").size() > 0)) {

            type = ruleFactory.getObjectRule().apply(nodeName, node, jClassContainer.getPackage(), schema);
        } else if (propertyTypeName.equals("array")) {

            type = ruleFactory.getArrayRule().apply(nodeName, node, jClassContainer.getPackage(), schema);
        } else {

            type = jClassContainer.owner().ref(Object.class);
        }

        if (node.has("format")) {
            type = ruleFactory.getFormatRule().apply(nodeName, node.get("format"), type, schema);
        } else if(propertyTypeName.equals("string") && node.has("media")) {
            type = ruleFactory.getMediaRule().apply(nodeName, node.get("media"), type, schema);
        }

        return type;
    }

    private String getTypeName(JsonNode node) {
        if (node.has("type") && node.get("type").isArray() && node.get("type").size() > 0) {
            return node.get("type").get(0).asText();
        }

        if (node.has("type")) {
            return node.get("type").asText();
        }

        return DEFAULT_TYPE_NAME;
    }

    private JType unboxIfNecessary(JType type, GenerationConfig config) {
        if (config.isUsePrimitives()) {
            return type.unboxify();
        } else {
            return type;
        }
    }

    private JType getIntegerType(JCodeModel owner, GenerationConfig config) {
        if (config.isUseLongIntegers()) {
            return owner.ref(Long.class);
        } else {
            return owner.ref(Integer.class);
        }
    }

    private JType getNumberType(JCodeModel owner, GenerationConfig config) {
        if (config.isUseDoubleNumbers()) {
            return owner.ref(Double.class);
        } else {
            return owner.ref(Float.class);
        }
    }
}
