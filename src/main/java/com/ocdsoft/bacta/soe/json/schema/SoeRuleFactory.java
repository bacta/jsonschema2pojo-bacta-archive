package com.ocdsoft.bacta.soe.json.schema;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;

/**
 * Created by kburkhardt on 1/4/15.
 */
public class SoeRuleFactory extends RuleFactory {

    @Override
    public Rule<JClassContainer, JType> getTypeRule() {
        return new SoeTypeRule(this);
    }

}
