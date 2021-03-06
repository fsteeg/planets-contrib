package eu.planets_project.services.migration.kakadu;

import eu.planets_project.services.datatypes.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author onbscs
 */
public class BooleanParameter extends ServiceParameter {

    String trueStrVal;
    String falseStrVal;
    Logger log = Logger.getLogger(BooleanParameter.class.getName());

    public BooleanParameter(Parameter parameter, String paramPattern, String trueStrVal, String falseStrVal) {
        super(parameter, paramPattern);
        this.trueStrVal = trueStrVal;
        this.falseStrVal = falseStrVal;
    }

    @Override
    public boolean validate() {
        if (requestValue.equalsIgnoreCase("true") || requestValue.equalsIgnoreCase("false")) {
            log.info("validate - Parameter '" + getParameter().getName() + "' with value '" + requestValue + "' is valid.");
            return true;
        } else {
            log.severe("validate - Parameter '" + getParameter().getName() + "' with value '" + requestValue + "' is not valid.");
            return false;
        }
    }

    @Override
    protected String getStatusMessage() {
        return "Value " + "'" + requestValue + "' of " +
                "parameter " + "'" + getParameter().getName() + "' is" +
                (valid ? "" : " not") + " a valid boolean value (true/false).";
    }

    @Override
    protected List<String> getCommandListItems() {
        String val;
        if (requestValue.equalsIgnoreCase("true")) {
            val = trueStrVal;
        } else {
            val = falseStrVal;
        }
        String result = String.format(paramPattern, val);
        List<String> command = new ArrayList<String>();
        command.add(result);
        log.info("Parameter pattern substitution - pattern: " + paramPattern + ", value: " + val +", result: " + result);
        return command;
    }

    @Override
    public void setRequestValue(String requestValue) {
        this.requestValue = requestValue;
    }
}
