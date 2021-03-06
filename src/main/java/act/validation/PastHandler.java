package act.validation;

import act.ActComponent;
import act.app.ActionContext;
import act.controller.ActionMethodParamAnnotationHandlerPlugin;
import org.osgl.$;
import org.osgl.util.C;

import javax.validation.constraints.Past;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Set;

@ActComponent
public class PastHandler extends ActionMethodParamAnnotationHandlerPlugin {

    @Override
    public Set<Class<? extends Annotation>> listenTo() {
        Set<Class<? extends Annotation>> set = C.newSet();
        set.add(Past.class);
        return set;
    }

    @Override
    public void handle(String paramName, Object paramVal, Annotation annotation, ActionContext context) {
        if (paramVal == null) return;
        if (paramVal instanceof Date) {
            Date date = (Date)paramVal;
            if (date.getTime() < $.ms()) return;
        }
        Past theAnno = (Past) annotation;
        context.addViolation(new ActionMethodParamConstraintViolation<Object>(paramVal, theAnno.message(), theAnno, context));
    }

}
